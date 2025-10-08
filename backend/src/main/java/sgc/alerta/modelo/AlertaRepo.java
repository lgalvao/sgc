package sgc.alerta.modelo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlertaRepo extends JpaRepository<Alerta, Long> {
    Page<Alerta> findByUsuarioDestino_Titulo(String usuarioTitulo, Pageable pageable);
    Page<Alerta> findByUnidadeDestino_Codigo(Long unidadeCodigo, Pageable pageable);
}