package cl.bch.technique.test.test.model;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class LoginAttempt {
    private String rut;
    private int numeroIntentos;
    private boolean bloqueado;
}
