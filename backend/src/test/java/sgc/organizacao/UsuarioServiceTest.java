package sgc.organizacao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.erros.ErroAutenticacao;
import sgc.organizacao.dto.*;
import sgc.organizacao.model.Perfil;
import sgc.seguranca.dto.EntrarReq;
import sgc.seguranca.dto.PerfilUnidadeDto;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Testes do Serviço UsuarioService")
class UsuarioServiceTest {

    // Data from backend/src/test/resources/data.sql
    private static final String TITULO_ADMIN = "111111111111";
    private static final String EMAIL_ADMIN = "admin.teste@tre-pe.jus.br";
    private static final String NOME_ADMIN = "Admin Teste";

    private static final Long COD_UNIT_SEC1 = 2L;
    private static final String NOME_UNIT_SEC1 = "Secretaria de Informática e Comunicações";

    private static final String TITULO_CHEFE_UNIT2 = "777"; // Chefe STIC Teste

    @Autowired
    private UsuarioService usuarioService;

    @Nested
    @DisplayName("Consultas de Usuário")
    class ConsultasUsuario {

        @Test
        @DisplayName("Deve buscar usuário por título")
        void deveBuscarUsuarioPorTitulo() {
            // Act
            Optional<UsuarioDto> result = usuarioService.buscarUsuarioPorTitulo(TITULO_ADMIN);

            // Assert
            assertTrue(result.isPresent());
            assertEquals(TITULO_ADMIN, result.get().getTituloEleitoral());
            assertEquals(NOME_ADMIN, result.get().getNome());
        }

        @Test
        @DisplayName("Deve buscar entidade usuário por login garantindo inicialização")
        void deveBuscarEntidadeUsuarioPorLogin() {
            // Act
            var usuario = usuarioService.buscarPorLogin(TITULO_ADMIN);

            // Assert
            assertNotNull(usuario);
            assertEquals(TITULO_ADMIN, usuario.getTituloEleitoral());

            // Verifica se a coleção foi inicializada (não deve lançar LazyInitializationException)
            assertDoesNotThrow(() -> {
                 if (usuario.getAtribuicoesTemporarias() != null) {
                     usuario.getAtribuicoesTemporarias().size();
                 }
            });
        }

