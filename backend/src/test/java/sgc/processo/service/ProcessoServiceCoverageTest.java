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
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
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
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.SubprocessoService;
import sgc.alerta.AlertaService;
import sgc.mapa.model.Mapa;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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
}
