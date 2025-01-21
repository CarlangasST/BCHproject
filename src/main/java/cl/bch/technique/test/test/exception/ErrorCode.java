package cl.bch.technique.test.test.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    VALIDATION_ERROR("VAL_001", "Error de validación", HttpStatus.BAD_REQUEST),
    USER_NOT_FOUND("USR_001", "Usuario no encontrado", HttpStatus.NOT_FOUND),
    INVALID_CREDENTIALS("AUT_001", "Credenciales inválidas", HttpStatus.UNAUTHORIZED),
    USER_BLOCKED("AUT_002", "Usuario bloqueado", HttpStatus.FORBIDDEN),
    DUPLICATE_ID("VAL_002", "ID duplicado", HttpStatus.BAD_REQUEST),
    DUPLICATE_RUT("VAL_003", "RUT duplicado", HttpStatus.BAD_REQUEST),
    INVALID_RUT_FORMAT("VAL_004", "Formato de RUT inválido", HttpStatus.BAD_REQUEST),
    INVALID_EMAIL_FORMAT("VAL_005", "Formato de email inválido", HttpStatus.BAD_REQUEST),
    INVALID_PHONE_FORMAT("VAL_006", "Formato de teléfono inválido", HttpStatus.BAD_REQUEST),
    INVALID_NAME_FORMAT("VAL_007", "Formato de nombre inválido", HttpStatus.BAD_REQUEST),
    INVALID_LASTNAME_FORMAT("VAL_008", "Formato de apellido inválido", HttpStatus.BAD_REQUEST),
    INVALID_DATEBIRTH_FORMAT("VAL_009", "Formato de fecha de nacimiento inválido", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD_FORMAT("VAL_010", "Formato de contraseña inválido", HttpStatus.BAD_REQUEST),
    INTERNAL_ERROR("SYS_001", "Error interno del servidor", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
