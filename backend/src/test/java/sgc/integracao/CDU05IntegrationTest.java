package sgc.integracao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import sgc.fixture.UnidadeFixture;
import sgc.integracao.mocks.WithMockAdmin;
import sgc.mapa.model.*;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.UnidadeMapa;
import sgc.organizacao.model.UnidadeMapaRepo;
import sgc.processo.dto.CriarProcessoRequest;
import sgc.processo.dto.IniciarProcessoRequest;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.model.Subprocesso;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
        // 1. Criar um processo de revisão para ser iniciado
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

        // 2. Iniciar o processo de revisão
        mockMvc.perform(post(API_PROCESSOS_ID_INICIAR, processoId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(iniciarReq)))
                .andExpect(status().isOk());

        // 3. Buscar o subprocesso criado e verificar a cópia do mapa
        List<Subprocesso> subprocessos = subprocessoRepo.findByProcessoCodigo(processoId);
        assertThat(subprocessos).hasSize(1);
        Subprocesso subprocessoCriado = subprocessos.getFirst();
        Mapa mapaCopiado = subprocessoCriado.getMapa();

        // 3.1. Verificar se o mapa copiado é uma nova instância (código diferente)
        assertThat(mapaCopiado.getCodigo()).isNotNull();
        assertThat(mapaCopiado.getCodigo()).isNotEqualTo(mapaOriginal.getCodigo());

        // 3.2. Verificar se o conteúdo foi copiado
        List<Competencia> competenciasCopiadas = competenciaRepo.findByMapa_Codigo(mapaCopiado.getCodigo());
        assertThat(competenciasCopiadas).hasSize(1);
        assertThat(competenciasCopiadas.getFirst().getDescricao())
                .isEqualTo(competenciaOriginal.getDescricao());

        List<Atividade> atividadesCopiadas = atividadeRepo.findByMapa_Codigo(mapaCopiado.getCodigo());
        assertThat(atividadesCopiadas).hasSize(1);
        assertThat(atividadesCopiadas.getFirst().getDescricao())
                .isEqualTo(atividadeOriginal.getDescricao());

        List<Conhecimento> conhecimentosCopiados = conhecimentoRepo
                .findByAtividade_Codigo(atividadesCopiadas.getFirst().getCodigo());
        assertThat(conhecimentosCopiados).hasSize(1);
        assertThat(conhecimentosCopiados.getFirst().getDescricao())
                .isEqualTo(conhecimentoOriginal.getDescricao());
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
