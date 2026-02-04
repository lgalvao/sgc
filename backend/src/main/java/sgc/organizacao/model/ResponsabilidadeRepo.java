package sgc.organizacao.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResponsabilidadeRepo extends JpaRepository<Responsabilidade, Long> {
    
    Optional<Responsabilidade> findByUnidadeCodigo(Long unidadeCodigo);

    List<Responsabilidade> findByUnidadeCodigoIn(List<Long> unidadeCodigos);

    List<Responsabilidade> findByUsuarioTitulo(String usuarioTitulo);
}
