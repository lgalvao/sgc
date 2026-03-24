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

        // Simular exceção ao buscar unidade para link (usado para perfis não ADMIN/GESTOR)
        // Mas listarProcessos chama unidadeService.buscarIdsDescendentes para GESTOR.
        // Para CHEFE chama apenas para propria unidade.

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
        // Paged e Unsorted
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

        UnidadeProcesso up1 = mock(UnidadeProcesso.class);
        when(up1.getUnidadeCodigo()).thenReturn(10L);
        when(up1.getSigla()).thenReturn("U1");

        UnidadeProcesso up2 = mock(UnidadeProcesso.class);
        when(up2.getUnidadeCodigo()).thenReturn(10L); // Mesmo código
        when(up2.getSigla()).thenReturn("U1"); // Mock de sigla

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

        UnidadeProcesso up1 = mock(UnidadeProcesso.class);
        when(up1.getUnidadeCodigo()).thenReturn(1L);
        when(up1.getSigla()).thenReturn("U1");

        UnidadeProcesso up2 = mock(UnidadeProcesso.class);
        when(up2.getUnidadeCodigo()).thenReturn(2L);
        when(up2.getSigla()).thenReturn("U2");

        when(p.getParticipantes()).thenReturn(List.of(up1, up2));

        Map<Long, List<Long>> hierarquia = new HashMap<>();
        hierarquia.put(0L, List.of(1L)); // 0L é a raiz, 1L é o nível abaixo da raiz
        hierarquia.put(1L, List.of(2L)); // U1 tem U2 como filho
        hierarquia.put(2L, new ArrayList<>());

        when(hierarquiaService.buscarMapaHierarquia()).thenReturn(hierarquia);
        when(processoService.listarTodos(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(p)));

        Page<ProcessoResumoDto> result = painelFacade.listarProcessos(Perfil.ADMIN, 100L, PageRequest.of(0, 10));

        // Se U2 participa e U1 participa, e U2 é única subordinada de U1, deve mostrar apenas U1
        assertThat(result.getContent().getFirst().unidadesParticipantes()).isEqualTo("U1");
    }

    @Test
    @DisplayName("Deve cobrir sigla ausente no snapshot")
    void deveCobrirSiglaAusente() {
        Processo p = mock(Processo.class);
        when(p.getCodigo()).thenReturn(1L);
        when(p.getSituacao()).thenReturn(SituacaoProcesso.EM_ANDAMENTO);
        when(p.getTipo()).thenReturn(TipoProcesso.MAPEAMENTO);

        UnidadeProcesso up1 = mock(UnidadeProcesso.class);
        when(up1.getUnidadeCodigo()).thenReturn(1L);
        when(up1.getSigla()).thenReturn("U1");
        when(p.getParticipantes()).thenReturn(List.of(up1));

        Map<Long, List<Long>> hierarquia = new HashMap<>();
        hierarquia.put(0L, List.of(1L)); 
        // 1L tem pai 0L. Agrupamento pára em 1L.
        // isCovered(0L) vai dar false porque não tem participantesIds.contains(0L)
        
        when(hierarquiaService.buscarMapaHierarquia()).thenReturn(hierarquia);
        when(processoService.listarTodos(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(p)));
        
        // Simular que uma unidade (ID 2) "subiu" no agrupamento mas não está no snapshot
        // Para isso, precisamos que 2L seja pai de 1L e 2L seja "covered"
        hierarquia.clear();
        hierarquia.put(0L, List.of(2L));
        hierarquia.put(2L, List.of(1L));
        hierarquia.put(1L, new ArrayList<>());
        
        Unidade u2 = new Unidade(); u2.setSigla("U2");
        when(unidadeService.porCodigos(List.of(2L))).thenReturn(List.of(u2));

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

        UnidadeProcesso up1 = mock(UnidadeProcesso.class);
        when(up1.getUnidadeCodigo()).thenReturn(1L);
        when(up1.getSigla()).thenReturn("U1");
        when(p.getParticipantes()).thenReturn(List.of(up1));

        Map<Long, List<Long>> hierarquia = new HashMap<>();
        // 1L tem pai 2L, e 2L tem pai 0L.
        // Se 2L NÃO estiver no mapa, mapaPaiFilhos.get(2L) será null -> aciona line 186 children == null
        hierarquia.put(0L, List.of(2L));
        // hierarquia.put(2L, ...) AUSENTE propositalmente
        
        when(hierarquiaService.buscarMapaHierarquia()).thenReturn(hierarquia);
        when(processoService.listarTodos(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(p)));

        Page<ProcessoResumoDto> result = painelFacade.listarProcessos(Perfil.ADMIN, 100L, PageRequest.of(0, 10));
        assertThat(result).isNotEmpty();
    }

    @Test
    @DisplayName("Deve cobrir ordenacao customizada")
    void deveCobrirOrdenacaoCustomizada() {
        when(hierarquiaService.buscarMapaHierarquia()).thenReturn(new HashMap<>());
        when(processoService.listarTodos(any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

        // Pageable com sort -> aciona branch false na line 63
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

        // U1 e U2 participam. U3 é pai de U1 e U2. U4 é pai de U3.
        UnidadeProcesso up1 = mock(UnidadeProcesso.class);
        when(up1.getUnidadeCodigo()).thenReturn(1L);
        when(up1.getSigla()).thenReturn("U1");
        UnidadeProcesso up2 = mock(UnidadeProcesso.class);
        when(up2.getUnidadeCodigo()).thenReturn(2L);
        when(up2.getSigla()).thenReturn("U2");
        
        when(p.getParticipantes()).thenReturn(List.of(up1, up2));

        Map<Long, List<Long>> hierarquia = new HashMap<>();
        hierarquia.put(4L, List.of(3L));
        hierarquia.put(3L, List.of(1L, 2L));
        hierarquia.put(1L, new ArrayList<>());
        hierarquia.put(2L, new ArrayList<>());
        
        when(hierarquiaService.buscarMapaHierarquia()).thenReturn(hierarquia);
        when(processoService.listarTodos(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(p)));

        // Isso vai forçar múltiplas chamadas de isCovered para o mesmo nó dependendo da ordem
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