        @Test
        @DisplayName("Deve buscar usuário por email")
        void deveBuscarUsuarioPorEmail() {
            // Act
            Optional<UsuarioDto> result = usuarioService.buscarUsuarioPorEmail(EMAIL_ADMIN);

            // Assert
            assertTrue(result.isPresent());
            assertEquals(TITULO_ADMIN, result.get().getTituloEleitoral());
            assertEquals(EMAIL_ADMIN, result.get().getEmail());
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
    class ConsultasUnidade {

        @Test
        @DisplayName("Deve buscar unidade por código")
        void deveBuscarUnidadePorCodigo() {
            // Act
            Optional<UnidadeDto> result = usuarioService.buscarUnidadePorCodigo(COD_UNIT_SEC1);

            // Assert
            assertTrue(result.isPresent());
            assertEquals(COD_UNIT_SEC1, result.get().getCodigo());
            assertEquals(NOME_UNIT_SEC1, result.get().getNome());
        }

        @Test
        @DisplayName("Deve buscar unidades ativas")
        void deveBuscarUnidadesAtivas() {
            // Act
            List<UnidadeDto> result = usuarioService.buscarUnidadesAtivas();

            // Assert
            assertNotNull(result);
            assertFalse(result.isEmpty());
            assertTrue(result.size() >= 2);
        }

        @Test
        @DisplayName("Deve buscar subunidades")
        void deveBuscarSubunidades() {
            // Act
            List<UnidadeDto> result = usuarioService.buscarSubunidades(COD_UNIT_SEC1);

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
            List<UnidadeDto> result = usuarioService.construirArvoreHierarquica();

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
            List<UsuarioDto> res = usuarioService.buscarUsuariosPorUnidade(2L);
            assertFalse(res.isEmpty());
        }

        @Test
        @DisplayName("Deve retornar vazio ao buscar unidade inexistente por código ou sigla")
        void deveRetornarVazioAoBuscarUnidadeInexistente() {
            assertTrue(usuarioService.buscarUnidadePorCodigo(9999L).isEmpty());
            assertTrue(usuarioService.buscarUnidadePorSigla("SIGLA_NAO_EXISTE").isEmpty());
        }
    }

    @Nested
    @DisplayName("Gestão de Responsáveis")
    class GestaoResponsaveis {

        @Test
        @DisplayName("Deve buscar responsável da unidade")
        void deveBuscarResponsavelUnidade() {
            // Act
            Optional<ResponsavelDto> result = usuarioService.buscarResponsavelUnidade(2L);

            // Assert
            assertTrue(result.isPresent());
            assertEquals(2L, result.get().getUnidadeCodigo());
            assertEquals(TITULO_CHEFE_UNIT2, result.get().getTitularTitulo());
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
            Map<Long, ResponsavelDto> result = usuarioService.buscarResponsaveisUnidades(unidades);

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
            List<PerfilDto> result = usuarioService.buscarPerfisUsuario(TITULO_CHEFE_UNIT2);

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
            assertTrue(usuarioService.usuarioTemPerfil(TITULO_CHEFE_UNIT2, "CHEFE", 2L));
            assertTrue(usuarioService.usuarioTemPerfil(TITULO_ADMIN, "ADMIN", 100L));
        }

        @Test
        @DisplayName("Deve buscar unidades por perfil")
        void deveBuscarUnidadesPorPerfil() {
            // Act
            List<Long> adminUnits = usuarioService.buscarUnidadesPorPerfil(TITULO_ADMIN, "ADMIN");

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
            boolean resultado = usuarioService.autenticar(TITULO_ADMIN, "senha");

            // Assert
            assertTrue(resultado);
        }

        @Test
        @DisplayName("Deve impedir autorização sem autenticação prévia")
        void deveImpedirAutorizacaoSemAutenticacao() {
            // Usa um usuário diferente que não foi autenticado nos outros testes
            String usuarioNaoAutenticado = "999999999999";
            assertThrows(ErroAutenticacao.class,
                    () -> usuarioService.autorizar(usuarioNaoAutenticado));
        }

        @Test
        @DisplayName("Deve autorizar e retornar lista de perfis/unidades")
        void deveAutorizarERetornarPerfis() {
            // Arrange
            usuarioService.autenticar(TITULO_CHEFE_UNIT2, "senha");

            // Act
            List<PerfilUnidadeDto> resultado = usuarioService.autorizar(TITULO_CHEFE_UNIT2);

            // Assert
            assertNotNull(resultado);
            assertFalse(resultado.isEmpty());
            assertTrue(resultado.stream()
                    .anyMatch(pu -> pu.getPerfil() == Perfil.CHEFE
                            && pu.getUnidade().getCodigo().equals(2L)));
        }

        @Test
        @DisplayName("Deve retornar false ao autenticar usuário não encontrado")
        void deveRetornarFalseAoAutenticarUsuarioInexistente() {
            // Act - usuário inexistente não passa na autenticação em ambiente de testes
            boolean resultado = usuarioService.autenticar("TITULO_INEXISTENTE_XYZ", "senha");

            // Assert
            assertFalse(resultado);
        }

        @Test
        @DisplayName("Deve falhar 'entrar' com sessão expirada")
        void deveFalharEntrarSessaoExpirada() {
             // Usa um título que com certeza não foi autenticado recentemente
             EntrarReq req = new EntrarReq("TITULO_NUNCA_VISTO", "ADMIN", 100L);
             assertThrows(ErroAutenticacao.class, () -> usuarioService.entrar(req));
        }
    }

    @Nested
    @DisplayName("Gestão de Administradores")
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
            assertTrue(admins.stream().anyMatch(a -> a.getTituloEleitoral().equals(tituloNovoAdmin)));
            
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
