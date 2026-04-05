package sgc.organizacao;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.test.util.ReflectionTestUtils;
import sgc.organizacao.dto.*;
import sgc.organizacao.model.*;

import java.lang.reflect.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static sgc.organizacao.model.SituacaoUnidade.*;
import static sgc.organizacao.model.TipoUnidade.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ValidadorDadosOrganizacionais")
class ValidadorDadosOrganizacionaisTest {

    @Mock
    private UnidadeRepo unidadeRepo;

    @Mock
    private UsuarioRepo usuarioRepo;

    @Mock
    private ResponsabilidadeRepo responsabilidadeRepo;

    @Mock
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @InjectMocks
    private ValidadorDadosOrganizacionais validador;

    @Test
    @DisplayName("deve validar com sucesso quando as invariantes organizacionais estao satisfeitas")
    void deveValidarComSucesso() {
        Unidade intermediaria = criarUnidade(10L, "INT", INTERMEDIARIA, "TITULO_INT");
        Unidade operacional = criarUnidade(20L, "OPE", OPERACIONAL, "TITULO_OPE");
        operacional.setUnidadeSuperior(intermediaria);

        mockarCenarioBase(
                List.of(intermediaria, operacional),
                List.of(
                        criarResponsabilidade(10L, "RESP_INT"),
                        criarResponsabilidade(20L, "RESP_OPE")
                ),
                List.of(
                        criarUsuario("TITULO_INT"),
                        criarUsuario("TITULO_OPE"),
                        criarUsuario("RESP_INT"),
                        criarUsuario("RESP_OPE")
                ),
                List.of(),
                List.of(
                        linhaPerfil("RESP_INT", "GESTOR", 10L),
                        linhaPerfil("RESP_OPE", "CHEFE", 20L)
                )
        );

        DiagnosticoOrganizacionalDto diagnostico = validador.diagnosticar();

        assertThat(diagnostico.possuiViolacoes()).isFalse();
        assertThat(diagnostico.grupos()).isEmpty();
    }

    @Test
    @DisplayName("deve aceitar quando responsavel e titular sao a mesma pessoa")
    void deveAceitarResponsavelIgualAoTitular() {
        Unidade intermediaria = criarUnidade(10L, "INT", INTERMEDIARIA, "TITULO_INT");
        Unidade operacional = criarUnidade(20L, "OPE", OPERACIONAL, "MESMA_PESSOA");
        operacional.setUnidadeSuperior(intermediaria);

        mockarCenarioBase(
                List.of(intermediaria, operacional),
                List.of(
                        criarResponsabilidade(10L, "TITULO_INT"),
                        criarResponsabilidade(20L, "MESMA_PESSOA")
                ),
                List.of(
                        criarUsuario("TITULO_INT"),
                        criarUsuario("MESMA_PESSOA")
                ),
                List.of(),
                List.of(
                        linhaPerfil("TITULO_INT", "GESTOR", 10L),
                        linhaPerfil("MESMA_PESSOA", "CHEFE", 20L)
                )
        );

        DiagnosticoOrganizacionalDto diagnostico = validador.diagnosticar();

        assertThat(diagnostico.possuiViolacoes()).isFalse();
    }

    @Test
    @DisplayName("deve falhar quando unidade nao possui responsavel")
    void deveFalharSemResponsavel() {
        Unidade operacional = criarUnidade(20L, "OPE", OPERACIONAL, "TITULO_OPE");

        mockarCenarioBase(
                List.of(operacional),
                List.of(),
                List.of(criarUsuario("TITULO_OPE")),
                List.of(),
                List.of()
        );

        DiagnosticoOrganizacionalDto diagnostico = validador.diagnosticar();

        assertThat(diagnostico.possuiViolacoes()).isTrue();
        assertThat(diagnostico.quantidadeTiposViolacao()).isEqualTo(1);
        assertThat(diagnostico.resumo())
                .isEqualTo("Há unidades atualmente sem responsável efetivo: OPE. Essas unidades só poderão participar de processos do SGC quando a responsabilidade for definida, externamente ou via atribuição temporária no próprio sistema.");
        assertThat(diagnostico.grupos())
                .extracting(GrupoViolacaoOrganizacionalDto::tipo)
                .containsExactly("Unidade participante sem responsavel efetivo");
    }

