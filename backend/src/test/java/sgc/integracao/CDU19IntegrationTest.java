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
import sgc.comum.modelo.SituacaoProcesso;
import sgc.comum.modelo.SituacaoSubprocesso;
import sgc.comum.modelo.Usuario;
import sgc.comum.modelo.UsuarioRepo;
import sgc.processo.modelo.TipoProcesso;
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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
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
    private Usuario gestor;
    private Usuario chefe;

    @BeforeEach
    void setUp() {
        Unidade unidadeSuperiorSuperior = unidadeRepo.save(new Unidade("Unidade Superior Superior", "UNISUPSUP"));
        Unidade unidadeSuperior = new Unidade("Unidade Superior", "UNISUP");
        unidadeSuperior.setUnidadeSuperior(unidadeSuperiorSuperior);
        unidadeRepo.save(unidadeSuperior);

        Unidade unidade = new Unidade("Unidade Subprocesso", "UNISUB");
        unidade.setUnidadeSuperior(unidadeSuperior);
        unidadeRepo.save(unidade);

        this.chefe = usuarioRepo.findByTitulo("chefe").orElse(new Usuario());
        this.chefe.setTitulo("chefe");
        this.chefe.setUnidade(unidadeSuperior);
        usuarioRepo.save(this.chefe);

        this.gestor = new Usuario();
        this.gestor.setTitulo("gestor_unidade");
        this.gestor.setUnidade(unidade);
        usuarioRepo.save(this.gestor);

        // Define os titulares das unidades
        unidade.setTitular(this.gestor);
        unidadeRepo.save(unidade);
        unidadeSuperior.setTitular(this.chefe);
        unidadeRepo.save(unidadeSuperior);

        Processo processo = processoRepo.save(new Processo("Processo de Teste", TipoProcesso.MAPEAMENTO, SituacaoProcesso.EM_ANDAMENTO, LocalDate.now()));
        subprocesso = subprocessoRepo.save(
                new Subprocesso(processo, unidade, null, SituacaoSubprocesso.MAPA_VALIDADO, LocalDate.now())
        );
    }

    @Test
    @DisplayName("Devolução e aceite da validação do mapa com verificação do histórico")
    void devolucaoEaceiteComVerificacaoHistorico() throws Exception {
        // Devolução do mapa
        DevolverValidacaoReq devolverReq = new DevolverValidacaoReq("Justificativa da devolução");
        mockMvc.perform(post("/api/subprocessos/{id}/devolver-validacao", subprocesso.getCodigo())
                        .with(user(this.chefe)).with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(devolverReq)))
                .andExpect(status().isOk());

        // Verificação do histórico após devolução
        String responseDevolucao = mockMvc.perform(get("/api/subprocessos/{id}/historico-validacao", subprocesso.getCodigo())
                        .with(user(this.chefe)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        List<AnaliseValidacaoDto> historicoDevolucao = objectMapper.readValue(responseDevolucao, new TypeReference<>() {
        });

        assertThat(historicoDevolucao).hasSize(1);
        assertThat(historicoDevolucao.getFirst().acao()).isEqualTo("DEVOLUCAO");
        assertThat(historicoDevolucao.getFirst().unidadeSigla()).isNotNull();
        assertThat(historicoDevolucao.getFirst().observacoes()).isEqualTo("Justificativa da devolução");

        // Unidade inferior valida o mapa novamente
        mockMvc.perform(post("/api/subprocessos/{id}/validar-mapa", subprocesso.getCodigo())
                        .with(user(this.gestor)).with(csrf()))
                .andExpect(status().isOk());

        // Chefe da unidade superior aceita a validação
        mockMvc.perform(post("/api/subprocessos/{id}/aceitar-validacao", subprocesso.getCodigo())
                        .with(user(this.chefe)).with(csrf()))
                .andExpect(status().isOk());

        // Verificação do histórico após aceite
        String responseAceite = mockMvc.perform(get("/api/subprocessos/{id}/historico-validacao", subprocesso.getCodigo())
                        .with(user(this.chefe)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        List<AnaliseValidacaoDto> historicoAceite = objectMapper.readValue(responseAceite, new TypeReference<>() {
        });

        assertThat(historicoAceite).hasSize(1);
        assertThat(historicoAceite.getFirst().acao()).isEqualTo("ACEITE");
        assertThat(historicoAceite.getFirst().unidadeSigla()).isNotNull();
    }
}
