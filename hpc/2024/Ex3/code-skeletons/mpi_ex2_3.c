#include <mpi.h>   // Include MPI library header for parallel programming functions
#include <stdio.h> // Standard Input and Output Library for functions like printf
#include <string.h> // String Library for string manipulation functions

int main(int argc, char** argv) {
    MPI_Init(&argc, &argv); // Initialize the MPI environment

    // Variable to store the rank of the current process
    int world_rank;
    MPI_Comm_rank(MPI_COMM_WORLD, &world_rank); // Get the rank of the current process

    // Define the maximum length for the message string
    const int MAX_STRING = 100;
    char message[MAX_STRING]; // Buffer to store the message

    // If the current process is the root process (rank 0)
    if (world_rank == 0) {
        // Prepare a broadcast message
        sprintf(message, "Broadcast message from process %d!", world_rank);
    }

    // Broadcast the message to all processes in the MPI_COMM_WORLD communicator
    MPI_Bcast(message, MAX_STRING, MPI_CHAR, 0, MPI_COMM_WORLD);

    // All processes except the root process receive the message and print it
    if (world_rank != 0) {
        printf("Process %d received broadcast message: %s\n", world_rank, message);
    }

    MPI_Finalize(); // Finalize the MPI environment, cleaning up all MPI resources
    return 0;
}
