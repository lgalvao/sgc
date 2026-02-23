package sgc.processo.service;

import net.jqwik.api.*;
import sgc.comum.ComumRepo;
import sgc.organizacao.model.*;
import sgc.processo.dto.AtualizarProcessoRequest;
import sgc.processo.erros.ErroProcessoEmSituacaoInvalida;
import sgc.processo.model.Processo;
import sgc.processo.model.ProcessoRepo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.service.SubprocessoFacade;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@Tag("PBT")
class ProcessoWorkflowPbtTest {

    @Property
    void atualizar_deveFalharSeNaoEstiverCriado(@ForAll SituacaoProcesso situacao, 
                                               @ForAll TipoProcesso tipo,
                                               @ForAll String novaDescricao) {
        if (situacao == SituacaoProcesso.CRIADO) return;

        ProcessoRepo repo = mock(ProcessoRepo.class);
        sgc.organizacao.UnidadeFacade unidadeFacade = mock(sgc.organizacao.UnidadeFacade.class);
        ProcessoValidador validador = mock(ProcessoValidador.class);
        ProcessoConsultaService consultaService = mock(ProcessoConsultaService.class);

        ProcessoManutencaoService service = new ProcessoManutencaoService(repo, unidadeFacade, validador, consultaService);

        Processo p = Processo.builder()
                .situacao(situacao)
                .codigo(1L)
                .build();
        
        when(consultaService.buscarProcessoCodigo(1L)).thenReturn(p);

        AtualizarProcessoRequest req = AtualizarProcessoRequest.builder()
                .codigo(1L)
                .descricao(novaDescricao)
                .tipo(tipo)
                .dataLimiteEtapa1(LocalDateTime.now().plusDays(1))
                .unidades(List.of(100L))
                .build();

        assertThatThrownBy(() -> service.atualizar(1L, req))
                .isInstanceOf(ErroProcessoEmSituacaoInvalida.class);
    }

    @Property
    void iniciar_deveCriarSubprocessosParaTodosParticipantes(@ForAll("processoComParticipantes") ProcessoETunidades data) {
        Processo repoProcesso = data.processo;
        List<Unidade> unidades = data.unidades;

        ProcessoRepo processoRepo = mock(ProcessoRepo.class);
        ComumRepo comumRepo = mock(ComumRepo.class);
        UnidadeRepo unidadeRepo = mock(UnidadeRepo.class);
        UnidadeMapaRepo unidadeMapaRepo = mock(UnidadeMapaRepo.class);
        ProcessoNotificacaoService notificacaoService = mock(ProcessoNotificacaoService.class);
        SubprocessoFacade subprocessoFacade = mock(SubprocessoFacade.class);
        ProcessoValidador validador = mock(ProcessoValidador.class);

        ProcessoInicializador inicializador = new ProcessoInicializador(
                processoRepo, comumRepo, unidadeRepo, unidadeMapaRepo, 
                notificacaoService, subprocessoFacade, validador
        );

        when(comumRepo.buscar(Processo.class, repoProcesso.getCodigo())).thenReturn(repoProcesso);
        when(unidadeRepo.findAllById(any())).thenReturn(unidades);
        when(comumRepo.buscarPorSigla(Unidade.class, "ADMIN")).thenReturn(
                Unidade.builder().sigla("ADMIN").tipo(TipoUnidade.INTERMEDIARIA).situacao(SituacaoUnidade.ATIVA).build()
        );
        when(processoRepo.findUnidadeCodigosBySituacaoAndUnidadeCodigosIn(any(), any())).thenReturn(Collections.emptyList());

        Usuario usuario = new Usuario();

        inicializador.iniciar(repoProcesso.getCodigo(), Collections.emptyList(), usuario);

        // Verifica se a situação mudou
        assertThat(repoProcesso.getSituacao()).isEqualTo(SituacaoProcesso.EM_ANDAMENTO);
        
        // Verifica se subprocessoFacade foi chamado com as unidades
        if (repoProcesso.getTipo() == TipoProcesso.MAPEAMENTO) {
            verify(subprocessoFacade, atLeastOnce()).criarParaMapeamento(eq(repoProcesso), anyCollection(), any(), eq(usuario));
        } else if (repoProcesso.getTipo() == TipoProcesso.DIAGNOSTICO) {
            verify(subprocessoFacade, atLeastOnce()).criarParaDiagnostico(eq(repoProcesso), any(), any(), any(), eq(usuario));
        }
    }

    public static class ProcessoETunidades {
        Processo processo;
        List<Unidade> unidades;
    }

    @Provide
    Arbitrary<ProcessoETunidades> processoComParticipantes() {
        return Arbitraries.longs().between(1, 1000).flatMap(id -> 
            Arbitraries.of(TipoProcesso.MAPEAMENTO, TipoProcesso.DIAGNOSTICO).flatMap(tipo ->
                Arbitraries.longs().between(10000, 20000).list().uniqueElements().ofMinSize(1).ofMaxSize(10).map(uIds -> {
                    ProcessoETunidades data = new ProcessoETunidades();
                    Processo p = Processo.builder()
                            .codigo(id)
                            .tipo(tipo)
                            .situacao(SituacaoProcesso.CRIADO)
                            .build();
                    data.processo = p;
                    
                    data.unidades = new ArrayList<>();
                    Set<Unidade> setUnidades = new HashSet<>();
                    for (Long uId : uIds) {
                        Unidade u = Unidade.builder()
                                .codigo(uId)
                                .sigla("U" + uId)
                                .nome("Unidade " + uId)
                                .tipo(TipoUnidade.OPERACIONAL)
                                .situacao(SituacaoUnidade.ATIVA)
                                .build();
                        data.unidades.add(u);
                        setUnidades.add(u);
                    }
                    data.processo.adicionarParticipantes(setUnidades);
                    return data;
                })
            )
        );
    }
}
