package sgc.processo.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.springframework.data.domain.*;
import org.springframework.security.core.*;
import sgc.alerta.*;
import sgc.comum.Mensagens;
import sgc.comum.erros.ErroValidacao;
import sgc.comum.model.*;
import sgc.fixture.*;
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
import static sgc.processo.model.AcaoProcesso.*;
import static sgc.seguranca.AcaoPermissao.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProcessoService Test suite")
class ProcessoServiceTest {
    @InjectMocks
    private ProcessoService processoService;
    @Mock
    private ProcessoRepo processoRepo;
    @Mock
    private ComumRepo repo;
    @Mock
    private UnidadeService unidadeService;
    @Mock
    private SubprocessoService subprocessoService;
    @Mock
    private SubprocessoValidacaoService validacaoService;
    @Mock
    private UsuarioFacade usuarioService;
    @Mock
    private AlertaFacade servicoAlertas;
    @Mock
    private EmailService emailService;
    @Mock
    private EmailModelosService emailModelosService;
    @Mock
    private SgcPermissionEvaluator permissionEvaluator;
    @Mock
    private SubprocessoTransicaoService transicaoService;

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
        @DisplayName("executarAcaoEmBloco ignora ação null na categorização")
        void executarAcaoEmBloco_IgnoraAcaoNull() {
            Long codProcesso = 1L;
            AcaoEmBlocoRequest req = new AcaoEmBlocoRequest(
                    List.of(10L),
                    ACEITAR, // Mock a valid action for the initial check, then we'll test categorization
                    LocalDate.now()
            );

            Usuario usuario = new Usuario();
            when(usuarioService.usuarioAutenticado()).thenReturn(usuario);

            Subprocesso sub = Subprocesso.builder()
                    .codigo(100L)
                    .unidade(Unidade.builder().codigo(10L).build())
                    .situacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO)
                    .build();

            when(subprocessoService.listarEntidadesPorProcessoEUnidades(codProcesso, req.unidadeCodigos()))
                    .thenReturn(List.of(sub));

            AcaoEmBlocoRequest reqNull = new AcaoEmBlocoRequest(List.of(10L), null, LocalDate.now());
            processoService.executarAcaoEmBloco(codProcesso, reqNull);

            verify(transicaoService, never()).aceitarCadastroEmBloco(any(), any());
            verify(transicaoService, never()).homologarCadastroEmBloco(any(), any());
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
            
            Processo p = new Processo();
            p.setCodigo(id);
            p.setSituacao(SituacaoProcesso.CRIADO);
            p.setTipo(TipoProcesso.MAPEAMENTO);
            Unidade uni = new Unidade();
            uni.setSituacao(SituacaoUnidade.ATIVA);
            p.adicionarParticipantes(Set.of(uni));
            
            when(repo.buscar(Processo.class, id)).thenReturn(p);
            Unidade uniAdmin = new Unidade();
            uniAdmin.setSituacao(SituacaoUnidade.ATIVA);
            when(repo.buscarPorSigla(Unidade.class, "ADMIN")).thenReturn(uniAdmin);

            processoService.iniciar(id, List.of(), usuario);

