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

        System.out.println("---------------USER CHOOSE A RANDOM SERVER---------------");
        for (User user : users) {
            Server randomServer = chooseRandomServer();
            if (randomServer != null) {
                randomServer.getProposedUsers().add(user);
            }
        }

        for (Server server : servers) {
            System.out.println("\n\n---------------ELABORATION IN SERVER " + server.getId() + "---------------");
            System.out.println("List of proposed users: " + server.getProposedUsers() + "\n");

            for (User proposedUser : server.getProposedUsers()) {
                /*elaboration.calculateTransmissionTime(proposedUser, server, 1);
                elaboration.calculateComputationTime(proposedUser, server, 1);
                elaboration.calculateLocalComputationTime(proposedUser, server,1);
                elaboration.calculateTransmissionEnergy(proposedUser, server, 1);
                elaboration.calculateComputationEnergy(proposedUser, server, 1);
                elaboration.calculateLocalEnergy(proposedUser, server,1);*/
            }

            for (User user : server.getProposedUsers()) {
                System.out.println("\nElaboration of " + user);
                /*totalSystemTime += elaboration.getList_value(user, server, elaboration.getTransmissionTime_listRandom());
                totalEnergy += elaboration.getList_value(user, server, elaboration.getTransmissionEnergy_listRandom());*/

                if (server.getBuffer() >= 6000000) {
                    System.out.println("User can be elaborated");
                    setValueAM(users.indexOf(user), servers.indexOf(server), 1);
                    server.reduceBuffer(user.getTask());
                    System.out.println("Remaining buffer: " + (int) server.getBuffer());
                    /*totalSystemTime += elaboration.getList_value(user, server, elaboration.getComputationTime_listRandom());
                    totalEnergy += elaboration.getList_value(user, server, elaboration.getComputationEnergy_listRandom());*/

                } else {
                    System.out.println("User cannot be elaborated\n");
                    /*totalSystemTime += elaboration.getList_value(user, server, elaboration.getLocalComputationTime_listRandom());
                    totalEnergy += elaboration.getList_value(user, server, elaboration.getLocalEnergy_listRandom());*/
                }
            }
        }

        for (Server s : servers) {
            totalUnusedBuffer += s.getBuffer();
        }

        System.out.println("\nTotal unused buffer (cumulative): " + totalUnusedBuffer);

    }

    private Server chooseRandomServer() {
        Random rand = new Random();
        Server randomServer = servers.get(rand.nextInt(servers.size()));
        return randomServer;
    }
}
