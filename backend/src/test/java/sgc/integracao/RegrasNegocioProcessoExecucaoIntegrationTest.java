package sgc.integracao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.*;
import org.springframework.test.web.servlet.*;
import org.springframework.transaction.annotation.*;
import sgc.fixture.*;
import sgc.integracao.mocks.*;
import sgc.organizacao.model.*;
import sgc.processo.dto.*;
import sgc.processo.model.*;

import java.time.*;
import java.util.*;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Tag("integration")
@Transactional
@DisplayName("Regras de negócio: execução real do ciclo de vida de processos")
@WithMockAdmin
class RegrasNegocioProcessoExecucaoIntegrationTest extends BaseIntegrationTest {
    private static final String API_PROCESSOS = "/api/processos";
    private static final String SQL_INSERIR_RESPONSABILIDADE = """
            INSERT INTO SGC.VW_RESPONSABILIDADE (unidade_codigo, usuario_titulo, usuario_matricula, tipo, data_inicio)
            VALUES (?, ?, ?, ?, ?)
            """;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UsuarioRepo usuarioRepo;

    private Unidade unidadeParticipante;

    @BeforeEach
    void prepararCenario() {
        unidadeParticipante = UnidadeFixture.unidadePadrao();
        unidadeParticipante.setCodigo(null);
        unidadeParticipante.setNome("Unidade Regra");
        unidadeParticipante.setSigla("URG");
        unidadeParticipante = unidadeRepo.saveAndFlush(unidadeParticipante);

        registrarUsuarioSeNecessario("151515151515");
        registrarResponsabilidade(unidadeParticipante.getCodigo(), "151515151515", "15151515");
    }

