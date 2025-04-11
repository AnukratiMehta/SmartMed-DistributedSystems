package distsys.smartmed.security;

import io.grpc.*;

public class JwtServerInterceptor implements ServerInterceptor {
    private static final Metadata.Key<String> AUTH_HEADER = 
        Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER);

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
        ServerCall<ReqT, RespT> call,
        Metadata headers,
        ServerCallHandler<ReqT, RespT> next) {
        
        String token = headers.get(AUTH_HEADER);
        if (token == null) {
            call.close(Status.UNAUTHENTICATED.withDescription("Authorization token is missing"), headers);
            return new ServerCall.Listener<ReqT>() {};
        }

        // Remove "Bearer " prefix if present
        if (token.startsWith("Bearer ")) {
            token = token.substring(7).trim();
        }

        if (!JwtUtil.validateToken(token)) {
            call.close(Status.UNAUTHENTICATED.withDescription("Invalid JWT token"), headers);
            return new ServerCall.Listener<ReqT>() {};
        }

        return next.startCall(call, headers);
    }
}