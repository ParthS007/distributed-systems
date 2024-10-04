# Vector Clocks

### Manual for Running the Vector Clock and Causal Graph Script

#### Prerequisites

- Python 3.x installed on the system.
- Basic understanding of Git DAGs and vector clocks.

#### Files
- vector_clock.py : The main script file.
- `vector_clocks.json`: Output file containing the vector clocks for each commit.
- `causal_graph.json`: Output file containing the causal graph with all causal relationships.
- `minimal_causal_graph.json`: Output file containing the minimal causal graph with transitive edges removed.

#### Steps to Run the Script

1. Ensure the `vector_clock.py` script in the working directory.

2. **Run the Script**: `python vector_clock.py`

3. **Check the Output Files**

   After running the script, three JSON files will be generated in the same directory:

   - `vector_clocks.json`
   - `causal_graph.json`
   - `minimal_causal_graph.json`

#### Interpreting the Output

1. Vector Clocks (vector_clocks.json):

   ```json
   {
       "commit_hash": ["list_of_clock_values"]
   }
   ```

   Example:
   ```json
   {
       "1111": [1, 0, 0],
       "12f3": [2, 0, 0],
       "f432": [3, 1, 0],
       "2101": [1, 1, 0],
       "9634": [1, 1, 1],
       "e13b": [3, 1, 2]
   }
   ```

2. Causal Graph (causal_graph.json)

   ```json
   {
       "commit_hash": ["list_of_commits_it_causally_precedes"]
   }
   ```
   Example:
   ```json
   {
       "1111": ["12f3", "2101"],
       "12f3": ["f432"],
       "2101": ["f432", "9634"],
       "9634": ["e13b"],
       "f432": ["e13b"]
   }
   ```

3. Minimal Causal Graph (minimal_causal_graph.json)

   ```json
   {
       "commit_hash": ["list_of_commits_it_directly_causally_precedes"]
   }
   ```
   Example:
   ```json
   {
       "1111": ["12f3", "2101"],
       "12f3": ["f432"],
       "2101": ["9634"],
       "9634": ["e13b"],
       "f432": ["e13b"]
   }
   ```

By following these steps, we can run the script, generate the necessary output files, and understand the causal relationships between commits in a Git DAG.
