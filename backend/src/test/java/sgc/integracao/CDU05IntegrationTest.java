package sgc.integracao;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.*;
import org.springframework.transaction.annotation.*;
import sgc.fixture.*;
import sgc.integracao.mocks.*;
import sgc.mapa.model.*;
import sgc.organizacao.model.*;
import sgc.processo.dto.*;
import sgc.processo.model.*;
import sgc.subprocesso.model.*;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Tag("integration")
@WithMockAdmin
@Transactional
@DisplayName("CDU-05: Iniciar processo de revisão")
class CDU05IntegrationTest extends BaseIntegrationTest {
    private static final String API_PROCESSOS_ID_INICIAR = "/api/processos/{codigo}/iniciar";

    @Autowired
    private UnidadeMapaRepo unidadeMapaRepo;

    @Autowired
    private CompetenciaRepo competenciaRepo;

    @Autowired
    private ConhecimentoRepo conhecimentoRepo;

    @Autowired
    private MovimentacaoRepo movimentacaoRepo;

    private Unidade unidade;
    private Mapa mapaOriginal;
    private Competencia competenciaOriginal;
    private Atividade atividadeOriginal;
    private Conhecimento conhecimentoOriginal;

    @BeforeEach
    void setUp() {

        // 1. Criar unidade
        unidade = UnidadeFixture.unidadePadrao();
        unidade.setCodigo(null);
        unidade.setSigla("U_REV");
        unidade.setNome("Unidade Revisão");
        unidade = unidadeRepo.save(unidade);

        // Cria um mapa detalhado para ser copiado
        mapaOriginal = new Mapa();
        mapaRepo.save(mapaOriginal);

        competenciaOriginal = Competencia.builder()
                .descricao("Competencia Original")
                .mapa(mapaOriginal)
                .build();
        competenciaRepo.save(competenciaOriginal);

        atividadeOriginal = Atividade.builder()
                .mapa(mapaOriginal)
                .descricao("Atividade Original")
                .build();
        atividadeRepo.save(atividadeOriginal);

        conhecimentoOriginal = Conhecimento.builder()
                .descricao("Conhecimento Original")
                .atividade(atividadeOriginal)
                .build();
        atividadeOriginal.getConhecimentos().add(conhecimentoOriginal); // Mantém consistência bidirecional
        conhecimentoRepo.save(conhecimentoOriginal);

        unidadeMapaRepo.save(UnidadeMapa.builder()
                .unidadeCodigo(unidade.getCodigo())
                .mapaVigente(mapaOriginal)
                .build());
    }

    private CriarProcessoRequest criarCriarProcessoReq(String descricao, List<Long> unidades,
                                                       LocalDateTime dataLimiteEtapa1) {
        return new CriarProcessoRequest(descricao, TipoProcesso.REVISAO, dataLimiteEtapa1, unidades);
    }

    @Test
    void testIniciarProcessoRevisao_sucesso() throws Exception {
        // 1. Criar hierarquia de unidades
        Unidade unidadeSuperior = UnidadeFixture.unidadePadrao();
        unidadeSuperior.setCodigo(null);
        unidadeSuperior.setSigla("U_SUP");
        unidadeSuperior.setNome("Unidade Superior");
        unidadeSuperior = unidadeRepo.save(unidadeSuperior);

        unidade.setUnidadeSuperior(unidadeSuperior);
        unidade = unidadeRepo.save(unidade);

        // Preencher dados originais no mapa para verificar se são limpos
        mapaOriginal.setSugestoes("Sugestões Legadas");
        mapaOriginal.setObservacoesDisponibilizacao("Observações Legadas");
        mapaRepo.save(mapaOriginal);

        // 2. Criar um processo de revisão para ser iniciado
        List<Long> unidades = new ArrayList<>();
        unidades.add(unidade.getCodigo());
        CriarProcessoRequest criarRequestDTO = criarCriarProcessoReq("Processo de Revisão para Iniciar",
                unidades,
                LocalDateTime.now().plusDays(30));

        MvcResult result = mockMvc.perform(post("/api/processos")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(criarRequestDTO)))
                .andExpect(status().isCreated())
                .andReturn();

        Long processoId = objectMapper.readTree(result.getResponse().getContentAsString()).get("codigo")
                .asLong();
        var iniciarReq = new IniciarProcessoRequest(TipoProcesso.REVISAO, unidades);

        // 3. Iniciar o processo de revisão
        mockMvc.perform(post(API_PROCESSOS_ID_INICIAR, processoId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(iniciarReq)))
                .andExpect(status().isOk());

