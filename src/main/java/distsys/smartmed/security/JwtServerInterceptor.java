/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package distsys.smartmed.security;

import io.grpc.*;
import java.util.logging.Logger;

/**
 * gRPC interceptor for authenticating requests using JWT tokens.
 * This interceptor checks for a valid "authorization" header and verifies the token's validity
 * before allowing access to any service endpoints, except for the login endpoint.
 * 
 * @author anukratimehta
 */
public class JwtServerInterceptor implements ServerInterceptor {
    private static final Logger logger = Logger.getLogger(JwtServerInterceptor.class.getName());
    private static final Metadata.Key<String> AUTH_HEADER = 
        Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER);

    /**
     * Intercepts the incoming gRPC call to check for authentication using JWT.
     * If the token is missing or invalid, the request is rejected with an UNAUTHENTICATED status.
     * 
     * @param <ReqT> the type of the request
     * @param <RespT> the type of the response
     * @param call the server call to be processed
     * @param headers the metadata containing the headers of the request
     * @param next the handler to call after authentication
     * @return a listener for the server call
     */
    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
        ServerCall<ReqT, RespT> call,
        Metadata headers,
        ServerCallHandler<ReqT, RespT> next) {
        
        String method = call.getMethodDescriptor().getFullMethodName();
        
        // Skip auth for login endpoint
        if (method.equals("healthcare.AuthService/Login")) {
            return next.startCall(call, headers);
        }

        // Extract the token from the headers
        String token = headers.get(AUTH_HEADER);
        if (token == null) {
            logger.warning("Unauthenticated attempt to access: " + method);
            call.close(Status.UNAUTHENTICATED.withDescription("Missing token"), headers);
            return new ServerCall.Listener<ReqT>() {};
        }

        // Remove "Bearer " prefix if present
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // Validate the token
        if (!JwtUtil.validateToken(token)) {
            logger.warning("Invalid token for: " + method);
            call.close(Status.UNAUTHENTICATED.withDescription("Invalid token"), headers);
            return new ServerCall.Listener<ReqT>() {};
        }

        // Extract username from the token and log authenticated access
        String username = JwtUtil.getUsernameFromToken(token);
        logger.info("Authenticated access to " + method + " by " + username);
        return next.startCall(call, headers);
    }
}
