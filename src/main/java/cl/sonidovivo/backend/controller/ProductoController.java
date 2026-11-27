package cl.sonidovivo.backend.controller;

import cl.sonidovivo.backend.dto.ProductoDtos.ProductoRequest;
import cl.sonidovivo.backend.dto.ProductoDtos.ProductoResponse;
import cl.sonidovivo.backend.dto.ProductoDtos.StockUpdateRequest;
import cl.sonidovivo.backend.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/productos")
@Tag(name = "Productos", description = "Catálogo público y administración de inventario")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    @Operation(summary = "Listar productos", description = "Filtra productos activos por categoría o término de búsqueda")
    public ResponseEntity<List<ProductoResponse>> listar(
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String q
    ) {
        return ResponseEntity.ok(productoService.listar(categoria, q));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener producto", description = "Busca el detalle de un producto por su código SKU")
    public ResponseEntity<ProductoResponse> obtenerPorId(@PathVariable String id) {
        return ResponseEntity.ok(productoService.obtenerPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Crear producto", description = "Agrega un nuevo instrumento o equipo al catálogo (solo Administrador)")
    public ResponseEntity<ProductoResponse> crear(@Valid @RequestBody ProductoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productoService.crear(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Actualizar producto", description = "Modifica todos los datos de un producto existente (solo Administrador)")
    public ResponseEntity<ProductoResponse> actualizar(
            @PathVariable String id,
            @Valid @RequestBody ProductoRequest request
    ) {
        return ResponseEntity.ok(productoService.actualizar(id, request));
    }

    @PatchMapping("/{id}/stock")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VENDEDOR')")
    @Operation(summary = "Actualizar stock", description = "Ajusta la cantidad disponible de un producto (Administrador o Vendedor)")
    public ResponseEntity<ProductoResponse> actualizarStock(
            @PathVariable String id,
            @Valid @RequestBody StockUpdateRequest request
    ) {
        return ResponseEntity.ok(productoService.actualizarStock(id, request.stock()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Eliminar producto", description = "Elimina o desactiva un producto del catálogo (solo Administrador)")
    public ResponseEntity<Void> eliminar(@PathVariable String id) {
        productoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/restablecer")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Restablecer catálogo inicial", description = "Repone los 28 instrumentos iniciales de Sonido Vivo (solo Administrador)")
    public ResponseEntity<List<ProductoResponse>> restablecer() {
        return ResponseEntity.ok(productoService.restablecerCatalogoInicial());
    }
}
