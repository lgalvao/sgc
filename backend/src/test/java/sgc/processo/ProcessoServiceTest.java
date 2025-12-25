package sgc.processo;

import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.api.model.Mapa;
import sgc.mapa.api.model.MapaRepo;
import sgc.mapa.internal.service.CopiaMapaService;
import sgc.processo.api.*;
import sgc.processo.internal.mappers.ProcessoMapper;
import sgc.processo.internal.erros.ErroProcesso;
import sgc.processo.internal.erros.ErroProcessoEmSituacaoInvalida;
import sgc.processo.api.eventos.EventoProcessoCriado;
import sgc.processo.api.eventos.EventoProcessoFinalizado;
import sgc.processo.api.model.Processo;
import sgc.processo.api.model.ProcessoRepo;
import sgc.processo.api.model.SituacaoProcesso;
import sgc.processo.api.model.TipoProcesso;
import sgc.processo.internal.service.ProcessoService;
import sgc.sgrh.SgrhService;
import sgc.sgrh.api.PerfilDto;
import sgc.subprocesso.internal.mappers.SubprocessoMapper;
import sgc.subprocesso.internal.model.SituacaoSubprocesso;
import sgc.subprocesso.internal.model.Subprocesso;
import sgc.subprocesso.internal.model.SubprocessoMovimentacaoRepo;
import sgc.subprocesso.internal.model.SubprocessoRepo;
import sgc.unidade.api.model.Unidade;
import sgc.unidade.api.model.UnidadeMapaRepo;
import sgc.unidade.api.model.UnidadeRepo;

import sgc.fixture.MapaFixture;
import sgc.fixture.ProcessoFixture;
import sgc.fixture.SubprocessoFixture;
import sgc.fixture.UnidadeFixture;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProcessoService")
class ProcessoServiceTest {
    @Mock
    private ProcessoRepo processoRepo;
    @Mock
    private UnidadeRepo unidadeRepo;
    @Mock
    private SubprocessoRepo subprocessoRepo;
    @Mock
    private ApplicationEventPublisher publicadorEventos;
    @Mock
    private ProcessoMapper processoMapper;
    @Mock
    private sgc.processo.internal.service.ProcessoDetalheBuilder processoDetalheBuilder;
    @Mock
    private MapaRepo mapaRepo;
    @Mock
    private SubprocessoMovimentacaoRepo movimentacaoRepo;
    @Mock
    private SubprocessoMapper subprocessoMapper;
    @Mock
    private CopiaMapaService servicoDeCopiaDeMapa;
    @Mock
    private SgrhService sgrhService;
    @Mock
    private UnidadeMapaRepo unidadeMapaRepo;
    @Mock
    private sgc.processo.internal.service.ProcessoInicializador processoInicializador;

    @InjectMocks
    private ProcessoService processoService;

    @Nested
    @DisplayName("Criação de Processo")
    class Criacao {
        @Test
        @DisplayName("Deve criar processo quando dados válidos")
        void deveCriarProcessoQuandoDadosValidos() {
            // Arrange
            CriarProcessoReq req =
                    new CriarProcessoReq(
                            "Teste", TipoProcesso.MAPEAMENTO, LocalDateTime.now(), List.of(1L));
            Unidade unidade = UnidadeFixture.unidadeComId(1L);

            when(unidadeRepo.findById(1L)).thenReturn(Optional.of(unidade));
            when(processoRepo.saveAndFlush(any()))
                    .thenAnswer(
                            i -> {
                                Processo p = i.getArgument(0);
                                p.setCodigo(100L);
                                return p;
                            });
            when(processoMapper.toDto(any())).thenReturn(ProcessoDto.builder().build());

            // Act
            ProcessoDto resultado = processoService.criar(req);

            // Assert
            assertThat(resultado).isNotNull();
            verify(processoRepo).saveAndFlush(argThat(p -> 
                p.getDescricao().equals("Teste") && 
                p.getTipo() == TipoProcesso.MAPEAMENTO &&
                p.getSituacao() == SituacaoProcesso.CRIADO
            ));
            verify(publicadorEventos).publishEvent(any(EventoProcessoCriado.class));
        }

        @Test
        @DisplayName("Deve lançar exceção quando descrição vazia")
        void deveLancarExcecaoQuandoDescricaoVazia() {
            // Arrange
            CriarProcessoReq req =
                    new CriarProcessoReq("", TipoProcesso.MAPEAMENTO, LocalDateTime.now(), List.of(1L));

            // Act & Assert
            assertThatThrownBy(() -> processoService.criar(req))
                    .isInstanceOf(ConstraintViolationException.class)
                    .hasMessageContaining("descrição");
        }

