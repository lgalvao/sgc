package sgc.sgrh;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.erros.ErroAutenticacao;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.sgrh.dto.PerfilDto;
import sgc.sgrh.dto.PerfilUnidade;
import sgc.sgrh.dto.ResponsavelDto;
import sgc.sgrh.dto.UnidadeDto;
import sgc.sgrh.dto.UsuarioDto;
import sgc.sgrh.model.Perfil;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Testes do Serviço SgrhService")
class SgrhServiceTest {

    // Data from backend/src/test/resources/data.sql
    private static final String TITULO_ADMIN = "111111111111";
    private static final String EMAIL_ADMIN = "admin.teste@tre-pe.jus.br";
    private static final String NOME_ADMIN = "Admin Teste";

    private static final Long COD_UNIT_SEC1 = 2L;
    private static final String NOME_UNIT_SEC1 = "Secretaria de Informática e Comunicações";

    private static final String TITULO_CHEFE_UNIT2 = "777"; // Chefe STIC Teste

    @Autowired
    private SgrhService sgrhService;

    @Nested
    @DisplayName("Consultas de Usuário")
    class ConsultasUsuario {

        @Test
        @DisplayName("Deve buscar usuário por título")
        void deveBuscarUsuarioPorTitulo() {
            // Act
            Optional<UsuarioDto> result = sgrhService.buscarUsuarioPorTitulo(TITULO_ADMIN);

            // Assert
            assertTrue(result.isPresent());
            assertEquals(TITULO_ADMIN, result.get().getTituloEleitoral());
            assertEquals(NOME_ADMIN, result.get().getNome());
        }

        @Test
        @DisplayName("Deve buscar usuário por email")
        void deveBuscarUsuarioPorEmail() {
            // Act
            Optional<UsuarioDto> result = sgrhService.buscarUsuarioPorEmail(EMAIL_ADMIN);

            // Assert
            assertTrue(result.isPresent());
            assertEquals(TITULO_ADMIN, result.get().getTituloEleitoral());
            assertEquals(EMAIL_ADMIN, result.get().getEmail());
        }

        @Test
        @DisplayName("Deve buscar usuários ativos")
        void deveBuscarUsuariosAtivos() {
            // Act
            List<UsuarioDto> result = sgrhService.buscarUsuariosAtivos();

            // Assert
            assertNotNull(result);
            assertFalse(result.isEmpty());
            assertTrue(result.size() >= 2);
        }

        @Test
        @DisplayName("Deve buscar usuários por lista de títulos")
        void deveBuscarUsuariosPorTitulos() {
            // Arrange
            List<String> titulos = List.of(TITULO_CHEFE_UNIT2, TITULO_ADMIN);

            // Act
            Map<String, UsuarioDto> result = sgrhService.buscarUsuariosPorTitulos(titulos);

            // Assert
            assertNotNull(result);
            assertEquals(2, result.size());
            assertTrue(result.containsKey(TITULO_CHEFE_UNIT2));
            assertTrue(result.containsKey(TITULO_ADMIN));
        }
    }

    @Nested
    @DisplayName("Consultas de Unidade")
    class ConsultasUnidade {

        @Test
        @DisplayName("Deve buscar unidade por código")
        void deveBuscarUnidadePorCodigo() {
            // Act
            Optional<UnidadeDto> result = sgrhService.buscarUnidadePorCodigo(COD_UNIT_SEC1);

            // Assert
            assertTrue(result.isPresent());
            assertEquals(COD_UNIT_SEC1, result.get().getCodigo());
            assertEquals(NOME_UNIT_SEC1, result.get().getNome());
        }

        @Test
        @DisplayName("Deve buscar unidades ativas")
        void deveBuscarUnidadesAtivas() {
            // Act
            List<UnidadeDto> result = sgrhService.buscarUnidadesAtivas();

            // Assert
            assertNotNull(result);
            assertFalse(result.isEmpty());
            assertTrue(result.size() > 5);
        }

        @Test
        @DisplayName("Deve buscar subunidades")
        void deveBuscarSubunidades() {
            // Act
            List<UnidadeDto> result = sgrhService.buscarSubunidades(COD_UNIT_SEC1);

            // Assert
            assertNotNull(result);
            assertFalse(result.isEmpty());
            for (UnidadeDto unidade : result) {
                assertEquals(COD_UNIT_SEC1, unidade.getCodigoPai());
            }
        }

        @Test
        @DisplayName("Deve construir árvore hierárquica")
        void deveConstruirArvoreHierarquica() {
            // Act
            List<UnidadeDto> result = sgrhService.construirArvoreHierarquica();

            // Assert
            assertNotNull(result);
            assertFalse(result.isEmpty());
            // Roots in data.sql: 1 (TRE)
            assertTrue(result.stream().anyMatch(u -> u.getCodigo().equals(1L)));

            // Ensure roots have no parent
            for (UnidadeDto unidade : result) {
                if (unidade.getCodigo().equals(1L)) {
                    assertNull(unidade.getCodigoPai());
                }
            }
        }
    }

    @Nested
    @DisplayName("Gestão de Responsáveis")
    class GestaoResponsaveis {

