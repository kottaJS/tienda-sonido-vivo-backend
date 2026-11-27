package cl.sonidovivo.backend.service;

import cl.sonidovivo.backend.dto.UsuarioDtos.UsuarioCreateRequest;
import cl.sonidovivo.backend.dto.UsuarioDtos.UsuarioResponse;
import cl.sonidovivo.backend.dto.UsuarioDtos.UsuarioUpdateRequest;
import cl.sonidovivo.backend.exception.BusinessRuleException;
import cl.sonidovivo.backend.exception.DuplicateResourceException;
import cl.sonidovivo.backend.exception.ResourceNotFoundException;
import cl.sonidovivo.backend.model.Rol;
import cl.sonidovivo.backend.model.Usuario;
import cl.sonidovivo.backend.repository.UsuarioRepository;
import cl.sonidovivo.backend.util.RutValidator;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UsuarioResponse> listarTodos() {
        return usuarioRepository.findAllByOrderByIdAsc()
                .stream()
                .map(UsuarioService::toResponse)
                .toList();
    }

    public UsuarioResponse obtenerPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));
        return toResponse(usuario);
    }

    @Transactional
    public UsuarioResponse crear(UsuarioCreateRequest request) {
        String email = request.email().trim().toLowerCase();
        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateResourceException("Ya existe un usuario con el correo " + email);
        }
        if (!RutValidator.isValid(request.rut())) {
            throw new BusinessRuleException("El RUT ingresado no es válido");
        }

        Rol rol = Rol.fromString(request.rol());
        String rawPassword = (request.password() != null && !request.password().isBlank())
                ? request.password()
                : "SonidoVivo2026!";

        Usuario usuario = new Usuario();
        usuario.setNombre(request.nombre().trim());
        usuario.setEmail(email);
        usuario.setPasswordHash(passwordEncoder.encode(rawPassword));
        usuario.setRut(request.rut().trim());
        usuario.setCiudad(request.ciudad().trim());
        usuario.setRol(rol);
        usuario.setActivo(true);

        return toResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponse actualizar(Long id, UsuarioUpdateRequest request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));

        if (!RutValidator.isValid(request.rut())) {
            throw new BusinessRuleException("El RUT ingresado no es válido");
        }

        usuario.setNombre(request.nombre().trim());
        usuario.setRut(request.rut().trim());
        usuario.setCiudad(request.ciudad().trim());
        usuario.setRol(Rol.fromString(request.rol()));
        if (request.activo() != null) {
            usuario.setActivo(request.activo());
        }

        return toResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponse cambiarRol(Long id, String nuevoRol) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));
        usuario.setRol(Rol.fromString(nuevoRol));
        return toResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public void eliminar(Long id, String emailAutenticado) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));
        if (usuario.getEmail().equalsIgnoreCase(emailAutenticado)) {
            throw new BusinessRuleException("No puedes eliminar tu propia cuenta de administrador activa");
        }
        usuarioRepository.delete(usuario);
    }

    public static UsuarioResponse toResponse(Usuario u) {
        return new UsuarioResponse(
                u.getId(),
                u.getNombre(),
                u.getEmail(),
                u.getRut(),
                u.getCiudad(),
                u.getRol().getEtiqueta(),
                u.isActivo()
        );
    }
}
