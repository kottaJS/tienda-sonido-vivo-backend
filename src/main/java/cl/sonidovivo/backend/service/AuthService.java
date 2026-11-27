package cl.sonidovivo.backend.service;

import cl.sonidovivo.backend.dto.AuthDtos.AuthResponse;
import cl.sonidovivo.backend.dto.AuthDtos.LoginRequest;
import cl.sonidovivo.backend.dto.AuthDtos.RegisterRequest;
import cl.sonidovivo.backend.dto.UsuarioDtos.UsuarioResponse;
import cl.sonidovivo.backend.exception.BusinessRuleException;
import cl.sonidovivo.backend.exception.DuplicateResourceException;
import cl.sonidovivo.backend.exception.ResourceNotFoundException;
import cl.sonidovivo.backend.model.Rol;
import cl.sonidovivo.backend.model.Usuario;
import cl.sonidovivo.backend.repository.UsuarioRepository;
import cl.sonidovivo.backend.security.JwtService;
import cl.sonidovivo.backend.util.RutValidator;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email().trim(), request.password())
        );

        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(request.email().trim())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        String token = jwtService.generateToken(usuario);
        return new AuthResponse(token, "Bearer", jwtService.getExpirationMs(), UsuarioService.toResponse(usuario));
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateResourceException("Ya existe una cuenta registrada con el correo " + email);
        }
        if (!RutValidator.isValid(request.rut())) {
            throw new BusinessRuleException("El RUT ingresado no es válido (ej: 12.345.678-5)");
        }

        Usuario nuevo = new Usuario();
        nuevo.setNombre(request.nombre().trim());
        nuevo.setEmail(email);
        nuevo.setPasswordHash(passwordEncoder.encode(request.password()));
        nuevo.setRut(request.rut().trim());
        nuevo.setCiudad(request.ciudad().trim());
        nuevo.setRol(Rol.CLIENTE);
        nuevo.setActivo(true);

        Usuario guardado = usuarioRepository.save(nuevo);
        String token = jwtService.generateToken(guardado);
        return new AuthResponse(token, "Bearer", jwtService.getExpirationMs(), UsuarioService.toResponse(guardado));
    }

    public UsuarioResponse getCurrentUser(String email) {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario autenticado no encontrado"));
        return UsuarioService.toResponse(usuario);
    }
}
