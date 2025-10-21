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
import sgc.atividade.modelo.Atividade;
import sgc.atividade.modelo.AtividadeRepo;
import sgc.competencia.modelo.Competencia;
import sgc.competencia.modelo.CompetenciaAtividade;
import sgc.competencia.modelo.CompetenciaAtividadeRepo;
import sgc.competencia.modelo.CompetenciaRepo;
import sgc.integracao.mocks.TestSecurityConfig;
import sgc.integracao.mocks.WithMockAdmin;
import sgc.subprocesso.dto.SalvarAjustesReq;
import sgc.subprocesso.dto.CompetenciaAjusteDto;
import sgc.subprocesso.dto.AtividadeAjusteDto;
import sgc.subprocesso.dto.ConhecimentoAjusteDto;
import sgc.mapa.modelo.Mapa;
import sgc.mapa.modelo.MapaRepo;
import sgc.processo.SituacaoProcesso;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {Sgc.class, TestSecurityConfig.class})
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
    private Atividade atividade2;

    @BeforeEach
    void setUp() {
        var admin = new Usuario();
        admin.setTituloEleitoral(111111111111L);
        admin.setPerfis(java.util.Set.of(Perfil.ADMIN));
        usuarioRepo.save(admin);
        unidadeRepo.save(new Unidade("SEDOC", "SEDOC"));

        Unidade unidade = new Unidade("Unidade Teste", "UT");
        unidadeRepo.save(unidade);

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
        atividade2 = atividadeRepo.save(new Atividade(mapa, "Atividade 2"));
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
            Competencia c1 = competenciaRepo.findAll().get(0);

            var request = new SalvarAjustesReq(List.of(
                CompetenciaAjusteDto.builder()
                    .competenciaId(c1.getCodigo())
                    .nome("Competência Ajustada")
                    .atividades(List.of(
                        AtividadeAjusteDto.builder()
                            .atividadeId(atividade1.getCodigo())
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
                .andExpect(status().isConflict());
        }
    }
}