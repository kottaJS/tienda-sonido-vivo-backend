package cl.sonidovivo.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class UsuarioDtos {

    private UsuarioDtos() {
    }

    public record UsuarioResponse(
            Long id,
            String nombre,
            String email,
            String rut,
            String ciudad,
            String rol,
            boolean activo
    ) {
    }

    public record UsuarioCreateRequest(
            @NotBlank(message = "El nombre es obligatorio")
            @Size(min = 2, max = 120)
            String nombre,

            @NotBlank(message = "El correo es obligatorio")
            @Email(message = "Formato de correo inválido")
            String email,

            @NotBlank(message = "El RUT es obligatorio")
            String rut,

            @NotBlank(message = "La ciudad es obligatoria")
            String ciudad,

            @NotBlank(message = "El rol es obligatorio")
            String rol,

            @Size(min = 6, max = 100, message = "La contraseña debe tener al menos 6 caracteres")
            String password
    ) {
    }

    public record UsuarioUpdateRequest(
            @NotBlank(message = "El nombre es obligatorio")
            String nombre,

            @NotBlank(message = "El RUT es obligatorio")
            String rut,

            @NotBlank(message = "La ciudad es obligatoria")
            String ciudad,

            @NotBlank(message = "El rol es obligatorio")
            String rol,

            Boolean activo
    ) {
    }

    public record UsuarioRolRequest(
            @NotBlank(message = "El rol es obligatorio")
            String rol
    ) {
    }
}
