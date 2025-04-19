/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package distsys.smartmed.server;

/**
 * Main server class for SmartMed.
 * Initializes and starts the gRPC server with all healthcare-related services and interceptors.
 * Configures logging and handles server shutdown gracefully.
 * 
 * @author anukratimehta
 */

import io.grpc.Server;
import io.grpc.ServerBuilder;
import java.io.IOException;
import java.util.logging.*;
import distsys.smartmed.security.JwtServerInterceptor;

public class SmartMedServer {
    private static final Logger logger = Logger.getLogger(SmartMedServer.class.getName());
    private Server server;

    // Static block to configure logging once during class loading
    static {
        configureLogging();
    }

    /**
     * Configures the global logging behavior.
     * Logs are written to 'smartmed-%g.log' files with rolling file handlers.
     */
    private static void configureLogging() {
        try {
            FileHandler fileHandler = new FileHandler("smartmed-%g.log", 1000000, 3);
            fileHandler.setFormatter(new SimpleFormatter());
            Logger.getLogger("").addHandler(fileHandler);
            logger.info("Logging configured successfully");
        } catch (IOException e) {
            System.err.println("Failed to configure logging: " + e.getMessage());
        }
    }

    /**
     * Entry point for starting the SmartMed server.
     *
     * @param args command-line arguments
     * @throws IOException if the server fails to start
     * @throws InterruptedException if the server is interrupted
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        SmartMedServer serverInstance = new SmartMedServer();
        serverInstance.start();
        serverInstance.blockUntilShutdown();
    }

    /**
     * Starts the gRPC server on port 50051 and registers all services and interceptors.
     *
     * @throws IOException if there is an error starting the server
     */
    private void start() throws IOException {
        int port = 50051;
        server = ServerBuilder.forPort(port)
            .addService(new AuthServiceImpl())
            .addService(new PatientServiceImpl())
            .addService(new MonitoringServiceImpl())
            .addService(new MedicationServiceImpl())
            .addService(new RehabServiceImpl())
            .intercept(new JwtServerInterceptor())
            .build()
            .start();
        
        logger.info("Server started on port " + port);

        // Ensure graceful shutdown on JVM termination
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down server...");
            stop();
        }));
    }

    /**
     * Stops the server if it is running.
     */
    private void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    /**
     * Blocks the main thread until the server is terminated.
     *
     * @throws InterruptedException if the server is interrupted while waiting
     */
    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }
}
