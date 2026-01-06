package sgc.processo.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import sgc.processo.erros.ErroProcesso;
import sgc.processo.erros.ErroProcessoEmSituacaoInvalida;
import sgc.organizacao.UnidadeService;
import sgc.organizacao.UsuarioService;
import sgc.organizacao.dto.PerfilDto;
import sgc.organizacao.model.Unidade;
import sgc.processo.dto.AtualizarProcessoReq;
import sgc.processo.dto.mappers.ProcessoMapper;
import sgc.processo.model.Processo;
import sgc.processo.model.ProcessoRepo;
import sgc.processo.model.SituacaoProcesso;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.SubprocessoService;
import sgc.alerta.AlertaService;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcessoServiceCoverageTest {

    @InjectMocks
    private ProcessoService service;

    @Mock private ProcessoRepo processoRepo;
    @Mock private UnidadeService unidadeService;
    @Mock private SubprocessoService subprocessoService;
    @Mock private ApplicationEventPublisher publicadorEventos;
    @Mock private ProcessoMapper processoMapper;
    @Mock private UsuarioService usuarioService;
    @Mock private AlertaService alertaService;
    @Mock private sgc.processo.service.ProcessoDetalheBuilder processoDetalheBuilder;
    @Mock private sgc.subprocesso.mapper.SubprocessoMapper subprocessoMapper;
    @Mock private sgc.processo.service.ProcessoInicializador processoInicializador;

    @Test
    @DisplayName("atualizar: deve lançar erro se processo não estiver CRIADO")
    void atualizar_ErroSeNaoCriado() {
        Long codigo = 1L;
        Processo processo = new Processo();
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);

        when(processoRepo.findById(codigo)).thenReturn(Optional.of(processo));

        assertThatThrownBy(() -> service.atualizar(codigo, new AtualizarProcessoReq()))
                .isInstanceOf(ErroProcessoEmSituacaoInvalida.class);
    }

    @Test
    @DisplayName("finalizar: deve lançar erro se processo não estiver EM_ANDAMENTO")
    void finalizar_ErroSeNaoEmAndamento() {
        Long codigo = 1L;
        Processo processo = new Processo();
        processo.setSituacao(SituacaoProcesso.CRIADO);

        when(processoRepo.findById(codigo)).thenReturn(Optional.of(processo));

        assertThatThrownBy(() -> service.finalizar(codigo))
                .isInstanceOf(ErroProcesso.class)
                .hasMessageContaining("Apenas processos 'EM ANDAMENTO' podem ser finalizados");
    }

    @Test
    @DisplayName("finalizar: deve lançar erro se subprocesso não estiver homologado")
    void finalizar_ErroSubprocessoNaoHomologado() {
        Long codigo = 1L;
        Processo processo = new Processo();
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        processo.setCodigo(codigo);

        Subprocesso sp = new Subprocesso();
        sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        sp.setUnidade(new Unidade());

        when(processoRepo.findById(codigo)).thenReturn(Optional.of(processo));
        when(subprocessoService.listarEntidadesPorProcesso(codigo)).thenReturn(List.of(sp));

        assertThatThrownBy(() -> service.finalizar(codigo))
                .isInstanceOf(ErroProcesso.class)
                .hasMessageContaining("Unidades pendentes de homologação");
    }

    @Test
    @DisplayName("finalizar: deve lançar erro se subprocesso sem unidade")
    void finalizar_ErroSubprocessoSemUnidade() {
        Long codigo = 1L;
        Processo processo = new Processo();
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        processo.setCodigo(codigo);

        Subprocesso sp = new Subprocesso();
        sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO);
        sp.setUnidade(null);

        when(processoRepo.findById(codigo)).thenReturn(Optional.of(processo));
        when(subprocessoService.listarEntidadesPorProcesso(codigo)).thenReturn(List.of(sp));

        assertThatThrownBy(() -> service.finalizar(codigo))
                .isInstanceOf(ErroProcesso.class)
                .hasMessageContaining("sem unidade associada");
    }

    @Test
    @DisplayName("finalizar: deve lançar erro se subprocesso sem mapa")
    void finalizar_ErroSubprocessoSemMapa() {
        Long codigo = 1L;
        Processo processo = new Processo();
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        processo.setCodigo(codigo);

        Subprocesso sp = new Subprocesso();
        sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO);
        sp.setUnidade(new Unidade());
        sp.setMapa(null);

        when(processoRepo.findById(codigo)).thenReturn(Optional.of(processo));
        when(subprocessoService.listarEntidadesPorProcesso(codigo)).thenReturn(List.of(sp));

        assertThatThrownBy(() -> service.finalizar(codigo))
                .isInstanceOf(ErroProcesso.class)
                .hasMessageContaining("sem mapa associado");
    }

    @Test
    @DisplayName("enviarLembrete: deve lançar erro se unidade não participa")
    void enviarLembrete_ErroUnidadeNaoParticipa() {
        Long codProcesso = 1L;
        Long codUnidade = 99L;

        Processo processo = new Processo();
        processo.setParticipantes(Collections.emptySet());

        Unidade unidade = new Unidade();
        unidade.setCodigo(codUnidade);

        when(processoRepo.findById(codProcesso)).thenReturn(Optional.of(processo));
        when(unidadeService.buscarEntidadePorId(codUnidade)).thenReturn(unidade);

        assertThatThrownBy(() -> service.enviarLembrete(codProcesso, codUnidade))
                .isInstanceOf(ErroProcesso.class)
                .hasMessageContaining("Unidade não participa");
    }

    @Test
    @DisplayName("enviarLembrete: sucesso")
    void enviarLembrete_Sucesso() {
        Long codProcesso = 1L;
        Long codUnidade = 10L;

        Unidade unidade = new Unidade();
        unidade.setCodigo(codUnidade);

        Processo processo = new Processo();
        processo.setDescricao("Processo Teste");
        processo.setDataLimite(java.time.LocalDateTime.now());
        processo.setParticipantes(Set.of(unidade));

        when(processoRepo.findById(codProcesso)).thenReturn(Optional.of(processo));
        when(unidadeService.buscarEntidadePorId(codUnidade)).thenReturn(unidade);

        service.enviarLembrete(codProcesso, codUnidade);

        verify(alertaService).criarAlerta(eq(processo), any(), eq(unidade), anyString());
    }

    @Test
    @DisplayName("checarAcesso: deve retornar false se não autenticado")
    void checarAcesso_FalsoSeNaoAutenticado() {
        assertThat(service.checarAcesso(null, 1L)).isFalse();
    }

    @Test
    @DisplayName("checarAcesso: deve retornar false se role invalida")
    void checarAcesso_FalsoSeRoleInvalida() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("user");
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_USER"))).when(auth).getAuthorities();

        assertThat(service.checarAcesso(auth, 1L)).isFalse();
    }

    @Test
    @DisplayName("checarAcesso: deve retornar false se usuario sem unidade")
    void checarAcesso_FalsoSeUsuarioSemUnidade() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("user");
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_GESTOR"))).when(auth).getAuthorities();

        when(usuarioService.buscarPerfisUsuario("user")).thenReturn(List.of(new PerfilDto()));

        assertThat(service.checarAcesso(auth, 1L)).isFalse();
    }

    @Test
    @DisplayName("listarSubprocessosElegiveis: retorna vazio se não autenticado")
    void listarSubprocessosElegiveis_VazioSeNaoAutenticado() {
        SecurityContextHolder.clearContext();
        assertThat(service.listarSubprocessosElegiveis(1L)).isEmpty();
    }

    @Test
    @DisplayName("listarSubprocessosElegiveis: usuario sem unidade retorna vazio")
    void listarSubprocessosElegiveis_UsuarioSemUnidade() {
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user");
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_GESTOR"))).when(auth).getAuthorities();
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        when(usuarioService.buscarPerfisUsuario("user")).thenReturn(List.of(new PerfilDto()));

        assertThat(service.listarSubprocessosElegiveis(1L)).isEmpty();
    }

    @Test
    @DisplayName("checarAcesso: retorna false se buscarCodigosDescendentes retorna vazio")
    void checarAcesso_HierarquiaVazia() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("user");
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_GESTOR"))).when(auth).getAuthorities();

        PerfilDto perfil = new PerfilDto();
        perfil.setUnidadeCodigo(10L);
        when(usuarioService.buscarPerfisUsuario("user")).thenReturn(List.of(perfil));
        when(unidadeService.buscarTodasEntidadesComHierarquia()).thenReturn(List.of());

        assertThat(service.checarAcesso(auth, 1L)).isFalse();
    }

    @Test
    @DisplayName("obterContextoCompleto: sucesso")
    void obterContextoCompleto_Sucesso() {
        Long codProcesso = 1L;
        Processo processo = new Processo();
        processo.setCodigo(codProcesso);

        // Setup security context
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("admin");
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))).when(auth).getAuthorities();
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        sgc.processo.dto.ProcessoDetalheDto detalhe = new sgc.processo.dto.ProcessoDetalheDto();
        when(processoRepo.findById(codProcesso)).thenReturn(Optional.of(processo));
        when(processoDetalheBuilder.build(processo)).thenReturn(detalhe);
        when(subprocessoService.listarEntidadesPorProcesso(codProcesso)).thenReturn(List.of());

        sgc.processo.dto.ProcessoContextoDto resultado = service.obterContextoCompleto(codProcesso);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getProcesso()).isEqualTo(detalhe);
        
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("iniciarProcessoDiagnostico: sucesso")
    void iniciarProcessoDiagnostico_Sucesso() {
        Long codigo = 1L;
        List<Long> unidades = List.of(10L, 20L);
        List<String> avisos = List.of("aviso1");

        when(processoInicializador.iniciar(codigo, unidades)).thenReturn(avisos);

        List<String> resultado = service.iniciarProcessoDiagnostico(codigo, unidades);

        assertThat(resultado).isEqualTo(avisos);
        verify(processoInicializador).iniciar(codigo, unidades);
    }

    @Test
    @DisplayName("criar: erro se unidade INTERMEDIARIA")
    void criar_ErroUnidadeIntermediaria() {
        sgc.processo.dto.CriarProcessoReq req = new sgc.processo.dto.CriarProcessoReq();
        req.setDescricao("Teste");
        req.setUnidades(List.of(10L));
        req.setTipo(sgc.processo.model.TipoProcesso.MAPEAMENTO);

        Unidade unidade = new Unidade();
        unidade.setCodigo(10L);
        unidade.setTipo(sgc.organizacao.model.TipoUnidade.INTERMEDIARIA);
        unidade.setSigla("INT");

        when(unidadeService.buscarEntidadePorId(10L)).thenReturn(unidade);

        assertThatThrownBy(() -> service.criar(req))
                .isInstanceOf(sgc.comum.erros.ErroEstadoImpossivel.class)
                .hasMessageContaining("Erro interno: unidade não elegível");
    }

    @Test
    @DisplayName("atualizar: erro se tipo REVISAO e unidade sem mapa")
    void atualizar_ErroRevisaoSemMapa() {
        Long codigo = 1L;
        Processo processo = new Processo();
        processo.setSituacao(SituacaoProcesso.CRIADO);

        AtualizarProcessoReq req = new AtualizarProcessoReq();
        req.setDescricao("Teste");
        req.setTipo(sgc.processo.model.TipoProcesso.REVISAO);
        req.setUnidades(List.of(10L));

        Unidade unidade = new Unidade();
        unidade.setCodigo(10L);
        unidade.setSigla("UND");

        when(processoRepo.findById(codigo)).thenReturn(Optional.of(processo));
        when(unidadeService.buscarEntidadesPorIds(List.of(10L))).thenReturn(List.of(unidade));
        when(unidadeService.verificarExistenciaMapaVigente(10L)).thenReturn(false);
        when(unidadeService.buscarSiglasPorIds(List.of(10L))).thenReturn(List.of("UND"));

        assertThatThrownBy(() -> service.atualizar(codigo, req))
                .isInstanceOf(ErroProcesso.class)
                .hasMessageContaining("não possuem mapa vigente");
    }

    @Test
    @DisplayName("criar: sucesso com getMensagemErroUnidadesSemMapa vazio")
    void criar_GetMensagemErroVazio() {
        sgc.processo.dto.CriarProcessoReq req = new sgc.processo.dto.CriarProcessoReq();
        req.setDescricao("Teste");
        req.setUnidades(List.of());
        req.setTipo(sgc.processo.model.TipoProcesso.REVISAO);

        assertThatThrownBy(() -> service.criar(req))
                .isInstanceOf(jakarta.validation.ConstraintViolationException.class);
    }
}