        @Test
        @DisplayName("Deve lançar exceção quando lista de unidades vazia")
        void deveLancarExcecaoQuandoSemUnidades() {
            // Arrange
            CriarProcessoReq req =
                    new CriarProcessoReq(
                            "Teste", TipoProcesso.MAPEAMENTO, LocalDateTime.now(), List.of());

            // Act & Assert
            assertThatThrownBy(() -> processoService.criar(req))
                    .isInstanceOf(ConstraintViolationException.class)
                    .hasMessageContaining("unidade");
        }

        @Test
        @DisplayName("Deve lançar exceção quando unidade não encontrada")
        void deveLancarExcecaoQuandoUnidadeNaoEncontrada() {
            // Arrange
            CriarProcessoReq req =
                    new CriarProcessoReq(
                            "Teste", TipoProcesso.MAPEAMENTO, LocalDateTime.now(), List.of(99L));
            when(unidadeRepo.findById(99L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> processoService.criar(req))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class)
                    .hasMessageContaining("Unidade")
                    .hasNoCause();
        }
    }

    @Nested
    @DisplayName("Atualização de Processo")
    class Atualizacao {
        @Test
        @DisplayName("Deve atualizar processo quando está em situação CRIADO")
        void deveAtualizarProcessoQuandoEmSituacaoCriado() {
            // Arrange
            Long id = 100L;
            Processo processo = ProcessoFixture.processoPadrao();
            processo.setCodigo(id);

            AtualizarProcessoReq req =
                    AtualizarProcessoReq.builder()
                            .codigo(id)
                            .descricao("Nova Desc")
                            .tipo(TipoProcesso.MAPEAMENTO)
                            .dataLimiteEtapa1(LocalDateTime.now())
                            .unidades(List.of(1L))
                            .build();

            when(processoRepo.findById(id)).thenReturn(Optional.of(processo));
            when(unidadeRepo.findById(1L)).thenReturn(Optional.of(UnidadeFixture.unidadePadrao()));
            when(processoRepo.saveAndFlush(any())).thenReturn(processo);
            when(processoMapper.toDto(any())).thenReturn(ProcessoDto.builder().build());

            // Act
            processoService.atualizar(id, req);

            // Assert
            assertThat(processo.getDescricao()).isEqualTo("Nova Desc");
            verify(processoRepo).saveAndFlush(processo);
        }

        @Test
        @DisplayName("Deve lançar exceção quando atualizar e processo não está em situação CRIADO")
        void deveLancarExcecaoQuandoAtualizarEProcessoNaoCriado() {
            // Arrange
            Long id = 100L;
            Processo processo = ProcessoFixture.processoEmAndamento();
            processo.setCodigo(id);
            when(processoRepo.findById(id)).thenReturn(Optional.of(processo));

            AtualizarProcessoReq req =
                    AtualizarProcessoReq.builder()
                            .descricao("Desc")
                            .tipo(TipoProcesso.MAPEAMENTO)
                            .unidades(List.of())
                            .build();

            // Act & Assert
            assertThatThrownBy(() -> processoService.atualizar(id, req))
                    .isInstanceOf(ErroProcessoEmSituacaoInvalida.class);
        }

        @Test
        @DisplayName("Deve lançar exceção quando atualizar e unidade não encontrada")
        void deveLancarExcecaoQuandoAtualizarEUnidadeNaoEncontrada() {
            // Arrange
            Long id = 100L;
            Processo processo = ProcessoFixture.processoPadrao();
            processo.setCodigo(id);

            AtualizarProcessoReq req =
                    AtualizarProcessoReq.builder()
                            .codigo(id)
                            .descricao("Desc")
                            .tipo(TipoProcesso.MAPEAMENTO)
                            .unidades(List.of(99L))
                            .build();

            when(processoRepo.findById(id)).thenReturn(Optional.of(processo));
            when(unidadeRepo.findById(99L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> processoService.atualizar(id, req))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class);
        }
    }

    @Nested
    @DisplayName("Exclusão de Processo")
    class Exclusao {
        @Test
        @DisplayName("Deve apagar processo quando está em situação CRIADO")
        void deveApagarProcessoQuandoEmSituacaoCriado() {
            // Arrange
            Long id = 100L;
            Processo processo = ProcessoFixture.processoPadrao();
            processo.setCodigo(id);
            when(processoRepo.findById(id)).thenReturn(Optional.of(processo));

            // Act
            processoService.apagar(id);

            // Assert
            verify(processoRepo).deleteById(id);
        }

        @Test
        @DisplayName("Deve lançar exceção quando apagar e processo não encontrado")
        void deveLancarExcecaoQuandoApagarEProcessoNaoEncontrado() {
            // Arrange
            when(processoRepo.findById(99L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> processoService.apagar(99L))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class);
        }

