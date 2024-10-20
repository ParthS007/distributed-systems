import threading
import time
import random

nodes = []
buffer = {}  # items are in the form 'node_id': [(msg_type, value)]
election_timeout = 2  # seconds
heartbeat_interval = 1  # seconds
election_in_progress = False  # Flag to track election state
election_lock = threading.Lock()  # Lock to synchronize access to the flag


class Node:
    def __init__(self, id):
        self.id = id
        buffer[id] = []
        self.state = "follower"
        self.votes = 0
        self.voted_for = None
        self.leader_id = None
        self.last_heartbeat = time.time()
        self.silent_period = 0

    def start(self):
        print(f"Node {self.id} started")
        threading.Thread(target=self.run).start()

    def run(self):
        while True:
            if self.state == "isolate":
                if time.time() > self.last_heartbeat + self.silent_period:
                    print(f"Node {self.id} is restored from isolation.")
                    self.state = "follower"
                    self.last_heartbeat = time.time()
                else:
                    # If the node is isolated, it doesn't send or receive any messages
                    continue

            while buffer[self.id]:
                msg_type, value = buffer[self.id].pop(0)
                self.deliver(msg_type, value)

            if (
                self.state == "follower"
                and time.time() - self.last_heartbeat > election_timeout
            ):
                self.start_election()

            elif (
                self.state == "leader"
                and time.time() - self.last_heartbeat >= heartbeat_interval
            ):
                self.broadcast("heartbeat", self)
                self.last_heartbeat = time.time()

            time.sleep(0.1)

    def start_election(self):
        global election_in_progress

        # Prevent multiple election processes from starting simultaneously
        with election_lock:
            # Exit if an election is already in progress
            if election_in_progress:
                return
            election_in_progress = True

        self.state = "candidate"
        self.votes += 1
        print(f"Node {self.id} is starting an election.")
        print(f"Node {self.id} voted to node {self.id}")

        # Wait for election timeout and check if won
        time.sleep(random.uniform(1, 3))
        self.broadcast("candidacy", self)

    def broadcast(self, msg_type, value):
        for node in nodes:
            buffer[node.id].append((msg_type, value))

    def deliver(self, msg_type, value):
        global election_in_progress

        if msg_type == "candidacy":
            if self.state == "follower":
                value.votes += 1
                print(f"Node {self.id} voted to node {value.id}")
                if value.votes > len(nodes) // 2:
                    value.state = "leader"
                    self.leader_id = value.id
                    value.broadcast("heartbeat", self)
                    print(
                        f"Node {self.id} got a heartbeat and followed node {value.id} as leader"
                    )

                    # Reset the election flag after a leader is chosen
                    with election_lock:
                        election_in_progress = False
            elif self.state == "candidate":
                if self.votes > len(nodes) // 2:
                    self.state = "leader"
                    self.leader_id = self.id
                    print(f"Node {self.id} detected node {self.id} as leader")
                    self.broadcast("heartbeat", self)

                    # Reset the election flag after a leader is chosen
                    with election_lock:
                        election_in_progress = False
            self.broadcast("vote", self)
        elif msg_type == "vote":
            if self.state == "candidate":
                if self.votes > len(nodes) // 2:
                    print(f"Node {self.id} detected node {self.id} as leader")
                    self.state = "leader"
                    self.leader_id = self.id
                    self.broadcast("heartbeat", self)

                    # Reset the election flag after a leader is chosen
                    with election_lock:
                        election_in_progress = False
        elif msg_type == "heartbeat":
            if self.state == "follower":
                self.leader_id = value
                self.last_heartbeat = time.time()

                # Reset the election flag if a heartbeat is received
                with election_lock:
                    election_in_progress = False

    def isolate(self, secs):
        self.__init__(self.id)
        self.silent_period = secs
        self.state = "isolate"
        self.last_heartbeat = time.time()


def main():
    global nodes
    nodes = [Node(i) for i in range(3)]
    for node in nodes:
        node.start()

    time.sleep(10)

    while True:
        act = input("state or isolate > ")
        if act == "isolate":
            id = int(input("node id > "))
            secs = int(input("how many seconds > "))
            nodes[id].isolate(secs)
        elif act == "state":
            for node in nodes:
                print(f"state of node {node.id}: {node.state}")


if __name__ == "__main__":
    main()
