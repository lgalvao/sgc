package sgc.organizacao;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import sgc.organizacao.dto.AdministradorDto;
import sgc.organizacao.dto.PerfilDto;
import sgc.organizacao.dto.ResponsavelDto;
import sgc.organizacao.dto.UnidadeDto;
import sgc.organizacao.dto.UsuarioDto;
import sgc.organizacao.model.Perfil;

@Tag("integration")
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Testes do Serviço UsuarioFacade")
class UsuarioServiceTest {

    // Data from backend/src/test/resources/data.sql
    private static final String TITULO_ADMIN = "111111111111";
    private static final String EMAIL_ADMIN = "admin.teste@tre-pe.jus.br";
    private static final String NOME_ADMIN = "Admin Teste";

    private static final Long COD_UNIT_SEC1 = 2L;
    private static final String NOME_UNIT_SEC1 = "Secretaria de Informática e Comunicações";

    private static final String TITULO_CHEFE_UNIT2 = "777"; // Chefe STIC Teste

    @Autowired
    private UsuarioFacade usuarioService;

    @Autowired
    private UnidadeFacade unidadeService;

    @Nested
    @DisplayName("Consultas de Usuário")
    @SuppressWarnings("unused")
    class ConsultasUsuario {
        @Test
        @DisplayName("Deve buscar usuário por título")
        void deveBuscarUsuarioPorTitulo() {
            // Act
            Optional<UsuarioDto> result = usuarioService.buscarUsuarioPorTitulo(TITULO_ADMIN);

            // Assert
            assertTrue(result.isPresent());
            assertEquals(TITULO_ADMIN, result.get().tituloEleitoral());
            assertEquals(NOME_ADMIN, result.get().nome());
        }

        @Test
        @DisplayName("Deve buscar entidade usuário por login garantindo inicialização")
        void deveBuscarEntidadeUsuarioPorLogin() {
            // Act
            var usuario = usuarioService.buscarPorLogin(TITULO_ADMIN);

            // Assert
            assertNotNull(usuario);
            assertEquals(TITULO_ADMIN, usuario.getTituloEleitoral());

            // Verifica se a coleção foi inicializada (não deve lançar
            // LazyInitializationException)
            assertDoesNotThrow(() -> {
                usuario.getAtribuicoesTemporarias().size();
            });
        }

        @Test
        @DisplayName("Deve buscar usuário por email")
        void deveBuscarUsuarioPorEmail() {
            // Act
            Optional<UsuarioDto> result = usuarioService.buscarUsuarioPorEmail(EMAIL_ADMIN);

            // Assert
            assertTrue(result.isPresent());
            assertEquals(TITULO_ADMIN, result.get().tituloEleitoral());
            assertEquals(EMAIL_ADMIN, result.get().email());
        }

        @Test
        @DisplayName("Deve buscar usuários ativos")
        void deveBuscarUsuariosAtivos() {
            // Act
            List<UsuarioDto> result = usuarioService.buscarUsuariosAtivos();

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
            Map<String, UsuarioDto> result = usuarioService.buscarUsuariosPorTitulos(titulos);

            // Assert
            assertNotNull(result);
            assertEquals(2, result.size());
            assertTrue(result.containsKey(TITULO_CHEFE_UNIT2));
            assertTrue(result.containsKey(TITULO_ADMIN));
        }
    }

    @Nested
    @DisplayName("Consultas de Unidade")
    @SuppressWarnings("unused")
    class ConsultasUnidade {
        @Test
        @DisplayName("Deve buscar unidade por código")
        void deveBuscarUnidadePorCodigo() {
            // Act
            UnidadeDto result = unidadeService.buscarPorCodigo(COD_UNIT_SEC1);

            // Assert
            assertNotNull(result);
            assertEquals(COD_UNIT_SEC1, result.getCodigo());
            assertEquals(NOME_UNIT_SEC1, result.getNome());
        }

        @Test
        @DisplayName("Deve buscar unidades ativas")
        void deveBuscarUnidadesAtivas() {
            // Act
            List<UnidadeDto> result = unidadeService.buscarTodasUnidades();

            // Assert
            assertNotNull(result);
            assertFalse(result.isEmpty());
            assertTrue(result.size() >= 2);
        }

