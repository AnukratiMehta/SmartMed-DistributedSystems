/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package distsys.smartmed.common;

/**
 *
 * @author anukratimehta
 */

import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggingUtils {
    
    // Standardized log message templates
    private static final String SERVICE_START = "[%s] Starting for patient %s";
    private static final String SERVICE_END = "[%s] Completed for patient %s (%s)";
    private static final String ERROR_OCCURRED = "[%s] Error for patient %s: %s";

    /**
     * Logs service start with consistent formatting
     */
    public static void logServiceStart(Logger logger, String serviceName, String patientId) {
        logger.log(Level.INFO, String.format(SERVICE_START, serviceName, patientId));
    }

    /**
     * Logs service completion with results/status
     */
    public static void logServiceEnd(Logger logger, String serviceName, String patientId, String resultSummary) {
        logger.log(Level.INFO, String.format(SERVICE_END, serviceName, patientId, resultSummary));
    }

    /**
     * Logs errors with consistent severity (WARNING for client errors, SEVERE for server errors)
     */
    public static void logError(Logger logger, String serviceName, String patientId, Throwable error, boolean isClientError) {
        Level level = isClientError ? Level.WARNING : Level.SEVERE;
        logger.log(level, String.format(ERROR_OCCURRED, serviceName, patientId, error.getMessage()));
    }

    /**
     * Logs detailed debug info (only if FINE logging is enabled)
     */
    public static void logFine(Logger logger, String format, Object... args) {
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, String.format(format, args));
        }
    }
}
