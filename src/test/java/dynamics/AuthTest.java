package dynamics;

import java.io.File;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.m1namoto.etc.AuthRequest;
import com.m1namoto.service.PropertiesService;
import com.m1namoto.utils.Utils;

public class AuthTest {

    private final static String AUTH_REQUESTS_PATH = PropertiesService.getInstance().getDynamicPropertyValue("saved_auth_requests_path").get();
    
    private double getFRR(double threshold) throws Exception {
        File requestsRoot = new File(AUTH_REQUESTS_PATH);
        if (!requestsRoot.exists()) {
            throw new Exception("Requests directory does not exist");
        }
        
        int authAttempts = 0, allFailed = 0;

        for (File file : requestsRoot.listFiles()) {
            if (file.isDirectory()) {
                File loginDir = file;
                File ownRequestsDir = new File(loginDir.getAbsolutePath() + "/own");
                int successful = 0, failed = 0;

                for (File jsonFile : ownRequestsDir.listFiles()) {
                    Type type = new TypeToken<AuthRequest>(){}.getType();
                    String json = Utils.readFile(jsonFile.getAbsolutePath(), Charset.defaultCharset());
                    AuthRequest authRequest = new Gson().fromJson(json, type);
                    authRequest.setThreshold(threshold);
                    int code = RequestSender.sendAuthRequest(authRequest);
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
        }
        
        System.out.println("Attempts: " + authAttempts);
        System.out.println("All failed: " + allFailed);
        double frr = (double) allFailed / authAttempts;
        System.out.println("FRR: " + frr);
        
        return frr;
    }
    
//    @Test
    public void doAuth() throws Exception {
        Map<Double, Double> results = new HashMap<Double, Double>();
     /*
        List<AuthThread> threads = new ArrayList<AuthThread>();
        for (double t = 0.05; t < 0.5; t+=0.05) {
            threads.add(new AuthThread(t, t + 0.05, 0.05, results));
        }

        for (AuthThread thread : threads) {
            thread.run();
        }
        for (AuthThread thread : threads) {
            thread.join();
            System.out.println("Thread " + thread.getId() + " has finished");
        }
        */
        
        for (double t = 0.05; t < 0.5; t+=0.05) {
            results.put(t, getFRR(t));
        }
        
        for (Double threshold : results.keySet()) {
            System.out.println(threshold + " - " + results.get(threshold));
        }
    }

}

