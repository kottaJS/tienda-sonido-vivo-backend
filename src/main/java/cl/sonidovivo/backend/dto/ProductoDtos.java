package cl.sonidovivo.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class ProductoDtos {

    private ProductoDtos() {
    }

    public record ProductoResponse(
            String id,
            String categoria,
            Long categoriaId,
            String nombre,
            String marca,
            String modelo,
            Integer stock,
            Integer precio,
            String descripcion,
            String imagen,
            boolean activo
    ) {
    }

    public record ProductoRequest(
            @Size(max = 20, message = "El código no puede superar 20 caracteres")
            String id,

            @NotBlank(message = "La categoría es obligatoria")
            String categoria,

            @NotBlank(message = "El nombre es obligatorio")
            @Size(max = 150)
            String nombre,

            @NotBlank(message = "La marca es obligatoria")
            @Size(max = 80)
            String marca,

            @Size(max = 80)
            String modelo,

            @NotNull(message = "El stock es obligatorio")
            @Min(value = 0, message = "El stock no puede ser negativo")
            Integer stock,

            @NotNull(message = "El precio es obligatorio")
            @Min(value = 1, message = "El precio debe ser mayor a 0")
            Integer precio,

            @Size(max = 500)
            String descripcion,

            @Size(max = 255)
            String imagen
    ) {
    }

    public record StockUpdateRequest(
            @NotNull(message = "El stock es obligatorio")
            @Min(value = 0, message = "El stock no puede ser negativo")
            Integer stock
    ) {
    }

    public record CategoriaResponse(
            Long id,
            String nombre,
            String descripcion
    ) {
    }

    public record CategoriaCreateRequest(
            @NotBlank(message = "El nombre de la categoría es obligatorio")
            @Size(max = 80)
            String nombre,
            @Size(max = 255)
            String descripcion
    ) {
    }
}
