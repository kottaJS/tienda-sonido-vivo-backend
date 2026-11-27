package cl.sonidovivo.backend.controller;

import cl.sonidovivo.backend.dto.ProductoDtos.CategoriaCreateRequest;
import cl.sonidovivo.backend.dto.ProductoDtos.CategoriaResponse;
import cl.sonidovivo.backend.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categorias")
@Tag(name = "Categorías", description = "Consulta y registro de categorías del catálogo musical")
public class CategoriaController {

    private final ProductoService productoService;

    public CategoriaController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    @Operation(summary = "Listar categorías", description = "Retorna todas las categorías disponibles en la tienda")
    public ResponseEntity<List<CategoriaResponse>> listar() {
        return ResponseEntity.ok(productoService.listarCategorias());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Crear categoría", description = "Registra una nueva categoría (requiere rol Administrador)")
    public ResponseEntity<CategoriaResponse> crear(@Valid @RequestBody CategoriaCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productoService.crearCategoria(request));
    }
}
