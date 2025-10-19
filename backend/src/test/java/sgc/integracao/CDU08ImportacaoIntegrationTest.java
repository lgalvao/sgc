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
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import sgc.integracao.mocks.TestSecurityConfig;
import sgc.integracao.mocks.WithMockChefe;
import sgc.mapa.modelo.Mapa;
import sgc.mapa.modelo.MapaRepo;
import sgc.sgrh.Perfil;
import sgc.sgrh.Usuario;
import sgc.sgrh.UsuarioRepo;
import sgc.subprocesso.SituacaoSubprocesso;
import sgc.subprocesso.dto.ImportarAtividadesRequest;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.subprocesso.modelo.SubprocessoRepo;
import sgc.unidade.modelo.Unidade;
import sgc.unidade.modelo.UnidadeRepo;
import sgc.atividade.modelo.Atividade;
import sgc.atividade.modelo.AtividadeRepo;
import sgc.conhecimento.modelo.Conhecimento;
import sgc.conhecimento.modelo.ConhecimentoRepo;
import sgc.subprocesso.modelo.MovimentacaoRepo;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.is;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
@DisplayName("CDU-08: Manter cadastro de atividades e conhecimentos - Importação")
class CDU08ImportacaoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepo usuarioRepo;

    @Autowired
    private UnidadeRepo unidadeRepo;

    @Autowired
    private MapaRepo mapaRepo;

    @Autowired
    private SubprocessoRepo subprocessoRepo;

    @Autowired
    private AtividadeRepo atividadeRepo;

    @Autowired
    private ConhecimentoRepo conhecimentoRepo;

    @Autowired
    private MovimentacaoRepo movimentacaoRepo;

    private Subprocesso subprocessoOrigem;
    private Subprocesso subprocessoDestino;

    @Nested
    @DisplayName("Testes de importação de atividades")
    @WithMockChefe("888888888888")
    class ImportacaoAtividades {

        @Test
        @DisplayName("Deve importar atividades e conhecimentos com sucesso")
        void deveImportarAtividadesEConhecimentosComSucesso() throws Exception {
            ImportarAtividadesRequest request = new ImportarAtividadesRequest(subprocessoOrigem.getCodigo());

            mockMvc.perform(post("/api/subprocessos/{id}/importar-atividades", subprocessoDestino.getCodigo()).with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Atividades importadas com sucesso.")));

            List<Atividade> atividadesDestino = atividadeRepo.findByMapaCodigo(subprocessoDestino.getMapa().getCodigo());
            assertThat(atividadesDestino).hasSize(2);

            Atividade atividade1Importada = atividadesDestino.stream().filter(a -> a.getDescricao().equals("Atividade 1")).findFirst().orElse(null);
            assertThat(atividade1Importada).isNotNull();
            List<Conhecimento> conhecimentos1 = conhecimentoRepo.findByAtividadeCodigo(atividade1Importada.getCodigo());
            assertThat(conhecimentos1).hasSize(1);
            assertThat(conhecimentos1.get(0).getDescricao()).isEqualTo("Conhecimento 1.1");

            Atividade atividade2Importada = atividadesDestino.stream().filter(a -> a.getDescricao().equals("Atividade 2")).findFirst().orElse(null);
            assertThat(atividade2Importada).isNotNull();
            List<Conhecimento> conhecimentos2 = conhecimentoRepo.findByAtividadeCodigo(atividade2Importada.getCodigo());
            assertThat(conhecimentos2).hasSize(2);
            assertThat(conhecimentos2.stream().map(Conhecimento::getDescricao).toList()).containsExactlyInAnyOrder("Conhecimento 2.1", "Conhecimento 2.2");

            List<sgc.subprocesso.modelo.Movimentacao> movimentacoes = movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(subprocessoDestino.getCodigo());
            assertThat(movimentacoes).hasSize(1);
            assertThat(movimentacoes.get(0).getDescricao()).contains("Importação de atividades do subprocesso #" + subprocessoOrigem.getCodigo());
        }

        @Test
        @DisplayName("Deve falhar ao importar para subprocesso em estado inválido")
        void deveFalharAoImportarParaSubprocessoEmEstadoInvalido() throws Exception {
            subprocessoDestino.setSituacao(SituacaoSubprocesso.CADASTRO_DISPONIBILIZADO);
            subprocessoRepo.save(subprocessoDestino);

            ImportarAtividadesRequest request = new ImportarAtividadesRequest(subprocessoOrigem.getCodigo());

            mockMvc.perform(post("/api/subprocessos/{id}/importar-atividades", subprocessoDestino.getCodigo()).with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("Deve falhar ao importar de subprocesso inexistente")
        void deveFalharAoImportarDeSubprocessoInexistente() throws Exception {
            ImportarAtividadesRequest request = new ImportarAtividadesRequest(9999L);

            mockMvc.perform(post("/api/subprocessos/{id}/importar-atividades", subprocessoDestino.getCodigo()).with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Não deve importar nada de subprocesso sem atividades")
        void naoDeveImportarNadaDeSubprocessoSemAtividades() throws Exception {
            Mapa mapaOrigemVazio = new Mapa();
            mapaRepo.save(mapaOrigemVazio);
            subprocessoOrigem.setMapa(mapaOrigemVazio);
            subprocessoRepo.save(subprocessoOrigem);

            ImportarAtividadesRequest request = new ImportarAtividadesRequest(subprocessoOrigem.getCodigo());

            mockMvc.perform(post("/api/subprocessos/{id}/importar-atividades", subprocessoDestino.getCodigo()).with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

            List<Atividade> atividadesDestino = atividadeRepo.findByMapaCodigo(subprocessoDestino.getMapa().getCodigo());
            assertThat(atividadesDestino).isEmpty();
        }

        @Test
        @DisplayName("Deve importar apenas atividades não existentes no destino")
        void deveImportarApenasAtividadesNaoExistentesNoDestino() throws Exception {
            Atividade atividadeExistente = new Atividade(subprocessoDestino.getMapa(), "Atividade 2");
            atividadeRepo.save(atividadeExistente);

            ImportarAtividadesRequest request = new ImportarAtividadesRequest(subprocessoOrigem.getCodigo());

            mockMvc.perform(post("/api/subprocessos/{id}/importar-atividades", subprocessoDestino.getCodigo()).with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

            List<Atividade> atividadesDestino = atividadeRepo.findByMapaCodigo(subprocessoDestino.getMapa().getCodigo());
            assertThat(atividadesDestino).hasSize(2);
            assertThat(atividadesDestino.stream().map(Atividade::getDescricao).toList()).containsExactlyInAnyOrder("Atividade 1", "Atividade 2");
        }
    }

    @BeforeEach
    void setUp() {
        Usuario chefe = new Usuario();
        chefe.setTituloEleitoral(888888888888L);
        chefe.setNome("Chefe de Teste");
        chefe.setPerfis(java.util.Set.of(Perfil.CHEFE));
        usuarioRepo.save(chefe);

        Unidade unidadeOrigem = new Unidade("UNIDADE-CDU08-ORIGEM", "U08O");
        unidadeOrigem.setTitular(chefe);
        unidadeRepo.save(unidadeOrigem);

        Unidade unidadeDestino = new Unidade("UNIDADE-CDU08-DESTINO", "U08D");
        unidadeDestino.setTitular(chefe);
        unidadeRepo.save(unidadeDestino);

        Mapa mapaOrigem = new Mapa();
        mapaRepo.save(mapaOrigem);

        Atividade atividade1 = new Atividade(mapaOrigem, "Atividade 1");
        atividadeRepo.save(atividade1);
        conhecimentoRepo.save(new Conhecimento("Conhecimento 1.1", atividade1));

        Atividade atividade2 = new Atividade(mapaOrigem, "Atividade 2");
        atividadeRepo.save(atividade2);
        conhecimentoRepo.save(new Conhecimento("Conhecimento 2.1", atividade2));
        conhecimentoRepo.save(new Conhecimento("Conhecimento 2.2", atividade2));

        subprocessoOrigem = new Subprocesso();
        subprocessoOrigem.setUnidade(unidadeOrigem);
        subprocessoOrigem.setMapa(mapaOrigem);
        subprocessoOrigem.setSituacao(SituacaoSubprocesso.MAPA_HOMOLOGADO);
        subprocessoOrigem.setDataLimiteEtapa1(LocalDateTime.now().plusDays(10));
        subprocessoRepo.save(subprocessoOrigem);

        Mapa mapaDestino = new Mapa();
        mapaRepo.save(mapaDestino);

        subprocessoDestino = new Subprocesso();
        subprocessoDestino.setUnidade(unidadeDestino);
        subprocessoDestino.setMapa(mapaDestino);
        subprocessoDestino.setSituacao(SituacaoSubprocesso.CADASTRO_EM_ANDAMENTO);
        subprocessoDestino.setDataLimiteEtapa1(LocalDateTime.now().plusDays(10));
        subprocessoRepo.save(subprocessoDestino);
    }
}
