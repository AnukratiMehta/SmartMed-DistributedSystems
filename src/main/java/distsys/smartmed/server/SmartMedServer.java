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
        
        // Register services
        registerService("PatientService");
        registerService("MonitoringService");
        registerService("MedicationService");
        registerService("RehabService");

        // Start server with JWT interceptor
        server = ServerBuilder.forPort(PORT)
            .intercept(new JwtServerInterceptor())
            .addService(new PatientServiceImpl())
            .addService(new MonitoringServiceImpl())
            .addService(new MedicationServiceImpl())
            .addService(new RehabServiceImpl())
            .build()
            .start();

        System.out.println("Secure server started on port " + PORT);
        
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
            "Secure gRPC service"
        );
        jmdns.registerService(serviceInfo);
        System.out.println("Registered service: " + serviceName);
    }

    private void stop() {
        if (jmdns != null) {
            jmdns.unregisterAllServices();
            try { jmdns.close(); } catch (IOException e) {}
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