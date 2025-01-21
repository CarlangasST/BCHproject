package cl.bch.technique.test.test.service;

import cl.bch.technique.test.test.dto.UserDTO;
import cl.bch.technique.test.test.dto.UserResponseDTO;
import cl.bch.technique.test.test.exception.BusinessException;
import cl.bch.technique.test.test.exception.ErrorCode;
import cl.bch.technique.test.test.exception.UserNotFoundException;
import cl.bch.technique.test.test.model.User;
import cl.bch.technique.test.test.util.JsonFileUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final JsonFileUtil jsonFileUtil;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    /**
     * Busca un usuario por su ID
     * 
     * @param id ID del usuario
     * @return UserResponseDTO con la información del usuario y el token actualizado
     * @throws UserNotFoundException si el usuario no existe
     */
    public UserResponseDTO getUserById(Long id, String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(jwtService.getSecretKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String siguienteEtapa = claims.get("siguiente_etapa", String.class);

            if (!"consulta_cliente".equals(siguienteEtapa)) {
                throw new RuntimeException("Invalid siguiente_etapa value");
            }

            List<User> users = jsonFileUtil.readUsers();

            UserDTO userDTO = users.stream()
                    .filter(user -> user.getId().equals(id))
                    .findFirst()
                    .map(this::convertToDTO)
                    .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con ID: " + id));

            userDTO.setPassword(null);

            // Update the "siguiente_etapa" field in the JWT token
            claims.put("siguiente_etapa", "guardar_cliente");
            String updatedToken = Jwts.builder()
                    .setClaims(claims)
                    .signWith(jwtService.getSecretKey(), SignatureAlgorithm.HS256)
                    .compact();
            // Create UserResponseDTO
            UserResponseDTO userResponseDTO = new UserResponseDTO();
            userResponseDTO.setUser(userDTO);
            userResponseDTO.setToken(updatedToken);

            return userResponseDTO;
        } catch (io.jsonwebtoken.security.SecurityException e) {
            log.error("Invalid JWT signature", e);
            throw new RuntimeException("Unauthorized", e);
        } catch (IOException e) {
            log.error("Error al leer el archivo de usuarios", e);
            throw new RuntimeException("Error al procesar la solicitud", e);
        }
    }

    /**
     * Busca un usuario por su RUT
     * 
     * @param rut RUT del usuario
     * @return User si existe, null si no existe
     */
    public User findByRut(String rut) {
        try {
            List<User> users = jsonFileUtil.readUsers();
            return users.stream()
                    .filter(user -> user.getRut().equals(rut))
                    .findFirst()
                    .orElse(null);
        } catch (IOException e) {
            log.error("Error al leer el archivo de usuarios", e);
            throw new RuntimeException("Error al procesar la solicitud", e);
        }
    }

    /**
     * Guarda un nuevo usuario o actualiza uno existente
     * 
     * @param userDTO Datos del usuario a guardar
     * @param token   Token JWT con información de la etapa actual
     * @return UserDTO con la información del usuario guardado
     */
    public UserDTO saveUser(UserDTO userDTO, String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(jwtService.getSecretKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String siguienteEtapa = claims.get("siguiente_etapa", String.class);

            if (!"guardar_cliente".equals(siguienteEtapa)) {
                throw new RuntimeException("Invalid siguiente_etapa value");
            }
            // Validar campos obligatorios y formato
            validateUser(userDTO);

            List<User> users = jsonFileUtil.readUsers();
            User user = convertToEntity(userDTO);

            // Validar ID único
            validateUniqueId(users, user.getId());

            // Validar Rut único
            validateUniqueRut(users, user.getRut());

            users.add(user);
            // jsonFileUtil.writeUsers(users);

            return convertToDTO(user);
        } catch (IOException e) {
            log.error("Error al guardar el usuario", e);
            throw new RuntimeException("Error al procesar la solicitud", e);
        }
    }

    private void validateUser(UserDTO user) {
        // Validar RUT
        if (user.getRut() == null || user.getRut().trim().isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "El RUT es obligatorio");
        }
        if (!validateRutFormat(user.getRut())) {
            throw new BusinessException(ErrorCode.INVALID_RUT_FORMAT);
        }

        // Validar nombres
        if (user.getFirstName() != null && !user.getFirstName().trim().isEmpty()
                && user.getFirstName().length() > 50 || user.getFirstName().length() < 2) {
            throw new BusinessException(ErrorCode.INVALID_NAME_FORMAT);
        }

        if (user.getLastName() != null && !user.getLastName().trim().isEmpty()
                && user.getLastName().length() > 50 || user.getLastName().length() < 2) {
            throw new BusinessException(ErrorCode.INVALID_LASTNAME_FORMAT);
        }

        // Validar fecha de nacimiento
        if (user.getDateBirth() != null && user.getDateBirth().isAfter(LocalDate.now())) {
            throw new BusinessException(ErrorCode.INVALID_DATEBIRTH_FORMAT);
        }

        // Validar teléfono
        if (user.getMobilePhone() != null && !user.getMobilePhone().trim().isEmpty()
                && !user.getMobilePhone().matches("\\d{9}")) {
            throw new BusinessException(ErrorCode.INVALID_PHONE_FORMAT);
        }

        // Validar email
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "El email es obligatorio");
        }
        if (!validateEmailFormat(user.getEmail())) {
            throw new BusinessException(ErrorCode.INVALID_EMAIL_FORMAT);
        }

        // Encriptar password
        if (user.getPassword() == null || user.getPassword().isEmpty() || user.getPassword().length() <= 8) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD_FORMAT);
        } else {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
    }

    private boolean validateRutFormat(String rut) {
        // Formato básico: XXXXXXXX-X
        return rut.matches("\\d{7,8}-[\\dkK]");
    }

    private boolean validateEmailFormat(String email) {
        // Formato básico de email
        return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    private void validateUniqueRut(List<User> users, String rut) {
        boolean rutExists = users.stream()
                .anyMatch(user -> user.getRut().equals(rut));

        if (rutExists) {
            throw new BusinessException(ErrorCode.DUPLICATE_RUT);
        }
    }

    private void validateUniqueId(List<User> users, Long id) {
        boolean idExists = users.stream()
                .anyMatch(user -> user.getId().equals(id));

        if (idExists) {
            throw new BusinessException(ErrorCode.DUPLICATE_ID, "El ID ya existe");
        }
    }

    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setRut(user.getRut());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setDateBirth(user.getDateBirth());
        dto.setMobilePhone(user.getMobilePhone());
        dto.setEmail(user.getEmail());
        dto.setAddress(user.getAddress());
        dto.setCityId(user.getCityId());
        dto.setSessionActive(user.isSessionActive());
        return dto;
    }

    private User convertToEntity(UserDTO dto) {
        User user = new User();
        user.setId(dto.getId());
        user.setRut(dto.getRut());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setDateBirth(dto.getDateBirth());
        user.setMobilePhone(dto.getMobilePhone());
        user.setEmail(dto.getEmail());
        user.setAddress(dto.getAddress());
        user.setCityId(dto.getCityId());
        user.setSessionActive(dto.isSessionActive());
        return user;
    }
}
