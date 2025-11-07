package sgc.integracao;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import sgc.Sgc;
import sgc.atividade.model.Atividade;
import sgc.atividade.model.AtividadeRepo;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.CompetenciaAtividade;
import sgc.mapa.model.CompetenciaAtividadeRepo;
import sgc.mapa.model.CompetenciaRepo;
import sgc.integracao.mocks.TestSecurityConfig;
import sgc.integracao.mocks.TestThymeleafConfig;
import sgc.integracao.mocks.WithMockAdmin;
import sgc.mapa.model.Mapa;
import sgc.mapa.model.MapaRepo;
import sgc.processo.model.Processo;
import sgc.processo.model.ProcessoRepo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.sgrh.model.Perfil;
import sgc.sgrh.model.Usuario;
import sgc.sgrh.model.UsuarioRepo;
import sgc.subprocesso.dto.AtividadeAjusteDto;
import sgc.subprocesso.dto.CompetenciaAjusteDto;
import sgc.subprocesso.dto.SalvarAjustesReq;
import sgc.subprocesso.dto.SubmeterMapaAjustadoReq;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.unidade.model.Unidade;
import sgc.unidade.model.UnidadeRepo;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {Sgc.class, TestSecurityConfig.class, TestThymeleafConfig.class})
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("CDU-16: Ajustar mapa de competências")
@WithMockAdmin
public class CDU16IntegrationTest {
    private static final String API_SUBPROCESSO_MAPA_AJUSTE = "/api/subprocessos/{codSubprocesso}/mapa-ajuste/atualizar";
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProcessoRepo processoRepo;

    @Autowired
    private UnidadeRepo unidadeRepo;

    @Autowired
    private SubprocessoRepo subprocessoRepo;

    @Autowired
    private MapaRepo mapaRepo;

    @Autowired
    private AtividadeRepo atividadeRepo;

    @Autowired
    private CompetenciaRepo competenciaRepo;

    @Autowired
    private CompetenciaAtividadeRepo competenciaAtividadeRepo;

    @Autowired
    private UsuarioRepo usuarioRepo;

    private Subprocesso subprocesso;
    private Atividade atividade1;

    @BeforeEach
    void setUp() {
        // Use existing admin user from data-postgresql.sql
        var admin = usuarioRepo.findById("111111111111").orElseThrow();

        Unidade unidade = unidadeRepo.findById(15L).orElseThrow(); // Use existing SEDOC

        Processo processo = new Processo(
                "Processo de Revisão",
                TipoProcesso.REVISAO,
                SituacaoProcesso.EM_ANDAMENTO,
                LocalDateTime.now().plusDays(30)
        );
        processoRepo.save(processo);

        Mapa mapa = mapaRepo.save(new Mapa());
        subprocesso = new Subprocesso(
                processo,
                unidade,
                mapa,
                SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA,
                processo.getDataLimite()
        );
        subprocessoRepo.save(subprocesso);

        Competencia c1 = competenciaRepo.save(new Competencia("Competência 1", mapa));
        atividade1 = atividadeRepo.save(new Atividade(mapa, "Atividade 1"));
        Atividade atividade2 = atividadeRepo.save(new Atividade(mapa, "Atividade 2"));
        competenciaAtividadeRepo.save(new CompetenciaAtividade(new CompetenciaAtividade.Id(c1.getCodigo(), atividade1.getCodigo()), c1, atividade1));
        competenciaAtividadeRepo.save(new CompetenciaAtividade(new CompetenciaAtividade.Id(c1.getCodigo(), atividade2.getCodigo()), c1, atividade2));
    }

    @Test
    @DisplayName("Deve submeter o mapa ajustado e alterar a situação do subprocesso")
    void deveSubmeterMapaAjustadoComSucesso() throws Exception {
        var request = new SubmeterMapaAjustadoReq(
                "Ajustes realizados conforme solicitado.",
                LocalDateTime.now().plusDays(10)
        );

        mockMvc.perform(post("/api/subprocessos/{id}/submeter-mapa-ajustado", subprocesso.getCodigo())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        Subprocesso subprocessoAtualizado = subprocessoRepo.findById(subprocesso.getCodigo()).orElseThrow();
        assertThat(subprocessoAtualizado.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPA_DISPONIBILIZADO);
    }

    @Nested
    @DisplayName("Testes de ajuste do mapa")
    class AjusteDoMapa {

        @Test
        @DisplayName("Deve salvar ajustes no mapa e alterar a situação do subprocesso")
        void deveSalvarAjustesComSucesso() throws Exception {
            Competencia c1 = competenciaRepo.findAll().getFirst();

            var request = new SalvarAjustesReq(List.of(
                CompetenciaAjusteDto.builder()
                    .codCompetencia(c1.getCodigo())
                    .nome("Competência Ajustada")
                    .atividades(List.of(
                        AtividadeAjusteDto.builder()
                            .codAtividade(atividade1.getCodigo())
                            .nome("Atividade 1 Ajustada")
                            .conhecimentos(List.of())
                            .build()
                    ))
                    .build()
            ));

            mockMvc.perform(post(API_SUBPROCESSO_MAPA_AJUSTE, subprocesso.getCodigo())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

            Subprocesso subprocessoAtualizado = subprocessoRepo.findById(subprocesso.getCodigo()).orElseThrow();
            assertThat(subprocessoAtualizado.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPA_AJUSTADO);

            Atividade atividadeAtualizada = atividadeRepo.findById(atividade1.getCodigo()).orElseThrow();
            assertThat(atividadeAtualizada.getDescricao()).isEqualTo("Atividade 1 Ajustada");
        }


        @Test
        @DisplayName("Deve retornar 409 se tentar ajustar mapa em situação inválida")
        void deveRetornarErroParaSituacaoInvalida() throws Exception {
            subprocesso.setSituacao(SituacaoSubprocesso.MAPA_DISPONIBILIZADO);
            subprocessoRepo.save(subprocesso);

            var request = new SalvarAjustesReq(List.of());

            mockMvc.perform(post(API_SUBPROCESSO_MAPA_AJUSTE, subprocesso.getCodigo())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity());
        }
    }
}