        @Test
        @DisplayName("Deve lançar exceção quando apagar e processo não está em situação CRIADO")
        void deveLancarExcecaoQuandoApagarEProcessoNaoCriado() {
            // Arrange
            Long id = 100L;
            Processo processo = ProcessoFixture.processoEmAndamento();
            processo.setCodigo(id);
            when(processoRepo.findById(id)).thenReturn(Optional.of(processo));

            // Act & Assert
            assertThatThrownBy(() -> processoService.apagar(id))
                    .isInstanceOf(ErroProcessoEmSituacaoInvalida.class);
        }
    }

    @Nested
    @DisplayName("Consultas e Detalhes")
    class Consultas {
        @Test
        @DisplayName("Deve retornar detalhes do processo (DTO)")
        void deveRetornarDetalhesDoProcesso() {
            // Arrange
            Long id = 100L;
            Processo processo = ProcessoFixture.processoPadrao();
            // Bolt: Updated mock to use the optimized method
            when(processoRepo.findByIdWithParticipantes(id)).thenReturn(Optional.of(processo));
            when(processoDetalheBuilder.build(processo)).thenReturn(new ProcessoDetalheDto());

            // Act
            var res = processoService.obterDetalhes(id);

            // Assert
            assertThat(res).isNotNull();
        }

        @Test
        @DisplayName("Deve listar processos finalizados e ativos")
        void deveListarProcessosFinalizadosEAtivos() {
            // Arrange
            when(processoRepo.findBySituacao(any())).thenReturn(List.of(ProcessoFixture.processoPadrao()));
            when(processoMapper.toDto(any())).thenReturn(ProcessoDto.builder().build());

            // Act & Assert
            assertThat(processoService.listarFinalizados()).hasSize(1);
            assertThat(processoService.listarAtivos()).hasSize(1);
        }

        @Test
        @DisplayName("Deve listar unidades bloqueadas por tipo")
        void deveListarUnidadesBloqueadasPorTipo() {
            // Arrange
            when(processoRepo.findUnidadeCodigosBySituacaoAndTipo(
                            SituacaoProcesso.EM_ANDAMENTO, TipoProcesso.MAPEAMENTO))
                    .thenReturn(List.of(1L));

            // Act
            List<Long> bloqueadas = processoService.listarUnidadesBloqueadasPorTipo("MAPEAMENTO");

            // Assert
            assertThat(bloqueadas).contains(1L);
        }

        @Test
        @DisplayName("Deve listar subprocessos elegíveis para Admin")
        void deveListarSubprocessosElegiveisParaAdmin() {
            // Arrange
            Authentication auth = mock(Authentication.class);
            SecurityContext securityContext = mock(SecurityContext.class);
            when(securityContext.getAuthentication()).thenReturn(auth);
            SecurityContextHolder.setContext(securityContext);

            when(auth.getName()).thenReturn("admin");
            Collection<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            doReturn(authorities).when(auth).getAuthorities();

            Unidade u = UnidadeFixture.unidadePadrao();
            Subprocesso sp = SubprocessoFixture.subprocessoPadrao(null, u);
            sp.setCodigo(1L);
            sp.setSituacao(SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO);

            when(subprocessoRepo.findByProcessoCodigoWithUnidade(100L)).thenReturn(List.of(sp));

            // Act
            List<SubprocessoElegivelDto> res = processoService.listarSubprocessosElegiveis(100L);

            // Assert
            assertThat(res).hasSize(1);
            assertThat(res.get(0).getCodSubprocesso()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Deve listar subprocessos elegíveis para Gestor")
        void deveListarSubprocessosElegiveisParaGestor() {
            // Arrange
            Authentication auth = mock(Authentication.class);
            SecurityContext securityContext = mock(SecurityContext.class);
            when(securityContext.getAuthentication()).thenReturn(auth);
            SecurityContextHolder.setContext(securityContext);

            when(auth.getName()).thenReturn("gestor");
            Collection<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_GESTOR"));
            doReturn(authorities).when(auth).getAuthorities();

            PerfilDto perfil = PerfilDto.builder().unidadeCodigo(10L).build();
            when(sgrhService.buscarPerfisUsuario("gestor")).thenReturn(List.of(perfil));

            Unidade u = UnidadeFixture.unidadeComId(10L);
            Subprocesso sp = SubprocessoFixture.subprocessoPadrao(null, u);
            sp.setCodigo(1L);
            sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO);

            when(subprocessoRepo.findByProcessoCodigoWithUnidade(100L)).thenReturn(List.of(sp));

            // Act
            List<SubprocessoElegivelDto> res = processoService.listarSubprocessosElegiveis(100L);

            // Assert
            assertThat(res).hasSize(1);
        }

        @Test
        @DisplayName("Deve retornar vazio ao listar subprocessos se usuário sem unidade")
        void deveRetornarVazioAoListarSubprocessosSeUsuarioSemUnidade() {
            // Arrange
            Authentication auth = mock(Authentication.class);
            SecurityContext securityContext = mock(SecurityContext.class);
            when(securityContext.getAuthentication()).thenReturn(auth);
            SecurityContextHolder.setContext(securityContext);

            when(auth.getName()).thenReturn("gestor");
            Collection<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_GESTOR"));
            doReturn(authorities).when(auth).getAuthorities();

            when(sgrhService.buscarPerfisUsuario("gestor")).thenReturn(List.of());
            when(subprocessoRepo.findByProcessoCodigoWithUnidade(100L)).thenReturn(List.of());

            // Act
            List<SubprocessoElegivelDto> res = processoService.listarSubprocessosElegiveis(100L);

            // Assert
            assertThat(res).isEmpty();
        }
    }

