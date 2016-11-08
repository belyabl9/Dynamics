package dynamics.test;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;

import javax.servlet.http.HttpServletResponse;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.m1namoto.etc.AuthRequest;
import com.m1namoto.etc.RegRequest;
import com.m1namoto.service.EventsService;
import com.m1namoto.service.FeaturesService;
import com.m1namoto.service.SessionsService;
import com.m1namoto.service.UsersService;
import com.m1namoto.utils.PropertiesService;
import com.m1namoto.utils.Utils;

public class RegistrationTest {

    private final static String regRequestsPath = PropertiesService.getPropertyValue("saved_reg_requests_path");
    private final static int learingRate = Integer.parseInt(PropertiesService.getPropertyValue("learning_rate"));
    
    @BeforeClass 
    public static void setUpClass() {
        FeaturesService.deleteAll();
        SessionsService.deleteAll();
        EventsService.deleteAll();
        UsersService.deleteAll();
    }
    
    private void registration() throws IOException {
        File requestsRoot = new File(regRequestsPath);
        if (!requestsRoot.exists()) {
            return;
        }

        for (File loginDir : requestsRoot.listFiles()) {
            if (loginDir.isDirectory()) {
                for (File file : loginDir.listFiles()) {
                    if (!file.isDirectory()) {
                        Type type = new TypeToken<RegRequest>(){}.getType();
                        String json = Utils.readFile(file.getAbsolutePath(), Charset.defaultCharset());
                        RegRequest regRequest = new Gson().fromJson(json, type);
                        int code = RequestSender.sendRegRequest(regRequest);
                    }
                }
            }
        }
    }
    
    private void initAuth() throws IOException {
        File requestsRoot = new File(regRequestsPath);
        if (!requestsRoot.exists()) {
            return;
        }

        for (File loginDir : requestsRoot.listFiles()) {
            if (loginDir.isDirectory()) {
                File initDir = new File(loginDir.getAbsolutePath() + "/init");
                File[] authRequests = initDir.listFiles();
                for (int i = 0; i < learingRate - 1; i++) {
                    File file = authRequests[i];
                    if (!file.isDirectory()) {
                        Type type = new TypeToken<AuthRequest>(){}.getType();
                        String json = Utils.readFile(file.getAbsolutePath(), Charset.defaultCharset());
                        AuthRequest authRequest = new Gson().fromJson(json, type);
                        int code = RequestSender.sendAuthRequest(authRequest);
                    }
                }
            }
        }
    }
    
    @Test
    public void doRegistration() throws IOException {
        registration();
        initAuth();
    }

}
