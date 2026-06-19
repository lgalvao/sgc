package sgc.integracao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.transaction.annotation.Transactional;
import sgc.integracao.mocks.WithMockAdmin;
import sgc.integracao.mocks.WithMockGestor;
import sgc.organizacao.model.Unidade;
import sgc.processo.model.Processo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;

import java.time.LocalDateTime;
import java.util.Set;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Tag("integration")
@Transactional
@DisplayName("CDU-37: Gerar relatório de unidades sem mapas vigentes")
class CDU37IntegrationTest extends BaseIntegrationTest {
    private static final String API_REL_UNIDADES_SEM_MAPAS = "/api/relatorios/unidades-sem-mapas-vigentes/exportar";
    private static final String API_UNIDADES_SEM_MAPA_VIGENTE = "/api/unidades/sem-mapa-vigente";

    @Test
    @DisplayName("Deve gerar relatório de unidades sem mapas vigentes em PDF quando ADMIN")
    @WithMockAdmin
    void gerarRelatorioUnidadesSemMapasVigentes_comoAdmin_sucesso() throws Exception {
        mockMvc.perform(get(API_REL_UNIDADES_SEM_MAPAS).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "application/pdf"))
                .andExpect(header().exists(HttpHeaders.CONTENT_DISPOSITION));
    }

    @Test
    @DisplayName("Deve negar geração do relatório de unidades sem mapas vigentes quando GESTOR")
    @WithMockGestor
    void gerarRelatorioUnidadesSemMapasVigentes_comoGestor_proibido() throws Exception {
        mockMvc.perform(get(API_REL_UNIDADES_SEM_MAPAS).with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Deve buscar códigos das unidades sem mapa vigente com sucesso quando ADMIN")
    @WithMockAdmin
    void buscarCodigosUnidadesSemMapaVigente_comoAdmin_sucesso() throws Exception {
        mockMvc.perform(get(API_UNIDADES_SEM_MAPA_VIGENTE).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("Deve negar busca de códigos de unidades sem mapa vigente quando GESTOR")
    @WithMockGestor
    void buscarCodigosUnidadesSemMapaVigente_comoGestor_proibido() throws Exception {
        mockMvc.perform(get(API_UNIDADES_SEM_MAPA_VIGENTE).with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Deve filtrar unidades com processos ativos na busca de códigos")
    @WithMockAdmin
    void buscarCodigosUnidadesSemMapaVigente_filtrandoProcessosAtivos() throws Exception {
        // Unidade 905: sem mapa e sem processo no data.sql
        Long codSemMapaEProcesso = 905L;
        // Unidade 4: sem mapa no data.sql, mas vamos criar um processo para ela
        Long codComProcessoAtivo = 4L;

        Processo p = new Processo()
                .setDescricao("Processo Ativo de Teste")
                .setTipo(TipoProcesso.MAPEAMENTO)
                .setSituacao(SituacaoProcesso.EM_ANDAMENTO)
                .setDataCriacao(LocalDateTime.now())
                .setDataLimite(LocalDateTime.now().plusDays(30));

        Unidade u = unidadeRepo.findById(codComProcessoAtivo).orElseThrow();
        p.adicionarParticipantes(Set.of(u));
        processoRepo.saveAndFlush(p);

        mockMvc.perform(get(API_UNIDADES_SEM_MAPA_VIGENTE).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@ == %d)]".formatted(codSemMapaEProcesso)).exists())
                .andExpect(jsonPath("$[?(@ == %d)]".formatted(codComProcessoAtivo)).doesNotExist());
    }
}
