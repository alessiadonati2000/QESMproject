import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AlgorithmAssociation extends Association{
    public AlgorithmAssociation(List<User> user, List<Server> server, Elaboration elaboration){
        this.users = user;
        this.servers = server;
        this.associationMatrix = new int[user.size()][server.size()];
        this.elaboration = elaboration;
        this.totalUnusedBuffer = 0.0;
        this.totalSystemTime = 0.0;
    }

    public void associationUserServer(List<User> users, List<Server> servers) {
        List<User> unallocatedUsers = new ArrayList<>();
        inizializeAM();

        // User choose the best server for him based on SNR and buffer availability
        System.out.println("------------CALCULATING SNR BETWEEN USERS AND SERVERS------------");
        for (User user : users) {
            Server bestServer = chooseBestServer(user);
            if (bestServer != null) {
                System.out.println("User "+ user.getId() + " choose: " + bestServer.getId() + "\n");
                bestServer.getProposedUsers().add(user);
            } else {
                System.out.println("User cannot choose a server");
            }
        }

        // For each server, i'll process the proposed users
        for (Server server : servers) {
            System.out.println("\n\n---------------------ELABORATION IN SERVER " + server.getId() + "---------------------");
            System.out.println("List of proposed users: " + server.getProposedUsers() + "\n");

            System.out.println("Ruin probability: " + elaboration.calculateRuinProbability(server, 0.1));

            // Calculate some metrics for evaluation
            for (User proposedUser : server.getProposedUsers()) {
                Map<User, Double> ruinDegreeMap = elaboration.associateUserRuinDegree(proposedUser, server);
                System.out.printf("Ruin degree user %s: %.2e%n", proposedUser.getId(), ruinDegreeMap.get(proposedUser));
            }

            // Build the priority list using Ruin Theory
            // First the user with less impact on the ruin of server (the one with ruinDegree smaller)
            List<User> priorityList = elaboration.buildPriorityList(server);
            System.out.println("\nPriority list: " + priorityList);

            // Elaborate every user, if cannot put it into an unallocated list
            for (User user : priorityList) {
                System.out.println("\nElaboration of " + user);

                if (server.getBuffer() >= user.getTask()) {
                    System.out.println("User can be elaborated");

                    elaboration.calculateTransmissionTime(user, server, 0);
                    System.out.printf("Transmission time: %.2e s%n", elaboration.getList_value(user, server, elaboration.getTransmissionTime_listAlgoritm()));
                    elaboration.calculateComputationTime(user, server, 0);
                    System.out.printf("Computation time: %.2e s%n", elaboration.getList_value(user, server, elaboration.getComputationTime_listAlgoritm()));

                    setValueAM(users.indexOf(user), servers.indexOf(server), 1);
                    server.reduceBuffer(user.getTask());

                    System.out.println("Remaining buffer: " + (int) server.getBuffer()/8 + " Byte");

                } else {
                    System.out.println("User cannot be elaborated\n");
                    unallocatedUsers.add(user);
                }
            }
        }

        // Sort the unallocated user from the smallest to biggest with respect ruin theory
        System.out.println("\n\n--------------------REALLOCATION--------------------------");
        unallocatedUsers = elaboration.sortUnallocatedUsersByTask(unallocatedUsers);
        System.out.println("List of unallocated users: " + unallocatedUsers);

        for (User user : unallocatedUsers) {
            System.out.println("\nElaboration of " + user);
            Server newServer = chooseSecondBestServer(user);
            System.out.println("User choose: " + newServer + "\n");

            if (newServer != null && newServer.getBuffer() >= user.getTask()) {
                System.out.println("User can be elaborated");

                elaboration.calculateTransmissionTime(user, newServer, 0);
                System.out.printf("Transmission time: %.2e s%n", elaboration.getList_value(user, newServer, elaboration.getTransmissionTime_listAlgoritm()));
                elaboration.calculateComputationTime(user, newServer, 0);
                System.out.printf("Computation time: %.2e s%n", elaboration.getList_value(user, newServer, elaboration.getComputationTime_listAlgoritm()));

                setValueAM(users.indexOf(user), servers.indexOf(newServer), 1);
                newServer.reduceBuffer(user.getTask());

                System.out.println("Remaining buffer: " + (int) newServer.getBuffer()/8 + " Byte");

            } else if (newServer == null){
                System.out.println("User cannot be elaborated");
                elaboration.calculateLocalComputationTime(user, 0);
                System.out.printf("Local computation time: %.2e s%n ", elaboration.getList_value(user, newServer, elaboration.getComputationTime_listAlgoritm()));

            } else {
                System.out.println("User cannot be elaborated");
                elaboration.calculateLocalComputationTime(user, 0);
                System.out.printf("Local computation time: %.2e s%n", elaboration.getList_value(user, newServer, elaboration.getComputationTime_listAlgoritm()));
            }
        }

        for (Server s : servers) {
            totalUnusedBuffer += s.getBuffer();
        }
        System.out.println("\nTotal unused buffer (cumulative): " + (int) totalUnusedBuffer/8 + " Bytes");

    }

    // Function to found the best server based on SNR and buffer availability
    private Server chooseBestServer(User user) {
        Server bestServer = null;
        double bestMetric  = Double.NEGATIVE_INFINITY;

        for (Server server : servers) {
            double snr_value = elaboration.calculateSNR(user, server);
            double bufferAvailability = server.getBuffer() - user.getTask();

            if (bufferAvailability >= 0) {
                double metric = snr_value / (1 + Math.abs(bufferAvailability));
                if (metric > bestMetric) {
                    bestMetric = metric;
                    bestServer = server;
                }
            } else {
                System.out.println("Buffer cannot process task " + user.getTask() + " of " + user);
            }
        }
        return bestServer;
    }

    private Server chooseSecondBestServer(User user) {
        Server bestServer = null;
        double bestMetric  = Double.NEGATIVE_INFINITY;
        List<Match> snr_list = elaboration.getSNR_list();

        for (Server server : servers) {
            double bufferAvailability = server.getBuffer() - user.getTask();

            if (bufferAvailability >= 0) {
                double metric = elaboration.getList_value(user, server, snr_list) / (1 + Math.abs(bufferAvailability));

                if (metric > bestMetric) {
                    bestMetric = metric;
                    bestServer = server;
                }
            }
        }
        return bestServer;
    }

}
