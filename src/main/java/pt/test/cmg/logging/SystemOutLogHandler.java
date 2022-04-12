package pt.test.cmg.logging;

import java.util.logging.Filter;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

public class SystemOutLogHandler extends StreamHandler {

    public SystemOutLogHandler() {
        super(System.out, new SimpleFormatter());
        configureSysOutHandler();
    }

    /**
     * This method uses the same logic as its counterpart in Console Handler.
     * The reason for this was mostly the usage of properties with a slightly different naming from ConsoleHandler.
     * It couldn't be done otherwise because most useful methods in ConsoleHandler and LogManager are package private or private.
     * It might also be useful in case of any need for extension.
     */
    private void configureSysOutHandler() {
        LogManager manager = LogManager.getLogManager();
        String fullyClassifiedName = getClass().getName();

        String requestedLevel = manager.getProperty(fullyClassifiedName + ".level");
        String formatterClassName = manager.getProperty(fullyClassifiedName + ".formatter");
        String filterClassName = manager.getProperty(fullyClassifiedName + ".filter");
        String textEncoding = manager.getProperty(fullyClassifiedName + ".encoding");

        setLevel(parseLevel(requestedLevel));
        setFormatter(parseFormatter(formatterClassName));
        setFilter(parseFilter(filterClassName));

        try {
            setEncoding(textEncoding);
        } catch (Exception ex) {
            try {
                setEncoding(null);
            } catch (Exception ex2) {
                // doing a setEncoding with null should always work. <- actual words from ConsoleHandler, not mine
            }
        }

    }

    /*
     * The following code was copied almost ipsis verbis from ConsoleHandler because we needed a different
     * configuration fully qualified class name and this function was package private on the LogManager
     */
    private Level parseLevel(String logLevelRequested) {

        if (logLevelRequested != null && !logLevelRequested.isEmpty()) {
            try {
                return Level.parse(logLevelRequested.trim());
            } catch (IllegalArgumentException e) {
                System.err.println("Couldn't find a valid leve, so we use INFO");
                return Level.INFO;
            }
        }

        return Level.INFO;
    }

    /*
     * The following code was copied almost ipsis verbis from ConsoleHandler because we needed a different
     * configuration fully qualified class name and this function was package private on the LogManager
     */
    private Formatter parseFormatter(String requestedFormatterClass) {

        try {
            if (requestedFormatterClass != null) {
                Class<?> clz = ClassLoader.getSystemClassLoader().loadClass(requestedFormatterClass);
                return (Formatter) clz.newInstance();
            }
        } catch (Exception ex) {
            // We got one of a variety of exceptions in creating the
            // class or creating an instance.
            // Drop through.
        }
        // We got an exception.  Return the defaultValue.
        return new SimpleFormatter();
    }

    /*
     * The following code was copied almost ipsis verbis from ConsoleHandler because we needed a different
     * configuration fully qualified class name and this function was package private on the LogManager
     */
    private Filter parseFilter(String requestedFilterClass) {
        try {
            if (requestedFilterClass != null) {
                Class<?> clz = ClassLoader.getSystemClassLoader().loadClass(requestedFilterClass);
                return (Filter) clz.newInstance();
            }
        } catch (Exception ex) {
            // We got one of a variety of exceptions in creating the
            // class or creating an instance.
            // Drop through.
        }
        // We got an exception.  Return the defaultValue.
        return null;
    }

    @Override
    public synchronized void publish(LogRecord record) {
        super.publish(record);
        flush();
    }

    /**
     * Override <tt>StreamHandler.close</tt> to do a flush but not
     * to close the output stream. That is, we do <b>not</b>
     * close <tt>System.err</tt>.
     */
    @Override
    public synchronized void close() {
        flush();
    }

}
