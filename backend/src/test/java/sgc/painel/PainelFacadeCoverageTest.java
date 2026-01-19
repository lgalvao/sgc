package sgc.painel;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PainelFacadeCoverageTest {

    @Mock private ProcessoFacade processoFacade;
    @Mock private AlertaFacade alertaService;
    @Mock private UnidadeFacade unidadeService;

    @InjectMocks
    private PainelFacade facade;

    @Test
    @DisplayName("listarProcessos - Non-Admin with Null CodigoUnidade")
    void listarProcessos_NonAdminNullCodigoUnidade() {
        // Covers line 107
        Page<ProcessoResumoDto> result = facade.listarProcessos(Perfil.SERVIDOR, null, org.springframework.data.domain.PageRequest.of(0, 10));
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("listarProcessos - CalcularLinkDestino Exception")
    void listarProcessos_CalcularLinkDestinoException() {
        // Covers line 231 (catch block)

        Long codUnidade = 1L;
        Unidade unidade = new Unidade();
        unidade.setCodigo(codUnidade);

        Processo processo = new Processo();
        processo.setCodigo(10L);
        processo.setDescricao("P");
        processo.setSituacao(SituacaoProcesso.CRIADO);
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        processo.setParticipantes(Set.of(unidade));

        // Use PageImpl instead of Page.of (Java 9+ List.of is fine, but Page.of might be newer Spring Data)
        // Spring Data 2+ uses PageImpl
        when(processoFacade.listarPorParticipantesIgnorandoCriado(any(), any()))
                .thenReturn(new PageImpl<>(Collections.singletonList(processo)));

        // Mock exception when fetching unit for link calculation
        when(unidadeService.buscarPorCodigo(codUnidade)).thenThrow(new RuntimeException("Error"));

        Page<ProcessoResumoDto> result = facade.listarProcessos(Perfil.SERVIDOR, codUnidade, org.springframework.data.domain.PageRequest.of(0, 10));

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertNull(result.getContent().get(0).linkDestino());
    }

    @Test
    @DisplayName("listarAlertas - Null Units")
    void listarAlertas_NullUnits() {
        // Covers null checks in paraAlertaDto

        Alerta alerta = new Alerta();
        alerta.setCodigo(1L);
        alerta.setDescricao("Alerta");
        alerta.setDataHora(LocalDateTime.now());
        // Null origin and destination

        when(alertaService.listarPorUnidade(anyLong(), any()))
                .thenReturn(new PageImpl<>(Collections.singletonList(alerta)));

        Page<AlertaDto> result = facade.listarAlertas("123", 1L, Pageable.unpaged());

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertNull(result.getContent().get(0).getUnidadeOrigem());
        assertNull(result.getContent().get(0).getUnidadeDestino());
    }

    @Test
    @DisplayName("encontrarMaiorIdVisivel - Reflection Coverage")
    void encontrarMaiorIdVisivel_Reflection() throws Exception {
        // Covers line 191 (if reachable via reflection or logic tweak)

        Method method = PainelFacade.class.getDeclaredMethod("encontrarMaiorIdVisivel", Unidade.class, Set.class);
        method.setAccessible(true);

        // Case 1: Unidade is null
        Long result1 = (Long) method.invoke(facade, null, Set.of(1L));
        assertNull(result1);

        // Case 2: Unidade code not in set
        Unidade u = new Unidade();
        u.setCodigo(1L);
        Long result2 = (Long) method.invoke(facade, u, Set.of(2L));
        assertNull(result2);
    }
}
