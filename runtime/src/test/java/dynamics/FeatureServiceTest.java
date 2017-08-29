package dynamics;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.m1namoto.domain.Event;
import com.m1namoto.domain.HoldFeature;
import com.m1namoto.domain.ReleasePressFeature;
import com.m1namoto.service.FeatureExtractorService;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.util.List;

public class FeatureServiceTest {

//    @Test
//    public void test() throws Exception {
//        List<Event> events = getEvents();
//
//        Assert.assertArrayEquals(getKeyPressTimeList().toArray(), FeatureExtractorService.getInstance().getKeyPressTimeList(events).toArray());
//        Assert.assertArrayEquals(getTimeBetweenKeysList().toArray(), FeatureExtractorService.getInstance().getTimeBetweenKeysList(events).toArray());
//
//        Assert.assertArrayEquals(getHoldFeatures().toArray(), FeatureExtractorService.getInstance().getHoldFeatures(events).toArray());
//        Assert.assertArrayEquals(getReleasePressFeatures().toArray(), FeatureExtractorService.getInstance().getReleasePressFeatures(events).toArray());
//
//        String expectedMeanKeyPressTime = "105.19";
//        Assert.assertEquals(expectedMeanKeyPressTime, new DecimalFormat("###.##").format(FeatureExtractorService.getInstance().getMeanKeyPressTime(events)) );
//
//        String expectedMeanTimeBetweenKeys = "100.05";
//        Assert.assertEquals(expectedMeanTimeBetweenKeys, new DecimalFormat("###.##").format(FeatureExtractorService.getInstance().getMeanTimeBetweenKeys(events)) );
//    }
    
    private List<Event> getEvents() {
        String eventsJson = "";
        ClassLoader classLoader = getClass().getClassLoader();
        try {
            eventsJson = IOUtils.toString(classLoader.getResourceAsStream("data/events.json"));
        } catch (IOException e) {
          e.printStackTrace();
        }
        
        Type type = new TypeToken<List<Event>>(){}.getType();

        return new Gson().fromJson(eventsJson, type);
    }
    
    private List<Double> getKeyPressTimeList() {
        String listJson = "";
        ClassLoader classLoader = getClass().getClassLoader();
        try {
            listJson = IOUtils.toString(classLoader.getResourceAsStream("data/keyPressTimeList.json"));
        } catch (IOException e) {
          e.printStackTrace();
        }
        
        Type type = new TypeToken<List<Double>>(){}.getType();

        return new Gson().fromJson(listJson, type);
    }
    
    private List<Double> getTimeBetweenKeysList() {
        String listJson = "";
        ClassLoader classLoader = getClass().getClassLoader();
        try {
            listJson = IOUtils.toString(classLoader.getResourceAsStream("data/timeBetweenKeysList.json"));
        } catch (IOException e) {
          e.printStackTrace();
        }
        
        Type type = new TypeToken<List<Double>>(){}.getType();

        return new Gson().fromJson(listJson, type);
    }

    private List<HoldFeature> getHoldFeatures() {
        String listJson = "";
        ClassLoader classLoader = getClass().getClassLoader();
        try {
            listJson = IOUtils.toString(classLoader.getResourceAsStream("data/holdFeatures.json"));
        } catch (IOException e) {
          e.printStackTrace();
        }
        
        Type type = new TypeToken<List<HoldFeature>>(){}.getType();

        return new Gson().fromJson(listJson, type);
    }
    
    
    private List<ReleasePressFeature> getReleasePressFeatures() {
        String listJson = "";
        ClassLoader classLoader = getClass().getClassLoader();
        try {
            listJson = IOUtils.toString(classLoader.getResourceAsStream("data/releasePressFeatures.json"));
        } catch (IOException e) {
          e.printStackTrace();
        }
        
        Type type = new TypeToken<List<ReleasePressFeature>>(){}.getType();

        return new Gson().fromJson(listJson, type);
    }

}
