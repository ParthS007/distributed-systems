# Vector Clocks

### Manual for Running the Vector Clock and Causal Graph Script

#### Prerequisites

- Python 3.x installed on the system.
- Install library for plotting graphs using `pip` => `pip install networkx matplotlib`

#### Files
- vector_clock.py : The main script file.
- `vector_clocks.json`: Output file containing the vector clocks for each commit.
- `causal_graph.json`: Output file containing the causal graph with all causal relationships.
- `minimal_causal_graph.json`: Output file containing the minimal causal graph with transitive edges removed.

#### Running Instructions

We can run the script, generate the output files and plots, and understand the causal relationships between commits in a Git DAG.


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

2. Causal Graph (causal_graph.json)

   ```json
   {
       "commit_hash": ["list_of_commits_it_causally_precedes"]
   }
   ```

3. Minimal Causal Graph (minimal_causal_graph.json)

   ```json
   {
       "commit_hash": ["list_of_commits_it_directly_causally_precedes"]
   }
   ```

4. Visualize the Graphs: The script will automatically display the causal graph and the minimal causal graph using matplotlib. The graphs will be shown in separate file with titles "Causal Graph" and "Minimal Causal Graph".
