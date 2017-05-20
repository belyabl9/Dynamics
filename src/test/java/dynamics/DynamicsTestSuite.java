package dynamics;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import com.m1namoto.service.EventService;
import com.m1namoto.service.FeatureService;
import com.m1namoto.service.UsersService;

@RunWith(Suite.class)

@Suite.SuiteClasses({ 
    RegistrationTest.class, AuthTest.class
})

public class DynamicsTestSuite {

    @BeforeClass 
    public static void setUpClass() {
        FeatureService.removeAll();
        EventService.removeAll();
        UsersService.removeAll();
    }
}