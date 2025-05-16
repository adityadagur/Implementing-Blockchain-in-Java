BlockchainVEB - A Blockchain Implementation with vEB Tree Indexing
Overview
BlockchainVEB is a Java implementation of a blockchain system that incorporates a van Emde Boas (vEB) tree for efficient indexing and searching of blocks. This project demonstrates how blockchain technology can be combined with advanced data structures to create a secure and efficient record-keeping system.

Features
Blockchain Implementation: Stores records in an immutable, chained structure

vEB Tree Indexing: Provides O(log log n) time complexity for search operations

Data Encryption: Uses XOR encryption for sensitive data protection

CSV Integration: Supports loading and saving blocks from/to CSV files

Tamper Detection: Includes blockchain validation mechanism

User Interface: Interactive console menu for system interaction

Technical Components
1. Block Structure
Each block contains:

Roll number (unique identifier)

Name (encrypted)

Date of birth (encrypted)

Timestamp

Previous block's hash

Block key (hashed)

Pointer to next block

2. vEB Tree Implementation
The vEB tree is used to:

Quickly check if a roll number exists

Efficiently find available roll numbers for new blocks

Support O(log log n) time complexity for operations

3. Cryptographic Features
SHA-256 hashing for block integrity

XOR-based symmetric encryption for sensitive data

Hash chaining for blockchain immutability

How to Use
Check Existing Data:

Enter your roll number and block key to view your encrypted/decrypted information

Add New Block:

Provide name, date of birth, and an 8-digit block key

The system automatically assigns an available roll number

Validate Blockchain:

Verify the integrity of the entire blockchain

Data Persistence:

New blocks are automatically saved to "random_entries.csv"

Existing blocks are loaded from the CSV file on startup

Implementation Details
Key Classes
Block: Represents a single block in the blockchain

VEBTree: van Emde Boas tree implementation for efficient indexing

BlockchainVEB: Main class with all blockchain operations

Important Methods
calculateHash(): Computes SHA-256 hash of a block

encrypt()/decrypt(): XOR-based data protection

hashKey(): Creates hash from block key

isBlockchainValid(): Verifies blockchain integrity

Requirements
Java 8 or later

Standard Java libraries only (no external dependencies)

Usage Example
java
// The system provides an interactive console menu:
// 1. Check your data
// 2. Enter a new block
// 3. Check if blockchain is tampered
// 4. Exit
Performance Characteristics
Block insertion: O(1) + vEB tree insertion O(log log u)

Block lookup: O(1) with hash map, O(log log u) with vEB tree

Blockchain validation: O(n) where n is number of blocks

Security Notes
Uses simple XOR encryption for demonstration purposes (not suitable for production)

SHA-256 hashing provides cryptographic integrity

Each block contains the hash of the previous block for chaining

Limitations
In-memory storage (blocks are lost when program terminates except for CSV)

Simple XOR encryption can be easily broken

Limited error handling for edge cases

Future Enhancements
Implement proper cryptographic encryption

Add database persistence

Implement peer-to-peer network functionality

Add more comprehensive error handling

Implement Merkle trees for efficient verification

This implementation serves as an educational demonstration of blockchain concepts combined with advanced data structures.
