import java.util.*;

public class Elaboration {
    private Map<User, Double> ruinDegreeMap;
    private List<Match> snr_list;
    final double BANDWIDTH = 20e6;         // Hz
    final double COSTANT_CHIP = 1e-28;

    private List<Match> transmissionTime_listAlgoritm;
    private List<Match> transmissionTime_listRandom;
    private List<Match> computationTime_listAlgoritm;
    private List<Match> computationTime_listRandom;
    private List<Match> localComputationTime_listAlgoritm;
    private List<Match> localComputationTime_listRandom;

    public Elaboration(){
        this.ruinDegreeMap = new HashMap<>();
        this.snr_list = new ArrayList<>();
        this.transmissionTime_listAlgoritm = new ArrayList<>();
        this.transmissionTime_listRandom = new ArrayList<>();
        this.computationTime_listAlgoritm = new ArrayList<>();
        this.computationTime_listRandom = new ArrayList<>();
        this.localComputationTime_listAlgoritm = new ArrayList<>();
        this.localComputationTime_listRandom = new ArrayList<>();
    }

    public double getList_value(User user, Server server, List<Match> list) {
        for (Match match : list) {
            double userMatchId = match.getUser().getId();
            double serverMatchId = match.getServer().getId();
            if(userMatchId == user.getId() && serverMatchId == server.getId()) {
                return match.getValue();
            }
        }
        throw new IllegalArgumentException("No corresponding value found");
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public double calculateSNR(User user, Server server) {
        // SNR is calculated in a randomic way because i did not implement the concept of distance between users and servers
        // Higher SNR is, best connection between user and server is
        Random rng = new Random();
        double snrDb = rng.nextDouble() * 30.0;             // [0,30) dB
        double snr_value = Math.pow(10.0, snrDb / 10.0);    // linear
        System.out.println("SNR between user " + user.getId() + " and server " + server.getId() + ": " + (int) snr_value);
        snr_list.add(new Match(user, server, snr_value));
        return snr_value;
    }

    public List<Match> getSNR_list() {
        return snr_list;
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

        double ruinDegree = user.getTask() / ruinProbability;
        ruinDegreeMap.put(user, ruinDegree);

        return ruinDegree;
    }

    public Map<User, Double> associateUserRuinDegree(User user, Server server) {
        Map<User, Double> ruinDegreeMap = new HashMap<>();
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
        double uplinkDataRate = (BANDWIDTH / server.getProposedUsers().size()) * (Math.log(1 + getList_value(user, server, snr_list)) / Math.log(2));
        double transmissionTime_value = user.getTask() / uplinkDataRate;

        if (flag == 0) {
            transmissionTime_listAlgoritm.add(new Match(user, server, transmissionTime_value));
        } else if (flag == 1) {
            transmissionTime_listRandom.add(new Match(user, server, transmissionTime_value));
        }
    }

    public List<Match> getTransmissionTime_listAlgoritm() {
        return transmissionTime_listAlgoritm;
    }
    public List<Match> getTransmissionTime_listRandom() {
        return transmissionTime_listRandom;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // REMOTE COMPUTATION

    public void calculateComputationTime(User user, Server server, int flag) {
        double computationTime_value = (server.CPU_CYCLExBIT * user.getTask()) / (server.COMPUTING_CAPACITY/server.getProposedUsers().size());

        if (flag == 0) {
            computationTime_listAlgoritm.add(new Match(user, server, computationTime_value));
        } else if (flag == 1) {
            computationTime_listRandom.add(new Match(user, server, computationTime_value));
        }
    }

    public List<Match> getComputationTime_listAlgoritm(){
        return computationTime_listAlgoritm;
    }
    public List<Match> getComputationTime_listRandom(){
        return computationTime_listRandom;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // LOCAL COMPUTATION

    public void calculateLocalComputationTime(User user, int flag){
        double localComputationTime_value = (user.CPU_CYCLExBIT * user.getTask()) / user.LOCAL_COMPUTING_CAPACITY;

        if (flag == 0) {
            localComputationTime_listAlgoritm.add(new Match(user, null, localComputationTime_value));
        } else if (flag == 1) {
            localComputationTime_listRandom.add(new Match(user, null, localComputationTime_value));
        }
    }

    public List<Match> getLocalComputationTime_listAlgoritm(){
        return localComputationTime_listAlgoritm;
    }
    public List<Match> getLocalComputationTime_listRandom(){
        return localComputationTime_listRandom;
    }



}
