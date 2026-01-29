package sgc.processo.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.processo.model.Processo;
import sgc.processo.model.ProcessoRepo;
import sgc.processo.model.SituacaoProcesso;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProcessoRepositoryServiceTest {

    @Mock
    private ProcessoRepo processoRepo;

    @InjectMocks
    private ProcessoRepositoryService service;

    @Test
    @DisplayName("Deve buscar por ID retornando Optional")
    void deveBuscarPorIdOptional() {
        Long id = 1L;
        Processo processo = new Processo();
        when(processoRepo.findById(id)).thenReturn(Optional.of(processo));

        Optional<Processo> result = service.findById(id);

        assertTrue(result.isPresent());
        verify(processoRepo).findById(id);
    }

    @Test
    @DisplayName("Deve buscar por ID lançando erro se não encontrar")
    void deveBuscarPorIdComErro() {
        Long id = 1L;
        Processo processo = new Processo();
        when(processoRepo.findById(id)).thenReturn(Optional.of(processo));

        Processo result = service.buscarPorId(id);

        assertNotNull(result);

        when(processoRepo.findById(id)).thenReturn(Optional.empty());
        assertThrows(ErroEntidadeNaoEncontrada.class, () -> service.buscarPorId(id));
    }

    @Test
    @DisplayName("Deve salvar processo")
    void deveSalvar() {
        Processo processo = new Processo();
        when(processoRepo.save(processo)).thenReturn(processo);

        Processo result = service.salvar(processo);

        assertNotNull(result);
        verify(processoRepo).save(processo);
    }

    @Test
    @DisplayName("Deve excluir processo")
    void deveExcluir() {
        Long id = 1L;
        service.excluir(id);
        verify(processoRepo).deleteById(id);
    }

    @Test
    @DisplayName("Deve buscar por situação")
    void deveBuscarPorSituacao() {
        SituacaoProcesso situacao = SituacaoProcesso.EM_ANDAMENTO;
        when(processoRepo.findBySituacao(situacao)).thenReturn(Collections.emptyList());

        service.findBySituacao(situacao);
        verify(processoRepo).findBySituacao(situacao);
    }

    @Test
    @DisplayName("Deve listar por participantes")
    void deveListarPorParticipantes() {
        List<Long> codigos = List.of(1L);
        SituacaoProcesso situacao = SituacaoProcesso.FINALIZADO;
        Pageable pageable = PageRequest.of(0, 10);
        when(processoRepo.findDistinctByParticipantes_CodigoInAndSituacaoNot(codigos, situacao, pageable))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        Page<Processo> result = service.listarPorParticipantesIgnorandoSituacao(codigos, situacao, pageable);
        
        assertNotNull(result);
        verify(processoRepo).findDistinctByParticipantes_CodigoInAndSituacaoNot(codigos, situacao, pageable);
    }
}