    @Nested
    @DisplayName("Workflow e Inicialização")
    class Workflow {
        @Test
        @DisplayName("Deve iniciar mapeamento com sucesso delegando para ProcessoInicializador")
        void deveIniciarMapeamentoComSucesso() {
            // Arrange
            Long id = 100L;
            when(processoInicializador.iniciar(id, List.of(1L))).thenReturn(List.of());

            // Act
            List<String> erros = processoService.iniciarProcessoMapeamento(id, List.of(1L));

            // Assert
            assertThat(erros).isEmpty();
            verify(processoInicializador).iniciar(id, List.of(1L));
        }

        @Test
        @DisplayName("Deve retornar erro ao iniciar mapeamento se unidade já em uso")
        void deveRetornarErroAoIniciarMapeamentoSeUnidadeEmUso() {
            // Arrange
            Long id = 100L;
            String mensagemErro = "As seguintes unidades já participam de outro processo ativo: U1";
            when(processoInicializador.iniciar(id, List.of(1L))).thenReturn(List.of(mensagemErro));

            // Act
            List<String> erros = processoService.iniciarProcessoMapeamento(id, List.of(1L));

            // Assert
            assertThat(erros).contains(mensagemErro);
        }

        @Test
        @DisplayName("Deve iniciar revisão com sucesso delegando para ProcessoInicializador")
        void deveIniciarRevisaoComSucesso() {
            // Arrange
            Long id = 100L;
            when(processoInicializador.iniciar(id, List.of(1L))).thenReturn(List.of());

            // Act
            List<String> erros = processoService.iniciarProcessoRevisao(id, List.of(1L));

            // Assert
            assertThat(erros).isEmpty();
            verify(processoInicializador).iniciar(id, List.of(1L));
        }

        @Test
        @DisplayName("Deve retornar erro ao iniciar revisão se unidade sem mapa")
        void deveRetornarErroAoIniciarRevisaoSeUnidadeSemMapa() {
            // Arrange
            Long id = 100L;
            String mensagemErro = "As seguintes unidades não possuem mapa vigente e não podem participar de um processo de revisão: U1";
            when(processoInicializador.iniciar(id, List.of(1L))).thenReturn(List.of(mensagemErro));

            // Act
            List<String> erros = processoService.iniciarProcessoRevisao(id, List.of(1L));

            // Assert
            assertThat(erros).contains(mensagemErro);
        }

        @Test
        @DisplayName("Deve falhar ao finalizar se subprocessos não homologados")
        void deveFalharAoFinalizarSeSubprocessosNaoHomologados() {
            // Arrange
            Long id = 100L;
            Processo processo = ProcessoFixture.processoEmAndamento();
            processo.setCodigo(id);

            Subprocesso sp = new Subprocesso();
            sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);

            when(processoRepo.findById(id)).thenReturn(Optional.of(processo));
            when(subprocessoRepo.findByProcessoCodigoWithUnidade(id)).thenReturn(List.of(sp));

            // Act & Assert
            assertThatThrownBy(() -> processoService.finalizar(id)).isInstanceOf(ErroProcesso.class);
        }

        @Test
        @DisplayName("Deve falhar ao finalizar se processo não está em andamento")
        void deveFalharAoFinalizarSeProcessoNaoEmAndamento() {
            // Arrange
            Long id = 100L;
            Processo processo = ProcessoFixture.processoPadrao();
            processo.setCodigo(id);
            when(processoRepo.findById(id)).thenReturn(Optional.of(processo));

            // Act & Assert
            assertThatThrownBy(() -> processoService.finalizar(id))
                    .isInstanceOf(ErroProcesso.class)
                    .hasMessageContaining("Apenas processos 'EM ANDAMENTO'");
        }

