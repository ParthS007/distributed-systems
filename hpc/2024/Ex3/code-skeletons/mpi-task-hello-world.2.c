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

    // MPI_Request object for managing non-blocking communication
    MPI_Request request;

    // If not the last process, send a message to the next process
    if (world_rank != world_size - 1) {
        sprintf(message, "Greetings from process %d!", world_rank);
        // Non-blocking send of the message to the next process
        MPI_Isend(message, strlen(message) + 1, MPI_CHAR, world_rank + 1, 0, MPI_COMM_WORLD, &request);
    } else {
        // If the last process, receive the message from the previous process
        MPI_Irecv(message, MAX_STRING, MPI_CHAR, world_rank - 1, 0, MPI_COMM_WORLD, &request);
        // Wait for the non-blocking receive to complete
        MPI_Wait(&request, MPI_STATUS_IGNORE);
        printf("Process %d received message: %s\n", world_rank, message);
    }

    MPI_Finalize();
    return 0;
}
