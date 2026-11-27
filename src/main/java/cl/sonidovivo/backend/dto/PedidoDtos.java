package cl.sonidovivo.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public final class PedidoDtos {

    private PedidoDtos() {
    }

    public record PedidoItemRequest(
            @NotBlank(message = "El código del producto es obligatorio")
            String productoId,

            @NotNull(message = "La cantidad es obligatoria")
            @Min(value = 1, message = "La cantidad debe ser al menos 1")
            Integer cantidad
    ) {
    }

    public record PedidoCreateRequest(
            @NotBlank(message = "El nombre del cliente es obligatorio")
            String nombre,

            @NotBlank(message = "El apellido del cliente es obligatorio")
            String apellido,

            @NotBlank(message = "El RUT es obligatorio")
            String rut,

            @NotBlank(message = "El correo electrónico es obligatorio")
            @Email(message = "Formato de correo inválido")
            String email,

            @NotBlank(message = "El teléfono es obligatorio")
            String telefono,

            @NotBlank(message = "El tipo de entrega es obligatorio (retiro o domicilio)")
            String tipoEntrega,

            String direccion,
            String ciudad,
            String comuna,

            @NotBlank(message = "El método de pago es obligatorio")
            String metodoPago,

            String notas,

            @NotEmpty(message = "El pedido debe incluir al menos un producto")
            @Valid
            List<PedidoItemRequest> items
    ) {
    }

    public record EstadoPedidoRequest(
            @NotBlank(message = "El estado es obligatorio")
            String estado
    ) {
    }

    public record DetallePedidoResponse(
            String id,
            String nombre,
            Integer cantidad,
            Integer precio,
            Integer subtotal
    ) {
    }

    public record PedidoResponse(
            String id,
            String fecha,
            String cliente,
            String rut,
            String email,
            String telefono,
            String tipoEntrega,
            String direccion,
            String ciudad,
            String comuna,
            String metodoPago,
            String notas,
            String estado,
            Integer subtotal,
            Integer costoEnvio,
            Integer total,
            Long usuarioId,
            List<DetallePedidoResponse> items
    ) {
    }
}
