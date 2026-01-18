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
import sgc.alerta.AlertaFacade;
import sgc.alerta.dto.AlertaDto;
import sgc.alerta.model.Alerta;
import sgc.organizacao.UnidadeFacade;
import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.Unidade;
import sgc.painel.erros.ErroParametroPainelInvalido;
import sgc.processo.dto.ProcessoResumoDto;
import sgc.processo.model.Processo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.processo.service.ProcessoFacade;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PainelFacadeCoverageTest {

    @InjectMocks
    private PainelFacade painelFacade;

    @Mock
    private ProcessoFacade processoFacade;
    @Mock
    private AlertaFacade alertaService;
    @Mock
    private UnidadeFacade unidadeService;

    @Test
    @DisplayName("listarProcessos - Erro Perfil Nulo")
    void listarProcessos_ErroPerfilNulo() {
        assertThrows(ErroParametroPainelInvalido.class,
            () -> painelFacade.listarProcessos(null, 1L, PageRequest.of(0, 10)));
    }

    @Test
    @DisplayName("listarProcessos - Admin - Vê Todos")
    void listarProcessos_Admin() {
        Pageable p = PageRequest.of(0, 10);
        Processo proc = new Processo();
        proc.setCodigo(1L);
        proc.setSituacao(SituacaoProcesso.CRIADO);
        proc.setTipo(TipoProcesso.MAPEAMENTO);

        when(processoFacade.listarTodos(any())).thenReturn(new PageImpl<>(List.of(proc)));

        Page<ProcessoResumoDto> result = painelFacade.listarProcessos(Perfil.ADMIN, null, p);

        assertEquals(1, result.getContent().size());
        assertTrue(result.getContent().get(0).linkDestino().contains("/cadastro"));
    }

    @Test
    @DisplayName("listarProcessos - Gestor - Vê Descendentes")
    void listarProcessos_Gestor() {
        Pageable p = PageRequest.of(0, 10);
        Processo proc = new Processo();
        proc.setCodigo(1L);
        proc.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        proc.setTipo(TipoProcesso.MAPEAMENTO);

        when(unidadeService.buscarIdsDescendentes(10L)).thenReturn(List.of(11L));
        when(processoFacade.listarPorParticipantesIgnorandoCriado(any(), any()))
            .thenReturn(new PageImpl<>(List.of(proc)));

        Page<ProcessoResumoDto> result = painelFacade.listarProcessos(Perfil.GESTOR, 10L, p);

        assertEquals(1, result.getContent().size());
        assertTrue(result.getContent().get(0).linkDestino().contains("/processo/1"));
    }

    @Test
    @DisplayName("listarProcessos - Chefe - Link Com Unidade")
    void listarProcessos_Chefe() {
        Pageable p = PageRequest.of(0, 10);
        Processo proc = new Processo();
        proc.setCodigo(1L);
        proc.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        proc.setTipo(TipoProcesso.MAPEAMENTO);

        when(processoFacade.listarPorParticipantesIgnorandoCriado(any(), any()))
            .thenReturn(new PageImpl<>(List.of(proc)));

        sgc.organizacao.dto.UnidadeDto u = new sgc.organizacao.dto.UnidadeDto();
        u.setSigla("SIGLA");
        when(unidadeService.buscarPorCodigo(10L)).thenReturn(u);

        Page<ProcessoResumoDto> result = painelFacade.listarProcessos(Perfil.CHEFE, 10L, p);

        assertEquals(1, result.getContent().size());
        assertTrue(result.getContent().get(0).linkDestino().contains("/processo/1/SIGLA"));
    }

    @Test
    @DisplayName("listarProcessos - Null Unit - Empty")
    void listarProcessos_NullUnit() {
        Page<ProcessoResumoDto> result = painelFacade.listarProcessos(Perfil.CHEFE, null, PageRequest.of(0, 10));
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("listarAlertas - Por Unidade")
    void listarAlertas_PorUnidade() {
        Pageable p = PageRequest.of(0, 10);
        Alerta a = new Alerta();
        a.setCodigo(1L);

        when(alertaService.listarPorUnidade(eq(10L), any())).thenReturn(new PageImpl<>(List.of(a)));

        Page<AlertaDto> result = painelFacade.listarAlertas(null, 10L, p);
        assertEquals(1, result.getContent().size());
    }

    @Test
    @DisplayName("listarAlertas - Sem Unidade - Vazio")
    void listarAlertas_SemUnidade() {
        Page<AlertaDto> result = painelFacade.listarAlertas(null, null, PageRequest.of(0, 10));
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("formatarUnidadesParticipantes - Logic")
    void formatarUnidadesParticipantes_Logic() {
        // Need to expose private logic? No, test via listarProcessos.
        // We need a process with participants.
        Processo proc = new Processo();
        proc.setCodigo(1L);
        proc.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        proc.setTipo(TipoProcesso.MAPEAMENTO);

        Unidade u1 = new Unidade(); u1.setCodigo(1L); u1.setSigla("U1");
        Unidade u2 = new Unidade(); u2.setCodigo(2L); u2.setSigla("U2");
        proc.setParticipantes(Set.of(u1, u2));

        when(processoFacade.listarTodos(any())).thenReturn(new PageImpl<>(List.of(proc)));

        Page<ProcessoResumoDto> result = painelFacade.listarProcessos(Perfil.ADMIN, null, PageRequest.of(0, 10));

        String unidades = result.getContent().get(0).unidadesParticipantes();
        assertTrue(unidades.contains("U1") || unidades.contains("U2"));
    }
}
