package distsys.smartmed.client;

import java.net.InetAddress;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.io.IOException;
import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceListener;


public class SmartMedClient {
    private JmDNS jmdns;
    private ManagedChannel channel;

    public SmartMedClient() throws IOException {
        // Use InetAddress.getByName("0.0.0.0") to listen on all interfaces
        jmdns = JmDNS.create(InetAddress.getByName("0.0.0.0"));
        
        jmdns.addServiceListener("_smartmed._tcp.local.", new ServiceListener() {
            @Override
            public void serviceResolved(ServiceEvent event) {
                ServiceInfo info = event.getInfo();
                String host = info.getHostAddresses()[0];
                int port = info.getPort();
                System.out.println("[Client] Discovered: " + host + ":" + port);
                
                // Auto-connect to the first discovered service
                if (channel == null) {
                    channel = ManagedChannelBuilder.forAddress(host, port)
                        .usePlaintext()
                        .build();
                    System.out.println("[Client] Connected to: " + host + ":" + port);
                }
            }

            @Override public void serviceAdded(ServiceEvent event) {
                jmdns.requestServiceInfo(event.getType(), event.getName());
            }

            @Override public void serviceRemoved(ServiceEvent event) {
                System.out.println("[Client] Service left: " + event.getName());
            }
        });
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        SmartMedClient client = new SmartMedClient();
        System.out.println("[Client] Waiting for services... Press Ctrl+C to exit.");
        Thread.currentThread().join(); // Wait indefinitely
    }
}