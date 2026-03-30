package sgc.organizacao;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.springframework.boot.*;
import sgc.comum.erros.*;
import sgc.organizacao.model.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static sgc.organizacao.model.Perfil.*;
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
    private UsuarioPerfilRepo usuarioPerfilRepo;

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
                List.of(
                        criarPerfil("RESP_INT", 10L, GESTOR),
                        criarPerfil("RESP_OPE", 20L, CHEFE)
                )
        );

        assertThatCode(() -> validador.run(new DefaultApplicationArguments()))
                .doesNotThrowAnyException();
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
                List.of(
                        criarPerfil("TITULO_INT", 10L, GESTOR),
                        criarPerfil("MESMA_PESSOA", 20L, CHEFE)
                )
        );

        assertThatCode(() -> validador.run(new DefaultApplicationArguments()))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("deve falhar quando unidade nao possui responsavel")
    void deveFalharSemResponsavel() {
        Unidade operacional = criarUnidade(20L, "OPE", OPERACIONAL, "TITULO_OPE");

        mockarCenarioBase(
                List.of(operacional),
                List.of(),
                List.of(criarUsuario("TITULO_OPE")),
                List.of()
        );

        assertThatThrownBy(() -> validador.run(new DefaultApplicationArguments()))
                .isInstanceOf(ErroConfiguracao.class)
                .hasMessageContaining("1 violacao encontrada");
    }

    @Test
    @DisplayName("deve falhar quando unidade nao possui titular")
    void deveFalharSemTitular() {
        Unidade operacional = criarUnidade(20L, "OPE", OPERACIONAL, null);

        mockarCenarioBase(
                List.of(operacional),
                List.of(criarResponsabilidade(20L, "RESP_OPE")),
                List.of(criarUsuario("RESP_OPE")),
                List.of(criarPerfil("RESP_OPE", 20L, CHEFE))
        );

        assertThatThrownBy(() -> validador.run(new DefaultApplicationArguments()))
                .isInstanceOf(ErroConfiguracao.class)
                .hasMessageContaining("1 violacao encontrada");
    }

    @Test
    @DisplayName("deve falhar quando unidade intermediaria nao possui filhas")
    void deveFalharIntermediariaSemFilhas() {
        Unidade intermediaria = criarUnidade(10L, "INT", INTERMEDIARIA, "TITULO_INT");

        mockarCenarioBase(
                List.of(intermediaria),
                List.of(criarResponsabilidade(10L, "RESP_INT")),
                List.of(criarUsuario("TITULO_INT"), criarUsuario("RESP_INT")),
                List.of(criarPerfil("RESP_INT", 10L, GESTOR))
        );

        assertThatThrownBy(() -> validador.run(new DefaultApplicationArguments()))
                .isInstanceOf(ErroConfiguracao.class)
                .hasMessageContaining("1 violacao encontrada");
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
                List.of(criarPerfil("RESP_OPE", 20L, CHEFE))
        );

        assertThatThrownBy(() -> validador.run(new DefaultApplicationArguments()))
                .isInstanceOf(ErroConfiguracao.class)
                .hasMessageContaining("1 violacao encontrada");
    }

    @Test
    @DisplayName("deve acumular multiplas violacoes")
    void deveAcumularMultiplasViolacoes() {
        Unidade intermediaria = criarUnidade(10L, "INT", INTERMEDIARIA, null);

        mockarCenarioBase(
                List.of(intermediaria),
                List.of(criarResponsabilidade(10L, "RESP_INT")),
                List.of(criarUsuario("RESP_INT")),
                List.of()
        );

        assertThatThrownBy(() -> validador.run(new DefaultApplicationArguments()))
                .isInstanceOf(ErroConfiguracao.class)
                .hasMessageContaining("3 violacoes encontradas");
    }

    private void mockarCenarioBase(
            List<Unidade> unidades,
            List<Responsabilidade> responsabilidades,
            List<Usuario> usuarios,
            List<UsuarioPerfil> perfis
    ) {
        when(unidadeRepo.findAllWithHierarquia()).thenReturn(unidades);
        when(responsabilidadeRepo.findByUnidadeCodigoIn(anyList())).thenReturn(responsabilidades);
        when(usuarioRepo.findAllById(anyList())).thenReturn(usuarios);
        when(usuarioPerfilRepo.findByUnidadeCodigoIn(anyList())).thenReturn(perfis);
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

    private UsuarioPerfil criarPerfil(String usuarioTitulo, Long unidadeCodigo, Perfil perfil) {
        return UsuarioPerfil.builder()
                .usuarioTitulo(usuarioTitulo)
                .unidadeCodigo(unidadeCodigo)
                .perfil(perfil)
                .build();
    }
}
