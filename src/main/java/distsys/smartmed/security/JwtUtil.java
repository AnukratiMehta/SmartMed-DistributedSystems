package distsys.smartmed.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.util.Date;

public class JwtUtil {
    // Fixed secret key (same for client and server)
    private static final String SECRET_STRING = "smartmed-secret-key-1234567890-abcdefgh";
    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(SECRET_STRING.getBytes());
    private static final long EXPIRATION_TIME = 3600000; // 1 hour

    public static String generateToken() {
        return generateToken("default-client"); // Default username for simple cases
    }
    
    public static String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY)
                .compact();
    }

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
}