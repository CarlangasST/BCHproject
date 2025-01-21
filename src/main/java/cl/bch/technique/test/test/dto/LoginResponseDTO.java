package cl.bch.technique.test.test.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponseDTO {
    private String token;
    private String mensaje;
    private boolean bloqueado;

    public LoginResponseDTO() {
    }

    public LoginResponseDTO(String token, String mensaje, boolean bloqueado) {
        this.token = token;
        this.mensaje = mensaje;
        this.bloqueado = bloqueado;
    }
}