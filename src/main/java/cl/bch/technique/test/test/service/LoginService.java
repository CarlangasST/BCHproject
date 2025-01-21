package cl.bch.technique.test.test.service;

import cl.bch.technique.test.test.dto.LoginRequestDTO;
import cl.bch.technique.test.test.dto.LoginResponseDTO;
import cl.bch.technique.test.test.exception.UserBlockedException;
import cl.bch.technique.test.test.exception.InvalidCredentialsException;
import cl.bch.technique.test.test.model.LoginAttempt;
import cl.bch.technique.test.test.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class LoginService {
    private final UserService userService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    // Cache para almacenar intentos de login (en producción debería usar Redis o
    // similar)
    private final Map<String, LoginAttempt> loginAttempts = new ConcurrentHashMap<>();
    private static final int MAX_INTENTOS = 3;

    public LoginResponseDTO login(LoginRequestDTO request) {
        // Verificar si el usuario está bloqueado
        LoginAttempt attempt = loginAttempts.getOrDefault(request.getRut(),
                LoginAttempt.builder()
                        .rut(request.getRut())
                        .numeroIntentos(0)
                        .bloqueado(false)
                        .build());

        if (attempt.isBloqueado()) {
            throw new UserBlockedException("Usuario bloqueado por exceder el número máximo de intentos");
        }

        // Buscar usuario y validar credenciales
        User user = userService.findByRut(request.getRut());
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            // Incrementar contador de intentos
            attempt.setNumeroIntentos(attempt.getNumeroIntentos() + 1);

            // Verificar si debe ser bloqueado
            if (attempt.getNumeroIntentos() >= MAX_INTENTOS) {
                attempt.setBloqueado(true);
                loginAttempts.put(request.getRut(), attempt);
                throw new UserBlockedException("Usuario bloqueado por exceder el número máximo de intentos");
            }

            loginAttempts.put(request.getRut(), attempt);
            throw new InvalidCredentialsException("Credenciales inválidas");
        }

        // Credenciales correctas, generar token
        String token = jwtService.generateToken(
                request.getRut(),
                attempt.getNumeroIntentos(),
                attempt.isBloqueado(),
                "consulta_cliente");

        // Resetear intentos si el login es exitoso
        loginAttempts.remove(request.getRut());

        return LoginResponseDTO.builder()
                .token(token)
                .mensaje("Login exitoso")
                .bloqueado(false)
                .build();
    }
}