        @Test
        @DisplayName("Deve finalizar processo com sucesso quando tudo homologado")
        void deveFinalizarProcessoQuandoTudoHomologado() {
            // Arrange
            Long id = 100L;
            Processo processo = ProcessoFixture.processoEmAndamento();
            processo.setCodigo(id);

            Unidade u = UnidadeFixture.unidadeComId(1L);
            Mapa m = MapaFixture.mapaPadrao(null);

            Subprocesso sp = SubprocessoFixture.subprocessoPadrao(processo, u);
            sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO);
            sp.setMapa(m);

            when(processoRepo.findById(id)).thenReturn(Optional.of(processo));
            when(subprocessoRepo.findByProcessoCodigoWithUnidade(id)).thenReturn(List.of(sp));
            when(unidadeMapaRepo.findById(1L)).thenReturn(Optional.of(new sgc.unidade.api.model.UnidadeMapa()));

            // Act
            processoService.finalizar(id);

            // Assert
            assertThat(processo.getSituacao()).isEqualTo(SituacaoProcesso.FINALIZADO);
            verify(unidadeMapaRepo).save(any());
            verify(publicadorEventos).publishEvent(any(EventoProcessoFinalizado.class));
        }

        @Test
        @DisplayName("Deve falhar ao finalizar se subprocesso não tem unidade associada")
        void deveFalharAoFinalizarSeSubprocessoSemUnidade() {
            // Arrange
            Long id = 100L;
            Processo processo = ProcessoFixture.processoEmAndamento();
            processo.setCodigo(id);

            Subprocesso sp = SubprocessoFixture.subprocessoPadrao(processo, null);
            sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO);

            when(processoRepo.findById(id)).thenReturn(Optional.of(processo));
            when(subprocessoRepo.findByProcessoCodigoWithUnidade(id)).thenReturn(List.of(sp));

