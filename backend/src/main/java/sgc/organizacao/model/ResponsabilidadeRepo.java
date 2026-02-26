package sgc.organizacao.model;

import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.*;

import java.util.*;

@Repository
public interface ResponsabilidadeRepo extends JpaRepository<Responsabilidade, Long> {
    List<Responsabilidade> findByUnidadeCodigoIn(List<Long> unidadeCodigos);

    List<Responsabilidade> findByUsuarioTitulo(String usuarioTitulo);
}
