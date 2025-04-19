package distsys.smartmed.client;

import distsys.smartmed.security.JwtClientInterceptor;
import distsys.smartmed.security.JwtUtil;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.io.IOException;
import java.net.InetAddress;
import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

/**
 * The SmartMedClient class represents a gRPC client that connects to a remote service
 * using dynamic service discovery (via JmDNS) and JWT-based authentication.
 * It listens for available services on the local network and connects to them securely using
 * a JWT token for authentication.
 * 
 * @author anukratimehta
 */
public class SmartMedClient {
    private JmDNS jmdns;
    private ManagedChannel channel;
    private final String jwtToken;

    /**
     * Constructs a SmartMedClient instance.
     * It generates a JWT token and listens for services using JmDNS to discover available services.
     * Once a service is resolved, it connects to it using gRPC with the JWT token for authentication.
     * 
     * @throws IOException if an I/O error occurs while creating JmDNS or managing the channel
     */
    public SmartMedClient() throws IOException {
        // Generate a JWT token for authentication
        this.jwtToken = JwtUtil.generateToken();

        // Initialize JmDNS for service discovery
        jmdns = JmDNS.create(InetAddress.getByName("0.0.0.0"));
        
        // Add a service listener for discovering services of type "_smartmed._tcp.local."
        jmdns.addServiceListener("_smartmed._tcp.local.", new ServiceListener() {
            @Override
            public void serviceResolved(ServiceEvent event) {
                // When a service is resolved, extract the host and port
                ServiceInfo info = event.getInfo();
                String host = info.getHostAddresses()[0];
                int port = info.getPort();
                
                // If not connected, establish a connection with the service
                if (channel == null) {
                    channel = ManagedChannelBuilder.forAddress(host, port)
                        .intercept(new JwtClientInterceptor(jwtToken))  // Intercept requests with JWT for authentication
                        .usePlaintext()  // Disable encryption for local connections
                        .build();
                    System.out.println("Connected to secure service at " + host + ":" + port);
                }
            }

            @Override 
            public void serviceAdded(ServiceEvent event) {
                // Request service info for newly added services
                jmdns.requestServiceInfo(event.getType(), event.getName());
            }

            @Override 
            public void serviceRemoved(ServiceEvent event) {
                // Log when a service is removed from the network
                System.out.println("Service left: " + event.getName());
            }
        });
    }

    /**
     * The entry point for the SmartMedClient application.
     * Initializes the client and starts listening for services.
     * 
     * @param args command-line arguments
     * @throws IOException if an I/O error occurs during client initialization
     * @throws InterruptedException if the main thread is interrupted
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        // Create a SmartMedClient instance and start the service discovery
        SmartMedClient client = new SmartMedClient();
        System.out.println("Client started. Press Ctrl+C to exit.");
        // Keep the client running indefinitely
        Thread.currentThread().join();
    }
}
