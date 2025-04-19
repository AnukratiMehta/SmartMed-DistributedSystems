/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package distsys.smartmed.common;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for standardized logging across the services.
 * Provides methods for logging service start, completion, errors, and detailed debug information.
 * 
 * @author anukratimehta
 */
public class LoggingUtils {
    
    // Standardized log message templates
    private static final String SERVICE_START = "[%s] Starting for patient %s";
    private static final String SERVICE_END = "[%s] Completed for patient %s (%s)";
    private static final String ERROR_OCCURRED = "[%s] Error for patient %s: %s";

    /**
     * Logs the start of a service with consistent formatting.
     * 
     * @param logger the Logger instance to use for logging
     * @param serviceName the name of the service being logged
     * @param patientId the ID of the patient involved in the service
     */
    public static void logServiceStart(Logger logger, String serviceName, String patientId) {
        logger.log(Level.INFO, String.format(SERVICE_START, serviceName, patientId));
    }

    /**
     * Logs the completion of a service with a summary of the results/status.
     * 
     * @param logger the Logger instance to use for logging
     * @param serviceName the name of the service being logged
     * @param patientId the ID of the patient involved in the service
     * @param resultSummary the result or summary of the service completion
     */
    public static void logServiceEnd(Logger logger, String serviceName, String patientId, String resultSummary) {
        logger.log(Level.INFO, String.format(SERVICE_END, serviceName, patientId, resultSummary));
    }

    /**
     * Logs errors with consistent severity. Client errors are logged as WARNING, while server errors are logged as SEVERE.
     * 
     * @param logger the Logger instance to use for logging
     * @param serviceName the name of the service where the error occurred
     * @param patientId the ID of the patient involved in the error
     * @param error the Throwable object representing the error
     * @param isClientError true if the error is a client error, false for server errors
     */
    public static void logError(Logger logger, String serviceName, String patientId, Throwable error, boolean isClientError) {
        Level level = isClientError ? Level.WARNING : Level.SEVERE;
        logger.log(level, String.format(ERROR_OCCURRED, serviceName, patientId, error.getMessage()));
    }

    /**
     * Logs detailed debug information, but only if FINE logging level is enabled.
     * 
     * @param logger the Logger instance to use for logging
     * @param format the format string for the log message
     * @param args the arguments to format the log message
     */
    public static void logFine(Logger logger, String format, Object... args) {
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, String.format(format, args));
        }
    }
}
