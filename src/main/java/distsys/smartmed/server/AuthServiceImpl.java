package distsys.smartmed.server;

import com.healthcare.grpc.auth.*;
import distsys.smartmed.security.JwtUtil;
import io.grpc.stub.StreamObserver;
import io.grpc.Status;
import java.util.logging.Logger;

public class AuthServiceImpl extends AuthServiceGrpc.AuthServiceImplBase {
    private static final Logger logger = Logger.getLogger(AuthServiceImpl.class.getName());
    private static final String EXPECTED_USERNAME = "admin";
    private static final String EXPECTED_PASSWORD = "smartmed123";

    @Override
    public void login(LoginRequest request, StreamObserver<LoginResponse> responseObserver) {
        String username = request.getUsername();
        logger.info("Login attempt for user: " + username);

        if (EXPECTED_USERNAME.equals(username) && 
            EXPECTED_PASSWORD.equals(request.getPassword())) {
            
            String token = JwtUtil.generateToken(username);
            logger.info("Successful login for: " + username);
            
            responseObserver.onNext(LoginResponse.newBuilder()
                .setToken(token)
                .setMessage("Login successful")
                .build());
            responseObserver.onCompleted();
        } else {
            logger.warning("Failed login attempt for: " + username);
            responseObserver.onError(Status.UNAUTHENTICATED
                .withDescription("Invalid credentials")
                .asRuntimeException());
        }
    }
}