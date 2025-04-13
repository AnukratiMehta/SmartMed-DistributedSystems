package distsys.smartmed.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import java.io.IOException;
import java.util.logging.*;
import distsys.smartmed.security.JwtServerInterceptor;


public class SmartMedServer {
    private static final Logger logger = Logger.getLogger(SmartMedServer.class.getName());
    private Server server;

    static {
        configureLogging();
    }

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

    public static void main(String[] args) throws IOException, InterruptedException {
        SmartMedServer serverInstance = new SmartMedServer();
        serverInstance.start();
        serverInstance.blockUntilShutdown();
    }

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
        
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down server...");
            stop();
        }));
    }

    private void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }
}