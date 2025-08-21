import java.util.*;

public class Main {
    public static void main(String[] args) {
        int numUsers = 50;
        int numServer = 3;
        double sumUnusedResourcesAlgorithm = 0.0;
        double sumUnusedResourcesRandom = 0.0;
        Map<Server, Double> ruinProbabilitiesMap = new HashMap<Server, Double>();
        Map<Server, Double> loadStatusMap = new HashMap<Server, Double>();

        Elaboration elaboration = new Elaboration();

        // Generate users
        User user = new User();
        List<User> users = user.generateUsers(numUsers,30000, 100000); // Task between 30 and 100 KB
        List<User> usersRandom = deepCopyUser(users);  // In this way i have a lir/ of User with the same features generated
        for (User u : users){
            System.out.println(u);
        }

        // Generate servers
        Server server = new Server();
        List<Server> servers = server.generateServers(numServer, 1500000); // Buffer of 1 MB
        List<Server> serversRandom = deepCopyServer(servers);
        for (Server s : servers) {
            System.out.println(s);
        }

        System.out.println("\n------------------------------ASSOCIATION WITH ALGORITHM----------------------------\n");
        AlgorithmAssociation algorithmAssociation = new AlgorithmAssociation(users, servers, elaboration);
        algorithmAssociation.associationUserServer(users, servers);
        sumUnusedResourcesAlgorithm = algorithmAssociation.getTotalUnusedBuffer(); // sum of the total unused buffer
        ruinProbabilitiesMap = algorithmAssociation.getRuinProbabilityMap();
        loadStatusMap = algorithmAssociation.getLoadStatusMap();

        System.out.println("\n---------------------------------ASSOCIATION WITH RANDOM-------------------------------------\n");
        RandomAssociation randomAssociation = new RandomAssociation(usersRandom, serversRandom, algorithmAssociation.elaboration);
        randomAssociation.randomAssociation(usersRandom, serversRandom);
        sumUnusedResourcesRandom = randomAssociation.getTotalUnusedBuffer();

        System.out.println("\n----------------------RESULTS----------------------");
        System.out.println("\nNumber of associated users");
        System.out.println("Algoritm: " + algorithmAssociation.getTotalNumberAssociatedUsers());
        System.out.println("Random: " + randomAssociation.getTotalNumberAssociatedUsers());

        System.out.println("\nNumber of unused resources");
        System.out.println("Algoritm: " + (int) sumUnusedResourcesAlgorithm/8 + " Bytes");
        System.out.println("Random: " + (int) sumUnusedResourcesRandom/8 + " Bytes");

        System.out.println("\nRuin probability of servers");
        for (Server s : ruinProbabilitiesMap.keySet()){
            System.out.println("Server " + s.getId() + ": " + ruinProbabilitiesMap.get(s) + " (Associeted users: " + s.getProposedUsers().size() + ")");
        }

        System.out.println("\nLoad status of servers");
        for (Server s : loadStatusMap.keySet()){
            System.out.println("Server " + s.getId() + ": " + loadStatusMap.get(s) + " (Associeted users: " + s.getProposedUsers().size() + ")");
        }

    }

    private static List<User> deepCopyUser(List<User> originalList) {
        List<User> copiedList = new ArrayList<>();
        for (User item : originalList) {
            copiedList.add(new User(item.getId(), item.getTask()));
        }
        return copiedList;
    }

    private static List<Server> deepCopyServer(List<Server> originalList) {
        List<Server> copiedList = new ArrayList<>();
        for (Server item : originalList) {
            copiedList.add(new Server(item.getId(), item.getBuffer()));
        }
        return copiedList;
    }

}
