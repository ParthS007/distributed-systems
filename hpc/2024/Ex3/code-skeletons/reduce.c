#include <stdio.h>
#include <stdlib.h>
#include <mpi.h>

int main(int argc, char* argv[]) {
    int my_rank, p;
    MPI_Comm comm = MPI_COMM_WORLD;

    MPI_Init(&argc, &argv);
    MPI_Comm_size(comm, &p);
    MPI_Comm_rank(comm, &my_rank);

    int range = 2000;
    int local_start = my_rank * (range / p) + 1;
    int local_end = (my_rank + 1) * (range / p);
    if (my_rank == p - 1) {
        local_end = range;
    }

    int local_sum = 0;
    for (int i = local_start; i <= local_end; i++) {
        local_sum += i;
    }

    int global_sum = 0;
    // Reduce the local sum to the global sum
    MPI_Reduce(&local_sum, &global_sum, 1, MPI_INT, MPI_SUM, 0, comm);

    if (my_rank == 0) {
        printf("Cumulative sum of numbers from 1 to %d is %d\n", range, global_sum);
    }

    MPI_Finalize();
    return 0;
}
