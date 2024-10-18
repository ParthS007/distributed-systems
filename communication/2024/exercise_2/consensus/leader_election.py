import threading
import time
import random

nodes = []
buffer = {}  # items are in the form 'node_id': [(msg_type, value)]
election_timeout = 2  # seconds
heartbeat_interval = 1  # seconds


class Node:
    def __init__(self, id):
        self.id = id
        buffer[id] = []
        self.state = 'follower'
        self.voted_for = None
        self.leader_id = None
        self.last_heartbeat = time.time()
        self.isolated = False  # Flag to check if node is isolated
        self.followed_leader = False  # To track if the node has already followed a leader

    def start(self):
        print(f'Node {self.id} started')
        threading.Thread(target=self.run).start()

    def run(self):
        while True:
            if self.isolated:
                # If the node is isolated, it doesn't send or receive any messages
                continue

            while buffer[self.id]:
                msg_type, value = buffer[self.id].pop(0)
                self.deliver(msg_type, value)

            current_time = time.time()

            if self.state == 'leader' and current_time - self.last_heartbeat > heartbeat_interval:
                self.broadcast('heartbeat', self.id)
                self.last_heartbeat = current_time

            # If no leader and election timeout occurs, or no heartbeats have been received, start election
            if self.state == 'follower' and current_time - self.last_heartbeat > election_timeout:
                print(f"Node {self.id} detected no heartbeat. Starting an election.")
                self.start_election()

            time.sleep(0.1)

    def start_election(self):
        if self.isolated:
            return  # No election if the node is isolated

        self.state = 'candidate'
        self.votes = 1  # vote for self
        self.voted_for = self.id
        print(f'Node {self.id} voted to node {self.id}')
        self.broadcast('candidacy', self.id)

        # Wait for election timeout and check if won
        time.sleep(random.uniform(1, 3))

    def broadcast(self, msg_type, value):
        if self.isolated:
            return  # No broadcast if the node is isolated

        for node in nodes:
            if node.id != self.id and not node.isolated:  # Don't send to self or isolated nodes
                buffer[node.id].append((msg_type, value))

    def deliver(self, msg_type, value):
        if self.isolated:
            return  # No delivery if the node is isolated

        if msg_type == 'candidacy':
            if self.state == 'follower' and self.voted_for is None:
                self.voted_for = value
                print(f"Node {self.id} voted to node {value}")
                self.broadcast('vote', (self.id, value))
        elif msg_type == 'vote':
            candidate_id = value[1]
            if self.state == 'candidate' and candidate_id == self.id:
                self.votes += 1
                if self.votes > len(nodes) // 2:
                    print(f'Node {self.id} detected node {self.id} as leader')
                    self.state = 'leader'
                    self.leader_id = self.id
                    self.broadcast('heartbeat', self.id)
        elif msg_type == 'heartbeat':
            if self.state != 'leader':
                self.state = 'follower'
                self.leader_id = value
                self.last_heartbeat = time.time()
                if not self.followed_leader:  # Print only the first time a node follows a leader
                    print(f"Node {self.id} got a heartbeat and followed node {value} as leader")
                    self.followed_leader = True  # Mark that it has followed a leader

    def isolate(self, secs):
        """Isolate the node for a specific number of seconds."""
        print(f'Node {self.id} is isolated for {secs} seconds.')
        self.isolated = True
        # After the specified time, restore the node
        threading.Timer(secs, self.restore).start()

    def restore(self):
        """Restore the node after isolation."""
        print(f'Node {self.id} is restored from isolation.')
        self.isolated = False
        self.last_heartbeat = time.time()  # Reset heartbeat timer to avoid immediate election


def main():
    global nodes
    nodes = [Node(i) for i in range(3)]
    for node in nodes:
        node.start()

    time.sleep(10)

    while True:
        act = input('state or isolate > ')
        if act == 'isolate':
            id = int(input('node id > '))
            secs = int(input('how many seconds > '))
            nodes[id].isolate(secs)
        elif act == 'state':
            for node in nodes:
                print(f'state of node {node.id}: {node.state}')


if __name__ == "__main__":
    main()
