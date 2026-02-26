package sgc.organizacao.model;

import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.*;

@Repository
public interface AdministradorRepo extends JpaRepository<Administrador, String> {
}
