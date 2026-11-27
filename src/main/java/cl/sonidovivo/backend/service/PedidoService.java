package cl.sonidovivo.backend.service;

import cl.sonidovivo.backend.dto.PedidoDtos.DetallePedidoResponse;
import cl.sonidovivo.backend.dto.PedidoDtos.PedidoCreateRequest;
import cl.sonidovivo.backend.dto.PedidoDtos.PedidoItemRequest;
import cl.sonidovivo.backend.dto.PedidoDtos.PedidoResponse;
import cl.sonidovivo.backend.exception.BusinessRuleException;
import cl.sonidovivo.backend.exception.ResourceNotFoundException;
import cl.sonidovivo.backend.model.DetallePedido;
import cl.sonidovivo.backend.model.EstadoPedido;
import cl.sonidovivo.backend.model.Pedido;
import cl.sonidovivo.backend.model.Producto;
import cl.sonidovivo.backend.model.Usuario;
import cl.sonidovivo.backend.repository.PedidoRepository;
import cl.sonidovivo.backend.repository.ProductoRepository;
import cl.sonidovivo.backend.repository.UsuarioRepository;
import cl.sonidovivo.backend.util.RutValidator;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PedidoService {

    private static final int COSTO_DESPACHO_DOMICILIO = 4990;
    private static final String DIRECCION_RETIRO = "Retiro en Tienda Central Viña del Mar (Calle Valparaíso #580)";
    private static final DateTimeFormatter FECHA_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;

    public PedidoService(
            PedidoRepository pedidoRepository,
            ProductoRepository productoRepository,
            UsuarioRepository usuarioRepository
    ) {
        this.pedidoRepository = pedidoRepository;
        this.productoRepository = productoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<PedidoResponse> listarTodos() {
        return pedidoRepository.findAllByOrderByFechaCreacionDesc()
                .stream()
                .map(PedidoService::toResponse)
                .toList();
    }

    public List<PedidoResponse> listarMisPedidos(String emailAutenticado) {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(emailAutenticado)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        return pedidoRepository.findByUsuarioIdOrEmailIgnoreCaseOrderByFechaCreacionDesc(usuario.getId(), usuario.getEmail())
                .stream()
                .map(PedidoService::toResponse)
                .toList();
    }

    public PedidoResponse obtenerPorId(String id, String emailAutenticado, boolean esPersonal) {
        Pedido pedido = pedidoRepository.findById(id.trim().toUpperCase(Locale.ROOT))
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado: " + id));

        if (!esPersonal) {
            boolean esDuenoPorUsuario = pedido.getUsuario() != null
                    && pedido.getUsuario().getEmail().equalsIgnoreCase(emailAutenticado);
            boolean esDuenoPorEmail = pedido.getEmail().equalsIgnoreCase(emailAutenticado);
            if (!esDuenoPorUsuario && !esDuenoPorEmail) {
                throw new AccessDeniedException("No tienes permiso para consultar pedidos de otro cliente");
            }
        }
        return toResponse(pedido);
    }

    @Transactional
    public PedidoResponse crearPedido(PedidoCreateRequest request, String emailAutenticado) {
        if (!RutValidator.isValid(request.rut())) {
            throw new BusinessRuleException("El RUT ingresado no es válido (ej: 12.345.678-5)");
        }

        String tipoEntrega = request.tipoEntrega().trim().toLowerCase(Locale.ROOT);
        if (!"retiro".equals(tipoEntrega) && !"domicilio".equals(tipoEntrega)) {
            throw new BusinessRuleException("Tipo de entrega no válido. Use 'retiro' o 'domicilio'");
        }

        if ("domicilio".equals(tipoEntrega)) {
            if (request.direccion() == null || request.direccion().isBlank()
                    || request.ciudad() == null || request.ciudad().isBlank()) {
                throw new BusinessRuleException("La dirección y ciudad son obligatorias para despacho a domicilio");
            }
        }

        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(emailAutenticado)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario autenticado no encontrado"));

        Pedido pedido = new Pedido();
        pedido.setId(generarCodigoPedido());
        pedido.setUsuario(usuario);
        LocalDateTime ahora = LocalDateTime.now();
        pedido.setFechaCreacion(ahora);
        pedido.setFechaTexto(ahora.format(FECHA_FORMATTER));
        pedido.setClienteNombre((request.nombre().trim() + " " + request.apellido().trim()).trim());
        pedido.setRut(request.rut().trim());
        pedido.setEmail(request.email().trim().toLowerCase(Locale.ROOT));
        pedido.setTelefono(request.telefono().trim());
        pedido.setTipoEntrega(tipoEntrega);
        pedido.setDireccion("domicilio".equals(tipoEntrega)
                ? request.direccion().trim() + ", " + request.ciudad().trim()
                : DIRECCION_RETIRO);
        pedido.setCiudad(request.ciudad() != null ? request.ciudad().trim() : "Viña del Mar");
        pedido.setComuna(request.comuna() != null ? request.comuna().trim() : "Viña del Mar");
        pedido.setMetodoPago(request.metodoPago().trim());
        pedido.setNotas(request.notas() != null ? request.notas().trim() : "");
        pedido.setEstado(EstadoPedido.EN_PREPARACION);

        int subtotalGeneral = 0;

        for (PedidoItemRequest itemReq : request.items()) {
            String prodId = itemReq.productoId().trim().toUpperCase(Locale.ROOT);
            Producto producto = productoRepository.findById(prodId)
                    .filter(Producto::isActivo)
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado: " + prodId));

            if (producto.getStock() < itemReq.cantidad()) {
                throw new BusinessRuleException("Stock insuficiente para '" + producto.getNombre()
                        + "'. Disponible: " + producto.getStock() + ", solicitado: " + itemReq.cantidad());
            }

            producto.setStock(producto.getStock() - itemReq.cantidad());
            productoRepository.save(producto);

            int subtotalLinea = producto.getPrecio() * itemReq.cantidad();
            subtotalGeneral += subtotalLinea;

            DetallePedido detalle = new DetallePedido();
            detalle.setProducto(producto);
            String nombreCompleto = (producto.getNombre() + " " + producto.getMarca() + " "
                    + (producto.getModelo() != null ? producto.getModelo() : "")).trim();
            detalle.setNombreProducto(nombreCompleto);
            detalle.setCantidad(itemReq.cantidad());
            detalle.setPrecioUnitario(producto.getPrecio());
            detalle.setSubtotal(subtotalLinea);
            pedido.addItem(detalle);
        }

        int costoEnvio = "domicilio".equals(tipoEntrega) ? COSTO_DESPACHO_DOMICILIO : 0;
        pedido.setSubtotal(subtotalGeneral);
        pedido.setCostoEnvio(costoEnvio);
        pedido.setTotal(subtotalGeneral + costoEnvio);

        return toResponse(pedidoRepository.save(pedido));
    }

    @Transactional
    public PedidoResponse actualizarEstado(String id, String nuevoEstadoStr) {
        Pedido pedido = pedidoRepository.findById(id.trim().toUpperCase(Locale.ROOT))
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado: " + id));

        EstadoPedido estadoAnterior = pedido.getEstado();
        EstadoPedido estadoNuevo = EstadoPedido.fromString(nuevoEstadoStr);

        if (estadoAnterior != EstadoPedido.CANCELADO && estadoNuevo == EstadoPedido.CANCELADO) {
            for (DetallePedido item : pedido.getItems()) {
                Producto prod = item.getProducto();
                prod.setStock(prod.getStock() + item.getCantidad());
                productoRepository.save(prod);
            }
        } else if (estadoAnterior == EstadoPedido.CANCELADO && estadoNuevo != EstadoPedido.CANCELADO) {
            for (DetallePedido item : pedido.getItems()) {
                Producto prod = item.getProducto();
                if (prod.getStock() < item.getCantidad()) {
                    throw new BusinessRuleException("No hay stock suficiente para reactivar el pedido en producto " + prod.getId());
                }
                prod.setStock(prod.getStock() - item.getCantidad());
                productoRepository.save(prod);
            }
        }

        pedido.setEstado(estadoNuevo);
        return toResponse(pedidoRepository.save(pedido));
    }

    @Transactional
    public void eliminar(String id) {
        Pedido pedido = pedidoRepository.findById(id.trim().toUpperCase(Locale.ROOT))
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado: " + id));
        pedidoRepository.delete(pedido);
    }

    private String generarCodigoPedido() {
        String codigo;
        do {
            int numero = ThreadLocalRandom.current().nextInt(1000, 9999);
            codigo = "SV-" + numero;
        } while (pedidoRepository.existsById(codigo));
        return codigo;
    }

    public static PedidoResponse toResponse(Pedido p) {
        List<DetallePedidoResponse> itemsDto = p.getItems().stream()
                .map(d -> new DetallePedidoResponse(
                        d.getProducto().getId(),
                        d.getNombreProducto(),
                        d.getCantidad(),
                        d.getPrecioUnitario(),
                        d.getSubtotal()
                ))
                .toList();

        return new PedidoResponse(
                p.getId(),
                p.getFechaTexto(),
                p.getClienteNombre(),
                p.getRut(),
                p.getEmail(),
                p.getTelefono(),
                p.getTipoEntrega(),
                p.getDireccion(),
                p.getCiudad(),
                p.getComuna(),
                p.getMetodoPago(),
                p.getNotas(),
                p.getEstado().getEtiqueta(),
                p.getSubtotal(),
                p.getCostoEnvio(),
                p.getTotal(),
                p.getUsuario() != null ? p.getUsuario().getId() : null,
                itemsDto
        );
    }
}