    @Test
    @DisplayName("RN-04.02: somente ADMIN pode criar processo")
    @WithMockGestor("202020202020")
    void deveImpedirCriacaoDeProcessoPorPerfilNaoAdmin() throws Exception {
        CriarProcessoRequest request = criarProcessoReq("Processo por gestor", List.of(unidadeParticipante.getCodigo()));

        mockMvc.perform(post(API_PROCESSOS)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("RN-04.04: processo iniciado não pode ser editado")
    void deveImpedirEdicaoDeProcessoAposInicio() throws Exception {
        Long codigoProcesso = criarProcessoComoAdmin("Processo para bloqueio de edição");
        iniciarProcessoComoAdmin(codigoProcesso);

        AtualizarProcessoRequest atualizar = new AtualizarProcessoRequest(
                codigoProcesso,
                "Descrição alterada após início",
                TipoProcesso.MAPEAMENTO,
                LocalDateTime.now().plusDays(20),
                List.of(unidadeParticipante.getCodigo())
        );

        mockMvc.perform(post(API_PROCESSOS + "/{codigo}/atualizar", codigoProcesso)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(atualizar)))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.message").value("Apenas processos na situação &#39;CRIADO&#39; podem ser editados."));
    }

    @Test
    @DisplayName("RN-04.05: processo iniciado não pode ser removido")
    void deveImpedirExclusaoDeProcessoAposInicio() throws Exception {
        Long codigoProcesso = criarProcessoComoAdmin("Processo para bloqueio de exclusão");
        iniciarProcessoComoAdmin(codigoProcesso);

        mockMvc.perform(post(API_PROCESSOS + "/{codigo}/excluir", codigoProcesso)
                        .with(csrf()))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.message").value("Apenas processos na situação &#39;CRIADO&#39; podem ser removidos."));
    }

    @Test
    @DisplayName("RN-04.03: processo nasce na situação CRIADO")
    void deveCriarProcessoNaSituacaoCriado() throws Exception {
        CriarProcessoRequest request = criarProcessoReq("Processo com situação inicial", TipoProcesso.MAPEAMENTO, List.of(unidadeParticipante.getCodigo()));

        mockMvc.perform(post(API_PROCESSOS)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.situacao").value("CRIADO"));
    }

    @Test
    @DisplayName("RN-04.15: descrição é obrigatória na criação")
    void deveValidarDescricaoObrigatoriaNaCriacao() throws Exception {
        CriarProcessoRequest request = criarProcessoReq("", TipoProcesso.MAPEAMENTO, List.of(unidadeParticipante.getCodigo()));

        mockMvc.perform(post(API_PROCESSOS)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.subErrors[0].message").value("Preencha a descrição"));
    }

    @Test
    @DisplayName("RN-04.15: ao menos uma unidade participante é obrigatória")
    void deveValidarUnidadesObrigatoriasNaCriacao() throws Exception {
        CriarProcessoRequest request = criarProcessoReq("Processo sem unidades", TipoProcesso.MAPEAMENTO, Collections.emptyList());

        mockMvc.perform(post(API_PROCESSOS)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.subErrors[0].message").value("Pelo menos uma unidade participante deve ser incluída."));
    }

    @Test
    @DisplayName("RN-04.07: unidade não pode iniciar dois processos ativos do mesmo tipo")
    void deveImpedirMesmoParticipanteEmDoisProcessosAtivosDoMesmoTipo() throws Exception {
        Long primeiroProcesso = criarProcessoComoAdmin("Primeiro processo ativo");
        iniciarProcessoComoAdmin(primeiroProcesso);

        Long segundoProcesso = criarProcessoComoAdmin("Segundo processo com unidade repetida");
        IniciarProcessoRequest iniciar = new IniciarProcessoRequest(TipoProcesso.MAPEAMENTO, List.of(unidadeParticipante.getCodigo()));

        mockMvc.perform(post(API_PROCESSOS + "/{codigo}/iniciar", segundoProcesso)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(iniciar)))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.message").value("Unidades já em processo ativo."));
    }

    @Test
    @DisplayName("RN-04.06: revisão exige mapa de competências vigente")
    void deveImpedirCriacaoDeProcessoRevisaoSemMapaVigente() throws Exception {
        CriarProcessoRequest request = criarProcessoReq("Revisão sem mapa vigente", TipoProcesso.REVISAO, List.of(unidadeParticipante.getCodigo()));

                mockMvc.perform(post(API_PROCESSOS)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.message").value("Unidades sem mapa vigente: URG"));
    }

    private CriarProcessoRequest criarProcessoReq(String descricao, List<Long> unidades) {
        return criarProcessoReq(descricao, TipoProcesso.MAPEAMENTO, unidades);
    }

    private CriarProcessoRequest criarProcessoReq(String descricao, TipoProcesso tipo, List<Long> unidades) {
        return new CriarProcessoRequest(
                descricao,
                tipo,
                LocalDateTime.now().plusDays(15),
                unidades
        );
    }

    private Long criarProcessoComoAdmin(String descricao) throws Exception {
        CriarProcessoRequest request = criarProcessoReq(descricao, List.of(unidadeParticipante.getCodigo()));

        MvcResult resultado = mockMvc.perform(post(API_PROCESSOS)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(resultado.getResponse().getContentAsString()).get("codigo").asLong();
    }

    private void iniciarProcessoComoAdmin(Long codigoProcesso) throws Exception {
        IniciarProcessoRequest iniciar = new IniciarProcessoRequest(
                TipoProcesso.MAPEAMENTO,
                List.of(unidadeParticipante.getCodigo())
        );

        mockMvc.perform(post(API_PROCESSOS + "/{codigo}/iniciar", codigoProcesso)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(iniciar)))
                .andExpect(status().isOk());
    }

    private void registrarUsuarioSeNecessario(String tituloUsuario) {
        if (usuarioRepo.findById(tituloUsuario).isPresent()) {
            return;
        }
        usuarioRepo.saveAndFlush(UsuarioFixture.usuarioComTitulo(tituloUsuario));
    }

    private void registrarResponsabilidade(Long codUnidade, String tituloUsuario, String matriculaUsuario) {
        jdbcTemplate.update(
                SQL_INSERIR_RESPONSABILIDADE,
                codUnidade,
                tituloUsuario,
                matriculaUsuario,
                "TITULAR",
                LocalDateTime.now()
        );
    }
}
