package sgc.integracao;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import sgc.alerta.modelo.AlertaRepo;
import sgc.analise.modelo.AnaliseRepo;
import sgc.atividade.modelo.Atividade;
import sgc.atividade.modelo.AtividadeRepo;
import sgc.integracao.mocks.TestSecurityConfig;
import sgc.integracao.mocks.WithMockAdmin;
import sgc.integracao.mocks.WithMockGestor;
import sgc.mapa.modelo.Mapa;
import sgc.mapa.modelo.MapaRepo;
import sgc.mapa.modelo.UnidadeMapa;
import sgc.mapa.modelo.UnidadeMapaRepo;
import sgc.processo.SituacaoProcesso;
import sgc.processo.modelo.Processo;
import sgc.processo.modelo.ProcessoRepo;
import sgc.processo.modelo.TipoProcesso;
import sgc.sgrh.Perfil;
import sgc.sgrh.Usuario;
import sgc.sgrh.UsuarioRepo;
import sgc.subprocesso.SituacaoSubprocesso;
import sgc.subprocesso.modelo.Movimentacao;
import sgc.subprocesso.modelo.MovimentacaoRepo;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.subprocesso.modelo.SubprocessoRepo;
import sgc.unidade.modelo.SituacaoUnidade;
import sgc.unidade.modelo.TipoUnidade;
import sgc.unidade.modelo.Unidade;
import sgc.unidade.modelo.UnidadeRepo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("CDU-14: Analisar revisão de cadastro de atividades e conhecimentos")
class CDU14IntegrationTest {    @Autowired
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
    private AlertaRepo alertaRepo;
    @Autowired
    private AnaliseRepo analiseRepo;
    @Autowired
    private MapaRepo mapaRepo;
    @Autowired
    private AtividadeRepo atividadeRepo;
    @Autowired
    private UnidadeMapaRepo unidadeMapaRepo;
    @Autowired
    private WebApplicationContext context;

    private Subprocesso subprocesso;
    private Unidade unidade, unidadeGestor, unidadeAdmin;

    @BeforeEach
    void setUp() {
        mockMvc = webAppContextSetup(context).apply(springSecurity()).build();

        // Limpeza de dados
        processoRepo.deleteAll();
        unidadeRepo.deleteAll();
        usuarioRepo.deleteAll();
        mapaRepo.deleteAll();
        atividadeRepo.deleteAll();
        unidadeMapaRepo.deleteAll();
        subprocessoRepo.deleteAll();
        movimentacaoRepo.deleteAll();
        alertaRepo.deleteAll();

        // Hierarquia de Unidades e Usuários
        unidadeAdmin = criarUnidade(1L, "ADMIN-UNIT", null, null);
        Usuario admin = criarUsuario(111111111111L, "Admin", "admin@email.com", "1111", unidadeAdmin, Set.of(Perfil.ADMIN));
        unidadeAdmin.setTitular(admin);
        unidadeRepo.save(unidadeAdmin);

        unidadeGestor = criarUnidade(3L, "GESTOR-UNIT", unidadeAdmin, null);
        Usuario gestor = criarUsuario(222222222222L, "Gestor", "gestor@email.com", "2222", unidadeGestor, Set.of(Perfil.GESTOR));
        unidadeGestor.setTitular(gestor);
        unidadeRepo.save(unidadeGestor);

        unidade = criarUnidade(4L, "SUB-UNIT", unidadeGestor, null);
        Usuario chefe = criarUsuario(333333333333L, "Chefe", "chefe@email.com", "3333", unidade, Set.of(Perfil.CHEFE));
        unidade.setTitular(chefe);
        unidadeRepo.save(unidade);

        // Processo e Subprocesso
        Processo processo = criarProcesso("Processo de Revisão Teste", TipoProcesso.REVISAO, SituacaoProcesso.EM_ANDAMENTO);

        // Mapa Vigente e suas atividades
        Mapa mapaVigente = criarMapa();
        criarAtividade(mapaVigente, "Atividade Vigente 1");
        criarAtividade(mapaVigente, "Atividade Vigente 2");

        // Associar mapa vigente à unidade
        criarUnidadeMapa(unidade.getCodigo(), mapaVigente.getCodigo());

        Mapa mapaSubprocesso = criarMapa(); // Este é o mapa do subprocesso
        subprocesso = criarSubprocesso(processo, unidade, mapaSubprocesso, SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA);

        // Coloca o subprocesso na fila da unidade gestora para análise
        criarMovimentacao(subprocesso, unidade, unidadeGestor, "Disponibilização da revisão do cadastro");
    }

