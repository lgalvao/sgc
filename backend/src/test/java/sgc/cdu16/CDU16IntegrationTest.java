package sgc.cdu16;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import sgc.Sgc;
import sgc.TestSecurityConfig;
import sgc.WithMockChefe;
import sgc.atividade.modelo.Atividade;
import sgc.atividade.modelo.AtividadeRepo;
import sgc.competencia.modelo.Competencia;
import sgc.competencia.modelo.CompetenciaRepo;
import sgc.comum.enums.SituacaoSubprocesso;
import sgc.comum.modelo.Usuario;
import sgc.comum.modelo.UsuarioRepo;
import sgc.mapa.modelo.Mapa;
import sgc.mapa.modelo.MapaRepo;
import sgc.processo.enums.TipoProcesso;
import sgc.processo.modelo.Processo;
import sgc.processo.modelo.ProcessoRepo;
import sgc.subprocesso.dto.SubmeterMapaAjustadoReq;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.subprocesso.modelo.SubprocessoRepo;
import sgc.unidade.modelo.Unidade;
import sgc.unidade.modelo.UnidadeRepo;

import java.time.LocalDate;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {Sgc.class, TestSecurityConfig.class})
@ActiveProfiles("test")
@Transactional
@DisplayName("CDU-16 - Ajustar mapa de competências")
public class CDU16IntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ProcessoRepo processoRepo;

    @Autowired
    private SubprocessoRepo subprocessoRepo;

    @Autowired
    private UnidadeRepo unidadeRepo;

    @Autowired
    private UsuarioRepo usuarioRepo;

    @Autowired
    private MapaRepo mapaRepo;

    @Autowired
    private AtividadeRepo atividadeRepo;

    @Autowired
    private CompetenciaRepo competenciaRepo;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    private Subprocesso subprocesso;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        Unidade unidade = new Unidade("Test Unit", "TU");
        unidadeRepo.save(unidade);
        Usuario chefe = new Usuario();
        chefe.setTitulo("chefe");
        chefe.setUnidade(unidade);
        usuarioRepo.save(chefe);

        Processo processo = new Processo();
        processo.setTipo(TipoProcesso.REVISAO);
        processo.setDescricao("Processo de Teste");
        processoRepo.save(processo);

        Mapa mapa = new Mapa();
        mapaRepo.save(mapa);

        subprocesso = new Subprocesso();
        subprocesso.setProcesso(processo);
        subprocesso.setUnidade(unidade);
        subprocesso.setSituacao(SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA);
        subprocesso.setMapa(mapa);
        subprocessoRepo.save(subprocesso);
    }

    @Test
    @WithMockChefe
    @DisplayName("Deve submeter o mapa ajustado e alterar a situação para MAPA_DISPONIBILIZADO")
    void submeterMapaAjustado_deveMudarSituacao() throws Exception {
        subprocesso.setSituacao(SituacaoSubprocesso.MAPA_AJUSTADO);
        subprocessoRepo.save(subprocesso);

        SubmeterMapaAjustadoReq request = new SubmeterMapaAjustadoReq("Observações de teste", LocalDate.now().plusDays(10));

        mockMvc.perform(post("/api/subprocessos/{id}/submeter-mapa-ajustado", subprocesso.getCodigo())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.situacao").value(SituacaoSubprocesso.MAPA_DISPONIBILIZADO.name()));
    }

    @Test
    @WithMockChefe
    @DisplayName("Não deve submeter o mapa se existirem atividades não associadas a competências")
    void submeterMapaAjustado_deveFalharSeAtividadeNaoAssociada() throws Exception {
        // Arrange
        Atividade atividade = new Atividade(subprocesso.getMapa(), "Atividade Teste");
        atividadeRepo.save(atividade);
        Competencia competencia = new Competencia(subprocesso.getMapa(), "Competência Teste");
        competenciaRepo.save(competencia);

        SubmeterMapaAjustadoReq request = new SubmeterMapaAjustadoReq("Observações", LocalDate.now().plusDays(5));

        // Act & Assert
        mockMvc.perform(post("/api/subprocessos/{id}/submeter-mapa-ajustado", subprocesso.getCodigo())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("Existem atividades que não foram associadas a nenhuma competência."));
    }
}