    @Test
    @DisplayName("deve aceitar unidade sem titular quando houver responsavel efetivo")
    void deveAceitarSemTitularQuandoHouverResponsavelEfetivo() {
        Unidade operacional = criarUnidade(20L, "OPE", OPERACIONAL, null);

        mockarCenarioBase(
                List.of(operacional),
                List.of(criarResponsabilidade(20L, "RESP_OPE")),
                List.of(criarUsuario("RESP_OPE")),
                List.of(),
                List.of(linhaPerfil("RESP_OPE", "CHEFE", 20L))
        );

        DiagnosticoOrganizacionalDto diagnostico = validador.diagnosticar();

        assertThat(diagnostico.possuiViolacoes()).isFalse();
    }

    @Test
    @DisplayName("deve falhar quando unidade intermediaria nao possui filhas")
    void deveFalharIntermediariaSemFilhas() {
        Unidade intermediaria = criarUnidade(10L, "INT", INTERMEDIARIA, "TITULO_INT");

        mockarCenarioBase(
                List.of(intermediaria),
                List.of(criarResponsabilidade(10L, "RESP_INT")),
                List.of(criarUsuario("TITULO_INT"), criarUsuario("RESP_INT")),
                List.of(),
                List.of(linhaPerfil("RESP_INT", "GESTOR", 10L))
        );

        DiagnosticoOrganizacionalDto diagnostico = validador.diagnosticar();

        assertThat(diagnostico.possuiViolacoes()).isTrue();
        assertThat(diagnostico.quantidadeTiposViolacao()).isEqualTo(1);
        assertThat(diagnostico.grupos().getFirst().tipo()).isEqualTo("Unidade intermediaria sem filhas ativas participantes");
    }

    @Test
    @DisplayName("deve falhar quando unidade intermediaria nao possui gestor")
    void deveFalharIntermediariaSemGestor() {
        Unidade intermediaria = criarUnidade(10L, "INT", INTERMEDIARIA, "TITULO_INT");
        Unidade operacional = criarUnidade(20L, "OPE", OPERACIONAL, "TITULO_OPE");
        operacional.setUnidadeSuperior(intermediaria);

        mockarCenarioBase(
                List.of(intermediaria, operacional),
                List.of(
                        criarResponsabilidade(10L, "RESP_INT"),
                        criarResponsabilidade(20L, "RESP_OPE")
                ),
                List.of(
                        criarUsuario("TITULO_INT"),
                        criarUsuario("TITULO_OPE"),
                        criarUsuario("RESP_INT"),
                        criarUsuario("RESP_OPE")
                ),
                List.of(),
                List.of(linhaPerfil("RESP_OPE", "CHEFE", 20L))
        );

        DiagnosticoOrganizacionalDto diagnostico = validador.diagnosticar();

        assertThat(diagnostico.possuiViolacoes()).isTrue();
        assertThat(diagnostico.quantidadeTiposViolacao()).isEqualTo(1);
        assertThat(diagnostico.grupos().getFirst().tipo()).isEqualTo("Unidade intermediaria sem perfil GESTOR");
    }

    @Test
    @DisplayName("deve acumular multiplas violacoes")
    void deveAcumularMultiplasViolacoes() {
        Unidade intermediaria = criarUnidade(10L, "INT", INTERMEDIARIA, "DUMMY_TITULAR");

        mockarCenarioBase(
                List.of(intermediaria),
                List.of(criarResponsabilidade(10L, "RESP_INT")),
                List.of(criarUsuario("RESP_INT"), criarUsuario("DUMMY_TITULAR")),
                List.of(),
                List.of()
        );

        DiagnosticoOrganizacionalDto diagnostico = validador.diagnosticar();

        assertThat(diagnostico.possuiViolacoes()).isTrue();
        assertThat(diagnostico.quantidadeTiposViolacao()).isEqualTo(2);
    }

