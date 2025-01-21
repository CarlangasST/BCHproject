package cl.bch.technique.test.test.dto;

import lombok.Data;
import javax.validation.constraints.NotEmpty;

@Data
public class LoginRequestDTO {
    @NotEmpty(message = "El RUT es obligatorio")
    private String rut;

    @NotEmpty(message = "La contraseña es obligatoria")
    private String password;
}