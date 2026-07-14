# multithreading-average
It's a very simple code that uses divide and conquer technique that find the average for very large amount of numbers in no time and no interemediate overflow

# Result
```shell
  The average without threads: 74.61, Took: 0.004600ms
  The average with threads:    74.61, Took: 0.002700ms
```
half time, while it doesn't worth in small amount of numbers but it do really when they are huge


Even by marking the method `portion` as `synchronized` where the shared memory mutation happen, a race condition might happen because I have two different shared items I mutate
```java
// Find the avg for this portion
avgs.add(this.internalAvg(item.get(0), item.get(1)));

// Add the total count
count.add(item.get(1) - item.get(0));
```

and they must be symmetriced meaning the first item at array `avgs` its count must be first element in the array `count`. even if we stored them in array or arrays and the array was large enough (or even the process in general) the `add` method here isn't a thread safe, therefore an interruption is expected. So, I used `Semaphore` with 1 premit to make sure that only one thread at the time can add an element to the end of both arrays and guarantee that thet will be symmetriced. In the end this is just a hands on very simple mockup to simulate how something like `database engines` uses workers to do an expenisve aggergation