    @Nested
    @DisplayName("Testes para o fluxo de 'Devolver para Ajustes'")
    class DevolverCadastroTest {
        @Test
        @DisplayName("GESTOR deve devolver revisão do cadastro, alterar status, registrar análise, movimentação e alerta")
        @WithMockGestor
        void testGestorDevolveRevisaoCadastro() throws Exception {
            // Ação
            mockMvc.perform(post("/api/subprocessos/{id}/devolver-revisao-cadastro", subprocesso.getCodigo())
                            .with(csrf())
                            .contentType("application/json")
                            .content("{\"motivo\": \"Teste\", \"observacoes\": \"Ajustar X, Y, Z\"}"))
                    .andExpect(status().isOk());

            // Verificações
            Subprocesso spAtualizado = subprocessoRepo.findById(subprocesso.getCodigo()).orElseThrow();
            assertThat(spAtualizado.getSituacao()).isEqualTo(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);

            var analises = analiseRepo.findBySubprocessoCodigoOrderByDataHoraDesc(subprocesso.getCodigo());
            assertThat(analises).hasSize(1);
            assertThat(analises.getFirst().getAcao()).isEqualTo(sgc.analise.modelo.TipoAcaoAnalise.DEVOLUCAO_REVISAO);
            assertThat(analises.getFirst().getObservacoes()).isEqualTo("Ajustar X, Y, Z");
            assertThat(analises.getFirst().getUnidadeSigla()).isEqualTo(unidadeGestor.getSigla());

            var movimentacoes = movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(subprocesso.getCodigo());
            assertThat(movimentacoes).hasSize(2);
            assertThat(movimentacoes.getFirst().getUnidadeDestino().getSigla()).isEqualTo(unidade.getSigla());

            var alertas = alertaRepo.findAll();
            assertThat(alertas).hasSize(1);
            assertThat(alertas.getFirst().getUnidadeDestino().getSigla()).isEqualTo(unidade.getSigla());
            assertThat(alertas.getFirst().getDescricao()).contains("devolvido para ajustes");
        }
    }

    @Nested
    @DisplayName("Testes para o fluxo de 'Registrar Aceite'")
    class AceitarCadastroTest {
        @Test
        @DisplayName("GESTOR deve aceitar revisão do cadastro, registrar análise, e mover para unidade superior")
        @WithMockGestor
        void testGestorAceitaRevisaoCadastro() throws Exception {
            mockMvc.perform(post("/api/subprocessos/{id}/aceitar-revisao-cadastro", subprocesso.getCodigo())
                            .with(csrf())
                            .contentType("application/json")
                            .content("{\"observacoes\": \"Tudo certo.\"}"))
                    .andExpect(status().isOk());

            Subprocesso spAtualizado = subprocessoRepo.findById(subprocesso.getCodigo()).orElseThrow();
            assertThat(spAtualizado.getSituacao()).isEqualTo(SituacaoSubprocesso.AGUARDANDO_HOMOLOGACAO_CADASTRO);

            var analises = analiseRepo.findBySubprocessoCodigoOrderByDataHoraDesc(subprocesso.getCodigo());
            assertThat(analises).hasSize(1);
            assertThat(analises.getFirst().getAcao()).isEqualTo(sgc.analise.modelo.TipoAcaoAnalise.ACEITE_REVISAO);
            assertThat(analises.getFirst().getUnidadeSigla()).isEqualTo(unidadeGestor.getSigla());

            var movimentacoes = movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(subprocesso.getCodigo());
            assertThat(movimentacoes).hasSize(2);
            assertThat(movimentacoes.getFirst().getUnidadeOrigem().getSigla()).isEqualTo(unidadeGestor.getSigla());
            assertThat(movimentacoes.getFirst().getUnidadeDestino().getSigla()).isEqualTo(unidadeAdmin.getSigla());
        }
    }

