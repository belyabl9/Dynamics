package dynamics;

import java.io.File;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.m1namoto.etc.AuthRequest;
import com.m1namoto.service.PropertiesService;
import com.m1namoto.utils.Utils;

public class AuthThread extends Thread {
    private final static String authRequestsPath = PropertiesService.getInstance().getDynamicPropertyValue("saved_auth_requests_path").get();
    
    private double leftBoundary;
    private double rightBoundary;
    private double step = 0.05;
    private Map<Double, Double> results;
    
    public AuthThread(double leftBoundary, double rightBoundary, double step, Map<Double, Double> results) {
        this.leftBoundary = leftBoundary;
        this.rightBoundary = rightBoundary;
        this.step = step;
        this.results = results;
    }
    
    private double getFRR(double threshold) throws Exception {
        File requestsRoot = new File(authRequestsPath);
        if (!requestsRoot.exists()) {
            throw new Exception("Requests directory does not exist");
        }
        
        int authAttempts = 0, allFailed = 0;

        for (File file : requestsRoot.listFiles()) {
            if (file.isDirectory()) {
                File loginDir = file;
                File ownRequestsDir = new File(loginDir.getAbsolutePath() + "/own");

                for (File jsonFile : ownRequestsDir.listFiles()) {
                    Type type = new TypeToken<AuthRequest>(){}.getType();
                    String json = Utils.readFile(jsonFile.getAbsolutePath(), Charset.defaultCharset());
                    AuthRequest authRequest = new Gson().fromJson(json, type);
                    authRequest.setThreshold(threshold);
                    authAttempts++;
                }
            }
        }
        
        double frr = (double) allFailed / authAttempts;
        
        return frr;
    }
    
    public void run() {
        for (double threshold = leftBoundary; threshold < rightBoundary; threshold += step) {
            double frr = 0;
            try {
                frr = getFRR(threshold);
            } catch (Exception e) {
                e.printStackTrace();
            }
            results.put(threshold, frr);
        }
    }
}
