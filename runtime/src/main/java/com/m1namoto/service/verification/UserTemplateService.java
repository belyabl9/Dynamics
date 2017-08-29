package com.m1namoto.service.verification;

import com.m1namoto.domain.User;
import com.m1namoto.entity.UserTemplate;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class UserTemplateService {

    private static Map<String, UserTemplate> CACHED_TEMPLATES = new HashMap<>();

    private UserTemplateService() {}

    private static class LazyHolder {
        static final UserTemplateService INSTANCE = new UserTemplateService();
    }
    public static UserTemplateService getInstance() {
        return UserTemplateService.LazyHolder.INSTANCE;
    }

    public UserTemplate getTemplate(@NotNull User user) {
        UserTemplate userTemplate = CACHED_TEMPLATES.get(user.getLogin());
        if (userTemplate == null) {
            synchronized (this) {
                userTemplate = CACHED_TEMPLATES.get(user.getLogin());
                if (userTemplate == null) {
                    userTemplate = new UserTemplate(user, VerificationTypeService.getInstance().getVerificationType());
                    CACHED_TEMPLATES.put(user.getLogin(), userTemplate);
                }
            }
        }
        return userTemplate;
    }

    public void invalidateCache(@NotNull String login) {
        CACHED_TEMPLATES.remove(login);
    }

}
