package sesel.sgc.atribuicao;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import sesel.sgc.notificacao.NotificacaoService;
import sesel.sgc.unidade.Unidade;
import sesel.sgc.servidor.Servidor;
import sesel.sgc.processo.Processo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class AtribuicaoServiceTest {
    @MockitoBean
    private AtribuicaoRepository atribuicaoRepository;

    @MockitoBean
    private NotificacaoService notificacaoService;

    @MockitoSpyBean
    private AtribuicaoService atribuicaoService;

    @Test
    void deveBuscarAtribuicoesPorUnidade() {
        UUID unidadeId = UUID.randomUUID();
        Atribuicao atribuicao = new Atribuicao();
        when(atribuicaoRepository.findByUnidadeId(unidadeId)).thenReturn(List.of(atribuicao));

        List<Atribuicao> resultado = atribuicaoService.buscarAtribuicoesPorUnidade(unidadeId);
        assertEquals(1, resultado.size());

        verify(atribuicaoRepository).findByUnidadeId(unidadeId);
    }

    @Test
    void deveBuscarAtribuicoesPorServidor() {
        UUID servidorId = UUID.randomUUID();
        Atribuicao atribuicao = new Atribuicao();
        when(atribuicaoRepository.findByServidorId(servidorId)).thenReturn(List.of(atribuicao));
        List<Atribuicao> resultado = atribuicaoService.buscarAtribuicoesPorServidor(servidorId);
        assertEquals(1, resultado.size());
        verify(atribuicaoRepository).findByServidorId(servidorId);
    }

    @Test
    void deveRemoverAtribuicao() {
        UUID id = UUID.randomUUID();
        Atribuicao atribuicao = new Atribuicao();
        when(atribuicaoRepository.findById(id)).thenReturn(Optional.of(atribuicao));
        atribuicaoService.removerAtribuicao(id);
        verify(atribuicaoRepository).findById(id);
        verify(atribuicaoRepository).delete(atribuicao);
    }

    @Test
    void deveLancarExcecaoAoRemoverAtribuicaoNaoEncontrada() {
        UUID id = UUID.randomUUID();
        when(atribuicaoRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> atribuicaoService.removerAtribuicao(id));
        verify(atribuicaoRepository).findById(id);
    }

    @Test
    void deveCriarAtribuicaoComSucesso() {
        Unidade unidade = new Unidade();
        Servidor servidor = new Servidor();
        Processo processo = new Processo();
        java.time.LocalDate dataInicio = java.time.LocalDate.now();
        java.time.LocalDate dataTermino = java.time.LocalDate.now().plusDays(1);
        String justificativa = "Justificativa de teste";
        Atribuicao atribuicao = Atribuicao.builder()
                .unidade(unidade)
                .servidor(servidor)
                .processo(processo)
                .dataInicio(dataInicio)
                .dataTermino(dataTermino)
                .justificativa(justificativa)
                .build();
        when(atribuicaoRepository.save(org.mockito.ArgumentMatchers.any(Atribuicao.class))).thenReturn(atribuicao);
        Atribuicao resultado = atribuicaoService.criarAtribuicao(unidade, servidor, processo, dataInicio, dataTermino,
                justificativa);
        assertEquals(unidade, resultado.getUnidade());
        assertEquals(servidor, resultado.getServidor());
        assertEquals(processo, resultado.getProcesso());
        assertEquals(dataInicio, resultado.getDataInicio());
        assertEquals(dataTermino, resultado.getDataTermino());
        assertEquals(justificativa, resultado.getJustificativa());
        verify(notificacaoService).notificarAtribuicaoTemporaria(atribuicao);
    }
}