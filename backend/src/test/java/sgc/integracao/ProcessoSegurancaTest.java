package sgc.integracao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import sgc.integracao.mocks.TestSecurityConfig;
import sgc.processo.api.CriarProcessoReq;
import sgc.processo.internal.model.TipoProcesso;
import sgc.unidade.internal.model.Unidade;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestSecurityConfig.class)
class ProcessoSegurancaTest extends BaseIntegrationTest {

    @Test
    @DisplayName("Não deve permitir que usuário sem ROLE_ADMIN crie um processo")
    @WithMockUser(username = "servidor", roles = {"SERVIDOR"})
    void naoDeveCriarProcessoComoServidor() throws Exception {
        // Busca uma unidade existente para usar no request
        Unidade unidade = unidadeRepo.findAll().stream().findFirst().orElseThrow();

        CriarProcessoReq req = CriarProcessoReq.builder()
                .descricao("Processo Indevido")
                .tipo(TipoProcesso.MAPEAMENTO)
                .dataLimiteEtapa1(LocalDateTime.now().plusDays(10))
                .unidades(List.of(unidade.getCodigo()))
                .build();

        mockMvc.perform(post("/api/processos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }
}
