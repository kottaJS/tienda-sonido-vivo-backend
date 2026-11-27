package cl.sonidovivo.backend.repository;

import cl.sonidovivo.backend.model.Producto;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductoRepository extends JpaRepository<Producto, String> {

    List<Producto> findByActivoTrueOrderByIdAsc();

    @Query("""
            SELECT p FROM Producto p
            WHERE p.activo = true
              AND (:categoria IS NULL OR LOWER(p.categoria.nombre) = LOWER(:categoria))
              AND (:q IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(p.marca) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(p.modelo) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(p.id) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(p.descripcion) LIKE LOWER(CONCAT('%', :q, '%')))
            ORDER BY p.id ASC
            """)
    List<Producto> buscarActivos(@Param("categoria") String categoria, @Param("q") String q);
}
