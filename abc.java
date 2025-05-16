import java.util.*;
import java.io.*;
import java.nio.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;

public class BlockchainVEB {
    private static String currentHash;
    private static final int MAX_NAME_LENGTH = 100;
    private static final int HASH_LENGTH = 65; // 64 chars + null terminator
    private static final int MAX_BLOCK_KEY = 100000000; // 8-digit block key
    private static final int VEB_UNIVERSE = 131072; // For 5-digit numbers
    private static final String KEY = "supersecret"; // Encryption key
    
    // Block structure
    static class Block {
        int rollNo;
        String name;
        String dob;
        long timestamp;
        String previousHash;
        int blockKey;
        Block next;
        
        public Block(int rollNo, String name, String dob, int blockKey, String previousHash) {
            this.rollNo = rollNo;
            this.name = encrypt(name);
            this.dob = encrypt(dob);
            this.blockKey = blockKey;
            this.previousHash = (previousHash != null) ? previousHash : "0";
            this.timestamp = Instant.now().getEpochSecond();
            this.next = null;
        }
    }
    
    // vEB Tree implementation
    static class VEBTree {
        int size;
        int min, max;
        VEBTree summary;
        VEBTree[] clusters;
        
        public VEBTree(int size) {
            this.size = size;
            this.min = this.max = -1;
            
            if (size > 2) {
                int sqrtSize = (int) Math.ceil(Math.sqrt(size));
                this.summary = new VEBTree(sqrtSize);
                this.clusters = new VEBTree[sqrtSize];
            }
        }
        
        private int high(int x) {
            return x / (int) Math.ceil(Math.sqrt(size));
        }
        
        private int low(int x) {
            return x % (int) Math.ceil(Math.sqrt(size));
        }
        
        public boolean contains(int x) {
            if (x == min || x == max) return true;
            if (size <= 2) return false;
            
            int highX = high(x);
            if (clusters[highX] == null) return false;
            return clusters[highX].contains(low(x));
        }
        
        public void insert(int x) {
            if (min == -1) {
                min = max = x;
            } else {
                if (x < min) {
                    int temp = x;
                    x = min;
                    min = temp;
                }
                
                if (size > 2) {
                    int highX = high(x);
                    int lowX = low(x);
                    
                    if (clusters[highX] == null) {
                        clusters[highX] = new VEBTree((int) Math.ceil(Math.sqrt(size)));
                    }
                    
                    if (clusters[highX].min == -1) {
                        summary.insert(highX);
                        clusters[highX].min = clusters[highX].max = lowX;
                    } else {
                        clusters[highX].insert(lowX);
                    }
                }
                
                if (x > max) {
                    max = x;
                }
            }
        }
    }
    
    // Blockchain implementation
    private static Block blockchain = null;
    private static VEBTree vebTree = new VEBTree(VEB_UNIVERSE);
    private static Map<Integer, Block> blockMap = new HashMap<>();
    
    // Helper methods
    private static String encrypt(String data) {
        StringBuilder encrypted = new StringBuilder();
        for (int i = 0; i < data.length(); i++) {
            encrypted.append((char) (data.charAt(i) ^ KEY.charAt(i % KEY.length())));
        }
        return encrypted.toString();
    }
    
    private static String decrypt(String data) {
        return encrypt(data); // XOR is symmetric
    }
    
    private static int hashKey(int key) {
        int hash = 5381;
        byte[] bytes = ByteBuffer.allocate(4).putInt(key).array();
        
        for (byte b : bytes) {
            hash = ((hash << 5) + hash) + b;
        }
        
        return hash;
    }
    
    private static String calculateHash(Block block) {
        try {
            String combined = block.rollNo + block.name + block.dob + block.timestamp + block.previousHash;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(combined.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            
            // Pad with zeros if needed
            while (hexString.length() < 64) {
                hexString.append('0');
            }
            
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }
    
    private static Block createBlock(int rollNo, String name, String dob, int blockKey, String previousHash) {
        Block newBlock = new Block(rollNo, name, dob, hashKey(blockKey), previousHash);
        currentHash = calculateHash(newBlock);
        blockMap.put(rollNo, newBlock);
        return newBlock;
    }
    
    private static void appendBlock(Block newBlock) {
        if (blockchain == null) {
            blockchain = newBlock;
        } else {
            Block current = blockchain;
            while (current.next != null) {
                current = current.next;
            }
            current.next = newBlock;
        }
    }
    
    private static void loadBlocksFromCSV() {
        try (BufferedReader br = new BufferedReader(new FileReader("random_entries.csv"))) {
            br.readLine(); // Skip header
            
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    int id = Integer.parseInt(parts[0]);
                    String name = parts[1];
                    String date = parts[2];
                    int num = Integer.parseInt(parts[3]);
                    
                    appendBlock(createBlock(id, name, date, num, currentHash));
                    vebTree.insert(id);
                }
            }
        } catch (IOException e) {
            System.out.println("Could not read file: " + e.getMessage());
        }
    }
    