        @Test
        @DisplayName("Deve buscar responsável da unidade")
        void deveBuscarResponsavelUnidade() {
            // Act
            Optional<ResponsavelDto> result = sgrhService.buscarResponsavelUnidade(2L);

            // Assert
            assertTrue(result.isPresent());
            assertEquals(2L, result.get().getUnidadeCodigo());
            assertEquals(TITULO_CHEFE_UNIT2, result.get().getTitularTitulo());
        }

        @Test
        @DisplayName("Deve buscar unidades onde usuário é responsável")
        void deveBuscarUnidadesOndeEhResponsavel() {
            // Act
            List<Long> result = sgrhService.buscarUnidadesOndeEhResponsavel(TITULO_CHEFE_UNIT2);

            // Assert
            assertNotNull(result);
            assertFalse(result.isEmpty());
            assertTrue(result.contains(2L));
        }

        @Test
        @DisplayName("Deve buscar responsáveis de múltiplas unidades")
        void deveBuscarResponsaveisUnidades() {
            // Arrange
            List<Long> unidades = List.of(2L, 9L);

            // Act
            Map<Long, ResponsavelDto> result = sgrhService.buscarResponsaveisUnidades(unidades);

            // Assert
            assertNotNull(result);
            assertTrue(result.containsKey(2L));
            assertTrue(result.containsKey(9L));
            assertEquals(TITULO_CHEFE_UNIT2, result.get(2L).getTitularTitulo());
            assertEquals("333333333333", result.get(9L).getTitularTitulo());
        }
    }

    @Nested
    @DisplayName("Gestão de Perfis")
    class GestaoPerfis {

        @Test
        @DisplayName("Deve buscar perfis do usuário")
        void deveBuscarPerfisUsuario() {
            // Act
            List<PerfilDto> result = sgrhService.buscarPerfisUsuario(TITULO_CHEFE_UNIT2);

            // Assert
            assertNotNull(result);
            assertFalse(result.isEmpty());
            assertTrue(result.stream()
                    .anyMatch(p -> p.getPerfil().equals("CHEFE") && p.getUnidadeCodigo().equals(2L)));
        }

        @Test
        @DisplayName("Deve verificar se usuário tem perfil")
        void deveVerificarUsuarioTemPerfil() {
            // Act & Assert
            assertTrue(sgrhService.usuarioTemPerfil(TITULO_CHEFE_UNIT2, "CHEFE", 2L));
            assertTrue(sgrhService.usuarioTemPerfil(TITULO_ADMIN, "ADMIN", 100L));
        }

        @Test
        @DisplayName("Deve buscar unidades por perfil")
        void deveBuscarUnidadesPorPerfil() {
            // Act
            List<Long> adminUnits = sgrhService.buscarUnidadesPorPerfil(TITULO_ADMIN, "ADMIN");

            // Assert
            assertTrue(adminUnits.contains(100L));
        }
    }

    @Nested
    @DisplayName("Autenticação e Autorização")
    class AutenticacaoAutorizacao {

        @Test
        @DisplayName("Deve autenticar usuário com sucesso")
        void deveAutenticarComSucesso() {
            // Act
            boolean resultado = sgrhService.autenticar(TITULO_ADMIN, "senha");

            // Assert
            assertTrue(resultado);
        }

        @Test
        @DisplayName("Deve impedir autorização sem autenticação prévia")
        void deveImpedirAutorizacaoSemAutenticacao() {
            // Usa um usuário diferente que não foi autenticado nos outros testes
            String usuarioNaoAutenticado = "999999999999";
            assertThrows(ErroAutenticacao.class,
                    () -> sgrhService.autorizar(usuarioNaoAutenticado));
        }

        @Test
        @DisplayName("Deve autorizar e retornar lista de perfis/unidades")
        void deveAutorizarERetornarPerfis() {
            // Arrange
            sgrhService.autenticar(TITULO_CHEFE_UNIT2, "senha");

            // Act
            List<PerfilUnidade> resultado = sgrhService.autorizar(TITULO_CHEFE_UNIT2);

            // Assert
            assertNotNull(resultado);
            assertFalse(resultado.isEmpty());
            assertTrue(resultado.stream()
                    .anyMatch(pu -> pu.getPerfil() == Perfil.CHEFE
                            && pu.getUnidade().getCodigo().equals(2L)));
        }

        @Test
        @DisplayName("Deve lançar exceção ao autorizar usuário não encontrado")
        void deveLancarExcecaoAoAutorizarUsuarioNaoEncontrado() {
            // Arrange
            sgrhService.autenticar("TITULO_INEXISTENTE_XYZ", "senha");

            // Act & Assert
            assertThrows(ErroEntidadeNaoEncontrada.class,
                    () -> sgrhService.autorizar("TITULO_INEXISTENTE_XYZ"));
        }

        @Test
        @DisplayName("Deve entrar no sistema com perfil selecionado")
        void deveEntrarComSucesso() {
            // Arrange
            sgrhService.autenticar(TITULO_CHEFE_UNIT2, "senha");
            List<PerfilUnidade> perfis = sgrhService.autorizar(TITULO_CHEFE_UNIT2);
            PerfilUnidade perfilUnidade = perfis.getFirst();

            // Act & Assert
            assertDoesNotThrow(() -> sgrhService.entrar(TITULO_CHEFE_UNIT2, perfilUnidade));
        }
    }
}
