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
import sgc.processo.dto.AtualizarProcessoReq;
import sgc.processo.dto.CriarProcessoReq;
import sgc.processo.dto.ProcessoDetalheDto;
import sgc.processo.dto.ProcessoDto;
import sgc.processo.dto.SubprocessoElegivelDto;
import sgc.processo.dto.mappers.ProcessoDetalheMapper;
import sgc.processo.dto.mappers.ProcessoMapper;
import sgc.processo.erros.ErroProcesso;
import sgc.processo.erros.ErroProcessoEmSituacaoInvalida;
import sgc.processo.erros.ErroUnidadesNaoDefinidas;
import sgc.processo.eventos.EventoProcessoCriado;
import sgc.processo.eventos.EventoProcessoFinalizado;
import sgc.processo.eventos.EventoProcessoIniciado;
import sgc.processo.model.Processo;
import sgc.processo.model.ProcessoRepo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.processo.service.ProcessoNotificacaoService;
import sgc.processo.service.ProcessoService;
import sgc.sgrh.dto.PerfilDto;
import sgc.sgrh.service.SgrhService;
import sgc.subprocesso.model.*;
import sgc.unidade.model.TipoUnidade;
import sgc.unidade.model.Unidade;
import sgc.unidade.model.UnidadeRepo;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProcessoServiceTest {
    @Mock private ProcessoRepo processoRepo;
    @Mock private UnidadeRepo unidadeRepo;
    @Mock private SubprocessoRepo subprocessoRepo;
    @Mock private ApplicationEventPublisher publicadorEventos;
    @Mock private ProcessoMapper processoMapper;
    @Mock private ProcessoDetalheMapper processoDetalheMapper;
    @Mock private MapaRepo mapaRepo;
    @Mock private SubprocessoMovimentacaoRepo movimentacaoRepo;
    @Mock private CopiaMapaService servicoDeCopiaDeMapa;
    @Mock private ProcessoNotificacaoService processoNotificacaoService;
    @Mock private SgrhService sgrhService;

    @InjectMocks private ProcessoService processoService;

    @Test
    @DisplayName("Criar processo deve persistir e publicar evento")
    void criar() {
        CriarProcessoReq req = new CriarProcessoReq("Teste", TipoProcesso.MAPEAMENTO, LocalDateTime.now(), List.of(1L));
        Unidade unidade = new Unidade();
        unidade.setCodigo(1L);

        when(unidadeRepo.findById(1L)).thenReturn(Optional.of(unidade));
        when(processoRepo.save(any())).thenAnswer(i -> {
            Processo p = i.getArgument(0);
            p.setCodigo(100L);
            return p;
        });
        when(processoMapper.toDto(any())).thenReturn(ProcessoDto.builder().build());

        processoService.criar(req);

        verify(processoRepo).save(any());
        verify(publicadorEventos).publishEvent(any(EventoProcessoCriado.class));
    }

    @Test
    @DisplayName("Criar deve lançar exceção se descrição vazia")
    void criarDescricaoVazia() {
        CriarProcessoReq req = new CriarProcessoReq("", TipoProcesso.MAPEAMENTO, LocalDateTime.now(), List.of(1L));
        assertThatThrownBy(() -> processoService.criar(req))
            .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    @DisplayName("Criar deve lançar exceção se lista de unidades vazia")
    void criarSemUnidades() {
        CriarProcessoReq req = new CriarProcessoReq("Teste", TipoProcesso.MAPEAMENTO, LocalDateTime.now(), List.of());
        assertThatThrownBy(() -> processoService.criar(req))
            .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    @DisplayName("Criar deve lançar exceção se unidade não encontrada")
    void criarUnidadeNaoEncontrada() {
        CriarProcessoReq req = new CriarProcessoReq("Teste", TipoProcesso.MAPEAMENTO, LocalDateTime.now(), List.of(99L));
        when(unidadeRepo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> processoService.criar(req))
            .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }

    @Test
    @DisplayName("Atualizar deve modificar processo se estiver CRIADO")
    void atualizar() {
        Long id = 100L;
        Processo processo = new Processo();
        processo.setCodigo(id);
        processo.setSituacao(SituacaoProcesso.CRIADO);

        AtualizarProcessoReq req = AtualizarProcessoReq.builder()
            .codigo(id)
            .descricao("Nova Desc")
            .tipo(TipoProcesso.MAPEAMENTO)
            .dataLimiteEtapa1(LocalDateTime.now())
            .unidades(List.of(1L))
            .build();

        when(processoRepo.findById(id)).thenReturn(Optional.of(processo));
        when(unidadeRepo.findById(1L)).thenReturn(Optional.of(new Unidade()));
        when(processoRepo.save(any())).thenReturn(processo);
        when(processoMapper.toDto(any())).thenReturn(ProcessoDto.builder().build());

        processoService.atualizar(id, req);

        assertThat(processo.getDescricao()).isEqualTo("Nova Desc");
        verify(processoRepo).save(processo);
    }

    @Test
    @DisplayName("Atualizar deve falhar se não estiver CRIADO")
    void atualizarInvalido() {
        Long id = 100L;
        Processo processo = new Processo();
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        when(processoRepo.findById(id)).thenReturn(Optional.of(processo));

        AtualizarProcessoReq req = AtualizarProcessoReq.builder()
            .descricao("Desc")
            .tipo(TipoProcesso.MAPEAMENTO)
            .unidades(List.of())
            .build();

        assertThatThrownBy(() -> processoService.atualizar(id, req))
            .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Atualizar deve falhar se unidade não encontrada")
    void atualizarUnidadeNaoEncontrada() {
        Long id = 100L;
        Processo processo = new Processo();
        processo.setCodigo(id);
        processo.setSituacao(SituacaoProcesso.CRIADO);

        AtualizarProcessoReq req = AtualizarProcessoReq.builder()
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
        Processo processo = new Processo();
        processo.setSituacao(SituacaoProcesso.CRIADO);
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
        Processo processo = new Processo();
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        when(processoRepo.findById(id)).thenReturn(Optional.of(processo));

        assertThatThrownBy(() -> processoService.apagar(id))
            .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("ObterDetalhes deve retornar DTO")
    void obterDetalhes() {
        Long id = 100L;
        Processo processo = new Processo();
        when(processoRepo.findById(id)).thenReturn(Optional.of(processo));
        when(processoDetalheMapper.toDetailDTO(processo)).thenReturn(new ProcessoDetalheDto());

        var res = processoService.obterDetalhes(id);

        assertThat(res).isNotNull();
    }

    @Test
    @DisplayName("IniciarMapeamento deve criar subprocessos e mudar estado")
    void iniciarProcessoMapeamento() {
        Long id = 100L;
        Processo processo = new Processo();
        processo.setCodigo(id);
        processo.setSituacao(SituacaoProcesso.CRIADO);
        processo.setTipo(TipoProcesso.MAPEAMENTO);

        Unidade u1 = new Unidade();
        u1.setCodigo(1L);
        u1.setTipo(TipoUnidade.OPERACIONAL);
        processo.setParticipantes(Set.of(u1));

        when(processoRepo.findById(id)).thenReturn(Optional.of(processo));
        when(mapaRepo.save(any())).thenReturn(new Mapa());
        when(subprocessoRepo.save(any())).thenReturn(new Subprocesso());

        processoService.iniciarProcessoMapeamento(id, List.of(1L));

        assertThat(processo.getSituacao()).isEqualTo(SituacaoProcesso.EM_ANDAMENTO);
        verify(publicadorEventos).publishEvent(any(EventoProcessoIniciado.class));
    }

    @Test
    @DisplayName("IniciarMapeamento deve falhar se não houver participantes")
    void iniciarProcessoMapeamentoSemParticipantes() {
        Long id = 100L;
        Processo processo = new Processo();
        processo.setCodigo(id);
        processo.setSituacao(SituacaoProcesso.CRIADO);
        processo.setParticipantes(Set.of());

        when(processoRepo.findById(id)).thenReturn(Optional.of(processo));

        assertThatThrownBy(() -> processoService.iniciarProcessoMapeamento(id, List.of()))
            .isInstanceOf(ErroUnidadesNaoDefinidas.class);
    }

    @Test
    @DisplayName("IniciarMapeamento deve falhar se unidade já participa de outro processo")
    void iniciarProcessoMapeamentoUnidadeJaEmUso() {
        Long id = 100L;
        Processo processo = new Processo();
        processo.setCodigo(id);
        processo.setSituacao(SituacaoProcesso.CRIADO);

        Unidade u1 = new Unidade();
        u1.setCodigo(1L);
        processo.setParticipantes(Set.of(u1));

        Processo outroProcesso = new Processo();
        outroProcesso.setParticipantes(Set.of(u1));

        when(processoRepo.findById(id)).thenReturn(Optional.of(processo));
        when(processoRepo.findBySituacao(SituacaoProcesso.EM_ANDAMENTO)).thenReturn(List.of(outroProcesso));

        assertThatThrownBy(() -> processoService.iniciarProcessoMapeamento(id, List.of(1L)))
            .isInstanceOf(ErroProcesso.class)
            .hasMessageContaining("já participam de outro processo");
    }

    @Test
    @DisplayName("IniciarProcessoRevisao sucesso")
    void iniciarProcessoRevisao() {
        Long id = 100L;
        Processo processo = new Processo();
        processo.setCodigo(id);
        processo.setSituacao(SituacaoProcesso.CRIADO);
        processo.setTipo(TipoProcesso.REVISAO);

        Unidade u1 = new Unidade();
        u1.setCodigo(1L);
        Mapa mapaVigente = new Mapa();
        mapaVigente.setCodigo(10L);
        u1.setMapaVigente(mapaVigente);

        when(processoRepo.findById(id)).thenReturn(Optional.of(processo));
        when(unidadeRepo.findAllById(List.of(1L))).thenReturn(List.of(u1));
        when(unidadeRepo.findById(1L)).thenReturn(Optional.of(u1));
        when(servicoDeCopiaDeMapa.copiarMapaParaUnidade(10L, 1L)).thenReturn(new Mapa());
        when(subprocessoRepo.save(any())).thenReturn(new Subprocesso());

        processoService.iniciarProcessoRevisao(id, List.of(1L));

        assertThat(processo.getSituacao()).isEqualTo(SituacaoProcesso.EM_ANDAMENTO);
        verify(publicadorEventos).publishEvent(any(EventoProcessoIniciado.class));
    }

    @Test
    @DisplayName("IniciarProcessoRevisao falha se unidade sem mapa vigente")
    void iniciarProcessoRevisaoUnidadeSemMapa() {
        Long id = 100L;
        Processo processo = new Processo();
        processo.setCodigo(id);
        processo.setSituacao(SituacaoProcesso.CRIADO);

        Unidade u1 = new Unidade();
        u1.setCodigo(1L);
        u1.setMapaVigente(null);

        when(processoRepo.findById(id)).thenReturn(Optional.of(processo));
        when(unidadeRepo.findAllById(List.of(1L))).thenReturn(List.of(u1));
        when(unidadeRepo.findSiglasByCodigos(any())).thenReturn(List.of("U1"));

        assertThatThrownBy(() -> processoService.iniciarProcessoRevisao(id, List.of(1L)))
            .isInstanceOf(ErroProcesso.class)
            .hasMessageContaining("não possuem mapa vigente");
    }

    @Test
    @DisplayName("IniciarProcessoRevisao falha se lista vazia")
    void iniciarProcessoRevisaoListaVazia() {
        Long id = 100L;
        Processo processo = new Processo();
        processo.setSituacao(SituacaoProcesso.CRIADO);
        when(processoRepo.findById(id)).thenReturn(Optional.of(processo));

        assertThatThrownBy(() -> processoService.iniciarProcessoRevisao(id, List.of()))
            .isInstanceOf(ErroUnidadesNaoDefinidas.class);
    }

    @Test
    @DisplayName("Finalizar deve falhar se houver subprocessos não homologados")
    void finalizarFalha() {
        Long id = 100L;
        Processo processo = new Processo();
        processo.setCodigo(id);
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);

        Subprocesso sp = new Subprocesso();
        sp.setSituacao(SituacaoSubprocesso.CADASTRO_EM_ANDAMENTO);

        when(processoRepo.findById(id)).thenReturn(Optional.of(processo));
        when(subprocessoRepo.findByProcessoCodigoWithUnidade(id)).thenReturn(List.of(sp));

        assertThatThrownBy(() -> processoService.finalizar(id))
            .isInstanceOf(ErroProcesso.class);
    }

    @Test
    @DisplayName("Finalizar deve falhar se processo não está em andamento")
    void finalizarNaoEmAndamento() {
        Long id = 100L;
        Processo processo = new Processo();
        processo.setSituacao(SituacaoProcesso.CRIADO);
        when(processoRepo.findById(id)).thenReturn(Optional.of(processo));

        assertThatThrownBy(() -> processoService.finalizar(id))
            .isInstanceOf(ErroProcesso.class)
            .hasMessageContaining("Apenas processos 'EM ANDAMENTO'");
    }

    @Test
    @DisplayName("Finalizar deve completar se tudo homologado")
    void finalizarSucesso() {
        Long id = 100L;
        Processo processo = new Processo();
        processo.setCodigo(id);
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);

        Unidade u = new Unidade();
        u.setCodigo(1L);

        Mapa m = new Mapa();

        Subprocesso sp = new Subprocesso();
        sp.setSituacao(SituacaoSubprocesso.MAPA_HOMOLOGADO);
        sp.setUnidade(u);
        sp.setMapa(m);

        when(processoRepo.findById(id)).thenReturn(Optional.of(processo));
        when(subprocessoRepo.findByProcessoCodigoWithUnidade(id)).thenReturn(List.of(sp));

        processoService.finalizar(id);

        assertThat(processo.getSituacao()).isEqualTo(SituacaoProcesso.FINALIZADO);
        verify(unidadeRepo).save(u); // Mapa vigente set
        verify(publicadorEventos).publishEvent(any(EventoProcessoFinalizado.class));
    }

    @Test
    @DisplayName("Finalizar deve falhar se subprocesso sem unidade")
    void finalizarSubprocessoSemUnidade() {
        Long id = 100L;
        Processo processo = new Processo();
        processo.setCodigo(id);
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);

        Subprocesso sp = new Subprocesso();
        sp.setSituacao(SituacaoSubprocesso.MAPA_HOMOLOGADO);
        sp.setUnidade(null);

        when(processoRepo.findById(id)).thenReturn(Optional.of(processo));
        when(subprocessoRepo.findByProcessoCodigoWithUnidade(id)).thenReturn(List.of(sp));

        assertThatThrownBy(() -> processoService.finalizar(id))
            .isInstanceOf(ErroProcesso.class)
            .hasMessageContaining("sem unidade associada");
    }

    @Test
    @DisplayName("listarFinalizados e listarAtivos devem chamar repo")
    void listagens() {
        when(processoRepo.findBySituacao(any())).thenReturn(List.of(new Processo()));
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

        PerfilDto perfil = PerfilDto.builder().unidadeCodigo(10L).build();
        when(sgrhService.buscarPerfisUsuario("gestor")).thenReturn(List.of(perfil));

        when(subprocessoRepo.existsByProcessoCodigoAndUnidadeCodigo(1L, 10L)).thenReturn(true);

        assertThat(processoService.checarAcesso(auth, 1L)).isTrue();
    }

    @Test
    @DisplayName("Listar Unidades Bloqueadas")
    void listarUnidadesBloqueadas() {
        Processo p = new Processo();
        p.setTipo(TipoProcesso.MAPEAMENTO);
        Unidade u = new Unidade();
        u.setCodigo(1L);
        p.setParticipantes(Set.of(u));

        when(processoRepo.findBySituacao(SituacaoProcesso.EM_ANDAMENTO)).thenReturn(List.of(p));

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

        Subprocesso sp = new Subprocesso();
        sp.setCodigo(1L);
        sp.setSituacao(SituacaoSubprocesso.MAPA_AJUSTADO);
        Unidade u = new Unidade();
        u.setNome("U1");
        u.setSigla("S1");
        sp.setUnidade(u);

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

        Unidade u = new Unidade();
        u.setCodigo(10L);
        u.setNome("U1");
        u.setSigla("S1");

        Subprocesso sp = new Subprocesso();
        sp.setCodigo(1L);
        sp.setSituacao(SituacaoSubprocesso.CADASTRO_DISPONIBILIZADO);
        sp.setUnidade(u);

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
