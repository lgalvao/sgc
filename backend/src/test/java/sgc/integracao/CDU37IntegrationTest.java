package sgc.integracao;

import org.junit.jupiter.api.*;
import org.springframework.http.*;
import org.springframework.transaction.annotation.*;
import sgc.integracao.mocks.*;
import sgc.mapa.model.*;
import sgc.organizacao.model.*;
import sgc.processo.model.*;
import sgc.subprocesso.model.*;

import java.time.*;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
    @DisplayName("Deve filtrar unidades com mapa existente na busca de códigos")
    @WithMockAdmin
    void buscarCodigosUnidadesSemMapaVigente_filtrandoUnidadesComMapaExistente() throws Exception {
        // Unidade 905: sem mapa no data.sql
        Long codSemMapa = 905L;
        // Unidade 4: sem mapa no data.sql, vamos criar um mapa para ela
        Long codComMapa = 4L;

        Processo processo = new Processo()
                .setDescricao("Processo com mapa de teste")
                .setTipo(TipoProcesso.MAPEAMENTO)
                .setSituacao(SituacaoProcesso.EM_ANDAMENTO)
                .setDataCriacao(LocalDateTime.now())
                .setDataLimite(LocalDateTime.now().plusDays(30));

        processoRepo.saveAndFlush(processo);

        Unidade unidade = unidadeRepo.findById(codComMapa).orElseThrow();
        Subprocesso subprocesso = Subprocesso.builder()
                .processo(processo)
                .unidade(unidade)
                .situacao(SituacaoSubprocesso.NAO_INICIADO)
                .dataLimiteEtapa1(LocalDateTime.now().plusDays(10))
                .build();
        subprocessoRepo.saveAndFlush(subprocesso);

        Mapa mapa = Mapa.builder()
                .subprocesso(subprocesso)
                .build();
        mapaRepo.saveAndFlush(mapa);

        mockMvc.perform(get(API_UNIDADES_SEM_MAPA_VIGENTE).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@ == %d)]".formatted(codSemMapa)).exists())
                .andExpect(jsonPath("$[?(@ == %d)]".formatted(codComMapa)).doesNotExist());
    }
}
