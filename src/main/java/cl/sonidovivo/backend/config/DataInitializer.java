package cl.sonidovivo.backend.config;

import cl.sonidovivo.backend.model.DetallePedido;
import cl.sonidovivo.backend.model.EstadoPedido;
import cl.sonidovivo.backend.model.Pedido;
import cl.sonidovivo.backend.model.Producto;
import cl.sonidovivo.backend.model.Rol;
import cl.sonidovivo.backend.model.Usuario;
import cl.sonidovivo.backend.repository.PedidoRepository;
import cl.sonidovivo.backend.repository.ProductoRepository;
import cl.sonidovivo.backend.repository.UsuarioRepository;
import cl.sonidovivo.backend.service.ProductoService;
import java.time.LocalDateTime;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;
    private final PedidoRepository pedidoRepository;
    private final ProductoService productoService;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(
            UsuarioRepository usuarioRepository,
            ProductoRepository productoRepository,
            PedidoRepository pedidoRepository,
            ProductoService productoService,
            PasswordEncoder passwordEncoder
    ) {
        this.usuarioRepository = usuarioRepository;
        this.productoRepository = productoRepository;
        this.pedidoRepository = pedidoRepository;
        this.productoService = productoService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (productoRepository.count() == 0) {
            productoService.restablecerCatalogoInicial();
        }

        Usuario admin = crearUsuarioSiNoExiste(
                "Maximiliano Dinamarca",
                "maximiliano.dueno@sonidovivo.cl",
                "AdminDemo2026!",
                "19.876.543-0",
                "Viña del Mar",
                Rol.ADMINISTRADOR
        );

        crearUsuarioSiNoExiste(
                "Patricio Muñoz",
                "patricio.ventas@sonidovivo.cl",
                "VendedorDemo2026!",
                "18.765.432-1",
                "Viña del Mar",
                Rol.VENDEDOR
        );

        Usuario cliente = crearUsuarioSiNoExiste(
                "Cliente Particular",
                "cliente@gmail.com",
                "ClienteDemo2026!",
                "11.111.111-1",
                "Valparaíso",
                Rol.CLIENTE
        );

        if (pedidoRepository.count() == 0) {
            crearPedidosDemo(admin, cliente);
        }
    }

    private Usuario crearUsuarioSiNoExiste(
            String nombre, String email, String rawPass, String rut, String ciudad, Rol rol
    ) {
        return usuarioRepository.findByEmailIgnoreCase(email).orElseGet(() -> {
            Usuario u = new Usuario();
            u.setNombre(nombre);
            u.setEmail(email);
            u.setPasswordHash(passwordEncoder.encode(rawPass));
            u.setRut(rut);
            u.setCiudad(ciudad);
            u.setRol(rol);
            u.setActivo(true);
            return usuarioRepository.save(u);
        });
    }

    private void crearPedidosDemo(Usuario admin, Usuario cliente) {
        Producto ga001 = productoRepository.findById("GA001").orElse(null);
        Producto pe001 = productoRepository.findById("PE001").orElse(null);

        if (ga001 != null) {
            Pedido p1 = new Pedido();
            p1.setId("SV-1001");
            p1.setUsuario(cliente);
            p1.setFechaCreacion(LocalDateTime.now().minusDays(2));
            p1.setFechaTexto("25-11-2026");
            p1.setClienteNombre("Cliente Particular");
            p1.setRut("11.111.111-1");
            p1.setEmail("cliente@gmail.com");
            p1.setTelefono("+56 9 8765 4321");
            p1.setTipoEntrega("domicilio");
            p1.setDireccion("Av. Libertad 450, Viña del Mar");
            p1.setCiudad("Viña del Mar");
            p1.setComuna("Viña del Mar");
            p1.setMetodoPago("Webpay / Débito");
            p1.setEstado(EstadoPedido.EN_PREPARACION);
            p1.setSubtotal(ga001.getPrecio());
            p1.setCostoEnvio(4990);
            p1.setTotal(ga001.getPrecio() + 4990);

            DetallePedido d1 = new DetallePedido();
            d1.setProducto(ga001);
            d1.setNombreProducto("Guitarra Acústica Folk Yamaha F310");
            d1.setCantidad(1);
            d1.setPrecioUnitario(ga001.getPrecio());
            d1.setSubtotal(ga001.getPrecio());
            p1.addItem(d1);
            pedidoRepository.save(p1);
        }

        if (pe001 != null) {
            Pedido p2 = new Pedido();
            p2.setId("SV-1002");
            p2.setUsuario(admin);
            p2.setFechaCreacion(LocalDateTime.now().minusDays(3));
            p2.setFechaTexto("24-11-2026");
            p2.setClienteNombre("Patricio Muñoz");
            p2.setRut("18.765.432-1");
            p2.setEmail("patricio.ventas@sonidovivo.cl");
            p2.setTelefono("+56 9 9123 4567");
            p2.setTipoEntrega("retiro");
            p2.setDireccion("Retiro en Tienda Central Viña del Mar (Calle Valparaíso #580)");
            p2.setCiudad("Viña del Mar");
            p2.setComuna("Viña del Mar");
            p2.setMetodoPago("Transferencia Bancaria");
            p2.setEstado(EstadoPedido.DESPACHADO);
            int sub = pe001.getPrecio() * 2;
            p2.setSubtotal(sub);
            p2.setCostoEnvio(0);
            p2.setTotal(sub);

            DetallePedido d2 = new DetallePedido();
            d2.setProducto(pe001);
            d2.setNombreProducto("Pedal Distorsión Boss DS-1");
            d2.setCantidad(2);
            d2.setPrecioUnitario(pe001.getPrecio());
            d2.setSubtotal(sub);
            p2.addItem(d2);
            pedidoRepository.save(p2);
        }
    }
}
