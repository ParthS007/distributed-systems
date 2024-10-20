# Report on Leader Election Algorithm

This report demonstrates the behavior of a system under various node configurations (1 node, 3 nodes, and 5 nodes), showing the election process, node isolation, and restoration scenarios.

## Scenarios and Observations

### Scenario 1: Leader Election with 1 Node
**Execution Output:**

```bash
Node 0 started
Node 0 is starting an election.
Node 0 voted to node 0
Node 0 detected node 0 as leader
state or isolate > isolate
node id > 0
how many seconds > 10
state or isolate > Node 0 is restored from isolation.
Node 0 is starting an election.
Node 0 voted to node 0
Node 0 detected node 0 as leader
```

**Summary**:
- In a single-node scenario, the only node (Node 0) automatically becomes the leader. 
- Even after isolation and restoration, Node 0 re-elects itself since no other nodes exist in the system. This demonstrates the self-sufficient nature of a system with just one node.

### Scenario 2: Leader Election with 3 Nodes
**Execution Output:**

```bash
Node 0 started
Node 1 started
Node 2 started
Node 2 is starting an election.
Node 2 voted to node 2
Node 0 voted to node 2
Node 0 got a heartbeat and followed node 2 as leader
Node 1 voted to node 2
Node 1 got a heartbeat and followed node 2 as leader
state or isolate > state
state of node 0: follower
state of node 1: follower
state of node 2: leader
state or isolate > isolate
node id > 2
how many seconds > 5
state or isolate > Node 1 is starting an election.
Node 1 voted to node 1
Node 0 voted to node 1
Node 0 got a heartbeat and followed node 1 as leader
Node 2 is restored from isolation.
Node 2 voted to node 1
Node 2 got a heartbeat and followed node 1 as leader
```

**Summary**:
- Initially, Node 2 starts an election and is voted as the leader by Node 0 and Node 1.
- After isolating the leader (Node 2), Node 1 starts a new election and becomes the leader.
- Once Node 2 is restored, it follows Node 1 as the leader, showing that nodes gracefully accept new leadership when restored to the network.

### Scenario 3: Leader Election with 5 Nodes
**Execution Output:**

```bash
Node 0 started
Node 1 started
Node 2 started
Node 3 started
Node 4 started
Node 0 is starting an election.
Node 0 voted to node 0
Node 1 voted to node 0
Node 4 voted to node 0
Node 4 got a heartbeat and followed node 0 as leader
Node 3 voted to node 0
Node 3 got a heartbeat and followed node 0 as leader
Node 2 voted to node 0
Node 2 got a heartbeat and followed node 0 as leader
state or isolate > isolate
node id > 0
how many seconds > 5
state or isolate > Node 4 is starting an election.
Node 4 voted to node 4
Node 2 voted to node 4
Node 1 voted to node 4
Node 1 got a heartbeat and followed node 4 as leader
Node 3 voted to node 4
Node 3 got a heartbeat and followed node 4 as leader
Node 0 is restored from isolation.
Node 0 voted to node 4
Node 0 got a heartbeat and followed node 4 as leader
```

**Summary**:
- Initially, Node 0 starts an election and is elected leader by receiving votes from Node 1, Node 4, Node 3, and Node 2.
- After isolating Node 0, Node 4 starts a new election and becomes the leader.
- Upon restoration, Node 0 follows Node 4 as the new leader, showing smooth recovery and transition of leadership in a larger system.
