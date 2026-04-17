package sgc.processo;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.webmvc.test.autoconfigure.*;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.*;
import org.springframework.test.web.servlet.*;
import sgc.organizacao.model.*;
import sgc.processo.model.*;
import sgc.processo.service.*;
import sgc.subprocesso.model.*;
import sgc.subprocesso.service.*;

import java.util.*;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProcessoController.class)
@DisplayName("ProcessoController - Cobertura de Testes")
class ProcessoControllerCoverageTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProcessoService processoService;
    @MockitoBean
    private SubprocessoConsultaService consultaService;
    @MockitoBean
    private LocalizacaoSubprocessoService localizacaoSubprocessoService;

    @Test
    @DisplayName("listarUnidadesParaImportacao - deve cobrir fallback de localização")
    @WithMockUser(roles = "CHEFE")
    void listarUnidadesParaImportacao_FallbackLocalizacao() throws Exception {
        Long cod = 1L;
        Processo p = new Processo();
        p.setSituacao(SituacaoProcesso.FINALIZADO);
        UnidadeProcesso up = new UnidadeProcesso();
        up.setUnidadeCodigo(10L);
        p.setParticipantes(List.of(up));
        
        when(processoService.buscarPorCodigoComParticipantes(cod)).thenReturn(p);
        
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(100L);
        Unidade u = new Unidade(); u.setCodigo(10L);
        sp.setUnidade(u);
        
        when(consultaService.listarEntidadesPorProcesso(cod)).thenReturn(List.of(sp));
        
        // Simular que o mapa de localizações NÃO tem o subprocesso 100
        when(localizacaoSubprocessoService.obterLocalizacoesAtuais(any())).thenReturn(Map.of());
        when(localizacaoSubprocessoService.obterLocalizacaoAtual(sp)).thenReturn(u);

        mockMvc.perform(get("/api/processos/" + cod + "/unidades-importacao"))
                .andExpect(status().isOk());
                
        verify(localizacaoSubprocessoService).obterLocalizacaoAtual(sp);
    }
}