    @Test
    @DisplayName("deve falhar quando houver titulo duplicado na view de usuarios")
    void deveFalharQuandoHouverTituloDuplicadoNaViewUsuarios() {
        Unidade operacional = criarUnidade(20L, "OPE", OPERACIONAL, "TITULO_OPE");

        mockarCenarioBase(
                List.of(operacional),
                List.of(criarResponsabilidade(20L, "RESP_OPE")),
                List.of(
                        criarUsuario("TITULO_OPE"),
                        criarUsuario("RESP_OPE"),
                        criarUsuario("RESP_OPE")
                ),
                List.of(Map.of("TITULO", "RESP_OPE", "QUANTIDADE", 2L)),
                List.of(linhaPerfil("RESP_OPE", "CHEFE", 20L))
        );

        DiagnosticoOrganizacionalDto diagnostico = validador.diagnosticar();

        assertThat(diagnostico.possuiViolacoes()).isTrue();
        assertThat(diagnostico.quantidadeTiposViolacao()).isEqualTo(1);
        assertThat(diagnostico.grupos().getFirst().tipo()).isEqualTo("VW_USUARIO com titulo duplicado");
    }

    @Test
    @DisplayName("deve ignorar perfil nulo derivado de unidade sem responsavel efetivo")
    void deveIgnorarPerfilNuloDerivadoDeUnidadeSemResponsavelEfetivo() {
        Unidade operacional = criarUnidade(20L, "OPE", OPERACIONAL, "DUMMY_TITULAR");

        mockarCenarioBase(
                List.of(operacional),
                List.of(),
                List.of(
                        criarUsuario("RESP_OPE"),
                        criarUsuario("DUMMY_TITULAR")
                ),
                List.of(),
                List.of(
                        linhaPerfil(null, "CHEFE", 20L)
                )
        );

        DiagnosticoOrganizacionalDto diagnostico = validador.diagnosticar();

        assertThat(diagnostico.possuiViolacoes()).isTrue();
        assertThat(diagnostico.quantidadeTiposViolacao()).isEqualTo(1);
        assertThat(diagnostico.grupos().getFirst().tipo()).isEqualTo("Unidade participante sem responsavel efetivo");
    }

    @Test
    @DisplayName("deve reportar inconsistencias de perfis invalidos e chave duplicada na view")
    void deveReportarInconsistenciasPerfisInvalidosEChaveDuplicada() {
        Unidade operacional = criarUnidade(20L, "OPE", OPERACIONAL, "TITULO_OPE");

        mockarCenarioBase(
                List.of(operacional),
                List.of(criarResponsabilidade(20L, "RESP_OPE")),
                List.of(criarUsuario("TITULO_OPE"), criarUsuario("RESP_OPE")),
                List.of(),
                List.of(
                        linhaPerfil(null, "CHEFE", 20L),
                        linhaPerfil("RESP_OPE", null, 20L),
                        linhaPerfil("RESP_OPE", "CHEFE", null),
                        linhaPerfil("RESP_OPE", "PERFIL_INVALIDO", 20L),
                        linhaPerfil("RESP_OPE", "CHEFE", 20L),
                        linhaPerfil("RESP_OPE", "CHEFE", 20L)
                )
        );

        DiagnosticoOrganizacionalDto diagnostico = validador.diagnosticar();

        assertThat(diagnostico.possuiViolacoes()).isTrue();
        assertThat(diagnostico.grupos())
                .extracting(GrupoViolacaoOrganizacionalDto::tipo)
                .contains(
                        "VW_USUARIO_PERFIL_UNIDADE com usuario_titulo nulo",
                        "VW_USUARIO_PERFIL_UNIDADE com perfil nulo",
                        "VW_USUARIO_PERFIL_UNIDADE com unidade_codigo nulo",
                        "VW_USUARIO_PERFIL_UNIDADE com perfil invalido",
                        "VW_USUARIO_PERFIL_UNIDADE com chave duplicada"
                );
    }

    @Test
    @DisplayName("deve considerar responsabilidade com usuario em branco como ausente")
    void deveConsiderarResponsabilidadeComUsuarioEmBrancoComoAusente() {
        Unidade operacional = criarUnidade(20L, "OPE", OPERACIONAL, "TITULO_OPE");

        mockarCenarioBase(
                List.of(operacional),
                List.of(criarResponsabilidade(20L, "   ")),
                List.of(criarUsuario("TITULO_OPE")),
                List.of(),
                List.of()
        );

        DiagnosticoOrganizacionalDto diagnostico = validador.diagnosticar();

        assertThat(diagnostico.possuiViolacoes()).isTrue();
        assertThat(diagnostico.grupos())
                .extracting(GrupoViolacaoOrganizacionalDto::tipo)
                .contains("Unidade participante sem responsavel efetivo");
    }

