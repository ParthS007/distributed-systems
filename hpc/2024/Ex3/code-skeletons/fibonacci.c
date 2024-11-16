#include <stdio.h>
#include <stdlib.h>
#include <omp.h>
#include <math.h>
#include <time.h>
#include <ctype.h>


int fibonacci_seq(int n) {
    if (n <= 1) {
        return n;
    }
    return fibonacci_seq(n-1) + fibonacci_seq(n-2);
}

void sequentialFibonacci(int terms) {
    printf("Sequential Fibonacci:\n");
    for (int i = 0; i < terms; i++) {
        printf("%d, ", fibonacci_seq(i));
    }
    printf("\n");
}

int fibonacci_par(int n) {
    int i, j;
    if (n < 2)
        return n;
    else {
        #pragma omp task shared(i)
        i = fibonacci_par(n-1);
        #pragma omp task shared(j)
        j = fibonacci_par(n-2);
        #pragma omp taskwait
        return i + j;
    }
}

void parallelFibonacci(int terms) {
    printf("Parallel Fibonacci:\n");
    #pragma omp parallel
    {
        #pragma omp single
        {
            for (int i = 0; i < terms; i++) {
                int result = fibonacci_par(i);
                printf("%d, ", result);
            }
        }
    }
    printf("\n");
}

int main() {
    const int terms = 12;

    sequentialFibonacci(terms);
    parallelFibonacci(terms);

    return 0;
}
