package dynamics;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.m1namoto.RequestSender;
import com.m1namoto.etc.AuthRequest;
import com.m1namoto.service.PropertiesService;
import com.m1namoto.utils.Utils;
import org.junit.Test;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.lang.reflect.Type;
import java.nio.charset.Charset;

public class AuthServletTest {

    private final static String AUTH_REQUESTS_PATH = PropertiesService.getInstance().getStaticPropertyValue("saved_auth_requests_path").get();

    @Test
    public void runAuth() throws Exception {
        File requestsRoot = new File(AUTH_REQUESTS_PATH);
        if (!requestsRoot.exists()) {
            throw new Exception("Requests directory does not exist");
        }
        
        int authAttempts = 0, allFailed = 0;

        for (File loginDir : requestsRoot.listFiles()) {
            if (!loginDir.isDirectory()) {
                continue;
            }
            File ownRequestsDir = new File(loginDir, "own");
            int successful = 0, failed = 0;

            for (File jsonFile : ownRequestsDir.listFiles()) {
                Type type = new TypeToken<AuthRequest>(){}.getType();
                String json = Utils.readFile(jsonFile.getAbsolutePath(), Charset.defaultCharset());
                AuthRequest authRequest = new Gson().fromJson(json, type);
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

        System.out.println("Attempts: " + authAttempts);
        System.out.println("All failed: " + allFailed);
        double frr = (double) allFailed / authAttempts;
        System.out.println("FRR: " + frr);
    }

}