    @Test
    @DisplayName("deve reportar quando responsavel de unidade intermediaria nao possui perfil gestor correspondente")
    void deveReportarQuandoResponsavelIntermediarioNaoTemPerfilGestorCorrespondente() {
        Unidade intermediaria = criarUnidade(10L, "INT", INTERMEDIARIA, "TITULO_INT");
        Unidade operacional = criarUnidade(20L, "OPE", OPERACIONAL, "TITULO_OPE");
        operacional.setUnidadeSuperior(intermediaria);

        mockarCenarioBase(
                List.of(intermediaria, operacional),
                List.of(
                        criarResponsabilidade(10L, "RESP_INT"),
                        criarResponsabilidade(20L, "RESP_OPE")
                ),
                List.of(
                        criarUsuario("TITULO_INT"),
                        criarUsuario("TITULO_OPE"),
                        criarUsuario("RESP_INT"),
                        criarUsuario("RESP_OPE"),
                        criarUsuario("OUTRO_GESTOR")
                ),
                List.of(),
                List.of(
                        linhaPerfil("OUTRO_GESTOR", "GESTOR", 10L),
                        linhaPerfil("RESP_OPE", "CHEFE", 20L)
                )
        );

        DiagnosticoOrganizacionalDto diagnostico = validador.diagnosticar();

        assertThat(diagnostico.possuiViolacoes()).isTrue();
        assertThat(diagnostico.grupos())
                .extracting(GrupoViolacaoOrganizacionalDto::tipo)
                .contains("Responsavel de unidade intermediaria sem perfil GESTOR correspondente");
    }

    @Test
    @DisplayName("deve retornar sem violacoes quando nao houver unidades participantes ativas")
    void deveRetornarSemViolacoesSemUnidadesParticipantesAtivas() {
        Unidade raiz = criarUnidade(1L, "RAIZ", RAIZ, null);
        Unidade operacionalInativa = criarUnidade(2L, "INAT", OPERACIONAL, "TITULO_INAT");
        operacionalInativa.setSituacao(INATIVA);

        when(unidadeRepo.listarEstruturasAtivas()).thenReturn(List.of(
                toLeitura(raiz),
                toLeitura(operacionalInativa)
        ));

        DiagnosticoOrganizacionalDto diagnostico = validador.diagnosticar();

        assertThat(diagnostico.possuiViolacoes()).isFalse();
        assertThat(diagnostico.grupos()).isEmpty();
    }

    @Test
    @DisplayName("deve ler string de mapa com chaves em diferentes cases")
    void deveLerStringComDiferentesCases() {
        Unidade operacional = criarUnidade(20L, "OPE", OPERACIONAL, "TITULO_OPE");

        Map<String, Object> linhaMinusc = new HashMap<>();
        linhaMinusc.put("titulo", "TITULO_1");
        linhaMinusc.put("quantidade", 2L);

        Map<String, Object> linhaMajusc = new HashMap<>();
        linhaMajusc.put("TITULO", "TITULO_2");
        linhaMajusc.put("QUANTIDADE", 2L);

        Map<String, Object> linhaNull = new HashMap<>();
        linhaNull.put("titulo", null);

        mockarCenarioBase(
                List.of(operacional),
                List.of(criarResponsabilidade(20L, "RESP_OPE")),
                List.of(criarUsuario("TITULO_OPE"), criarUsuario("RESP_OPE")),
                List.of(linhaMinusc, linhaMajusc, linhaNull),
                List.of(linhaPerfil("RESP_OPE", "GESTOR", 20L))
        );

        DiagnosticoOrganizacionalDto diagnostico = validador.diagnosticar();

        assertThat(diagnostico.grupos().stream()
                .filter(g -> g.tipo().equals("VW_USUARIO com titulo duplicado"))
                .flatMap(g -> g.ocorrencias().stream())
                .toList())
                .containsExactlyInAnyOrder("titulo=TITULO_1", "titulo=TITULO_2");
    }

