/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package distsys.smartmed.server;

/**
 * gRPC authentication service implementation.
 * Handles login requests by validating credentials and generating JWT tokens.
 * 
 * @author anukratimehta
 */

import com.healthcare.grpc.auth.*;
import distsys.smartmed.common.LoggingUtils;
import distsys.smartmed.security.JwtUtil;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import java.util.logging.Logger;

public class AuthServiceImpl extends AuthServiceGrpc.AuthServiceImplBase {
    private static final Logger logger = Logger.getLogger(AuthServiceImpl.class.getName());

    // Hardcoded expected credentials for demonstration purposes
    private static final String EXPECTED_USERNAME = "admin";
    private static final String EXPECTED_PASSWORD = "smartmed123";

    /**
     * Handles user login requests.
     * If credentials match, a JWT token is generated and returned.
     * Otherwise, an UNAUTHENTICATED error is returned.
     *
     * @param request the LoginRequest containing username and password
     * @param responseObserver the StreamObserver used to return the LoginResponse
     */
    @Override
    public void login(LoginRequest request, StreamObserver<LoginResponse> responseObserver) {
        String username = request.getUsername();
        
        try {
            // Log the start of the authentication process
            LoggingUtils.logServiceStart(logger, "AuthService", "User: " + username);
            
            // Check if the provided credentials match the expected values
            if (EXPECTED_USERNAME.equals(username) && 
                EXPECTED_PASSWORD.equals(request.getPassword())) {
                
                // Generate a JWT token for the authenticated user
                String token = JwtUtil.generateToken(username);
                
                // Send a successful login response
                responseObserver.onNext(LoginResponse.newBuilder()
                    .setToken(token)
                    .setMessage("Login successful")
                    .build());
                responseObserver.onCompleted();
                
                // Log successful authentication
                LoggingUtils.logServiceEnd(logger, "AuthService", "User: " + username, "Login succeeded");
                
            } else {
                // Handle incorrect credentials
                String errorMsg = "Invalid credentials for user: " + username;
                LoggingUtils.logError(logger, "AuthService", "User: " + username, 
                    new IllegalArgumentException(errorMsg), true);
                
                // Return authentication error to the client
                responseObserver.onError(Status.UNAUTHENTICATED
                    .withDescription(errorMsg)
                    .asRuntimeException());
            }
            
        } catch (Exception e) {
            // Handle internal errors during login
            LoggingUtils.logError(logger, "AuthService", "User: " + username, e, false);
            responseObserver.onError(Status.INTERNAL
                .withDescription("Authentication error")
                .asRuntimeException());
        }
    }
}
