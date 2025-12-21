package sgc.processo;

import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

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
import sgc.subprocesso.mapper.SubprocessoMapper;
import sgc.processo.erros.ErroProcesso;
import sgc.processo.erros.ErroProcessoEmSituacaoInvalida;
import sgc.processo.eventos.EventoProcessoCriado;
import sgc.processo.eventos.EventoProcessoFinalizado;
import sgc.processo.model.Processo;
import sgc.processo.model.ProcessoRepo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.processo.service.ProcessoService;
import sgc.sgrh.dto.PerfilDto;
import sgc.sgrh.SgrhService;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoMovimentacaoRepo;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.unidade.model.Unidade;
import sgc.unidade.model.UnidadeMapaRepo;
import sgc.unidade.model.UnidadeRepo;

import sgc.fixture.ProcessoFixture;
import sgc.fixture.SubprocessoFixture;
import sgc.fixture.UnidadeFixture;
import sgc.fixture.MapaFixture;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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

    @Test
    @DisplayName("Criar processo deve persistir e publicar evento")
    void criar() {
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

        processoService.criar(req);

        verify(processoRepo).saveAndFlush(any());
        verify(publicadorEventos).publishEvent(any(EventoProcessoCriado.class));
    }

    @Test
    @DisplayName("Criar deve lançar exceção se descrição vazia")
    void criarDescricaoVazia() {
        CriarProcessoReq req =
                new CriarProcessoReq("", TipoProcesso.MAPEAMENTO, LocalDateTime.now(), List.of(1L));
        assertThatThrownBy(() -> processoService.criar(req))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    @DisplayName("Criar deve lançar exceção se lista de unidades vazia")
    void criarSemUnidades() {
        CriarProcessoReq req =
                new CriarProcessoReq(
                        "Teste", TipoProcesso.MAPEAMENTO, LocalDateTime.now(), List.of());
        assertThatThrownBy(() -> processoService.criar(req))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    @DisplayName("Criar deve lançar exceção se unidade não encontrada")
    void criarUnidadeNaoEncontrada() {
        CriarProcessoReq req =
                new CriarProcessoReq(
                        "Teste", TipoProcesso.MAPEAMENTO, LocalDateTime.now(), List.of(99L));
        when(unidadeRepo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> processoService.criar(req))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }

    @Test
    @DisplayName("Atualizar deve modificar processo se estiver CRIADO")
    void atualizar() {
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

        processoService.atualizar(id, req);

        assertThat(processo.getDescricao()).isEqualTo("Nova Desc");
        verify(processoRepo).saveAndFlush(processo);
    }

    @Test
    @DisplayName("Atualizar deve falhar se não estiver CRIADO")
    void atualizarInvalido() {
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

        assertThatThrownBy(() -> processoService.atualizar(id, req))
                .isInstanceOf(ErroProcessoEmSituacaoInvalida.class);
    }

    @Test
    @DisplayName("Atualizar deve falhar se unidade não encontrada")
    void atualizarUnidadeNaoEncontrada() {
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

        assertThatThrownBy(() -> processoService.atualizar(id, req))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }

    @Test
    @DisplayName("Apagar deve remover se estiver CRIADO")
    void apagar() {
        Long id = 100L;
        Processo processo = ProcessoFixture.processoPadrao();
        processo.setCodigo(id);
        when(processoRepo.findById(id)).thenReturn(Optional.of(processo));

        processoService.apagar(id);

        verify(processoRepo).deleteById(id);
    }

    @Test
    @DisplayName("Apagar falha se processo não encontrado")
    void apagarNaoEncontrado() {
        when(processoRepo.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> processoService.apagar(99L))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }

    @Test
    @DisplayName("Apagar falha se processo não estiver CRIADO")
    void apagarSituacaoInvalida() {
        Long id = 100L;
        Processo processo = ProcessoFixture.processoEmAndamento();
        processo.setCodigo(id);
        when(processoRepo.findById(id)).thenReturn(Optional.of(processo));

        assertThatThrownBy(() -> processoService.apagar(id))
                .isInstanceOf(ErroProcessoEmSituacaoInvalida.class);
    }

    @Test
    @DisplayName("ObterDetalhes deve retornar DTO")
    void obterDetalhes() {
        Long id = 100L;
        Processo processo = ProcessoFixture.processoPadrao();
        when(processoRepo.findById(id)).thenReturn(Optional.of(processo));
        when(processoDetalheBuilder.build(processo)).thenReturn(new ProcessoDetalheDto());

        var res = processoService.obterDetalhes(id);

        assertThat(res).isNotNull();
    }

    @Test
    @DisplayName("IniciarMapeamento deve delegar para ProcessoInicializador")
    void iniciarProcessoMapeamento() {
        Long id = 100L;
        when(processoInicializador.iniciar(id, List.of(1L))).thenReturn(List.of());

        List<String> erros = processoService.iniciarProcessoMapeamento(id, List.of(1L));

        assertThat(erros).isEmpty();
        verify(processoInicializador).iniciar(id, List.of(1L));
    }

    @Test
    @DisplayName("IniciarMapeamento retorna erros do ProcessoInicializador")
    void iniciarProcessoMapeamentoUnidadeJaEmUso() {
        Long id = 100L;
        String mensagemErro = "As seguintes unidades já participam de outro processo ativo: U1";
        when(processoInicializador.iniciar(id, List.of(1L))).thenReturn(List.of(mensagemErro));

        List<String> erros = processoService.iniciarProcessoMapeamento(id, List.of(1L));

        assertThat(erros).contains(mensagemErro);
    }

    @Test
    @DisplayName("IniciarProcessoRevisao deve delegar para ProcessoInicializador")
    void iniciarProcessoRevisao() {
        Long id = 100L;
        when(processoInicializador.iniciar(id, List.of(1L))).thenReturn(List.of());

        List<String> erros = processoService.iniciarProcessoRevisao(id, List.of(1L));

        assertThat(erros).isEmpty();
        verify(processoInicializador).iniciar(id, List.of(1L));
    }

    @Test
    @DisplayName("IniciarProcessoRevisao retorna erros do ProcessoInicializador")
    void iniciarProcessoRevisaoUnidadeSemMapa() {
        Long id = 100L;
        String mensagemErro = "As seguintes unidades não possuem mapa vigente e não podem participar de um processo de revisão: U1";
        when(processoInicializador.iniciar(id, List.of(1L))).thenReturn(List.of(mensagemErro));

        List<String> erros = processoService.iniciarProcessoRevisao(id, List.of(1L));

        assertThat(erros).contains(mensagemErro);
    }

    @Test
    @DisplayName("Finalizar deve falhar se houver subprocessos não homologados")
    void finalizarFalha() {
        Long id = 100L;
        Processo processo = ProcessoFixture.processoEmAndamento();
        processo.setCodigo(id);

        Subprocesso sp = new Subprocesso();
        sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);

        when(processoRepo.findById(id)).thenReturn(Optional.of(processo));
        when(subprocessoRepo.findByProcessoCodigoWithUnidade(id)).thenReturn(List.of(sp));

        assertThatThrownBy(() -> processoService.finalizar(id)).isInstanceOf(ErroProcesso.class);
    }

    @Test
    @DisplayName("Finalizar deve falhar se processo não está em andamento")
    void finalizarNaoEmAndamento() {
        Long id = 100L;
        Processo processo = ProcessoFixture.processoPadrao();
        processo.setCodigo(id);
        when(processoRepo.findById(id)).thenReturn(Optional.of(processo));

        assertThatThrownBy(() -> processoService.finalizar(id))
                .isInstanceOf(ErroProcesso.class)
                .hasMessageContaining("Apenas processos 'EM ANDAMENTO'");
    }

    @Test
    @DisplayName("Finalizar deve completar se tudo homologado")
    void finalizarSucesso() {
        Long id = 100L;
        Processo processo = ProcessoFixture.processoEmAndamento();
        processo.setCodigo(id);

        Unidade u = UnidadeFixture.unidadeComId(1L);
        Mapa m = MapaFixture.mapaPadrao(null); // Subprocesso linked later via setter if needed, but here it is circular.

        Subprocesso sp = SubprocessoFixture.subprocessoPadrao(processo, u);
        sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO);
        sp.setMapa(m);

        when(processoRepo.findById(id)).thenReturn(Optional.of(processo));
        when(subprocessoRepo.findByProcessoCodigoWithUnidade(id)).thenReturn(List.of(sp));
        when(unidadeMapaRepo.findById(1L)).thenReturn(Optional.of(new sgc.unidade.model.UnidadeMapa()));

        processoService.finalizar(id);

        assertThat(processo.getSituacao()).isEqualTo(SituacaoProcesso.FINALIZADO);
        verify(unidadeMapaRepo).save(any()); // Mapa vigente alterado
        verify(publicadorEventos).publishEvent(any(EventoProcessoFinalizado.class));
    }

    @Test
    @DisplayName("Finalizar deve falhar se subprocesso sem unidade")
    void finalizarSubprocessoSemUnidade() {
        Long id = 100L;
        Processo processo = ProcessoFixture.processoEmAndamento();
        processo.setCodigo(id);

        Subprocesso sp = SubprocessoFixture.subprocessoPadrao(processo, null);
        sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO);

        when(processoRepo.findById(id)).thenReturn(Optional.of(processo));
        when(subprocessoRepo.findByProcessoCodigoWithUnidade(id)).thenReturn(List.of(sp));

        assertThatThrownBy(() -> processoService.finalizar(id))
                .isInstanceOf(ErroProcesso.class)
                .hasMessageContaining("sem unidade associada");
    }

    @Test
    @DisplayName("listarFinalizados e listarAtivos devem chamar repo")
    void listagens() {
        when(processoRepo.findBySituacao(any())).thenReturn(List.of(ProcessoFixture.processoPadrao()));
        when(processoMapper.toDto(any())).thenReturn(ProcessoDto.builder().build());

        assertThat(processoService.listarFinalizados()).hasSize(1);
        assertThat(processoService.listarAtivos()).hasSize(1);
    }

    @Test
    @DisplayName("Checar acesso deve retornar false se nao autenticado")
    void checarAcesso() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(false);

        assertThat(processoService.checarAcesso(auth, 1L)).isFalse();
    }

    @Test
    @DisplayName("Checar acesso deve retornar false se usuário não tiver role adequada")
    void checarAcessoSemRole() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("user");

        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        doReturn(authorities).when(auth).getAuthorities(); // Safe unchecked cast workaround

        assertThat(processoService.checarAcesso(auth, 1L)).isFalse();
    }

    @Test
    @DisplayName("Checar acesso retorna false se usuário sem unidade")
    void checarAcessoSemUnidade() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("gestor");

        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_GESTOR"));
        doReturn(authorities).when(auth).getAuthorities();

        when(sgrhService.buscarPerfisUsuario("gestor")).thenReturn(List.of());

        assertThat(processoService.checarAcesso(auth, 1L)).isFalse();
    }

    @Test
    @DisplayName("Checar acesso retorna true se gestor da unidade participante")
    void checarAcessoParticipante() {
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

        assertThat(processoService.checarAcesso(auth, 1L)).isTrue();
    }

    @Test
    @DisplayName("Listar Unidades Bloqueadas")
    void listarUnidadesBloqueadas() {
        when(processoRepo.findUnidadeCodigosBySituacaoAndTipo(
                        SituacaoProcesso.EM_ANDAMENTO, TipoProcesso.MAPEAMENTO))
                .thenReturn(List.of(1L));

        List<Long> bloqueadas = processoService.listarUnidadesBloqueadasPorTipo("MAPEAMENTO");
        assertThat(bloqueadas).contains(1L);
    }

    @Test
    @DisplayName("Listar Subprocessos Elegiveis para Admin")
    void listarSubprocessosElegiveisAdmin() {
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

        List<SubprocessoElegivelDto> res = processoService.listarSubprocessosElegiveis(100L);
        assertThat(res).hasSize(1);
        assertThat(res.get(0).getCodSubprocesso()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Listar Subprocessos Elegiveis para Gestor")
    void listarSubprocessosElegiveisGestor() {
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

        List<SubprocessoElegivelDto> res = processoService.listarSubprocessosElegiveis(100L);
        assertThat(res).hasSize(1);
    }

    @Test
    @DisplayName("Listar Subprocessos Elegiveis retorna vazio se usuário sem unidade")
    void listarSubprocessosElegiveisUsuarioSemUnidade() {
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

        List<SubprocessoElegivelDto> res = processoService.listarSubprocessosElegiveis(100L);
        assertThat(res).isEmpty();
    }
}
