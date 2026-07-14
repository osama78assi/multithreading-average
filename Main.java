import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

class InavlidPointerException extends Exception {
    public InavlidPointerException(String msg) {
        super(msg);
    }
}

class NoValidPortion extends Exception {
    public NoValidPortion(String msg) {
        super(msg);
    }
}

// Helper class to be able to take divides from any collection
class Divider {
    private int total;
    private int pointer;

    public Divider(int total) {
        this.total = total;
    }

    public Divider(int total, int pointer) throws InavlidPointerException {
        if(pointer > total || pointer < 0) {
            throw new InavlidPointerException("Pointer has invalid value");
        }

        this.total = total;
        this.pointer = pointer;
    }

    private int validPortion(int portion) {
        if(this.total > this.pointer + portion) {
            return portion;
        }

        return this.total % this.pointer;
    }

    public synchronized ArrayList<Integer> portion(int portionLen) throws NoValidPortion {
        if(this.pointer == this.total) {
            throw new NoValidPortion("There is no more valid portion to generate");
        }
        
        ArrayList arr = new ArrayList<Integer>(2);
        int validPortion =  this.validPortion(portionLen);

        arr.add(this.pointer);
        arr.add(this.pointer + validPortion);

        this.pointer += validPortion;

        return arr;
    }

    public boolean portionable() {
        return this.total > this.pointer;
    }

    public int getPointer() {
        return  this.pointer;
    }

    public int getTotal() {
        return  this.total;
    }

    @Override
    public String toString() {
        return String.format("Total values: %s, Current pointer at: %s", this.total, this.pointer);
    }

}

class Aggregator {
    public double[] items;
    public Semaphore syncLock;

    public Aggregator(double[] items) {
        this.items = items;

        this.syncLock = new Semaphore(1);
    }


    private double internalAvg() {
        double totSum = 0;
        for(double num : this.items) {
            totSum += num;
        }

        return totSum / this.items.length;
    }

    private double internalAvg(int from, int to) {
        double totSum = 0;
        for(int i = from; i < to; i++) {
            totSum += this.items[i];
        }

        return totSum / (to - from);
    }

    private double multiThreadAvg() throws InterruptedException {
        Divider div = new Divider(this.items.length);

        // { {avg1, total1}, {avg1, total2} }
        ArrayList<Double> avgs = new ArrayList<>(10);
        ArrayList<Integer> count = new ArrayList<>(10);

        // Define the runnable unit
        Runnable unit = () ->  {
            // Divid and find frist and last
            try {
                while(div.portionable()) {
                    ArrayList<Integer> item = div.portion(2);

                    try {
                        this.syncLock.acquire();
                        // Find the avg for this portion
                        avgs.add(this.internalAvg(item.get(0), item.get(1)));

                        // Add the total count
                        count.add(item.get(1) - item.get(0));

                        this.syncLock.release();
                    } catch(InterruptedException e) {
                        System.out.println("Interruption happened");
                    } finally {
                    }
                }
                
            } catch (NoValidPortion e) {
                System.out.println(e);
            }
        };

        // Create two threads for the same processing unit
        Thread t1 = new Thread(unit);
        Thread t2 = new Thread(unit);

        // Start thread
        t1.start();
        t2.start();

        // Join with the main thread
        t1.join();
        t2.join();

        // Store the final avg
        double finalAvg = 0;
        double totalCount = 0;

        // Sum the counts
        for(int c : count) {
            totalCount += c;
        }

        // Apply the general rule, By evading intermediate overflow
        for(int i = 0; i < avgs.size(); i++) {
            // Sum to the final
            finalAvg += (avgs.get(i) * (count.get(i) / totalCount));
        }

        return finalAvg;
    }

    public double avg() throws InterruptedException {
        if(items.length < 10_000) {
            return this.internalAvg();
        }

        return this.multiThreadAvg();
    }

    public double avg(boolean multiThreading) throws InterruptedException {
        if(!multiThreading) {
            return this.internalAvg();
        }

        return this.multiThreadAvg();
    }
}


