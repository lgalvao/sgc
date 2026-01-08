package sgc.painel;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import sgc.alerta.AlertaService;
import sgc.alerta.dto.AlertaDto;
import sgc.alerta.model.Alerta;
import sgc.organizacao.UnidadeService;
import sgc.organizacao.dto.UnidadeDto;
import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.Unidade;
import sgc.processo.dto.ProcessoResumoDto;
import sgc.processo.model.Processo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.processo.service.ProcessoFacade;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PainelServiceCoverageTest {

    @InjectMocks
    private PainelService service;

    @Mock
    private ProcessoFacade processoFacade;
    @Mock
    private AlertaService alertaService;
    @Mock
    private UnidadeService unidadeService;

    private Pageable pageable = PageRequest.of(0, 10);

    @Test
    @DisplayName("listarProcessos: Perfil GESTOR deve buscar subordinadas")
    void listarProcessos_GestorBuscaSubordinadas() {
        Long codigoUnidade = 1L;
        // Mock new optimized method
        when(unidadeService.buscarIdsDescendentes(codigoUnidade)).thenReturn(List.of(2L));

        Processo p = new Processo();
        p.setCodigo(100L);
        p.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        p.setTipo(TipoProcesso.MAPEAMENTO);
        p.setParticipantes(Set.of());

        when(processoFacade.listarPorParticipantesIgnorandoCriado(anyList(), any())).thenReturn(new PageImpl<>(List.of(p)));

        Page<ProcessoResumoDto> result = service.listarProcessos(Perfil.GESTOR, codigoUnidade, pageable);

        assertThat(result.getContent()).isNotEmpty();
        verify(unidadeService).buscarIdsDescendentes(codigoUnidade);
        verify(processoFacade).listarPorParticipantesIgnorandoCriado(
                argThat(list -> list.contains(1L) && list.contains(2L)), any());
    }

    @Test
    @DisplayName("listarProcessos: Perfil CHEFE retorna link com sigla")
    void listarProcessos_ChefeRetornaLinkComSigla() {
        Long codigoUnidade = 1L;
        UnidadeDto unidadeDto = UnidadeDto.builder().sigla("SIGLA").build();
        when(unidadeService.buscarPorCodigo(codigoUnidade)).thenReturn(unidadeDto);

        Processo p = new Processo();
        p.setCodigo(100L);
        p.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        p.setTipo(TipoProcesso.MAPEAMENTO);
        p.setParticipantes(Set.of());

        when(processoFacade.listarPorParticipantesIgnorandoCriado(anyList(), any())).thenReturn(new PageImpl<>(List.of(p)));

        Page<ProcessoResumoDto> result = service.listarProcessos(Perfil.CHEFE, codigoUnidade, pageable);

        assertThat(result.getContent().get(0).getLinkDestino()).isEqualTo("/processo/100/SIGLA");
    }

    @Test
    @DisplayName("listarProcessos: Link destino nulo se unidade não encontrada para Chefe")
    void listarProcessos_LinkNuloSeUnidadeNaoEncontrada() {
        Long codigoUnidade = 1L;
        when(unidadeService.buscarPorCodigo(codigoUnidade)).thenThrow(new RuntimeException("Erro"));

        Processo p = new Processo();
        p.setCodigo(100L);
        p.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        p.setTipo(TipoProcesso.MAPEAMENTO);

        when(processoFacade.listarPorParticipantesIgnorandoCriado(anyList(), any())).thenReturn(new PageImpl<>(List.of(p)));

        Page<ProcessoResumoDto> result = service.listarProcessos(Perfil.CHEFE, codigoUnidade, pageable);

        assertThat(result.getContent().get(0).getLinkDestino()).isNull();
    }

    @Test
    @DisplayName("listarAlertas: busca por unidade se codigoUnidade informado")
    void listarAlertas_PorUnidade() {
        Long codigoUnidade = 1L;

        Alerta alerta = new Alerta();
        alerta.setCodigo(1L);
        alerta.setDataHora(LocalDateTime.now());

        when(alertaService.listarPorUnidade(eq(codigoUnidade), any())).thenReturn(new PageImpl<>(List.of(alerta)));

        Page<AlertaDto> result = service.listarAlertas(null, codigoUnidade, pageable);

        assertThat(result.getContent()).isNotEmpty();
        verify(alertaService).listarPorUnidade(eq(codigoUnidade), any());
    }

    @Test
    @DisplayName("listarAlertas: retorna vazio se titulo e unidade nulos")
    void listarAlertas_SemFiltrosRetornaVazio() {
        Page<AlertaDto> result = service.listarAlertas(null, null, pageable);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("formatarUnidadesParticipantes: deve formatar corretamente e agrupar hierarquia")
    void formatarUnidadesParticipantes_Complexa() {
        // Setup hierarquia: PAI -> FILHO. Ambas participam. Só PAI deve aparecer?
        // Lógica diz: se PAI participa e contém FILHO, e FILHO participa...
        // encontrarMaiorIdVisivel tenta subir.

        Unidade pai = new Unidade(); pai.setCodigo(1L); pai.setSigla("PAI");
        Unidade filho = new Unidade(); filho.setCodigo(2L); filho.setSigla("FILHO");
        filho.setUnidadeSuperior(pai);

        // Mock buscarIdsDescendentes for todasSubordinadasParticipam
        when(unidadeService.buscarIdsDescendentes(1L)).thenReturn(List.of(2L));
        when(unidadeService.buscarIdsDescendentes(2L)).thenReturn(Collections.emptyList());

        // Mock buscarEntidadePorId para o loop
        when(unidadeService.buscarEntidadePorId(1L)).thenReturn(pai);
        when(unidadeService.buscarEntidadePorId(2L)).thenReturn(filho);

        Processo p = new Processo();
        p.setCodigo(100L);
        p.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        p.setTipo(TipoProcesso.MAPEAMENTO);
        p.setParticipantes(Set.of(pai, filho));

        when(processoFacade.listarTodos(any())).thenReturn(new PageImpl<>(List.of(p)));

        Page<ProcessoResumoDto> result = service.listarProcessos(Perfil.ADMIN, null, pageable);

        // Se PAI e FILHO participam, e FILHO é subordinada de PAI:
        // encontrarMaiorIdVisivel(FILHO): PAI participa? Sim. Sobe para PAI.
        // PAI não tem superior participante. Retorna PAI.
        // Então set visiveis terá apenas PAI.

        assertThat(result.getContent().get(0).getUnidadesParticipantes()).isEqualTo("PAI");
    }

    @Test
    @DisplayName("formatarUnidadesParticipantes: deve mostrar filho se pai não participa")
    void formatarUnidadesParticipantes_FilhoSemPai() {
        Unidade pai = new Unidade(); pai.setCodigo(1L); pai.setSigla("PAI");
        Unidade filho = new Unidade(); filho.setCodigo(2L); filho.setSigla("FILHO");
        filho.setUnidadeSuperior(pai);

        when(unidadeService.buscarEntidadePorId(2L)).thenReturn(filho);
        // PAI não participa, então não está no Set de participantesIds.
        // encontrarMaiorIdVisivel(FILHO): Superior (PAI) participa? Não. Retorna FILHO.

        Processo p = new Processo();
        p.setCodigo(100L);
        p.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        p.setTipo(TipoProcesso.MAPEAMENTO);
        p.setParticipantes(Set.of(filho));

        when(processoFacade.listarTodos(any())).thenReturn(new PageImpl<>(List.of(p)));

        Page<ProcessoResumoDto> result = service.listarProcessos(Perfil.ADMIN, null, pageable);

        assertThat(result.getContent().get(0).getUnidadesParticipantes()).isEqualTo("FILHO");
    }

    @Test
    @DisplayName("Deve usar busca otimizada (in-memory) ao buscar unidades subordinadas")
    void deveUsarBuscaOtimizadaDeSubordinadas() {
        // Cenário: Hierarquia com profundidade 3
        Long raizId = 1L;

        // Mock para evitar NPE
        when(processoFacade.listarPorParticipantesIgnorandoCriado(any(), any()))
                .thenReturn(org.springframework.data.domain.Page.empty());

        // Mock da resposta otimizada (simulando que o serviço retornou os IDs corretamente)
        when(unidadeService.buscarIdsDescendentes(raizId))
                .thenReturn(List.of(2L, 3L, 4L, 5L));

        // Ação: Listar processos como GESTOR
        service.listarProcessos(Perfil.GESTOR, raizId, org.springframework.data.domain.PageRequest.of(0, 10));

        // Verificação:
        // DEVE chamar buscarIdsDescendentes UMA vez
        verify(unidadeService, times(1)).buscarIdsDescendentes(raizId);

        // E NÃO DEVE chamar o método antigo (listarSubordinadas) NENHUMA vez
        verify(unidadeService, never()).listarSubordinadas(any());
    }
}
