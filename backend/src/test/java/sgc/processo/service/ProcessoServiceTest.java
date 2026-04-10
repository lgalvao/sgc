package sgc.processo.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.springframework.data.domain.*;
import org.springframework.security.core.*;
import sgc.alerta.*;
import sgc.comum.*;
import sgc.comum.erros.*;
import sgc.comum.model.*;
import sgc.fixture.*;
import sgc.mapa.model.*;
import sgc.organizacao.*;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;
import sgc.processo.dto.*;
import sgc.processo.model.*;
import sgc.seguranca.*;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.model.*;
import sgc.subprocesso.service.*;
import sgc.subprocesso.service.SubprocessoValidacaoService.*;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.*;
import static sgc.processo.model.AcaoProcesso.*;
import static sgc.seguranca.AcaoPermissao.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProcessoService Test suite")
@SuppressWarnings("NullAway.Init")
class ProcessoServiceTest {
    @InjectMocks
    private ProcessoService processoService;
    @Mock
    private ProcessoRepo processoRepo;
    @Mock
    private ComumRepo repo;
    @Mock
    private UnidadeHierarquiaService unidadeHierarquiaService;
    @Mock
    private UnidadeService unidadeService;
    @Mock
    private ResponsavelUnidadeService responsavelUnidadeService;
    @Mock
    private SubprocessoService subprocessoService;
    @Mock
    private SubprocessoConsultaService consultaService;
    @Mock
    private LocalizacaoSubprocessoService localizacaoSubprocessoService;
    @Mock
    private SubprocessoValidacaoService validacaoService;
    @Mock
    private UsuarioFacade usuarioService;
    @Mock
    private AlertaFacade servicoAlertas;
    @Mock
    private SgcPermissionEvaluator permissionEvaluator;
    @Mock
    private SubprocessoTransicaoService transicaoService;

    private void mockarResponsaveisEfetivos() {
        when(responsavelUnidadeService.todasPossuemResponsavelEfetivo(anyList())).thenReturn(true);
    }

    private Unidade criarUnidadeValida(Long codigo) {
        Unidade unidade = new Unidade();
        unidade.setCodigo(codigo);
        unidade.setTipo(TipoUnidade.OPERACIONAL);
        unidade.setSituacao(SituacaoUnidade.ATIVA);
        unidade.setSigla("U" + codigo);
        unidade.setNome("Unidade " + codigo);
        return unidade;
    }

    @Nested
    @DisplayName("Cobertura e Casos de Borda")
    class CoverageTests {

        @Test
        @DisplayName("buscarIdsUnidadesComProcessosAtivos deve delegar para repo")
        void buscarIdsUnidadesComProcessosAtivos_DeveDelegar() {
            Long codigoIgnorar = 1L;
            when(processoRepo.listarUnidadesEmSituacoesExcetoProcesso(anyList(), eq(codigoIgnorar)))
                    .thenReturn(List.of(10L, 20L));

            Set<Long> resultado = processoService.buscarIdsUnidadesComProcessosAtivos(codigoIgnorar);

            assertThat(resultado).containsExactlyInAnyOrder(10L, 20L);
        }


        @Test
        @DisplayName("executarAcaoEmBloco nao executa transicoes quando subprocesso nao eh elegivel")
        void executarAcaoEmBloco_NaoExecutaTransicoesQuandoNaoElegivel() {
            Long codProcesso = 1L;
            ProcessarAnaliseEmBlocoCommand req = new ProcessarAnaliseEmBlocoCommand(
                    List.of(10L),
                    ACEITAR
            );

            Usuario usuario = new Usuario();
            when(usuarioService.usuarioAutenticado()).thenReturn(usuario);

            Subprocesso sub = Subprocesso.builder()
                    .codigo(100L)
                    .unidade(Unidade.builder().codigo(10L).build())
                    .situacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO)
                    .build();

            when(consultaService.listarEntidadesPorProcessoEUnidades(codProcesso, req.unidadeCodigos()))
                    .thenReturn(List.of(sub));

            Subprocesso subNaoElegivel = Subprocesso.builder()
                    .codigo(101L)
                    .unidade(Unidade.builder().codigo(10L).build())
                    .situacao(SituacaoSubprocesso.NAO_INICIADO)
                    .build();
            when(consultaService.listarEntidadesPorProcessoEUnidades(codProcesso, req.unidadeCodigos()))
                    .thenReturn(List.of(subNaoElegivel));

            processoService.executarAcaoEmBloco(codProcesso, req);