        @Test
        @DisplayName("Deve buscar subunidades")
        void deveBuscarSubunidades() {
            // Act
            List<UnidadeDto> result = unidadeService.buscarSubordinadas(COD_UNIT_SEC1);

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
            List<UnidadeDto> result = unidadeService.buscarArvoreHierarquica();

            // Assert
            assertNotNull(result);
            assertFalse(result.isEmpty());
            // Roots in data.sql: 1 (TRE)
            assertTrue(result.stream().anyMatch(u -> u.getCodigo().equals(1L)));

            for (UnidadeDto unidade : result) {
                if (unidade.getCodigo().equals(1L)) {
                    assertNull(unidade.getCodigoPai());
                }
            }
        }

        @Test
        @DisplayName("Deve buscar usuários por unidade de lotação")
        void deveBuscarPorUnidadeLotacao() {
            // Unidade 2 tem usuários no data.sql
            List<UsuarioDto> res = unidadeService.buscarUsuariosPorUnidade(2L);
            assertFalse(res.isEmpty());
        }

        @Test
        @DisplayName("Deve lançar erro ao buscar unidade inexistente por código ou sigla")
        void deveRetornarErroAoBuscarUnidadeInexistente() {
            assertThrows(sgc.comum.erros.ErroEntidadeNaoEncontrada.class, () -> unidadeService.buscarPorCodigo(9999L));
            assertThrows(sgc.comum.erros.ErroEntidadeNaoEncontrada.class, () -> unidadeService.buscarPorSigla("SIGLA_NAO_EXISTE"));
        }
    }

    @Nested
    @DisplayName("Gestão de Responsáveis")
    @SuppressWarnings("unused")
    class GestaoResponsaveis {
        @Test
        @DisplayName("Deve buscar responsável da unidade")
        void deveBuscarResponsavelUnidade() {
            // Act
            ResponsavelDto result = unidadeService.buscarResponsavelUnidade(2L);

            // Assert
            assertNotNull(result);
            assertEquals(2L, result.unidadeCodigo());
            assertEquals(TITULO_CHEFE_UNIT2, result.titularTitulo());
        }

        @Test
        @DisplayName("Deve buscar unidades onde usuário é responsável")
        void deveBuscarUnidadesOndeEhResponsavel() {
            // Act
            List<Long> result = usuarioService.buscarUnidadesOndeEhResponsavel(TITULO_CHEFE_UNIT2);

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
            Map<Long, ResponsavelDto> result = unidadeService.buscarResponsaveisUnidades(unidades);

            // Assert
            assertNotNull(result);
            assertTrue(result.containsKey(2L));
            assertTrue(result.containsKey(9L));
            assertEquals(TITULO_CHEFE_UNIT2, result.get(2L).titularTitulo());
            assertEquals("333333333333", result.get(9L).titularTitulo());
        }
    }

    @Nested
    @DisplayName("Gestão de Perfis")
    @SuppressWarnings("unused")
    class GestaoPerfis {
        @Test
        @DisplayName("Deve buscar perfis do usuário")
        void deveBuscarPerfisUsuario() {
            // Act
            List<PerfilDto> result = usuarioService.buscarPerfisUsuario(TITULO_CHEFE_UNIT2);

            // Assert
            assertNotNull(result);
            assertFalse(result.isEmpty());
            assertTrue(result.stream()
                    .anyMatch(p -> p.perfil().equals("CHEFE") && p.unidadeCodigo().equals(2L)));
        }

        @Test
        @DisplayName("Deve verificar se usuário tem perfil")
        void deveVerificarUsuarioTemPerfil() {
            // Act & Assert
            assertTrue(usuarioService.usuarioTemPerfil(TITULO_CHEFE_UNIT2, String.valueOf(Perfil.CHEFE), 2L));
            assertTrue(usuarioService.usuarioTemPerfil(TITULO_ADMIN, String.valueOf(Perfil.ADMIN), 100L));
        }

        @Test
        @DisplayName("Deve buscar unidades por perfil")
        void deveBuscarUnidadesPorPerfil() {
            // Act
            List<Long> adminUnits = usuarioService.buscarUnidadesPorPerfil(TITULO_ADMIN, String.valueOf(Perfil.ADMIN));

            // Assert
            assertTrue(adminUnits.contains(100L));
        }
    }

