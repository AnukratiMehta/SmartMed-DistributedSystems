
package distsys.smartmed.server;

/**
 *
 * @author anukratimehta
 */


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
        // Create JmDNS instance (as shown in PPT)
    jmdns = JmDNS.create(InetAddress.getByName("0.0.0.0"));

        // Register all services (PPT Slide jmDNS II-III)
        registerService("PatientService", "Patient records service");
        registerService("MonitoringService", "Real-time vitals monitoring");
        registerService("MedicationService", "Medication tracking");
        registerService("RehabService", "Physical therapy feedback");

        // Start gRPC server
        server = ServerBuilder.forPort(PORT)
            .addService(new PatientServiceImpl())
            .addService(new MonitoringServiceImpl())
            .addService(new MedicationServiceImpl())
            .addService(new RehabServiceImpl())
            .build()
            .start();

        System.out.println("Server started on port " + PORT + " with services:");
        System.out.println("- PatientService");
        System.out.println("- MonitoringService");
        System.out.println("- MedicationService");
        System.out.println("- RehabService");

        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down server...");
            stop();
        }));
    }

    private void registerService(String serviceName, String serviceDesc) throws IOException {
        ServiceInfo serviceInfo = ServiceInfo.create(
            SERVICE_TYPE,
            serviceName,
            PORT,
            serviceDesc
        );
        jmdns.registerService(serviceInfo);
        System.out.println("Registered service: " + serviceName);
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