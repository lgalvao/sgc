package sgc.integracao;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import sgc.fixture.UnidadeFixture;
import sgc.fixture.UsuarioFixture;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Conhecimento;
import sgc.mapa.model.ConhecimentoRepo;
import sgc.mapa.model.Mapa;
import sgc.organizacao.model.*;
import sgc.processo.model.Processo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.dto.ImportarAtividadesRequest;
import sgc.subprocesso.model.Movimentacao;
import sgc.subprocesso.model.MovimentacaoRepo;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("integration")
@Transactional
@DisplayName("CDU-08: Manter cadastro de atividades e conhecimentos")
class CDU08IntegrationTest extends BaseIntegrationTest {
    @Autowired
    private ConhecimentoRepo conhecimentoRepo;

    @Autowired
    private MovimentacaoRepo movimentacaoRepo;

    @Autowired
    private UsuarioRepo usuarioRepo;

    @Autowired
    private UsuarioPerfilRepo usuarioPerfilRepo;

    @Autowired
    private EntityManager entityManager;

    private Subprocesso subprocessoOrigem;
    private Subprocesso subprocessoDestino;
    private Usuario chefe;

    @BeforeEach
    void setUp() {

        // 1. Criar unidades (DISTINTAS)
        Unidade raiz = unidadeRepo.findById(1L).orElseThrow();
        Unidade unidadeOrigem = UnidadeFixture.unidadePadrao();
        unidadeOrigem.setCodigo(null);
        unidadeOrigem.setSigla("U_ORIG");
        unidadeOrigem.setNome("Unidade Origem");
        unidadeOrigem.setUnidadeSuperior(raiz);
        unidadeOrigem = unidadeRepo.save(unidadeOrigem);

        Unidade unidadeDestino = UnidadeFixture.unidadePadrao();
        unidadeDestino.setCodigo(null);
        unidadeDestino.setSigla("U_DEST");
        unidadeDestino.setNome("Unidade Destino");
        unidadeDestino.setUnidadeSuperior(raiz);
        unidadeDestino = unidadeRepo.save(unidadeDestino);

        // 2. Criar chefe para unidade destino (para validação de segurança)
        chefe = UsuarioFixture.usuarioPadrao();
        chefe.setTituloEleitoral("888888888888");
        chefe.setNome("Chefe Destino");
        chefe.setUnidadeLotacao(unidadeDestino);
        chefe = usuarioRepo.save(chefe);

        UsuarioPerfil perfilChefe = UsuarioPerfil.builder()
                .usuarioTitulo(chefe.getTituloEleitoral())
                .usuario(chefe)
                .unidadeCodigo(unidadeDestino.getCodigo())
                .unidade(unidadeDestino)
                .perfil(Perfil.CHEFE)
                .build();
        usuarioPerfilRepo.save(perfilChefe);

        // Configuração manual de autenticação
        chefe.setPerfilAtivo(Perfil.CHEFE);
        chefe.setUnidadeAtivaCodigo(unidadeDestino.getCodigo());
        chefe.setAuthorities(Set.of(new SimpleGrantedAuthority("ROLE_CHEFE")));
        Authentication auth = new UsernamePasswordAuthenticationToken(chefe, null, chefe.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        // 3. Processo Origem (FINALIZADO) - Permite importação de qualquer unidade
        Processo processoOrigem = Processo.builder()
                .descricao("Processo Origem Finalizado")
                .tipo(TipoProcesso.MAPEAMENTO)
                .situacao(SituacaoProcesso.FINALIZADO)
                .dataLimite(LocalDateTime.now().minusDays(30))
                .build();
        processoRepo.save(processoOrigem);

        subprocessoOrigem = Subprocesso.builder()
                .unidade(unidadeOrigem)
                .situacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO)
                .dataLimiteEtapa1(LocalDateTime.now().minusDays(40))
                .processo(processoOrigem)
                .build();
        subprocessoRepo.save(subprocessoOrigem);

        Mapa mapaOrigem = new Mapa();
        mapaOrigem.setSubprocesso(subprocessoOrigem);
        mapaRepo.save(mapaOrigem);
        subprocessoOrigem.setMapa(mapaOrigem);

        Atividade atividade1 = Atividade.builder().mapa(mapaOrigem).descricao("Atividade 1").build();
        atividadeRepo.save(atividade1);
        conhecimentoRepo.save(Conhecimento.builder().descricao("Conhecimento 1.1").atividade(atividade1).build());

        Atividade atividade2 = Atividade.builder().mapa(mapaOrigem).descricao("Atividade 2").build();
        atividadeRepo.save(atividade2);
        conhecimentoRepo.save(Conhecimento.builder().descricao("Conhecimento 2.1").atividade(atividade2).build());
        conhecimentoRepo.save(Conhecimento.builder().descricao("Conhecimento 2.2").atividade(atividade2).build());

        // 4. Processo Destino (EM ANDAMENTO)
        Processo processoDestino = Processo.builder()
                .descricao("Processo Destino Ativo")
                .tipo(TipoProcesso.MAPEAMENTO)
                .situacao(SituacaoProcesso.EM_ANDAMENTO)
                .dataLimite(LocalDateTime.now().plusDays(30))
                .build();
        processoRepo.save(processoDestino);

        subprocessoDestino = Subprocesso.builder()
                .unidade(unidadeDestino)
                .situacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO)
                .dataLimiteEtapa1(LocalDateTime.now().plusDays(10))
                .processo(processoDestino)
                .build();
        subprocessoRepo.save(subprocessoDestino);

