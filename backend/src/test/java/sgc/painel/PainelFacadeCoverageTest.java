package sgc.painel;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import sgc.alerta.AlertaFacade;
import sgc.alerta.dto.AlertaDto;
import sgc.alerta.model.Alerta;
import sgc.organizacao.UnidadeFacade;
import sgc.organizacao.model.Perfil;
import sgc.processo.dto.ProcessoResumoDto;
import sgc.processo.model.Processo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.processo.service.ProcessoFacade;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.data.domain.Sort;
import sgc.organizacao.dto.UnidadeDto;
import sgc.organizacao.model.Unidade;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("Testes de Cobertura para PainelFacade")
class PainelFacadeCoverageTest {

    @InjectMocks
    private PainelFacade painelFacade;

    @Mock private AlertaFacade alertaService;
    @Mock private ProcessoFacade processoFacade;
    @Mock private UnidadeFacade unidadeService;

    @Test
    @DisplayName("Deve mapear AlertaDto corretamente")
    void deveMapearAlertaDtoCorretamente() {
        Processo p = new Processo();
        p.setCodigo(55L);
        
        Unidade origem = new Unidade();
        origem.setSigla("ORG");
        
        Unidade destino = new Unidade();
        destino.setSigla("DST");

        Alerta a = new Alerta();
        a.setCodigo(100L);
        a.setDescricao("D");
        a.setUnidadeOrigem(origem);
        a.setUnidadeDestino(destino);
        a.setProcesso(p);

        Pageable pageable = PageRequest.of(0, 10);
        when(alertaService.listarPorUnidade(eq(1L), any())).thenReturn(new PageImpl<>(List.of(a)));
        when(alertaService.obterDataHoraLeitura(anyLong(), anyString())).thenReturn(Optional.empty());

        Page<AlertaDto> result = painelFacade.listarAlertas("T", 1L, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getUnidadeOrigem()).isEqualTo("ORG");
        assertThat(result.getContent().getFirst().getUnidadeDestino()).isEqualTo("DST");
        assertThat(result.getContent().getFirst().getCodProcesso()).isEqualTo(55L);
    }

    @Test
    @DisplayName("Deve calcular link para ADMIN e processo CRIADO")
    void deveCalcularLinkAdminCriado() {
        Processo p = new Processo();
        p.setCodigo(10L);
        p.setSituacao(SituacaoProcesso.CRIADO);
        p.setTipo(TipoProcesso.MAPEAMENTO); // Inicializando tipo para evitar NPE se mapper usar
        
        Unidade u = new Unidade();
        u.setCodigo(1L);
        u.setSigla("U1");
        u.setNome("Unidade 1");
        p.getParticipantes().add(u);
        
        when(processoFacade.listarTodos(any())).thenReturn(new PageImpl<>(List.of(p)));

        Page<ProcessoResumoDto> result = painelFacade.listarProcessos(Perfil.ADMIN, null, PageRequest.of(0, 10));
        assertThat(result.getContent().getFirst().linkDestino()).isEqualTo("/processo/cadastro?codProcesso=10");
    }

    @Test
    @DisplayName("Deve calcular link com erro e retornar null")
    void deveCalcularLinkErro() {
        Processo p = new Processo();
        p.setCodigo(10L);
        p.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        p.setTipo(TipoProcesso.MAPEAMENTO);

        Unidade u = new Unidade();
        u.setCodigo(1L);
        u.setSigla("U1");
        p.getParticipantes().add(u);

        when(processoFacade.listarPorParticipantesIgnorandoCriado(any(), any())).thenReturn(new PageImpl<>(List.of(p)));
        // Simular erro ao buscar unidade
        when(unidadeService.buscarPorCodigo(anyLong())).thenThrow(new RuntimeException("DB Error"));

        Page<ProcessoResumoDto> result = painelFacade.listarProcessos(Perfil.SERVIDOR, 1L, PageRequest.of(0, 10));
        assertThat(result.getContent().getFirst().linkDestino()).isNull();
    }

    @Test
    @DisplayName("Deve listar alertas com pageable não ordenado")
    void deveListarAlertasUnsorted() {
        // Mock comportamento normal do alertaService
        when(alertaService.listarPorUnidade(any(), any())).thenReturn(new PageImpl<>(List.of()));

        // Passar pageable sem sort
        painelFacade.listarAlertas("T", 1L, PageRequest.of(0, 10));

        // Verifica se chamou alertaService com sort adicionado
        verify(alertaService).listarPorUnidade(eq(1L), argThat(p -> p.getSort().isSorted()));
    }

    @Test
    @DisplayName("Deve formatar unidades com hierarquia complexa (loop infinito visualização)")
    void deveFormatarUnidadesComHierarquiaComplexa() {
        Processo p = new Processo();
        p.setCodigo(1L);
        p.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        p.setTipo(TipoProcesso.MAPEAMENTO);

        // A -> B -> C
        Unidade a = new Unidade();
        a.setCodigo(1L);
        a.setSigla("A");

        Unidade b = new Unidade();
        b.setCodigo(2L);
        b.setSigla("B");
        b.setUnidadeSuperior(a);

        Unidade c = new Unidade();
        c.setCodigo(3L);
        c.setSigla("C");
        c.setUnidadeSuperior(b);

        // Todos participam
        p.getParticipantes().add(a);
        p.getParticipantes().add(b);
        p.getParticipantes().add(c);

        when(processoFacade.listarPorParticipantesIgnorandoCriado(any(), any())).thenReturn(new PageImpl<>(List.of(p)));
        // Mock buscarIdsDescendentes para suportar a lógica de 'todasSubordinadasParticipam'
        // A tem B (e C via recursao, mas o metodo recebe so diretos? Nao, 'buscarIdsDescendentes' retorna todos)
        when(unidadeService.buscarIdsDescendentes(1L)).thenReturn(List.of(1L, 2L, 3L));
        when(unidadeService.buscarIdsDescendentes(2L)).thenReturn(List.of(2L, 3L));
        when(unidadeService.buscarIdsDescendentes(3L)).thenReturn(List.of(3L));

        UnidadeDto aDto = UnidadeDto.builder().sigla("A").build();
        when(unidadeService.buscarPorCodigo(anyLong())).thenReturn(aDto);

        Page<ProcessoResumoDto> result = painelFacade.listarProcessos(Perfil.SERVIDOR, 1L, PageRequest.of(0, 10));

        // Se A engloba B e B engloba C, e todos participam, então deve aparecer apenas "A"
        assertThat(result.getContent()).hasSize(1);
        String unidadesStr = result.getContent().getFirst().unidadesParticipantes();
        assertThat(unidadesStr).isEqualTo("A");
    }

    @Test
    @DisplayName("Deve manter ordenação se já definida no Pageable")
    void deveManterOrdenacaoSeJaDefinida() {
        Processo p = new Processo();
        p.setCodigo(1L);
        p.setTipo(TipoProcesso.MAPEAMENTO);

        // Unidade participante para evitar NPE
        Unidade u = new Unidade();
        u.setCodigo(1L);
        u.setSigla("U");
        p.getParticipantes().add(u);

        when(processoFacade.listarTodos(any())).thenReturn(new PageImpl<>(List.of(p)));

        Pageable sorted = PageRequest.of(0, 10, Sort.by("codigo"));

        painelFacade.listarProcessos(Perfil.ADMIN, null, sorted);

        // Verifica se chamou com a ordenação original
        verify(processoFacade).listarTodos(eq(sorted));
    }
}
