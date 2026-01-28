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
import sgc.processo.service.ProcessoFacade;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
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

        Page<ProcessoResumoDto> result = painelFacade.listarProcessos(Perfil.ADMIN, 100L, PageRequest.of(0, 10));

        assertThat(result).hasSize(1);
        assertThat(result.getContent().get(0).linkDestino()).isEqualTo("/processo/cadastro?codProcesso=1");
    }

    @Test
    @DisplayName("Deve listar processos para GESTOR")
    void deveListarProcessosGestor() {
        Processo p = criarProcesso(1L, SituacaoProcesso.EM_ANDAMENTO);
        Page<Processo> page = new PageImpl<>(List.of(p));
        when(unidadeFacade.buscarIdsDescendentes(100L)).thenReturn(List.of(101L));
        when(processoFacade.listarPorParticipantesIgnorandoCriado(anyList(), any(Pageable.class))).thenReturn(page);

        Page<ProcessoResumoDto> result = painelFacade.listarProcessos(Perfil.GESTOR, 100L, PageRequest.of(0, 10));

        assertThat(result).hasSize(1);
        assertThat(result.getContent().get(0).linkDestino()).isEqualTo("/processo/1");
    }

    @Test
    @DisplayName("Deve lidar com exceção ao calcular link")
    void deveLidarComExcecaoAoCalcularLink() {
        Processo p = criarProcesso(1L, SituacaoProcesso.EM_ANDAMENTO);
        Page<Processo> page = new PageImpl<>(List.of(p));

        // Simular exceção ao buscar unidade para link (usado para perfis não ADMIN/GESTOR)
        // Mas listarProcessos chama unidadeService.buscarIdsDescendentes para GESTOR.
        // Para CHEFE chama apenas para propria unidade.

        when(processoFacade.listarPorParticipantesIgnorandoCriado(anyList(), any(Pageable.class))).thenReturn(page);
        when(unidadeFacade.buscarPorCodigo(100L)).thenThrow(new RuntimeException("Erro"));

        Page<ProcessoResumoDto> result = painelFacade.listarProcessos(Perfil.CHEFE, 100L, PageRequest.of(0, 10));

        // Link deve ser null
        assertThat(result.getContent().get(0).linkDestino()).isNull();
    }

    @Test
    @DisplayName("Deve listar alertas")
    void deveListarAlertas() {
        Alerta a = new Alerta();
        a.setCodigo(1L);
        a.setProcesso(new Processo());
        a.getProcesso().setCodigo(10L);
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
        assertThat(result.getContent().get(0).getDataHoraLeitura()).isNotNull();
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
                .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of()));

        Page<ProcessoResumoDto> result = painelFacade.listarProcessos(Perfil.ADMIN, 100L, Pageable.unpaged());

        assertThat(result).isEmpty();
        verify(processoFacade).listarTodos(Pageable.unpaged());
    }

    private Processo criarProcesso(Long codigo, SituacaoProcesso situacao) {
        Processo p = new Processo();
        p.setCodigo(codigo);
        p.setSituacao(situacao);
        p.setTipo(TipoProcesso.MAPEAMENTO);
        p.setDataCriacao(LocalDateTime.now());
        Unidade u = new Unidade();
        u.setCodigo(10L);
        u.setNome("Unit");
        u.setSigla("U");
        p.setParticipantes(new java.util.HashSet<>(Set.of(u)));
        return p;
    }
}
