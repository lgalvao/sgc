package sgc.processo.painel;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.springframework.data.domain.*;
import sgc.alerta.*;
import sgc.alerta.model.*;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;
import sgc.processo.dto.*;
import sgc.processo.model.*;
import sgc.processo.service.*;
import sgc.testutils.*;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PainelFacade Test")
@SuppressWarnings("NullAway.Init")
class PainelFacadeTest {
    @Mock
    private ProcessoService processoService;
    @Mock
    private AlertaFacade alertaFacade;
    @Mock
    private UnidadeHierarquiaService hierarquiaService;
    @Mock
    private UnidadeService unidadeService;

    @InjectMocks
    private PainelFacade painelFacade;

    @Test
    @DisplayName("Deve listar processos para ADMIN")
    void deveListarProcessosAdmin() {
        Processo p = criarProcesso(1L, SituacaoProcesso.CRIADO);
        Page<Processo> page = new PageImpl<>(List.of(p));
        when(processoService.listarTodos(any(Pageable.class))).thenReturn(page);
        when(hierarquiaService.buscarMapaHierarquia()).thenReturn(new HashMap<>());

        Page<ProcessoResumoDto> result = painelFacade.listarProcessos(Perfil.ADMIN, 100L, PageRequest.of(0, 10));

        assertThat(result).hasSize(1);
        assertThat(result.getContent().getFirst().linkDestino()).isEqualTo("/processo/cadastro?codProcesso=1");
    }

    @Test
    @DisplayName("Deve listar processos para GESTOR")
    void deveListarProcessosGestor() {
        Processo p = criarProcesso(1L, SituacaoProcesso.EM_ANDAMENTO);
        Page<Processo> page = new PageImpl<>(List.of(p));
        when(hierarquiaService.buscarMapaHierarquia()).thenReturn(new HashMap<>());
        when(hierarquiaService.buscarDescendentes(eq(100L), anyMap())).thenReturn(List.of(101L));
        when(processoService.listarIniciadosPorParticipantes(anyList(), any(Pageable.class))).thenReturn(page);

        Page<ProcessoResumoDto> result = painelFacade.listarProcessos(Perfil.GESTOR, 100L, PageRequest.of(0, 10));

        assertThat(result).hasSize(1);
        assertThat(result.getContent().getFirst().linkDestino()).isEqualTo("/processo/1");
    }

    @Test
    @DisplayName("Deve listar processo de revisão para CHEFE com link direto ao subprocesso")
    void deveListarProcessoRevisaoParaChefe() {
        Processo p = criarProcesso(1L, SituacaoProcesso.EM_ANDAMENTO);
        p.setTipo(TipoProcesso.REVISAO);
        Page<Processo> page = new PageImpl<>(List.of(p));
        Unidade unidade = UnidadeTestBuilder.umaDe()
                .comCodigo("100")
                .comSigla("ASSESSORIA_22")
                .comNome("Assessoria 22")
                .build();

        when(hierarquiaService.buscarMapaHierarquia()).thenReturn(new HashMap<>());
        when(processoService.listarIniciadosPorParticipantes(anyList(), any(Pageable.class))).thenReturn(page);
        when(unidadeService.buscarPorCodigo(100L)).thenReturn(unidade);

        Page<ProcessoResumoDto> result = painelFacade.listarProcessos(Perfil.CHEFE, 100L, PageRequest.of(0, 10));

        assertThat(result).hasSize(1);
        assertThat(result.getContent().getFirst().linkDestino()).isEqualTo("/processo/1/ASSESSORIA_22");
    }