            verify(transicaoService, never()).aceitarCadastroEmBloco(any());
            verify(transicaoService, never()).homologarCadastroEmBloco(any());
        }
    }

    @Nested
    @DisplayName("Segurança e Controle de Acesso")
    class SecurityTests {
        @Test
        @DisplayName("Deve negar acesso quando usuário não autenticado")
        void deveNegarAcessoQuandoNaoAutenticado() {
            Authentication auth = mock(Authentication.class);
            // Assume permissionEvaluator handles this
            assertThat(processoService.checarAcesso(auth, 1L)).isFalse();
        }
    }

    @Nested
    @DisplayName("Workflow e Inicialização")
    class Workflow {
        @Test
        @DisplayName("Deve iniciar mapeamento com sucesso e salvar")
        void deveIniciarMapeamentoComSucesso() {
            Long id = 100L;
            Usuario usuario = new Usuario();
            when(usuarioService.usuarioAutenticado()).thenReturn(usuario);
            
            Processo p = new Processo();
            p.setCodigo(id);
            p.setSituacao(SituacaoProcesso.CRIADO);
            p.setTipo(TipoProcesso.MAPEAMENTO);
            Unidade uni = criarUnidadeValida(10L);
            p.adicionarParticipantes(Set.of(uni));
            
            when(repo.buscar(Processo.class, id)).thenReturn(p);
            Unidade uniAdmin = new Unidade();
            uniAdmin.setSituacao(SituacaoUnidade.ATIVA);
            when(unidadeService.buscarAdmin()).thenReturn(uniAdmin);
            mockarResponsaveisEfetivos();

            processoService.iniciar(id, List.of());

            verify(processoRepo).save(any(Processo.class));
        }

        @Test
        @DisplayName("Deve iniciar revisao com sucesso e salvar")
        void deveIniciarRevisaoComSucesso() {
            Long id = 100L;
            Usuario usuario = new Usuario();
            when(usuarioService.usuarioAutenticado()).thenReturn(usuario);

            Processo p = new Processo();
            p.setCodigo(id);
            p.setSituacao(SituacaoProcesso.CRIADO);
            p.setTipo(TipoProcesso.REVISAO);
            Unidade uni = criarUnidadeValida(1L);
            p.adicionarParticipantes(Set.of(uni));

            when(repo.buscar(Processo.class, id)).thenReturn(p);
            when(unidadeService.buscarPorCodigos(anyList())).thenReturn(List.of(uni));
            when(unidadeService.buscarTodosCodigosUnidadesComMapa()).thenReturn(List.of(1L));
            
            UnidadeMapa um = new UnidadeMapa();
            um.setUnidadeCodigo(1L);
            when(unidadeService.buscarMapasPorUnidades(anyList())).thenReturn(List.of(um));

            Unidade uniAdmin = new Unidade();
            uniAdmin.setSituacao(SituacaoUnidade.ATIVA);
            when(unidadeService.buscarAdmin()).thenReturn(uniAdmin);
            mockarResponsaveisEfetivos();

            processoService.iniciar(id, List.of(1L));

            verify(processoRepo).save(any(Processo.class));
            verify(subprocessoService).criarParaRevisao(argThat(command ->
                    command.processo() == p
                            && command.unidade() == uni
                            && command.unidadeMapa() == um
                            && command.unidadeOrigem() == uniAdmin));
        }

        @Test
        @DisplayName("Deve iniciar revisao ignorando unidade ancestral selecionada junto com a descendente")
        void deveIniciarRevisaoIgnorandoAncestralRedundante() {
            Long id = 101L;
            Usuario usuario = new Usuario();
            when(usuarioService.usuarioAutenticado()).thenReturn(usuario);

            Processo processo = new Processo();
            processo.setCodigo(id);
            processo.setSituacao(SituacaoProcesso.CRIADO);
            processo.setTipo(TipoProcesso.REVISAO);

            Unidade unidadePai = criarUnidadeValida(10L);
            Unidade unidadeFilha = criarUnidadeValida(20L);
            processo.adicionarParticipantes(Set.of(unidadePai, unidadeFilha));

            when(repo.buscar(Processo.class, id)).thenReturn(processo);
            when(unidadeHierarquiaService.buscarMapaFilhoPai()).thenReturn(Map.of(20L, 10L));
            when(unidadeHierarquiaService.buscarCodigosSuperiores(20L)).thenReturn(List.of(10L));
            when(unidadeService.buscarPorCodigos(List.of(20L))).thenReturn(List.of(unidadeFilha));
            when(unidadeService.buscarTodosCodigosUnidadesComMapa()).thenReturn(List.of(20L));
            when(unidadeService.buscarMapasPorUnidades(List.of(20L))).thenReturn(List.of(
                    UnidadeMapa.builder().unidadeCodigo(20L).build()
            ));
            when(unidadeService.buscarPorCodigos(List.of(10L))).thenReturn(List.of(unidadePai));
            Unidade admin = criarUnidadeValida(999L);
            when(unidadeService.buscarAdmin()).thenReturn(admin);
            mockarResponsaveisEfetivos();

            processoService.iniciar(id, List.of(10L, 20L));

            verify(subprocessoService, times(1)).criarParaRevisao(any());
            verify(subprocessoService).criarParaRevisao(argThat(command ->
                    command.processo() == processo
                            && command.unidade().getCodigo().equals(20L)
            ));
        }

        @Test
        @DisplayName("Deve falhar ao iniciar processo se houver unidades em processo ativo")
        void deveFalharAoIniciarSeHouverUnidadesEmProcessoAtivo() {
            Long id = 100L;
            Usuario usuario = new Usuario();

            Processo p = new Processo();
            p.setCodigo(id);
            p.setSituacao(SituacaoProcesso.CRIADO);
            p.setTipo(TipoProcesso.MAPEAMENTO);
            Unidade uni = criarUnidadeValida(1L);
            p.adicionarParticipantes(Set.of(uni));

            when(repo.buscar(Processo.class, id)).thenReturn(p);
            // Simular que a unidade já está em outro processo
            when(processoRepo.listarUnidadesEmProcessoAtivo(eq(SituacaoProcesso.EM_ANDAMENTO), anyList()))
                    .thenReturn(List.of(1L));
            mockarResponsaveisEfetivos();
            when(usuarioService.usuarioAutenticado()).thenReturn(usuario);

            assertThatThrownBy(() -> processoService.iniciar(id, List.of()))
                    .isInstanceOf(ErroValidacao.class)
                    .hasMessageContaining(Mensagens.UNIDADES_EM_PROCESSO_ATIVO);
        }

        @Test
        @DisplayName("Deve falhar ao iniciar processo se houver unidades sem mapa em REVISAO")
        void deveFalharAoIniciarSeHouverUnidadesSemMapaEmRevisao() {
            Long id = 100L;
            Usuario usuario = new Usuario();

            Processo p = new Processo();
            p.setCodigo(id);
            p.setSituacao(SituacaoProcesso.CRIADO);
            p.setTipo(TipoProcesso.REVISAO);
            Unidade uni = criarUnidadeValida(1L);
            p.adicionarParticipantes(Set.of(uni));

            when(repo.buscar(Processo.class, id)).thenReturn(p);
            when(unidadeService.buscarPorCodigos(anyList())).thenReturn(List.of(uni));
            // Simular que a unidade não tem mapa vigente
            when(unidadeService.buscarTodosCodigosUnidadesComMapa()).thenReturn(List.of());
            when(unidadeService.buscarSiglasPorCodigos(anyList())).thenReturn(List.of("U1"));
            mockarResponsaveisEfetivos();
            when(usuarioService.usuarioAutenticado()).thenReturn(usuario);

            assertThatThrownBy(() -> processoService.iniciar(id, List.of(1L)))
                    .isInstanceOf(ErroValidacao.class)
                    .hasMessageContaining(Mensagens.UNIDADES_SEM_MAPA);
        }

        @Test
        @DisplayName("Deve finalizar processo delegando para repo")
        void deveFinalizarProcessoQuandoTudoHomologado() {
            Long id = 100L;
            Processo p = new Processo();
            p.setCodigo(id);
            p.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
            p.setTipo(TipoProcesso.DIAGNOSTICO);
            
            when(repo.buscar(Processo.class, id)).thenReturn(p);
            when(unidadeService.buscarPorCodigos(anyList())).thenReturn(List.of());
            when(validacaoService.validarSubprocessosParaFinalizacao(id))
                    .thenReturn(ValidationResult.ofValido());
            
            processoService.finalizar(id);
            verify(processoRepo).save(p);
            assertThat(p.getSituacao()).isEqualTo(SituacaoProcesso.FINALIZADO);
        }

        @Test
        @DisplayName("Deve falhar ao iniciar processo se houver unidades sem responsavel efetivo")
        void deveFalharAoIniciarSeHouverUnidadesSemResponsavelEfetivo() {
            Long id = 100L;
            Usuario usuario = new Usuario();

            Processo p = new Processo();
            p.setCodigo(id);
            p.setSituacao(SituacaoProcesso.CRIADO);
            p.setTipo(TipoProcesso.MAPEAMENTO);
            Unidade uni = criarUnidadeValida(1L);
            p.adicionarParticipantes(Set.of(uni));

            when(repo.buscar(Processo.class, id)).thenReturn(p);
            when(unidadeService.buscarPorCodigos(anyList())).thenReturn(List.of(uni));
            when(responsavelUnidadeService.todasPossuemResponsavelEfetivo(anyList())).thenReturn(false);
            when(usuarioService.usuarioAutenticado()).thenReturn(usuario);

            assertThatThrownBy(() -> processoService.iniciar(id, List.of()))
                    .isInstanceOf(ErroValidacao.class)
                    .hasMessageContaining(Mensagens.OPERACAO_NAO_PERMITIDA);
        }
    }

    @Nested
    @DisplayName("Detalhes e Elegibilidade")
    class DetalhesEElegibilidade {
        @Test
        @DisplayName("Deve obter detalhes completos do processo")
        void deveObterDetalhesCompletos() {
            Long codProcesso = 1L;
            Usuario usuario = new Usuario();
            usuario.setPerfilAtivo(Perfil.ADMIN);

            Processo p = new Processo();
            p.setCodigo(codProcesso);
            p.setDescricao("Processo");
            p.setTipo(TipoProcesso.MAPEAMENTO);
            p.setSituacao(SituacaoProcesso.EM_ANDAMENTO);

            Unidade u = criarUnidadeValida(10L);
            p.adicionarParticipantes(Set.of(u));

            when(repo.buscar(Processo.class, codProcesso)).thenReturn(p);
            when(permissionEvaluator.verificarPermissao(usuario, p, AcaoPermissao.FINALIZAR_PROCESSO)).thenReturn(true);

            Subprocesso sp = new Subprocesso();
            sp.setCodigo(100L);
            sp.setUnidade(u);
            sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
            when(consultaService.listarEntidadesPorProcesso(codProcesso)).thenReturn(List.of(sp));
            when(localizacaoSubprocessoService.obterLocalizacoesAtuais(anyCollection())).thenReturn(Map.of(sp.getCodigo(), u));
            when(validacaoService.validarSubprocessosParaFinalizacao(codProcesso)).thenReturn(ValidationResult.ofValido());

            when(usuarioService.usuarioAutenticado()).thenReturn(usuario);
            ProcessoDetalheDto result = processoService.obterDetalhesCompleto(codProcesso, false);

            assertThat(result).isNotNull();
            assertThat(result.getCodigo()).isEqualTo(codProcesso);
            assertThat(result.getUnidades()).isNotEmpty();
        }

        @Test
        @DisplayName("Deve listar subprocessos elegíveis para ação em bloco")
        void deveListarSubprocessosElegiveis() {
            Long codProcesso = 1L;
            Usuario usuario = new Usuario();
            usuario.setUnidadeAtivaCodigo(10L);
            usuario.setPerfilAtivo(Perfil.CHEFE);
            when(usuarioService.usuarioAutenticado()).thenReturn(usuario);

            Subprocesso s1 = new Subprocesso();
            s1.setCodigo(101L);
            s1.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
            Unidade u1 = new Unidade();
            u1.setCodigo(10L);
            s1.setUnidade(u1);

            Subprocesso s2 = new Subprocesso();
            s2.setCodigo(102L);
            s2.setSituacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO);
            Unidade u2 = new Unidade();
            u2.setCodigo(20L);
            s2.setUnidade(u2);

            Subprocesso s3 = new Subprocesso();
            s3.setCodigo(103L);
            s3.setSituacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO); // Não elegível
            Unidade u3 = new Unidade();
            u3.setCodigo(30L);
            s3.setUnidade(u3);

            when(consultaService.listarEntidadesPorProcessoEUnidades(eq(codProcesso), anyList())).thenReturn(List.of(s1, s2, s3));
            when(localizacaoSubprocessoService.obterLocalizacoesAtuais(anyCollection())).thenReturn(Map.of(
                    s1.getCodigo(), u1,
                    s2.getCodigo(), u1
            ));
            when(permissionEvaluator.verificarPermissaoSilenciosa(usuario, s1, AcaoPermissao.ACEITAR_CADASTRO)).thenReturn(true);
            when(permissionEvaluator.verificarPermissaoSilenciosa(usuario, s1, AcaoPermissao.HOMOLOGAR_CADASTRO)).thenReturn(false);
            when(permissionEvaluator.verificarPermissaoSilenciosa(usuario, s2, AcaoPermissao.ACEITAR_MAPA)).thenReturn(true);
            when(permissionEvaluator.verificarPermissaoSilenciosa(usuario, s2, AcaoPermissao.HOMOLOGAR_MAPA)).thenReturn(false);

            List<SubprocessoElegivelDto> result = processoService.listarSubprocessosElegiveis(codProcesso);

            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Deve montar hierarquia no DTO corretamente para GESTOR")
        void deveMontarHierarquiaDtoGestor() {
            Long codProcesso = 1L;
            Usuario usuario = new Usuario();
            usuario.setUnidadeAtivaCodigo(10L); // Pai
            usuario.setPerfilAtivo(Perfil.GESTOR);

            Processo p = new Processo();
            p.setCodigo(codProcesso);
            p.setDescricao("Processo");
            p.setTipo(TipoProcesso.MAPEAMENTO);
            p.setSituacao(SituacaoProcesso.EM_ANDAMENTO);

            Unidade uPai = criarUnidadeValida(10L);
            uPai.setSigla("PAI");
            p.adicionarParticipantes(Set.of(uPai));

            Unidade uFilho = criarUnidadeValida(20L);
            uFilho.setSigla("FILHO");
            uFilho.setUnidadeSuperior(uPai);
            p.adicionarParticipantes(Set.of(uFilho));

            Unidade uSemSub = criarUnidadeValida(30L);
            uSemSub.setSigla("SEMSUB");
            p.adicionarParticipantes(Set.of(uSemSub));

            when(repo.buscar(Processo.class, codProcesso)).thenReturn(p);
            when(unidadeHierarquiaService.buscarIdsDescendentes(10L)).thenReturn(List.of(20L));

            Subprocesso sp = new Subprocesso();
            sp.setCodigo(100L);
            sp.setUnidade(uPai);
            sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
            // Filho não tem subprocesso para cobrir branch sp != null

            when(consultaService.listarEntidadesPorProcesso(codProcesso)).thenReturn(List.of(sp));
            when(localizacaoSubprocessoService.obterLocalizacoesAtuais(anyCollection())).thenReturn(Map.of(sp.getCodigo(), uPai));

            when(usuarioService.usuarioAutenticado()).thenReturn(usuario);
            ProcessoDetalheDto result = processoService.obterDetalhesCompleto(codProcesso, false);

            assertThat(result.getUnidades()).isNotEmpty();
            assertThat(result.getUnidades().getFirst().getFilhos()).isNotEmpty();
        }

        @Test
        @DisplayName("Deve falhar quando houver snapshot legado sem nome ou sigla")
        void deveFalharQuandoHouverSnapshotLegadoSemNomeOuSigla() {
            Long codProcesso = 1L;
            Usuario usuario = new Usuario();
            usuario.setPerfilAtivo(Perfil.ADMIN);

            Processo p = new Processo();
            p.setCodigo(codProcesso);
            p.setDescricao("Processo");
            p.setTipo(TipoProcesso.MAPEAMENTO);
            p.setSituacao(SituacaoProcesso.EM_ANDAMENTO);

            Unidade unidadeComSigla = criarUnidadeValida(10L);
            unidadeComSigla.setSigla("ABC");
            Unidade unidadeSemSigla = criarUnidadeValida(20L);
            unidadeSemSigla.setSigla(null);
            p.adicionarParticipantes(Set.of(unidadeComSigla, unidadeSemSigla));
            p.getParticipantes().stream()
                    .filter(participante -> Objects.equals(participante.getUnidadeCodigoPersistido(), 20L))
                    .findFirst()
                    .ifPresent(participante -> participante.setSigla(null));

            Subprocesso sp1 = new Subprocesso();
            sp1.setCodigo(100L);
            sp1.setUnidade(unidadeComSigla);
            sp1.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO);

            Subprocesso sp2 = new Subprocesso();
            sp2.setCodigo(200L);
            sp2.setUnidade(unidadeSemSigla);
            sp2.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO);

            when(repo.buscar(Processo.class, codProcesso)).thenReturn(p);
            when(permissionEvaluator.verificarPermissao(usuario, p, AcaoPermissao.FINALIZAR_PROCESSO)).thenReturn(true);
            when(consultaService.listarEntidadesPorProcesso(codProcesso)).thenReturn(List.of(sp1, sp2));
            when(localizacaoSubprocessoService.obterLocalizacoesAtuais(anyCollection())).thenReturn(Map.of(
                    sp1.getCodigo(), unidadeComSigla,
                    sp2.getCodigo(), unidadeSemSigla
            ));
            when(validacaoService.validarSubprocessosParaFinalizacao(codProcesso)).thenReturn(ValidationResult.ofValido());

            when(usuarioService.usuarioAutenticado()).thenReturn(usuario);
            assertThatThrownBy(() -> processoService.obterDetalhesCompleto(codProcesso, false))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Snapshot inconsistente de unidade participante")
                    .hasMessageContaining("processo 1")
                    .hasMessageContaining("unidade 20");
        }

        @Test
        @DisplayName("isElegivelParaAcaoEmBloco deve retornar false quando elegivelMapa mas sem permissao ACEITAR ou HOMOLOGAR")
        void isElegivelParaAcaoEmBloco_DeveRetornarFalseQuandoElegivelMapaSemPermissoes() {
            Long codProcesso = 1L;
            Usuario usuario = new Usuario();
            usuario.setPerfilAtivo(Perfil.ADMIN);
            when(usuarioService.usuarioAutenticado()).thenReturn(usuario);

            Subprocesso sp = new Subprocesso();
            sp.setCodigo(100L);
            sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO);
            sp.setUnidade(new Unidade());

            when(consultaService.listarEntidadesPorProcesso(codProcesso)).thenReturn(List.of(sp));
            when(permissionEvaluator.verificarPermissaoSilenciosa(usuario, sp, ACEITAR_MAPA)).thenReturn(false);
            when(permissionEvaluator.verificarPermissaoSilenciosa(usuario, sp, HOMOLOGAR_MAPA)).thenReturn(false);

            List<SubprocessoElegivelDto> result = processoService.listarSubprocessosElegiveis(codProcesso);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("isElegivelParaAcaoEmBloco deve retornar false quando elegivelDisponibilizacao mas sem permissao DISPONIBILIZAR_MAPA")
        void isElegivelParaAcaoEmBloco_DeveRetornarFalseQuandoElegivelDisponibilizacaoSemPermissao() {
            Long codProcesso = 1L;
            Usuario usuario = new Usuario();
            usuario.setPerfilAtivo(Perfil.ADMIN);
            when(usuarioService.usuarioAutenticado()).thenReturn(usuario);

            Subprocesso sp = new Subprocesso();
            sp.setCodigo(100L);
            sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
            sp.setUnidade(new Unidade());

            when(consultaService.listarEntidadesPorProcesso(codProcesso)).thenReturn(List.of(sp));
            when(permissionEvaluator.verificarPermissaoSilenciosa(usuario, sp, DISPONIBILIZAR_MAPA)).thenReturn(false);

            List<SubprocessoElegivelDto> result = processoService.listarSubprocessosElegiveis(codProcesso);
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Ações em Bloco")
    class AcoesEmBloco {
        @Test
        @DisplayName("Deve falhar ao executar ação em bloco sem unidades")
        void deveFalharAoExecutarAcaoEmBlocoSemUnidades() {
            ProcessarAnaliseEmBlocoCommand req = new ProcessarAnaliseEmBlocoCommand(List.of(), ACEITAR);
            assertThatThrownBy(() -> processoService.executarAcaoEmBloco(1L, req))
                    .isInstanceOf(ErroValidacao.class)
                    .hasMessageContaining(Mensagens.SELECIONE_AO_MENOS_UMA_UNIDADE);
        }

        @Test
        @DisplayName("Deve executar ação de HOMOLOGAR e ACEITAR separando cadastro e validacao")
        void deveExecutarAcaoBlocoHomologarEAceitar() {
            Usuario usuario = new Usuario();
            when(usuarioService.usuarioAutenticado()).thenReturn(usuario);

            Subprocesso sCad = new Subprocesso();
            sCad.setCodigo(10L);
            sCad.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO);

            Subprocesso sVal = new Subprocesso();
            sVal.setCodigo(20L);
            sVal.setSituacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO);

            when(consultaService.listarEntidadesPorProcessoEUnidades(eq(1L), anyList()))
                    .thenReturn(List.of(sCad, sVal));

            // Teste ACEITAR
            ProcessarAnaliseEmBlocoCommand reqAceitar = new ProcessarAnaliseEmBlocoCommand(List.of(10L, 20L), ACEITAR);
            processoService.executarAcaoEmBloco(1L, reqAceitar);
            verify(transicaoService).aceitarCadastroEmBloco(List.of(10L));
            verify(transicaoService).aceitarValidacaoEmBloco(List.of(20L));

            // Teste HOMOLOGAR
            ProcessarAnaliseEmBlocoCommand reqHomologar = new ProcessarAnaliseEmBlocoCommand(List.of(10L, 20L), HOMOLOGAR);
            processoService.executarAcaoEmBloco(1L, reqHomologar);
            verify(transicaoService).homologarCadastroEmBloco(List.of(10L));
            verify(transicaoService).homologarValidacaoEmBloco(List.of(20L));
        }
    }

    @Nested
    @DisplayName("Criação de Processo")
    class Criacao {
        @Test
        @DisplayName("Deve criar processo quando dados válidos")
        void deveCriarProcessoQuandoDadosValidos() {
            CriarProcessoRequest req = new CriarProcessoRequest(
                    "Teste", TipoProcesso.MAPEAMENTO, LocalDateTime.now(), List.of(1L));

            Unidade uni = criarUnidadeValida(1L);
            when(unidadeService.buscarPorCodigos(List.of(1L))).thenReturn(List.of(uni));
            when(processoRepo.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));
            mockarResponsaveisEfetivos();

            Processo resultado = processoService.criar(req);

            assertThat(resultado).isNotNull();
            assertThat(resultado.getDescricao()).isEqualTo("Teste");
            verify(processoRepo).saveAndFlush(any());
        }

        @Test
        @DisplayName("Deve falhar ao criar processo com unidade sem responsável efetivo")
        void deveFalharCriacaoSemResponsavelEfetivo() {
            CriarProcessoRequest req = new CriarProcessoRequest(
                    "Teste", TipoProcesso.MAPEAMENTO, LocalDateTime.now(), List.of(1L));
            Unidade unidade = new Unidade();
            unidade.setCodigo(1L);
            unidade.setSigla("U1");
            unidade.setSituacao(SituacaoUnidade.ATIVA);
            when(unidadeService.buscarPorCodigos(List.of(1L))).thenReturn(List.of(unidade));
            when(responsavelUnidadeService.todasPossuemResponsavelEfetivo(anyList())).thenReturn(false);

            assertThatThrownBy(() -> processoService.criar(req))
                    .isInstanceOf(ErroValidacao.class)
                    .hasMessageContaining(Mensagens.OPERACAO_NAO_PERMITIDA);
        }
    }

    @Nested
    @DisplayName("Checagem de Acesso")
    class ChecagemAcesso {
        @Test
        @DisplayName("Deve retornar false se auth for nulo ou invalido")
        void deveRetornarFalseSeAuthInvalido() {
            assertThat(processoService.checarAcesso(null, 1L)).isFalse();

            Authentication auth = mock(Authentication.class);
            when(auth.isAuthenticated()).thenReturn(false);
            assertThat(processoService.checarAcesso(auth, 1L)).isFalse();

            when(auth.isAuthenticated()).thenReturn(true);
            when(auth.getPrincipal()).thenReturn(new Object());
            assertThat(processoService.checarAcesso(auth, 1L)).isFalse();
        }

        @Test
        @DisplayName("Deve retornar true se ADMIN")
        void deveRetornarTrueSeAdmin() {
            Authentication auth = mock(Authentication.class);
            Usuario user = new Usuario();
            user.setPerfilAtivo(Perfil.ADMIN);
            when(auth.isAuthenticated()).thenReturn(true);
            when(auth.getPrincipal()).thenReturn(user);

            assertThat(processoService.checarAcesso(auth, 1L)).isTrue();
        }

        @Test
        @DisplayName("Deve retornar true se unidade esta no processo para GESTOR/CHEFE")
        void deveRetornarTrueSeUnidadeNoProcesso() {
            Authentication auth = mock(Authentication.class);
            Usuario user = new Usuario();
            user.setPerfilAtivo(Perfil.CHEFE);
            user.setUnidadeAtivaCodigo(10L);
            when(auth.isAuthenticated()).thenReturn(true);
            when(auth.getPrincipal()).thenReturn(user);

            Processo p = new Processo();
            Unidade u = criarUnidadeValida(10L);
            p.adicionarParticipantes(Set.of(u));

            when(processoRepo.buscarPorCodigoComParticipantes(1L)).thenReturn(Optional.of(p));

            assertThat(processoService.checarAcesso(auth, 1L)).isTrue();
        }
    }

    @Nested
    @DisplayName("Consultas e Detalhes")
    class Consultas {
        @Test
        @DisplayName("Deve listar para importacao")
        void deveListarParaImportacao() {
            Processo p = new Processo();
            when(processoRepo.listarPorSituacaoComParticipantes(SituacaoProcesso.FINALIZADO)).thenReturn(List.of(p));

            List<Processo> res = processoService.listarParaImportacao();
            assertThat(res).containsExactly(p);
            verify(processoRepo).listarPorSituacaoComParticipantes(SituacaoProcesso.FINALIZADO);
        }

        @Test
        @DisplayName("Deve listar ativos para ADMIN")
        void deveListarAtivosParaAdmin() {
            Usuario admin = new Usuario();
            admin.setPerfilAtivo(Perfil.ADMIN);
            when(usuarioService.usuarioAutenticado()).thenReturn(admin);

            Processo p = new Processo();
            when(processoRepo.listarPorSituacao(SituacaoProcesso.EM_ANDAMENTO)).thenReturn(List.of(p));

            List<Processo> res = processoService.listarAtivos();
            assertThat(res).containsExactly(p);
            verify(processoRepo).listarPorSituacao(SituacaoProcesso.EM_ANDAMENTO);
            verify(processoRepo, never()).listarPorSituacaoEUnidadeCodigos(any(), any());
        }

        @Test
        @DisplayName("Deve listar ativos para usuario normal")
        void deveListarAtivosParaUsuarioNormal() {
            Usuario gestor = new Usuario();
            gestor.setPerfilAtivo(Perfil.GESTOR);
            Unidade u = new Unidade();
            u.setCodigo(1L);
            gestor.setUnidadeAtivaCodigo(1L);
            when(usuarioService.usuarioAutenticado()).thenReturn(gestor);
            when(unidadeHierarquiaService.buscarIdsDescendentes(1L)).thenReturn(List.of());

            Processo p = new Processo();
            when(processoRepo.listarPorSituacaoEUnidadeCodigos(eq(SituacaoProcesso.EM_ANDAMENTO), anyList())).thenReturn(List.of(p));

            List<Processo> res = processoService.listarAtivos();
            assertThat(res).containsExactly(p);
            verify(processoRepo).listarPorSituacaoEUnidadeCodigos(eq(SituacaoProcesso.EM_ANDAMENTO), anyList());
            verify(processoRepo, never()).listarPorSituacao(any());
        }

        @Test
        @DisplayName("Deve listar iniciados por participantes")
        void deveListarIniciadosPorParticipantes() {
            Pageable pageable = Pageable.unpaged();
            Processo p = new Processo();
            when(processoRepo.listarPorParticipantesESituacaoDiferente(anyList(), eq(SituacaoProcesso.CRIADO), eq(pageable)))
                    .thenReturn(new PageImpl<>(List.of(p)));

            Page<Processo> res = processoService.listarIniciadosPorParticipantes(List.of(1L), pageable);
            assertThat(res.getContent()).containsExactly(p);
            verify(processoRepo).listarPorParticipantesESituacaoDiferente(List.of(1L), SituacaoProcesso.CRIADO, pageable);
        }

        @Test
        @DisplayName("Deve listar unidades bloqueadas por tipo")
        void deveListarUnidadesBloqueadasPorTipo() {
            when(processoRepo.listarUnidadesBloqueadasPorSituacaoETipo(SituacaoProcesso.EM_ANDAMENTO, TipoProcesso.MAPEAMENTO))
                    .thenReturn(List.of(1L, 2L));

            List<Long> res = processoService.listarUnidadesBloqueadasPorTipo(TipoProcesso.MAPEAMENTO);
            assertThat(res).containsExactly(1L, 2L);
            verify(processoRepo).listarUnidadesBloqueadasPorSituacaoETipo(SituacaoProcesso.EM_ANDAMENTO, TipoProcesso.MAPEAMENTO);
        }

        @Test
        @DisplayName("Deve buscar entidade por ID")
        void deveBuscarEntidadePorId() {
            Long id = 100L;
            Processo processo = ProcessoFixture.processoPadrao();
            when(repo.buscar(Processo.class, id)).thenReturn(processo);

            Processo res = processoService.buscarPorCodigo(id);
            assertThat(res).isEqualTo(processo);
        }

        @Test
        @DisplayName("Deve obter processo por ID (Optional)")
        void deveobterPorCodigoOptional() {
            Long id = 100L;
            Processo processo = ProcessoFixture.processoPadrao();
            when(processoRepo.buscarPorCodigoComParticipantes(id)).thenReturn(Optional.of(processo));

            Optional<Processo> res = processoService.buscarOpt(id);
            assertThat(res).isPresent();
        }

        @Test
        @DisplayName("Deve listar todos com paginação")
        void deveListarTodosPaginado() {
            Pageable pageable = Pageable.unpaged();
            when(processoRepo.findAll(pageable)).thenReturn(Page.empty());

            var res = processoService.listarTodos(pageable);
            assertThat(res).isEmpty();
        }
    }

    @Nested
    @DisplayName("Operações em Bloco")
    class OperacoesEmBloco {
        @Nested
        @DisplayName("Executar ação em Bloco - DISPONIBILIZAR")
        class AcaoDisponibilizar {
            @Test
            @DisplayName("Deve disponibilizar mapas em bloco quando ação é DISPONIBILIZAR")
            void deveDisponibilizarMapasEmBloco() {

                Usuario usuario = new Usuario();
                usuario.setTituloEleitoral("12345678901");
                when(usuarioService.usuarioAutenticado()).thenReturn(usuario);

                LocalDate dataLimite = LocalDate.now().plusDays(30);
                DisponibilizarMapaEmBlocoCommand req = new DisponibilizarMapaEmBlocoCommand(
                        List.of(1L, 2L, 3L),
                        dataLimite
                );

                Subprocesso sp1 = Subprocesso.builder().codigo(1001L).unidade(Unidade.builder().codigo(1L).build()).build();
                Subprocesso sp2 = Subprocesso.builder().codigo(1002L).unidade(Unidade.builder().codigo(2L).build()).build();
                Subprocesso sp3 = Subprocesso.builder().codigo(1003L).unidade(Unidade.builder().codigo(3L).build()).build();
                when(consultaService.listarEntidadesPorProcessoEUnidades(100L, List.of(1L, 2L, 3L))).thenReturn(List.of(sp1, sp2, sp3));
                doReturn(true).when(permissionEvaluator).verificarPermissao(eq(usuario), any(), eq(DISPONIBILIZAR_MAPA));

                processoService.executarAcaoEmBloco(100L, req);

                ArgumentCaptor<DisponibilizarMapaRequest> captor =
                        ArgumentCaptor.forClass(DisponibilizarMapaRequest.class);
                verify(transicaoService).disponibilizarMapaEmBloco(
                        eq(List.of(1001L, 1002L, 1003L)),
                        captor.capture()
                );

                DisponibilizarMapaRequest captured = captor.getValue();
                assertThat(captured.dataLimite()).isEqualTo(dataLimite);
                assertThat(captured.observacoes()).isEqualTo("Disponibilização em bloco");
            }
        }

        @Nested
        @DisplayName("Executar ação em Bloco - ACEITAR")
        class AcaoAceitar {
            @Test
            @DisplayName("Deve aceitar cadastro quando subprocessos estão em MAPEAMENTO_CADASTRO_DISPONIBILIZADO")
            void deveAceitarCadastroQuandoMapeamentoCadastro() {

                Usuario usuario = new Usuario();
                when(usuarioService.usuarioAutenticado()).thenReturn(usuario);

                Subprocesso sp1 = Subprocesso.builder()
                        .codigo(1L)
                        .unidade(Unidade.builder().codigo(10L).build())
                        .situacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO)
                        .build();
                Subprocesso sp2 = Subprocesso.builder()
                        .codigo(2L)
                        .unidade(Unidade.builder().codigo(20L).build())
                        .situacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO)
                        .build();

                when(consultaService.listarEntidadesPorProcessoEUnidades(100L, List.of(10L, 20L))).thenReturn(List.of(sp1, sp2));

                ProcessarAnaliseEmBlocoCommand req = new ProcessarAnaliseEmBlocoCommand(
                        List.of(10L, 20L),
                        ACEITAR
                );

                processoService.executarAcaoEmBloco(100L, req);

                verify(transicaoService).aceitarCadastroEmBloco(List.of(1L, 2L));
            }

            @Test
            @DisplayName("Deve aceitar validação quando subprocessos estão em situação de mapa disponibilizado")
            void deveAceitarValidacaoQuandoMapaDisponibilizado() {

                Usuario usuario = new Usuario();
                when(usuarioService.usuarioAutenticado()).thenReturn(usuario);

                Subprocesso sp1 = Subprocesso.builder()
                        .codigo(1L)
                        .unidade(Unidade.builder().codigo(10L).build())
                        .situacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO)
                        .build();

                when(consultaService.listarEntidadesPorProcessoEUnidades(100L, List.of(10L))).thenReturn(List.of(sp1));

                ProcessarAnaliseEmBlocoCommand req = new ProcessarAnaliseEmBlocoCommand(
                        List.of(10L),
                        ACEITAR
                );

                processoService.executarAcaoEmBloco(100L, req);

                verify(transicaoService).aceitarValidacaoEmBloco(List.of(1L));
            }
        }

        @Nested
        @DisplayName("Executar ação em Bloco - HOMOLOGAR")
        class AcaoHomologar {
            @Test
            @DisplayName("Deve homologar cadastro quando subprocessos estão em situação de cadastro")
            void deveHomologarCadastroQuandoCadastro() {

                Usuario usuario = new Usuario();
                when(usuarioService.usuarioAutenticado()).thenReturn(usuario);

                Subprocesso sp1 = Subprocesso.builder()
                        .codigo(1L)
                        .unidade(Unidade.builder().codigo(10L).build())
                        .situacao(SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA)
                        .build();

                when(consultaService.listarEntidadesPorProcessoEUnidades(100L, List.of(10L))).thenReturn(List.of(sp1));

                ProcessarAnaliseEmBlocoCommand req = new ProcessarAnaliseEmBlocoCommand(
                        List.of(10L),
                        HOMOLOGAR
                );

                processoService.executarAcaoEmBloco(100L, req);

                verify(transicaoService).homologarCadastroEmBloco(List.of(1L));
            }

            @Test
            @DisplayName("Deve homologar validação quando subprocessos estão em validação")
            void deveHomologarValidacaoQuandoValidacao() {

                Usuario usuario = new Usuario();
                when(usuarioService.usuarioAutenticado()).thenReturn(usuario);

                Subprocesso sp1 = Subprocesso.builder()
                        .codigo(1L)
                        .unidade(Unidade.builder().codigo(10L).build())
                        .situacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO)
                        .build();

                when(consultaService.listarEntidadesPorProcessoEUnidades(100L, List.of(10L))).thenReturn(List.of(sp1));

                ProcessarAnaliseEmBlocoCommand req = new ProcessarAnaliseEmBlocoCommand(
                        List.of(10L),
                        HOMOLOGAR
                );

                processoService.executarAcaoEmBloco(100L, req);

                verify(transicaoService).homologarValidacaoEmBloco(List.of(1L));
            }
        }
    }

    @Nested
    @DisplayName("Cobertura Adicional de Branches")
    class CoberturaAdicional {

        @Test
        @DisplayName("Deve iniciar diagnostico com sucesso e salvar")
        void deveIniciarDiagnosticoComSucesso() {
            Long id = 100L;
            Usuario usuario = new Usuario();
            when(usuarioService.usuarioAutenticado()).thenReturn(usuario);

            Processo p = new Processo();
            p.setCodigo(id);
            p.setSituacao(SituacaoProcesso.CRIADO);
            p.setTipo(TipoProcesso.DIAGNOSTICO);
            Unidade uni = criarUnidadeValida(1L);
            p.adicionarParticipantes(Set.of(uni));

            when(repo.buscar(Processo.class, id)).thenReturn(p);
            when(unidadeService.buscarPorCodigos(anyList())).thenReturn(List.of(uni));
            when(unidadeService.buscarTodosCodigosUnidadesComMapa()).thenReturn(List.of(1L));
            
            UnidadeMapa um = new UnidadeMapa();
            um.setUnidadeCodigo(1L);
            when(unidadeService.buscarMapasPorUnidades(anyList())).thenReturn(List.of(um));

            Unidade uniAdmin = new Unidade();
            uniAdmin.setSituacao(SituacaoUnidade.ATIVA);
            when(unidadeService.buscarAdmin()).thenReturn(uniAdmin);
            mockarResponsaveisEfetivos();

            processoService.iniciar(id, List.of());

            verify(processoRepo).save(any(Processo.class));
            verify(subprocessoService).criarParaDiagnostico(argThat(command ->
                    command.processo() == p
                            && command.unidade() == uni
                            && command.unidadeMapa() == um
                            && command.unidadeOrigem() == uniAdmin));
        }

        @Test
        @DisplayName("Deve omitir mapaCodigo no DTO quando subprocesso nao possuir mapa")
        void deveOmitirMapaCodigoNoDtoQuandoSubprocessoSemMapa() {
            Long codProcesso = 1L;
            Usuario usuario = new Usuario();
            usuario.setPerfilAtivo(Perfil.ADMIN);

            Processo p = new Processo();
            p.setCodigo(codProcesso);
            p.setTipo(TipoProcesso.MAPEAMENTO);
            p.setSituacao(SituacaoProcesso.EM_ANDAMENTO);

            Unidade u = criarUnidadeValida(10L);
            p.adicionarParticipantes(Set.of(u));

            when(repo.buscar(Processo.class, codProcesso)).thenReturn(p);
            Subprocesso sp = new Subprocesso();
            sp.setCodigo(100L);
            sp.setUnidade(u);
            sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
            sp.setMapa(null); // Explicitly null
            
            when(consultaService.listarEntidadesPorProcesso(codProcesso)).thenReturn(List.of(sp));
            when(localizacaoSubprocessoService.obterLocalizacoesAtuais(anyCollection())).thenReturn(Map.of(sp.getCodigo(), u));

            when(usuarioService.usuarioAutenticado()).thenReturn(usuario);
            ProcessoDetalheDto result = processoService.obterDetalhesCompleto(codProcesso, false);

            assertThat(result.getUnidades().getFirst().getMapaCodigo()).isNull();
        }

        @Test
        @DisplayName("Deve incluir mapaCodigo no DTO quando subprocesso possuir mapa")
        void deveIncluirMapaCodigoNoDtoQuandoSubprocessoComMapa() {
            Long codProcesso = 1L;
            Usuario usuario = new Usuario();
            usuario.setPerfilAtivo(Perfil.ADMIN);

            Processo p = new Processo();
            p.setCodigo(codProcesso);
            p.setTipo(TipoProcesso.MAPEAMENTO);
            p.setSituacao(SituacaoProcesso.EM_ANDAMENTO);

            Unidade u = criarUnidadeValida(10L);
            p.adicionarParticipantes(Set.of(u));

            when(repo.buscar(Processo.class, codProcesso)).thenReturn(p);
            Subprocesso sp = new Subprocesso();
            sp.setCodigo(100L);
            sp.setUnidade(u);
            sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
            Mapa mapa = new Mapa();
            mapa.setCodigo(500L);
            sp.setMapa(mapa);
            
            when(consultaService.listarEntidadesPorProcesso(codProcesso)).thenReturn(List.of(sp));
            when(localizacaoSubprocessoService.obterLocalizacoesAtuais(anyCollection())).thenReturn(Map.of(sp.getCodigo(), u));

            when(usuarioService.usuarioAutenticado()).thenReturn(usuario);
            ProcessoDetalheDto result = processoService.obterDetalhesCompleto(codProcesso, false);

            assertThat(result.getUnidades().getFirst().getMapaCodigo()).isEqualTo(500L);
        }

        @ParameterizedTest
        @EnumSource(value = SituacaoSubprocesso.class, names = {
                "REVISAO_CADASTRO_DISPONIBILIZADA",
                "REVISAO_MAPA_COM_SUGESTOES",
                "REVISAO_MAPA_VALIDADO",
                "MAPEAMENTO_CADASTRO_HOMOLOGADO",
                "MAPEAMENTO_MAPA_CRIADO",
                "REVISAO_CADASTRO_HOMOLOGADA",
                "REVISAO_MAPA_AJUSTADO"
        })

        @DisplayName("Deve verificar elegibilidade para acao em bloco para diversas situacoes")
        void deveVerificarElegibilidadeParaDiversasSituacoes(SituacaoSubprocesso situacao) {
            Long codProcesso = 1L;
            Usuario usuario = new Usuario();
            usuario.setPerfilAtivo(Perfil.ADMIN);
            when(usuarioService.usuarioAutenticado()).thenReturn(usuario);

            Subprocesso sp = new Subprocesso();
            sp.setCodigo(100L);
            sp.setUnidade(new Unidade());
            sp.setSituacao(situacao);

            when(consultaService.listarEntidadesPorProcesso(codProcesso)).thenReturn(List.of(sp));
            when(permissionEvaluator.verificarPermissaoSilenciosa(any(), any(), any())).thenReturn(true);
            when(localizacaoSubprocessoService.obterLocalizacaoAtual(any())).thenReturn(new Unidade());

            List<SubprocessoElegivelDto> result = processoService.listarSubprocessosElegiveis(codProcesso);
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Deve verificar elegibilidade quando permissao eh HOMOLOGAR_MAPA")
        void deveVerificarElegibilidadeComPermissaoHomologarMapa() {
            Long codProcesso = 1L;
            Usuario usuario = new Usuario();
            usuario.setPerfilAtivo(Perfil.ADMIN);
            when(usuarioService.usuarioAutenticado()).thenReturn(usuario);

            Subprocesso sp = new Subprocesso();
            sp.setCodigo(100L);
            sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_COM_SUGESTOES);
            sp.setUnidade(new Unidade());

            when(consultaService.listarEntidadesPorProcesso(codProcesso)).thenReturn(List.of(sp));
            when(permissionEvaluator.verificarPermissaoSilenciosa(usuario, sp, ACEITAR_MAPA)).thenReturn(false);
            when(permissionEvaluator.verificarPermissaoSilenciosa(usuario, sp, HOMOLOGAR_MAPA)).thenReturn(true);
            when(localizacaoSubprocessoService.obterLocalizacaoAtual(any())).thenReturn(new Unidade());

            List<SubprocessoElegivelDto> result = processoService.listarSubprocessosElegiveis(codProcesso);
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Deve falhar ao iniciar revisao com unidades vazias")
        void deveFalharAoIniciarRevisaoSemUnidades() {
            Long id = 100L;
            Usuario usuario = new Usuario();
            when(usuarioService.usuarioAutenticado()).thenReturn(usuario);

            Processo p = new Processo();
            p.setCodigo(id);
            p.setSituacao(SituacaoProcesso.CRIADO);
            p.setTipo(TipoProcesso.REVISAO);

            when(repo.buscar(Processo.class, id)).thenReturn(p);

            assertThatThrownBy(() -> processoService.iniciar(id, List.of()))
                    .isInstanceOf(ErroValidacao.class)
                    .hasMessageContaining(Mensagens.LISTA_UNIDADES_OBRIGATORIA_REVISAO);
        }

        @Test
        @DisplayName("Deve falhar ao enviar lembrete quando processo sem data limite")
        void deveFalharAoEnviarLembreteSemDataLimite() {
            Long codProcesso = 1L;
            Long codUnidade = 10L;

            Processo p = new Processo();
            p.setCodigo(codProcesso);
            Unidade u = criarUnidadeValida(codUnidade);
            u.setTituloTitular("TITULAR");
            p.adicionarParticipantes(Set.of(u));
            p.setDataLimite(null);

            when(processoRepo.buscarPorCodigoComParticipantes(codProcesso)).thenReturn(Optional.of(p));
            when(unidadeService.buscarPorCodigo(codUnidade)).thenReturn(u);

            assertThatThrownBy(() -> processoService.enviarLembrete(codProcesso, codUnidade))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("sem data limite");
        }

        @Test
        @DisplayName("processarAcoesBlocoAceiteHomologacao - fall-through branch")
        void deveNaoFazerNadaQuandoAcaoNaoForAceitarOuHomologar() {
            ProcessarAnaliseEmBlocoCommand req = new ProcessarAnaliseEmBlocoCommand(List.of(), AcaoProcesso.DISPONIBILIZAR);
            
            assertThatCode(() -> invokeMethod(processoService, "processarAcoesBlocoAceiteHomologacao", req, new Usuario(), List.of()))
                .doesNotThrowAnyException();
        }
    }
}
