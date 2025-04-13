package distsys.smartmed.server;

import com.healthcare.grpc.auth.*;
import distsys.smartmed.common.LoggingUtils;
import distsys.smartmed.security.JwtUtil;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import java.util.logging.Logger;

public class AuthServiceImpl extends AuthServiceGrpc.AuthServiceImplBase {
    private static final Logger logger = Logger.getLogger(AuthServiceImpl.class.getName());
    private static final String EXPECTED_USERNAME = "admin";
    private static final String EXPECTED_PASSWORD = "smartmed123";

    @Override
    public void login(LoginRequest request, StreamObserver<LoginResponse> responseObserver) {
        String username = request.getUsername();
        
        try {
            // Standardized service start log
            LoggingUtils.logServiceStart(logger, "AuthService", "User: " + username);
            
            if (EXPECTED_USERNAME.equals(username) && 
                EXPECTED_PASSWORD.equals(request.getPassword())) {
                
                String token = JwtUtil.generateToken(username);
                
                responseObserver.onNext(LoginResponse.newBuilder()
                    .setToken(token)
                    .setMessage("Login successful")
                    .build());
                responseObserver.onCompleted();
                
                // Standardized success log
                LoggingUtils.logServiceEnd(logger, "AuthService", "User: " + username, "Login succeeded");
                
            } else {
                String errorMsg = "Invalid credentials for user: " + username;
                LoggingUtils.logError(logger, "AuthService", "User: " + username, 
                    new IllegalArgumentException(errorMsg), true);
                
                responseObserver.onError(Status.UNAUTHENTICATED
                    .withDescription(errorMsg)
                    .asRuntimeException());
            }
            
        } catch (Exception e) {
            LoggingUtils.logError(logger, "AuthService", "User: " + username, e, false);
            responseObserver.onError(Status.INTERNAL
                .withDescription("Authentication error")
                .asRuntimeException());
        }
    }
}