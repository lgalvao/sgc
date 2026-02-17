package sgc.painel;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import sgc.alerta.AlertaFacade;
import sgc.alerta.dto.AlertaDto;
import sgc.alerta.model.Alerta;
import sgc.organizacao.UnidadeFacade;
import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.Unidade;
import sgc.processo.dto.ProcessoResumoDto;
import sgc.processo.model.Processo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.processo.model.UnidadeProcesso;
import sgc.processo.service.ProcessoFacade;
import sgc.testutils.UnidadeTestBuilder;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("PainelFacade Test")
class PainelFacadeTest {

    @Mock
    private ProcessoFacade processoFacade;
    @Mock
    private AlertaFacade alertaFacade;
    @Mock
    private UnidadeFacade unidadeFacade;

    @InjectMocks
    private PainelFacade painelFacade;

    @Test
    @DisplayName("Deve listar processos para ADMIN")
    void deveListarProcessosAdmin() {
        Processo p = criarProcesso(1L, SituacaoProcesso.CRIADO);
        Page<Processo> page = new PageImpl<>(List.of(p));
        when(processoFacade.listarTodos(any(Pageable.class))).thenReturn(page);
        when(unidadeFacade.buscarMapaHierarquia()).thenReturn(new HashMap<>());

        Page<ProcessoResumoDto> result = painelFacade.listarProcessos(Perfil.ADMIN, 100L, PageRequest.of(0, 10));

        assertThat(result).hasSize(1);
        assertThat(result.getContent().getFirst().linkDestino()).isEqualTo("/processo/cadastro?codProcesso=1");
    }

    @Test
    @DisplayName("Deve listar processos para GESTOR")
    void deveListarProcessosGestor() {
        Processo p = criarProcesso(1L, SituacaoProcesso.EM_ANDAMENTO);
        Page<Processo> page = new PageImpl<>(List.of(p));
        when(unidadeFacade.buscarMapaHierarquia()).thenReturn(new HashMap<>());
        when(unidadeFacade.buscarIdsDescendentes(eq(100L), anyMap())).thenReturn(List.of(101L));
        when(processoFacade.listarPorParticipantesIgnorandoCriado(anyList(), any(Pageable.class))).thenReturn(page);

        Page<ProcessoResumoDto> result = painelFacade.listarProcessos(Perfil.GESTOR, 100L, PageRequest.of(0, 10));

        assertThat(result).hasSize(1);
        assertThat(result.getContent().getFirst().linkDestino()).isEqualTo("/processo/1");
    }

    @Test
    @DisplayName("Deve propagar erro ao calcular link sem fallback")
    void devePropagarErroAoCalcularLink() {
        Processo p = criarProcesso(1L, SituacaoProcesso.EM_ANDAMENTO);
        Page<Processo> page = new PageImpl<>(List.of(p));

        // Simular exceção ao buscar unidade para link (usado para perfis não ADMIN/GESTOR)
        // Mas listarProcessos chama unidadeService.buscarIdsDescendentes para GESTOR.
        // Para CHEFE chama apenas para propria unidade.

        when(unidadeFacade.buscarMapaHierarquia()).thenReturn(new HashMap<>());
        when(processoFacade.listarPorParticipantesIgnorandoCriado(anyList(), any(Pageable.class))).thenReturn(page);
        when(unidadeFacade.buscarPorCodigo(100L)).thenThrow(new RuntimeException("Erro"));

        assertThatThrownBy(() -> painelFacade.listarProcessos(Perfil.CHEFE, 100L, PageRequest.of(0, 10)))
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
        when(alertaFacade.listarPorUnidade(eq(100L), any(Pageable.class))).thenReturn(page);
        when(alertaFacade.obterDataHoraLeitura(1L, "123")).thenReturn(Optional.of(LocalDateTime.now()));

        Page<AlertaDto> result = painelFacade.listarAlertas("123", 100L, Pageable.unpaged());

        assertThat(result).hasSize(1);
        assertThat(result.getContent().getFirst().getDataHoraLeitura()).isNotNull();
        assertThat(result.getContent().getFirst().getMensagem()).contains("Lembrete");
        assertThat(result.getContent().getFirst().getProcesso()).isEqualTo("Processo teste");
    }

