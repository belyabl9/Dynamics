package dynamics;

import com.google.common.base.Optional;
import com.google.common.base.Stopwatch;
import com.m1namoto.api.AnomalyDetector;
import com.m1namoto.dao.HibernateUtil;
import com.m1namoto.domain.Feature;
import com.m1namoto.domain.User;
import com.m1namoto.entity.FeatureType;
import com.m1namoto.service.*;
import com.m1namoto.service.verification.VerificationService;
import com.m1namoto.service.verification.VerificationType;
import dynamics.samepass.DataSetRecord;
import dynamics.samepass.DataSetRecordIterator;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


public class SamePasswordDataTest {

    private static final UserService USER_SERVICE = UserService.getInstance();

    private static final String PASSWORD = ".tie5Roanl";

    private static final int LEARNING_RATE = 200;

    private static Transaction transaction;

    private SessionFactory sessionFactory = HibernateUtil.getSessionFactory();

    private Session currentSession;

    @Before
    public void setUp() {
        currentSession = sessionFactory.getCurrentSession();
        transaction = currentSession.beginTransaction();
    }

    @After
    public void cleanUp() {
        if (transaction != null) {
            transaction.rollback();
        }
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        Session currentSession = sessionFactory.getCurrentSession();
        currentSession.close();
    }

    private static Map<String, User> CACHED_USERS = new HashMap<>();

    private static final List<VerificationType> VERIFICATION_TYPES = Arrays.asList(
            VerificationType.MANHATTAN,
            VerificationType.MANHATTAN_SCALED,
            VerificationType.MAHANABOLIS
    );

    private static final Map<VerificationType, Double> VERIFICATION_TYPE_THRESHOLDS = new HashMap<>();

    static {
        VERIFICATION_TYPE_THRESHOLDS.put(VerificationType.MANHATTAN, 0.8d);
        VERIFICATION_TYPE_THRESHOLDS.put(VerificationType.MANHATTAN_SCALED, 38.5d);
        VERIFICATION_TYPE_THRESHOLDS.put(VerificationType.MAHANABOLIS, 8.5d);
    }

    private static List<DataSetRecord> records;

    @Test
    public void run() {
        records = readRecords();

        registerTemplates();

//        for (VerificationType verificationType : VERIFICATION_TYPES) {
        for (VerificationType verificationType : Arrays.asList(VerificationType.DTW)) {
            System.out.println("Distance algorithm: " + verificationType.name());

            Stopwatch stopwatch = Stopwatch.createStarted();
            AnomalyDetector anomalyDetector = new VerificationService(verificationType);
//            PerformanceResult result = evaluate(anomalyDetector, Optional.of(VERIFICATION_TYPE_THRESHOLDS.get(verificationType)));
            PerformanceResult result = evaluate(anomalyDetector, Optional.<Double>absent());

//            double threshold = 1d;
//            boolean processedAll = false;
//            while (!processedAll) {
//                PerformanceResult result = evaluate(anomalyDetector, Optional.of(threshold));
//                if (result.getFar() == 1d) {
//                    processedAll = true;
//                }
//
//                System.out.println("FRR: " + result.getFrr());
//                System.out.println("FAR: " + result.getFar());
//
//                threshold += 0.1d;
//            }

            System.out.println("FRR: " + result.getFrr());
            System.out.println("FAR: " + result.getFar());
            System.out.println("Executed in " + stopwatch);
        }
    }

    private List<DataSetRecord> readRecords() {
        List<DataSetRecord> records = new ArrayList<>();
        DataSetRecordIterator dataSetRecordIterator = new DataSetRecordIterator(
                SamePasswordDataTest.class.getResourceAsStream("/SamePasswordData.csv")
        );
        while (dataSetRecordIterator.hasNext()) {
            records.add(dataSetRecordIterator.next());
        }
        return records;
    }

    private void registerTemplates() {
        User curUser = null;
        int processedRecordsPerUser = 0;
        for (DataSetRecord record : records) {
            String userLogin = record.getLogin();
            if (!CACHED_USERS.containsKey(userLogin)) {
                curUser = createUser(userLogin);
                curUser.setPassword(PASSWORD);
                CACHED_USERS.put(userLogin, curUser);
                processedRecordsPerUser = 0;
            }

            if (processedRecordsPerUser >= LEARNING_RATE) {
                continue;
            }

            saveSession(curUser, record);

            processedRecordsPerUser++;
        }
    }

    private double evalFrr(@NotNull AnomalyDetector anomalyDetector, Optional<Double> maxAllowedDistanceOpt) {
        int processedRecordsPerUser = 0;
        AuthResult totalAuthResult = new AuthResult();
        Map<String, AuthResult> userAuthResultMap = new HashMap<>();
        for (DataSetRecord record : records) {
            String userLogin = record.getLogin();
            if (!userAuthResultMap.containsKey(userLogin)) {
                userAuthResultMap.put(userLogin, new AuthResult());
                processedRecordsPerUser = 0;
            }

            if (processedRecordsPerUser < LEARNING_RATE) {
                processedRecordsPerUser++;
                continue;
            }

            User user = CACHED_USERS.get(userLogin);
            boolean isAnomaly = anomalyDetector.isAnomaly(makeTestFeatureValuesMap(record), user, maxAllowedDistanceOpt);
            if (isAnomaly) {
                totalAuthResult.incFailed();
                userAuthResultMap.get(userLogin).incFailed();
            } else {
                totalAuthResult.incSuccessful();
                userAuthResultMap.get(userLogin).incSuccessful();

                // Update template
                // saveSession(user, record);
            }

            processedRecordsPerUser++;
        }

        return totalAuthResult.getFailed() / (double) totalAuthResult.getTotal();
    }

