package distsys.smartmed.client;

import com.healthcare.grpc.auth.*;
import io.grpc.*;

/**
 * AuthClient is a simple gRPC client for testing the login functionality
 * of the authentication service. It attempts to log in with valid and invalid
 * credentials and prints the result for each case.
 * 
 * @author anukratimehta
 */
public class AuthClient {
    // MUST match EXACTLY what's in AuthServiceImpl
    private static final String CORRECT_USERNAME = "admin";
    private static final String CORRECT_PASSWORD = "smartmed123";

    /**
     * Main method that sets up the gRPC channel and tests login with both
     * correct and incorrect credentials.
     * 
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        // Set up the gRPC channel to the server at localhost:50051
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051)
            .usePlaintext()  // Disable encryption for local communication
            .build();
        
        try {
            // Test with valid credentials
            testLogin(channel, CORRECT_USERNAME, CORRECT_PASSWORD, "SUCCESS CASE");
            
            // Test with invalid credentials
            testLogin(channel, "wrong", "credentials", "FAILURE CASE");
        } finally {
            // Shut down the channel after testing
            channel.shutdown();
        }
    }
    
    /**
     * Attempts to log in with the provided username and password and prints the result.
     * 
     * @param channel gRPC channel for communication with the authentication service
     * @param username the username to attempt to log in with
     * @param password the password to attempt to log in with
     * @param caseName a description of the case being tested (e.g., SUCCESS CASE, FAILURE CASE)
     */
    private static void testLogin(ManagedChannel channel, 
                               String username, 
                               String password, 
                               String caseName) {
        System.out.printf("%n=== %s ===%n", caseName);
        System.out.printf("Trying: username='%s' password='%s'%n", username, password);
        
        try {
            // Send login request to the service and retrieve the response
            LoginResponse response = AuthServiceGrpc.newBlockingStub(channel)
                .login(LoginRequest.newBuilder()
                    .setUsername(username)
                    .setPassword(password)
                    .build());
            
            // Print the success result and the returned token
            System.out.printf("SUCCESS! Token: %s%n", response.getToken());
        } catch (StatusRuntimeException e) {
            // Handle failure cases and print the error description
            System.out.printf("EXPECTED FAILURE: %s%n", e.getStatus().getDescription());
        }
    }
}