        Mapa mapa = new Mapa();
        mapa.setSubprocesso(subprocessoDestino);
        mapaRepo.save(mapa);
        subprocessoDestino.setMapa(mapa);

        entityManager.flush();
        entityManager.clear();

        // Recarregar objetos
        subprocessoOrigem = subprocessoRepo.findById(subprocessoOrigem.getCodigo()).orElseThrow();
        subprocessoDestino = subprocessoRepo.findById(subprocessoDestino.getCodigo()).orElseThrow();
    }

    @Nested
    @DisplayName("Testes de importação de atividades")
    class ImportacaoAtividades {
        @Test
        @DisplayName("Deve importar atividades e conhecimentos")
        void deveImportarAtividadesEConhecimentosComSucesso() throws Exception {
            ImportarAtividadesRequest request =
                    new ImportarAtividadesRequest(subprocessoOrigem.getCodigo());

            mockMvc.perform(post("/api/subprocessos/{id}/importar-atividades", subprocessoDestino.getCodigo())
                            .with(user(chefe))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message", is("Atividades importadas.")));

            entityManager.flush();
            entityManager.clear();

            List<Atividade> atividadesDestino =
                    atividadeRepo.findByMapa_Codigo(subprocessoDestino.getMapa().getCodigo());
            assertThat(atividadesDestino).hasSize(2);

            Atividade atividade1Importada =
                    atividadesDestino.stream()
                            .filter(a -> a.getDescricao().equals("Atividade 1"))
                            .findFirst()
                            .orElse(null);
            assertThat(atividade1Importada).isNotNull();
                    List<Conhecimento> conhecimentos1 =
                            conhecimentoRepo.findByAtividade_Codigo(atividade1Importada.getCodigo());
                        assertThat(conhecimentos1).hasSize(1);
            assertThat(conhecimentos1.getFirst().getDescricao()).isEqualTo("Conhecimento 1.1");

            Atividade atividade2Importada =
                    atividadesDestino.stream()
                            .filter(a -> a.getDescricao().equals("Atividade 2"))
                            .findFirst()
                            .orElse(null);
            assertThat(atividade2Importada).isNotNull();
                    List<Conhecimento> conhecimentos2 =
                            conhecimentoRepo.findByAtividade_Codigo(atividade2Importada.getCodigo());
            
            assertThat(conhecimentos2).hasSize(2);
            assertThat(conhecimentos2.stream().map(Conhecimento::getDescricao).toList())
                    .containsExactlyInAnyOrder("Conhecimento 2.1", "Conhecimento 2.2");

            List<Movimentacao> movimentacoes =
                    movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(
                            subprocessoDestino.getCodigo());
            assertThat(movimentacoes).hasSize(1);
            assertThat(movimentacoes.getFirst().getDescricao()).contains("Importação de atividades do subprocesso #" + subprocessoOrigem.getCodigo());
        }

        @Test
        @DisplayName("Deve importar e atualizar status se NAO_INICIADO")
        void deveImportarEAtualizarStatus() throws Exception {
            subprocessoDestino.setSituacaoForcada(SituacaoSubprocesso.NAO_INICIADO);
            subprocessoRepo.save(subprocessoDestino);

            ImportarAtividadesRequest request =
                    new ImportarAtividadesRequest(subprocessoOrigem.getCodigo());

            mockMvc.perform(
                            post(
                                    "/api/subprocessos/{id}/importar-atividades",
                                    subprocessoDestino.getCodigo())
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            Subprocesso atualizado =
                    subprocessoRepo.findById(subprocessoDestino.getCodigo()).orElseThrow();
            assertThat(atualizado.getSituacao())
                    .isEqualTo(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        }

        @Test
        @DisplayName("Não deve importar se subprocesso já estiver disponiblizado")
        void naoDeveImportarSeSubprocessoJaEstiverDisponibilizado() throws Exception {
            subprocessoDestino.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
            subprocessoRepo.save(subprocessoDestino);

            ImportarAtividadesRequest request =
                    new ImportarAtividadesRequest(subprocessoOrigem.getCodigo());

            mockMvc.perform(
                            post(
                                    "/api/subprocessos/{id}/importar-atividades",
                                    subprocessoDestino.getCodigo())
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Deve falhar ao importar de subprocesso inexistente")
        void deveFalharAoImportarDeSubprocessoInexistente() throws Exception {
            ImportarAtividadesRequest request = new ImportarAtividadesRequest(99999L);

            mockMvc.perform(
                            post(
                                    "/api/subprocessos/{id}/importar-atividades",
                                    subprocessoDestino.getCodigo())
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Deve importar apenas atividades não existentes no destino")
        void deveImportarApenasAtividadesNaoExistentesNoDestino() throws Exception {
            Atividade atividadeExistente =
                    Atividade.builder().mapa(subprocessoDestino.getMapa()).descricao("Atividade 2").build();
            atividadeRepo.save(atividadeExistente);

            ImportarAtividadesRequest request =
                    new ImportarAtividadesRequest(subprocessoOrigem.getCodigo());

            mockMvc.perform(
                            post(
                                    "/api/subprocessos/{id}/importar-atividades",
                                    subprocessoDestino.getCodigo())
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            List<Atividade> atividadesDestino =
                    atividadeRepo.findByMapa_Codigo(subprocessoDestino.getMapa().getCodigo());
            assertThat(atividadesDestino).hasSize(2);
            assertThat(atividadesDestino.stream().map(Atividade::getDescricao).toList())
                    .containsExactlyInAnyOrder("Atividade 1", "Atividade 2");
        }
    }
}
