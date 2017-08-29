package dynamics;

import com.google.common.base.Stopwatch;
import com.m1namoto.api.ClassificationResult;
import com.m1namoto.api.ClassifierMakerStrategy;
import com.m1namoto.dao.HibernateUtil;
import com.m1namoto.domain.Feature;
import com.m1namoto.domain.User;
import com.m1namoto.entity.DynamicsInstance;
import com.m1namoto.identification.classifier.ClassifierType;
import com.m1namoto.identification.classifier.weka.WekaClassifier;
import com.m1namoto.identification.classifier.weka.WekaClassifierMakerStrategy;
import com.m1namoto.service.FeatureService;
import com.m1namoto.service.PasswordService;
import com.m1namoto.service.SessionService;
import com.m1namoto.service.UserService;
import com.m1namoto.utils.Utils;
import dynamics.samepass.DataSetRecord;
import dynamics.samepass.DataSetRecordIterator;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SamePasswordDataClassifierTest {

    private static final UserService USER_SERVICE = UserService.getInstance();

    private static final ClassifierMakerStrategy CLASSIFIER_MAKER_STRATEGY = new WekaClassifierMakerStrategy(ClassifierType.MLP);

    private static final Map<String, WekaClassifier> CLASSIFIERS = new HashMap<>();
    private static final Map<String, User> USERS = new HashMap<>();

    private static final String PASSWORD = ".tie5Roanl";

    private static final int LEARNING_RATE = 20;

    private static final double SIMILARITY_THRESHOLD = 0.7d;

    private static Transaction transaction;
    private static SessionFactory sessionFactory;
    private static Session currentSession;

    @Before
    public void setUp() {
        sessionFactory = HibernateUtil.getSessionFactory();
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

    @Test
    public void run() {
        Stopwatch stopwatch = Stopwatch.createStarted();

        registerTemplates();
        evalFrr();
        evalFar();

        System.out.println("Done in " + stopwatch);
    }

    private void registerTemplates() {
        DataSetRecordIterator dataSetRecordIterator = new DataSetRecordIterator(
                SamePasswordDataClassifierTest.class.getResourceAsStream("/SamePasswordData.csv")
        );

        Map<String, Boolean> createdUsersMap = new HashMap<>();
        User curUser = null;
        int processedRecordsPerUser = 0;
        while (dataSetRecordIterator.hasNext()) {
            DataSetRecord record = dataSetRecordIterator.next();
            String userLogin = record.getLogin();
            Boolean created = createdUsersMap.get(userLogin);
            if (created != Boolean.TRUE) {
                curUser = createUser(userLogin);
                createdUsersMap.put(userLogin, true);
                processedRecordsPerUser = 0;

                curUser.setPassword(PASSWORD);
                USERS.put(userLogin, curUser);
            }

            if (processedRecordsPerUser >= LEARNING_RATE) {
                continue;
            }

            saveSession(curUser, record);

            processedRecordsPerUser++;
        }

        // ONLY FOR MLP
        WekaClassifier classifier = (WekaClassifier) CLASSIFIER_MAKER_STRATEGY.makeClassifier(USERS.get("s002"));
        for (User user : USERS.values()) {
            CLASSIFIERS.put(user.getLogin(), classifier);
        }

//        for (User user : USERS.values()) {
//            CLASSIFIERS.put(user.getLogin(), (WekaClassifier) CLASSIFIER_MAKER_STRATEGY.makeClassifier(user));
//        }
    }



    private void evalFrr() {
        DataSetRecordIterator dataSetRecordIterator = new DataSetRecordIterator(
                SamePasswordDataClassifierTest.class.getResourceAsStream("/SamePasswordData.csv")
        );

        List<Double> meanFrrProbLst = new ArrayList<>();

        int processedRecordsPerUser = 0;
        AuthResult totalAuthResult = new AuthResult();
        Map<String, Boolean> authUsersMap = new HashMap<>();
        Map<String, AuthResult> authResultMap = new HashMap<>();
        User curUser = null;
        WekaClassifier curClassifier = null;
        while (dataSetRecordIterator.hasNext()) {
            DataSetRecord record = dataSetRecordIterator.next();
            String userLogin = record.getLogin();
            Boolean authenticated = authUsersMap.get(userLogin);
            if (authenticated != Boolean.TRUE) {
                authUsersMap.put(userLogin, true);
                authResultMap.put(userLogin, new AuthResult());
                processedRecordsPerUser = 0;

                curUser = USERS.get(userLogin);
                curClassifier = CLASSIFIERS.get(userLogin);
            }

            if (processedRecordsPerUser < LEARNING_RATE) {
                processedRecordsPerUser++;
                continue;
            }

            ClassificationResult classificationResult = classify(curClassifier, curUser, record);
            meanFrrProbLst.add(classificationResult.getProbability());

            if (classificationResult.getProbability() > SIMILARITY_THRESHOLD) {
                authResultMap.get(userLogin).incSuccessful();
                totalAuthResult.incSuccessful();
            } else {
                authResultMap.get(userLogin).incFailed();
                totalAuthResult.incFailed();
            }

            processedRecordsPerUser++;
        }

        System.out.println("FRR TOTAL: " + totalAuthResult.getTotal());
        System.out.println("Successful: " + totalAuthResult.getSuccessful());
        System.out.println("Failed: " + totalAuthResult.getFailed());
        System.out.println("FRR: " + totalAuthResult.getFailed() / (double) totalAuthResult.getTotal());
        System.out.println("Mean FRR prob: " + Utils.mean(meanFrrProbLst));

        for (Map.Entry<String, AuthResult> entry : authResultMap.entrySet()) {
            String key = entry.getKey();
//            System.out.println(key + " : " + entry.getValue().getSuccessful() + " - " + entry.getValue().getFailed());
        }
    }

    private void evalFar() {
        DataSetRecordIterator dataSetRecordIterator;
        Map<String, Boolean> authUsersMap;
        Map<String, AuthResult> authResultMap;
        dataSetRecordIterator = new DataSetRecordIterator(
                SamePasswordDataClassifierTest.class.getResourceAsStream("/SamePasswordData.csv")
        );

        List<Double> meanFarProbLst = new ArrayList<>();

        int processedRecordsPerUser = 0;
        AuthResult totalAuthResult = new AuthResult();
        authUsersMap = new HashMap<>();
        authResultMap = new HashMap<>();
        while (dataSetRecordIterator.hasNext()) {
            DataSetRecord record = dataSetRecordIterator.next();
            String userLogin = record.getLogin();

            Boolean authenticated = authUsersMap.get(userLogin);
            if (authenticated != Boolean.TRUE) {
                authUsersMap.put(userLogin, true);
                authResultMap.put(userLogin, new AuthResult());
                processedRecordsPerUser = 0;

//                System.out.println("Processing user authentications: " + userLogin);
            }

            for (String login : USERS.keySet()) {
                if (userLogin.equals(login)) {
                    continue;
                }

                ClassificationResult classificationResult = classify(CLASSIFIERS.get(login), USERS.get(login), record);
                meanFarProbLst.add(classificationResult.getProbability());

                if (classificationResult.getProbability() > SIMILARITY_THRESHOLD) {
                    authResultMap.get(userLogin).incSuccessful();
                    totalAuthResult.incSuccessful();
                } else {
                    authResultMap.get(userLogin).incFailed();
                    totalAuthResult.incFailed();
                }
            }

            if (processedRecordsPerUser > 5) {
                processedRecordsPerUser++;
                continue;
            }

            processedRecordsPerUser++;
        }

        System.out.println("FAR TOTAL: " + totalAuthResult.getTotal());
        System.out.println("Successful: " + totalAuthResult.getSuccessful());
        System.out.println("Failed: " + totalAuthResult.getFailed());
        System.out.println("FAR: " + totalAuthResult.getSuccessful() / (double) totalAuthResult.getTotal());
        System.out.println("Mean FAR prob: " + Utils.mean(meanFarProbLst));

        for (Map.Entry<String, AuthResult> entry : authResultMap.entrySet()) {
            String key = entry.getKey();
//            System.out.println(key + " : " + entry.getValue().getSuccessful() + " - " + entry.getValue().getFailed());
        }
    }

    @NotNull
    private ClassificationResult classify(@NotNull WekaClassifier classifier, User curUser, @NotNull DataSetRecord record) {
        List<Double> featureValues = new ArrayList<>();
        for (Feature feature : record.getHoldFeatures()) {
            featureValues.add(feature.getValue());
        }
        for (Feature feature : record.getReleasePressFeatures()) {
            featureValues.add(feature.getValue());
        }
        for (Feature feature : record.getPressPressFeatures()) {
            featureValues.add(feature.getValue());
        }
//        featureValues.add(FeatureService.getInstance().getMeanTime(record.getHoldFeatures()).orNull());
        DynamicsInstance instance = new DynamicsInstance(featureValues);
        try {
            return classifier.getClassForInstance(instance, curUser.getId());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    private User createUser(String userLogin) {
        User user = new User();
        user.setName(userLogin + " " + userLogin);
        user.setLogin(userLogin);
        user.setPassword(PasswordService.getInstance().makeHash(PASSWORD));
        user.setUserType(User.Type.REGULAR);

        return USER_SERVICE.save(user);

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
    }

    private static class AuthResult {
        private int successful;
        private int failed;

        public AuthResult() {}

        public AuthResult(int successful, int failed) {
            this.successful = successful;
            this.failed = failed;
        }

        public int getSuccessful() {
            return successful;
        }

        public void incSuccessful() {
            successful++;
        }

        public int getFailed() {
            return failed;
        }

        public void incFailed() {
            failed++;
        }

        public int getTotal() {
            return successful + failed;
        }

        @Override
        public String toString() {
            return "AuthResult{" +
                    "successful=" + successful +
                    ", failed=" + failed +
                    '}';
        }
    }

}
