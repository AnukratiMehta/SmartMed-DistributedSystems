/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package distsys.smartmed.security;

import io.grpc.*;

/**
 * gRPC client interceptor for adding a JWT token to the request headers.
 * This interceptor attaches the provided token with a "Bearer " prefix to the request headers
 * for each outgoing call.
 * 
 * @author anukratimehta
 */

public class JwtClientInterceptor implements ClientInterceptor {
    private final String token;

    /**
     * Constructor that initializes the interceptor with the given JWT token.
     * 
     * @param token the JWT token to be added to the request headers
     */
    public JwtClientInterceptor(String token) {
        this.token = token;
    }

    /**
     * Intercepts the outgoing client call and adds the JWT token to the headers.
     * 
     * @param <ReqT> the type of the request
     * @param <RespT> the type of the response
     * @param method the method descriptor for the call
     * @param callOptions the call options
     * @param next the channel to be called
     * @return a client call with the token added to the headers
     */
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