    @Nested
    @DisplayName("Testes para o fluxo de 'Homologar'")
    class HomologarCadastroTest {
        @BeforeEach
        void setup() {
            // Limpeza de dados específica para este setup, se necessário, ou confiar no setup global
            // Para este caso, como o setup global já limpa tudo, podemos focar na criação.
            // No entanto, para garantir o isolamento, vou manter as limpezas específicas aqui.
            unidadeMapaRepo.deleteAll();
            atividadeRepo.deleteAll();
            movimentacaoRepo.deleteAll();
            subprocessoRepo.deleteAll();
            mapaRepo.deleteAll();
            processoRepo.deleteAll();

            // Criar um novo processo
            Processo processo = criarProcesso("Processo de Revisão Teste", TipoProcesso.REVISAO, SituacaoProcesso.EM_ANDAMENTO);

            // Criar e persistir o mapa vigente
            Mapa mapaVigente = criarMapa();
            criarAtividade(mapaVigente, "Atividade Vigente 1");
            criarAtividade(mapaVigente, "Atividade Vigente 2");

            // Associar mapa vigente à unidade
            criarUnidadeMapa(CDU14IntegrationTest.this.unidade.getCodigo(), mapaVigente.getCodigo());

            subprocesso = criarSubprocesso(processo, CDU14IntegrationTest.this.unidade, mapaVigente, SituacaoSubprocesso.AGUARDANDO_HOMOLOGACAO_CADASTRO);
        }

        @Test
        @DisplayName("ADMIN deve homologar revisão do cadastro SEM impactos, alterando status para MAPA_HOMOLOGADO")
        @WithMockAdmin
        void testHomologarRevisaoCadastro_SemImpactos() throws Exception {
            Mapa mapaVigente = mapaRepo.findById(unidadeMapaRepo.findByUnidadeCodigo(unidade.getCodigo()).orElseThrow().getMapaVigenteCodigo()).orElseThrow();
            List<Atividade> atividadesVigentes = atividadeRepo.findByMapaCodigo(mapaVigente.getCodigo());
            atividadeRepo.saveAll(atividadesVigentes.stream()
                    .map(a -> new Atividade(subprocesso.getMapa(), a.getDescricao()))
                    .toList());

            // Ação
            subprocesso = subprocessoRepo.findById(subprocesso.getCodigo()).orElseThrow();
            String jsonContent = objectMapper.writeValueAsString(Map.of("observacoes", "Homologado sem impactos"));
            mockMvc.perform(post("/api/subprocessos/{codigo}/homologar-revisao-cadastro", subprocesso.getCodigo())
                            .with(csrf())
                            .contentType("application/json")
                            .content(jsonContent))
                    .andExpect(status().isOk());

            // Verificações
            Subprocesso spAtualizado = subprocessoRepo.findById(subprocesso.getCodigo()).orElseThrow();
            assertThat(spAtualizado.getSituacao()).isEqualTo(SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA);
            List<Movimentacao> movimentacoes = movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(subprocesso.getCodigo());
            assertThat(movimentacoes).hasSize(1);
            assertThat(movimentacoes.getFirst().getDescricao()).isEqualTo("Cadastro de atividades e conhecimentos homologado");
            assertThat(movimentacoes.getFirst().getUnidadeOrigem().getSigla()).isEqualTo(unidadeAdmin.getSigla());
            assertThat(movimentacoes.getFirst().getUnidadeDestino().getSigla()).isEqualTo(unidadeAdmin.getSigla());
        }