    @Test
    @DisplayName("Deve propagar erro ao calcular link sem fallback")
    void devePropagarErroAoCalcularLink() {
        Processo p = criarProcesso(1L, SituacaoProcesso.EM_ANDAMENTO);
        Page<Processo> page = new PageImpl<>(List.of(p));

        when(hierarquiaService.buscarMapaHierarquia()).thenReturn(new HashMap<>());
        when(processoService.listarIniciadosPorParticipantes(anyList(), any(Pageable.class))).thenReturn(page);
        when(unidadeService.buscarPorCodigo(100L)).thenThrow(new RuntimeException("Erro"));

        PageRequest pageRequest = PageRequest.of(0, 10);
        assertThatThrownBy(() -> painelFacade.listarProcessos(Perfil.CHEFE, 100L, pageRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Erro");
    }

    @Test
    @DisplayName("Deve listar alertas")
    void deveListarAlertas() {
        Alerta a = new Alerta();
        a.setCodigo(1L);
        a.setProcesso(new Processo());
        a.getProcesso().setCodigo(10L);
        a.getProcesso().setDescricao("Processo teste");
        a.setDescricao("Lembrete de prazo enviado");
        a.setUnidadeOrigem(new Unidade());
        a.getUnidadeOrigem().setSigla("U1");
        a.setUnidadeDestino(new Unidade());
        a.getUnidadeDestino().setSigla("U2");
        a.setDataHora(LocalDateTime.now());

        Page<Alerta> page = new PageImpl<>(List.of(a));
        when(alertaFacade.listarPorUnidade(eq("123"), eq(100L), eq("ADMIN"), any(Pageable.class))).thenReturn(page);
        when(alertaFacade.obterDataHoraLeitura(1L, "123")).thenReturn(Optional.of(LocalDateTime.now()));

        Page<Alerta> result = painelFacade.listarAlertas("123", 100L, "ADMIN", Pageable.unpaged());

        assertThat(result).hasSize(1);
        assertThat(result.getContent().getFirst().getDataHoraLeitura()).isNotNull();
        assertThat(result.getContent().getFirst().getDescricao()).contains("Lembrete");
        assertThat(result.getContent().getFirst().getProcesso().getDescricao()).isEqualTo("Processo teste");
    }

    @Test
    @DisplayName("Deve listar alertas com ordenação definida (não paged ou unsorted)")
    void deveListarAlertasComOrdenacaoDefinida() {
        Pageable sorted = PageRequest.of(0, 10, Sort.by("dataHora"));
        Alerta a = new Alerta();
        a.setCodigo(1L);
        a.setProcesso(new Processo());
        a.getProcesso().setCodigo(10L);
        a.getProcesso().setDescricao("Processo teste");
        a.setDescricao("Lembrete de prazo enviado");
        a.setUnidadeOrigem(new Unidade());
        a.getUnidadeOrigem().setSigla("U1");
        a.setUnidadeDestino(new Unidade());
        a.getUnidadeDestino().setSigla("U2");
        a.setDataHora(LocalDateTime.now());

        Page<Alerta> page = new PageImpl<>(List.of(a));
        when(alertaFacade.listarPorUnidade(eq("123"), eq(100L), eq("ADMIN"), any(Pageable.class))).thenReturn(page);
        when(alertaFacade.obterDataHoraLeitura(1L, "123")).thenReturn(Optional.of(LocalDateTime.now()));

        Page<Alerta> result = painelFacade.listarAlertas("123", 100L, "ADMIN", sorted);
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Deve listar alertas com ordenação padrão (paged e unsorted)")
    void deveListarAlertasComOrdenacaoPadrao() {
        Pageable unsortedPaged = PageRequest.of(0, 10);
        Alerta a = new Alerta();
        a.setCodigo(1L);
        a.setProcesso(new Processo());
        a.getProcesso().setCodigo(10L);
        a.getProcesso().setDescricao("Processo teste");
        a.setDescricao("Lembrete de prazo enviado");
        a.setUnidadeOrigem(new Unidade());
        a.getUnidadeOrigem().setSigla("U1");
        a.setUnidadeDestino(new Unidade());
        a.getUnidadeDestino().setSigla("U2");
        a.setDataHora(LocalDateTime.now());

        Page<Alerta> page = new PageImpl<>(List.of(a));
        when(alertaFacade.listarPorUnidade(eq("123"), eq(100L), eq("ADMIN"), any(Pageable.class))).thenReturn(page);
        when(alertaFacade.obterDataHoraLeitura(1L, "123")).thenReturn(Optional.of(LocalDateTime.now()));

        Page<Alerta> result = painelFacade.listarAlertas("123", 100L, "ADMIN", unsortedPaged);
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Deve lidar com solicitação não paginada")
    void deveLidarComSolicitacaoNaoPaginada() {
        when(processoService.listarTodos(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        Page<ProcessoResumoDto> result = painelFacade.listarProcessos(Perfil.ADMIN, 100L, Pageable.unpaged());

        assertThat(result).isEmpty();
        verify(processoService).listarTodos(Pageable.unpaged());
    }

    @Test
    @DisplayName("Deve cobrir merge function do toMap com participantes duplicados")
    void deveCobrirMergeFunctionComParticipantesDuplicados() {
        Processo p = mock(Processo.class);
        when(p.getCodigo()).thenReturn(1L);
        when(p.getSituacao()).thenReturn(SituacaoProcesso.EM_ANDAMENTO);
        when(p.getTipo()).thenReturn(TipoProcesso.MAPEAMENTO);

        UnidadeProcesso up1 = new UnidadeProcesso();
        up1.setUnidadeCodigo(10L);
        up1.setSigla("U1");

        UnidadeProcesso up2 = new UnidadeProcesso();
        up2.setUnidadeCodigo(10L);
        up2.setSigla("U1");

        when(p.getParticipantes()).thenReturn(List.of(up1, up2));

        when(hierarquiaService.buscarMapaHierarquia()).thenReturn(new HashMap<>());
        when(processoService.listarTodos(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(p)));

        Page<ProcessoResumoDto> result = painelFacade.listarProcessos(Perfil.ADMIN, 100L, PageRequest.of(0, 10));
        assertThat(result.getContent().getFirst().unidadesParticipantes()).contains("U1");
    }

    @Test
    @DisplayName("Deve cobrir lógica de hierarquia visível profunda")
    void deveCobrirLogicaHierarquiaVisivelProfunda() {
        Processo p = mock(Processo.class);
        when(p.getCodigo()).thenReturn(1L);
        when(p.getSituacao()).thenReturn(SituacaoProcesso.EM_ANDAMENTO);
        when(p.getTipo()).thenReturn(TipoProcesso.MAPEAMENTO);

        UnidadeProcesso up1 = new UnidadeProcesso();
        up1.setUnidadeCodigo(1L);
        up1.setSigla("U1");

        UnidadeProcesso up2 = new UnidadeProcesso();
        up2.setUnidadeCodigo(2L);
        up2.setSigla("U2");

        when(p.getParticipantes()).thenReturn(List.of(up1, up2));

        Map<Long, List<Long>> hierarquia = new HashMap<>();
        hierarquia.put(0L, List.of(1L));
        hierarquia.put(1L, List.of(2L));
        hierarquia.put(2L, new ArrayList<>());

        when(hierarquiaService.buscarMapaHierarquia()).thenReturn(hierarquia);
        when(processoService.listarTodos(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(p)));

        Page<ProcessoResumoDto> result = painelFacade.listarProcessos(Perfil.ADMIN, 100L, PageRequest.of(0, 10));
        assertThat(result.getContent().getFirst().unidadesParticipantes()).isEqualTo("U1");
    }

    @Test
    @DisplayName("Deve cobrir sigla ausente no snapshot")
    void deveCobrirSiglaAusente() {
        Processo p = mock(Processo.class);
        when(p.getCodigo()).thenReturn(1L);
        when(p.getSituacao()).thenReturn(SituacaoProcesso.EM_ANDAMENTO);
        when(p.getTipo()).thenReturn(TipoProcesso.MAPEAMENTO);

        UnidadeProcesso up1 = new UnidadeProcesso();
        up1.setUnidadeCodigo(1L);
        up1.setSigla("U1");
        when(p.getParticipantes()).thenReturn(List.of(up1));

        Map<Long, List<Long>> hierarquia = new HashMap<>();
        hierarquia.put(0L, List.of(2L));
        hierarquia.put(2L, List.of(1L));
        hierarquia.put(1L, new ArrayList<>());
        
        when(hierarquiaService.buscarMapaHierarquia()).thenReturn(hierarquia);
        when(processoService.listarTodos(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(p)));
        
        Unidade u2 = new Unidade(); u2.setSigla("U2");
        when(unidadeService.buscarPorCodigos(List.of(2L))).thenReturn(List.of(u2));

        Page<ProcessoResumoDto> result = painelFacade.listarProcessos(Perfil.ADMIN, 100L, PageRequest.of(0, 10));
        assertThat(result.getContent().getFirst().unidadesParticipantes()).isEqualTo("U2");
    }

    @Test
    @DisplayName("Deve cobrir isCovered com children nulo")
    void deveCobrirIsCoveredNull() {
        Processo p = mock(Processo.class);
        when(p.getCodigo()).thenReturn(1L);
        when(p.getSituacao()).thenReturn(SituacaoProcesso.EM_ANDAMENTO);
        when(p.getTipo()).thenReturn(TipoProcesso.MAPEAMENTO);

        UnidadeProcesso up1 = new UnidadeProcesso();
        up1.setUnidadeCodigo(1L);
        up1.setSigla("U1");
        when(p.getParticipantes()).thenReturn(List.of(up1));

        Map<Long, List<Long>> hierarquia = new HashMap<>();
        hierarquia.put(0L, List.of(2L));

        when(hierarquiaService.buscarMapaHierarquia()).thenReturn(hierarquia);
        when(processoService.listarTodos(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(p)));

        Page<ProcessoResumoDto> result = painelFacade.listarProcessos(Perfil.ADMIN, 100L, PageRequest.of(0, 10));
        assertThat(result).isNotEmpty();
    }

    @Test
    @DisplayName("Deve cobrir sigla nula ou em branco")
    void deveCobrirSiglaNulaOuEmBranco() {
        Processo p = mock(Processo.class);
        when(p.getCodigo()).thenReturn(1L);
        when(p.getSituacao()).thenReturn(SituacaoProcesso.EM_ANDAMENTO);
        when(p.getTipo()).thenReturn(TipoProcesso.MAPEAMENTO);

        UnidadeProcesso up1 = new UnidadeProcesso();
        up1.setUnidadeCodigo(1L);
        up1.setSigla(null);

        UnidadeProcesso up2 = new UnidadeProcesso();
        up2.setUnidadeCodigo(2L);
        up2.setSigla(" ");

        when(p.getParticipantes()).thenReturn(List.of(up1, up2));

        Map<Long, List<Long>> hierarquia = new HashMap<>();
        hierarquia.put(0L, List.of(1L, 2L));

        when(hierarquiaService.buscarMapaHierarquia()).thenReturn(hierarquia);
        when(processoService.listarTodos(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(p)));

        Unidade u1 = new Unidade(); u1.setSigla("U1");
        Unidade u2 = new Unidade(); u2.setSigla("U2");
        when(unidadeService.buscarPorCodigos(argThat(list -> list.contains(1L) && list.contains(2L)))).thenReturn(List.of(u1, u2));

        Page<ProcessoResumoDto> result = painelFacade.listarProcessos(Perfil.ADMIN, 100L, PageRequest.of(0, 10));
        assertThat(result.getContent().getFirst().unidadesParticipantes()).contains("U1", "U2");
    }

    @Test
    @DisplayName("Deve cobrir children sendo lista vazia")
    void deveCobrirChildrenVazia() {
        Processo p = mock(Processo.class);
        when(p.getCodigo()).thenReturn(1L);
        when(p.getSituacao()).thenReturn(SituacaoProcesso.EM_ANDAMENTO);
        when(p.getTipo()).thenReturn(TipoProcesso.MAPEAMENTO);

        UnidadeProcesso up1 = new UnidadeProcesso();
        up1.setUnidadeCodigo(1L);
        up1.setSigla("U1");
        when(p.getParticipantes()).thenReturn(List.of(up1));

        Map<Long, List<Long>> hierarquia = new HashMap<>();
        hierarquia.put(0L, List.of(2L));
        hierarquia.put(2L, Collections.emptyList());

        when(hierarquiaService.buscarMapaHierarquia()).thenReturn(hierarquia);
        when(processoService.listarTodos(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(p)));

        painelFacade.listarProcessos(Perfil.ADMIN, 100L, PageRequest.of(0, 10));
        verify(processoService).listarTodos(any());
    }

    @Test
    @DisplayName("Deve cobrir ordenacao customizada")
    void deveCobrirOrdenacaoCustomizada() {
        when(hierarquiaService.buscarMapaHierarquia()).thenReturn(new HashMap<>());
        when(processoService.listarTodos(any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

        Pageable sorted = PageRequest.of(0, 10, Sort.by("descricao"));
        painelFacade.listarProcessos(Perfil.ADMIN, 100L, sorted);
        
        verify(processoService).listarTodos(sorted);
    }

    @Test
    @DisplayName("Deve cobrir cache do isCovered")
    void deveCobrirCacheIsCovered() {
        Processo p = mock(Processo.class);
        when(p.getCodigo()).thenReturn(1L);
        when(p.getSituacao()).thenReturn(SituacaoProcesso.EM_ANDAMENTO);
        when(p.getTipo()).thenReturn(TipoProcesso.MAPEAMENTO);

        UnidadeProcesso up1 = new UnidadeProcesso();
        up1.setUnidadeCodigo(1L);
        up1.setSigla("U1");
        UnidadeProcesso up2 = new UnidadeProcesso();
        up2.setUnidadeCodigo(2L);
        up2.setSigla("U2");
        
        when(p.getParticipantes()).thenReturn(List.of(up1, up2));

        Map<Long, List<Long>> hierarquia = new HashMap<>();
        hierarquia.put(4L, List.of(3L));
        hierarquia.put(3L, List.of(1L, 2L));
        hierarquia.put(1L, new ArrayList<>());
        hierarquia.put(2L, new ArrayList<>());
        
        when(hierarquiaService.buscarMapaHierarquia()).thenReturn(hierarquia);
        when(processoService.listarTodos(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(p)));

        painelFacade.listarProcessos(Perfil.ADMIN, 100L, PageRequest.of(0, 10));
        verify(processoService).listarTodos(any());
    }

    private Processo criarProcesso(Long codigo, SituacaoProcesso situacao) {
        Processo p = new Processo();
        p.setCodigo(codigo);
        p.setSituacao(situacao);
        p.setTipo(TipoProcesso.MAPEAMENTO);
        p.setDataCriacao(LocalDateTime.now());
        Unidade u = UnidadeTestBuilder.umaDe()
                .comCodigo("10")
                .comSigla("U")
                .comNome("Unit")
                .build();
        p.adicionarParticipantes(Set.of(u));
        return p;
    }
}
