# DistributedLedger

# Organization of code

**The code is divided into:**

## Data structures
At this part the definition of all the needed data structures for the project are found eg. Ledger, Block, Transaction, Transaction input, …etc.
The data structures are self contained and they have the methods to validate themselves.
## Crypto and Hash:
RSA is used for asymmetric cryptography
SHA256 was used for hashing
## Communication:
A Publish subscribe network where events as request for ledger, broadcast transaction or broadcast block occur.
As clients and miners listen to the announcements they act accordingly
This is abstracted by the use of a process object that encapsulates sockets needed by a thread to communicate with others.
## Client
This process is responsible for the manufacture of valid transactions based on the available UTXOs so far in the ledger and broadcasts it to the miners.
It listens to the incoming blocks then check if the ledger accepts them.
## Miners
A miner is a process that listens to the incoming transactions from clients and places them in a block.
When the block is complete the miner is places a special transaction which is the mining reward and then starts mining the block according to the algorithm

**POW:** Change the nonce until the block header hash becomes < 2^(256-difficulty_level) then broadcast the block

**BFT:** Send the block to a leader who starts the election process.

# Main functions
## In the Data structure Part:
### In the Ledger:
- AddToPartitionTree
Which handles forking in the last part of the ledger
- AddToBaseLedger
Which adds the block as a trusted one in the base of the ledger allowing no more forking
- GetUTXOset
Returns the UTXOs that can be spent in new transactions given the current ledger
- addBlock
Adds a block to the ledger if it is valid
### In the block
- Create Merkle Tree
Creates merkle tree hash from the transactions in the block
- HashBlock
Returns the block header hash
### In Transaction
- ValidateTransactions
Validates the transactions passed to it given a utxo set
## In the Crypto Part

### RSA
- Create Key
- Encrypt
- Decrypt
### SHA
- getHash
## In the communication part:
### SubscribeOnEvents
### StartListners
## In the Client:
### RequestLedger
- Requests the ledger from network
### CreateTransaction
- Creates a number of Transactions chosen at random that are valid given the utxo set corresponding to the client’s public key.
### BroadcastPublicKey
- Sends its public key to the network
### busyWaiting
- Waits until the ledger and the public keys are set
### createKeys
- Creates a publickey and its modulus from RSA for the client
## In the Miners:
### POWMine
- Mines the block and Validates if it is compliant with the difficulty level desired.
### RequestLedger
- Requests the ledger from network
### Request Public Keys
- Requests publicKeys from the network

# Components details
## In Data structures

### Ledger:
- The wrapper class for all the data structures needed to represent the ledger.
- Consists of two parts namely baseLedgerPartition which contains blocks that we are sure of and no forking is allowed in it and can be viewed as a linked list and the second part is a transient partition tree where the blocks represent a tree with arbitrary fan out but with limited depth.
### LedgerPartition:
- It is a part of the ledger with the starting index and depth.
### Transient Partition Tree:
- A tree that represents the forking in the last part of the ledger keeping the depth limited
### Block:
- A block consists of block header and set of transactions
- The header contains the hash of previous block, merkle tree root, nonce and a sequence number set to -1
- The block can validate the Transaction which it contains.
### Transaction:
- A transaction has outputs and inputs as arrays
- Has a timestamp, version number, input and output counters
### TransactionInput
- Represents the input to the transaction that points to a previous utxo by hashing the transaction in which it is contained along with the index
- The public key and the signature of the one who wishes to use them which plays the role of the “Unlocking Script”
### TransactionOutput
- Has the public key hash of the receiver along with the amount.

## In the Crypto:

### RSA:
- Creates a pair of private-public key of length 2048 bits.
- Handles all encryption-decryption operations.
### SHA:
- Returns a byte array that is the hash of the object sent to it
## In client:

**There are only 2 threads:**

### Main thread
-Initialize the client with its public key, and set ledger, publickeys of other available nodes.
- Create and send valid transactions
### The notification thread
- The network request or send ledger, public keys, a new block to add in the ledger, etc.

## In Miner:

**There are 5 threads that work in parallel:**

### Main thread
- This thread is responsible for listening for different events on the network and acts upon them.
- When needed it fill different queues for other thread for them to start consuming them
### Block producer thread
-It takes the transactions from transactions queue and start filling the block
### Block Consumer thread
- It takes the blocks that produced by block producer and start mining them weather by pow or bft algorithms
### Block adder
- It listens for blocks broadcasted by other miners and add them to its ledger if it is accepted
### Voting system (BFT only)
- If the current node is the leader it owns it’s voting system
- If any other miner wants to vote on their blocks they send it to the leader and it will initiate the request to vote on this block.

## In communication:

### Process:
- Class that encapsulates the network “A Facade” that creates sockets and allows the code to subscribe to event broadcasts.
# Assumptions
- We use Pay To Public Key Hash Ledger
- Nodes are known to each other beforehand
- No node can be added dynamically once the process start but processes are allowed to fail
- A forked branch can be utmost of depth 9.
- All votes on blocks happen synchronously

# Analysis:
https://docs.google.com/spreadsheets/d/1z5X9gj-7yNcMDe0S8jhGGk5Iw4_TzM7F6Rjd9cFBluU/htmlview?fbclid=IwAR1beK6uGzYatfbGrjAZW2Paz4YZLlEEccFLdF5YkAIVvjX15iB7bCUTovE
