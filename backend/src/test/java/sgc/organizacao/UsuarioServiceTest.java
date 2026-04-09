package sgc.organizacao;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.context.*;
import org.springframework.transaction.annotation.*;
import sgc.comum.erros.*;
import sgc.organizacao.dto.*;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
@SpringBootTest
@Transactional
@DisplayName("Testes do Serviço UsuarioFacade")
class UsuarioServiceTest {

    private static final String TITULO_ADMIN = "111111111111";
    private static final String NOME_ADMIN = "Admin Teste";

    private static final Long COD_UNIT_SEC1 = 2L;
    private static final String NOME_UNIT_SEC1 = "Secretaria de Informática e Comunicações";

    private static final String TITULO_CHEFE_UNIT2 = "777"; // Chefe STIC Teste

    @Autowired
    private UsuarioFacade usuarioService;

    @Autowired
    private UnidadeService unidadeService2;

    @Autowired
    private UnidadeHierarquiaService hierarquiaService;

    @Autowired
    private ResponsavelUnidadeService responsavelService;

    @Autowired
    private UsuarioService usuarioServiceInternal;

    private void autenticarComo(String titulo, Long unidadeAtivaCodigo, Perfil perfil) {
        Usuario usuario = usuarioServiceInternal.buscar(titulo);
        usuario.setUnidadeAtivaCodigo(unidadeAtivaCodigo);
        usuario.setPerfilAtivo(perfil);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(usuario, null, List.of()));
        SecurityContextHolder.setContext(context);
    }

    @AfterEach
    void limparContextoSeguranca() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("Consultas de Usuário")
    class ConsultasUsuario {
        @Test
        @DisplayName("Deve falhar ao buscar usuario inexistente")
        void deveFalharAoBuscarUsuarioInexistente() {
            assertThrows(ErroEntidadeNaoEncontrada.class, () -> usuarioServiceInternal.buscar("0000"));
        }

        @Test
        @DisplayName("Deve carregar authorities")
        void deveCarregarAuthorities() {
            Usuario usuario = new Usuario();
            usuario.setTituloEleitoral(TITULO_ADMIN);
            usuarioServiceInternal.carregarAuthorities(usuario);
            assertNotNull(usuario.getAuthorities());
            assertFalse(usuario.getAuthorities().isEmpty());
        }

        @Test
        @DisplayName("Deve buscar usuário por título")
        void deveBuscarUsuarioPorTitulo() {

            Optional<Usuario> result = usuarioServiceInternal.buscarOpt(TITULO_ADMIN);

            assertTrue(result.isPresent());
            assertEquals(TITULO_ADMIN, result.get().getTituloEleitoral());
            assertEquals(NOME_ADMIN, result.get().getNome());
        }

        @Test
        @DisplayName("Deve buscar entidade usuário por login garantindo inicialização")
        void deveBuscarEntidadeUsuarioPorLogin() {

            var usuario = usuarioService.buscarPorLogin(TITULO_ADMIN);

            assertNotNull(usuario);
            assertEquals(TITULO_ADMIN, usuario.getTituloEleitoral());
        }

        @Test
        @DisplayName("Deve buscar usuários por lista de títulos")
        void deveBuscarUsuariosPorTitulos() {

            List<String> titulos = List.of(TITULO_CHEFE_UNIT2, TITULO_ADMIN);

            Map<String, Usuario> result = usuarioService.buscarUsuariosPorTitulos(titulos);

            assertNotNull(result);
            assertEquals(2, result.size());
            assertTrue(result.containsKey(TITULO_CHEFE_UNIT2));
            assertTrue(result.containsKey(TITULO_ADMIN));
        }

        @Test
        @DisplayName("Deve pesquisar usuários por nome")
        void devePesquisarUsuariosPorNome() {
            List<UsuarioPesquisaDto> resultado = usuarioServiceInternal.pesquisarPorNome("Admin");

            assertNotNull(resultado);
            assertFalse(resultado.isEmpty());
            assertTrue(resultado.stream().anyMatch(usuario -> usuario.nome().contains("Admin")));
        }

        @Test
        @DisplayName("Deve pesquisar usuários por título eleitoral")
        void devePesquisarUsuariosPorTituloEleitoral() {
            List<UsuarioPesquisaDto> resultado = usuarioServiceInternal.pesquisarPorNome("17");

            assertNotNull(resultado);
            assertFalse(resultado.isEmpty());
            assertTrue(resultado.stream().anyMatch(usuario -> "17".equals(usuario.tituloEleitoral())));
        }

        @Test
        @DisplayName("Deve retornar vazio ao pesquisar termo curto")
        void deveRetornarVazioTermoCurto() {
            List<UsuarioPesquisaDto> resultado = usuarioServiceInternal.pesquisarPorNome("a");
            assertTrue(resultado.isEmpty());
        }

        @Test
        @DisplayName("Deve buscar com Optional")
        void deveBuscarComOptional() {
            Optional<Usuario> res = usuarioServiceInternal.buscarOpt(TITULO_ADMIN);
            assertTrue(res.isPresent());

            Optional<Usuario> naoEncontrado = usuarioServiceInternal.buscarOpt("000");
            assertTrue(naoEncontrado.isEmpty());
        }

        @Test
        @DisplayName("Deve buscar Consulta de Leitura por titulo")
        void deveBuscarConsultaLeituraPorTitulo() {
            Optional<UsuarioConsultaLeitura> res = usuarioServiceInternal.buscarConsultaPorTitulo(TITULO_ADMIN);
            assertTrue(res.isPresent());
        }

        @Test
        @DisplayName("Deve buscar consultas de leitura por unidade lotação")
        void deveBuscarConsultasLeituraPorLotacao() {
            List<UsuarioConsultaLeitura> res = usuarioServiceInternal.buscarConsultasPorUnidadeLotacao(COD_UNIT_SEC1);
            assertFalse(res.isEmpty());
        }

        @Test
        @DisplayName("Deve buscar com Opt por titulo e lotacao")
        void deveBuscarComOptPorTituloELotacao() {
            Optional<Usuario> res = usuarioServiceInternal.buscarOptComUnidadeLotacao(TITULO_ADMIN);
            assertTrue(res.isPresent());
        }

        @Test
        @DisplayName("Deve buscar usuarios por titulos no Internal Service")
        void deveBuscarUsuariosPorTitulosInternal() {
            List<Usuario> res = usuarioServiceInternal.buscarPorTitulos(List.of(TITULO_ADMIN, TITULO_CHEFE_UNIT2));
            assertFalse(res.isEmpty());
        }

        @Test
        @DisplayName("Deve buscar autorizacoes de perfil e perfis")
        void deveBuscarAutorizacoesEPerfis() {
            List<UsuarioPerfilAutorizacaoLeitura> auts = usuarioServiceInternal.buscarAutorizacoesPerfil(TITULO_ADMIN);
            assertNotNull(auts);

            List<Perfil> perfis = usuarioServiceInternal.buscarPerfisPorUsuarioTitulo(TITULO_ADMIN);
            assertNotNull(perfis);
        }

        @Test
        @DisplayName("Deve buscar administradores")
        void deveBuscarAdministradores() {
            List<Administrador> admins = usuarioServiceInternal.buscarAdministradores();
            assertFalse(admins.isEmpty());
        }
    }

    @Nested
    @DisplayName("Consultas de Unidade")
    class ConsultasUnidade {
        @Test
        @DisplayName("Deve buscar unidade por código")
        void deveBuscarUnidadePorCodigo() {

            Unidade result = unidadeService2.buscarPorCodigo(COD_UNIT_SEC1);

            assertNotNull(result);
            assertEquals(COD_UNIT_SEC1, result.getCodigo());
            assertEquals(NOME_UNIT_SEC1, result.getNome());
        }

        @Test
        @DisplayName("Deve buscar unidades ativas")
        void deveBuscarUnidadesAtivas() {
            List<UnidadeDto> result = hierarquiaService.buscarArvoreHierarquica();

            assertNotNull(result);
            assertFalse(result.isEmpty());
        }

        @Test
        @DisplayName("Deve buscar subunidades")
        void deveBuscarSubunidades() {

            List<UnidadeDto> result = hierarquiaService.buscarSubordinadas(COD_UNIT_SEC1);

            assertNotNull(result);
            assertFalse(result.isEmpty());
            for (UnidadeDto unidade : result) {
                assertEquals(COD_UNIT_SEC1, unidade.getCodigoPai());
            }
        }

        @Test
        @DisplayName("Deve construir árvore hierárquica")
        void deveConstruirArvoreHierarquica() {

            List<UnidadeDto> result = hierarquiaService.buscarArvoreHierarquica();

            assertNotNull(result);
            assertFalse(result.isEmpty());

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

            List<Usuario> res = usuarioServiceInternal.buscarPorUnidadeLotacao(2L);
            assertFalse(res.isEmpty());
        }

        @Test
        @DisplayName("Deve lançar erro ao buscar unidade inexistente por código ou sigla")
        void deveRetornarErroAoBuscarUnidadeInexistente() {
            assertThrows(ErroEntidadeNaoEncontrada.class, () -> unidadeService2.buscarPorCodigo(9999L));
            assertThrows(ErroEntidadeNaoEncontrada.class, () -> unidadeService2.buscarPorSigla("SIGLA_NAO_EXISTE"));
        }
    }

    @Nested
    @DisplayName("Gestão de Responsáveis")
    class GestaoResponsaveis {
        @Test
        @DisplayName("Deve buscar responsável da unidade")
        void deveBuscarResponsavelUnidade() {

            UnidadeResponsavelDto result = responsavelService.buscarResponsavelUnidade(2L);

            assertNotNull(result);
            assertEquals(2L, result.unidadeCodigo());
            assertEquals(TITULO_CHEFE_UNIT2, result.titularTitulo());
        }

        @Test
        @DisplayName("Deve buscar unidades onde usuário é responsável")
        void deveBuscarUnidadesOndeEhResponsavel() {

            List<Long> result = responsavelService.buscarUnidadesOndeEhResponsavel(TITULO_CHEFE_UNIT2);

            assertNotNull(result);
            assertFalse(result.isEmpty());
            assertTrue(result.contains(2L));
        }

        @Test
        @DisplayName("Deve buscar responsáveis de múltiplas unidades")
        void deveBuscarResponsaveisUnidades() {

            List<Long> unidades = List.of(2L, 9L);

            Map<Long, UnidadeResponsavelDto> result = responsavelService.buscarResponsaveisUnidades(unidades);

            assertNotNull(result);
            assertTrue(result.containsKey(2L));
            assertTrue(result.containsKey(9L));
            UnidadeResponsavelDto responsavel2 = result.get(2L);
            UnidadeResponsavelDto responsavel9 = result.get(9L);
            assertNotNull(responsavel2);
            assertNotNull(responsavel9);
            assertEquals(TITULO_CHEFE_UNIT2, responsavel2.titularTitulo());
            assertEquals("333333333333", responsavel9.titularTitulo());
        }
    }

    @Nested
    @DisplayName("Gestão de Perfis")
    class GestaoPerfis {
        @Test
        @DisplayName("Deve buscar perfis do usuário")
        void deveBuscarPerfisUsuario() {

            List<PerfilDto> result = usuarioService.buscarPerfisUsuario(TITULO_CHEFE_UNIT2);

            assertNotNull(result);
            assertFalse(result.isEmpty());
            assertTrue(result.stream()
                    .anyMatch(p -> p.perfil().equals("CHEFE") && Objects.equals(p.unidadeCodigo(), 2L)));
        }
    }

    @Nested
    @DisplayName("Cobertura extra")
    class CoberturaExtra {
        @Test
        @DisplayName("Deve buscar responsáveis ignorando unidades sem chefe")
        void deveBuscarResponsaveisIgnorandoSemChefe() {

            Map<Long, UnidadeResponsavelDto> res = responsavelService.buscarResponsaveisUnidades(List.of(9999L));
            assertTrue(res.isEmpty());
        }
    }

    @Nested
    @DisplayName("Gestão de Administradores")
    class GestaoAdministradores {
        @Test
        @DisplayName("Deve listar, adicionar e remover administradores")
        void deveGerenciarAdministradores() {

            String tituloNovoAdmin = TITULO_CHEFE_UNIT2;

            // Adicionar
            usuarioService.adicionarAdministrador(tituloNovoAdmin);
            assertTrue(usuarioServiceInternal.isAdministrador(tituloNovoAdmin));

            // Listar
            List<AdministradorDto> admins = usuarioService.listarAdministradores();
            assertTrue(admins.stream().anyMatch(a -> a.tituloEleitoral().equals(tituloNovoAdmin)));

            // Falhar ao adicionar duplicado
            assertThrows(ErroValidacao.class,
                    () -> usuarioService.adicionarAdministrador(tituloNovoAdmin));

            // Remover
            autenticarComo(TITULO_ADMIN, COD_UNIT_SEC1, Perfil.ADMIN);
            usuarioService.removerAdministrador(tituloNovoAdmin);
            assertFalse(usuarioServiceInternal.isAdministrador(tituloNovoAdmin));
        }

        @Test
        @DisplayName("Deve falhar ao remover a si mesmo")
        void deveFalharRemoverSiMesmo() {
            autenticarComo(TITULO_ADMIN, COD_UNIT_SEC1, Perfil.ADMIN);
            assertThrows(ErroValidacao.class,
                    () -> usuarioService.removerAdministrador(TITULO_ADMIN));
        }

        @Test
        @DisplayName("Deve falhar ao remover único admin")
        void deveFalharRemoverUnicoAdmin() {

            autenticarComo(TITULO_ADMIN, COD_UNIT_SEC1, Perfil.ADMIN);
            usuarioService.removerAdministrador("6");
            usuarioService.removerAdministrador("999999999999");

            assertThrows(ErroValidacao.class,
                    () -> usuarioService.removerAdministrador(TITULO_ADMIN));
        }
    }
}
