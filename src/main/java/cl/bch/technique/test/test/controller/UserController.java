package cl.bch.technique.test.test.controller;

import cl.bch.technique.test.test.dto.LoginRequestDTO;
import cl.bch.technique.test.test.dto.LoginResponseDTO;
import cl.bch.technique.test.test.dto.UserDTO;
import cl.bch.technique.test.test.dto.UserResponseDTO;
import cl.bch.technique.test.test.service.LoginService;
import cl.bch.technique.test.test.service.UserService;
import lombok.RequiredArgsConstructor;

import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cliente")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final LoginService loginService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(loginService.login(request));
    }

    @GetMapping("/consulta/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id, @RequestHeader("Auth-x") String token) {
        return ResponseEntity.ok(userService.getUserById(id, token));
    }

    @PostMapping("/guardar")
    public ResponseEntity<Void> saveUser(@RequestBody UserDTO userDTO, @RequestHeader("Auth-x") String token) {
        userService.saveUser(userDTO, token);
        return ResponseEntity.noContent().build();
    }
}