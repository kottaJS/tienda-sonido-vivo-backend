package cl.sonidovivo.backend.model;

import java.text.Normalizer;
import java.util.Locale;

public enum EstadoPedido {
    EN_PREPARACION("En preparación"),
    DESPACHADO("Despachado"),
    ENTREGADO("Entregado"),
    CANCELADO("Cancelado");

    private final String etiqueta;

    EstadoPedido(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public static EstadoPedido fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("El estado del pedido es obligatorio");
        }
        String normalized = Normalizer.normalize(value.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ROOT)
                .replace(" ", "_");
        for (EstadoPedido estado : values()) {
            String etqNorm = Normalizer.normalize(estado.etiqueta, Normalizer.Form.NFD)
                    .replaceAll("\\p{M}", "")
                    .toUpperCase(Locale.ROOT)
                    .replace(" ", "_");
            if (estado.name().equals(normalized) || etqNorm.equals(normalized)) {
                return estado;
            }
        }
        throw new IllegalArgumentException("Estado de pedido no válido: " + value);
    }
}
