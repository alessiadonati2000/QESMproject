import java.util.*;

public class MainMean {

    public static void main(String[] args) {
        int numSimulations = 500;  // Number of simulation, max value 500, beyond that nothing changes
        int numServer = 3;
        int maxUser = 100;
        int step = 5;
        int[] meanAssociatedUsersAlgoritm = new int[maxUser/step + 1];
        int[] meanAssociatedUsersRandom = new int[maxUser/step + 1];
        int[] meanUnusedResourcesAlgoritm = new int[maxUser/step + 1];
        int[] meanUnusedResourcesRandom = new int[maxUser/step + 1];
        double[] meanRuinProbabilityS1 = new double[maxUser/step + 1];
        double[] meanRuinProbabilityS2 = new double[maxUser/step + 1];
        double[] meanRuinProbabilityS3 = new double[maxUser/step + 1];
        double[] meanLoadStatusS1 = new double[maxUser/step + 1];
        double[] meanLoadStatusS2 = new double[maxUser/step + 1];
        double[] meanLoadStatusS3 = new double[maxUser/step + 1];
        /*int[] meanTotalSystemTimeAlgoritm = new int[maxUser/step + 1];
        int[] meanTotalSystemTimeRandom = new int[maxUser/step + 1];
        int[] meanTotalEnergyAlgoritm = new int[maxUser/step + 1];
        int[] meanTotalEnergyRandom = new int[maxUser/step + 1];*/
        int index = 0;

        // I want to run the 500 simulations in a single run, in each varying number of users, thus obtaining the results that will be saved in the previous arrays
        // Im gonna try 5 by 5 -> 5, 10, 15, 20, 25...
        for (int numUsers = 5; numUsers <= maxUser; numUsers += step){
            int sumAssociatedUsersAlgoritm = 0;
            int sumAssociatedUsersRandom = 0;
            double sumUnusedResourcesAlgoritm = 0.0;
            double sumUnusedResourcesRandom = 0.0;
            double[] sumRuinProbability = new double[maxUser/step + 1];
            double[] sumLoadStatus = new double[maxUser/step + 1];
            /*double sumTotalSystemTimeAlgoritm = 0.0;
            double sumTotalSystemTimeRandom = 0.0;
            double sumTotalEnergyAlgoritm = 0.0;
            double sumTotalEnergyRandom = 0.0;*/

            // The results will be averaged over a large number of simulations to normalize the data
            for(int i = 0; i < numSimulations; i++) {
                Elaboration elaboration = new Elaboration();

                // Generate users
                User user = new User();
                List<User> users = user.generateUsers(numUsers,30000, 100000); // Task between 30 and 100 KB
                List<User> usersRandom = deepCopyUser(users);  // In this way i have a list of User with the same features generated
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

                System.out.println("\n---------------------ASSOCIATION WITH ALGORITM---------------------\n");
                AlgorithmAssociation algorithmAssociation = new AlgorithmAssociation(users, servers, elaboration);
                algorithmAssociation.associationUserServer(users, servers);
                sumAssociatedUsersAlgoritm += algorithmAssociation.getTotalNumberAssociatedUsers();      // sum of the number of associated users
                sumUnusedResourcesAlgoritm += algorithmAssociation.getTotalUnusedBuffer();               // sum of the total unused buffer
                /*sumTotalSystemTimeAlgoritm += algoritmAssociation.getTotalSystemTime();                 // sum of the total system time
                sumTotalEnergyAlgoritm += algoritmAssociation.getTotalEnergy();  */                       // sum of the total energy
                Map<Server, Double> ruinProbabilitiesMap = algorithmAssociation.getRuinProbabilityMap();
                int j = 0;
                for (Server s : ruinProbabilitiesMap.keySet()){
                    sumRuinProbability[j] += ruinProbabilitiesMap.get(s);
                    j++;
                }
                Map<Server, Double> loadStatusMap = algorithmAssociation.getLoadStatusMap();
                int y = 0;
                for (Server s : loadStatusMap.keySet()){
                    sumLoadStatus[y] += loadStatusMap.get(s);
                    y++;
                }

                System.out.println("\n----------------------ASSOCIATION WITH RANDOM-----------------------\n");
                RandomAssociation randomAssociation = new RandomAssociation(usersRandom, serversRandom, algorithmAssociation.elaboration);
                randomAssociation.randomAssociation(usersRandom, serversRandom);
                sumAssociatedUsersRandom += randomAssociation.getTotalNumberAssociatedUsers();
                sumUnusedResourcesRandom += randomAssociation.getTotalUnusedBuffer();
                /*sumTotalSystemTimeRandom += randomAssociation.getTotalSystemTime();
                sumTotalEnergyRandom += randomAssociation.getTotalEnergy();*/

            } // END SIMULATIONS

            // I find the average number of users associated for all users
            meanAssociatedUsersAlgoritm[index] = sumAssociatedUsersAlgoritm / numSimulations;
            meanAssociatedUsersRandom[index] = sumAssociatedUsersRandom / numSimulations;

            meanUnusedResourcesAlgoritm[index] = (int) (sumUnusedResourcesAlgoritm / numSimulations)/8;
            meanUnusedResourcesRandom[index] = (int) (sumUnusedResourcesRandom / numSimulations)/8;

            meanRuinProbabilityS1[index] = sumRuinProbability[0]/numSimulations;
            meanRuinProbabilityS2[index] = sumRuinProbability[1]/numSimulations;
            meanRuinProbabilityS3[index] = sumRuinProbability[2]/numSimulations;

            meanLoadStatusS1[index] = sumLoadStatus[0]/numSimulations;
            meanLoadStatusS2[index] = sumLoadStatus[1]/numSimulations;
            meanLoadStatusS3[index] = sumLoadStatus[2]/numSimulations;

            /*meanTotalSystemTimeAlgoritm[index] = (int) sumTotalSystemTimeAlgoritm / numSimulations;
            meanTotalSystemTimeRandom[index] = (int) sumTotalSystemTimeRandom / numSimulations;

            meanTotalEnergyAlgoritm[index] = (int) sumTotalEnergyAlgoritm / numSimulations;
            meanTotalEnergyRandom[index] = (int) sumTotalEnergyRandom / numSimulations;*/

            index++;

        } // END SIMULATIONS

        System.out.println("Number of associated users");
        System.out.println("Algoritm: " + Arrays.toString(meanAssociatedUsersAlgoritm));
        System.out.println("Random: " + Arrays.toString(meanAssociatedUsersRandom));

        System.out.println("\nNumber of unused resources (BYTE)");
        System.out.println("Algoritm: " + Arrays.toString(meanUnusedResourcesAlgoritm));
        System.out.println("Random: " + Arrays.toString(meanUnusedResourcesRandom));

        System.out.println("\nRuin probabilities");
        System.out.println("s1: " + Arrays.toString(meanRuinProbabilityS1));
        System.out.println("s2: " + Arrays.toString(meanRuinProbabilityS2));
        System.out.println("s3: " + Arrays.toString(meanRuinProbabilityS3));

        System.out.println("\nLoad status");
        System.out.println("s1: " + Arrays.toString(meanLoadStatusS1));
        System.out.println("s2: " + Arrays.toString(meanLoadStatusS2));
        System.out.println("s3: " + Arrays.toString(meanLoadStatusS3));

        /*System.out.println("\nTotal System Time");
        System.out.println("Algoritm: " + Arrays.toString(meanTotalSystemTimeAlgoritm));
        System.out.println("Random: " + Arrays.toString(meanTotalSystemTimeRandom));

        System.out.println("\nTotal Energy");
        System.out.println("Algoritm: " + Arrays.toString(meanTotalEnergyAlgoritm));
        System.out.println("Random: " + Arrays.toString(meanTotalEnergyRandom));*/

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
