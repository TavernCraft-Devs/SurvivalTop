package tk.taverncraft.survivaltop.task.queue;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Contains information related to each stats calculation task being performed currently.
 */
public class Task {
    // counter for unique task id
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

    /**
     * Gets name of entity that task is being performed for.
     *
     * @return name of entity
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the start time of the task.
     *
     * @return start time of task
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * Gets the type of task.
     *
     * @return type of task
     */
    public TaskType getType() {
        return type;
    }

    /**
     * Gets the id of the task.
     *
     * @return task id
     */
    public int getTaskId() {
        return taskId;
    }
}
