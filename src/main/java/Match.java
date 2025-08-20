// This class is used for create a structure where i user is associated with a server

import java.util.Objects;

public class Match {
    private User user;
    private Server server;

    public Match(User user, Server server) {
        this.user = user;
        this.server = server;
    }

    public User getUser() {
        return user;
    }

    public Server getServer() {
        return server;
    }

    // Fondamentale per poter usare Match come chiave in una HashMap
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Match)) return false;
        Match match = (Match) o;
        return Objects.equals(user, match.user) &&
                Objects.equals(server, match.server);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, server);
    }

    @Override
    public String toString() {
        return "Match{" +
                "user=" + user +
                ", server=" + server +
                '}';
    }
}
