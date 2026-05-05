package sgc.subprocesso.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import sgc.comum.erros.*;
import sgc.comum.model.*;
import sgc.mapa.dto.*;
import sgc.mapa.model.*;
import sgc.mapa.service.*;
import sgc.organizacao.*;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;
import sgc.processo.model.*;
import sgc.seguranca.*;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.model.*;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static sgc.subprocesso.model.SituacaoSubprocesso.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubprocessoService")
@SuppressWarnings("NullAway.Init")
class SubprocessoServiceTest {

    @Mock
    private ComumRepo repo;
    @Mock
    private AnaliseRepo analiseRepo;
    @Mock
    private UnidadeService unidadeService;
    @Mock
    private UsuarioFacade usuarioFacade;
    @Mock
    private ImpactoMapaService impactoMapaService;
    @Mock
    private MapaSalvamentoService mapaSalvamentoService;
    @Mock
    private SgcPermissionEvaluator permissionEvaluator;
    @Mock
    private MapaVisualizacaoService mapaVisualizacaoService;
    @Mock
    private SubprocessoValidacaoService validacaoService;
    @Mock
    private SubprocessoRepo subprocessoRepo;
    @Mock
    private MovimentacaoRepo movimentacaoRepo;
    @Mock
    private CopiaMapaService copiaMapaService;
    @Mock
    private MapaManutencaoService mapaManutencaoService;
    @Mock
    private HierarquiaService hierarquiaService;
    @Mock
    private SubprocessoConsultaService consultaService;

    @InjectMocks
    private SubprocessoService service;


    @Test
    @DisplayName("criarEntidade deve persistir subprocesso e mapa associado")
    void criarEntidadeDevePersistirSubprocessoEMapaAssociado() {
        Processo processo = new Processo();
        processo.setCodigo(10L);

        Unidade unidade = new Unidade();
        unidade.setCodigo(20L);

        LocalDateTime dataLimiteEtapa1 = LocalDateTime.of(2026, 4, 30, 12, 0);
        CriarSubprocessoRequest request = CriarSubprocessoRequest.builder()
                .codProcesso(10L)
                .codUnidade(20L)
                .dataLimiteEtapa1(dataLimiteEtapa1)
                .build();

        Subprocesso subprocessoPrimeiroSave = new Subprocesso();
        subprocessoPrimeiroSave.setCodigo(100L);
        subprocessoPrimeiroSave.setProcesso(processo);
        subprocessoPrimeiroSave.setUnidade(unidade);
        subprocessoPrimeiroSave.setDataLimiteEtapa1(dataLimiteEtapa1);

        Mapa mapaSalvo = new Mapa();
        mapaSalvo.setCodigo(200L);
        mapaSalvo.setSubprocesso(subprocessoPrimeiroSave);

        when(repo.buscar(Processo.class, 10L)).thenReturn(processo);
        when(unidadeService.buscarPorCodigo(20L)).thenReturn(unidade);
        when(subprocessoRepo.save(any(Subprocesso.class)))
                .thenReturn(subprocessoPrimeiroSave)
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(mapaManutencaoService.salvarMapa(any(Mapa.class))).thenReturn(mapaSalvo);

        Subprocesso resultado = service.criarEntidade(request);

        verify(subprocessoRepo, times(2)).save(any(Subprocesso.class));
        verify(mapaManutencaoService).salvarMapa(argThat(mapa -> mapa.getSubprocesso().equals(subprocessoPrimeiroSave)));
        assertThat(resultado.getProcesso()).isEqualTo(processo);
        assertThat(resultado.getUnidade()).isEqualTo(unidade);
        assertThat(resultado.getDataLimiteEtapa1()).isEqualTo(dataLimiteEtapa1);
        assertThat(resultado.getMapa()).isEqualTo(mapaSalvo);
    }