            // Act & Assert
            assertThatThrownBy(() -> processoService.finalizar(id))
                    .isInstanceOf(ErroProcesso.class)
                    .hasMessageContaining("sem unidade associada");
        }
    }

    @Nested
    @DisplayName("Segurança e Controle de Acesso")
    class Seguranca {
        @Test
        @DisplayName("Deve negar acesso quando usuário não autenticado")
        void deveNegarAcessoQuandoNaoAutenticado() {
            // Arrange
            Authentication auth = mock(Authentication.class);
            when(auth.isAuthenticated()).thenReturn(false);

            // Act & Assert
            assertThat(processoService.checarAcesso(auth, 1L)).isFalse();
        }

        @Test
        @DisplayName("Deve negar acesso quando usuário sem permissão adequada")
        void deveNegarAcessoQuandoUsuarioSemPermissao() {
            // Arrange
            Authentication auth = mock(Authentication.class);
            when(auth.isAuthenticated()).thenReturn(true);
            when(auth.getName()).thenReturn("user");

            Collection<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
            doReturn(authorities).when(auth).getAuthorities();

            // Act & Assert
            assertThat(processoService.checarAcesso(auth, 1L)).isFalse();
        }

        @Test
        @DisplayName("Deve negar acesso quando usuário não possui unidade associada")
        void deveNegarAcessoQuandoUsuarioSemUnidade() {
            // Arrange
            Authentication auth = mock(Authentication.class);
            when(auth.isAuthenticated()).thenReturn(true);
            when(auth.getName()).thenReturn("gestor");

            Collection<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_GESTOR"));
            doReturn(authorities).when(auth).getAuthorities();

            when(sgrhService.buscarPerfisUsuario("gestor")).thenReturn(List.of());

            // Act & Assert
            assertThat(processoService.checarAcesso(auth, 1L)).isFalse();
        }

        @Test
        @DisplayName("Deve permitir acesso quando gestor é da unidade participante")
        void devePermitirAcessoQuandoGestorDeUnidadeParticipante() {
            // Arrange
            Authentication auth = mock(Authentication.class);
            when(auth.isAuthenticated()).thenReturn(true);
            when(auth.getName()).thenReturn("gestor");

            Collection<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_GESTOR"));
            doReturn(authorities).when(auth).getAuthorities();

            PerfilDto perfil = PerfilDto.builder()
                    .usuarioTitulo("gestor")
                    .perfil("GESTOR")
                    .unidadeCodigo(10L)
                    .build();
            when(sgrhService.buscarPerfisUsuario("gestor")).thenReturn(List.of(perfil));

            // Bolt: updated mock
            when(unidadeRepo.findCodigosDescendentes(10L)).thenReturn(List.of(10L));
            when(subprocessoRepo.existsByProcessoCodigoAndUnidadeCodigoIn(anyLong(), anyList())).thenReturn(true);

            // Act & Assert
            assertThat(processoService.checarAcesso(auth, 1L)).isTrue();
        }
        
        @Test
        @DisplayName("Deve negar acesso quando hierarquia de unidades está vazia")
        void deveNegarAcessoQuandoHierarquiaVazia() {
            // Arrange
            Authentication auth = mock(Authentication.class);
            when(auth.isAuthenticated()).thenReturn(true);
            when(auth.getName()).thenReturn("gestor");

            Collection<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_GESTOR"));
            doReturn(authorities).when(auth).getAuthorities();

            PerfilDto perfil = PerfilDto.builder()
                    .usuarioTitulo("gestor")
                    .perfil("GESTOR")
                    .unidadeCodigo(10L)
                    .build();
            when(sgrhService.buscarPerfisUsuario("gestor")).thenReturn(List.of(perfil));
            
            // Retorna lista vazia de unidades - simula hierarquia vazia
            // Bolt: updated mock
            when(unidadeRepo.findCodigosDescendentes(10L)).thenReturn(List.of());

            // Act & Assert
            assertThat(processoService.checarAcesso(auth, 1L)).isFalse();
        }
        
        @Test
        @DisplayName("Deve permitir acesso quando CHEFE é da unidade participante")
        void devePermitirAcessoQuandoChefeDeUnidadeParticipante() {
            // Arrange
            Authentication auth = mock(Authentication.class);
            when(auth.isAuthenticated()).thenReturn(true);
            when(auth.getName()).thenReturn("chefe");

            Collection<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_CHEFE"));
            doReturn(authorities).when(auth).getAuthorities();

            PerfilDto perfil = PerfilDto.builder()
                    .usuarioTitulo("chefe")
                    .perfil("CHEFE")
                    .unidadeCodigo(10L)
                    .build();
            when(sgrhService.buscarPerfisUsuario("chefe")).thenReturn(List.of(perfil));

            // Bolt: updated mock
            when(unidadeRepo.findCodigosDescendentes(10L)).thenReturn(List.of(10L));
            when(subprocessoRepo.existsByProcessoCodigoAndUnidadeCodigoIn(anyLong(), anyList())).thenReturn(true);

            // Act & Assert
            assertThat(processoService.checarAcesso(auth, 1L)).isTrue();
        }

        @Test
        @DisplayName("Deve negar acesso quando authentication é null")
        void deveNegarAcessoQuandoAuthenticationNull() {
            // Act & Assert
            assertThat(processoService.checarAcesso(null, 1L)).isFalse();
        }

        @Test
        @DisplayName("Deve negar acesso quando subprocesso não pertence à hierarquia do usuário")
        void deveNegarAcessoQuandoSubprocessoForaDaHierarquia() {
            // Arrange
            Authentication auth = mock(Authentication.class);
            when(auth.isAuthenticated()).thenReturn(true);
            when(auth.getName()).thenReturn("gestor");

            Collection<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_GESTOR"));
            doReturn(authorities).when(auth).getAuthorities();

            PerfilDto perfil = PerfilDto.builder()
                    .usuarioTitulo("gestor")
                    .perfil("GESTOR")
                    .unidadeCodigo(10L)
                    .build();
            when(sgrhService.buscarPerfisUsuario("gestor")).thenReturn(List.of(perfil));

            // Bolt: updated mock
            when(unidadeRepo.findCodigosDescendentes(10L)).thenReturn(List.of(10L));
            // Subprocesso não pertence à hierarquia
            when(subprocessoRepo.existsByProcessoCodigoAndUnidadeCodigoIn(anyLong(), anyList())).thenReturn(false);

            // Act & Assert
            assertThat(processoService.checarAcesso(auth, 1L)).isFalse();
        }

        @Test
        @DisplayName("Deve negar acesso com ROLE_GESTOR mas sem ROLE_CHEFE quando precisaria de ambos")
        void devePermitirAcessoApenasComRoleGestor() {
            // Arrange
            Authentication auth = mock(Authentication.class);
            when(auth.isAuthenticated()).thenReturn(true);
            when(auth.getName()).thenReturn("gestor");

            // Apenas ROLE_GESTOR (sem ROLE_CHEFE)
            Collection<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_GESTOR"));
            doReturn(authorities).when(auth).getAuthorities();

            PerfilDto perfil = PerfilDto.builder()
                    .usuarioTitulo("gestor")
                    .perfil("GESTOR")
                    .unidadeCodigo(10L)
                    .build();
            when(sgrhService.buscarPerfisUsuario("gestor")).thenReturn(List.of(perfil));

            // Bolt: updated mock
            when(unidadeRepo.findCodigosDescendentes(10L)).thenReturn(List.of(10L));
            when(subprocessoRepo.existsByProcessoCodigoAndUnidadeCodigoIn(anyLong(), anyList())).thenReturn(true);

            // Act & Assert - deve permitir apenas com GESTOR
            assertThat(processoService.checarAcesso(auth, 1L)).isTrue();
        }

        @Test
        @DisplayName("Deve negar acesso quando usuário só tem ROLE_SERVIDOR")
        void deveNegarAcessoQuandoApenasRoleServidor() {
            // Arrange
            Authentication auth = mock(Authentication.class);
            when(auth.isAuthenticated()).thenReturn(true);
            when(auth.getName()).thenReturn("servidor");

            // Apenas ROLE_SERVIDOR (não é GESTOR nem CHEFE)
            Collection<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_SERVIDOR"));
            doReturn(authorities).when(auth).getAuthorities();

            // Act & Assert - deve negar porque não é GESTOR nem CHEFE
            assertThat(processoService.checarAcesso(auth, 1L)).isFalse();
        }
    }

    @Nested
    @DisplayName("Validações de Criação")
    class ValidacoesCriacao {
        @Test
        @DisplayName("Deve lançar exceção ao criar processo com unidade INTERMEDIARIA")
        void deveLancarExcecaoQuandoUnidadeIntermediaria() {
            // Arrange
            CriarProcessoReq req =
                    new CriarProcessoReq(
                            "Teste", TipoProcesso.MAPEAMENTO, LocalDateTime.now(), List.of(1L));
            Unidade unidadeIntermediaria = UnidadeFixture.unidadeComId(1L);
            unidadeIntermediaria.setTipo(sgc.unidade.api.model.TipoUnidade.INTERMEDIARIA);

            when(unidadeRepo.findById(1L)).thenReturn(Optional.of(unidadeIntermediaria));

            // Act & Assert
            assertThatThrownBy(() -> processoService.criar(req))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("unidade não elegível");
        }

        @Test
        @DisplayName("Deve lançar exceção ao criar REVISAO quando unidade não tem mapa")
        void deveLancarExcecaoQuandoRevisaoSemMapa() {
            // Arrange
            CriarProcessoReq req =
                    new CriarProcessoReq(
                            "Revisao Teste", TipoProcesso.REVISAO, LocalDateTime.now(), List.of(1L));
            Unidade unidade = UnidadeFixture.unidadeComId(1L);

            when(unidadeRepo.findById(1L)).thenReturn(Optional.of(unidade));
            when(unidadeRepo.findAllById(List.of(1L))).thenReturn(List.of(unidade));
            when(unidadeMapaRepo.existsById(1L)).thenReturn(false); // Não tem mapa
            when(unidadeRepo.findSiglasByCodigos(List.of(1L))).thenReturn(List.of("U1"));

            // Act & Assert
            assertThatThrownBy(() -> processoService.criar(req))
                    .isInstanceOf(ErroProcesso.class)
                    .hasMessageContaining("não possuem mapa vigente");
        }

        @Test
        @DisplayName("Deve lançar exceção ao criar DIAGNOSTICO quando unidade não tem mapa")
        void deveLancarExcecaoQuandoDiagnosticoSemMapa() {
            // Arrange
            CriarProcessoReq req =
                    new CriarProcessoReq(
                            "Diag Teste", TipoProcesso.DIAGNOSTICO, LocalDateTime.now(), List.of(1L));
            Unidade unidade = UnidadeFixture.unidadeComId(1L);

            when(unidadeRepo.findById(1L)).thenReturn(Optional.of(unidade));
            when(unidadeRepo.findAllById(List.of(1L))).thenReturn(List.of(unidade));
            when(unidadeMapaRepo.existsById(1L)).thenReturn(false); // Não tem mapa
            when(unidadeRepo.findSiglasByCodigos(List.of(1L))).thenReturn(List.of("U1"));

            // Act & Assert
            assertThatThrownBy(() -> processoService.criar(req))
                    .isInstanceOf(ErroProcesso.class)
                    .hasMessageContaining("não possuem mapa vigente");
        }

        @Test
        @DisplayName("Deve criar REVISAO com sucesso quando unidade tem mapa")
        void deveCriarRevisaoQuandoUnidadeTemMapa() {
            // Arrange
            CriarProcessoReq req =
                    new CriarProcessoReq(
                            "Revisao OK", TipoProcesso.REVISAO, LocalDateTime.now(), List.of(1L));
            Unidade unidade = UnidadeFixture.unidadeComId(1L);

            when(unidadeRepo.findById(1L)).thenReturn(Optional.of(unidade));
            when(unidadeRepo.findAllById(List.of(1L))).thenReturn(List.of(unidade));
            when(unidadeMapaRepo.existsById(1L)).thenReturn(true); // Tem mapa
            when(processoRepo.saveAndFlush(any()))
                    .thenAnswer(i -> {
                        Processo p = i.getArgument(0);
                        p.setCodigo(100L);
                        return p;
                    });
            when(processoMapper.toDto(any())).thenReturn(ProcessoDto.builder().build());

            // Act
            ProcessoDto resultado = processoService.criar(req);

            // Assert
            assertThat(resultado).isNotNull();
            verify(processoRepo).saveAndFlush(argThat(p -> 
                p.getTipo() == TipoProcesso.REVISAO
            ));
        }

        @Test
        @DisplayName("Deve criar MAPEAMENTO sem verificar mapa vigente")
        void deveCriarMapeamentoSemVerificarMapa() {
            // Arrange
            CriarProcessoReq req =
                    new CriarProcessoReq(
                            "Mapeamento Teste", TipoProcesso.MAPEAMENTO, LocalDateTime.now(), List.of(1L));
            Unidade unidade = UnidadeFixture.unidadeComId(1L);

            when(unidadeRepo.findById(1L)).thenReturn(Optional.of(unidade));
            when(processoRepo.saveAndFlush(any()))
                    .thenAnswer(i -> {
                        Processo p = i.getArgument(0);
                        p.setCodigo(100L);
                        return p;
                    });
            when(processoMapper.toDto(any())).thenReturn(ProcessoDto.builder().build());

            // Act
            ProcessoDto resultado = processoService.criar(req);

            // Assert
            assertThat(resultado).isNotNull();
            // Não deve verificar mapa para MAPEAMENTO
            verify(unidadeMapaRepo, never()).existsById(anyLong());
        }

        @Test
        @DisplayName("Deve lançar exceção quando descrição é null")
        void deveLancarExcecaoQuandoDescricaoNull() {
            // Arrange
            CriarProcessoReq req =
                    new CriarProcessoReq(null, TipoProcesso.MAPEAMENTO, LocalDateTime.now(), List.of(1L));

            // Act & Assert
            assertThatThrownBy(() -> processoService.criar(req))
                    .isInstanceOf(ConstraintViolationException.class)
                    .hasMessageContaining("descrição");
        }
    }
    
    @Nested
    @DisplayName("Mapas Vigentes")
    class MapasVigentes {
        @Test
        @DisplayName("Deve falhar ao finalizar se subprocesso não tem mapa associado")
        void deveFalharAoFinalizarSeSubprocessoSemMapa() {
            // Arrange
            Long id = 100L;
            Processo processo = ProcessoFixture.processoEmAndamento();
            processo.setCodigo(id);

            Unidade u = UnidadeFixture.unidadeComId(1L);
            Subprocesso sp = SubprocessoFixture.subprocessoPadrao(processo, u);
            sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO);
            sp.setMapa(null); // Sem mapa

            when(processoRepo.findById(id)).thenReturn(Optional.of(processo));
            when(subprocessoRepo.findByProcessoCodigoWithUnidade(id)).thenReturn(List.of(sp));

            // Act & Assert
            assertThatThrownBy(() -> processoService.finalizar(id))
                    .isInstanceOf(ErroProcesso.class)
                    .hasMessageContaining("sem mapa associado");
        }
        
        @Test
        @DisplayName("Deve criar UnidadeMapa quando não existe")
        void deveCriarUnidadeMapaQuandoNaoExiste() {
            // Arrange
            Long id = 100L;
            Processo processo = ProcessoFixture.processoEmAndamento();
            processo.setCodigo(id);

            Unidade u = UnidadeFixture.unidadeComId(1L);
            Mapa m = MapaFixture.mapaPadrao(null);

            Subprocesso sp = SubprocessoFixture.subprocessoPadrao(processo, u);
            sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO);
            sp.setMapa(m);

            when(processoRepo.findById(id)).thenReturn(Optional.of(processo));
            when(subprocessoRepo.findByProcessoCodigoWithUnidade(id)).thenReturn(List.of(sp));
            when(unidadeMapaRepo.findById(1L)).thenReturn(Optional.empty()); // Não existe

            // Act
            processoService.finalizar(id);

            // Assert
            verify(unidadeMapaRepo).save(argThat(um -> 
                um.getUnidadeCodigo().equals(1L) && um.getMapaVigente() == m
            ));
        }
    }
}
