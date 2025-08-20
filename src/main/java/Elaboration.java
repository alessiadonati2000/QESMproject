import java.util.*;

public class Elaboration {
    private Map<User, Double> ruinDegreeMap;
    private Map<Match, Double> snrMap;
    private Map<Match, Double> transmissionTime_algorithmMap;
    private Map<Match, Double> transmissionTime_randomMap;
    private Map<Match, Double> computationTime_algorithmMap;
    private Map<Match, Double> computationTime_randomMap;
    private Map<User, Double> localComputationTime_algorithmMap;
    private Map<User, Double> localComputationTime_randomMap;

    final double BANDWIDTH = 20e6;         // Hz
    final double COSTANT_CHIP = 1e-28;

    public Elaboration(){
        this.ruinDegreeMap = new HashMap<>();
        this.snrMap = new HashMap<>();
        this.transmissionTime_algorithmMap = new HashMap<>();
        this.transmissionTime_randomMap = new HashMap<>();
        this.computationTime_algorithmMap = new HashMap<>();
        this.computationTime_randomMap = new HashMap<>();
        this.localComputationTime_algorithmMap = new HashMap<>();
        this.localComputationTime_randomMap = new HashMap<>();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public double calculateSNR(User user, Server server) {
        // SNR is calculated in a randomic way because i did not implement the concept of distance between users and servers
        // Higher SNR is, best connection between user and server is
        Random rng = new Random();
        double snrDb = rng.nextDouble() * 30.0;             // [0,30) dB
        double snr_value = Math.pow(10.0, snrDb / 10.0);    // linear
        System.out.println("SNR between user " + user.getId() + " and server " + server.getId() + ": " + (int) snr_value);
        snrMap.put(new Match(user, server), snr_value);
        return snr_value;
    }

    public Map<Match, Double> get_snrMap() {
        return snrMap;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public double calculateRuinProbability(Server server, double time) {
        // Using a sigmoide to model probability, different from the paper
        double totalArrivalData = 0;
        for (User user : server.getProposedUsers()){
            totalArrivalData += user.getTask();
        }
        double initialSurplus = server.getBuffer();

        double term = (initialSurplus - totalArrivalData) / (((server.COMPUTING_CAPACITY/server.getProposedUsers().size()) * time) / server.CPU_CYCLExBIT);
        double ruinProbability = 1 / (1 + Math.exp(term));

        // To limit the ruin probability to 1
        ruinProbability = Math.min(ruinProbability, 1);

        return ruinProbability;
    }

    public double calculateRuinDegree(User user, Server server) {
        double ruinProbability = calculateRuinProbability(server, 0.1);

        // To avoid a division by 0
        if (ruinProbability == 0) {
            ruinProbability = 0.01;
        }

        return user.getTask() / ruinProbability;
    }

    public Map<User, Double> associateUserRuinDegree(User user, Server server) {
        double ruinDegree = calculateRuinDegree(user, server);
        ruinDegreeMap.put(user, ruinDegree);
        return ruinDegreeMap;
    }

    public List<User> buildPriorityList(Server server) {
        server.getProposedUsers().sort(Comparator.comparing(user -> associateUserRuinDegree(user, server).get(user)));
        return server.getProposedUsers();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public List<User> sortUnallocatedUsersByTask(List<User> unallocatedUsers) {
        unallocatedUsers.sort(Comparator.comparing(User::getTask));
        return unallocatedUsers;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // TRANSMISSION

    public void calculateTransmissionTime(User user, Server server, int flag) {
        double uplinkDataRate = (BANDWIDTH / server.getProposedUsers().size()) * (Math.log(1 + snrMap.get(new Match(user, server))) / Math.log(2));
        double transmissionTime_value = user.getTask() / uplinkDataRate;

        if (flag == 0) {
            transmissionTime_algorithmMap.put(new Match(user, server), transmissionTime_value);
        } else if (flag == 1) {
            transmissionTime_randomMap.put(new Match(user, server), transmissionTime_value);
        }
    }

    public Map<Match, Double> getTransmissionTime_algorithmMap() {
        return transmissionTime_algorithmMap;
    }
    public Map<Match, Double> getTransmissionTime_randomMap() {
        return transmissionTime_randomMap;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // REMOTE COMPUTATION

    public void calculateComputationTime(User user, Server server, int flag) {
        double computationTime_value = (server.CPU_CYCLExBIT * user.getTask()) / (server.COMPUTING_CAPACITY/server.getProposedUsers().size());

        if (flag == 0) {
            computationTime_algorithmMap.put(new Match(user, server), computationTime_value);
        } else if (flag == 1) {
            computationTime_randomMap.put(new Match(user, server), computationTime_value);
        }
    }

    public Map<Match, Double> getComputationTime_algorithmMap() {
        return computationTime_algorithmMap;
    }
    public Map<Match, Double> getComputationTime_randomMap() {
        return computationTime_randomMap;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // LOCAL COMPUTATION

    public void calculateLocalComputationTime(User user, int flag){
        double localComputationTime_value = (user.CPU_CYCLExBIT * user.getTask()) / user.LOCAL_COMPUTING_CAPACITY;

        if (flag == 0) {
            localComputationTime_algorithmMap.put(user, localComputationTime_value);
        } else if (flag == 1) {
            localComputationTime_randomMap.put(user, localComputationTime_value);
        }
    }

    public Map<User, Double> getLocalComputationTime_algorithmMap(){
        return localComputationTime_algorithmMap;
    }
    public Map<User, Double> getLocalComputationTime_randomMap(){
        return localComputationTime_randomMap;
    }

}
