package sgc.subprocesso.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.comum.repo.RepositorioComum;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;

import java.util.Collections;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SubprocessoRepositoryServiceTest {

    @Mock private SubprocessoRepo repo;
    @Mock private RepositorioComum repositorioComum;
    @InjectMocks private SubprocessoRepositoryService service;

    @Test
    void delegateToRepos() {
        service.buscar(1L);
        verify(repositorioComum).buscar(Subprocesso.class, 1L);

        service.findById(1L);
        verify(repo).findById(1L);

        service.findByMapaCodigo(1L);
        verify(repo).findByMapaCodigo(1L);

        service.findByProcessoCodigoAndUnidadeCodigo(1L, 2L);
        verify(repo).findByProcessoCodigoAndUnidadeCodigo(1L, 2L);

        service.findByProcessoCodigoWithUnidade(1L);
        verify(repo).findByProcessoCodigoWithUnidade(1L);

        service.findByProcessoCodigoAndSituacaoWithUnidade(1L, SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        verify(repo).findByProcessoCodigoAndSituacaoWithUnidade(1L, SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);

        service.findAllComFetch();
        verify(repo).findAllComFetch();

        service.existsByProcessoCodigoAndUnidadeCodigoIn(1L, Collections.emptyList());
        verify(repo).existsByProcessoCodigoAndUnidadeCodigoIn(1L, Collections.emptyList());

        service.findBySituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        verify(repo).findBySituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);

        Subprocesso s = new Subprocesso();
        service.save(s);
        verify(repo).save(s);

        service.deleteById(1L);
        verify(repo).deleteById(1L);
    }
}