    @Test
    @DisplayName("deve ler unidade_codigo em diferentes formatos e cases")
    void deveLerUnidadeCodigoEmDiferentesFormatosECases() {
        Unidade operacional = criarUnidade(20L, "OPE", OPERACIONAL, "TITULO_OPE");

        Map<String, Object> linha1 = new HashMap<>();
        linha1.put("usuario_titulo", "U1");
        linha1.put("perfil", "GESTOR");
        linha1.put("unidade_codigo", "20");

        Map<String, Object> linha2 = new HashMap<>();
        linha2.put("usuario_titulo", "U2");
        linha2.put("perfil", "GESTOR");
        linha2.put("UNIDADE_CODIGO", 20L);

        Map<String, Object> linha3 = new HashMap<>();
        linha3.put("usuario_titulo", "U3");
        linha3.put("perfil", "GESTOR");
        linha3.put("unidade_codigo", null);

        mockarCenarioBase(
                List.of(operacional),
                List.of(criarResponsabilidade(20L, "RESP_OPE")),
                List.of(criarUsuario("TITULO_OPE"), criarUsuario("RESP_OPE"), criarUsuario("U1"), criarUsuario("U2")),
                List.of(),
                List.of(linha1, linha2, linha3)
        );

        DiagnosticoOrganizacionalDto diagnostico = validador.diagnosticar();

        assertThat(diagnostico.grupos())
                .extracting(GrupoViolacaoOrganizacionalDto::tipo)
                .contains("VW_USUARIO_PERFIL_UNIDADE com unidade_codigo nulo");
    }

    @Test
    @DisplayName("deve lidar com casos de borda na extracao de sigla")
    void deveLidarComCasosBordaExtracaoSigla() {
        String siglaNoFim = ReflectionTestUtils.invokeMethod(validador, "extrairSigla", "sigla=INT_FIM");
        assertThat(siglaNoFim).isEqualTo("INT_FIM");

        String siglaNoMeio = ReflectionTestUtils.invokeMethod(validador, "extrairSigla", "sigla=INT_MEIO, tipo=OPERACIONAL");
        assertThat(siglaNoMeio).isEqualTo("INT_MEIO");

        String siglaEmBranco = ReflectionTestUtils.invokeMethod(validador, "extrairSigla", "sigla=  , tipo=OPERACIONAL");
        assertThat(siglaEmBranco).isNull();

        String siglaNaoEncontrada = ReflectionTestUtils.invokeMethod(validador, "extrairSigla", "sem_sigla_aqui");
        assertThat(siglaNaoEncontrada).isNull();
    }

    @Test
    @DisplayName("deve verificar se string esta vazia ou nula")
    void deveVerificarSeStringEstaVazia() throws Exception {
        Method metodo = ValidadorDadosOrganizacionais.class.getDeclaredMethod("estaVazio", String.class);
        metodo.setAccessible(true);

        assertThat((Boolean) metodo.invoke(validador, new Object[]{null})).isTrue();
        assertThat((Boolean) metodo.invoke(validador, "   ")).isTrue();
        assertThat((Boolean) metodo.invoke(validador, "valor")).isFalse();
    }

    @Test
    @DisplayName("deve usar pluralizacao correta no resumo")
    void deveUsarPluralizacaoCorretaNoResumo() {
        Unidade operacional = criarUnidade(20L, "OPE", OPERACIONAL, "TIT_OPE");

        // 2 tipos, 2 ocorrencias: sem responsavel e titular ausente
        mockarCenarioBase(
                List.of(operacional),
                List.of(),
                List.of(),
                List.of(),
                List.of()
        );

        DiagnosticoOrganizacionalDto diagnostico = validador.diagnosticar();
        assertThat(diagnostico.resumo()).contains("2 tipos de inconsistencias, totalizando 2 ocorrencias");
    }

    @Test
    @DisplayName("deve lidar com lista de unidades vazia ao carregar responsabilidades")
    void deveLidarComListaUnidadesVazia() {
        when(unidadeRepo.listarEstruturasAtivas()).thenReturn(List.of());

        DiagnosticoOrganizacionalDto diagnostico = validador.diagnosticar();

        assertThat(diagnostico.possuiViolacoes()).isFalse();
        verify(responsabilidadeRepo, never()).listarLeiturasPorCodigosUnidade(anyList());
    }

