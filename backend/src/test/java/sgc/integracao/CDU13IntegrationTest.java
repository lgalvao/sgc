package sgc.integracao;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import sgc.Sgc;
import sgc.analise.modelo.Analise;
import sgc.analise.modelo.AnaliseRepo;
import sgc.analise.modelo.TipoAcaoAnalise;
import sgc.integracao.mocks.TestSecurityConfig;
import sgc.integracao.mocks.WithMockAdmin;
import sgc.integracao.mocks.WithMockGestor;
import sgc.processo.modelo.Processo;
import sgc.processo.modelo.ProcessoRepo;
import sgc.processo.modelo.SituacaoProcesso;
import sgc.processo.modelo.TipoProcesso;
import sgc.sgrh.modelo.Perfil;
import sgc.sgrh.modelo.Usuario;
import sgc.sgrh.modelo.UsuarioRepo;
import sgc.subprocesso.dto.AceitarCadastroReq;
import sgc.subprocesso.dto.DevolverCadastroReq;
import sgc.subprocesso.dto.HomologarCadastroReq;
import sgc.subprocesso.modelo.*;
import sgc.unidade.modelo.Unidade;
import sgc.unidade.modelo.UnidadeRepo;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = Sgc.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({TestSecurityConfig.class, sgc.integracao.mocks.TestThymeleafConfig.class})
@Transactional
@DisplayName("CDU-13: Analisar cadastro de atividades e conhecimentos")
public class CDU13IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProcessoRepo processoRepo;

    @Autowired
    private SubprocessoRepo subprocessoRepo;

    @Autowired
    private UnidadeRepo unidadeRepo;

    @Autowired
    private UsuarioRepo usuarioRepo;

    @Autowired
    private MovimentacaoRepo movimentacaoRepo;

    @Autowired
    private AnaliseRepo analiseRepo;

    @Autowired
    private EntityManager entityManager;

    private Unidade unidade;
    private Unidade unidadeSuperior;
    private Subprocesso subprocesso;

    @BeforeEach
    void setUp() {
        Usuario titular = new Usuario();
        titular.setTituloEleitoral(333333333333L);
        titular.setNome("Chefe da Unidade");
        titular.setPerfis(java.util.Set.of(Perfil.CHEFE));
        usuarioRepo.save(titular);

        unidadeSuperior = new Unidade("Unidade Superior", "UO_SUP");
        unidadeRepo.save(unidadeSuperior);

        Usuario gestorDaUnidade = new Usuario();
        gestorDaUnidade.setTituloEleitoral(222222222222L);
        gestorDaUnidade.setNome("Gestor da Unidade");
        gestorDaUnidade.setUnidade(unidadeSuperior);
        gestorDaUnidade.setPerfis(java.util.Set.of(Perfil.GESTOR));
        usuarioRepo.save(gestorDaUnidade);

        unidadeSuperior.setTitular(gestorDaUnidade);
        unidadeRepo.save(unidadeSuperior);

        unidade = new Unidade("Unidade de Teste", "UO_TESTE");
        unidade.setUnidadeSuperior(unidadeSuperior);
        unidade.setTitular(titular);
        unidadeRepo.save(unidade);

        Usuario adminUser = new Usuario();
        adminUser.setTituloEleitoral(111111111111L);
        adminUser.setNome("Administrador");
        adminUser.setPerfis(java.util.Set.of(Perfil.ADMIN));
        usuarioRepo.save(adminUser);

        Unidade sedoc = new Unidade("Secretaria de Documentação", "SEDOC");
        sedoc.setTitular(adminUser);
        unidadeRepo.save(sedoc);

        Processo processo = new Processo();
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        processo.setDescricao("Processo de Teste");
        processoRepo.save(processo);

        subprocesso = new Subprocesso();
        subprocesso.setProcesso(processo);
        subprocesso.setUnidade(unidade);
        subprocesso.setSituacao(SituacaoSubprocesso.CADASTRO_DISPONIBILIZADO);
        subprocesso.setDataLimiteEtapa1(LocalDateTime.now().plusDays(10));
        subprocessoRepo.save(subprocesso);

        // Movimentação inicial para simular o estado
        Movimentacao movimentacaoInicial = new Movimentacao(subprocesso, unidade, unidadeSuperior, "Disponibilização inicial");
        movimentacaoRepo.save(movimentacaoInicial);
    }

    @Nested
    @DisplayName("Fluxo de Devolução")
    class Devolucao {

        @Test
        @DisplayName("Deve devolver cadastro, registrar análise corretamente e alterar situação")
        @WithMockGestor
            // Simula um usuário com perfil de gestor
        void devolverCadastro_deveFuncionarCorretamente() throws Exception {
            // Given
            String motivoDevolucao = "Atividades incompletas";
            String observacoes = "Favor revisar a atividade X e Y.";
            DevolverCadastroReq requestBody = new DevolverCadastroReq(motivoDevolucao, observacoes);

            // When
            mockMvc.perform(post("/api/subprocessos/{id}/devolver-cadastro", subprocesso.getCodigo())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestBody)))
                    .andExpect(status().isOk());

            // Then
            entityManager.flush();
            entityManager.clear();

            // 1. Verificar se o subprocesso foi atualizado
            Subprocesso subprocessoAtualizado = subprocessoRepo.findById(subprocesso.getCodigo()).orElseThrow();
            assertThat(subprocessoAtualizado.getSituacao()).isEqualTo(SituacaoSubprocesso.CADASTRO_EM_ANDAMENTO);
            assertThat(subprocessoAtualizado.getDataFimEtapa1()).isNull();

            List<Analise> analises = analiseRepo.findBySubprocessoCodigoOrderByDataHoraDesc(subprocesso.getCodigo());
            assertThat(analises).hasSize(1);
            Analise analiseRegistrada = analises.getFirst();
            assertThat(analiseRegistrada.getAcao()).isEqualTo(TipoAcaoAnalise.DEVOLUCAO);
            assertThat(analiseRegistrada.getMotivo()).isEqualTo(motivoDevolucao);
            assertThat(analiseRegistrada.getObservacoes()).isEqualTo(observacoes);
            assertThat(analiseRegistrada.getUnidadeSigla()).isEqualTo(unidadeSuperior.getSigla());

            // 3. Verificar a movimentação
            List<Movimentacao> movimentacoes = movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(subprocesso.getCodigo());
            assertThat(movimentacoes).hasSize(2); // A inicial + a de devolução
            Movimentacao movimentacaoDevolucao = movimentacoes.getFirst();
            assertThat(movimentacaoDevolucao.getUnidadeOrigem().getSigla()).isEqualTo(unidadeSuperior.getSigla());
            assertThat(movimentacaoDevolucao.getUnidadeDestino().getSigla()).isEqualTo(unidade.getSigla());
            assertThat(movimentacaoDevolucao.getDescricao()).contains(motivoDevolucao);
        }
    }

    @Nested
    @DisplayName("Fluxo de Aceite")
    class Aceite {

        @Test
        @DisplayName("Deve aceitar cadastro, registrar análise e mover para unidade superior")
        @WithMockGestor
        void aceitarCadastro_deveFuncionarCorretamente() throws Exception {
            // Given
            String observacoes = "Cadastro parece OK.";
            AceitarCadastroReq requestBody = new AceitarCadastroReq(observacoes);

            // When
            mockMvc.perform(post("/api/subprocessos/{id}/aceitar-cadastro", subprocesso.getCodigo())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestBody)))
                    .andExpect(status().isOk());

            // Then
            entityManager.flush();
            entityManager.clear();

            // 1. Verificar se a análise foi registrada corretamente
            List<Analise> analises = analiseRepo.findBySubprocessoCodigoOrderByDataHoraDesc(subprocesso.getCodigo());
            assertThat(analises).hasSize(1);
            Analise analiseRegistrada = analises.getFirst();
            assertThat(analiseRegistrada.getAcao()).isEqualTo(TipoAcaoAnalise.ACEITE);
            assertThat(analiseRegistrada.getObservacoes()).isEqualTo(observacoes);
            assertThat(analiseRegistrada.getAnalistaUsuarioTitulo()).isEqualTo("222222222222"); // From @WithMockGestor

            // 2. Verificar a movimentação
            List<Movimentacao> movimentacoes = movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(subprocesso.getCodigo());
            assertThat(movimentacoes).hasSize(2); // A inicial + a de aceite
            Movimentacao movimentacaoAceite = movimentacoes.getFirst();
            assertThat(movimentacaoAceite.getUnidadeOrigem().getSigla()).isEqualTo(unidade.getSigla());
            assertThat(movimentacaoAceite.getUnidadeDestino().getSigla()).isEqualTo(unidadeSuperior.getSigla());
            assertThat(movimentacaoAceite.getDescricao()).isEqualTo("Cadastro de atividades e conhecimentos aceito");
        }
    }

    @Nested
    @DisplayName("Fluxo de Homologação")
    class Homologacao {

        @Test
        @DisplayName("Deve homologar cadastro, alterar situação e registrar movimentação da SEDOC")
        @WithMockAdmin
            // Simula um usuário com perfil de ADMIN
        void homologarCadastro_deveFuncionarCorretamente() throws Exception {
            // Given
            HomologarCadastroReq requestBody = new HomologarCadastroReq("Homologado via teste.");

            // When
            mockMvc.perform(post("/api/subprocessos/{id}/homologar-cadastro", subprocesso.getCodigo())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestBody)))
                    .andExpect(status().isOk());

            // Then
            entityManager.flush();
            entityManager.clear();

            // 1. Verificar se o subprocesso foi atualizado
            Subprocesso subprocessoAtualizado = subprocessoRepo.findById(subprocesso.getCodigo()).orElseThrow();
            assertThat(subprocessoAtualizado.getSituacao()).isEqualTo(SituacaoSubprocesso.CADASTRO_HOMOLOGADO);

            // 2. Verificar a movimentação
            List<Movimentacao> movimentacoes = movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(subprocesso.getCodigo());
            assertThat(movimentacoes).hasSize(2); // A inicial + a de homologação
            Movimentacao movimentacaoHomologacao = movimentacoes.getFirst();
            assertThat(movimentacaoHomologacao.getUnidadeOrigem().getSigla()).isEqualTo("SEDOC");
            assertThat(movimentacaoHomologacao.getUnidadeDestino().getSigla()).isEqualTo("SEDOC");
            assertThat(movimentacaoHomologacao.getDescricao()).isEqualTo("Cadastro de atividades e conhecimentos homologado");
        }
    }

    @Nested
    @DisplayName("Fluxo de Histórico de Análise")
    class Historico {

        @Test
        @DisplayName("Deve retornar o histórico de devoluções e aceites ordenado")
        @WithMockGestor
        void getHistorico_deveRetornarAcoesOrdenadas() throws Exception {
            // Given: First, a manager returns the process for adjustments
            String motivoDevolucao = "Incompleto";
            String obsDevolucao = "Falta atividade Z";
            DevolverCadastroReq devolverReq = new DevolverCadastroReq(motivoDevolucao, obsDevolucao);

            mockMvc.perform(post("/api/subprocessos/{id}/devolver-cadastro", subprocesso.getCodigo())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(devolverReq)))
                    .andExpect(status().isOk());

            // And then, the unit submits it again and the manager accepts it
            subprocesso.setSituacao(SituacaoSubprocesso.CADASTRO_DISPONIBILIZADO);
            subprocessoRepo.saveAndFlush(subprocesso);

            String obsAceite = "Agora sim, completo.";
            AceitarCadastroReq aceitarReq = new AceitarCadastroReq(obsAceite);
            mockMvc.perform(post("/api/subprocessos/{id}/aceitar-cadastro", subprocesso.getCodigo())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(aceitarReq)))
                    .andExpect(status().isOk());

            // When
            String jsonResponse = mockMvc.perform(get("/api/subprocessos/{id}/historico-cadastro", subprocesso.getCodigo())
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            // Then
            List<sgc.analise.dto.AnaliseHistoricoDto> historico = objectMapper.readValue(jsonResponse, new TypeReference<>() {});

            assertThat(historico).hasSize(2);

            // First item in list is the most recent (ACEITE)
            sgc.analise.dto.AnaliseHistoricoDto aceite = historico.getFirst();
            assertThat(aceite.getAcao()).isEqualTo(TipoAcaoAnalise.ACEITE);
            assertThat(aceite.getObservacoes()).isEqualTo(obsAceite);
            assertThat(aceite.getUnidadeSigla()).isEqualTo(unidadeSuperior.getSigla());

            // Second item is the oldest (DEVOLUCAO)
            sgc.analise.dto.AnaliseHistoricoDto devolucao = historico.get(1);
            assertThat(devolucao.getAcao()).isEqualTo(TipoAcaoAnalise.DEVOLUCAO);
            assertThat(devolucao.getObservacoes()).isEqualTo(obsDevolucao);
            assertThat(devolucao.getUnidadeSigla()).isEqualTo(unidadeSuperior.getSigla());
        }
    }
}