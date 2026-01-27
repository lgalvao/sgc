package sgc.seguranca;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import sgc.comum.erros.RestExceptionHandler;
import sgc.mapa.dto.CompetenciaMapaDto;
import sgc.mapa.dto.MapaCompletoDto;
import sgc.mapa.dto.SalvarMapaRequest;
import sgc.subprocesso.SubprocessoMapaController;
import sgc.subprocesso.service.SubprocessoFacade;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(SubprocessoMapaController.class)
@Import(RestExceptionHandler.class)
@DisplayName("Testes de Sanitização em Mapas")
class SanitizacaoMapaTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SubprocessoFacade subprocessoFacade;

    @MockitoBean
    private sgc.mapa.service.MapaFacade mapaFacade;

    @MockitoBean
    private sgc.organizacao.UsuarioFacade usuarioFacade;


    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Deve sanitizar observações no SalvarMapaRequest")
    void deveSanitizarObservacoes() throws Exception {
        String scriptMalicioso = "<script>alert('xss')</script>Observação válida";

        CompetenciaMapaDto competencia = CompetenciaMapaDto.builder()
                .descricao("Competência teste")
                .atividadesCodigos(List.of(1L))
                .build();

        SalvarMapaRequest request = SalvarMapaRequest.builder()
                .observacoes(scriptMalicioso)
                .competencias(List.of(competencia))
                .build();

        when(subprocessoFacade.salvarMapaSubprocesso(eq(1L), any(SalvarMapaRequest.class)))
                .thenReturn(new MapaCompletoDto(1L, 1L, "Obs", Collections.emptyList()));

        mockMvc.perform(post("/api/subprocessos/1/mapa-completo/atualizar")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        org.mockito.ArgumentCaptor<SalvarMapaRequest> captor = org.mockito.ArgumentCaptor.forClass(SalvarMapaRequest.class);
        verify(subprocessoFacade).salvarMapaSubprocesso(eq(1L), captor.capture());

        SalvarMapaRequest capturado = captor.getValue();
        // Verificação manual para ver o que realmente chegou
        System.out.println("Valor sanitizado recebido: " + capturado.observacoes());
        
        org.assertj.core.api.Assertions.assertThat(capturado.observacoes())
            .doesNotContain("<script>")
            .contains("Observação válida");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Deve sanitizar descrição da competência no SalvarMapaRequest")
    void deveSanitizarDescricaoCompetencia() throws Exception {
        String scriptMalicioso = "<img src=x onerror=alert(1)>Descricao";

        CompetenciaMapaDto competencia = CompetenciaMapaDto.builder()
                .descricao(scriptMalicioso)
                .atividadesCodigos(List.of(1L))
                .build();

        SalvarMapaRequest request = SalvarMapaRequest.builder()
                .competencias(List.of(competencia))
                .build();

        when(subprocessoFacade.salvarMapaSubprocesso(eq(1L), any(SalvarMapaRequest.class)))
                .thenReturn(new MapaCompletoDto(1L, 1L, "Obs", Collections.emptyList()));

        mockMvc.perform(post("/api/subprocessos/1/mapa-completo/atualizar")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(subprocessoFacade).salvarMapaSubprocesso(
                eq(1L),
                org.mockito.ArgumentMatchers.argThat(arg ->
                        !arg.competencias().get(0).descricao().contains("<img") &&
                                arg.competencias().get(0).descricao().contains("Descricao")
                )
        );
    }
}