        @Test
        @DisplayName("ADMIN deve homologar revisão do cadastro COM impactos, alterando status para REVISAO_CADASTRO_HOMOLOGADA e criando movimentação")
        @WithMockAdmin
        void testHomologarRevisaoCadastro_ComImpactos() throws Exception {
            // Cenário: Adicionar atividades para simular impactos (uma atividade a mais que o mapa vigente)
            Mapa mapaVigente = mapaRepo.findById(unidadeMapaRepo.findByUnidadeCodigo(unidade.getCodigo()).orElseThrow().getMapaVigenteCodigo()).orElseThrow();
            List<Atividade> atividadesVigentes = atividadeRepo.findByMapaCodigo(mapaVigente.getCodigo());
            atividadeRepo.saveAll(atividadesVigentes.stream()
                    .map(a -> new Atividade(subprocesso.getMapa(), a.getDescricao()))
                    .toList());
            atividadeRepo.save(new Atividade(subprocesso.getMapa(), "Atividade com impacto extra"));

            // Ação
            subprocesso = subprocessoRepo.findById(subprocesso.getCodigo()).orElseThrow();
            String jsonContent = objectMapper.writeValueAsString(Map.of("observacoes", "Homologado com impactos"));
            mockMvc.perform(post("/api/subprocessos/{id}/homologar-revisao-cadastro", subprocesso.getCodigo())
                            .with(csrf())
                            .contentType("application/json")
                            .content(jsonContent))
                    .andExpect(status().isOk());

            // Verificações
            Subprocesso spAtualizado = subprocessoRepo.findById(subprocesso.getCodigo()).orElseThrow();
            assertThat(spAtualizado.getSituacao()).isEqualTo(SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA);

            // Deve haver a movimentação inicial e a de homologação
            List<Movimentacao> movimentacoes = movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(subprocesso.getCodigo());
            assertThat(movimentacoes).hasSize(1);
            assertThat(movimentacoes.getFirst().getDescricao()).isEqualTo("Cadastro de atividades e conhecimentos homologado");
            assertThat(movimentacoes.getFirst().getUnidadeOrigem().getSigla()).isEqualTo(unidadeAdmin.getSigla());
            assertThat(movimentacoes.getFirst().getUnidadeDestino().getSigla()).isEqualTo(unidadeAdmin.getSigla());
        }
    }

    // Métodos auxiliares para criação de entidades
    private Unidade criarUnidade(Long codigo, String sigla, Unidade unidadeSuperior, Usuario titular) {
        // Usar o construtor correto da Unidade que aceita Long para o código
        Unidade novaUnidade = new Unidade(codigo, sigla, sigla, TipoUnidade.OPERACIONAL, SituacaoUnidade.ATIVA);
        novaUnidade.setUnidadeSuperior(unidadeSuperior);
        unidadeRepo.save(novaUnidade);
        if (titular != null) {
            novaUnidade.setTitular(titular);
            unidadeRepo.save(novaUnidade);
        }
        return novaUnidade;
    }

    private Usuario criarUsuario(Long cpf, String nome, String email, String senha, Unidade unidade, Set<Perfil> perfis) {
        Usuario novoUsuario = new Usuario(cpf, nome, email, senha, unidade, perfis);
        return usuarioRepo.save(novoUsuario);
    }

    private Processo criarProcesso(String nome, TipoProcesso tipo, SituacaoProcesso situacao) {
        Processo novoProcesso = new Processo(nome, tipo, situacao, LocalDateTime.now());
        return processoRepo.save(novoProcesso);
    }

    private Mapa criarMapa() {
        return mapaRepo.save(new Mapa());
    }

    private Atividade criarAtividade(Mapa mapa, String descricao) {
        return atividadeRepo.save(new Atividade(mapa, descricao));
    }

    private UnidadeMapa criarUnidadeMapa(Long unidadeCodigo, Long mapaVigenteCodigo) {
        UnidadeMapa unidadeMapa = new sgc.mapa.modelo.UnidadeMapa(unidadeCodigo);
        unidadeMapa.setMapaVigenteCodigo(mapaVigenteCodigo);
        unidadeMapa.setDataVigencia(LocalDateTime.now());
        return unidadeMapaRepo.save(unidadeMapa);
    }

    private Subprocesso criarSubprocesso(Processo processo, Unidade unidade, Mapa mapa, SituacaoSubprocesso situacao) {
        Subprocesso novoSubprocesso = new Subprocesso(processo, unidade, mapa, situacao, LocalDateTime.now());
        return subprocessoRepo.save(novoSubprocesso);
    }

    private Movimentacao criarMovimentacao(Subprocesso subprocesso, Unidade origem, Unidade destino, String descricao) {
        Movimentacao novaMovimentacao = new Movimentacao(subprocesso, origem, destino, descricao);
        return movimentacaoRepo.save(novaMovimentacao);
    }
}