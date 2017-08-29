package dynamics.samepass;

import com.google.common.base.Splitter;
import com.m1namoto.domain.HoldFeature;
import com.m1namoto.domain.PressPressFeature;
import com.m1namoto.domain.ReleasePressFeature;
import com.m1namoto.domain.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class DataSetRecordIterator implements Iterator<DataSetRecord> {

    private static final Splitter COMMA_SPLITTER = Splitter.on(",");
    private static final String password = ".tie5Roanl";

    private int lineNum=0;
    private DataSetRecord next = null;

    @NotNull
    private BufferedReader reader;
    @Nullable
    private String nextLine;

    public DataSetRecordIterator(@NotNull InputStream inputStream) {
        reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
    }

    @Override
    public boolean hasNext() {
        if (nextLine == null) moveForward();
        return nextLine != null;
    }

    @Override
    public DataSetRecord next() {
        if (nextLine == null) moveForward();
        if (nextLine == null) {
            throw new NoSuchElementException();
        }
        String currentLine = nextLine;
        nextLine = null;
        return parseRecord(currentLine);
    }

    private void moveForward() {
        try {
            while ((nextLine = reader.readLine()) != null) {
                lineNum++;
                List<String> fields = COMMA_SPLITTER.splitToList(nextLine);

                // header line
                if (lineNum == 1) {
                    if (!fields.get(0).equals("subject")) {
                        throw new RuntimeException("Unexpected first line, expected header!");
                    }
                    continue;
                }
                break;
            }
            if (nextLine == null) {
                reader.close();
            }
        } catch(IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private DataSetRecord parseRecord(@NotNull String line) {
        List<String> fields = COMMA_SPLITTER.splitToList(line);

        List<HoldFeature> holdFeatures = new ArrayList<>();
        List<ReleasePressFeature> releasePressFeatures = new ArrayList<>();
        List<PressPressFeature> pressPressFeatures = new ArrayList<>();

        String login = fields.get(0);
        User user = new User();
        user.setLogin(login);

        int pos = 3;
        Character prevChar = null;
        for (char c : password.toCharArray()) {
            double holdTime = Double.parseDouble(fields.get(pos));
            holdFeatures.add(new HoldFeature(holdTime, (int) c, user));

            if (prevChar != null) {
                double releasePressTime = Double.parseDouble(fields.get(pos - 1));
                releasePressFeatures.add(new ReleasePressFeature(releasePressTime, (int) prevChar, (int) c, user));

                double pressPressTime = Double.parseDouble(fields.get(pos - 2));
                pressPressFeatures.add(new PressPressFeature(releasePressTime, (int) prevChar, (int) c, user));
            }

            prevChar = c;
            pos += 3;
        }

        return new DataSetRecord(
                login, // user
                Integer.parseInt(fields.get(1)), // session index
                Integer.parseInt(fields.get(2)), // repetition within session
                holdFeatures,
                releasePressFeatures,
                pressPressFeatures
        );
    }
}