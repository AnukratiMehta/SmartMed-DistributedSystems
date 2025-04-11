package distsys.smartmed.client;

import com.healthcare.grpc.auth.*;
import io.grpc.*;

public class AuthClient {
    // MUST match EXACTLY what's in AuthServiceImpl
    private static final String CORRECT_USERNAME = "admin";
    private static final String CORRECT_PASSWORD = "smartmed123";
    
    public static void main(String[] args) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051)
            .usePlaintext()
            .build();
        
        try {
            testLogin(channel, CORRECT_USERNAME, CORRECT_PASSWORD, "SUCCESS CASE");
            testLogin(channel, "wrong", "credentials", "FAILURE CASE");
        } finally {
            channel.shutdown();
        }
    }
    
    private static void testLogin(ManagedChannel channel, 
                               String username, 
                               String password, 
                               String caseName) {
        System.out.printf("%n=== %s ===%n", caseName);
        System.out.printf("Trying: username='%s' password='%s'%n", username, password);
        
        try {
            LoginResponse response = AuthServiceGrpc.newBlockingStub(channel)
                .login(LoginRequest.newBuilder()
                    .setUsername(username)
                    .setPassword(password)
                    .build());
            
            System.out.printf("SUCCESS! Token: %s%n", response.getToken());
        } catch (StatusRuntimeException e) {
            System.out.printf("EXPECTED FAILURE: %s%n", e.getStatus().getDescription());
        }
    }
}