    private static void saveBlockToCSV(int rollNo, String name, String dob, int blockKey) {
        try (PrintWriter pw = new PrintWriter(new FileWriter("random_entries.csv", true))) {
            pw.println(rollNo + "," + name + "," + dob + "," + blockKey);
        } catch (IOException e) {
            System.out.println("Failed to save block: " + e.getMessage());
        }
    }
    
    private static boolean isBlockchainValid() {
        Block current = blockchain;
        while (current != null && current.next != null) {
            String calculatedHash = calculateHash(current);
            if (!calculatedHash.equals(current.next.previousHash)) {
                System.out.println("Block " + current.rollNo + " is not valid");
                return false;
            }
            System.out.println("Block " + current.rollNo + " is valid");
            current = current.next;
        }
        return true;
    }
    
    public static void main(String[] args) {
        System.out.println("Welcome to the blockchain demo!");
        
        // Initialize with some blocks
        appendBlock(createBlock(12345, "Aditya", "2011-07-19", 12345678, null));
        vebTree.insert(12345);
        
        appendBlock(createBlock(54321, "Dagur", "2012-11-13", 87654321, currentHash));
        vebTree.insert(54321);
        
        // Load additional blocks from CSV
        loadBlocksFromCSV();
        
        // Main menu
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("--------------------");
            System.out.println("Menu:");
            System.out.println("1. Check your data");
            System.out.println("2. Enter a new block");
            System.out.println("3. Check if blockchain is tampered");
            System.out.println("4. Exit");
            System.out.print("Enter your choice: ");
            
            int choice = scanner.nextInt();
            System.out.println("--------------------");
            
            switch (choice) {
                case 1:
                    System.out.print("Enter your roll number: ");
                    int rollNo = scanner.nextInt();
                    
                    if (vebTree.contains(rollNo)) {
                        System.out.print("Enter your block key: ");
                        int blockKey = scanner.nextInt();
                        
                        Block block = blockMap.get(rollNo);
                        if (block.blockKey == hashKey(blockKey)) {
                            System.out.println("--------------------");
                            System.out.println("Welcome " + decrypt(block.name));
                            System.out.println("Name: " + decrypt(block.name));
                            System.out.println("DOB: " + decrypt(block.dob));
                            System.out.println("Hash: " + block.previousHash);
                            System.out.println("Encrypted Block key: " + block.blockKey);
                        } else {
                            System.out.println("--------------------");
                            System.out.println("Invalid block key (encrypted data)");
                            System.out.println("Name: " + block.name);
                            System.out.println("DOB: " + block.dob);
                            System.out.println("Hash: " + block.previousHash);
                            System.out.println("Encrypted Block key: " + block.blockKey);
                        }
                    } else {
                        System.out.println("User not found");
                    }
                    break;
                    
                case 2:
                    scanner.nextLine(); // Consume newline
                    System.out.print("Enter your name: ");
                    String name = scanner.nextLine();
                    
                    System.out.print("Enter your date of birth (YYYY-MM-DD): ");
                    String dob = scanner.next();
                    
                    System.out.print("Enter an 8-digit block key: ");
                    int newBlockKey = scanner.nextInt();
                    
                    // Find first available roll number
                    int newRollNo = -1;
                    for (int i = 10001; i <= 99999; i++) {
                        if (!vebTree.contains(i)) {
                            newRollNo = i;
                            break;
                        }
                    }
                    
                    if (newRollNo != -1) {
                        appendBlock(createBlock(newRollNo, name, dob, newBlockKey, currentHash));
                        vebTree.insert(newRollNo);
                        saveBlockToCSV(newRollNo, name, dob, newBlockKey);
                        System.out.println("New block created successfully with roll no " + newRollNo + "!");
                    } else {
                        System.out.println("Could not find available roll number");
                    }
                    break;
                    
                case 3:
                    if (isBlockchainValid()) {
                        System.out.println("Blockchain is not tampered");
                    } else {
                        System.out.println("Blockchain is tampered");
                    }
                    break;
                    
                case 4:
                    System.out.println("Exiting...");
                    scanner.close();
                    return;
                    
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
}