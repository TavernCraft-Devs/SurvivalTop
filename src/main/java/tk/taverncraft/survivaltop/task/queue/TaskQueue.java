package tk.taverncraft.survivaltop.task.queue;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TaskQueue {
    // prevent stats command spam by tracking stats tasks
    private final ConcurrentHashMap<Integer, Task> taskMap;

    // used to track creators of tasks that are ongoing
    private final Set<UUID> creatorList;

    public TaskQueue() {
        taskMap = new ConcurrentHashMap<>();
        creatorList = new HashSet<>();
    }

    public void addTask(int id, Task task) {
        taskMap.put(id, task);
    }

    public Task removeTask(int id) {
        return taskMap.remove(id);
    }

    public void addCreator(UUID uuid) {
        creatorList.add(uuid);
    }

    public void removeCreator(UUID uuid) {
        creatorList.remove(uuid);
    }

    public void clear() {
        taskMap.clear();
        creatorList.clear();
    }

    public boolean hasTask(int id) {
        return taskMap.containsKey(id);
    }

    public boolean hasCreator(UUID uuid) {
        return creatorList.contains(uuid);
    }
}
