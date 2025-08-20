// This class is used for create a structure where i save User, Server and a value specific for different context

public class Match {
    private User user;
    private Server server;
    private double value;

    public Match(User user, Server server, double value) {
        this.user = user;
        this.server = server;
        this.value = value;
    }
    public User getUser() {
        return user;
    }

    public Server getServer() {
        return server;
    }

    public double getValue() {
        return value;
    }
}
