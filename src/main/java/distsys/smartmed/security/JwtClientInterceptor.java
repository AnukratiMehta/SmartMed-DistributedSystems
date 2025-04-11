package distsys.smartmed.security;

import io.grpc.*;

public class JwtClientInterceptor implements ClientInterceptor {
    private final String token;

    public JwtClientInterceptor(String token) {
        this.token = token;
    }

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
        MethodDescriptor<ReqT, RespT> method,
        CallOptions callOptions,
        Channel next) {
        
        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(
            next.newCall(method, callOptions)) {
            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                // Add the token with "Bearer " prefix
                headers.put(Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER), 
                          "Bearer " + token);
                super.start(responseListener, headers);
            }
        };
    }
}