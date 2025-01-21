package cl.bch.technique.test.test.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class JwtService {
    private final SecretKey secretKey;
    private static final long EXPIRATION_TIME = 86400000; // 24 horas

    public String generateToken(String rut, int nroIntento, boolean bloqueo, String siguienteEtapa) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("rut", rut);
        claims.put("nroIntento", nroIntento);
        claims.put("bloqueo", bloqueo);
        claims.put("siguiente_etapa", siguienteEtapa);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public SecretKey getSecretKey() {
        return secretKey;
    }
}
