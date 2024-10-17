# Description: CRDT implementation for shopping cart


class CLS:
    def __init__(self):
        self.add_set = set()
        self.remove_set = set()

    def add(self, element):
        self.add_set.add(element)

    def remove(self, element):
        self.remove_set.add(element)
        if element in self.add_set:
            self.add_set.discard(element)

    def mutual_sync(self, other_lists):
        for other in other_lists:
            self.add_set.update(other.add_set)
            self.remove_set.update(other.remove_set)

        self.add_set.difference_update(self.remove_set)

    def contains(self, element):
        return element in self.add_set


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
