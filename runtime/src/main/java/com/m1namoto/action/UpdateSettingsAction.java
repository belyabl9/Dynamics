package com.m1namoto.action;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.m1namoto.page.PageData;
import com.m1namoto.service.PropertiesService;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class UpdateSettingsAction extends Action {

    private static final Set<String> OPTIONS = ImmutableSet.of(
            "save_requests", "update_template", "learning_rate", "threshold"
    );

    private static final Set<String> BOOLEAN_VALUES = ImmutableSet.of(
            "true", "false"
    );

    @Override
    protected ActionResult execute() throws Exception {
        PageData data = new PageData();

        Map<String, String> settings = new HashMap<>();
        for (String optName : OPTIONS) {
            Optional<String> valueOpt = getRequestParamValue(optName);
            if (valueOpt.isPresent() && !valueOpt.get().isEmpty()) {
                validate(optName, valueOpt.get());
            }
            settings.put(optName, valueOpt.or(""));
        }

        try {
            PropertiesService.getInstance().saveDynamicConfiguration(settings);
        } catch(Exception e) {
            data.setError(true);
        }

        return createAjaxResult(data);
    }

    private void validate(@NotNull String name, @NotNull String val) {
        if (val.isEmpty()) {
            throw new IllegalArgumentException("Value must be non-empty.");
        }

        switch(name) {
            case "save_requests":
            case "update_template":
                if (!BOOLEAN_VALUES.contains(val)) {
                    throw new IllegalArgumentException("Value must be from set [true, false].");
                }
                break;
            case "learning_rate":
                validateInt(val);
                break;
            case "threshold":
                validateDouble(val);
                break;
            default:
                throw new UnsupportedOperationException("Specified option is not supported.");

        }
    }

    private void validateInt(@NotNull String val) {
        try {
            int valInt = Integer.parseInt(val);
            if (valInt < 0) {
                throw new IllegalArgumentException("Value must be greater than 0.");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Value must be integer");
        }
    }

    private void validateDouble(@NotNull String val) {
        try {
            double valDouble = Double.parseDouble(val);
            if (valDouble < 0d || valDouble > 1d) {
                throw new IllegalArgumentException("Value must be in range [0-1].");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Value must be double");
        }
    }

}
