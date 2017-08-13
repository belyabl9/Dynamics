package com.m1namoto.utils;

import com.m1namoto.dao.HibernateUtil;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class Utils {

    private static final String ID_MUST_BE_SPECIFIED = "User id must be specified.";
    private static final String ONLY_DIGITS_IN_ID = "Id must contain only digits.";

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

        return sum / n;
    }

    public static void checkMandatoryParams(@NotNull Map<String, String[]> paramsMap, @NotNull List<String> paramNames) {
        checkMandatoryParams(
                paramsMap,
                paramNames.toArray(new String[paramNames.size()])
        );
    }

    public static void checkMandatoryParams(Map<String, String[]> paramsMap, String[] paramNames) {
        for (String paramName : paramNames) {
            String[] valuesArray = paramsMap.get(paramName);
            if (valuesArray == null || valuesArray[0] == null) {
                throw new NullPointerException(String.format("Parameter '%s' is mandatory", paramName));
            }
        }
    }

    public static String readFile(String path, Charset encoding)
            throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

    public static boolean isOpenShift() {
        return System.getenv("OPENSHIFT_DATA_DIR") != null;
    }

    public static boolean isTest() {
        return HibernateUtil.getDbConfig() == HibernateUtil.DatabaseConfigs.TEST;
    }

    public static long validateNumericId(@NotNull String id) {
        if (StringUtils.isEmpty(id)) {
            throw new IllegalArgumentException(ID_MUST_BE_SPECIFIED);
        }

        long userIdNum;
        try {
            userIdNum = Long.parseLong(id);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(ONLY_DIGITS_IN_ID);
        }

        return userIdNum;
    }

}