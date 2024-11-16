#include <mpi.h>
#include <stdio.h>
#include <string.h>

int main(int argc, char** argv) {
    MPI_Init(&argc, &argv);


    int world_rank;
    MPI_Comm_rank(MPI_COMM_WORLD, &world_rank);

    const int MAX_STRING = 100;
    char message[MAX_STRING]; // Buffer to store the message

    // If the current process is the root process (rank 0)
    if (world_rank == 0) {
        // Prepare a broadcast message
        sprintf(message, "Broadcast message from process %d!", world_rank);
    }

    // Broadcast the message to all processes in the MPI_COMM_WORLD communicator
    MPI_Bcast(message, MAX_STRING, MPI_CHAR, 0, MPI_COMM_WORLD);

    if (world_rank != 0) {
        printf("Process %d received broadcast message: %s\n", world_rank, message);
    }

    MPI_Finalize();
    return 0;
}
