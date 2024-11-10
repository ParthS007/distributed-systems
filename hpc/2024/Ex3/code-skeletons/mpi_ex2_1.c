#include <mpi.h>   // Include MPI library header for parallel programming functions
#include <stdio.h> // Standard Input and Output Library for functions like printf
#include <string.h> // String Library for string manipulation functions

int main(int argc, char** argv) {
    MPI_Init(&argc, &argv); // Initialize the MPI environment

    // Variables to store the total number of processes and the rank of each process
    int world_size;
    MPI_Comm_size(MPI_COMM_WORLD, &world_size); // Get the number of processes in the MPI world

    int world_rank;
    MPI_Comm_rank(MPI_COMM_WORLD, &world_rank); // Get the rank (identifier) of the current process

    // Define the maximum length for the message string
    const int MAX_STRING = 100;
    char message[MAX_STRING]; // Buffer to store the message

    // Check if the current process is not the last one
    if (world_rank != world_size - 1) {
        // Prepare a message
        sprintf(message, "Greetings from process %d!", world_rank);
        // Send the message to the next process in the sequence (blocking send)
        MPI_Send(message, strlen(message) + 1, MPI_CHAR, world_rank + 1, 0, MPI_COMM_WORLD);
    } else {
        // If the process is the last one, it receives a message from the previous process
        MPI_Recv(message, MAX_STRING, MPI_CHAR, world_rank - 1, 0, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
        // Print the received message
        printf("Process %d received message: %s\n", world_rank, message);
    }

    MPI_Finalize(); // Finalize the MPI environment, cleaning up all MPI resources
    return 0;
}
