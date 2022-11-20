package tk.taverncraft.survivaltop.stats.task;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

public class Task {
    private static AtomicInteger taskIdCounter = new AtomicInteger(0);

    private final String name;
    private final int taskId;
    private final long startTime;
    private final TaskType type;

    /**
     * Constructor for Task.
     *
     * @param name name of entity to create task for
     * @param type type of task
     */
    public Task(String name, TaskType type) {
        this.name = name;
        this.taskId = taskIdCounter.getAndIncrement();
        this.startTime = Instant.now().getEpochSecond();
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public long getStartTime() {
        return startTime;
    }

    public TaskType getType() {
        return type;
    }

    public int getTaskId() {
        return taskId;
    }
}
