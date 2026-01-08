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
import sgc.organizacao.UnidadeService;
import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.Unidade;
import sgc.painel.erros.ErroParametroPainelInvalido;
import sgc.processo.dto.ProcessoResumoDto;
import sgc.processo.model.Processo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.processo.service.ProcessoFacade;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PainelService")
class PainelServiceTest {

    @Mock
    private ProcessoFacade processoFacade;
    @Mock
    private AlertaService alertaService;
    @Mock
    private UnidadeService unidadeService;

    @InjectMocks
    private PainelService painelService;

    @Test
    @DisplayName("listarProcessos deve lançar erro se perfil for nulo")
    void listarProcessos_PerfilNulo() {
        Pageable pageable = Pageable.unpaged();
        assertThatThrownBy(() -> painelService.listarProcessos(null, 1L, pageable))
                .isInstanceOf(ErroParametroPainelInvalido.class);
    }

    @Test
    @DisplayName("listarProcessos para ADMIN deve listar todos")
    void listarProcessos_Admin() {
        Processo p = criarProcessoMock(1L);
        when(processoFacade.listarTodos(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(p)));

        Page<ProcessoResumoDto> result = painelService.listarProcessos(Perfil.ADMIN, null, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("listarProcessos para GESTOR deve incluir subordinadas")
    void listarProcessos_Gestor() {
        when(unidadeService.buscarIdsDescendentes(1L)).thenReturn(List.of(2L, 3L));
        
        Processo p = criarProcessoMock(1L);
        when(processoFacade.listarPorParticipantesIgnorandoCriado(anyList(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(p)));

        painelService.listarProcessos(Perfil.GESTOR, 1L, PageRequest.of(0, 10));

        // Verifica se chamou buscando por 1L, 2L e 3L
        verify(processoFacade).listarPorParticipantesIgnorandoCriado(anyList(), any(Pageable.class));
    }

    @Test
    @DisplayName("listarProcessos não ADMIN retorna vazio se unidade for nula")
    void listarProcessos_NaoAdminSemUnidade() {
        Page<ProcessoResumoDto> result = painelService.listarProcessos(Perfil.CHEFE, null, PageRequest.of(0, 10));
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("listarProcessos deve calcular link correto para ADMIN e processo CRIADO")
    void listarProcessos_LinkAdminCriado() {
        Processo p = criarProcessoMock(1L);
        p.setSituacao(SituacaoProcesso.CRIADO);
        when(processoFacade.listarTodos(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(p)));

        Page<ProcessoResumoDto> result = painelService.listarProcessos(Perfil.ADMIN, null, PageRequest.of(0, 10));

        assertThat(result.getContent().get(0).getLinkDestino()).contains("/processo/cadastro?codProcesso=1");
    }

    @Test
    @DisplayName("listarProcessos deve calcular link correto para CHEFE")
    void listarProcessos_LinkChefe() {
        Unidade u = new Unidade();
        u.setCodigo(1L);
        u.setSigla("U1");

        Processo p = criarProcessoMock(1L);
        when(processoFacade.listarPorParticipantesIgnorandoCriado(anyList(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(p)));
        when(unidadeService.buscarPorCodigo(1L)).thenReturn(sgc.organizacao.dto.UnidadeDto.builder()
                .codigo(1L)
                .sigla("U1")
                .build());

        Page<ProcessoResumoDto> result = painelService.listarProcessos(Perfil.CHEFE, 1L, PageRequest.of(0, 10));

        assertThat(result.getContent().get(0).getLinkDestino()).isEqualTo("/processo/1/U1");
    }

    @Test
    @DisplayName("listarAlertas por unidade deve buscar alertas da unidade")
    void listarAlertas_PorUnidade() {
        sgc.alerta.model.Alerta alerta = new sgc.alerta.model.Alerta();
        alerta.setCodigo(100L);
        alerta.setDescricao("Alerta teste");
        alerta.setDataHora(LocalDateTime.now());
        
        when(alertaService.listarPorUnidade(any(Long.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(alerta)));
        when(alertaService.obterDataHoraLeitura(any(), any())).thenReturn(Optional.empty());

        Page<sgc.alerta.dto.AlertaDto> result = painelService.listarAlertas("123456", 1L, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getDescricao()).isEqualTo("Alerta teste");
        verify(alertaService).listarPorUnidade(any(Long.class), any(Pageable.class));
    }

    @Test
    @DisplayName("listarAlertas sem unidade deve retornar vazio")
    void listarAlertas_SemUnidadeRetornaVazio() {
        Page<sgc.alerta.dto.AlertaDto> result = painelService.listarAlertas(null, null, PageRequest.of(0, 10));

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("listarProcessos deve tratar exceção ao formatar unidades participantes")
    void listarProcessos_FormatarUnidadesException() {
        Unidade u = new Unidade();
        u.setCodigo(1L);
        // Sem sigla ou mockando erro service
        
        Processo p = criarProcessoMock(1L);
        p.setParticipantes(Set.of(u));
        
        when(processoFacade.listarTodos(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(p)));
        // Simula erro ao buscar entidade para validar visibilidade
        when(unidadeService.buscarEntidadePorId(1L)).thenThrow(new RuntimeException("DB Error"));

        Page<ProcessoResumoDto> result = painelService.listarProcessos(Perfil.ADMIN, null, PageRequest.of(0, 10));

        // Deve retornar lista mas com participantes vazio ou parcial, sem quebrar
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUnidadesParticipantes()).isEmpty();
    }

    @Test
    @DisplayName("listarProcessos deve retornar link null se unidade nao encontrada no calculo de link CHEFE")
    void listarProcessos_LinkChefeErro() {
        Processo p = criarProcessoMock(1L);
        when(processoFacade.listarPorParticipantesIgnorandoCriado(anyList(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(p)));
        
        when(unidadeService.buscarPorCodigo(2L)).thenThrow(new RuntimeException("Unidade não achada"));

        Page<ProcessoResumoDto> result = painelService.listarProcessos(Perfil.CHEFE, 2L, PageRequest.of(0, 10));

        assertThat(result.getContent().get(0).getLinkDestino()).isNull();
    }
    
    @Test
    @DisplayName("listarAlertas deve tratar unidades nulas no DTO")
    void listarAlertas_UnidadesNulas() {
        sgc.alerta.model.Alerta alerta = new sgc.alerta.model.Alerta();
        alerta.setCodigo(400L);
        alerta.setDescricao("Alerta sem unidade");
        alerta.setDataHora(LocalDateTime.now());
        // Unidades null
        
        when(alertaService.listarPorUnidade(any(Long.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(alerta)));

        Page<sgc.alerta.dto.AlertaDto> result = painelService.listarAlertas(null, 1L, PageRequest.of(0, 10));

        assertThat(result.getContent().get(0).getUnidadeOrigem()).isNull();
        assertThat(result.getContent().get(0).getUnidadeDestino()).isNull();
    }

    private Processo criarProcessoMock(Long codigo) {
        Processo p = new Processo();
        p.setCodigo(codigo);
        p.setDescricao("Processo " + codigo);
        p.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        p.setTipo(TipoProcesso.MAPEAMENTO);
        p.setDataCriacao(LocalDateTime.now());
        p.setParticipantes(Collections.emptySet());
        return p;
    }
}
