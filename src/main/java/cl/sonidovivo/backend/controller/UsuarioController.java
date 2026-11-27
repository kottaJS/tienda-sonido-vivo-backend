package cl.sonidovivo.backend.controller;

import cl.sonidovivo.backend.dto.UsuarioDtos.UsuarioCreateRequest;
import cl.sonidovivo.backend.dto.UsuarioDtos.UsuarioResponse;
import cl.sonidovivo.backend.dto.UsuarioDtos.UsuarioRolRequest;
import cl.sonidovivo.backend.dto.UsuarioDtos.UsuarioUpdateRequest;
import cl.sonidovivo.backend.service.UsuarioService;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios")
@PreAuthorize("hasRole('ADMINISTRADOR')")
@Tag(name = "Usuarios", description = "Administración de cuentas y roles de la tienda (solo Administrador)")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    @Operation(summary = "Listar usuarios", description = "Obtiene todas las cuentas registradas")
    public ResponseEntity<List<UsuarioResponse>> listar() {
        return ResponseEntity.ok(usuarioService.listarTodos());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener usuario", description = "Consulta una cuenta por su ID")
    public ResponseEntity<UsuarioResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.obtenerPorId(id));
    }

    @PostMapping
    @Operation(summary = "Crear usuario", description = "Registra un nuevo usuario con rol asignado")
    public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody UsuarioCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.crear(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar usuario", description = "Edita los datos y estado de una cuenta existente")
    public ResponseEntity<UsuarioResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioUpdateRequest request
    ) {
        return ResponseEntity.ok(usuarioService.actualizar(id, request));
    }

    @PatchMapping("/{id}/rol")
    @Operation(summary = "Cambiar rol", description = "Actualiza el rol de acceso de un usuario")
    public ResponseEntity<UsuarioResponse> cambiarRol(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioRolRequest request
    ) {
        return ResponseEntity.ok(usuarioService.cambiarRol(id, request.rol()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar usuario", description = "Elimina una cuenta evitando borrar la del administrador en sesión")
    public ResponseEntity<Void> eliminar(@PathVariable Long id, Authentication authentication) {
        usuarioService.eliminar(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
