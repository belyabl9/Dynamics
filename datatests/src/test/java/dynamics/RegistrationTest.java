package dynamics;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.m1namoto.etc.AuthRequest;
import com.m1namoto.etc.RegRequest;
import com.m1namoto.service.PropertiesService;
import com.m1namoto.utils.Utils;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;

public class RegistrationTest {

    private final static String SAVED_REG_REQUESTS_PATH = PropertiesService.getInstance().getStaticPropertyValue("saved_reg_requests_path").get();

    private final static String INIT_DIR_PREFIX = "init";
    
    @BeforeClass
    public static void setUpClass() throws IOException {
        RequestSender.sendDBCleanupRequest();
    }
    
    private void registration() throws IOException {
        File requestsRoot = new File(SAVED_REG_REQUESTS_PATH);
        if (!requestsRoot.exists()) {
            throw new FileNotFoundException();
        }

        for (File loginDir : requestsRoot.listFiles()) {
            if (loginDir.isDirectory()) {
                for (File file : loginDir.listFiles()) {
                    if (file.isDirectory()) {
                        continue;
                    }
                    Type type = new TypeToken<RegRequest>(){}.getType();
                    String json = Utils.readFile(file.getAbsolutePath(), Charset.defaultCharset());
                    RegRequest regRequest = new Gson().fromJson(json, type);
                    RequestSender.sendRegRequest(regRequest);
                }
            }
        }
    }
    
    private void initAuth() throws IOException {
        File requestsRoot = new File(SAVED_REG_REQUESTS_PATH);
        if (!requestsRoot.exists()) {
            throw new FileNotFoundException();
        }

        for (File loginDir : requestsRoot.listFiles()) {
            if (loginDir.isDirectory()) {
                File initDir = new File(loginDir.getAbsolutePath() + "/" + INIT_DIR_PREFIX);
                File[] authRequests = initDir.listFiles();
                for (File file : authRequests) {
                    if (file.isDirectory()) {
                        continue;
                    }
                    Type type = new TypeToken<AuthRequest>(){}.getType();
                    String json = Utils.readFile(file.getAbsolutePath(), Charset.defaultCharset());
                    AuthRequest authRequest = new Gson().fromJson(json, type);
                    RequestSender.sendAuthRequest(authRequest);
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
