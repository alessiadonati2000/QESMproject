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
    public static double calculateRho(Server server) {
        double totalTask = 0.0;
        double time = 150;
        for(User user : server.getProposedUsers()){
            totalTask += user.getTask();
        }

        double Cn = server.COMPUTING_CAPACITY;
        double mu0 = server.CPU_CYCLExBIT;

        double lambdaInBitsPerSec = totalTask / time;
        double muServBitsPerSec   = Cn / mu0;

        return lambdaInBitsPerSec / muServBitsPerSec; // = (mu0/Cn) * (totalBits / t)
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
    /*public double calculateRuinProbability(Server server, double time) {
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
    }*/

    public static double calculateRuinProbability(Server server) {
        if (server == null) return 0.0;

        double Bn = server.getBuffer();
        double Cn = server.COMPUTING_CAPACITY;
        int Kn = Math.max(0, server.getProposedUsers().size());
        double mu0 = server.CPU_CYCLExBIT;
        double tau = 0.1;

        if (Kn == 0) return 0.0;

        double gamma_k_n = Cn / (double) Kn;
        double premiumPerSlotBits = gamma_k_n * tau / mu0;
        double c1 = Bn + premiumPerSlotBits;
        int n = Kn;

        double meanTaskBits = 65000 * 8;
        double mu = 1.0 / meanTaskBits;
        double muPrime = mu;

        double sum = 0.0;
        for (int j = 1; j <= n; j++) {
            int jm1 = j - 1;
            double cj = Bn + j * premiumPerSlotBits;

            // calcolo log-term per stabilità numerica:
            // lnTerm = (j-1)*ln(mu*cj) - ln((j-1)!) - mu' * cj
            double lnMuCj = Math.log(mu * cj);
            double lnFactorial = logFactorial(jm1);
            double lnTerm = (jm1 * lnMuCj) - lnFactorial - (muPrime * cj);

            // term = exp(lnTerm) * (c1 / cj)
            double term = Math.exp(lnTerm) * (c1 / cj);

            // difensive checks: se term è NaN/Inf -> salta o normalizza
            if (Double.isFinite(term)) {
                sum += term;
            } else {
                // se numericamente troppo piccolo/grande, ignora o gestisci:
                // se exp overflow -> term ~ +inf; in pratica la probabilità satura -> return large value
                // ma per sicurezza saltiamo termini non-finiti
            }
        }

        // la formula fornisce direttamente la probabilità (non clamped) ma per sicurezza clamp in [0,1]
        double ruinProbability = Math.max(0.0, Math.min(1.0, sum));
        return ruinProbability;
    }

    /**
     * Calcola ln(n!) sommando logaritmi (sufficientemente efficiente per n fino a qualche migliaio).
     * Se vuoi ottimizzare per n grandi, sostituisci con approssimazione di Stirling o caching.
     */
    private static double logFactorial(int n) {
        if (n <= 1) return 0.0;
        double s = 0.0;
        for (int k = 2; k <= n; k++) s += Math.log(k);
        return s;
    }

    public double calculateRuinDegree(User user, Server server) {
        double ruinProbability = calculateRuinProbability(server);

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
