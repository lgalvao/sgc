package sgc.organizacao.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.organizacao.model.UnidadeMapaRepo;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UnidadeMapaRepositoryServiceTest {

    @Mock private UnidadeMapaRepo repo;
    @InjectMocks private UnidadeMapaRepositoryService service;

    @Test
    void findAllUnidadeCodigos() {
        service.findAllUnidadeCodigos();
        verify(repo).findAllUnidadeCodigos();
    }
}
