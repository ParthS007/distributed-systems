#include <stdio.h>
#include <stdlib.h>
#include <omp.h>
#include <math.h>
#include <time.h>
#include <ctype.h>


int fibonacci(int n) {
    if (n <= 1) {
        return n;
    }
    return fibonacci(n-1) + fibonacci(n-2);
}

void sequentialFibonacci(int terms) {
    printf("Sequential Fibonacci:\n");
    for (int i = 0; i < terms; i++) {
        printf("%d, ", fibonacci(i));
    }
    printf("\n");
}

void parallelFibonacci(int terms) {
    int fib[terms];
    printf("Parallel Fibonacci:\n");

    #pragma omp parallel for
    for (int i = 0; i < terms; i++) {
        fib[i] = fibonacci(i);
    }

    for (int i = 0; i < terms; i++) {
        printf("%d, ", fib[i]);
    }
    printf("\n");
}

int main() {
    const int terms = 12;

    sequentialFibonacci(terms);
    parallelFibonacci(terms);

    return 0;
}
