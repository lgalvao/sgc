package sesel.sgc.mapa.atividades;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import sesel.sgc.mapa.MapaUnidadeSituacao;
import sesel.sgc.mapa.ValidacaoErro;
import sesel.sgc.mapa.conhecimentos.Conhecimento;
import sesel.sgc.mapa.conhecimentos.ConhecimentoRepository;
import sesel.sgc.processo.Processo;
import sesel.sgc.processo.ProcessoRepository;
import sesel.sgc.processounidade.ProcessoUnidade;
import sesel.sgc.processounidade.ProcessoUnidadeRepository;
import sesel.sgc.unidade.Unidade;
import sesel.sgc.unidade.UnidadeRepository;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static sesel.sgc.mapa.conhecimentos.ConhecimentoTipo.TECNICO;

@SpringBootTest
@AutoConfigureMockMvc
class AtividadeControllerIntegrationTest {
        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private UnidadeRepository unidadeRepository;

        @Autowired
        private ProcessoRepository processoRepository;

        @Autowired
        private ProcessoUnidadeRepository processoUnidadeRepository;

        @Autowired
        private ConhecimentoRepository conhecimentoRepository;

        @Autowired
        private ApplicationContext context;

        @MockitoSpyBean
        AtividadeService atividadeService;

        @WithMockUser(username = "admin", roles = { "SEDOC" })
        @Transactional
        @Test
        void deveRetornarOkNaListagemDeAtividadesPorProcessoUnidade() throws Exception {
                Unidade unidade = new Unidade();
                unidade.setNome("Unidade Teste %s".formatted(UUID.randomUUID()));
                unidade.setDescricao("Descricao Teste");
                unidade = unidadeRepository.save(unidade);

                Processo processo = new Processo();
                processo.setNome("Processo Teste %s".formatted(UUID.randomUUID()));
                processo.setDataLimite(LocalDate.now().plusDays(1));
                processo = processoRepository.save(processo);

                ProcessoUnidade processoUnidade = new ProcessoUnidade();
                processoUnidade.setUnidade(unidade);
                processoUnidade.setProcesso(processo);
                processoUnidade = processoUnidadeRepository.save(processoUnidade);

                mockMvc.perform(get("/processo-unidade/%s/atividades".formatted(processoUnidade.getId())))
                                .andExpect(status().isOk());
        }

        @WithMockUser(username = "admin", roles = { "SEDOC" })
        @Transactional
        @Test
        void deveCriarAtividadeComConhecimentos() throws Exception {
                Unidade unidade = new Unidade();
                unidade.setNome("Unidade Teste " + UUID.randomUUID());
                unidade.setDescricao("Descricao Teste");
                unidade = unidadeRepository.save(unidade);

                Processo processo = new Processo();
                processo.setNome("Processo Teste " + UUID.randomUUID());
                processo.setDataLimite(LocalDate.now().plusDays(1));
                processo = processoRepository.save(processo);

                ProcessoUnidade processoUnidade = new ProcessoUnidade();
                processoUnidade.setUnidade(unidade);
                processoUnidade.setProcesso(processo);
                processoUnidade = processoUnidadeRepository.save(processoUnidade);

                String descricaoAtividade = "Atividade Integração %s".formatted(UUID.randomUUID());
                String descricaoConhecimento = "Conhecimento Integração %s".formatted(UUID.randomUUID());
                String tipoConhecimento = "TECNICO";

                String formData = "atividades[0].descricao=%s&atividades[0].conhecimentos[0].descricao=%s&atividades[0].conhecimentos[0].tipo=%s"
                        .formatted(descricaoAtividade, descricaoConhecimento, tipoConhecimento);

                mockMvc.perform(post("/processo-unidade/%s/atividades".formatted(processoUnidade.getId()))
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                .content(formData))
                                .andExpect(redirectedUrlPattern("/processo-unidade/*/atividades"));

                // Verifica se o conhecimento foi persistido
                boolean conhecimentoExiste = conhecimentoRepository.findAll().stream()
                                .anyMatch(k -> descricaoConhecimento.equals(k.getDescricao()));
                assertTrue(conhecimentoExiste, "Conhecimento deveria ter sido persistido");
        }

        @WithMockUser(username = "admin", roles = { "SEDOC" })
        @Transactional
        @DirtiesContext
        @Test
        void deveRedirecionarComErroQuandoExcecaoNegocioNoPost() throws Exception {
                AtividadeService atividadeService = context.getBean(AtividadeService.class);
                Unidade unidade = new Unidade();
                unidade.setNome("Unidade Teste %s".formatted(UUID.randomUUID()));
                unidade.setDescricao("Descricao Teste");
                unidade = unidadeRepository.save(unidade);

                Processo processo = new Processo();
                processo.setNome("Processo Teste %s".formatted(UUID.randomUUID()));
                processo.setDataLimite(LocalDate.now().plusDays(1));
                processo = processoRepository.save(processo);

                ProcessoUnidade processoUnidade = new ProcessoUnidade();
                processoUnidade.setUnidade(unidade);
                processoUnidade.setProcesso(processo);
                processoUnidade = processoUnidadeRepository.save(processoUnidade);

                doThrow(new ValidacaoErro("Erro de negócio"))
                                .when(atividadeService).cadastrarAtividades(org.mockito.ArgumentMatchers.any());

                String formData = "atividades[0].descricao=Teste&atividades[0].conhecimentos[0].descricao=Java";
                mockMvc.perform(post("/processo-unidade/%s/atividades".formatted(processoUnidade.getId()))
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                .content(formData))
                        .andExpect(status().isOk())
                        .andExpect(view().name("mapa/atividades-form"))
                        .andExpect(model().attributeExists("error"));
        }

