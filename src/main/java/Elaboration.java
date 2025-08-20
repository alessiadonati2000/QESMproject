import java.util.*;

public class Elaboration {
    private Map<User, Double> ruinDegreeMap;
    private List<Match> snr_list;
    final double BANDWIDTH = 20e6;         // Hz
    final double COSTANT_CHIP = 1e-28;

    public Elaboration(){
        this.ruinDegreeMap = new HashMap<>();
        this.snr_list = new ArrayList<>();
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




}
