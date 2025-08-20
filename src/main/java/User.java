import java.util.ArrayList;
import java.util.List;

public class User {
    private int id;
    private double task;                            // bit
    final double TRANSMISSION_POWER = 0.2;          // W
    final double LOCAL_COMPUTING_CAPACITY = 7e4;    // Hz
    final int CPU_CYCLExBIT = 10;                   // cycles/bit

    public User(){}

    public User(int id, double task) {
        this.id = id;
        this.task = task;
    }

    public int getId() {
        return id;
    }

    public double getTask() {
        return task;
    }

    public double calculateTask(double min, double max) {
        return (Math.random() * (max - min) + min) * 8;
    }

    public List<User> generateUsers(int numUsers, int minTask, int maxTask) {
        List<User> users = new ArrayList<>();
        for (int i = 0; i < numUsers; i++) {
            User user = new User(i+1, calculateTask(minTask, maxTask));
            users.add(user);
        }
        return users;
    }

    @Override
    public String toString() {
        return "User " + id + " (Task: " + (int) task/8 + " Byte)";
    }



}
