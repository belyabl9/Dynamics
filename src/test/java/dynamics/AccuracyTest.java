package dynamics;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import com.m1namoto.classifier.Classifier;
import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.m1namoto.etc.AuthRequest;
import com.m1namoto.etc.RegRequest;
import com.m1namoto.service.PropertiesService;
import com.m1namoto.utils.Utils;

public class AccuracyTest {

    private final static String regRequestsPathConf = PropertiesService.getInstance().getDynamicPropertyValue("saved_reg_requests_path").get();
    private final static String authRequestsPathConf = PropertiesService.getInstance().getDynamicPropertyValue("saved_auth_requests_path").get();
    private final static String outputPathConf = PropertiesService.getInstance().getDynamicPropertyValue("test_results_output").get();
    private final static int learingRateConf = Integer.parseInt(PropertiesService.getInstance().getDynamicPropertyValue("learning_rate").get());
    
    private final static String outputFilePrefix = "dynamics_test";
    
    private String regRequestsPath;
    private String authRequestsPath;
    private int passwordLen = 12;
    private int learningRate = 5;
    
    private double thresholdStart = 0.1;
    private double thresholdEnd = 1;
    private double thresholdStep = 0.1;
    private List<Double> thresholds = new ArrayList<Double>();
    
    private Classifier.Type classifierType;
    
    private void initTest(int passwordLen, int learningRate, Classifier.Type classifierType) throws Exception {
        if (passwordLen <= 0) {
            throw new Exception("Password length should be more than 0");
        }
        this.classifierType = classifierType;
        this.passwordLen = passwordLen;
        this.learningRate = learningRate > 0 ? learningRate : learingRateConf;
        this.regRequestsPath = regRequestsPathConf + "/" + passwordLen;
        this.authRequestsPath = authRequestsPathConf + "/" + passwordLen;
        
        File regRequestsRoot = new File(regRequestsPath);
        if (!regRequestsRoot.exists()) {
            throw new Exception("Registration requests path does not exist");
        }

        File authRequestsRoot = new File(authRequestsPath);
        if (!authRequestsRoot.exists()) {
            throw new Exception("Authentication requests path does not exist");
        }
    }
    
    public AccuracyTest(int passwordLen, int learningRate, Classifier.Type classifierType) throws Exception {
        initTest(passwordLen, learningRate, classifierType);
        for (double threshold = thresholdStart; threshold < thresholdEnd; threshold += thresholdStep) {
            thresholds.add(threshold);
        }
    }
    
    public AccuracyTest(int passwordLen, int learningRate, Classifier.Type classifierType, List<Double> thresholds) throws Exception {
        initTest(passwordLen, learningRate, classifierType);
        this.thresholds = thresholds;
    }
    
    private int registerUser(File regRequestFile) throws IOException {
        Type type = new TypeToken<RegRequest>(){}.getType();
        String json = Utils.readFile(regRequestFile.getAbsolutePath(), Charset.defaultCharset());
        RegRequest regRequest = new Gson().fromJson(json, type);
        return RequestSender.sendRegRequest(regRequest);
    }
    
    private void registration() throws IOException {
        File regRequestsRoot = new File(regRequestsPath);

        for (File loginDir : regRequestsRoot.listFiles()) {
            if (!loginDir.isDirectory()) {
                continue;
            }
            for (File regRequestFile : loginDir.listFiles()) {
                if (regRequestFile.isDirectory()) {
                    continue;
                }
                registerUser(regRequestFile);
            }
        }
    }
    
    private int initAuthenticationRequest(File initAuthReqFile) throws IOException {
        Type type = new TypeToken<AuthRequest>(){}.getType();
        String json = Utils.readFile(initAuthReqFile.getAbsolutePath(), Charset.defaultCharset());
        AuthRequest authRequest = new Gson().fromJson(json, type);
        authRequest.setUpdateTemplate(true);
        authRequest.setLearningRate(learningRate);
        return RequestSender.sendAuthRequest(authRequest);
    }
    
