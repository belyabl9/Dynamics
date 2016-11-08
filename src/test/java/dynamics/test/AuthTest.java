package dynamics.test;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;

import javax.servlet.http.HttpServletResponse;

import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.m1namoto.etc.AuthRequest;
import com.m1namoto.utils.PropertiesService;
import com.m1namoto.utils.Utils;

public class AuthTest {

    private final static String authRequestsPath = PropertiesService.getPropertyValue("saved_auth_requests_path");
    
    private double getFRR(double threshold) throws Exception {
        File requestsRoot = new File(authRequestsPath);
        if (!requestsRoot.exists()) {
            throw new Exception("Requests directory does not exist");
        }
        
        int authAttempts = 0, allSuccessful = 0, allFailed = 0;

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
                        allSuccessful++;
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
    
    @Test
    public void doAuth() throws Exception {
        for (double threshold = 0.05; threshold < 0.5; threshold+=0.05) {
            double frr = getFRR(threshold);
            System.out.println(frr);
        }
    }

}

