package sgc.integracao;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import sgc.analise.modelo.TipoAcaoAnalise;
import sgc.integracao.mocks.WithMockChefe;
import sgc.integracao.mocks.WithMockGestor;
import sgc.processo.SituacaoProcesso;
import sgc.processo.modelo.Processo;
import sgc.processo.modelo.ProcessoRepo;
import sgc.processo.modelo.TipoProcesso;
import sgc.sgrh.Usuario;
import sgc.sgrh.UsuarioRepo;
import sgc.subprocesso.SituacaoSubprocesso;
import sgc.subprocesso.dto.DevolverValidacaoReq;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.subprocesso.modelo.SubprocessoRepo;
import sgc.unidade.modelo.Unidade;
import sgc.unidade.modelo.UnidadeRepo;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
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
    ProcessoRepo processoRepo;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SubprocessoRepo subprocessoRepo;

    @Autowired
    private UnidadeRepo unidadeRepo;

    @Autowired
    private UsuarioRepo usuarioRepo;

    private Subprocesso subprocesso;

    @BeforeEach
    void setUp() {
        Unidade unidadeSuperiorSuperior = unidadeRepo.save(new Unidade("Unidade Superior Superior", "UNISUPSUP"));
        Unidade unidadeSuperior = new Unidade("Unidade Superior", "UNISUP");
        unidadeSuperior.setUnidadeSuperior(unidadeSuperiorSuperior);
        unidadeRepo.save(unidadeSuperior);

        Unidade unidade = new Unidade("Unidade Subprocesso", "UNISUB");
        unidade.setUnidadeSuperior(unidadeSuperior);
        unidadeRepo.save(unidade);

        // Criar usuários mockados para as unidades
        Usuario chefeMock = new Usuario();
        chefeMock.setTitulo("chefe");
        chefeMock.setUnidade(unidadeSuperior);
        usuarioRepo.save(chefeMock);
        unidadeSuperior.setTitular(chefeMock);
        unidadeRepo.save(unidadeSuperior);

        Usuario gestorMock = new Usuario();
        gestorMock.setTitulo("gestor_unidade");
        gestorMock.setUnidade(unidade);
        usuarioRepo.save(gestorMock);
        unidade.setTitular(gestorMock);
        unidadeRepo.save(unidade);

        Processo processo = processoRepo.save(new Processo("Processo de Teste", TipoProcesso.MAPEAMENTO, SituacaoProcesso.EM_ANDAMENTO, LocalDate.now()));
        subprocesso = subprocessoRepo.save(
                new Subprocesso(processo, unidade, null, SituacaoSubprocesso.MAPA_VALIDADO, LocalDate.now())
        );
    }

    @Test
    @DisplayName("Devolução e aceite da validação do mapa com verificação do histórico")
    @WithMockChefe("chefe")
    void devolucaoEaceiteComVerificacaoHistorico() throws Exception {
        // Devolução do mapa
        DevolverValidacaoReq devolverReq = new DevolverValidacaoReq("Justificativa da devolução");
        mockMvc.perform(post("/api/subprocessos/{id}/devolver-validacao", subprocesso.getCodigo())
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(devolverReq)))
                .andExpect(status().isOk());

        // Verificação do histórico após devolução
        String responseDevolucao = mockMvc.perform(get("/api/subprocessos/{id}/historico-validacao", subprocesso.getCodigo())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        List<sgc.analise.dto.AnaliseValidacaoHistoricoDto> historicoDevolucao = objectMapper.readValue(responseDevolucao, new TypeReference<>() {
        });

        assertThat(historicoDevolucao).hasSize(1);
        assertThat(historicoDevolucao.getFirst().acao()).isEqualTo(TipoAcaoAnalise.DEVOLUCAO);
        assertThat(historicoDevolucao.getFirst().unidadeSigla()).isNotNull();
        assertThat(historicoDevolucao.getFirst().observacoes()).isEqualTo("Justificativa da devolução");

        // Unidade inferior valida o mapa novamente
        mockMvc.perform(post("/api/subprocessos/{id}/validar-mapa", subprocesso.getCodigo())
                        .with(csrf()))
                .andExpect(status().isOk());

        // Chefe da unidade superior aceita a validação
        mockMvc.perform(post("/api/subprocessos/{id}/aceitar-validacao", subprocesso.getCodigo())
                        .with(csrf()))
                .andExpect(status().isOk());

        // Verificação do histórico após aceite
        String responseAceite = mockMvc.perform(get("/api/subprocessos/{id}/historico-validacao", subprocesso.getCodigo())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        List<sgc.analise.dto.AnaliseValidacaoHistoricoDto> historicoAceite = objectMapper.readValue(responseAceite, new TypeReference<>() {
        });

        assertThat(historicoAceite).hasSize(1);
        assertThat(historicoAceite.getFirst().acao()).isEqualTo(TipoAcaoAnalise.ACEITE);
        assertThat(historicoAceite.getFirst().unidadeSigla()).isNotNull();
    }
}
