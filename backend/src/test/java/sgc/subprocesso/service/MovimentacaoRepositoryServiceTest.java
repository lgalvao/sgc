package sgc.subprocesso.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.subprocesso.model.Movimentacao;
import sgc.subprocesso.model.MovimentacaoRepo;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MovimentacaoRepositoryServiceTest {

    @Mock private MovimentacaoRepo repo;
    @InjectMocks private MovimentacaoRepositoryService service;

    @Test
    void delegateToRepo() {
        service.findBySubprocessoCodigoOrderByDataHoraDesc(1L);
        verify(repo).findBySubprocessoCodigoOrderByDataHoraDesc(1L);

        Movimentacao m = new Movimentacao();
        service.save(m);
        verify(repo).save(m);
    }
}
