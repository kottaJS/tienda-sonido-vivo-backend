package cl.sonidovivo.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record LoginRequest(
            @NotBlank(message = "El correo es obligatorio")
            @Email(message = "El correo no tiene un formato válido")
            String email,

            @NotBlank(message = "La contraseña es obligatoria")
            String password
    ) {
    }

    public record RegisterRequest(
            @NotBlank(message = "El nombre es obligatorio")
            @Size(min = 2, max = 120, message = "El nombre debe tener entre 2 y 120 caracteres")
            String nombre,

            @NotBlank(message = "El correo es obligatorio")
            @Email(message = "El correo no tiene un formato válido")
            String email,

            @NotBlank(message = "La contraseña es obligatoria")
            @Size(min = 6, max = 100, message = "La contraseña debe tener al menos 6 caracteres")
            String password,

            @NotBlank(message = "El RUT es obligatorio")
            String rut,

            @NotBlank(message = "La ciudad es obligatoria")
            String ciudad
    ) {
    }

    public record AuthResponse(
            String token,
            String tipo,
            long expiraEnMs,
            UsuarioDtos.UsuarioResponse usuario
    ) {
    }
}
