package packages.Thread;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicLong;

public class PrioritizedTask implements Runnable, Comparable<PrioritizedTask>{
    private final Runnable task;
    private final int priority;
    private final long sequenceNumber;
    private static final AtomicLong sequence = new AtomicLong(0);

    public PrioritizedTask(Runnable task, int priority) {
        this.task = task;
        this.priority = priority;
        this.sequenceNumber = sequence.getAndIncrement();
    }

    @Override
    public void run() {
        try{
            task.run();
        }catch (Exception e){
            throw new RejectedExecutionException(e);
        }
    }

    @Override
    public int compareTo(PrioritizedTask other) {
        int priorityCompare = Integer.compare(other.priority, this.priority);
        return priorityCompare != 0 ?
                priorityCompare :
                Long.compare(this.sequenceNumber, other.sequenceNumber);
    }

    public Runnable getTask() {
        return task;
    }

    public int getPriority(){
        return priority;
    }
}