    @Test
    @DisplayName("deve processar linha de perfil com valores vazios ou nulos")
    void deveProcessarLinhaPerfilComValoresVazios() {
        Unidade operacional = criarUnidade(20L, "OPE", OPERACIONAL, "TIT_OPE");

        Map<String, Object> linhaVazia = new HashMap<>();
        linhaVazia.put("usuario_titulo", "  ");
        linhaVazia.put("perfil", "GESTOR");
        linhaVazia.put("unidade_codigo", 20L);

        Map<String, Object> perfilVazio = new HashMap<>();
        perfilVazio.put("usuario_titulo", "U1");
        perfilVazio.put("perfil", "");
        perfilVazio.put("unidade_codigo", 20L);

        mockarCenarioBase(
                List.of(operacional),
                List.of(criarResponsabilidade(20L, "RESP_OPE")),
                List.of(criarUsuario("TIT_OPE"), criarUsuario("RESP_OPE")),
                List.of(),
                List.of(linhaVazia, perfilVazio)
        );

        DiagnosticoOrganizacionalDto diagnostico = validador.diagnosticar();

        assertThat(diagnostico.grupos())
                .extracting(GrupoViolacaoOrganizacionalDto::tipo)
                .contains(
                        "VW_USUARIO_PERFIL_UNIDADE com usuario_titulo nulo",
                        "VW_USUARIO_PERFIL_UNIDADE com perfil nulo"
                );
    }

    private void mockarCenarioBase(
            List<Unidade> unidades,
            List<Responsabilidade> responsabilidades,
            List<Usuario> usuarios,
            List<Map<String, Object>> linhasUsuariosDuplicados,
            List<Map<String, Object>> linhasPerfis
    ) {
        when(unidadeRepo.listarEstruturasAtivas()).thenReturn(unidades.stream().map(this::toLeitura).toList());
        when(responsabilidadeRepo.listarLeiturasPorCodigosUnidade(anyList())).thenReturn(
                responsabilidades.stream()
                        .map(r -> new ResponsabilidadeLeitura(r.getUnidadeCodigo(), r.getUsuarioTitulo()))
                        .toList()
        );
        when(usuarioRepo.findAllById(anyList())).thenReturn(usuarios);
        when(namedParameterJdbcTemplate.queryForList(contains("FROM sgc.vw_usuario"), ArgumentMatchers.<Map<String, ?>>any()))
                .thenReturn(linhasUsuariosDuplicados);
        when(namedParameterJdbcTemplate.queryForList(contains("FROM sgc.vw_usuario_perfil_unidade"), ArgumentMatchers.<Map<String, ?>>any()))
                .thenReturn(linhasPerfis);
    }

    private Unidade criarUnidade(Long codigo, String sigla, TipoUnidade tipo, String tituloTitular) {
        Unidade unidade = new Unidade();
        unidade.setCodigo(codigo);
        unidade.setSigla(sigla);
        unidade.setTipo(tipo);
        unidade.setSituacao(ATIVA);
        unidade.setTituloTitular(tituloTitular);
        unidade.setMatriculaTitular("MAT" + codigo);
        return unidade;
    }

    private Responsabilidade criarResponsabilidade(Long unidadeCodigo, String usuarioTitulo) {
        Responsabilidade responsabilidade = new Responsabilidade();
        responsabilidade.setUnidadeCodigo(unidadeCodigo);
        responsabilidade.setUsuarioTitulo(usuarioTitulo);
        responsabilidade.setUsuarioMatricula("MAT_" + unidadeCodigo);
        return responsabilidade;
    }

    private Usuario criarUsuario(String titulo) {
        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral(titulo);
        usuario.setNome("Usuario " + titulo);
        usuario.setEmail(titulo.toLowerCase(Locale.ROOT) + "@teste.com");
        return usuario;
    }

    private Map<String, Object> linhaPerfil(String usuarioTitulo, String perfil, Long unidadeCodigo) {
        Map<String, Object> linha = new LinkedHashMap<>();
        linha.put("USUARIO_TITULO", usuarioTitulo);
        linha.put("PERFIL", perfil);
        linha.put("UNIDADE_CODIGO", unidadeCodigo);
        return linha;
    }

    private UnidadeHierarquiaLeitura toLeitura(Unidade unidade) {
        return new UnidadeHierarquiaLeitura(
                unidade.getCodigo(),
                unidade.getNome(),
                unidade.getSigla(),
                unidade.getTituloTitular(),
                unidade.getTipo(),
                unidade.getSituacao(),
                unidade.getUnidadeSuperior() != null ? unidade.getUnidadeSuperior().getCodigo() : null
        );
    }
}