class Main {
    public static void main(String args[]) throws FileNotFoundException, InterruptedException, NoValidPortion {
        double[] marks = {
            100.00, 90.1, 95.2, 96.00, 78.20, 67.9,
            76, 19, 50, 60.8, 42.45, 60, 90, 99, 99.1,
            86, 84, 79.28, 100, 80, 80, 60, 40, 60,
            100.00, 90.1, 95.2, 96.00, 78.20, 67.9,
            76, 19, 50, 60.8, 42.45, 60, 90, 99, 99.1,
            86, 84, 79.28, 100, 80, 80, 60, 40, 60,
            86, 84, 79.28, 100, 80, 80, 60, 40, 60,
            86, 84, 79.28, 100, 80, 80, 60, 40, 60,
            86, 84, 79.28, 100, 80, 80, 60, 40, 60,
            86, 84, 79.28, 100, 80, 80, 60, 40, 60,
            86, 84, 79.28, 100, 80, 80, 60, 40, 60,
            86, 84, 79.28, 100, 80, 80, 60, 40, 60,
            86, 84, 79.28, 100, 80, 80, 60, 40, 60,
            86, 84, 79.28, 100, 80, 80, 60, 40, 60,
            86, 84, 79.28, 100, 80, 80, 60, 40, 60,
            86, 84, 79.28, 100, 80, 80, 60, 40, 60,
            86, 84, 79.28, 100, 80, 80, 60, 40, 60,
            100.00, 90.1, 95.2, 96.00, 78.20, 67.9,
            76, 19, 50, 60.8, 42.45, 60, 90, 99, 99.1,
            86, 84, 79.28, 100, 80, 80, 60, 40, 60,
            86, 84, 79.28, 100, 80, 80, 60, 40, 60.65,
            100.00, 90.1, 95.2, 96.00, 78.20, 67.9,
            76, 19, 50, 60.8, 42.45, 60, 90, 99, 99.1,
            86, 84, 79.28, 100, 80, 80, 60, 40, 60,
            100.00, 90.1, 95.2, 96.00, 78.20, 67.9,
            76, 19, 50, 60.8, 42.45, 60, 90, 99, 99.1,
            86, 84, 79.28, 100, 80, 80, 60, 40, 60,
            100.00, 90.1, 95.2, 96.00, 78.20, 67.9,
            76, 19, 50, 60.8, 42.45, 60, 90, 99, 99.1,
            86, 84, 79.28, 100, 80, 80, 60, 40, 60,
            86, 84, 79.28, 100, 80, 80, 60, 40, 60.65,
            100.00, 90.1, 95.2, 96.00, 78.20, 67.9,
            76, 19, 50, 60.8, 42.45, 60, 90, 99, 99.1,
            86, 84, 79.28, 100, 80, 80, 60, 40, 60,
            100.00, 90.1, 95.2, 96.00, 78.20, 67.9,
            76, 19, 50, 60.8, 42.45, 60, 90, 99, 99.1,
            86, 84, 79.28, 100, 80, 80, 60, 40, 60,
            100.00, 90.1, 95.2, 96.00, 78.20, 67.9,
            76, 19, 50, 60.8, 42.45, 60, 90, 99, 99.1,
            86, 84, 79.28, 100, 80, 80, 60, 40, 60,
            86, 84, 79.28, 100, 80, 80, 60, 40, 60.65,
            100.00, 90.1, 95.2, 96.00, 78.20, 67.9,
            76, 19, 50, 60.8, 42.45, 60, 90, 99, 99.1,
            86, 84, 79.28, 100, 80, 80, 60, 40, 60,
            100.00, 90.1, 95.2, 96.00, 78.20, 67.9,
            76, 19, 50, 60.8, 42.45, 60, 90, 99, 99.1,
            86, 84, 79.28, 100, 80, 80, 60, 40, 60,
            100.00, 90.1, 95.2, 96.00, 78.20, 67.9,
            76, 19, 50, 60.8, 42.45, 60, 90, 99, 99.1,
            86, 84, 79.28, 100, 80, 80, 60, 40, 60,
            86, 84, 79.28, 100, 80, 80, 60, 40, 60.65,
        };

        Aggregator agg = new Aggregator(marks.clone());
        double valWithoutThreads, valWithThreads;
        long start1, end1, start2, end2;

        start1 = System.nanoTime();
        valWithoutThreads =  agg.avg(false);
        end1 = System.nanoTime();

        start2 = System.nanoTime();
        valWithThreads =  agg.avg(false);
        end2 = System.nanoTime();
        
        System.out.println(String.format("The average without threads: %.2f, Took: %fms", valWithoutThreads, (end1-start1) / 1000d / 1000d));
        System.out.println(String.format("The average with threads:    %.2f, Took: %fms", valWithThreads, (end2-start2) / 1000d / 1000d));

    }
}
