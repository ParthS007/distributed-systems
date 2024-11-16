#include <mpi.h>
#include <stdio.h>
#include <string.h>

int main(int argc, char** argv) {
    MPI_Init(&argc, &argv);

    int world_size;
    MPI_Comm_size(MPI_COMM_WORLD, &world_size);

    int world_rank;
    MPI_Comm_rank(MPI_COMM_WORLD, &world_rank);

    const int MAX_STRING = 100;
    char message[MAX_STRING]; // Buffer to store the message

    // Check if the current process is not the last one
    if (world_rank != world_size - 1) {
        sprintf(message, "Greetings from process %d!", world_rank);
        // Send the message to the next process in the sequence (blocking send)
        MPI_Send(message, strlen(message) + 1, MPI_CHAR, world_rank + 1, 0, MPI_COMM_WORLD);
    } else {
        // If the process is the last one, it receives a message from the previous process
        MPI_Recv(message, MAX_STRING, MPI_CHAR, world_rank - 1, 0, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
        printf("Process %d received message: %s\n", world_rank, message);
    }

    MPI_Finalize();
    return 0;
}