    @Nested
    @DisplayName("Cobertura Extra")
    @SuppressWarnings("unused")
    class CoberturaExtra {
        @Test
        @DisplayName("Deve retornar null se não houver usuário autenticado")
        void deveRetornarNullSeNaoAutenticado() {
            // Limpa contexto
            org.springframework.security.core.context.SecurityContextHolder.clearContext();

            assertNull(usuarioService.obterUsuarioAutenticadoOuNull());

            assertThrows(sgc.comum.erros.ErroAccessoNegado.class,
                    () -> usuarioService.obterUsuarioAutenticado());
        }

        @Test
        @DisplayName("Deve extrair título de principal")
        void deveExtrairTituloDePrincipal() {
            assertNull(usuarioService.extrairTituloUsuario(null));
            assertEquals("123", usuarioService.extrairTituloUsuario("123"));

            sgc.organizacao.model.Usuario u = new sgc.organizacao.model.Usuario();
            u.setTituloEleitoral("456");
            assertEquals("456", usuarioService.extrairTituloUsuario(u));

            assertEquals("100", usuarioService.extrairTituloUsuario(100L));
        }

        @Test
        @DisplayName("Deve buscar responsáveis ignorando unidades sem chefe")
        void deveBuscarResponsaveisIgnorandoSemChefe() {
            // Unidade 9999 não existe ou não tem chefe
            Map<Long, ResponsavelDto> res = unidadeService.buscarResponsaveisUnidades(List.of(9999L));
            assertTrue(res.isEmpty());
        }

        @Test
        @DisplayName("Deve retornar false se usuário não tiver perfil na unidade")
        void deveRetornarFalseSeNaoTiverPerfil() {
            // Usuário 777 é CHEFE na unidade 2.
            // Verifica se é CHEFE na unidade 99 (não é)
            assertFalse(usuarioService.usuarioTemPerfil(TITULO_CHEFE_UNIT2, String.valueOf(Perfil.CHEFE), 99L));

            // Verifica se é GESTOR na unidade 2 (não é)
            assertFalse(usuarioService.usuarioTemPerfil(TITULO_CHEFE_UNIT2, String.valueOf(Perfil.GESTOR), 2L));
        }
    }

    @Nested
    @DisplayName("Gestão de Administradores")
    @SuppressWarnings("unused")
    class GestaoAdministradores {
        @Test
        @DisplayName("Deve listar, adicionar e remover administradores")
        void deveGerenciarAdministradores() {
            // TITULO_CHEFE_UNIT2 (777) não é admin no data.sql original
            String tituloNovoAdmin = TITULO_CHEFE_UNIT2;

            // Adicionar
            usuarioService.adicionarAdministrador(tituloNovoAdmin);
            assertTrue(usuarioService.isAdministrador(tituloNovoAdmin));

            // Listar
            List<AdministradorDto> admins = usuarioService.listarAdministradores();
            assertTrue(admins.stream().anyMatch(a -> a.tituloEleitoral().equals(tituloNovoAdmin)));

            // Falhar ao adicionar duplicado
            assertThrows(sgc.comum.erros.ErroValidacao.class,
                    () -> usuarioService.adicionarAdministrador(tituloNovoAdmin));

            // Remover
            usuarioService.removerAdministrador(tituloNovoAdmin, TITULO_ADMIN);
            assertFalse(usuarioService.isAdministrador(tituloNovoAdmin));
        }

        @Test
        @DisplayName("Deve falhar ao remover a si mesmo")
        void deveFalharRemoverSiMesmo() {
            assertThrows(sgc.comum.erros.ErroValidacao.class,
                    () -> usuarioService.removerAdministrador(TITULO_ADMIN, TITULO_ADMIN));
        }

        @Test
        @DisplayName("Deve falhar ao remover único admin")
        void deveFalharRemoverUnicoAdmin() {
            // Remove os admins extras do data.sql para sobrar apenas 1
            usuarioService.removerAdministrador("6", "OUTRO");
            usuarioService.removerAdministrador("999999999999", "OUTRO");

            assertThrows(sgc.comum.erros.ErroValidacao.class,
                    () -> usuarioService.removerAdministrador(TITULO_ADMIN, "OUTRO"));
        }
    }
}
