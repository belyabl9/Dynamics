package dynamics.samepass;

import com.m1namoto.domain.HoldFeature;
import com.m1namoto.domain.PressPressFeature;
import com.m1namoto.domain.ReleasePressFeature;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Describes one attempt of typing a password - ".tie5Roanl"
 */
public class DataSetRecord {

    @NotNull
    private final String login;

    private final int sessionIndex;

    /**
     * N-th repetition within one session
     */
    private final int repetition;

    @NotNull
    private final List<HoldFeature> holdFeatures;

    @NotNull
    private final List<ReleasePressFeature> releasePressFeatures;

    @NotNull
    private final List<PressPressFeature> pressPressFeatures;

    public DataSetRecord(@NotNull String login,
                         int sessionIndex,
                         int repetition,
                         @NotNull List<HoldFeature> holdFeatures,
                         @NotNull List<ReleasePressFeature> releasePressFeatures,
                         @NotNull List<PressPressFeature> pressPressFeatures) {
        this.login = login;
        this.sessionIndex = sessionIndex;
        this.repetition = repetition;
        this.holdFeatures = holdFeatures;
        this.releasePressFeatures = releasePressFeatures;
        this.pressPressFeatures = pressPressFeatures;
    }

    @NotNull
    public String getLogin() {
        return login;
    }

    public int getSessionIndex() {
        return sessionIndex;
    }

    public int getRepetition() {
        return repetition;
    }

    @NotNull
    public List<HoldFeature> getHoldFeatures() {
        return holdFeatures;
    }

    @NotNull
    public List<ReleasePressFeature> getReleasePressFeatures() {
        return releasePressFeatures;
    }

    @NotNull
    public List<PressPressFeature> getPressPressFeatures() {
        return pressPressFeatures;
    }

    @Override
    public String toString() {
        return "DataSetRecord{" +
                "user='" + login + '\'' +
                ", sessionIndex=" + sessionIndex +
                ", repetition=" + repetition +
                ", holdFeatures=" + holdFeatures +
                ", releasePressFeatures=" + releasePressFeatures +
                ", pressPressFeatures=" + pressPressFeatures +
                '}';
    }
}
