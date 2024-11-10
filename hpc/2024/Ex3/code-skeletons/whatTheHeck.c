#include <stdio.h>
#include <omp.h>


// In this exercise you need to correct this code. The parallel region is causing problems. //



static const int a[] = {1,2,3,4,5,6,7,8,9,10};
int global_sum = 0;

int
sum(const int* arr, size_t n)
{
  
  int s=0;
  #pragma omp parallel for
  for(size_t i=0; i < n; ++i) {
    s += arr[i];
  }
  return s;
}

int computeLocalSum(const int *arr, int start, int end) {
    int local_sum = 0;
    for (int i = start; i < end; i++) {
        local_sum += arr[i];
    }
    return local_sum;
}

void addToGlobalSum(int *global_sum, int local_sum) {
    #pragma omp critical
    {
        *global_sum += local_sum;
    }
}

int
main()
{
  #pragma omp parallel
  {
      int thread_num = omp_get_thread_num();
      int thread_count = omp_get_num_threads();
      // the array is divided among the threads
      int start = thread_num * (10 / thread_count);
      int end = (thread_num == thread_count - 1) ? 10 : start + (10 / thread_count);

      // each thread computes its local sum
      int local_sum = computeLocalSum(a, start, end);
      // each thread adds its local sum to the global sum in a critical section
      addToGlobalSum(&global_sum, local_sum);
  }

  printf("sum: %d\n", global_sum); //Expected: 55
  return 0;
}

/*

In provided code, parallelization in the sum function seems problematic.
The problem is that the variable s is shared between threads, and each thread
is modifying s concurrently. This is a race condition, and the result of the
program is non-deterministic. 

*/
