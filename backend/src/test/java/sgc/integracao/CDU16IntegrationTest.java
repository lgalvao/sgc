package sgc.integracao;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import sgc.Sgc;
import sgc.atividade.modelo.Atividade;
import sgc.atividade.modelo.AtividadeRepo;
import sgc.competencia.modelo.Competencia;
import sgc.competencia.modelo.CompetenciaAtividade;
import sgc.competencia.modelo.CompetenciaAtividadeRepo;
import sgc.competencia.modelo.CompetenciaRepo;
import sgc.integracao.mocks.TestSecurityConfig;
import sgc.mapa.modelo.Mapa;
import sgc.mapa.modelo.MapaRepo;
import sgc.processo.modelo.Processo;
import sgc.processo.modelo.ProcessoRepo;
import sgc.processo.modelo.TipoProcesso;
import sgc.sgrh.Perfil;
import sgc.sgrh.Usuario;
import sgc.sgrh.UsuarioRepo;
import sgc.subprocesso.SituacaoSubprocesso;
import sgc.subprocesso.dto.SubmeterMapaAjustadoReq;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.subprocesso.modelo.SubprocessoRepo;
import sgc.unidade.modelo.Unidade;
import sgc.unidade.modelo.UnidadeRepo;

import java.time.LocalDate;

import sgc.integracao.mocks.WithMockAdmin;
import sgc.processo.SituacaoProcesso;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

@SpringBootTest(classes = {Sgc.class, TestSecurityConfig.class})
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("CDU-16: Ajustar mapa de competências")
@WithMockAdmin
public class CDU16IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Repositories
    @Autowired private ProcessoRepo processoRepo;
    @Autowired private UnidadeRepo unidadeRepo;
    @Autowired private SubprocessoRepo subprocessoRepo;
    @Autowired private MapaRepo mapaRepo;
    @Autowired private AtividadeRepo atividadeRepo;
    @Autowired private CompetenciaRepo competenciaRepo;
    @Autowired private CompetenciaAtividadeRepo competenciaAtividadeRepo;
    @Autowired private UsuarioRepo usuarioRepo;

    private Subprocesso subprocesso;

    @BeforeEach
    void setUp() {
        var admin = new Usuario();
        admin.setTituloEleitoral(111111111111L);
        admin.setPerfis(java.util.Set.of(Perfil.ADMIN));
        usuarioRepo.save(admin);

        unidadeRepo.save(new Unidade("SEDOC", "SEDOC"));
        Unidade unidade = new Unidade("Unidade Teste", "UT");
        unidadeRepo.save(unidade);

        Processo processo = new Processo("Processo de Revisão", TipoProcesso.REVISAO, SituacaoProcesso.EM_ANDAMENTO, LocalDate.now().plusDays(30));
        processoRepo.save(processo);

        Mapa mapa = mapaRepo.save(new Mapa());
        subprocesso = new Subprocesso(processo, unidade, mapa, SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA, processo.getDataLimite());
        subprocessoRepo.save(subprocesso);

        Competencia c1 = competenciaRepo.save(new Competencia("Competência 1", mapa));
        Atividade a1 = atividadeRepo.save(new Atividade(mapa, "Atividade 1"));
        competenciaAtividadeRepo.save(new CompetenciaAtividade(new CompetenciaAtividade.Id(c1.getCodigo(), a1.getCodigo()), c1, a1));
    }

    @Test
    @DisplayName("Deve submeter o mapa ajustado e alterar a situação do subprocesso")
    void deveSubmeterMapaAjustadoComSucesso() throws Exception {
        SubmeterMapaAjustadoReq request = new SubmeterMapaAjustadoReq("Ajustes realizados conforme solicitado.", LocalDate.now().plusDays(10));

        mockMvc.perform(post("/api/subprocessos/{id}/submeter-mapa-ajustado", subprocesso.getCodigo())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        Subprocesso subprocessoAtualizado = subprocessoRepo.findById(subprocesso.getCodigo()).orElseThrow();
        assertThat(subprocessoAtualizado.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPA_DISPONIBILIZADO);
    }
}