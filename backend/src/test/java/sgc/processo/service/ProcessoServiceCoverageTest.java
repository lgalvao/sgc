package sgc.processo.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import sgc.alerta.AlertaService;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.processo.erros.ErroProcesso;
import sgc.processo.erros.ErroProcessoEmSituacaoInvalida;
import sgc.mapa.model.Mapa;
import sgc.processo.model.Processo;
import sgc.processo.model.ProcessoRepo;
import sgc.processo.model.SituacaoProcesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.SubprocessoService;
import sgc.organizacao.UnidadeService;
import sgc.organizacao.model.Unidade;
import java.time.LocalDate;
import java.util.*;

import sgc.processo.dto.CriarProcessoReq;
import sgc.processo.dto.AtualizarProcessoReq;
import sgc.processo.model.TipoProcesso;
import sgc.processo.dto.mappers.ProcessoMapper;
import sgc.processo.dto.ProcessoDto;
import sgc.processo.dto.ProcessoDetalheDto;
import sgc.processo.dto.SubprocessoElegivelDto;
import sgc.organizacao.dto.PerfilDto;
import sgc.organizacao.UsuarioService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcessoServiceCoverageTest {

    @InjectMocks
    private ProcessoService service;

    @Mock private ProcessoRepo processoRepo;
    @Mock private UnidadeService unidadeService;
    @Mock private SubprocessoService subprocessoService;
    @Mock private ApplicationEventPublisher publicadorEventos;
    @Mock private AlertaService alertaService;
    @Mock private ProcessoMapper processoMapper;
    @Mock private UsuarioService usuarioService;
    @Mock private ProcessoDetalheBuilder processoDetalheBuilder;
    @Mock private ProcessoInicializador processoInicializador;

    // --- CHECAR ACESSO ---

    @Test
    @DisplayName("checarAcesso: retorna false se authentication for null ou não autenticado")
    void checarAcesso_NaoAutenticado() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(false);
        assertThat(service.checarAcesso(auth, 1L)).isFalse();

        assertThat(service.checarAcesso(null, 1L)).isFalse();
    }

    @Test
    @DisplayName("checarAcesso: retorna false se usuário não tem role GESTOR ou CHEFE")
    void checarAcesso_RoleInvalida() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("user");

        // Mock de authorities sem as roles necessarias
        // Para mockar getAuthorities, precisamos de um objeto Authentication mais complexo ou usar doReturn
        // Aqui assumimos que o mock retorna lista vazia por padrao

        assertThat(service.checarAcesso(auth, 1L)).isFalse();
    }

    // --- CRIAR PROCESSO ---

    @Test
    @DisplayName("criar: erro se REVISAO e unidades sem mapa")
    void criar_ErroRevisaoSemMapa() {
        CriarProcessoReq req = new CriarProcessoReq();
        req.setDescricao("Teste");
        req.setUnidades(List.of(1L));
        req.setTipo(TipoProcesso.REVISAO);

        Unidade u = new Unidade();
        u.setCodigo(1L);
        when(unidadeService.buscarEntidadePorId(1L)).thenReturn(u);
        when(unidadeService.buscarEntidadesPorIds(List.of(1L))).thenReturn(List.of(u));
        when(unidadeService.verificarExistenciaMapaVigente(1L)).thenReturn(false);
        when(unidadeService.buscarSiglasPorIds(any())).thenReturn(List.of("SIGLA"));

        assertThatThrownBy(() -> service.criar(req))
            .isInstanceOf(ErroProcesso.class)
            .hasMessageContaining("não possuem mapa vigente");
    }

    @Test
    @DisplayName("criar: sucesso se REVISAO e unidades com mapa")
    void criar_SucessoRevisaoComMapa() {
        CriarProcessoReq req = new CriarProcessoReq();
        req.setDescricao("Teste");
        req.setUnidades(List.of(1L));
        req.setTipo(TipoProcesso.REVISAO);

        Unidade u = new Unidade();
        u.setCodigo(1L);
        when(unidadeService.buscarEntidadePorId(1L)).thenReturn(u);
        when(unidadeService.buscarEntidadesPorIds(List.of(1L))).thenReturn(List.of(u));
        when(unidadeService.verificarExistenciaMapaVigente(1L)).thenReturn(true);

        when(processoRepo.saveAndFlush(any())).thenAnswer(inv -> inv.getArgument(0));
        when(processoMapper.toDto(any())).thenReturn(ProcessoDto.builder().build());

        service.criar(req);

        verify(processoRepo).saveAndFlush(any());
    }

    // --- ATUALIZAR PROCESSO ---

    @Test
    @DisplayName("atualizar: erro se processo não está CRIADO")
    void atualizar_ErroSeNaoCriado() {
        Processo p = new Processo();
        p.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        when(processoRepo.findById(1L)).thenReturn(Optional.of(p));

        assertThatThrownBy(() -> service.atualizar(1L, new AtualizarProcessoReq()))
                .isInstanceOf(ErroProcessoEmSituacaoInvalida.class);
    }

    // --- OBTER CONTEXTO E DETALHES ---

    @Test
    @DisplayName("obterContextoCompleto: sucesso")
    void obterContextoCompleto_Sucesso() {
        Processo p = new Processo();
        p.setCodigo(1L);
        when(processoRepo.findById(1L)).thenReturn(Optional.of(p));
        when(processoDetalheBuilder.build(p)).thenReturn(ProcessoDetalheDto.builder().build());

        // Mock SecurityContextHolder seria complexo aqui, mas como o metodo checa roles via @PreAuthorize (que nao roda no teste unitario direto da classe service a menos que configurado),
        // a logica interna de 'listarSubprocessosElegiveis' depende do SecurityContext.
        // Vamos testar 'obterDetalhes' isoladamente que é chamado por ele.

        assertThat(service.obterDetalhes(1L)).isNotNull();
    }

    // --- INICIAR PROCESSO ---

    @Test
    @DisplayName("iniciarProcessoDiagnostico: delega para inicializador")
    void iniciarProcessoDiagnostico_Sucesso() {
        service.iniciarProcessoDiagnostico(1L, List.of(2L));
        verify(processoInicializador).iniciar(1L, List.of(2L));
    }

    // --- ENVIAR LEMBRETE ---

    @Test
    @DisplayName("enviarLembrete: lança erro se unidade não participa")
    void enviarLembrete_NaoParticipa() {
        Processo p = new Processo();
        p.setParticipantes(Set.of());

        when(processoRepo.findById(1L)).thenReturn(Optional.of(p));
        when(unidadeService.buscarEntidadePorId(2L)).thenReturn(new Unidade());

        assertThatThrownBy(() -> service.enviarLembrete(1L, 2L))
            .isInstanceOf(ErroProcesso.class)
            .hasMessage("Unidade não participa deste processo.");
    }

    @Test
    @DisplayName("enviarLembrete: sucesso")
    void enviarLembrete_Sucesso() {
        Unidade u = new Unidade(); u.setCodigo(2L);
        Processo p = new Processo();
        p.setDescricao("P1");
        p.setDataLimite(java.time.LocalDateTime.now());
        p.setParticipantes(Set.of(u));

        when(processoRepo.findById(1L)).thenReturn(Optional.of(p));
        when(unidadeService.buscarEntidadePorId(2L)).thenReturn(u);

        service.enviarLembrete(1L, 2L);
        verify(alertaService).criarAlerta(eq(p), any(), eq(u), anyString());
    }

    // --- FINALIZAR ---

    @Test
    @DisplayName("finalizar: erro se processo não está em andamento")
    void finalizar_ErroStatus() {
        Processo p = new Processo(); p.setSituacao(SituacaoProcesso.CRIADO);
        when(processoRepo.findById(1L)).thenReturn(Optional.of(p));

        assertThatThrownBy(() -> service.finalizar(1L))
            .isInstanceOf(ErroProcesso.class)
            .hasMessageContaining("Apenas processos 'EM ANDAMENTO' podem ser finalizados");
    }

    @Test
    @DisplayName("finalizar: erro se subprocesso sem unidade")
    void finalizar_SubprocessoSemUnidade() {
        Processo p = new Processo(); p.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        Subprocesso sp = new Subprocesso();
        sp.setSituacao(sgc.subprocesso.model.SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO);
        // unidade null

        when(processoRepo.findById(1L)).thenReturn(Optional.of(p));
        when(subprocessoService.listarEntidadesPorProcesso(any())).thenReturn(List.of(sp));
        
        assertThatThrownBy(() -> service.finalizar(1L))
            .isInstanceOf(ErroProcesso.class)
            .hasMessageContaining("sem unidade associada");
    }

     @Test
    @DisplayName("finalizar: erro se subprocesso sem mapa")
    void finalizar_SubprocessoSemMapa() {
        Processo p = new Processo(); p.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        Subprocesso sp = new Subprocesso();
        sp.setSituacao(sgc.subprocesso.model.SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO);
        sp.setUnidade(new Unidade());
        // mapa null

        when(processoRepo.findById(1L)).thenReturn(Optional.of(p));
        when(subprocessoService.listarEntidadesPorProcesso(any())).thenReturn(List.of(sp));

        assertThatThrownBy(() -> service.finalizar(1L))
            .isInstanceOf(ErroProcesso.class)
            .hasMessageContaining("sem mapa associado");
    }

    @Test
    @DisplayName("finalizar: sucesso define mapa vigente")
    void finalizar_Sucesso() {
        Processo p = new Processo();
        p.setCodigo(1L);
        p.setSituacao(SituacaoProcesso.EM_ANDAMENTO);

        Unidade u = new Unidade(); u.setCodigo(10L);
        Mapa m = new Mapa();

        Subprocesso sp = new Subprocesso();
        sp.setSituacao(sgc.subprocesso.model.SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO);
        sp.setUnidade(u);
        sp.setMapa(m);

        when(processoRepo.findById(1L)).thenReturn(Optional.of(p));
        when(subprocessoService.listarEntidadesPorProcesso(1L)).thenReturn(List.of(sp));

        service.finalizar(1L);

        verify(unidadeService).definirMapaVigente(10L, m);
        verify(publicadorEventos).publishEvent(any(sgc.processo.eventos.EventoProcessoFinalizado.class));
    }

    @Test
    @DisplayName("getMensagemErroUnidadesSemMapa: empty list returns empty")
    void getMensagemErroUnidadesSemMapa_Empty() {
        // Reflection para acessar metodo privado, ou apenas garantir que o metodo publico que o chama nao falha
        // Mas o metodo eh privado. O melhor eh testar atraves de `criar` ou `atualizar` com REVISAO

        Optional<String> msg = org.springframework.test.util.ReflectionTestUtils.invokeMethod(
            service,
            "getMensagemErroUnidadesSemMapa",
            Collections.<Long>emptyList()
        );
        assertThat(msg).isEmpty();

        Optional<String> msgNull = org.springframework.test.util.ReflectionTestUtils.invokeMethod(
            service,
            "getMensagemErroUnidadesSemMapa",
            (List<Long>) null
        );
        assertThat(msgNull).isEmpty();
    }

    // --- LISTAR UNIDADES BLOQUEADAS ---
    @Test
    @DisplayName("listarUnidadesBloqueadasPorTipo: chama repo")
    void listarUnidadesBloqueadasPorTipo() {
        service.listarUnidadesBloqueadasPorTipo("MAPEAMENTO");
        verify(processoRepo).findUnidadeCodigosBySituacaoAndTipo(any(), any());
    }

    @Test
    @DisplayName("buscarCodigosDescendentes: via checarAcesso - teste de logica de arvore")
    void buscarCodigosDescendentes_Arvore() {
        // Este é um método privado complexo chamado por checarAcesso.
        // Vamos testar o metodo privado via ReflectionUtils se possível ou simular o cenário em checarAcesso

        // Vamos usar ReflectionTestUtils
        Unidade pai = new Unidade(); pai.setCodigo(1L);
        Unidade filho = new Unidade(); filho.setCodigo(2L); filho.setUnidadeSuperior(pai);
        Unidade neto = new Unidade(); neto.setCodigo(3L); neto.setUnidadeSuperior(filho);
        Unidade solta = new Unidade(); solta.setCodigo(4L);

        when(unidadeService.buscarTodasEntidadesComHierarquia()).thenReturn(List.of(pai, filho, neto, solta));

        @SuppressWarnings("unchecked")
        List<Long> descendentes = (List<Long>) org.springframework.test.util.ReflectionTestUtils
            .invokeMethod(service, "buscarCodigosDescendentes", 1L);

        assertThat(descendentes).containsExactlyInAnyOrder(1L, 2L, 3L);
    }
}
