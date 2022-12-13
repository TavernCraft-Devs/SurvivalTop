package tk.taverncraft.survivaltop.task;

import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;
import tk.taverncraft.survivaltop.Main;
import tk.taverncraft.survivaltop.task.processor.TaskProcessor;
import tk.taverncraft.survivaltop.task.queue.Task;
import tk.taverncraft.survivaltop.task.queue.TaskQueue;
import tk.taverncraft.survivaltop.task.queue.TaskType;

public class TaskManager {
    private final Main main;
    private final TaskQueue taskQueue;
    private final TaskProcessor taskProcessor;

    public TaskManager(Main main) {
        this.main = main;
        taskQueue = new TaskQueue();
        taskProcessor = new TaskProcessor(main, taskQueue);
    }

    /**
     * Creates task for getting stats of entity.
     *
     * @param name name of entity
     * @param type type of task
     */
    public void createTask(CommandSender sender, String name, TaskType type) {
        Task task = new Task(name, type);
        int id = task.getTaskId();
        taskQueue.addCreator(main.getSenderUuid(sender));
        taskQueue.addTask(id, task);
        new BukkitRunnable() {
            @Override
            public void run() {
                taskProcessor.calculateEntityStats(sender, name, id);
            }
        }.runTaskAsynchronously(main);
    }

    /**
     * Checks if sender has an ongoing calculation.
     *
     * @param sender sender who requested for stats
     *
     * @return true if there is an ongoing calculation for the sender, false otherwise
     */
    public boolean hasCreator(CommandSender sender) {
        return taskQueue.hasCreator(main.getSenderUuid(sender));
    }

    /**
     * Sets the state for calculations to stop or continue.
     */
    public void stopAllTasks() {
        taskQueue.clear();
    }
}
