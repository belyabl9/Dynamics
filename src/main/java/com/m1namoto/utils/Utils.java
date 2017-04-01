package com.m1namoto.utils;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class Utils {
    
    public static double mean(@NotNull List<Double> values) {
        // protection against division by zero
        if (values.isEmpty()) {
            return 0;
        }
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
    
    public static String readFile(String path, Charset encoding) 
            throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

}