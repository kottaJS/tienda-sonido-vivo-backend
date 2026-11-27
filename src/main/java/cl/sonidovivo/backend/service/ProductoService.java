package cl.sonidovivo.backend.service;

import cl.sonidovivo.backend.dto.ProductoDtos.CategoriaCreateRequest;
import cl.sonidovivo.backend.dto.ProductoDtos.CategoriaResponse;
import cl.sonidovivo.backend.dto.ProductoDtos.ProductoRequest;
import cl.sonidovivo.backend.dto.ProductoDtos.ProductoResponse;
import cl.sonidovivo.backend.exception.DuplicateResourceException;
import cl.sonidovivo.backend.exception.ResourceNotFoundException;
import cl.sonidovivo.backend.model.Categoria;
import cl.sonidovivo.backend.model.Producto;
import cl.sonidovivo.backend.repository.CategoriaRepository;
import cl.sonidovivo.backend.repository.DetallePedidoRepository;
import cl.sonidovivo.backend.repository.ProductoRepository;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final DetallePedidoRepository detallePedidoRepository;

    public ProductoService(
            ProductoRepository productoRepository,
            CategoriaRepository categoriaRepository,
            DetallePedidoRepository detallePedidoRepository
    ) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
        this.detallePedidoRepository = detallePedidoRepository;
    }

    public List<ProductoResponse> listar(String categoria, String q) {
        String catParam = (categoria == null || categoria.isBlank() || "Todas".equalsIgnoreCase(categoria))
                ? null : categoria.trim();
        String qParam = (q == null || q.isBlank()) ? null : q.trim();
        return productoRepository.buscarActivos(catParam, qParam)
                .stream()
                .map(ProductoService::toResponse)
                .toList();
    }

    public ProductoResponse obtenerPorId(String id) {
        Producto producto = productoRepository.findById(id.trim().toUpperCase(Locale.ROOT))
                .filter(Producto::isActivo)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con código: " + id));
        return toResponse(producto);
    }

    public List<CategoriaResponse> listarCategorias() {
        return categoriaRepository.findAll()
                .stream()
                .map(c -> new CategoriaResponse(c.getId(), c.getNombre(), c.getDescripcion()))
                .toList();
    }

    @Transactional
    public CategoriaResponse crearCategoria(CategoriaCreateRequest request) {
        String nombre = request.nombre().trim();
        if (categoriaRepository.findByNombreIgnoreCase(nombre).isPresent()) {
            throw new DuplicateResourceException("La categoría '" + nombre + "' ya existe");
        }
        Categoria guardada = categoriaRepository.save(new Categoria(nombre, request.descripcion()));
        return new CategoriaResponse(guardada.getId(), guardada.getNombre(), guardada.getDescripcion());
    }

    @Transactional
    public ProductoResponse crear(ProductoRequest request) {
        String codigo = (request.id() != null && !request.id().isBlank())
                ? request.id().trim().toUpperCase(Locale.ROOT)
                : "PROD-" + String.valueOf(System.currentTimeMillis()).substring(9);

        if (productoRepository.findById(codigo).filter(Producto::isActivo).isPresent()) {
            throw new DuplicateResourceException("Ya existe un producto activo con el código " + codigo);
        }

        Categoria categoria = resolverCategoria(request.categoria());
        Producto producto = new Producto();
        producto.setId(codigo);
        producto.setCategoria(categoria);
        producto.setNombre(request.nombre().trim());
        producto.setMarca(request.marca().trim());
        producto.setModelo(request.modelo() != null ? request.modelo().trim() : "");
        producto.setStock(request.stock());
        producto.setPrecio(request.precio());
        producto.setDescripcion(request.descripcion() != null ? request.descripcion().trim() : "");
        producto.setImagen((request.imagen() != null && !request.imagen().isBlank())
                ? request.imagen().trim()
                : "/images/products/GA001.webp");
        producto.setActivo(true);

        return toResponse(productoRepository.save(producto));
    }

    @Transactional
    public ProductoResponse actualizar(String id, ProductoRequest request) {
        Producto producto = productoRepository.findById(id.trim().toUpperCase(Locale.ROOT))
                .filter(Producto::isActivo)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con código: " + id));

        Categoria categoria = resolverCategoria(request.categoria());
        producto.setCategoria(categoria);
        producto.setNombre(request.nombre().trim());
        producto.setMarca(request.marca().trim());
        producto.setModelo(request.modelo() != null ? request.modelo().trim() : "");
        producto.setStock(request.stock());
        producto.setPrecio(request.precio());
        producto.setDescripcion(request.descripcion() != null ? request.descripcion().trim() : "");
        if (request.imagen() != null && !request.imagen().isBlank()) {
            producto.setImagen(request.imagen().trim());
        }

        return toResponse(productoRepository.save(producto));
    }

    @Transactional
    public ProductoResponse actualizarStock(String id, Integer nuevoStock) {
        Producto producto = productoRepository.findById(id.trim().toUpperCase(Locale.ROOT))
                .filter(Producto::isActivo)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con código: " + id));
        producto.setStock(Math.max(0, nuevoStock));
        return toResponse(productoRepository.save(producto));
    }

    @Transactional
    public void eliminar(String id) {
        String codigo = id.trim().toUpperCase(Locale.ROOT);
        Producto producto = productoRepository.findById(codigo)
                .filter(Producto::isActivo)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con código: " + id));

        if (detallePedidoRepository.existsByProductoId(codigo)) {
            producto.setActivo(false);
            productoRepository.save(producto);
        } else {
            productoRepository.delete(producto);
        }
    }

    @Transactional
    public List<ProductoResponse> restablecerCatalogoInicial() {
        for (CatalogoSeedData.CategoriaSeed catSeed : CatalogoSeedData.categorias()) {
            categoriaRepository.findByNombreIgnoreCase(catSeed.nombre())
                    .orElseGet(() -> categoriaRepository.save(new Categoria(catSeed.nombre(), catSeed.descripcion())));
        }

        for (CatalogoSeedData.ProductoSeed seed : CatalogoSeedData.productos()) {
            Categoria cat = resolverCategoria(seed.categoria());
            Producto p = productoRepository.findById(seed.id()).orElseGet(Producto::new);
            p.setId(seed.id());
            p.setCategoria(cat);
            p.setNombre(seed.nombre());
            p.setMarca(seed.marca());
            p.setModelo(seed.modelo());
            p.setStock(seed.stock());
            p.setPrecio(seed.precio());
            p.setDescripcion(seed.descripcion());
            p.setImagen(seed.imagen());
            p.setActivo(true);
            productoRepository.save(p);
        }
        return listar(null, null);
    }

    private Categoria resolverCategoria(String nombreCategoria) {
        String nombre = nombreCategoria.trim();
        return categoriaRepository.findByNombreIgnoreCase(nombre)
                .orElseGet(() -> categoriaRepository.save(new Categoria(nombre, "Categoría " + nombre)));
    }

    public static ProductoResponse toResponse(Producto p) {
        return new ProductoResponse(
                p.getId(),
                p.getCategoria().getNombre(),
                p.getCategoria().getId(),
                p.getNombre(),
                p.getMarca(),
                p.getModelo(),
                p.getStock(),
                p.getPrecio(),
                p.getDescripcion(),
                p.getImagen(),
                p.isActivo()
        );
    }
}
