package sgc;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.enums.SituacaoProcesso;
import sgc.comum.enums.SituacaoSubprocesso;
import sgc.comum.modelo.Usuario;
import sgc.comum.modelo.UsuarioRepo;
import sgc.processo.enums.TipoProcesso;
import sgc.processo.modelo.Processo;
import sgc.processo.modelo.ProcessoRepo;
import sgc.subprocesso.dto.AnaliseValidacaoDto;
import sgc.subprocesso.dto.DevolverValidacaoReq;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.subprocesso.modelo.SubprocessoRepo;
import sgc.unidade.modelo.Unidade;
import sgc.unidade.modelo.UnidadeRepo;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("CDU-19 - Validar mapa de competências")
public class CDU19IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SubprocessoRepo subprocessoRepo;

    @Autowired
    private ProcessoRepo processoRepo;

    @Autowired
    private UnidadeRepo unidadeRepo;

    @Autowired
    private UsuarioRepo usuarioRepo;

    private Subprocesso subprocesso;

    @BeforeEach
    void setUp() {
        Unidade unidadeSuperior = unidadeRepo.save(new Unidade("Unidade Superior", "UNISUP"));
        Unidade unidade = new Unidade("Unidade Subprocesso", "UNISUB");
        unidade.setUnidadeSuperior(unidadeSuperior);
        unidadeRepo.save(unidade);

        Usuario chefe = new Usuario();
        chefe.setTitulo("chefe");
        chefe.setUnidade(unidadeSuperior);
        usuarioRepo.save(chefe);

        Processo processo = processoRepo.save(new Processo("Processo de Teste", TipoProcesso.MAPEAMENTO, SituacaoProcesso.EM_ANDAMENTO, LocalDate.now()));
        subprocesso = subprocessoRepo.save(
            new Subprocesso(processo, unidade, null, SituacaoSubprocesso.MAPA_VALIDADO, LocalDate.now())
        );
    }

    @Test
    @WithMockChefe
    @DisplayName("Devolução e aceite da validação do mapa com verificação do histórico")
    void devolucaoEaceiteComVerificacaoHistorico() throws Exception {
        // Devolução do mapa
        DevolverValidacaoReq devolverReq = new DevolverValidacaoReq("Justificativa da devolução");
        mockMvc.perform(post("/api/subprocessos/{id}/devolver-validacao", subprocesso.getCodigo())
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(devolverReq)))
            .andExpect(status().isOk());

        // Verificação do histórico após devolução
        String responseDevolucao = mockMvc.perform(get("/api/subprocessos/{id}/historico-validacao", subprocesso.getCodigo()))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        List<AnaliseValidacaoDto> historicoDevolucao = objectMapper.readValue(responseDevolucao, new TypeReference<List<AnaliseValidacaoDto>>() {});

        assertThat(historicoDevolucao).hasSize(1);
        assertThat(historicoDevolucao.get(0).acao()).isEqualTo("DEVOLUCAO");
        assertThat(historicoDevolucao.get(0).unidadeSigla()).isNotNull();
        assertThat(historicoDevolucao.get(0).observacoes()).isEqualTo("Justificativa da devolução");

        // Novo aceite do mapa
        subprocesso.setSituacao(SituacaoSubprocesso.MAPA_VALIDADO);
        subprocessoRepo.save(subprocesso);

        mockMvc.perform(post("/api/subprocessos/{id}/aceitar-validacao", subprocesso.getCodigo()))
            .andExpect(status().isOk());

        // Verificação do histórico após aceite
        String responseAceite = mockMvc.perform(get("/api/subprocessos/{id}/historico-validacao", subprocesso.getCodigo()))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        List<AnaliseValidacaoDto> historicoAceite = objectMapper.readValue(responseAceite, new TypeReference<List<AnaliseValidacaoDto>>() {});

        assertThat(historicoAceite).hasSize(2); // Histórico é cumulativo
        assertThat(historicoAceite.get(0).acao()).isEqualTo("ACEITE");
        assertThat(historicoAceite.get(0).unidadeSigla()).isNotNull();
    }
}