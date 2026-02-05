package sgc.organizacao.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResponsabilidadeRepo extends JpaRepository<Responsabilidade, Long> {
    List<Responsabilidade> findByUnidadeCodigoIn(List<Long> unidadeCodigos);

    List<Responsabilidade> findByUsuarioTitulo(String usuarioTitulo);
}
