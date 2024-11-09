#include <stdio.h>
#include <stdlib.h>
#include <mpi.h>
#include <omp.h>

#define SEED 42  // Fixed seed for random values

// Function to initialize matrices with a fixed seed for repeatability
void init_matrix(double *matrix, int rows, int cols) {
    #pragma omp parallel for
    for (int i = 0; i < rows; i++) {
        for (int j = 0; j < cols; j++) {
            matrix[i * cols + j] = drand48();  // Random values for demonstration
        }
    }
}

// Main GEMM (General Matrix Multiplication) function
void gemm(double *A, double *B, double *C, int n, int rows_per_rank, int rank) {
    int row_counter;  // Counter for rows processed in OpenMP threads
    
    //TODO: parallelize the region and the loop using OpenMP
    #pragma omp parallel private(row_counter)
    {
        row_counter = 0;

        #pragma omp for
        for (int i = 0; i < rows_per_rank; i++) {
            //TODO: Increment the row counter such that we can print the number of rows processed by each thread
            row_counter++;
            for (int j = 0; j < n; j++) {
                double sum = 0.0;
                for (int k = 0; k < n; k++) {
                    sum += A[i * n + k] * B[k * n + j];
                }
                C[i * n + j] = sum;
            }
        }

        //TODO: Print the number of rows processed by each thread. Remember, this might need to be inside the OpenMP parallel region
        # pragma omp critical
        {
            printf("Thread %d of rank %d processed %d rows\n", omp_get_thread_num(), rank, row_counter);
        }
    }
}

int main(int argc, char *argv[]) {
    int rank, size;
    
    //TODO: Initialize MPI
    MPI_Init(&argc, &argv);
    MPI_Comm_rank(MPI_COMM_WORLD, &rank);
    MPI_Comm_size(MPI_COMM_WORLD, &size);

    // Read matrix size from command line argument
    if (argc != 2) {
        if (rank == 0) {
            printf("Usage: %s <matrix_size>\n", argv[0]);
        }
        MPI_Finalize();
        return -1;
    }
    int N = atoi(argv[1]);  // Matrix size you provided and an argument

    //TODO: Calculate how many rows each rank will process
    int rows_per_rank = N / size;

    //TODO: Allocate memory for the matrices. 
    //Remember, matrix A is local to each rank, while matrix B is only on rank 0. 
    //Thus, the memory allocation for A will be different (smaller) from B.
    //Later, matrix B will be broadcasted to all ranks such that each rank has a copy of it and can use it to mutiply by their portion of A.
    //In summary, each rank will have a piece of A and the whole B, and will compute a piece of C.
    double *A = (double *)malloc(rows_per_rank * N * sizeof(double));
    double *B = (double *)malloc(N * N * sizeof(double));
    double *C = (double *)malloc(rows_per_rank * N * sizeof(double));

    // Initialize the seed we will use to generate the matrices. We use a fixed seed for repeatability.
    srand48(SEED);

    // Initialize matrix A.
    //HINT: rows_per_rank will be calculated in the TODO of line 52.
    init_matrix(A, rows_per_rank, N);
    
    // Rank 0 initializes matrix B and broadcasts to all other ranks.
    if (rank == 0) {
        init_matrix(B, N, N);  // Only rank 0 initializes matrix B
    }

    // Start timer
    MPI_Barrier(MPI_COMM_WORLD); 
    double start_time = MPI_Wtime();

    //TODO: Broadcast matrix B to all ranks
    MPI_Bcast(B, N * N, MPI_DOUBLE, 0, MPI_COMM_WORLD);

    // Perform GEMM
    gemm(A, B, C, N, rows_per_rank, rank);

    //TODO: Gather result from all ranks into rank 0.
    if (rank == 0) {
        //TODO: Allocate the memory for the final matrix C that will hold the result of the multiplication from all ranks.
        double *final_C = (double *)malloc(N * N * sizeof(double));
        MPI_Gather(C, rows_per_rank * N, MPI_DOUBLE, final_C, rows_per_rank * N, MPI_DOUBLE, 0, MPI_COMM_WORLD);
        free(final_C);
    } else {
        //TODO: Send the result of the multiplication from each rank to rank 0. HINT, "Send" DOES NOT mean MPI_Send!
        MPI_Gather(C, rows_per_rank * N, MPI_DOUBLE, NULL, 0, MPI_DOUBLE, 0, MPI_COMM_WORLD);
    }

    // End timer and print execution time on rank 0
    double end_time = MPI_Wtime();
    double max_time;
    MPI_Reduce(&end_time, &max_time, 1, MPI_DOUBLE, MPI_MAX, 0, MPI_COMM_WORLD);

    //TODO: print the execution time.
    // Make sure that rank 0 prints the CORRECT execution time. 
    // HINT, you need to make sure that rank 0 contains the correct end_time or, in other words, the end_time that actually represents the final execution time of the GEMM computation.
    if (rank == 0) {
        printf("Execution time: %f seconds\n", max_time - start_time);
    }

    // Free memory
    free(A);
    free(B);
    free(C);

    MPI_Finalize();
    return 0;
}
