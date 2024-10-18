"""
CRDT implementation for shopping cart using the Causal Length Set (CLS) approach.

CLS approach: Elements can be added and removed arbitrarily often without coordination,
yet all parties eventually agree on the final state.

The algorithm is implemented in the class CLS. The class has the following methods:
- add(element): Add an element to the list
- remove(element): Remove an element from the list
- contains(element): Check if the element is in the list
- mutual_sync(other_lists): Synchronize the list with other lists
- get_elements(): Get the elements in the list
"""


class CLS:
    def __init__(self):
        self.A = {}

    def add(self, element):
        # If element is not in the list, add it with a count of 1
        if element not in self.A:
            self.A[element] = 1
        # If element is in the list and its count is even, increment the count
        elif self.A[element] % 2 == 0:
            self.A[element] += 1

    def remove(self, element):
        # If element is in the list and it is odd, increment it
        if element in self.A and self.A[element] % 2 == 1:
            self.A[element] += 1

    def contains(self, element):
        # If element is in the list and it is odd, return True
        return element in self.A and self.A[element] % 2 == 1

    def mutual_sync(self, other_lists):
        # Synchronize the list with other lists by taking the maximum count of each element
        for other in other_lists:
            for element in set(self.A.keys()).union(other.A.keys()):
                self.A[element] = max(self.A.get(element, 0), other.A.get(element, 0))
                other.A[element] = self.A[element]

    def get_elements(self):
        return {e for e in self.A if self.contains(e)}


# Simulated shared shopping cart
alice_list = CLS()
bob_list = CLS()

alice_list.add("Milk")
alice_list.add("Potato")
alice_list.add("Eggs")

bob_list.add("Sausage")
bob_list.add("Mustard")
bob_list.add("Coke")
bob_list.add("Potato")
bob_list.mutual_sync([alice_list])

alice_list.remove("Sausage")
alice_list.add("Tofu")
alice_list.remove("Potato")
alice_list.mutual_sync([bob_list])

print("Bob’s list contains Potato ?", bob_list.contains("Potato"))
print("Alice’s list contains Potato ?", alice_list.contains("Potato"))

# We see both lists to verify they are the same after mutual synchronization
print("Alice's list:", alice_list.get_elements())
print("Bob's list:", bob_list.get_elements())
