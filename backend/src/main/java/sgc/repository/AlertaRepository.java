package sgc.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sgc.model.Alerta;

@Repository
public interface AlertaRepository extends JpaRepository<Alerta, Long> {
    Page<Alerta> findByUsuarioDestino_Titulo(String usuarioTitulo, Pageable pageable);
    Page<Alerta> findByUnidadeDestino_Codigo(Long unidadeCodigo, Pageable pageable);
}