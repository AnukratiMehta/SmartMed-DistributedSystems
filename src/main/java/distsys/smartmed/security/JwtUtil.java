package distsys.smartmed.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.util.Date;

public class JwtUtil {
    // Use a fixed secret key (in production, store this securely)
    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(
        "my-secret-key-1234-my-secret-key-1234".getBytes()
    );
    private static final long EXPIRATION_TIME = 3600000; // 1 hour

    public static String generateToken() {
        return Jwts.builder()
            .setSubject("smartmed-client")
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
            System.err.println("JWT validation error: " + e.getMessage());
            return false;
        }
    }
}