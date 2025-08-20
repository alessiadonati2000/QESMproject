import java.util.List;
import java.util.Random;

public class RandomAssociation extends Association{
    public RandomAssociation(List<User> user, List<Server> server, Elaboration elaboration){
        this.users = user;
        this.servers = server;
        this.associationMatrix = new int[user.size()][server.size()];
        this.elaboration = elaboration;
        this.totalUnusedBuffer = 0.0;
        this.totalSystemTime = 0.0;
        this.totalEnergy = 0.0;
    }

    public void randomAssociation(List<User> users, List<Server> servers) {
        inizializeAM();

        for (User user : users) {
            Server randomServer = chooseRandomServer();
            if (randomServer != null) {
                randomServer.getProposedUsers().add(user);
            }
        }

        for (Server server : servers) {
            System.out.println("\n\n---------------------ELABORATION IN SERVER " + server.getId() + "---------------------");
            System.out.println("List of proposed users: " + server.getProposedUsers() + "\n");

            for (User user : server.getProposedUsers()) {
                System.out.println("\nElaboration of " + user);

                if (server.getBuffer() >= 6000000) {
                    System.out.println("User can be elaborated");

                    elaboration.calculateTransmissionTime(user, server, 1);
                    System.out.printf("Transmission time: %.2e s%n", elaboration.getTransmissionTime_randomMap().get(new Match(user, server)));
                    elaboration.calculateComputationTime(user, server, 1);
                    System.out.printf("Computation time: %.2e s%n", elaboration.getComputationTime_randomMap().get(new Match(user, server)));

                    setValueAM(users.indexOf(user), servers.indexOf(server), 1);
                    server.reduceBuffer(user.getTask());

                    System.out.println("Remaining buffer: " + (int) server.getBuffer()/8 + " Bytes");

                } else {
                    System.out.println("User cannot be elaborated\n");
                    elaboration.calculateLocalComputationTime(user, 1);
                    System.out.printf("Local computation time: %.2e s%n", elaboration.getLocalComputationTime_randomMap().get(user));

                }
            }
        }

        for (Server s : servers) {
            totalUnusedBuffer += s.getBuffer();
        }

        System.out.println("\nTotal unused buffer (cumulative): " + (int) totalUnusedBuffer/8 + " Bytes");

    }

    private Server chooseRandomServer() {
        Random rand = new Random();
        Server randomServer = servers.get(rand.nextInt(servers.size()));
        return randomServer;
    }
}