            verify(processoRepo).save(any(Processo.class));
        }

        @Test
        @DisplayName("Deve iniciar revisao com sucesso e salvar")
        void deveIniciarRevisaoComSucesso() {
            Long id = 100L;
            Usuario usuario = new Usuario();

            Processo p = new Processo();
            p.setCodigo(id);
            p.setSituacao(SituacaoProcesso.CRIADO);
            p.setTipo(TipoProcesso.REVISAO);
            Unidade uni = new Unidade();
            uni.setCodigo(1L);
            uni.setSituacao(SituacaoUnidade.ATIVA);
            p.adicionarParticipantes(Set.of(uni));

            when(repo.buscar(Processo.class, id)).thenReturn(p);
            when(unidadeService.porCodigos(anyList())).thenReturn(List.of(uni));
            when(unidadeService.buscarPorCodigo(1L)).thenReturn(uni);
            when(unidadeService.verificarMapaVigente(1L)).thenReturn(true);
            
            UnidadeMapa um = new UnidadeMapa();
            um.setUnidadeCodigo(1L);
            when(unidadeService.buscarMapasPorUnidades(anyList())).thenReturn(List.of(um));

            Unidade uniAdmin = new Unidade();
            uniAdmin.setSituacao(SituacaoUnidade.ATIVA);
            when(repo.buscarPorSigla(Unidade.class, "ADMIN")).thenReturn(uniAdmin);

            processoService.iniciar(id, List.of(1L), usuario);

            verify(processoRepo).save(any(Processo.class));
            verify(subprocessoService).criarParaRevisao(eq(p), eq(uni), eq(um), eq(uniAdmin), eq(usuario));
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
            Unidade uni = new Unidade();
            uni.setCodigo(1L);
            uni.setSituacao(SituacaoUnidade.ATIVA);
            p.adicionarParticipantes(Set.of(uni));

            when(repo.buscar(Processo.class, id)).thenReturn(p);
            // Simular que a unidade já está em outro processo
            when(processoRepo.listarUnidadesEmProcessoAtivo(eq(SituacaoProcesso.EM_ANDAMENTO), anyList()))
                    .thenReturn(List.of(1L));

            assertThatThrownBy(() -> processoService.iniciar(id, List.of(), usuario))
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
            Unidade uni = new Unidade();
            uni.setCodigo(1L);
            uni.setSituacao(SituacaoUnidade.ATIVA);
            p.adicionarParticipantes(Set.of(uni));

            when(repo.buscar(Processo.class, id)).thenReturn(p);
            when(unidadeService.porCodigos(anyList())).thenReturn(List.of(uni));
            // Simular que a unidade não tem mapa vigente
            when(unidadeService.verificarMapaVigente(1L)).thenReturn(false);
            when(unidadeService.buscarSiglasPorCodigos(anyList())).thenReturn(List.of("U1"));

            assertThatThrownBy(() -> processoService.iniciar(id, List.of(1L), usuario))
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
            when(unidadeService.porCodigos(anyList())).thenReturn(List.of());
            when(validacaoService.validarSubprocessosParaFinalizacao(id))
                    .thenReturn(ValidationResult.ofValido());
            
            processoService.finalizar(id);
            verify(processoRepo).save(p);
            assertThat(p.getSituacao()).isEqualTo(SituacaoProcesso.FINALIZADO);
        }
    }

    @Nested
    @DisplayName("Lembretes")
    class Lembretes {
        @Test
        @DisplayName("Deve emitir alerta ao enviar lembrete")
        void deveEnviarLembrete() {
            Long codProcesso = 1L;
            Long codUnidade = 10L;
            
            Processo p = new Processo();
            p.setCodigo(codProcesso);
            p.setDescricao("Processo Teste");
            Unidade u = new Unidade();
            u.setCodigo(codUnidade);
            u.setSigla("U10");
            u.setTituloTitular("titular1");
            u.setSituacao(SituacaoUnidade.ATIVA);
            p.adicionarParticipantes(Set.of(u));
            
            when(processoRepo.buscarPorCodigoComParticipantes(codProcesso)).thenReturn(Optional.of(p));
            when(unidadeService.buscarPorCodigo(codUnidade)).thenReturn(u);
            when(emailModelosService.criarEmailLembretePrazo(anyString(), anyString(), any())).thenReturn("<html>body</html>");
            
            Usuario titular = new Usuario();
            titular.setEmail("titular@teste.com");
            when(usuarioService.buscarPorLogin("titular1")).thenReturn(titular);
            
            processoService.enviarLembrete(codProcesso, codUnidade);
            
            verify(emailService).enviarEmailHtml(eq("titular@teste.com"), anyString(), anyString());
            verify(servicoAlertas).criarAlertaAdmin(p, u, "Lembrete: Prazo do processo Processo Teste encerra em N/A");
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

            Unidade u = new Unidade();
            u.setCodigo(10L);
            u.setSigla("U10");
            u.setSituacao(SituacaoUnidade.ATIVA);
            p.adicionarParticipantes(Set.of(u));

            when(repo.buscar(Processo.class, codProcesso)).thenReturn(p);
            when(permissionEvaluator.verificarPermissao(eq(usuario), eq(p), eq(AcaoPermissao.FINALIZAR_PROCESSO))).thenReturn(true);

            Subprocesso sp = new Subprocesso();
            sp.setCodigo(100L);
            sp.setUnidade(u);
            sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
            when(subprocessoService.listarEntidadesPorProcessoComUnidade(codProcesso)).thenReturn(List.of(sp));
            when(subprocessoService.obterLocalizacaoAtual(sp)).thenReturn(u);
            when(validacaoService.validarSubprocessosParaFinalizacao(codProcesso)).thenReturn(ValidationResult.ofValido());

            ProcessoDetalheDto result = processoService.obterDetalhesCompleto(codProcesso, usuario, false);

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
            s1.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
            Unidade u1 = new Unidade();
            u1.setCodigo(10L);
            s1.setUnidade(u1);

            Subprocesso s2 = new Subprocesso();
            s2.setSituacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO);
            Unidade u2 = new Unidade();
            u2.setCodigo(20L);
            s2.setUnidade(u2);

            Subprocesso s3 = new Subprocesso();
            s3.setSituacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO); // Não elegível
            Unidade u3 = new Unidade();
            u3.setCodigo(30L);
            s3.setUnidade(u3);

            when(subprocessoService.listarEntidadesPorProcessoEUnidades(eq(codProcesso), anyList())).thenReturn(List.of(s1, s2, s3));
            when(permissionEvaluator.verificarPermissao(eq(usuario), eq(s1), eq(AcaoPermissao.ACEITAR_CADASTRO))).thenReturn(true);
            when(permissionEvaluator.verificarPermissao(eq(usuario), eq(s2), eq(AcaoPermissao.ACEITAR_MAPA))).thenReturn(true);
            when(subprocessoService.obterLocalizacaoAtual(any())).thenReturn(u1);

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

            Unidade uPai = new Unidade();
            uPai.setCodigo(10L);
            uPai.setSigla("PAI");
            uPai.setSituacao(SituacaoUnidade.ATIVA);
            p.adicionarParticipantes(Set.of(uPai));

            Unidade uFilho = new Unidade();
            uFilho.setCodigo(20L);
            uFilho.setSigla("FILHO");
            uFilho.setSituacao(SituacaoUnidade.ATIVA);
            uFilho.setUnidadeSuperior(uPai);
            p.adicionarParticipantes(Set.of(uFilho));

            Unidade uSemSub = new Unidade();
            uSemSub.setCodigo(30L);
            uSemSub.setSigla("SEMSUB");
            uSemSub.setSituacao(SituacaoUnidade.ATIVA);
            p.adicionarParticipantes(Set.of(uSemSub));

            when(repo.buscar(Processo.class, codProcesso)).thenReturn(p);
            when(unidadeService.todasComHierarquia()).thenReturn(List.of(uPai, uFilho, uSemSub));

            Subprocesso sp = new Subprocesso();
            sp.setCodigo(100L);
            sp.setUnidade(uPai);
            sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
            // Filho não tem subprocesso para cobrir branch sp != null

            when(subprocessoService.listarEntidadesPorProcessoComUnidade(codProcesso)).thenReturn(List.of(sp));
            when(subprocessoService.obterLocalizacaoAtual(sp)).thenReturn(uPai);

            ProcessoDetalheDto result = processoService.obterDetalhesCompleto(codProcesso, usuario, false);

            assertThat(result.getUnidades()).isNotEmpty();
            assertThat(result.getUnidades().getFirst().getFilhos()).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("Ações em Bloco")
    class AcoesEmBloco {
        @Test
        @DisplayName("Deve falhar ao executar ação em bloco sem unidades")
        void deveFalharAoExecutarAcaoEmBlocoSemUnidades() {
            AcaoEmBlocoRequest req = new AcaoEmBlocoRequest(List.of(), ACEITAR, LocalDate.now());
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

            when(subprocessoService.listarEntidadesPorProcessoEUnidades(eq(1L), anyList()))
                    .thenReturn(List.of(sCad, sVal));

            // Teste ACEITAR
            AcaoEmBlocoRequest reqAceitar = new AcaoEmBlocoRequest(List.of(10L, 20L), ACEITAR, LocalDate.now());
            processoService.executarAcaoEmBloco(1L, reqAceitar);
            verify(transicaoService).aceitarCadastroEmBloco(List.of(10L), usuario);
            verify(transicaoService).aceitarValidacaoEmBloco(List.of(20L), usuario);

            // Teste HOMOLOGAR
            AcaoEmBlocoRequest reqHomologar = new AcaoEmBlocoRequest(List.of(10L, 20L), HOMOLOGAR, LocalDate.now());
            processoService.executarAcaoEmBloco(1L, reqHomologar);
            verify(transicaoService).homologarCadastroEmBloco(List.of(10L), usuario);
            verify(transicaoService).homologarValidacaoEmBloco(List.of(20L), usuario);
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

            Unidade uni = new Unidade();
            uni.setSituacao(SituacaoUnidade.ATIVA);
            when(unidadeService.buscarPorCodigo(1L)).thenReturn(uni);
            when(processoRepo.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));

            Processo resultado = processoService.criar(req);

            assertThat(resultado).isNotNull();
            assertThat(resultado.getDescricao()).isEqualTo("Teste");
            verify(processoRepo).saveAndFlush(any());
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
            Unidade u = new Unidade();
            u.setCodigo(10L);
            u.setSituacao(SituacaoUnidade.ATIVA);
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
            when(unidadeService.todasComHierarquia()).thenReturn(List.of(u));

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
            when(repo.buscar(Processo.class, id)).thenReturn(processo);

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
                AcaoEmBlocoRequest req = new AcaoEmBlocoRequest(
                        List.of(1L, 2L, 3L),
                        AcaoProcesso.DISPONIBILIZAR,
                        dataLimite
                );

                Subprocesso sp1 = Subprocesso.builder().codigo(1001L).unidade(Unidade.builder().codigo(1L).build()).build();
                Subprocesso sp2 = Subprocesso.builder().codigo(1002L).unidade(Unidade.builder().codigo(2L).build()).build();
                Subprocesso sp3 = Subprocesso.builder().codigo(1003L).unidade(Unidade.builder().codigo(3L).build()).build();
                when(subprocessoService.listarEntidadesPorProcessoEUnidades(100L, List.of(1L, 2L, 3L))).thenReturn(List.of(sp1, sp2, sp3));
                doReturn(true).when(permissionEvaluator).verificarPermissao(eq(usuario), any(), eq(DISPONIBILIZAR_MAPA));

                processoService.executarAcaoEmBloco(100L, req);

                ArgumentCaptor<DisponibilizarMapaRequest> captor =
                        ArgumentCaptor.forClass(DisponibilizarMapaRequest.class);
                verify(transicaoService).disponibilizarMapaEmBloco(
                        eq(List.of(1001L, 1002L, 1003L)),
                        captor.capture(),
                        eq(usuario)
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

                when(subprocessoService.listarEntidadesPorProcessoEUnidades(100L, List.of(10L, 20L))).thenReturn(List.of(sp1, sp2));

                AcaoEmBlocoRequest req = new AcaoEmBlocoRequest(
                        List.of(10L, 20L),
                        ACEITAR,
                        null
                );

                processoService.executarAcaoEmBloco(100L, req);

                verify(transicaoService).aceitarCadastroEmBloco(List.of(1L, 2L), usuario);
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

                when(subprocessoService.listarEntidadesPorProcessoEUnidades(100L, List.of(10L))).thenReturn(List.of(sp1));

                AcaoEmBlocoRequest req = new AcaoEmBlocoRequest(
                        List.of(10L),
                        ACEITAR,
                        null
                );

                processoService.executarAcaoEmBloco(100L, req);

                verify(transicaoService).aceitarValidacaoEmBloco(List.of(1L), usuario);
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

                when(subprocessoService.listarEntidadesPorProcessoEUnidades(100L, List.of(10L))).thenReturn(List.of(sp1));

                AcaoEmBlocoRequest req = new AcaoEmBlocoRequest(
                        List.of(10L),
                        HOMOLOGAR,
                        null
                );

                processoService.executarAcaoEmBloco(100L, req);

                verify(transicaoService).homologarCadastroEmBloco(List.of(1L), usuario);
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

                when(subprocessoService.listarEntidadesPorProcessoEUnidades(100L, List.of(10L))).thenReturn(List.of(sp1));

                AcaoEmBlocoRequest req = new AcaoEmBlocoRequest(
                        List.of(10L),
                        HOMOLOGAR,
                        null
                );

                processoService.executarAcaoEmBloco(100L, req);

                verify(transicaoService).homologarValidacaoEmBloco(List.of(1L), usuario);
            }
        }
    }
}
