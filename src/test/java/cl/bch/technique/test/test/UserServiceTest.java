package cl.bch.technique.test.test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import cl.bch.technique.test.test.dto.UserDTO;
import cl.bch.technique.test.test.dto.UserResponseDTO;
import cl.bch.technique.test.test.exception.BusinessException;
import cl.bch.technique.test.test.exception.UserNotFoundException;
import cl.bch.technique.test.test.model.User;
import cl.bch.technique.test.test.service.JwtService;
import cl.bch.technique.test.test.service.UserService;
import cl.bch.technique.test.test.util.JsonFileUtil;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private JsonFileUtil jsonFileUtil;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserDTO testUserDTO;
    private final String SECRET_KEY = "testsecretkeytestsecretkeytestsecretkeytestsecretkey";

    @BeforeEach
    void setUp() {
        // Configurar usuario de prueba
        testUser = new User();
        testUser.setId(1L);
        testUser.setRut("12345678-9");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail("john.doe@example.com");
        testUser.setDateBirth(LocalDate.of(1990, 1, 1));
        testUser.setMobilePhone("123456789");

        // Configurar DTO de usuario
        testUserDTO = new UserDTO();
        testUserDTO.setId(1L);
        testUserDTO.setRut("12345678-9");
        testUserDTO.setFirstName("John");
        testUserDTO.setLastName("Doe");
        testUserDTO.setEmail("john.doe@example.com");
        testUserDTO.setDateBirth(LocalDate.of(1990, 1, 1));
        testUserDTO.setMobilePhone("123456789");
        testUserDTO.setPassword("password123");
    }

    @Test
    void getUserById_Success() throws IOException {
        // Arrange
        List<User> users = new ArrayList<>();
        users.add(testUser);
        when(jsonFileUtil.readUsers()).thenReturn(users);
        when(jwtService.getSecretKey()).thenReturn(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()));

        String validToken = Jwts.builder()
                .claim("siguiente_etapa", "consulta_cliente")
                .signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()))
                .compact();

        // Act
        UserResponseDTO response = userService.getUserById(1L, validToken);

        // Assert
        assertNotNull(response);
        assertEquals(testUser.getId(), response.getUser().getId());
        assertEquals(testUser.getRut(), response.getUser().getRut());
        assertNotNull(response.getToken());
        assertTrue(response.getToken().length() > 0);
    }

    @Test
    void getUserById_UserNotFound() throws IOException {
        // Arrange
        when(jsonFileUtil.readUsers()).thenReturn(new ArrayList<>());
        when(jwtService.getSecretKey()).thenReturn(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()));

        String validToken = Jwts.builder()
                .claim("siguiente_etapa", "consulta_cliente")
                .signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()))
                .compact();

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> 
            userService.getUserById(1L, validToken)
        );
    }

    @Test
    void getUserById_InvalidToken() {
        // Arrange
        when(jwtService.getSecretKey()).thenReturn(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()));
        String invalidToken = "invalid.token.here";

        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            userService.getUserById(1L, invalidToken)
        );
    }

    @Test
    void findByRut_Success() throws IOException {
        // Arrange
        List<User> users = new ArrayList<>();
        users.add(testUser);
        when(jsonFileUtil.readUsers()).thenReturn(users);

        // Act
        User result = userService.findByRut("12345678-9");

        // Assert
        assertNotNull(result);
        assertEquals(testUser.getRut(), result.getRut());
    }

    @Test
    void findByRut_NotFound() throws IOException {
        // Arrange
        when(jsonFileUtil.readUsers()).thenReturn(new ArrayList<>());

        // Act
        User result = userService.findByRut("12345678-9");

        // Assert
        assertNull(result);
    }

    @Test
    void saveUser_Success() throws IOException {
        // Arrange
        List<User> users = new ArrayList<>();
        when(jsonFileUtil.readUsers()).thenReturn(users);
        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
        when(jwtService.getSecretKey()).thenReturn(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()));
        
        String validSaveToken = Jwts.builder()
                .claim("siguiente_etapa", "guardar_cliente")
                .signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()))
                .compact();

        // Act
        UserDTO result = userService.saveUser(testUserDTO, validSaveToken);

        // Assert
        assertNotNull(result);
        assertEquals(testUserDTO.getRut(), result.getRut());
        assertEquals(testUserDTO.getEmail(), result.getEmail());
    }

    @Test
    void saveUser_InvalidRut() throws IOException {
        // Arrange
        testUserDTO.setRut("invalid-rut");
        when(jwtService.getSecretKey()).thenReturn(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()));
        
        String validSaveToken = Jwts.builder()
                .claim("siguiente_etapa", "guardar_cliente")
                .signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()))
                .compact();

        // Act & Assert
        assertThrows(BusinessException.class, () -> 
            userService.saveUser(testUserDTO, validSaveToken)
        );
    }

    @Test
    void saveUser_DuplicateRut() throws IOException {
        // Arrange
        List<User> users = new ArrayList<>();
        users.add(testUser);
        when(jsonFileUtil.readUsers()).thenReturn(users);
        when(jwtService.getSecretKey()).thenReturn(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()));
        
        String validSaveToken = Jwts.builder()
                .claim("siguiente_etapa", "guardar_cliente")
                .signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()))
                .compact();

        // Act & Assert
        assertThrows(BusinessException.class, () -> 
            userService.saveUser(testUserDTO, validSaveToken)
        );
    }

    @Test
    void saveUser_InvalidEmail() throws IOException {
        // Arrange
        testUserDTO.setEmail("invalid-email");
        when(jwtService.getSecretKey()).thenReturn(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()));
        
        String validSaveToken = Jwts.builder()
                .claim("siguiente_etapa", "guardar_cliente")
                .signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()))
                .compact();

        // Act & Assert
        assertThrows(BusinessException.class, () -> 
            userService.saveUser(testUserDTO, validSaveToken)
        );
    }

    @Test
    void saveUser_InvalidPassword() throws IOException {
        // Arrange
        testUserDTO.setPassword("short");
        when(jwtService.getSecretKey()).thenReturn(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()));
        
        String validSaveToken = Jwts.builder()
                .claim("siguiente_etapa", "guardar_cliente")
                .signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()))
                .compact();

        // Act & Assert
        assertThrows(BusinessException.class, () -> 
            userService.saveUser(testUserDTO, validSaveToken)
        );
    }

    @Test
    void saveUser_InvalidToken() {
        // Arrange
        when(jwtService.getSecretKey()).thenReturn(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            userService.saveUser(testUserDTO, "invalid.token")
        );
    }

    @Test
    void saveUser_WrongEtapa() {
        // Arrange
        when(jwtService.getSecretKey()).thenReturn(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()));
        
        String wrongEtapaToken = Jwts.builder()
                .claim("siguiente_etapa", "wrong_etapa")
                .signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()))
                .compact();

        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            userService.saveUser(testUserDTO, wrongEtapaToken)
        );
    }
}