    @Test
    @DisplayName("Deve listar alertas com ordenação definida (não paged ou unsorted)")
    void deveListarAlertasComOrdenacaoDefinida() {
        // Caso 2: Sorted.
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
        // Mock deve esperar sortedPageable que é igual ao sorted passado (pois não entra no if)
        when(alertaFacade.listarPorUnidade(100L, sorted)).thenReturn(page);
        when(alertaFacade.obterDataHoraLeitura(1L, "123")).thenReturn(Optional.of(LocalDateTime.now()));

        Page<AlertaDto> result = painelFacade.listarAlertas("123", 100L, sorted);

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

        // Mock deve esperar um pageable SORTED, pois o método aplica sort default
        when(alertaFacade.listarPorUnidade(eq(100L), argThat(p ->
                p.isPaged() && p.getSort().isSorted() && p.getSort().getOrderFor("dataHora") != null
        ))).thenReturn(page);

        when(alertaFacade.obterDataHoraLeitura(1L, "123")).thenReturn(Optional.of(LocalDateTime.now()));

        Page<AlertaDto> result = painelFacade.listarAlertas("123", 100L, unsortedPaged);

        assertThat(result).hasSize(1);
    }



    @Test
    @DisplayName("Deve lidar com solicitação não paginada")
    void deveLidarComSolicitacaoNaoPaginada() {
        when(processoFacade.listarTodos(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        Page<ProcessoResumoDto> result = painelFacade.listarProcessos(Perfil.ADMIN, 100L, Pageable.unpaged());

        assertThat(result).isEmpty();
        verify(processoFacade).listarTodos(Pageable.unpaged());
    }

    @Test
    @DisplayName("Deve cobrir merge function do toMap com participantes duplicados")
    void deveCobrirMergeFunctionComParticipantesDuplicados() {
        // Arrange
        Processo p = mock(Processo.class);
        when(p.getCodigo()).thenReturn(1L);
        when(p.getSituacao()).thenReturn(SituacaoProcesso.EM_ANDAMENTO);
        when(p.getTipo()).thenReturn(TipoProcesso.MAPEAMENTO);
        
        UnidadeProcesso up1 = mock(UnidadeProcesso.class);
        when(up1.getUnidadeCodigo()).thenReturn(10L);
        when(up1.getSigla()).thenReturn("U1");
        
        UnidadeProcesso up2 = mock(UnidadeProcesso.class);
        when(up2.getUnidadeCodigo()).thenReturn(10L); // Mesmo código

        // Retorna lista com duplicados
        when(p.getParticipantes()).thenReturn(List.of(up1, up2));

        when(unidadeFacade.buscarMapaHierarquia()).thenReturn(new HashMap<>());
        when(processoFacade.listarTodos(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(p)));

        // Act
        Page<ProcessoResumoDto> result = painelFacade.listarProcessos(Perfil.ADMIN, 100L, PageRequest.of(0, 10));

        // Assert
        assertThat(result.getContent().get(0).unidadesParticipantes()).contains("U1");
    }

    @Test
    @DisplayName("Deve cobrir lógica de hierarquia visível profunda")
    void deveCobrirLogicaHierarquiaVisivelProfunda() {
        // Arrange
        Processo p = mock(Processo.class);
        when(p.getCodigo()).thenReturn(1L);
        when(p.getSituacao()).thenReturn(SituacaoProcesso.EM_ANDAMENTO);
        when(p.getTipo()).thenReturn(TipoProcesso.MAPEAMENTO);

        // U1 (Pai) -> U2 (Filho)
        UnidadeProcesso up1 = mock(UnidadeProcesso.class);
        when(up1.getUnidadeCodigo()).thenReturn(1L);
        when(up1.getSigla()).thenReturn("U1");
        when(up1.getUnidadeSuperiorCodigo()).thenReturn(null);

        UnidadeProcesso up2 = mock(UnidadeProcesso.class);
        when(up2.getUnidadeCodigo()).thenReturn(2L);
        when(up2.getUnidadeSuperiorCodigo()).thenReturn(1L);

        when(p.getParticipantes()).thenReturn(List.of(up1, up2));

        Map<Long, List<Long>> hierarquia = new HashMap<>();
        hierarquia.put(1L, List.of(2L)); // U1 tem U2 como filho
        hierarquia.put(2L, new ArrayList<>());

        when(unidadeFacade.buscarMapaHierarquia()).thenReturn(hierarquia);
        when(unidadeFacade.buscarIdsDescendentes(eq(1L), anyMap())).thenReturn(List.of(2L));
        when(unidadeFacade.buscarIdsDescendentes(eq(2L), anyMap())).thenReturn(new ArrayList<>());
        when(processoFacade.listarTodos(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(p)));

        // Act
        Page<ProcessoResumoDto> result = painelFacade.listarProcessos(Perfil.ADMIN, 100L, PageRequest.of(0, 10));

        // Assert
        // Se U2 participa e U1 participa, e U2 é única subordinada de U1, deve mostrar apenas U1
        assertThat(result.getContent().get(0).unidadesParticipantes()).isEqualTo("U1");
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
