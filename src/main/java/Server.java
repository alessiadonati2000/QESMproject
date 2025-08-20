import java.util.ArrayList;
import java.util.List;

public class Server {
    private int id;
    private double buffer;                     // bit
    private List<User> proposedUsers;          // Users
    final double COMPUTING_CAPACITY = 6e5;     // Hz
    final int CPU_CYCLExBIT = 10;              // cycles/bit

    public Server() {}

    public Server(int id, double buffer) {
        this.id = id;
        this.buffer = buffer;
        this.proposedUsers = new ArrayList<User>();
    }

    public int getId() {
        return id;
    }

    public double getBuffer() {
        return buffer;
    }

    public List<User> getProposedUsers() {
        return proposedUsers;
    }

    public void setBuffer(double buffer) {
        this.buffer = buffer;
    }

    public void reduceBuffer(double task) {
        if (this.buffer >= task) {
            this.buffer -= task;
        } else {
            System.out.print("Buffer's server cannot support this task");
        }
    }

    public List<Server> generateServers(int numServers, double dimBuffer) {
        List<Server> servers = new ArrayList<>();
        for (int i = 0; i < numServers; i++) {
            Server server = new Server(i+1, dimBuffer*8);
            servers.add(server);
        }
        return servers;
    }

    @Override
    public String toString() {
        return "Server " + id + " (Buffer: " + (int) buffer/8 + " Byte)";
    }


}
