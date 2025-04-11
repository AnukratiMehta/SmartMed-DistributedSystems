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

public class SmartMedClient {
    private JmDNS jmdns;
    private ManagedChannel channel;
    private final String jwtToken;

    public SmartMedClient() throws IOException {
        this.jwtToken = JwtUtil.generateToken();
        jmdns = JmDNS.create(InetAddress.getByName("0.0.0.0"));
        
        jmdns.addServiceListener("_smartmed._tcp.local.", new ServiceListener() {
            @Override
            public void serviceResolved(ServiceEvent event) {
                ServiceInfo info = event.getInfo();
                String host = info.getHostAddresses()[0];
                int port = info.getPort();
                
                if (channel == null) {
                    channel = ManagedChannelBuilder.forAddress(host, port)
                        .intercept(new JwtClientInterceptor(jwtToken))
                        .usePlaintext()
                        .build();
                    System.out.println("Connected to secure service at " + host + ":" + port);
                }
            }

            @Override public void serviceAdded(ServiceEvent event) {
                jmdns.requestServiceInfo(event.getType(), event.getName());
            }

            @Override public void serviceRemoved(ServiceEvent event) {
                System.out.println("Service left: " + event.getName());
            }
        });
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        SmartMedClient client = new SmartMedClient();
        System.out.println("Client started. Press Ctrl+C to exit.");
        Thread.currentThread().join();
    }
}