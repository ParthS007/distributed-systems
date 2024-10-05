import json
import networkx as nx
import matplotlib.pyplot as plt

# Input JSON representing the Git DAG
dag = {
    "B1": {"1111": [], "12f3": ["1111"], "f432": ["12f3", "2101"]},
    "B2": {"2101": ["1111"]},
    "B3": {"9634": ["2101"], "e13b": ["f432", "9634"]},
}


def calculate_vector_clocks(dag):
    # Initialize vector clocks
    vector_clocks = {}
    branches = list(dag.keys())
    branch_indices = {branch: i for i, branch in enumerate(branches)}

    # Create a reverse mapping to find which branch a commit belongs to
    commit_to_branch = {}
    for branch, commits in dag.items():
        for commit in commits:
            commit_to_branch[commit] = branch

    # update vector clocks
    def update_vector_clock(commit, branch):
        if commit not in vector_clocks:
            vector_clocks[commit] = [0] * len(branches)
        vector_clocks[commit][branch_indices[branch]] += 1

    # Traverse the DAG and update vector clocks
    for branch, commits in dag.items():
        for commit, parents in commits.items():
            if not parents:
                # If there are no parents, initialize the clock for this commit
                update_vector_clock(commit, branch)
            else:
                max_clocks = [0] * len(branches)
                for parent in parents:
                    if parent not in vector_clocks:
                        # Ensure parent is processed before the current commit
                        parent_branch = commit_to_branch[parent]
                        update_vector_clock(parent, parent_branch)
                    # Now updating max_clocks with the parent's clock
                    max_clocks = [
                        max(max_clocks[i], vector_clocks[parent][i])
                        for i in range(len(branches))
                    ]
                # Set the vector clock for the current commit based on max_clocks
                vector_clocks[commit] = max_clocks
                vector_clocks[commit][branch_indices[branch]] += 1

    return vector_clocks


def causally_precedes(a, b):
    return all(x <= y for x, y in zip(a, b)) and any(x < y for x, y in zip(a, b))


def generate_causal_graph(vector_clocks):
    graph = {}
    commits = list(vector_clocks.keys())

    for i in range(len(commits)):
        for j in range(len(commits)):
            if i != j and causally_precedes(
                vector_clocks[commits[i]], vector_clocks[commits[j]]
            ):
                if commits[i] not in graph:
                    graph[commits[i]] = []
                graph[commits[i]].append(commits[j])

    return graph


def remove_transitive_edges(graph):
    minimal_graph = {node: set(edges) for node, edges in graph.items()}

    for node in graph:
        for intermediate in graph[node]:
            if intermediate in graph:
                for successor in graph[intermediate]:
                    if successor in minimal_graph[node]:
                        minimal_graph[node].remove(successor)

    return {node: list(edges) for node, edges in minimal_graph.items()}


def visualize_graph(graph, title):
    G = nx.DiGraph()
    for node, edges in graph.items():
        for edge in edges:
            G.add_edge(edge, node)

    pos = nx.spring_layout(G)
    plt.figure(figsize=(12, 8))
    nx.draw(
        G,
        pos,
        with_labels=True,
        node_size=3000,
        node_color="skyblue",
        font_size=10,
        font_weight="bold",
        arrows=True,
    )
    plt.title(title)
    plt.savefig(f"{title}.png")
    plt.show(block=False)


# Calculate vector clocks
vector_clocks = calculate_vector_clocks(dag)

# Output the vector clocks as a JSON file
output_file = "vector_clocks.json"
with open(output_file, "w") as f:
    json.dump(vector_clocks, f, indent=4)

print(f"Vector clocks have been written to {output_file}")

# Generate causal graph
causal_graph = generate_causal_graph(vector_clocks)

# Output the causal graph as a JSON file
causal_graph_file = "causal_graph.json"
with open(causal_graph_file, "w") as f:
    json.dump(causal_graph, f, indent=4)

print(f"Causal graph has been written to {causal_graph_file}")

# Remove transitive edges
minimal_causal_graph = remove_transitive_edges(causal_graph)

# Output the minimal causal graph as a JSON file
minimal_causal_graph_file = "minimal_causal_graph.json"
with open(minimal_causal_graph_file, "w") as f:
    json.dump(minimal_causal_graph, f, indent=4)

print(f"Minimal causal graph has been written to {minimal_causal_graph_file}")

# Visualize the graphs
visualize_graph(causal_graph, "Causal Graph")
visualize_graph(minimal_causal_graph, "Minimal Causal Graph")
