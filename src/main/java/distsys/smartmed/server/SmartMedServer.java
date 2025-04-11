package distsys.smartmed.server;

import distsys.smartmed.security.JwtServerInterceptor;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import java.io.IOException;
import java.net.InetAddress;
import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

public class SmartMedServer {
    private static final String SERVICE_TYPE = "_smartmed._tcp.local.";
    private JmDNS jmdns;
    private Server server;
    private final int PORT = 50051;

    public static void main(String[] args) throws IOException, InterruptedException {
        SmartMedServer smartMedServer = new SmartMedServer();
        smartMedServer.start();
        smartMedServer.blockUntilShutdown();
    }

    private void start() throws IOException {
        jmdns = JmDNS.create(InetAddress.getByName("0.0.0.0"));
        
        // Register all services
        registerService("AuthService");
        registerService("PatientService");
        registerService("MonitoringService");
        registerService("MedicationService");
        registerService("RehabService");

        // Build server with JWT interceptor
        server = ServerBuilder.forPort(PORT)
            .addService(new AuthServiceImpl()) 
            .intercept(new JwtServerInterceptor())
            .addService(new PatientServiceImpl())
            .addService(new MonitoringServiceImpl())
            .addService(new MedicationServiceImpl())
            .addService(new RehabServiceImpl())
            .build()
            .start();

        System.out.println("Server started on port " + PORT + " with services:");
        System.out.println("- AuthService (login)");
        System.out.println("- PatientService");
        System.out.println("- MonitoringService");
        System.out.println("- MedicationService");
        System.out.println("- RehabService");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down server...");
            stop();
        }));
    }

    private void registerService(String serviceName) throws IOException {
        ServiceInfo serviceInfo = ServiceInfo.create(
            SERVICE_TYPE,
            serviceName,
            PORT,
            serviceName.equals("AuthService") ? "Authentication service" : "SmartMed service"
        );
        jmdns.registerService(serviceInfo);
        System.out.println("Registered: " + serviceName);
    }

    private void stop() {
        if (jmdns != null) {
            jmdns.unregisterAllServices();
            try {
                jmdns.close();
            } catch (IOException e) {
                System.err.println("Error closing JmDNS: " + e.getMessage());
            }
        }
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