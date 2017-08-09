package dynamics;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;

import org.apache.http.client.ClientProtocolException;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.m1namoto.etc.AuthRequest;
import com.m1namoto.etc.RegRequest;
import com.m1namoto.service.PropertiesService;
import com.m1namoto.utils.Utils;

public class RegistrationTest {

    private final static String regRequestsPath = PropertiesService.getInstance().getStaticPropertyValue("saved_reg_requests_path").get();
    private final static int learingRate = Integer.parseInt(PropertiesService.getInstance().getDynamicPropertyValue("learning_rate").get());
    
    private final static String INIT_DIR_PREFIX = "init";
    
    @BeforeClass
    public static void setUpClass() throws ClientProtocolException, IOException {
        RequestSender.sendDBCleanupRequest();
    }
    
    private void registration() throws IOException {
        File requestsRoot = new File(regRequestsPath);
        if (!requestsRoot.exists()) {
            throw new FileNotFoundException();
        }

        for (File loginDir : requestsRoot.listFiles()) {
            if (loginDir.isDirectory()) {
                for (File file : loginDir.listFiles()) {
                    if (!file.isDirectory()) {
                        Type type = new TypeToken<RegRequest>(){}.getType();
                        String json = Utils.readFile(file.getAbsolutePath(), Charset.defaultCharset());
                        RegRequest regRequest = new Gson().fromJson(json, type);
                        RequestSender.sendRegRequest(regRequest);
                    }
                }
            }
        }
    }
    
    private void initAuth() throws IOException {
        File requestsRoot = new File(regRequestsPath);
        if (!requestsRoot.exists()) {
            throw new FileNotFoundException();
        }

        for (File loginDir : requestsRoot.listFiles()) {
            if (loginDir.isDirectory()) {
                File initDir = new File(loginDir.getAbsolutePath() + "/" + INIT_DIR_PREFIX);
                File[] authRequests = initDir.listFiles();
                for (int i = 0; i < learingRate - 1; i++) {
                    File file = authRequests[i];
                    if (!file.isDirectory()) {
                        Type type = new TypeToken<AuthRequest>(){}.getType();
                        String json = Utils.readFile(file.getAbsolutePath(), Charset.defaultCharset());
                        AuthRequest authRequest = new Gson().fromJson(json, type);
                        RequestSender.sendAuthRequest(authRequest);
                    }
                }
            }
        }
    }
    
//    @Test
    public void doRegistration() throws IOException {
        registration();
        initAuth();
    }

}
