package cl.sonidovivo.backend.controller;

import cl.sonidovivo.backend.dto.PedidoDtos.EstadoPedidoRequest;
import cl.sonidovivo.backend.dto.PedidoDtos.PedidoCreateRequest;
import cl.sonidovivo.backend.dto.PedidoDtos.PedidoResponse;
import cl.sonidovivo.backend.service.PedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pedidos")
@Tag(name = "Pedidos", description = "Checkout, historial de compras y gestión de despachos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VENDEDOR')")
    @Operation(summary = "Listar todos los pedidos", description = "Obtiene todos los pedidos de la tienda (Administrador o Vendedor)")
    public ResponseEntity<List<PedidoResponse>> listarTodos() {
        return ResponseEntity.ok(pedidoService.listarTodos());
    }

    @GetMapping("/mis-pedidos")
    @Operation(summary = "Listar mis pedidos", description = "Retorna las compras realizadas por el usuario autenticado")
    public ResponseEntity<List<PedidoResponse>> listarMisPedidos(Authentication authentication) {
        return ResponseEntity.ok(pedidoService.listarMisPedidos(authentication.getName()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener pedido por código", description = "Consulta el detalle de un pedido si pertenece al usuario o si es personal autorizado")
    public ResponseEntity<PedidoResponse> obtenerPorId(@PathVariable String id, Authentication authentication) {
        boolean esPersonal = authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMINISTRADOR".equals(a.getAuthority()) || "ROLE_VENDEDOR".equals(a.getAuthority()));
        return ResponseEntity.ok(pedidoService.obtenerPorId(id, authentication.getName(), esPersonal));
    }

    @PostMapping
    @Operation(summary = "Crear pedido", description = "Registra un nuevo pedido, valida RUT y descuenta stock en forma transaccional")
    public ResponseEntity<PedidoResponse> crear(
            @Valid @RequestBody PedidoCreateRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(pedidoService.crearPedido(request, authentication.getName()));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VENDEDOR')")
    @Operation(summary = "Cambiar estado de pedido", description = "Actualiza el estado del pedido y ajusta inventario si se cancela o reactiva")
    public ResponseEntity<PedidoResponse> actualizarEstado(
            @PathVariable String id,
            @Valid @RequestBody EstadoPedidoRequest request
    ) {
        return ResponseEntity.ok(pedidoService.actualizarEstado(id, request.estado()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Eliminar pedido", description = "Elimina un registro de pedido (solo Administrador)")
    public ResponseEntity<Void> eliminar(@PathVariable String id) {
        pedidoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
