package com.m1namoto.service.auth;

import com.google.common.base.Strings;
import com.m1namoto.domain.Event;
import com.m1namoto.domain.User;
import com.m1namoto.service.PropertiesService;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AuthenticationContext {

    private static class PropertyParam {
        static final String SAVE_REQUESTS = "save_requests";
        static final String UPDATE_TEMPLATE = "update_template";
        static final String THRESHOLD = "threshold";
        static final String LEARNING_RATE = "learning_rate";
    }

    /**
     * Specifies the number of first authentications for which keystroke dynamics is not checked
     */
    private static final int TRUSTED_AUTHENTICATION_LIMIT = 5;

    /**
     * Specifies how similar predicted class has to be in range [0-1]. May be overridden
     */
    private static final double CLASS_PREDICTION_THRESHOLD = 0.8;

    private final String login;
    private final String password;
    private final String stat;
    private final boolean stolen;
    private final boolean saveRequest;
    private final boolean updateTemplate;
    private final int learningRate;
    private final double threshold;

    private User user;
    private List<Event> sessionEvents;

    public AuthenticationContext(@NotNull String login, @NotNull String password, @NotNull String stat, boolean stolen) {
        this.login = login;
        this.password = password;
        this.stat = stat;

        // used for test purposes only
        this.stolen = stolen;

        // TODO "Dynamic - Static - Default" strategy should be used
        saveRequest = Boolean.valueOf(PropertiesService.getDynamicPropertyValue(PropertyParam.SAVE_REQUESTS).get());
        updateTemplate = Boolean.valueOf(PropertiesService.getDynamicPropertyValue(PropertyParam.UPDATE_TEMPLATE).get());

        String thresholdStr = PropertiesService.getDynamicPropertyValue(PropertyParam.THRESHOLD).get();
        threshold = Strings.isNullOrEmpty(thresholdStr) ? CLASS_PREDICTION_THRESHOLD : Double.valueOf(thresholdStr);

        String learningRateStr = PropertiesService.getDynamicPropertyValue(PropertyParam.LEARNING_RATE).get();
        learningRate = Strings.isNullOrEmpty(learningRateStr) ? TRUSTED_AUTHENTICATION_LIMIT : Integer.valueOf(learningRateStr);
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public String getStat() {
        return stat;
    }

    public boolean isStolen() {
        return stolen;
    }

    public boolean isSaveRequest() {
        return saveRequest;
    }

    public boolean isUpdateTemplate() {
        return updateTemplate;
    }

    public int getLearningRate() {
        return learningRate;
    }

    public double getThreshold() {
        return threshold;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<Event> getSessionEvents() {
        return sessionEvents;
    }

    public void setSessionEvents(List<Event> sessionEvents) {
        this.sessionEvents = sessionEvents;
    }

    @Override
    public String toString() {
        return "AuthenticationContext{" +
                "login='" + login + '\'' +
                ", password='" + password + '\'' +
                ", stat='" + stat + '\'' +
                ", stolen=" + stolen +
                ", saveRequest=" + saveRequest +
                ", updateTemplate=" + updateTemplate +
                ", learningRate=" + learningRate +
                ", threshold=" + threshold +
                ", user=" + user +
                ", sessionEvents=" + sessionEvents +
                '}';
    }
}