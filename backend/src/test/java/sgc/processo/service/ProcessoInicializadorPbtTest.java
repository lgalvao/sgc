package sgc.processo.service;

import net.jqwik.api.*;
import sgc.comum.model.*;
import sgc.organizacao.model.*;
import sgc.processo.model.*;
import sgc.subprocesso.service.*;

import java.time.*;
import java.util.*;

import static org.mockito.Mockito.*;

@Tag("PBT")
class ProcessoInicializadorPbtTest {
    @Property
    void iniciar_criaSubprocessosParaCadaParticipante(@ForAll("processosEAgumentos") ProcessoArgs args,
                                                      @ForAll("usuarioQualquer") Usuario usuario) {

        ProcessoRepo processoRepo = mock(ProcessoRepo.class);
        ComumRepo repo = mock(ComumRepo.class);
        UnidadeRepo unidadeRepo = mock(UnidadeRepo.class);
        UnidadeMapaRepo unidadeMapaRepo = mock(UnidadeMapaRepo.class);
        ProcessoNotificacaoService notificacaoService = mock(ProcessoNotificacaoService.class);
        SubprocessoService subprocessoService = mock(SubprocessoService.class);
        ProcessoValidador processoValidador = mock(ProcessoValidador.class);

        ProcessoInicializador inicializador = new ProcessoInicializador(
                processoRepo, repo, unidadeRepo, unidadeMapaRepo, notificacaoService, subprocessoService, processoValidador
        );

        Processo processo = args.processo;
        List<Long> codsUnidadesParam = args.codsUnidadesParam;

        when(repo.buscar(eq(Processo.class), anyLong())).thenReturn(processo);
        when(unidadeRepo.findAllById(any())).thenAnswer(inv -> {
            List<Long> ids = inv.getArgument(0);
            return ids.stream().map(id -> {
                Unidade u = new Unidade();
                u.setCodigo(id);
                u.setSigla("U" + id);
                return u;
            }).toList();
        });

        when(repo.buscar(eq(Unidade.class), anyLong())).thenAnswer(inv -> {
            Unidade u = new Unidade();
            u.setCodigo(inv.getArgument(1));
            u.setSigla("U" + u.getCodigo());
            return u;
        });
        when(repo.buscarPorSigla(Unidade.class, "ADMIN")).thenReturn(new Unidade());
        when(processoValidador.getMensagemErroUnidadesSemMapa(any())).thenReturn(Optional.empty());
        when(processoRepo.findUnidadeCodigosBySituacaoAndUnidadeCodigosIn(any(), any())).thenReturn(List.of());


        inicializador.iniciar(processo.getCodigo(), codsUnidadesParam, usuario);


        if (processo.getTipo() == TipoProcesso.MAPEAMENTO) {
            verify(subprocessoService, times(1)).criarParaMapeamento(eq(processo), any(), any(), eq(usuario));
        } else if (processo.getTipo() == TipoProcesso.REVISAO) {
            verify(subprocessoService, times(codsUnidadesParam.size())).criarParaRevisao(eq(processo), any(), any(), any(), eq(usuario));
        } else if (processo.getTipo() == TipoProcesso.DIAGNOSTICO) {
            verify(subprocessoService, times(processo.getParticipantes().size())).criarParaDiagnostico(eq(processo), any(), any(), any(), eq(usuario));
        }
    }

    @Provide
    Arbitrary<ProcessoArgs> processosEAgumentos() {
        return Arbitraries.of(TipoProcesso.values()).flatMap(tipo ->
                Arbitraries.longs().between(1, 100).flatMap(codigo ->
                        Arbitraries.longs().between(100, 200).set().ofMinSize(1).ofMaxSize(5).map(unidadesIds -> {
                            Processo p = Processo.builder()
                                    .codigo(codigo)
                                    .descricao("Processo " + codigo)
                                    .tipo(tipo)
                                    .situacao(SituacaoProcesso.CRIADO)
                                    .dataLimite(LocalDateTime.now().plusDays(10))
                                    .participantes(new ArrayList<>())
                                    .build();

                            List<Long> params = new ArrayList<>();
                            if (tipo != TipoProcesso.REVISAO) {
                                Set<Unidade> unidades = new HashSet<>();
                                for (Long id : unidadesIds) {
                                    Unidade u = new Unidade();
                                    u.setCodigo(id);
                                    u.setSigla("U" + id);
                                    u.setTipo(TipoUnidade.OPERACIONAL);
                                    u.setSituacao(SituacaoUnidade.ATIVA);
                                    unidades.add(u);
                                }
                                p.adicionarParticipantes(unidades);
                            } else {
                                params.addAll(unidadesIds);
                            }
                            return new ProcessoArgs(p, params);
                        })
                )
        );
    }

    @Provide
    Arbitrary<Usuario> usuarioQualquer() {
        return Arbitraries.strings().alpha().ofMinLength(5).map(nome -> {
            Usuario u = new Usuario();
            u.setNome(nome);
            u.setTituloEleitoral("12345");
            return u;
        });
    }

    static class ProcessoArgs {
        final Processo processo;
        final List<Long> codsUnidadesParam;

        ProcessoArgs(Processo p, List<Long> params) {
            this.processo = p;
            this.codsUnidadesParam = params;
        }
    }
}