        @WithMockUser(username = "admin", roles = { "SEDOC" })
        @Transactional
        @Test
        void deveExibirFormularioComAtividadesEChecarConhecimentos() throws Exception {
                Unidade unidade = new Unidade();
                unidade.setNome("Unidade Teste " + UUID.randomUUID());
                unidade.setDescricao("Descricao Teste");
                unidade = unidadeRepository.save(unidade);

                Processo processo = new Processo();
                processo.setNome("Processo Teste " + UUID.randomUUID());
                processo.setDataLimite(LocalDate.now().plusDays(1));
                processo = processoRepository.save(processo);

                ProcessoUnidade processoUnidade = new ProcessoUnidade();
                processoUnidade.setUnidade(unidade);
                processoUnidade.setProcesso(processo);
                processoUnidade = processoUnidadeRepository.save(processoUnidade);

                // Cria atividade e conhecimento
                Atividade atividade = new Atividade();
                atividade.setDescricao("Atividade Integracao");
                atividade.setProcessoUnidade(processoUnidade);
                // Não salva ainda

                Conhecimento conhecimento = new Conhecimento();
                conhecimento.setDescricao("Conhecimento Integracao");
                conhecimento.setTipo(TECNICO);
                conhecimento.setAtividade(atividade);

                // Relacionamento bidirecional
                atividade.getConhecimentos().add(conhecimento);

                // Salva atividade (em cascata salva conhecimento)
                context.getBean(AtividadeRepository.class).saveAndFlush(atividade);
                context.getBean(ConhecimentoRepository.class).flush();

                // Assert extra: conhecimento está salvo no banco?
                boolean conhecimentoExiste = context.getBean(ConhecimentoRepository.class)
                                .findAll().stream().anyMatch(k -> "Conhecimento Integracao".equals(k.getDescricao()));

                assertTrue(conhecimentoExiste, "Conhecimento deveria ter sido persistido antes do GET");

                MvcResult result = mockMvc
                                .perform(get("/processo-unidade/%s/atividades".formatted(processoUnidade.getId())))
                                .andExpect(status().isOk())
                                .andReturn();

                String content = result.getResponse().getContentAsString();

                assertTrue(content.contains("value=\"Atividade Integracao\""),
                                "Deve exibir campo preenchido para atividade");

                assertTrue(content.contains("value=\"Conhecimento Integracao\""),
                                "Deve exibir campo preenchido para conhecimento");
        }

        @WithAnonymousUser
        @Test
        void deveRedirecionarParaLoginQuandoUsuarioNaoAutenticado() throws Exception {
                mockMvc.perform(get("/processo-unidade/%s/atividades".formatted(UUID.randomUUID())))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrlPattern("**/login"));
        }

        @WithMockUser(username = "admin", roles = { "SEDOC" })
        @Transactional
        @Test
        void deveExibirFormularioQuandoAtividadeComConhecimentosNulo() throws Exception {
                Unidade unidade = new Unidade();
                unidade.setNome("Unidade Teste " + UUID.randomUUID());
                unidade.setDescricao("Descricao Teste");
                unidade = unidadeRepository.save(unidade);

                Processo processo = new Processo();
                processo.setNome("Processo Teste " + UUID.randomUUID());
                processo.setDataLimite(LocalDate.now().plusDays(1));
                processo = processoRepository.save(processo);

                ProcessoUnidade processoUnidade = new ProcessoUnidade();
                processoUnidade.setUnidade(unidade);
                processoUnidade.setProcesso(processo);
                processoUnidade = processoUnidadeRepository.save(processoUnidade);

                // Cria atividade com conhecimentos nulo
                Atividade atividade = new Atividade();
                atividade.setDescricao("Atividade Sem Conhecimentos");
                atividade.setProcessoUnidade(processoUnidade);
                atividade.setConhecimentos(null);
                context.getBean(AtividadeRepository.class).save(atividade);

                mockMvc.perform(get("/processo-unidade/%s/atividades".formatted(processoUnidade.getId())))
                                .andExpect(status().isOk());
        }

        @WithMockUser(username = "admin", roles = {"SEDOC"})
        @Transactional
        @Test
        void deveExibirModoSomenteLeituraParaUnidadeFinalizada() throws Exception {
                Unidade unidade = new Unidade();
                unidade.setNome("Unidade Homologada " + UUID.randomUUID());
                unidade.setDescricao("Descricao Teste");
                unidade = unidadeRepository.save(unidade);

                Processo processo = new Processo();
                processo.setNome("Processo Teste " + UUID.randomUUID());
                processo.setDataLimite(LocalDate.now().plusDays(1));
                processo = processoRepository.save(processo);

                ProcessoUnidade processoUnidade = new ProcessoUnidade();
                processoUnidade.setUnidade(unidade);
                processoUnidade.setProcesso(processo);
                processoUnidade.setSituacao(MapaUnidadeSituacao.FINALIZADA);
                processoUnidade = processoUnidadeRepository.save(processoUnidade);

                mockMvc.perform(get("/processo-unidade/%s/atividades".formatted(processoUnidade.getId())))
                        .andExpect(status().isOk())
                        .andExpect(model().attribute("somenteLeitura", true));
        }
}