        // 4. Buscar o processo e verificar snapshot da hierarquia (Passo 7)
        Processo processo = processoRepo.findByIdComParticipantes(processoId).orElseThrow();
        assertThat(processo.getParticipantes()).hasSize(2); // Unidade alvo + Unidade Superior
        assertThat(processo.getParticipantes().stream().map(up -> up.getSigla()).toList())
                .containsExactlyInAnyOrder("U_REV", "U_SUP");

        // 5. Buscar o subprocesso criado e verificar a cópia do mapa
        List<Subprocesso> subprocessos = subprocessoRepo.findByProcessoCodigo(processoId);
        assertThat(subprocessos).hasSize(1);
        Subprocesso subprocessoCriado = subprocessos.getFirst();
        Mapa mapaCopiado = subprocessoCriado.getMapa();

        // 5.1. Verificar se o mapa copiado é uma nova instância (código diferente)
        assertThat(mapaCopiado.getCodigo()).isNotNull();
        assertThat(mapaCopiado.getCodigo()).isNotEqualTo(mapaOriginal.getCodigo());

        // 5.2. Verificar se campos foram limpos (Passo 9/10)
        assertThat(mapaCopiado.getSugestoes()).isNull();
        assertThat(mapaCopiado.getObservacoesDisponibilizacao()).isNull();

        // 5.3. Verificar se o conteúdo foi copiado
        List<Competencia> competenciasCopiadas = competenciaRepo.findByMapa_Codigo(mapaCopiado.getCodigo());
        assertThat(competenciasCopiadas).hasSize(1);
        assertThat(competenciasCopiadas.getFirst().getDescricao())
                .isEqualTo(competenciaOriginal.getDescricao());

        // 6. Verificar movimentação (Passo 11)
        List<Movimentacao> movs = movimentacaoRepo.findBySubprocessoCodigo(subprocessoCriado.getCodigo());
        assertThat(movs).hasSize(1);
        assertThat(movs.getFirst().getDescricao()).isEqualTo("Processo iniciado");
    }

    @Test
    void testIniciarProcessoRevisao_unidadeSemMapaVigente_falha() throws Exception {
        // 1. Criar unidade SEM mapa vigente
        Unidade unidadeSemMapa = UnidadeFixture.unidadePadrao();
        unidadeSemMapa.setCodigo(null);
        unidadeSemMapa.setSigla("U_SEM_MAPA");
        unidadeSemMapa.setNome("Unidade Sem Mapa");
        unidadeSemMapa = unidadeRepo.save(unidadeSemMapa);

        List<Long> unidades = new ArrayList<>();
        unidades.add(unidadeSemMapa.getCodigo());
        CriarProcessoRequest criarRequestDTO = criarCriarProcessoReq(
                "Processo de Revisão para Unidade Sem Mapa", unidades,
                LocalDateTime.now().plusDays(30));

        // A validação agora ocorre na criação do processo
        mockMvc.perform(post("/api/processos")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(criarRequestDTO)))
                .andExpect(status().isConflict()); // ErroProcesso mapeado para 409 Conflict
    }

    @Test
    void testIniciarProcessoRevisao_processoNaoEncontrado_falha() throws Exception {
        var iniciarReq = new IniciarProcessoRequest(TipoProcesso.REVISAO, List.of(unidade.getCodigo()));
        mockMvc.perform(post(API_PROCESSOS_ID_INICIAR, 99999L)
                        .with(csrf()) // código que não existe
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(iniciarReq)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testIniciarProcessoRevisao_processoJaIniciado_falha() throws Exception {
        // 1. Criar e iniciar um processo de revisão
        List<Long> unidades = new ArrayList<>();
        unidades.add(unidade.getCodigo());
        CriarProcessoRequest criarRequestDTO = criarCriarProcessoReq(
                "Processo de Revisão para Iniciar Duas Vezes", unidades,
                LocalDateTime.now().plusDays(30));

        MvcResult result = mockMvc.perform(post("/api/processos")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(criarRequestDTO)))
                .andExpect(status().isCreated())
                .andReturn();

        Long processoId = objectMapper.readTree(result.getResponse().getContentAsString()).get("codigo")
                .asLong();
        var iniciarReq = new IniciarProcessoRequest(TipoProcesso.REVISAO, unidades);

        // Inicia o processo a primeira vez
        mockMvc.perform(post(API_PROCESSOS_ID_INICIAR, processoId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(iniciarReq)))
                .andExpect(status().isOk());

        // 2. Tentar iniciar o processo novamente
        mockMvc.perform(post(API_PROCESSOS_ID_INICIAR, processoId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(iniciarReq)))
                .andExpect(status().isUnprocessableContent());
    }
}
