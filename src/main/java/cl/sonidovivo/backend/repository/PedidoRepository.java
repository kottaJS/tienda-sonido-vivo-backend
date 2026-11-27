package cl.sonidovivo.backend.repository;

import cl.sonidovivo.backend.model.Pedido;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PedidoRepository extends JpaRepository<Pedido, String> {
    List<Pedido> findAllByOrderByFechaCreacionDesc();
    List<Pedido> findByUsuarioIdOrderByFechaCreacionDesc(Long usuarioId);
    List<Pedido> findByUsuarioIdOrEmailIgnoreCaseOrderByFechaCreacionDesc(Long usuarioId, String email);
}
