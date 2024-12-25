/**
 * Author: Pavith Bambaravanage
 * URL: https://github.com/Pavith19
 */

import java.io.IOException;
import java.util.logging.*;

/**
 * Logger class for the Ticket System.
 * Configures and handles logging to both console and file for the ticket system.
 */
public class TicketSystemLogger {
    private static final Logger logger = Logger.getLogger(TicketSystemLogger.class.getName());

    // Constant for the log file name
    private static final String LOG_FILE = "ticket_system.log";

    private TicketSystemLogger() {
        // Prevents instantiation of the utility class
    }

    /**
     * Configures the logger to log messages both to a file and the console.
     * This method sets up the file handler, console handler, and log level.
     */
    public static void configureLogger() {
        try {
            // Remove any pre-existing log handlers to prevent duplicate logging
            Logger rootLogger = Logger.getLogger("");
            Handler[] handlers = rootLogger.getHandlers();
            for (Handler handler : handlers) {
                rootLogger.removeHandler(handler);
            }

            // File Handler: Logs messages to a file, appending to existing logs
            FileHandler fileHandler = new FileHandler(LOG_FILE, true);
            fileHandler.setFormatter(new SimpleFormatter());

            // Console Handler: Logs messages to the console
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setFormatter(new SimpleFormatter());

            // Add handlers to the logger
            // Disable parent handlers to prevent duplicate logging
            logger.addHandler(fileHandler);
            logger.addHandler(consoleHandler);
            logger.setUseParentHandlers(false);

            // Set logging level (you can adjust as needed)
            logger.setLevel(Level.INFO);

        } catch (IOException e) {
            // Handle any errors during logger configuration
            TicketSystemLogger.severe("Failed to configure logger: " + e.getMessage());
            System.out.println("An error occurred while configuring the logger. Please try again.");
        }

    }

    /**
     * Convenience method to log an informational message.
     *
     * @param message The message to be logged as an info-level message.
     */
    public static void info(String message) {
        logger.info(message);
    }

    /**
     * Convenience method to log a warning message.
     *
     * @param message The message to be logged as a warning-level message.
     */
    public static void warning(String message) {
        logger.warning(message);
    }

    /**
     * Convenience method to log a severe error message.
     *
     * @param message The message to be logged as a severe-level message.
     */
    public static void severe(String message) {
        logger.severe(message);
    }
}
