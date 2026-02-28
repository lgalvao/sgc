package sgc.processo.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.comum.model.ComumRepo;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.UnidadeRepo;
import sgc.organizacao.model.Usuario;
import sgc.processo.model.Processo;
import sgc.processo.model.ProcessoRepo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.organizacao.model.UnidadeMapaRepo;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProcessoInicializador - Cobertura")
class ProcessoInicializadorCoverageTest {

    @InjectMocks
    private ProcessoInicializador inicializador;

    @Mock
    private ComumRepo repo;

    @Mock
    private ProcessoRepo processoRepo;

    @Mock
    private UnidadeRepo unidadeRepo;

    @Mock
    private UnidadeMapaRepo unidadeMapaRepo;

    @Mock
    private ProcessoValidador processoValidador;

    @Mock
    private sgc.subprocesso.service.SubprocessoService subprocessoService;

    @Mock
    private sgc.processo.service.ProcessoNotificacaoService notificacaoService;

    @Test
    void iniciar_revisao_semUnidades() {
        Processo p = new Processo();
        p.setSituacao(SituacaoProcesso.CRIADO);
        p.setTipo(TipoProcesso.REVISAO);

        when(repo.buscar(Processo.class, 1L)).thenReturn(p);

        assertThrows(sgc.processo.erros.ErroUnidadesNaoDefinidas.class, () ->
            inicializador.iniciar(1L, List.of(), new Usuario())
        );
    }

    @Test
    void iniciar_naoRevisao_semParticipantes() {
        Processo p = new Processo();
        p.setSituacao(SituacaoProcesso.CRIADO);
        p.setTipo(TipoProcesso.MAPEAMENTO);

        when(repo.buscar(Processo.class, 1L)).thenReturn(p);

        assertThrows(sgc.processo.erros.ErroUnidadesNaoDefinidas.class, () ->
            inicializador.iniciar(1L, List.of(), new Usuario())
        );
    }

    @Test
    void iniciar_validarUnidadesComErro() {
        Processo p = new Processo();
        p.setSituacao(SituacaoProcesso.CRIADO);
        p.setTipo(TipoProcesso.MAPEAMENTO);
        Unidade u = new Unidade();
        u.setCodigo(2L);
        u.setSituacao(sgc.organizacao.model.SituacaoUnidade.ATIVA);
        p.adicionarParticipantes(java.util.Set.of(u));

        when(repo.buscar(Processo.class, 1L)).thenReturn(p);
        when(processoRepo.findUnidadeCodigosBySituacaoAndUnidadeCodigosIn(eq(SituacaoProcesso.EM_ANDAMENTO), any())).thenReturn(List.of(2L));
        lenient().when(processoValidador.getMensagemErroUnidadesSemMapa(any())).thenReturn(java.util.Optional.empty());
        when(unidadeRepo.findSiglasByCodigos(any())).thenReturn(List.of("UN1"));

        List<String> erros = inicializador.iniciar(1L, List.of(), new Usuario());
        assertEquals(1, erros.size());
    }

    @Test
    void iniciar_validarUnidadesComErroTipoRevisao() {
        Processo p = new Processo();
        p.setSituacao(SituacaoProcesso.CRIADO);
        p.setTipo(TipoProcesso.REVISAO);
        Unidade u = new Unidade();
        u.setCodigo(2L);
        u.setSituacao(sgc.organizacao.model.SituacaoUnidade.INATIVA);

        when(repo.buscar(Processo.class, 1L)).thenReturn(p);
        when(unidadeRepo.findAllById(any())).thenReturn(List.of(u));
        when(processoValidador.getMensagemErroUnidadesSemMapa(any())).thenReturn(java.util.Optional.of("Erro mapa"));

        List<String> erros = inicializador.iniciar(1L, List.of(2L), new Usuario());
        assertEquals(1, erros.size());
    }
}
