package sgc;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
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
import sgc.analise.enums.TipoAcaoAnalise;
import sgc.analise.modelo.AnaliseCadastro;
import sgc.analise.modelo.AnaliseCadastroRepo;
import sgc.comum.enums.SituacaoProcesso;
import sgc.comum.enums.SituacaoSubprocesso;
import sgc.processo.enums.TipoProcesso;
import sgc.comum.modelo.Usuario;
import sgc.comum.modelo.UsuarioRepo;
import sgc.processo.modelo.Processo;
import sgc.processo.modelo.ProcessoRepo;
import sgc.subprocesso.dto.AceitarCadastroReq;
import sgc.subprocesso.dto.DevolverCadastroReq;
import com.fasterxml.jackson.core.type.TypeReference;
import sgc.analise.dto.AnaliseCadastroDto;
import sgc.subprocesso.dto.HomologarCadastroReq;
import sgc.subprocesso.modelo.Movimentacao;
import sgc.subprocesso.modelo.MovimentacaoRepo;
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

@SpringBootTest(classes = TestSecurityConfig.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
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
    private AnaliseCadastroRepo analiseCadastroRepo;

    @Autowired
    private EntityManager entityManager;

    private Unidade unidade;
    private Unidade unidadeSuperior;
    private Unidade sedoc;
    private Processo processo;
    private Subprocesso subprocesso;

    @BeforeEach
    void setUp() {
        Usuario titular = new Usuario();
        titular.setTitulo("chefe");
        titular.setNome("Chefe da Unidade");
        usuarioRepo.save(titular);

        unidadeSuperior = new Unidade("Unidade Superior", "UO_SUP");
        unidadeRepo.save(unidadeSuperior);

        Usuario gestorDaUnidade = new Usuario();
        gestorDaUnidade.setTitulo("gestor_unidade");
        gestorDaUnidade.setNome("Gestor da Unidade");
        gestorDaUnidade.setUnidade(unidadeSuperior);
        usuarioRepo.save(gestorDaUnidade);

        unidadeSuperior.setTitular(gestorDaUnidade);
        unidadeRepo.save(unidadeSuperior);

        unidade = new Unidade("Unidade de Teste", "UO_TESTE");
        unidade.setUnidadeSuperior(unidadeSuperior);
        unidade.setTitular(titular);
        unidadeRepo.save(unidade);

        Usuario adminUser = new Usuario();
        adminUser.setTitulo("admin");
        adminUser.setNome("Administrador");
        usuarioRepo.save(adminUser);

        sedoc = new Unidade("Secretaria de Documentação", "SEDOC");
        sedoc.setTitular(adminUser);
        unidadeRepo.save(sedoc);

        processo = new Processo();
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        processo.setDescricao("Processo de Teste");
        processoRepo.save(processo);

        subprocesso = new Subprocesso();
        subprocesso.setProcesso(processo);
        subprocesso.setUnidade(unidade);
        subprocesso.setSituacao(SituacaoSubprocesso.CADASTRO_DISPONIBILIZADO);
        subprocesso.setDataLimiteEtapa1(LocalDate.now().plusDays(10));
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
        @WithMockGestor // Simula um usuário com perfil de gestor
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

            // 2. Verificar se a análise foi registrada corretamente
            List<AnaliseCadastro> analises = analiseCadastroRepo.findBySubprocessoCodigoOrderByDataHoraDesc(subprocesso.getCodigo());
            assertThat(analises).hasSize(1);
            AnaliseCadastro analiseRegistrada = analises.get(0);
            assertThat(analiseRegistrada.getAcao()).isEqualTo(TipoAcaoAnalise.DEVOLUCAO);
            assertThat(analiseRegistrada.getMotivo()).isEqualTo(motivoDevolucao);
            assertThat(analiseRegistrada.getObservacoes()).isEqualTo(observacoes);
            assertThat(analiseRegistrada.getUnidadeSigla()).isEqualTo(unidadeSuperior.getSigla());

            // 3. Verificar a movimentação
            List<Movimentacao> movimentacoes = movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(subprocesso.getCodigo());
            assertThat(movimentacoes).hasSize(2); // A inicial + a de devolução
            Movimentacao movimentacaoDevolucao = movimentacoes.get(0);
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
            List<AnaliseCadastro> analises = analiseCadastroRepo.findBySubprocessoCodigoOrderByDataHoraDesc(subprocesso.getCodigo());
            assertThat(analises).hasSize(1);
            AnaliseCadastro analiseRegistrada = analises.get(0);
            assertThat(analiseRegistrada.getAcao()).isEqualTo(TipoAcaoAnalise.ACEITE);
            assertThat(analiseRegistrada.getObservacoes()).isEqualTo(observacoes);
            assertThat(analiseRegistrada.getAnalistaUsuarioTitulo()).isEqualTo("gestor_unidade"); // From @WithMockGestor

            // 2. Verificar a movimentação
            List<Movimentacao> movimentacoes = movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(subprocesso.getCodigo());
            assertThat(movimentacoes).hasSize(2); // A inicial + a de aceite
            Movimentacao movimentacaoAceite = movimentacoes.get(0);
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
        @WithMockAdmin // Simula um usuário com perfil de ADMIN
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
            Movimentacao movimentacaoHomologacao = movimentacoes.get(0);
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
            List<AnaliseCadastroDto> historico = objectMapper.readValue(jsonResponse, new TypeReference<>() {});

            assertThat(historico).hasSize(2);

            // First item in list is the most recent (ACEITE)
            AnaliseCadastroDto aceite = historico.get(0);
            assertThat(aceite.resultado()).isEqualTo(TipoAcaoAnalise.ACEITE.name());
            assertThat(aceite.observacoes()).isEqualTo(obsAceite);
            assertThat(aceite.unidadeSigla()).isEqualTo(unidadeSuperior.getSigla());

            // Second item is the oldest (DEVOLUCAO)
            AnaliseCadastroDto devolucao = historico.get(1);
            assertThat(devolucao.resultado()).isEqualTo(TipoAcaoAnalise.DEVOLUCAO.name());
            assertThat(devolucao.observacoes()).isEqualTo(obsDevolucao);
            assertThat(devolucao.unidadeSigla()).isEqualTo(unidadeSuperior.getSigla());
        }
    }
}