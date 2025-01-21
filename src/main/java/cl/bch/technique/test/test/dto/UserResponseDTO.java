package cl.bch.technique.test.test.dto;

import lombok.Data;

@Data
public class UserResponseDTO {
    private UserDTO user;
    private String token;
}
