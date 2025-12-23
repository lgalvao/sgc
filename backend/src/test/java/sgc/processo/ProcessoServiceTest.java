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
import sgc.mapa.model.Mapa;
import sgc.mapa.model.MapaRepo;
import sgc.mapa.service.CopiaMapaService;
import sgc.processo.dto.*;
import sgc.processo.dto.mappers.ProcessoMapper;
import sgc.processo.erros.ErroProcesso;
import sgc.processo.erros.ErroProcessoEmSituacaoInvalida;
import sgc.processo.eventos.EventoProcessoCriado;
import sgc.processo.eventos.EventoProcessoFinalizado;
import sgc.processo.model.Processo;
import sgc.processo.model.ProcessoRepo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.processo.service.ProcessoService;
import sgc.sgrh.SgrhService;
import sgc.sgrh.api.PerfilDto;
import sgc.subprocesso.mapper.SubprocessoMapper;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoMovimentacaoRepo;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.unidade.internal.model.Unidade;
import sgc.unidade.internal.model.UnidadeMapaRepo;
import sgc.unidade.internal.model.UnidadeRepo;

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
    private sgc.processo.service.ProcessoDetalheBuilder processoDetalheBuilder;
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
    private sgc.processo.service.ProcessoInicializador processoInicializador;

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
            when(processoRepo.findById(id)).thenReturn(Optional.of(processo));
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
        @DisplayName("Deve falhar ao finalizar se houver subprocessos não homologados")
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
            when(unidadeMapaRepo.findById(1L)).thenReturn(Optional.of(new sgc.unidade.internal.model.UnidadeMapa()));

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

            when(subprocessoRepo.existsByProcessoCodigoAndUnidadeCodigoIn(anyLong(), anyList())).thenReturn(true);

            // Act & Assert
            assertThat(processoService.checarAcesso(auth, 1L)).isTrue();
        }
    }
}