    @Test
    @DisplayName("criarParaRevisao deve lançar exceção se unidadeMapa não tem mapa vigente")
    void criarParaRevisaoDeveLancarExcecaoSeUnidadeMapaNaoTemMapaVigente() {
        Processo processo = new Processo();
        Unidade unidade = new Unidade();
        unidade.setSigla("UNIT");
        UnidadeMapa unidadeMapa = new UnidadeMapa();
        unidadeMapa.setMapaVigente(null);
        Unidade unidadeOrigem = new Unidade();
        assertThatThrownBy(() -> service.criarParaRevisao(
                new SubprocessoService.CriarSubprocessoComMapaCommand(processo, unidade, unidadeMapa, unidadeOrigem)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Unidade UNIT sem mapa vigente para revisão/diagnóstico");
    }

    @Test
    @DisplayName("criarParaDiagnostico deve lançar exceção se unidadeMapa não tem mapa vigente")
    void criarParaDiagnosticoDeveLancarExcecaoSeUnidadeMapaNaoTemMapaVigente() {
        Processo processo = new Processo();
        Unidade unidade = new Unidade();
        unidade.setSigla("UNIT");
        UnidadeMapa unidadeMapa = new UnidadeMapa();
        unidadeMapa.setMapaVigente(null);
        Unidade unidadeOrigem = new Unidade();
        assertThatThrownBy(() -> service.criarParaDiagnostico(
                new SubprocessoService.CriarSubprocessoComMapaCommand(processo, unidade, unidadeMapa, unidadeOrigem)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Unidade UNIT sem mapa vigente para revisão/diagnóstico");
    }

    @ParameterizedTest
    @CsvSource({
            "MAPEAMENTO_MAPA_CRIADO, MAPEAMENTO_CADASTRO_HOMOLOGADO",
            "REVISAO_MAPA_AJUSTADO, REVISAO_CADASTRO_HOMOLOGADA"
    })
    @DisplayName("removerCompetencia deve atualizar situação quando mapa fica vazio")
    void removerCompetenciaDeveAtualizarSituacaoQuandoMapaFicaVazio(SituacaoSubprocesso situacaoInicial, SituacaoSubprocesso situacaoFinal) {
        Long codSubprocesso = 1L;
        Long codCompetencia = 10L;
        Long codMapa = 100L;

        Subprocesso sp = new Subprocesso();
        sp.setCodigo(codSubprocesso);
        sp.setSituacao(situacaoInicial);
        Mapa mapa = new Mapa();
        mapa.setCodigo(codMapa);
        sp.setMapa(mapa);

        when(consultaService.buscarSubprocesso(codSubprocesso)).thenReturn(sp);
        when(mapaManutencaoService.competenciasCodMapa(codMapa)).thenReturn(List.of());
        when(mapaManutencaoService.mapaCodigo(codMapa)).thenReturn(mapa);

        service.removerCompetencia(codSubprocesso, codCompetencia);

        assertThat(sp.getSituacao()).isEqualTo(situacaoFinal);
        verify(subprocessoRepo).save(sp);
    }

    @ParameterizedTest
    @CsvSource({
            "MAPEAMENTO_CADASTRO_HOMOLOGADO, MAPEAMENTO_MAPA_CRIADO",
            "REVISAO_CADASTRO_HOMOLOGADA, REVISAO_MAPA_AJUSTADO"
    })
    @DisplayName("adicionarCompetencia deve atualizar situação quando mapa deixa de ser vazio")
    void adicionarCompetenciaDeveAtualizarSituacaoQuandoMapaDeixaDeSerVazio(SituacaoSubprocesso situacaoInicial, SituacaoSubprocesso situacaoFinal) {
        Long codSubprocesso = 1L;
        Long codMapa = 100L;
        CriarCompetenciaRequest request = CriarCompetenciaRequest.builder()
                .descricao("Nova Comp")
                .atividadesCodigos(List.of(10L))
                .build();

        Subprocesso sp = new Subprocesso();
        sp.setCodigo(codSubprocesso);
        sp.setSituacao(situacaoInicial);
        Mapa mapa = new Mapa();
        mapa.setCodigo(codMapa);
        sp.setMapa(mapa);

        when(consultaService.buscarSubprocesso(codSubprocesso)).thenReturn(sp);
        when(mapaManutencaoService.competenciasCodMapa(codMapa)).thenReturn(List.of());

        service.adicionarCompetencia(codSubprocesso, request);

        assertThat(sp.getSituacao()).isEqualTo(situacaoFinal);
        verify(subprocessoRepo).save(sp);
    }

    @ParameterizedTest
    @CsvSource({
            "MAPEAMENTO_CADASTRO_HOMOLOGADO, MAPEAMENTO_MAPA_CRIADO",
            "REVISAO_CADASTRO_HOMOLOGADA, REVISAO_MAPA_AJUSTADO"
    })
    @DisplayName("salvarMapaSubprocesso deve atualizar situação quando mapa deixa de ser vazio")
    void salvarMapaSubprocessoDeveAtualizarSituacaoQuandoMapaDeixaDeSerVazio(SituacaoSubprocesso situacaoInicial, SituacaoSubprocesso situacaoFinal) {
        Long codSubprocesso = 1L;
        Long codMapa = 100L;

        SalvarMapaRequest request = SalvarMapaRequest.builder()
                .competencias(List.of(SalvarMapaRequest.CompetenciaRequest.builder()
                        .descricao("Comp")
                        .atividadesCodigos(List.of(10L))
                        .build()))
                .build();

        Subprocesso sp = new Subprocesso();
        sp.setCodigo(codSubprocesso);
        sp.setSituacao(situacaoInicial);
        Mapa mapa = new Mapa();
        mapa.setCodigo(codMapa);
        sp.setMapa(mapa);

        when(consultaService.buscarSubprocesso(codSubprocesso)).thenReturn(sp);
        when(mapaManutencaoService.competenciasCodMapa(codMapa)).thenReturn(List.of());
        when(mapaSalvamentoService.salvarMapaCompleto(eq(codMapa), any())).thenReturn(mapa);

        service.salvarMapaSubprocesso(codSubprocesso, request);

        assertThat(sp.getSituacao()).isEqualTo(situacaoFinal);
        verify(subprocessoRepo).save(sp);
    }

    @Test
    @DisplayName("atualizarEntidade - deve usar comandos vazios quando nulos")
    void atualizarEntidade_ComandosNulos() {
        Long cod = 1L;
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(cod);
        when(consultaService.buscarSubprocesso(cod)).thenReturn(sp);
        when(subprocessoRepo.save(any())).thenReturn(sp);

        AtualizarSubprocessoCommand command = AtualizarSubprocessoCommand.builder()
                .vinculos(null)
                .prazos(null)
                .build();

        service.atualizarEntidade(cod, command);

        verify(subprocessoRepo).save(sp);
    }

    @Nested
    @DisplayName("Cobertura Extra")
    class CoberturaExtra {
        private Subprocesso criarSubprocessoComMapa(Long codigo) {
            return criarSubprocessoComMapa(codigo, TipoProcesso.MAPEAMENTO);
        }

        private Subprocesso criarSubprocessoComMapa(Long codigo, TipoProcesso tipo) {
            Subprocesso sp = new Subprocesso();
            sp.setCodigo(codigo);
            sp.setMapa(new Mapa());
            sp.setSituacaoForcada(NAO_INICIADO);
            sp.setProcesso(Processo.builder().tipo(tipo).situacao(SituacaoProcesso.EM_ANDAMENTO).build());
            return sp;
        }

        private Usuario criarUsuarioMock() {
            Usuario user = new Usuario();
            user.setTituloEleitoral("12345678");
            user.setNome("Usuario Teste");
            return user;
        }

        @Nested
        @DisplayName("Manutenção de Mapa")
        class ManutencaoMapa {
            @Test
            @DisplayName("deve salvar mapa de subprocesso mudando situacao se estava vazio e tem novas competencias")
            void salvarMapaSubprocesso_EraVazioTemNovasCompetencias() {
                Subprocesso sp = criarSubprocessoComMapa(1L);
                sp.setSituacaoForcada(MAPEAMENTO_CADASTRO_HOMOLOGADO);
                sp.getMapa().setCodigo(100L);

                when(consultaService.buscarSubprocesso(1L)).thenReturn(sp);
                when(mapaManutencaoService.competenciasCodMapa(100L)).thenReturn(List.of());
                when(mapaSalvamentoService.salvarMapaCompleto(eq(100L), any())).thenReturn(sp.getMapa());

                SalvarMapaRequest request = new SalvarMapaRequest("Desc", List.of(new SalvarMapaRequest.CompetenciaRequest(0L, "Comp", List.of())));
                service.salvarMapaSubprocesso(1L, request);

                verify(mapaManutencaoService).reconciliarSituacaoSubprocesso(sp);
                assertThat(sp.getSituacao()).isEqualTo(MAPEAMENTO_MAPA_CRIADO);
            }

            @Test
            @DisplayName("deve adicionar competencia e atualizar situacao se era vazio em REVISAO")
            void adicionarCompetencia_EraVazioEmRevisao() {
                Subprocesso sp = criarSubprocessoComMapa(1L, TipoProcesso.REVISAO);
                sp.setSituacaoForcada(REVISAO_CADASTRO_HOMOLOGADA);
                sp.getMapa().setCodigo(100L);

                when(consultaService.buscarSubprocesso(1L)).thenReturn(sp);
                when(mapaManutencaoService.competenciasCodMapa(100L)).thenReturn(List.of());

                service.adicionarCompetencia(1L, new CriarCompetenciaRequest("Desc", List.of(10L)));

                assertThat(sp.getSituacao()).isEqualTo(REVISAO_MAPA_AJUSTADO);
            }

            @Test
            @DisplayName("deve remover competencia e atualizar situacao se ficou vazio em MAPEAMENTO")
            void removerCompetencia_FicouVazioEmMapeamento() {
                Subprocesso sp = criarSubprocessoComMapa(1L);
                sp.setSituacaoForcada(MAPEAMENTO_MAPA_CRIADO);
                sp.getMapa().setCodigo(100L);

                when(consultaService.buscarSubprocesso(1L)).thenReturn(sp);
                when(mapaManutencaoService.competenciasCodMapa(100L)).thenReturn(List.of());
                when(mapaManutencaoService.mapaCodigo(100L)).thenReturn(sp.getMapa());

                service.removerCompetencia(1L, 10L);

                assertThat(sp.getSituacao()).isEqualTo(MAPEAMENTO_CADASTRO_HOMOLOGADO);
            }

            @Test
            @DisplayName("deve remover competencia e atualizar situacao se ficou vazio em REVISAO")
            void removerCompetencia_FicouVazioEmRevisao() {
                Subprocesso sp = criarSubprocessoComMapa(1L, TipoProcesso.REVISAO);
                sp.setSituacaoForcada(REVISAO_MAPA_AJUSTADO);
                sp.getMapa().setCodigo(100L);

                when(consultaService.buscarSubprocesso(1L)).thenReturn(sp);
                when(mapaManutencaoService.competenciasCodMapa(100L)).thenReturn(List.of());
                when(mapaManutencaoService.mapaCodigo(100L)).thenReturn(sp.getMapa());

                service.removerCompetencia(1L, 10L);

                assertThat(sp.getSituacao()).isEqualTo(REVISAO_CADASTRO_HOMOLOGADA);
            }

            @Test
            @DisplayName("deve salvar mapa de subprocesso sem mudar situacao se era vazio mas nao tem novas")
            void salvarMapaSubprocesso_EraVazioSemNovas() {
                Subprocesso sp = criarSubprocessoComMapa(1L);
                sp.setSituacaoForcada(MAPEAMENTO_CADASTRO_HOMOLOGADO);
                sp.getMapa().setCodigo(100L);

                when(consultaService.buscarSubprocesso(1L)).thenReturn(sp);
                when(mapaManutencaoService.competenciasCodMapa(100L)).thenReturn(List.of());
                when(mapaSalvamentoService.salvarMapaCompleto(eq(100L), any())).thenReturn(sp.getMapa());

                SalvarMapaRequest request = new SalvarMapaRequest("Desc", List.of());
                service.salvarMapaSubprocesso(1L, request);

                assertThat(sp.getSituacao()).isEqualTo(MAPEAMENTO_CADASTRO_HOMOLOGADO);
            }

            @Test
            @DisplayName("deve salvar mapa de subprocesso sem mudar situacao se nao era vazio e nem tem novas")
            void salvarMapaSubprocesso_NaoEraVazioSemNovas() {
                Subprocesso sp = criarSubprocessoComMapa(1L);
                sp.setSituacaoForcada(MAPEAMENTO_CADASTRO_HOMOLOGADO);
                sp.getMapa().setCodigo(100L);

                when(consultaService.buscarSubprocesso(1L)).thenReturn(sp);
                when(mapaManutencaoService.competenciasCodMapa(100L)).thenReturn(List.of(new Competencia()));
                when(mapaSalvamentoService.salvarMapaCompleto(eq(100L), any())).thenReturn(sp.getMapa());

                SalvarMapaRequest request = new SalvarMapaRequest("Desc", List.of());
                service.salvarMapaSubprocesso(1L, request);

                assertThat(sp.getSituacao()).isEqualTo(MAPEAMENTO_CADASTRO_HOMOLOGADO);
            }

            @Test
            @DisplayName("deve salvar mapa de subprocesso sem mudar situacao se nao era vazio")
            void salvarMapaSubprocesso_NaoEraVazio() {
                Subprocesso sp = criarSubprocessoComMapa(1L);
                sp.setSituacaoForcada(MAPEAMENTO_CADASTRO_HOMOLOGADO);
                sp.getMapa().setCodigo(100L);

                when(consultaService.buscarSubprocesso(1L)).thenReturn(sp);
                when(mapaManutencaoService.competenciasCodMapa(100L)).thenReturn(List.of(new Competencia()));
                when(mapaSalvamentoService.salvarMapaCompleto(eq(100L), any())).thenReturn(sp.getMapa());

                SalvarMapaRequest request = new SalvarMapaRequest("Desc", List.of(new SalvarMapaRequest.CompetenciaRequest(0L, "Comp", List.of())));
                service.salvarMapaSubprocesso(1L, request);

                assertThat(sp.getSituacao()).isEqualTo(MAPEAMENTO_CADASTRO_HOMOLOGADO);
            }

            @Test
            @DisplayName("deve adicionar competencia sem mudar situacao se nao era vazio")
            void adicionarCompetencia_NaoEraVazio() {
                Subprocesso sp = criarSubprocessoComMapa(1L, TipoProcesso.REVISAO);
                sp.setSituacaoForcada(REVISAO_CADASTRO_HOMOLOGADA);
                sp.getMapa().setCodigo(100L);

                when(consultaService.buscarSubprocesso(1L)).thenReturn(sp);
                when(mapaManutencaoService.competenciasCodMapa(100L)).thenReturn(List.of(new Competencia()));

                service.adicionarCompetencia(1L, new CriarCompetenciaRequest("Desc", List.of(10L)));

                assertThat(sp.getSituacao()).isEqualTo(REVISAO_CADASTRO_HOMOLOGADA);
            }

            @Test
            @DisplayName("deve remover competencia sem mudar situacao se nao ficou vazio")
            void removerCompetencia_NaoFicouVazio() {
                Subprocesso sp = criarSubprocessoComMapa(1L);
                sp.setSituacaoForcada(MAPEAMENTO_MAPA_CRIADO);
                sp.getMapa().setCodigo(100L);

                when(consultaService.buscarSubprocesso(1L)).thenReturn(sp);
                when(mapaManutencaoService.competenciasCodMapa(100L)).thenReturn(List.of(new Competencia()));
                when(mapaManutencaoService.mapaCodigo(100L)).thenReturn(sp.getMapa());

                service.removerCompetencia(1L, 10L);

                assertThat(sp.getSituacao()).isEqualTo(MAPEAMENTO_MAPA_CRIADO);
            }

            @Test
            @DisplayName("deve atualizar competencias validando nulidade")
            void atualizarCompetenciasNulidade() {
                Subprocesso sp = criarSubprocessoComMapa(1L, TipoProcesso.REVISAO);
                sp.setSituacaoForcada(REVISAO_MAPA_AJUSTADO);

                when(repo.buscar(Subprocesso.class, 1L)).thenReturn(sp);

                CompetenciaAjusteDto compDto = new CompetenciaAjusteDto(10L, "Nome", List.of());
                when(mapaManutencaoService.competenciasCodigos(anyList())).thenReturn(List.of());

                service.salvarAjustesMapa(1L, List.of(compDto));

                verify(subprocessoRepo).save(sp);
                assertThat(sp.getSituacao()).isEqualTo(REVISAO_MAPA_AJUSTADO);
            }
        }

        @Nested
        @DisplayName("Criação de Subprocesso")
        class CriacaoSubprocesso {
            @Test
            @DisplayName("criarParaMapeamento deve ignorar unidade raiz e tipos nao elegiveis")
            void criarParaMapeamento_TiposUnidades() {
                Processo p = new Processo();
                p.setCodigo(1L);
                p.setDataLimite(LocalDateTime.now());
                Unidade unidadeOrigem = new Unidade();
                unidadeOrigem.setCodigo(99L);
                Unidade u1 = new Unidade();
                u1.setCodigo(1L);
                u1.setTipo(TipoUnidade.OPERACIONAL);
                Unidade u2 = new Unidade();
                u2.setCodigo(2L);
                u2.setTipo(TipoUnidade.INTEROPERACIONAL);
                Unidade u3 = new Unidade();
                u3.setCodigo(3L);
                u3.setTipo(TipoUnidade.RAIZ);
                Unidade u4 = new Unidade();
                u4.setCodigo(4L);
                u4.setTipo(TipoUnidade.INTERMEDIARIA);

                when(usuarioFacade.usuarioAutenticado()).thenReturn(new Usuario());

                service.criarParaMapeamento(
                        new SubprocessoService.CriarSubprocessosMapeamentoCommand(p, List.of(u1, u2, u3, u4), unidadeOrigem));

                verify(subprocessoRepo).saveAll(argThat(subprocessos -> {
                    long quantidade = java.util.stream.StreamSupport.stream(subprocessos.spliterator(), false).count();
                    return quantidade == 2;
                }));
            }
        }

        @Nested
        @DisplayName("Atualizacao de Subprocesso")
        class AtualizacaoDeSubprocesso {
            @Test
            @DisplayName("deve atualizar subprocesso com todas as datas e mapa")
            void atualizarSubprocesso_Completo() {
                Subprocesso sp = criarSubprocessoComMapa(1L);
                when(consultaService.buscarSubprocesso(1L)).thenReturn(sp);

                LocalDateTime d1 = LocalDateTime.now();
                LocalDateTime f1 = LocalDateTime.now().plusDays(1);
                LocalDateTime d2 = LocalDateTime.now().plusDays(2);
                LocalDateTime f2 = LocalDateTime.now().plusDays(3);

                AtualizarSubprocessoRequest request = AtualizarSubprocessoRequest.builder()
                        .codUnidade(10L)
                        .codMapa(100L)
                        .dataLimiteEtapa1(d1)
                        .dataFimEtapa1(f1)
                        .dataLimiteEtapa2(d2)
                        .dataFimEtapa2(f2)
                        .build();

                when(subprocessoRepo.save(any())).thenReturn(sp);

                service.atualizarEntidade(1L, request.paraCommand());

                assertThat(sp.getDataLimiteEtapa1()).isEqualTo(d1);
                assertThat(sp.getDataFimEtapa1()).isEqualTo(f1);
                assertThat(sp.getDataLimiteEtapa2()).isEqualTo(d2);
                assertThat(sp.getDataFimEtapa2()).isEqualTo(f2);
                assertThat(sp.getMapa()).isNotNull();
                assertThat(sp.getMapa().getCodigo()).isEqualTo(100L);
            }

            @Test
            @DisplayName("deve atualizar subprocesso ignorando datas nulas")
            void atualizarSubprocesso_DatasNulas() {
                Subprocesso sp = criarSubprocessoComMapa(1L);
                when(consultaService.buscarSubprocesso(1L)).thenReturn(sp);
                LocalDateTime d1 = LocalDateTime.now();
                sp.setDataLimiteEtapa1(d1);

                AtualizarSubprocessoRequest request = AtualizarSubprocessoRequest.builder()
                        .codUnidade(10L)
                        .codMapa(100L)
                        .build();

                when(subprocessoRepo.save(any())).thenReturn(sp);

                service.atualizarEntidade(1L, request.paraCommand());

                assertThat(sp.getDataLimiteEtapa1()).isEqualTo(d1);
                assertThat(sp.getMapa().getCodigo()).isEqualTo(100L);
            }

            @Test
            @DisplayName("importarAtividades - switch default branch (DIAGNOSTICO)")
            void importarAtividadesDiagnostico() {
                Subprocesso spDest = criarSubprocessoComMapa(1L, TipoProcesso.DIAGNOSTICO);
                spDest.setSituacaoForcada(NAO_INICIADO);
                spDest.getMapa().setCodigo(100L);

                Subprocesso spOrig = criarSubprocessoComMapa(2L);
                spOrig.getProcesso().setSituacao(SituacaoProcesso.FINALIZADO);
                spOrig.getMapa().setCodigo(200L);
                spOrig.setUnidade(new Unidade());

                when(repo.buscar(Subprocesso.class, 1L)).thenReturn(spDest);
                when(repo.buscar(Subprocesso.class, 2L)).thenReturn(spOrig);
                Usuario user = criarUsuarioMock();
                when(usuarioFacade.usuarioAutenticado()).thenReturn(user);
                when(permissionEvaluator.verificarPermissao(eq(user), any(), any())).thenReturn(true);
                service.importarAtividades(1L, 2L, List.of());

                assertThat(spDest.getSituacao()).isEqualTo(NAO_INICIADO);
            }
        }

        @Nested
        @DisplayName("importarAtividades")
        class ImportarAtividades {
            @Test
            @DisplayName("deve lancar erro se nao tiver permissao no destino")
            void semPermissaoDestino() {
                Subprocesso sp = criarSubprocessoComMapa(null);
                when(repo.buscar(Subprocesso.class, 1L)).thenReturn(sp);
                Usuario user = criarUsuarioMock();
                when(usuarioFacade.usuarioAutenticado()).thenReturn(user);
                when(permissionEvaluator.verificarPermissao(user, sp, AcaoPermissao.EDITAR_CADASTRO)).thenReturn(false);

                List<Long> itens = List.of();
                assertThrows(ErroAcessoNegado.class, () -> service.importarAtividades(1L, 2L, itens));
            }

            @Test
            @DisplayName("deve importar atividades com sucesso")
            void sucesso() {
                Subprocesso spDest = criarSubprocessoComMapa(1L);
                spDest.getMapa().setCodigo(100L);

                Subprocesso spOrig = criarSubprocessoComMapa(2L);
                spOrig.getProcesso().setSituacao(SituacaoProcesso.FINALIZADO);
                spOrig.getMapa().setCodigo(200L);

                Unidade uOrig = new Unidade();
                uOrig.setSigla("UORIG");
                spOrig.setUnidade(uOrig);

                when(repo.buscar(Subprocesso.class, 1L)).thenReturn(spDest);
                when(repo.buscar(Subprocesso.class, 2L)).thenReturn(spOrig);
                Usuario user = criarUsuarioMock();
                when(usuarioFacade.usuarioAutenticado()).thenReturn(user);
                when(permissionEvaluator.verificarPermissao(user, spDest, AcaoPermissao.EDITAR_CADASTRO)).thenReturn(true);
                when(permissionEvaluator.verificarPermissao(user, spOrig, AcaoPermissao.CONSULTAR_PARA_IMPORTACAO)).thenReturn(true);

                service.importarAtividades(1L, 2L, List.of());
                verify(subprocessoRepo).save(spDest);
                verify(copiaMapaService).importarAtividadesDeOutroMapa(eq(200L), eq(100L), any());
                assertThat(spDest.getSituacao()).isEqualTo(MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
            }

            @Test
            @DisplayName("deve importar atividades com sucesso e mudar situacao para REVISAO")
            void sucessoRevisao() {
                Subprocesso spDest = criarSubprocessoComMapa(1L, TipoProcesso.REVISAO);
                spDest.setSituacaoForcada(NAO_INICIADO);
                spDest.getMapa().setCodigo(100L);

                Subprocesso spOrig = criarSubprocessoComMapa(2L);
                spOrig.getProcesso().setSituacao(SituacaoProcesso.FINALIZADO);
                spOrig.getMapa().setCodigo(200L);
                spOrig.setUnidade(new Unidade());

                when(repo.buscar(Subprocesso.class, 1L)).thenReturn(spDest);
                when(repo.buscar(Subprocesso.class, 2L)).thenReturn(spOrig);
                Usuario user = criarUsuarioMock();
                when(usuarioFacade.usuarioAutenticado()).thenReturn(user);
                when(permissionEvaluator.verificarPermissao(eq(user), eq(spDest), any())).thenReturn(true);
                when(permissionEvaluator.verificarPermissao(eq(user), eq(spOrig), any())).thenReturn(true);

                service.importarAtividades(1L, 2L, List.of());
                assertThat(spDest.getSituacao()).isEqualTo(REVISAO_CADASTRO_EM_ANDAMENTO);
            }

            @Test
            @DisplayName("deve falhar se nao tiver permissao na origem")
            void semPermissaoOrigem() {
                Subprocesso spDest = criarSubprocessoComMapa(1L);
                Subprocesso spOrig = criarSubprocessoComMapa(2L);

                when(repo.buscar(Subprocesso.class, 1L)).thenReturn(spDest);
                when(repo.buscar(Subprocesso.class, 2L)).thenReturn(spOrig);
                Usuario user = criarUsuarioMock();
                when(usuarioFacade.usuarioAutenticado()).thenReturn(user);
                when(permissionEvaluator.verificarPermissao(user, spDest, AcaoPermissao.EDITAR_CADASTRO)).thenReturn(true);
                when(permissionEvaluator.verificarPermissao(user, spOrig, AcaoPermissao.CONSULTAR_PARA_IMPORTACAO)).thenReturn(false);

                List<Long> itens = List.of();
                assertThrows(ErroAcessoNegado.class, () -> service.importarAtividades(1L, 2L, itens));
            }
        }
    }
}
