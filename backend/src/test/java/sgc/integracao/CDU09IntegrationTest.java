package sgc.integracao;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import sgc.Sgc;
import sgc.alerta.model.AlertaRepo;
import sgc.analise.model.AnaliseRepo;
import sgc.integracao.mocks.TestSecurityConfig;
import sgc.integracao.mocks.TestThymeleafConfig;
import sgc.integracao.mocks.WithMockChefe;
import sgc.integracao.mocks.WithMockCustomUser;
import sgc.mapa.model.*;
import sgc.subprocesso.model.Movimentacao;
import sgc.subprocesso.model.MovimentacaoRepo;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("integration")
@SpringBootTest(classes = Sgc.class)
@ActiveProfiles("test")
@Import({
        TestSecurityConfig.class,
        TestThymeleafConfig.class
})
@Transactional
@DisplayName("CDU-09: Disponibilizar Cadastro de Atividades e Conhecimentos")
class CDU09IntegrationTest extends BaseIntegrationTest {
    @Autowired
    private CompetenciaRepo competenciaRepo;
    @Autowired
    private MovimentacaoRepo movimentacaoRepo;
    @Autowired
    private ConhecimentoRepo conhecimentoRepo;
    @Autowired
    private AlertaRepo alertaRepo;

    @Autowired
    private AnaliseRepo analiseRepo;

    @Autowired
    protected jakarta.persistence.EntityManager entityManager;

    @MockitoBean
    private JavaMailSender javaMailSender;

    @BeforeEach
    void setUp() {
        when(javaMailSender.createMimeMessage()).thenReturn(mock(jakarta.mail.internet.MimeMessage.class));
    }

    @Nested
    @DisplayName("Testes para Disponibilizar Cadastro")
    class DisponibilizarCadastro {
        
        @Test
        @WithMockChefe("333333333333") // Chefe da Unidade 9 (SEDIA) no data.sql
        @DisplayName("Deve disponibilizar o cadastro quando todas as condições são atendidas")
        void deveDisponibilizarCadastroComSucesso() throws Exception {
            // No data.sql, a Unidade 9 tem o Mapa 1002 e o Subprocesso 60002
            Long spCodigo = 60002L;
            Subprocesso sp = subprocessoRepo.findById(spCodigo).orElseThrow();
            
            // Força situação inicial para o teste
            sp.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
            
            // Limpa competências pré-existentes do mapa que podem estar sem atividades
            competenciaRepo.deleteByMapaCodigo(sp.getMapa().getCodigo());
            
            // Simula histórico de análise prévia (Item 15)
            analiseRepo.saveAndFlush(sgc.analise.model.Analise.builder()
                    .subprocesso(sp)
                    .unidade(sp.getUnidade().getUnidadeSuperior())
                    .usuario(Usuario.builder().tituloEleitoral("666666666666").build())
                    .tipo(sgc.analise.model.TipoAnalise.CADASTRO)
                    .acao(sgc.analise.model.TipoAcaoAnalise.DEVOLUCAO_MAPEAMENTO)
                    .dataHora(java.time.LocalDateTime.now().minusDays(1))
                    .observacoes("Análise anterior")
                    .build());
            
            assertThat(analiseRepo.findBySubprocessoCodigoOrderByDataHoraDesc(spCodigo)).isNotEmpty();

            subprocessoRepo.saveAndFlush(sp);

            var competencia = competenciaRepo.save(Competencia.builder().descricao("Competência de Teste").mapa(sp.getMapa()).build());
            var atividade = Atividade.builder().mapa(sp.getMapa()).descricao("Atividade de Teste").build();
            atividade = atividadeRepo.save(atividade);
            
            atividade.getCompetencias().add(competencia);
            atividade = atividadeRepo.save(atividade);
            
            conhecimentoRepo.save(Conhecimento.builder()
                    .descricao("Conhecimento de Teste")
                    .atividade(atividade)
                    .build());

            entityManager.flush();

            mockMvc.perform(post("/api/subprocessos/{id}/cadastro/disponibilizar", spCodigo).with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.mensagem", is("Cadastro de atividades disponibilizado")));

            Subprocesso subprocessoAtualizado = subprocessoRepo.findById(spCodigo).orElseThrow();
            assertThat(subprocessoAtualizado.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO);

            List<Movimentacao> movimentacoes = movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(spCodigo);
            assertThat(movimentacoes).isNotEmpty();
            assertThat(movimentacoes.getFirst().getUnidadeDestino().getCodigo()).isEqualTo(6L); // Superior da 9 é 6 (COSIS)

            var alertas = alertaRepo.findByProcessoCodigo(sp.getProcesso().getCodigo());
            assertThat(alertas).isNotEmpty();

            // Valida exclusão do histórico de análise (Item 15)
            assertThat(analiseRepo.findBySubprocessoCodigoOrderByDataHoraDesc(spCodigo)).isEmpty();
        }

        @Test
        @WithMockChefe("333333333333")
        @DisplayName("Não deve disponibilizar se houver atividade sem conhecimento associado")
        void naoDeveDisponibilizarComAtividadeSemConhecimento() throws Exception {
            Long spCodigo = 60002L;
            Subprocesso sp = subprocessoRepo.findById(spCodigo).orElseThrow();
            sp.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
            subprocessoRepo.saveAndFlush(sp);

            Atividade atividade = Atividade.builder().mapa(sp.getMapa()).descricao("Atividade Vazia").build();
            atividadeRepo.saveAndFlush(atividade);

            mockMvc.perform(post("/api/subprocessos/{id}/cadastro/disponibilizar", spCodigo).with(csrf()))
                    .andExpect(status().isUnprocessableContent())
                    .andExpect(jsonPath("$.message", is("Existem atividades sem conhecimentos associados.")));
        }
    }

    @Nested
    @DisplayName("Testes de Segurança")
    class Seguranca {
        @Test
        @WithMockChefe("3") // Fernanda Oliveira - Chefe da Unidade 8 no data.sql
        @DisplayName("Não deve permitir que um CHEFE de outra unidade disponibilize o cadastro")
        void naoDevePermitirChefeDeOutraUnidadeDisponibilizar() throws Exception {
            Long spCodigo = 60002L; // Subprocesso da Unidade 9
            
            mockMvc.perform(post("/api/subprocessos/{id}/cadastro/disponibilizar", spCodigo).with(csrf()))
                    .andExpect(status().isForbidden());
        }
    }
}
