package cl.sonidovivo.backend.model;

import java.text.Normalizer;
import java.util.Locale;

public enum Rol {
    ADMINISTRADOR("Administrador"),
    VENDEDOR("Vendedor"),
    CLIENTE("Cliente");

    private final String etiqueta;

    Rol(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public String getAuthority() {
        return "ROLE_" + name();
    }

    public static Rol fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("El rol es obligatorio");
        }
        String normalized = Normalizer.normalize(value.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ROOT);
        for (Rol rol : values()) {
            if (rol.name().equals(normalized) || rol.etiqueta.toUpperCase(Locale.ROOT).equals(normalized)) {
                return rol;
            }
        }
        throw new IllegalArgumentException("Rol no válido: " + value + ". Valores permitidos: Administrador, Vendedor, Cliente");
    }
}
