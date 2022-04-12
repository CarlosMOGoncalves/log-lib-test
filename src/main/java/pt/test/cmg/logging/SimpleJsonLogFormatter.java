package pt.test.cmg.logging;

import java.text.MessageFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.IllegalFormatException;
import java.util.ResourceBundle;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import javax.json.Json;
import javax.json.JsonObjectBuilder;

public class SimpleJsonLogFormatter extends Formatter {

    // Event separator
    private static final String LINE_SEPARATOR = System.lineSeparator();
    private static final String EMPTY_STRING = "";

    // String values for field keys
    private static final String EVENT_SEVERITY = "EventSeverity";
    private static final String TIMESTAMP = "Timestamp";
    private static final String THREAD = "Thread";

    private static final String MSG = "Message";

    public SimpleJsonLogFormatter() {
    }

    @Override
    public String format(LogRecord logRecord) {

        JsonObjectBuilder logJsonObject = Json.createObjectBuilder();

        logJsonObject.add(EVENT_SEVERITY, logRecord.getLevel().getName());

        Instant timestamp = Instant.ofEpochMilli(logRecord.getMillis())
            .atZone(ZoneId.from(ZoneOffset.UTC))
            .toInstant();
        logJsonObject.add(TIMESTAMP, DateTimeFormatter.ISO_INSTANT.format(timestamp));

        logJsonObject.add(THREAD, String.valueOf(logRecord.getThreadID()));

        logJsonObject.add(MSG, getTranslatedMessage(logRecord));

        return logJsonObject.build().toString() + LINE_SEPARATOR;
    }

    private String getTranslatedMessage(LogRecord logRecord) {
        String message = logRecord.getMessage();

        if (message != null && !message.isEmpty()) {
            message = translateMessage(message, logRecord);
            return formatText(message, logRecord.getParameters());
        }

        return EMPTY_STRING;
    }

    /**
     * Attempts to translate a message using any Resource Bundle that is given.
     * Returns the original String translated or not.
     * 
     * @param message
     * @param logRecord
     * @return
     */
    private String translateMessage(String message, LogRecord logRecord) {

        ResourceBundle bundle = logRecord.getResourceBundle();
        if (bundle != null) {

            if (bundle.containsKey(message)) {

                String translatedMessage = bundle.getString(message);
                if (!translatedMessage.isEmpty()) {
                    message = translatedMessage;
                }
            }
        }

        return message;
    }

    private String formatText(String parameterizedText, Object... parameters) {

        if (parameters != null && parameterizedText.contains("{0") && parameterizedText.contains("}")) {

            // Code taken from JDK - checking for a possible parameter replacement. Only searches for {0} because it is much faster than using regex
            try {
                parameterizedText = MessageFormat.format(parameterizedText, parameters);
            } catch (IllegalFormatException e) {
                // If it couldn't replace, then it goes exactly like it came
            }
        }

        return parameterizedText;
    }

    @Override
    public String formatMessage(LogRecord record) {
        return "";
    }

}
