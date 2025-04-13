package distsys.smartmed.security;

import io.grpc.*;
import java.util.logging.Logger;

public class JwtServerInterceptor implements ServerInterceptor {
    private static final Logger logger = Logger.getLogger(JwtServerInterceptor.class.getName());
    private static final Metadata.Key<String> AUTH_HEADER = 
        Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER);

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

        String token = headers.get(AUTH_HEADER);
        if (token == null) {
            logger.warning("Unauthenticated attempt to access: " + method);
            call.close(Status.UNAUTHENTICATED.withDescription("Missing token"), headers);
            return new ServerCall.Listener<ReqT>() {};
        }

        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        if (!JwtUtil.validateToken(token)) {
            logger.warning("Invalid token for: " + method);
            call.close(Status.UNAUTHENTICATED.withDescription("Invalid token"), headers);
            return new ServerCall.Listener<ReqT>() {};
        }

        String username = JwtUtil.getUsernameFromToken(token);
        logger.info("Authenticated access to " + method + " by " + username);
        return next.startCall(call, headers);
    }
}