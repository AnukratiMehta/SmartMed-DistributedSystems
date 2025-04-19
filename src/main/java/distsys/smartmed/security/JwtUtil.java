/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package distsys.smartmed.security;

/**
 * Utility class for generating, validating, and extracting information from JWT tokens.
 * Uses a shared secret key for signing and verifying tokens.
 * Tokens expire after 1 hour.
 * 
 * @author anukratimehta
 */

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.util.Date;

public class JwtUtil {
    private static final String SECRET_STRING = "smartmed-secret-key-1234567890-abcdefgh";
    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(SECRET_STRING.getBytes());
    private static final long EXPIRATION_TIME = 3600000; // 1 hour in milliseconds

    /**
     * Generates a token with a default subject ("default-client").
     * 
     * @return the generated JWT token string
     */
    public static String generateToken() {
        return generateToken("default-client");
    }

    /**
     * Generates a JWT token for the given username.
     * 
     * @param username the subject or username for which the token is generated
     * @return the generated JWT token string
     */
    public static String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY)
                .compact();
    }

    /**
     * Validates a JWT token's signature and expiration.
     * 
     * @param token the JWT token string to validate
     * @return true if the token is valid, false otherwise
     */
    public static boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            System.err.println("[JWT] Invalid token: " + e.getMessage());
            return false;
        }
    }

    /**
     * Extracts the username (subject) from a valid JWT token.
     * 
     * @param token the JWT token string
     * @return the username if extraction succeeds, or "unknown" if it fails
     */
    public static String getUsernameFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
        } catch (Exception e) {
            System.err.println("[JWT] Failed to get username from token: " + e.getMessage());
            return "unknown";
        }
    }
}
