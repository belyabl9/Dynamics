package com.m1namoto.utils;

import java.util.List;
import java.util.Map;

public class Utils {
    
    public static double mean(List<Double> values) {
        double sum = 0;
        int n = 0;
        for (int i = 0; i < values.size(); i++, n++) {
            sum += values.get(i);
        }

        return sum/n;
    }
    
    public static void checkMandatoryParams(Map<String, String[]> paramsMap, String[] paramNames) throws Exception {
        for (String paramName : paramNames) {
            if ( paramsMap.get(paramName) == null ) {
                throw new Exception(String.format("Parameter '%s' is mandatory", paramName));
            }
        }
    }
    
}