    private void initAuth() throws IOException {
        File requestsRoot = new File(regRequestsPath);

        for (File loginDir : requestsRoot.listFiles()) {
            if (!loginDir.isDirectory()) {
                continue;
            }
            File initDir = new File(loginDir.getAbsolutePath() + "/init");
            File[] authRequests = initDir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.startsWith("req-");
                }
            });
            Arrays.sort(authRequests);
            for (int i = 0; i < learningRate - 1; i++) {
                File initAuthReqFile = authRequests[i];
                if (initAuthReqFile.isDirectory()) {
                    continue;
                }
                initAuthenticationRequest(initAuthReqFile);
            }
        }
    }
    
    private int sendAuthRequest(File authRequestFile, double threshold) throws IOException {
        Type type = new TypeToken<AuthRequest>(){}.getType();
        String json = Utils.readFile(authRequestFile.getAbsolutePath(), Charset.defaultCharset());
        AuthRequest authRequest = new Gson().fromJson(json, type);
        authRequest.setThreshold(threshold);
        authRequest.setLearningRate(learningRate);
        authRequest.setClassifierType(classifierType);

        return RequestSender.sendAuthRequest(authRequest);
    }
    
    private double getFRR(double threshold) throws Exception {
        System.out.println(String.format("Password len: %d; Learning rate: %d; FRR (threshold=%f)", passwordLen, learningRate, threshold));
        File requestsRoot = new File(authRequestsPath);
        int authAttempts = 0, allFailed = 0;

        for (File loginDir : requestsRoot.listFiles()) {
            if (!loginDir.isDirectory()) {
                continue;
            }
            File ownRequestsDir = new File(loginDir.getAbsolutePath() + "/own");
            if (!ownRequestsDir.exists()) {
                continue;
            }
            int successful = 0, failed = 0;
            File[] ownRequestFiles = ownRequestsDir.listFiles(new FilenameFilter() {
                
                @Override
                public boolean accept(File dir, String name) {
                    return name.startsWith("req-");
                }
            });
            File[] requestsExceptInit = Arrays.copyOfRange(ownRequestFiles, learningRate, ownRequestFiles.length);
            Arrays.sort(requestsExceptInit);
            
            for (File authRequestFile : requestsExceptInit) {
                int code = sendAuthRequest(authRequestFile, threshold);
                authAttempts++;
                if (code == HttpServletResponse.SC_OK) {
                    successful++;
                } else {
                    failed++;
                    allFailed++;
                }
            }
            System.out.println(String.format("User: %s; Successful: %d; Failed: %d", loginDir.getName(), successful, failed));
        }
        
        return (double) allFailed / authAttempts;
    }

    private double getFAR(double threshold) throws Exception {
        System.out.println(String.format("Password len: %d; Learning rate: %d; FAR (threshold=%f)", passwordLen, learningRate, threshold));
        File requestsRoot = new File(authRequestsPath);
        int authAttempts = 0, allSuccessful = 0;

        for (File loginDir : requestsRoot.listFiles()) {
            if (!loginDir.isDirectory()) {
                continue;
            }
            File stolenRequestsDir = new File(loginDir.getAbsolutePath() + "/stolen");
            if (!stolenRequestsDir.exists()) {
                continue;
            }
            int successful = 0, failed = 0;

            for (File authRequestFile : stolenRequestsDir.listFiles()) {
                int code = sendAuthRequest(authRequestFile, threshold);
                authAttempts++;
                if (code == HttpServletResponse.SC_OK) {
                    successful++;
                    allSuccessful++;
                } else {
                    failed++;
                }
            }
            System.out.println(String.format("User: %s; Successful: %d; Failed: %d", loginDir.getName(), successful, failed));
        }

        return (double) allSuccessful / authAttempts;
    }

    private AccuracyResult measureAccuracy() throws Exception {
        Map<Double, Double> farResults = new HashMap<Double, Double>();
        Map<Double, Double> frrResults = new HashMap<Double, Double>();
        
        for (Double threshold : thresholds) {
            double frr = getFRR(threshold);
            frrResults.put(threshold, frr);
            double far = getFAR(threshold);
            farResults.put(threshold, far);
        }

        return new AccuracyResult(passwordLen, learningRate, farResults, frrResults);
    }
    
    private void printResults(AccuracyResult results, double totalTime) throws IOException {
        Map<Double, Double> farResults = results.getFarResults();
        Map<Double, Double> frrResults = results.getFrrResults();
        
        StringBuilder frrOutput = new StringBuilder();
        StringBuilder farOutput = new StringBuilder();
        
        for (Double threshold : thresholds) {
            frrOutput.append(String.format("%.2f;%.2f\n", threshold, frrResults.get(threshold)));
            farOutput.append(String.format("%.2f;%.2f\n", threshold, farResults.get(threshold)));
        }

        String experimentOutput = String.format("Experiment\n\nLearning rate: %d\nPassword length: %d\nClassifier: %s\n", learningRate, passwordLen, classifierType);
        experimentOutput += String.format("\nFRR:\n%s\nFAR:\n%s\n\n", frrOutput.toString(), farOutput.toString());
        experimentOutput += String.format("Total time: %.1fm\n", totalTime);

        String outputFileName = String.format("%s_%d_%d_%s_%d",
                outputFilePrefix, passwordLen, learningRate, classifierType.toString().toLowerCase(), new Date().getTime()); 
        FileUtils.writeStringToFile(new File(outputPathConf + "/" + outputFileName), experimentOutput);
    }

    private void accuracyTest() throws Exception {
        long startTime = System.currentTimeMillis();
        RequestSender.sendDBCleanupRequest();
        registration();
        initAuth();
        AccuracyResult results = measureAccuracy();
        double totalTime = ((System.currentTimeMillis() - startTime) / 1000) / 60;
        printResults(results, totalTime);
    }

    private static void accuracyClassifierTestSet(Classifier.Type classifier) throws Exception {
    	/*AccuracyTest test6_1 = new AccuracyTest(6, 1, classifier);
        test6_1.accuracyTest();
        
        AccuracyTest test6_5 = new AccuracyTest(6, 5, classifier);
        test6_5.accuracyTest();
        
        AccuracyTest test6_10 = new AccuracyTest(6, 10, classifier);
        test6_10.accuracyTest();
        
        AccuracyTest test6_15 = new AccuracyTest(6, 15, classifier);
        test6_15.accuracyTest();
        */
        AccuracyTest test12_1_mlp = new AccuracyTest(12, 1, classifier);
        test12_1_mlp.accuracyTest();

        AccuracyTest test12_5_mlp = new AccuracyTest(12, 5, classifier);
        test12_5_mlp.accuracyTest();

        AccuracyTest test12_10 = new AccuracyTest(12, 10, classifier);
        test12_10.accuracyTest();
        
        AccuracyTest test12_15 = new AccuracyTest(12, 15, classifier);
        test12_15.accuracyTest();
        
    }

    public static void main(String[] args) throws Exception {
    	List<Double> thresholds = new ArrayList<Double>();
    	thresholds.add(0.8);
        AccuracyTest test12_15 = new AccuracyTest(12, 15, Classifier.Type.RANDOM_FOREST, thresholds);
        test12_15.accuracyTest();
    	
    	//accuracyClassifierTestSet(Classifiers.MLP);
    	//accuracyClassifierTestSet(Classifiers.RANDOM_FOREST);
        //accuracyClassifierTestSet(Classifiers.J48);
    }

}
