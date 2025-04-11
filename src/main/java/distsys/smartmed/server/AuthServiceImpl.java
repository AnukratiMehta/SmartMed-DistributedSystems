package distsys.smartmed.server;

import com.healthcare.grpc.auth.*;
import distsys.smartmed.security.JwtUtil;
import io.grpc.stub.StreamObserver;
import io.grpc.Status;


public class AuthServiceImpl extends AuthServiceGrpc.AuthServiceImplBase {
    // EXACT expected credentials (copy these to your client)
    private static final String EXPECTED_USERNAME = "admin";
    private static final String EXPECTED_PASSWORD = "smartmed123"; 

    @Override
    public void login(LoginRequest request, StreamObserver<LoginResponse> responseObserver) {
        String username = request.getUsername().trim();
        String password = request.getPassword().trim();
        
        System.out.printf("Login attempt: '%s'/'%s'%n", username, password);
        
        if (EXPECTED_USERNAME.equals(username) && 
            EXPECTED_PASSWORD.equals(password)) {
            
            String token = JwtUtil.generateToken(username);
            System.out.println("Generated token for " + username);
            
            responseObserver.onNext(LoginResponse.newBuilder()
                .setToken(token)
                .setMessage("Login successful")
                .build());
            responseObserver.onCompleted();
        } else {
            System.out.println("Rejected credentials");
            responseObserver.onError(Status.UNAUTHENTICATED
                .withDescription("Invalid credentials")
                .asRuntimeException());
        }
    }
}