    private double evalFar(@NotNull AnomalyDetector anomalyDetector, Optional<Double> maxAllowedDistanceOpt) {
        int processedRecordsPerUser = 0;
        final AuthResult totalAuthResult = new AuthResult();
        final Map<String, AuthResult> userAuthResultMap = new ConcurrentHashMap<>();
        for (DataSetRecord record : records) {
            final String userLogin = record.getLogin();

            if (!userAuthResultMap.containsKey(userLogin)) {
                userAuthResultMap.put(userLogin, new AuthResult());
                processedRecordsPerUser = 0;
            }

            final Map<FeatureType, List<Double>> testFeatureValuesMap = makeTestFeatureValuesMap(record);

            for (String login : CACHED_USERS.keySet()) {
                if (login.equals(userLogin)) {
                    continue;
                }

                boolean isAnomaly = anomalyDetector.isAnomaly(testFeatureValuesMap, CACHED_USERS.get(login), maxAllowedDistanceOpt);
                if (isAnomaly) {
                    totalAuthResult.incFailed();
                    userAuthResultMap.get(userLogin).incFailed();
                } else {
                    totalAuthResult.incSuccessful();
                    userAuthResultMap.get(userLogin).incSuccessful();
                }

            }

            if (processedRecordsPerUser > 5) {
                processedRecordsPerUser++;
                continue;
            }

            processedRecordsPerUser++;
        }

        return totalAuthResult.getSuccessful() / (double) totalAuthResult.getTotal();
    }

    @NotNull
    private Map<FeatureType, List<Double>> makeTestFeatureValuesMap(DataSetRecord record) {
        Map<FeatureType, List<Double>> testFeatureValuesMap = new HashMap<>();
        for (FeatureType featureType : FeatureSelectionService.getInstance().getFeatureTypes()) {
            List<Double> values;
            switch (featureType) {
                case HOLD:
                    values = new ArrayList<>();
                    for (Feature feature : record.getHoldFeatures()) {
                        values.add(feature.getValue());
                    }
                    testFeatureValuesMap.put(featureType, values);
                    break;
                case RELEASE_PRESS:
                    values = new ArrayList<>();
                    for (Feature feature : record.getReleasePressFeatures()) {
                        values.add(feature.getValue());
                    }
                    testFeatureValuesMap.put(featureType, values);
                    break;
                case PRESS_PRESS:
                    values = new ArrayList<>();
                    for (Feature feature : record.getPressPressFeatures()) {
                        values.add(feature.getValue());
                    }
                    testFeatureValuesMap.put(featureType, values);
                    break;
                default:
                    throw new UnsupportedOperationException("Specified feature type is not supported.");
            }
        }
        return testFeatureValuesMap;
    }

    private void saveSession(User curUser, DataSetRecord record) {
        com.m1namoto.domain.Session session = new com.m1namoto.domain.Session(curUser);
        SessionService.save(session);

        for (Feature feature : record.getHoldFeatures()) {
            feature.setSession(session);
            feature.setUser(curUser);
            FeatureService.getInstance().save(feature);
        }

        for (Feature feature : record.getReleasePressFeatures()) {
            feature.setSession(session);
            feature.setUser(curUser);
            FeatureService.getInstance().save(feature);
        }

        for (Feature feature : record.getPressPressFeatures()) {
            feature.setSession(session);
            feature.setUser(curUser);
            FeatureService.getInstance().save(feature);
        }

        transaction.commit();
        currentSession = sessionFactory.getCurrentSession();
        transaction = currentSession.beginTransaction();
    }

    private PerformanceResult evaluate(@NotNull AnomalyDetector anomalyDetector, Optional<Double> maxAllowedDistanceOpt) {
        return new PerformanceResult(
                evalFrr(anomalyDetector, maxAllowedDistanceOpt),
                evalFar(anomalyDetector, maxAllowedDistanceOpt)
        );
    }

//    private List<PerformanceResult> evaluate(@NotNull AnomalyDetector anomalyDetector, @NotNull List<Double> maxAllowedDistanceRange) {
//        List<PerformanceResult> results = new ArrayList<>();
//
//        for (double maxAllowedDistance : maxAllowedDistanceRange) {
//            results.add(new PerformanceResult(
//                    evalFrr(anomalyDetector, maxAllowedDistance),
//                    evalFar(anomalyDetector, maxAllowedDistance)
//            ));
//        }
//
//        return results;
//    }

    @NotNull
    private User createUser(String userLogin) {
        User user = new User();
        user.setName(userLogin + " " + userLogin);
        user.setLogin(userLogin);
        user.setPassword(PasswordService.getInstance().makeHash(PASSWORD));
        user.setUserType(User.Type.REGULAR);

        return USER_SERVICE.save(user);

    }

    private static class PerformanceResult {
        private double frr;
        private double far;

        public PerformanceResult(double frr, double far) {
            this.frr = frr;
            this.far = far;
        }

        public double getFrr() {
            return frr;
        }

        public double getFar() {
            return far;
        }

        @Override
        public String toString() {
            return "PerformanceResult{" +
                    "frr=" + frr +
                    ", far=" + far +
                    '}';
        }
    }

    private static class AuthResult {
        private AtomicInteger successful = new AtomicInteger();
        private AtomicInteger failed = new AtomicInteger();

        public AuthResult() {}

        public int getSuccessful() {
            return successful.get();
        }

        public void incSuccessful() {
            successful.incrementAndGet();
        }

        public int getFailed() {
            return failed.get();
        }

        public void incFailed() {
            failed.incrementAndGet();
        }

        public int getTotal() {
            return successful.get() + failed.get();
        }

        @Override
        public String toString() {
            return "AuthResult{" +
                    "successful=" + successful.get() +
                    ", failed=" + failed.get() +
                    '}';
        }
    }

}
