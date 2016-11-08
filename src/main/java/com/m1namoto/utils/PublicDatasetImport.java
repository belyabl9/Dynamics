package com.m1namoto.utils;

import java.util.HashMap;
import java.util.Map;

import com.m1namoto.classifier.Classifier;
import com.m1namoto.domain.HoldFeature;
import com.m1namoto.domain.ReleasePressFeature;
import com.m1namoto.domain.User;
import com.m1namoto.service.FeaturesService;
import com.m1namoto.service.UsersService;

import weka.core.Instance;
import weka.core.Instances;

public class PublicDatasetImport {

    private static final int CLASS_VAL_ATTRIBUTE_INDEX = 71;

    private static final String DATASET_FILE = "keystroke_normalized.arff";

    private static void saveInstances(Instances instances) throws Exception {
        Map<Long, Boolean> processedUsers = new HashMap<Long, Boolean>();
        
        Map<Integer, Character> codeMap = new HashMap<Integer, Character>();
        codeMap.put(1, 't');
        codeMap.put(2, 'i');
        codeMap.put(3, 'e');
        codeMap.put(10, 'o');
        codeMap.put(11, 'a');
        codeMap.put(12, 'n');
        codeMap.put(13, 'l');
        
        Map<Integer, ReleasePressPair> releasePressCodeMap = new HashMap<Integer, ReleasePressPair>();
        releasePressCodeMap.put(29, new ReleasePressPair('t', 'i'));
        releasePressCodeMap.put(30, new ReleasePressPair('i', 'e'));
        releasePressCodeMap.put(38, new ReleasePressPair('o', 'a'));
        releasePressCodeMap.put(39, new ReleasePressPair('a', 'n'));
        releasePressCodeMap.put(40, new ReleasePressPair('n', 'l'));
        
        Map<Long, Integer> usersFeatures = new HashMap<Long, Integer>();
        for (int i = 0; i < instances.numInstances(); i++) {
            if (processedUsers.size() >= 20) {
             //   break;
            }
            Instance instance = instances.instance(i);
            int classValue = (int) (instance.value(CLASS_VAL_ATTRIBUTE_INDEX) + 1);

            
            System.out.println(classValue);
            System.out.println( usersFeatures.get((long)classValue) );
            if ( usersFeatures.get((long)classValue) != null && usersFeatures.get((long)classValue) > 5 ) {
                System.out.println("More than 5");
                continue;
            }

            System.out.println(classValue);
            System.out.println(processedUsers);

            if (processedUsers.containsKey((long)classValue)) {
                //continue;
            }

            User user = UsersService.findById(classValue);
            if (user == null) {
                throw new Exception("User was not found");
            }

            //List<Double> holdTimeList = new ArrayList<Double>();
            //List<Double> releasePressList = new ArrayList<Double>();
            
            usersFeatures.put(user.getId(), usersFeatures.get((long)user.getId()) == null ? 0 : usersFeatures.get((long)user.getId()) + 1);
            
            for (Integer index : codeMap.keySet()) {
                double val = instance.value(index) * 1000;
                HoldFeature feature = new HoldFeature(val, (int) codeMap.get(index), user);
                FeaturesService.save(feature);
            }
            
            for (Integer index : releasePressCodeMap.keySet()) {
                double val = instance.value(index) * 1000;
                ReleasePressPair codePair = releasePressCodeMap.get(index);
                ReleasePressFeature feature = new ReleasePressFeature(val, codePair.getReleaseCode(), codePair.getPressCode(), user);
                FeaturesService.save(feature);
            }

            processedUsers.put(user.getId(), true);
        }
    }
    
    public static void main(String[] args) throws Exception {
       Instances instances = Classifier.readInstancesFromFile(System.getProperty("user.dir") + "/" + DATASET_FILE);
       saveInstances(instances);
    }
    
}
