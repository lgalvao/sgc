package sgc.organizacao;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.springframework.jdbc.core.namedparam.*;
import sgc.organizacao.dto.*;
import sgc.organizacao.model.*;

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

    private void mockarCenarioBase(
            List<Unidade> unidades,
            List<Responsabilidade> responsabilidades,
            List<Usuario> usuarios,
            List<Map<String, Object>> linhasUsuariosDuplicados,
            List<Map<String, Object>> linhasPerfis
    ) {
        when(unidadeRepo.listarTodasComHierarquia()).thenReturn(unidades);
        when(responsabilidadeRepo.listarPorCodigosUnidade(anyList())).thenReturn(responsabilidades);
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
}
