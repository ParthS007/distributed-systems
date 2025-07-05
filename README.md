# Distributed Systems

A comprehensive repository containing implementations and exercises for fundamental distributed systems concepts across communication, data systems, high-performance computing, and privacy-preserving technologies.

## 📚 Course Overview

This repository is organized into four main areas of distributed systems study:

- **Communication**: Protocol implementations, consensus algorithms, and causal broadcast systems
- **Data Systems**: Peer-to-peer networks, distributed hash tables, and data consistency models
- **High-Performance Computing (HPC)**: Parallel algorithms, MPI implementations, and OpenMP optimizations
- **Privacy**: Differential privacy mechanisms and homomorphic encryption implementations

## 🏗️ Repository Structure

```
distributed-systems/
├── communication/          # Communication protocols and algorithms
│   ├── 2023/              # Git-based causal broadcast system
│   └── 2024/              # RPC, vector clocks, CRDT, consensus algorithms
├── data/                  # Data systems and P2P networks
│   └── 2024/              # Chord protocol implementation
├── hpc/                   # High-performance computing exercises
│   ├── 2023/              # Basic parallel computing concepts
│   └── 2024/              # Advanced MPI and OpenMP implementations
├── privacy/               # Privacy-preserving technologies
│   └── 2024/              # Differential privacy and homomorphic encryption
└── env/                   # Python virtual environment
```

## 🔧 Technologies & Languages

- **Python 3.11+**: Core implementations, Jupyter notebooks
- **C/C++**: MPI and OpenMP parallel computing
- **Java**: Chord protocol simulation
- **gRPC/Protocol Buffers**: Remote procedure calls
- **Git**: Collaborative branching and causal broadcast
- **Shell Scripts**: Automation and deployment

## 🚀 Getting Started

### Prerequisites

1. **Python Environment**:
   ```bash
   # Activate the existing virtual environment
   source env/bin/activate
   
   # Or create a new one
   python3 -m venv venv
   source venv/bin/activate
   pip install -r requirements.txt
   ```

2. **System Dependencies**:
   - Git (for causal broadcast)
   - Java 17+ (for Chord protocol)
   - MPI implementation (for HPC exercises)
   - C compiler with OpenMP support

### Quick Start

Each module contains its own README with specific setup instructions:

- [Communication Examples](communication/)
- [Data Systems](data/)
- [HPC Exercises](hpc/)
- [Privacy Technologies](privacy/)

## 📋 Key Components

### Communication Systems

#### 1. Remote Procedure Calls (RPC)
- **Location**: `communication/2024/exercise_1/rpc/`
- **Description**: gRPC-based client-server architecture with hash computation
- **Key Files**: 
  - `hash_server/hash_server_main.py` - SHA256 hash service
  - `client/client_main.py` - RPC client implementation

#### 2. Vector Clocks
- **Location**: `communication/2024/exercise_1/vector_clock/`
- **Description**: Implementation of vector clocks for distributed event ordering
- **Features**: Causal graph generation and visualization

#### 3. Conflict-Free Replicated Data Types (CRDT)
- **Location**: `communication/2024/exercise_2/crdt/`
- **Description**: Causal Length Set (CLS) implementation for distributed shopping cart
- **Key Concepts**: Add/remove semantics, eventual consistency, conflict resolution

#### 4. Consensus Algorithms
- **Location**: `communication/2024/exercise_2/consensus/`
- **Description**: Leader election algorithm with fault tolerance
- **Features**: Node isolation, recovery, heartbeat mechanisms

#### 5. Git-based Causal Broadcast
- **Location**: `communication/2023/git-cb-template/`
- **Description**: Distributed messaging system using Git as transport layer
- **Tools**: Complete set of CLI tools for causal message ordering

### Data Systems

#### Chord Protocol (P2P DHT)
- **Location**: `data/2024/Ex7-FDS-2024/p2p-chord/`
- **Description**: Java implementation of Chord distributed hash table
- **Features**: Ring topology, finger tables, stabilization protocol
- **Usage**: `java -jar p2p-1.0-SNAPSHOT.jar --bits=<m> --dynamic=<true|false>`

### High-Performance Computing

#### 1. MPI Implementations
- **Location**: `hpc/2024/Ex3/code-skeletons/`
- **Examples**:
  - Tree-based global sum reduction
  - Ring communication patterns
  - Non-blocking message passing
  - Broadcast algorithms

#### 2. OpenMP Parallelization
- **Examples**:
  - Parallel Fibonacci computation
  - Thread synchronization patterns
  - Critical sections and race condition fixes

### Privacy-Preserving Technologies

#### 1. Differential Privacy
- **Location**: `privacy/2024/E5/ex5.ipynb`
- **Implementation**: Laplace mechanism for counting queries
- **Dataset**: Adult income dataset with PII protection

#### 2. Homomorphic Encryption
- **Location**: `privacy/2024/E6/ex6.ipynb`
- **Implementation**: ElGamal encryption system
- **Use Case**: Privacy-preserving shopping cart calculations

## 🛠️ Development Setup

### Running Communication Examples

```bash
# RPC Example
cd communication/2024/exercise_1/rpc
python3 -m grpc_tools.protoc -I. --python_out=./ --grpc_python_out=./ hash_server/hash.proto
python3 hash_server/hash_server_main.py  # Terminal 1
python3 client/client_main.py            # Terminal 2

# CRDT Example
cd communication/2024/exercise_2/crdt
python crdt.py

# Consensus Algorithm
cd communication/2024/exercise_2/consensus
python leader_election.py
```

### Running HPC Examples

```bash
cd hpc/2024/Ex3/code-skeletons
mpicc -o tree tree.c
mpirun -np 4 ./tree
```

### Running Privacy Examples

```bash
cd privacy/2024/E5
jupyter notebook ex5.ipynb

cd ../E6
jupyter notebook ex6.ipynb
```

## 📖 Learning Objectives

### Communication & Coordination
- Understanding of distributed communication patterns
- Implementation of consensus protocols
- Causal ordering and vector clocks
- Conflict resolution in replicated systems

### Data Systems
- Peer-to-peer network architectures
- Distributed hash tables and consistent hashing
- Load balancing in distributed systems

### High-Performance Computing
- Parallel algorithm design
- Message passing interface (MPI)
- Shared memory programming (OpenMP)
- Performance optimization techniques

### Privacy & Security
- Differential privacy mechanisms
- Homomorphic encryption schemes
- Privacy-preserving computation

## 🤝 Contributing

This repository represents coursework and exercises. Each implementation includes:

- Comprehensive documentation
- Test cases and validation
- Performance analysis (where applicable)
- Error handling and edge cases

## 📚 References

- **Chord Protocol**: [Original Paper](http://pdos.lcs.mit.edu/chord/)
- **Vector Clocks**: Lamport timestamps and causal ordering
- **CRDT**: Conflict-free replicated data types literature
- **Differential Privacy**: Dwork, C. (2008) framework
- **MPI**: Message Passing Interface standards

## 📄 License

This project is part of academic coursework at the University of Basel. All implementations are for educational purposes.

---

*Built with ❤️ for distributed systems*
