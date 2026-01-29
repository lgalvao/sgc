package sgc.organizacao.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.UnidadeRepo;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UnidadeRepositoryServiceTest {

    @Mock private UnidadeRepo repo;
    @InjectMocks private UnidadeRepositoryService service;

    @Test
    void methodsDelegateToRepo() {
        service.findById(1L);
        verify(repo).findById(1L);

        service.findBySigla("SIG");
        verify(repo).findBySigla("SIG");
        
        service.findAllById(Collections.emptyList());
        verify(repo).findAllById(Collections.emptyList());

        service.findAllWithHierarquia();
        verify(repo).findAllWithHierarquia();

        service.findSiglasByCodigos(Collections.emptyList());
        verify(repo).findSiglasByCodigos(Collections.emptyList());

        Unidade u = new Unidade();
        service.salvar(u);
        verify(repo).save(u);
    }

    @Test
    void buscarPorIdThrowsIfNotFound() {
        when(repo.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ErroEntidadeNaoEncontrada.class, () -> service.buscarPorId(1L));
    }
}
