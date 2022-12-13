package tk.taverncraft.survivaltop.task.queue;

/**
 * Enum representing the 2 types of tasks. PLAYER refers to stats calculations requested by a user.
 * LEADERBOARD refers to stats calculations done to update the leaderboard.
 */
public enum TaskType {
    PLAYER,
    LEADERBOARD
}
