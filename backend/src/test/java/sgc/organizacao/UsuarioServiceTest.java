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

import static org.assertj.core.api.Assertions.*;

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

    private void autenticarComo() {
        Usuario usuario = usuarioServiceInternal.buscar(UsuarioServiceTest.TITULO_ADMIN);
        usuario.setUnidadeAtivaCodigo(UsuarioServiceTest.COD_UNIT_SEC1);
        usuario.setPerfilAtivo(Perfil.ADMIN);
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
            assertThatThrownBy(() -> usuarioServiceInternal.buscar("0000"))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class);
        }

        @Test
        @DisplayName("Deve carregar authorities")
        void deveCarregarAuthorities() {
            Usuario usuario = new Usuario();
            usuario.setTituloEleitoral(TITULO_ADMIN);
            usuarioServiceInternal.carregarAuthorities(usuario);
            assertThat(usuario.getAuthorities()).isNotEmpty();
        }

        @Test
        @DisplayName("Deve buscar usuário por título")
        void deveBuscarUsuarioPorTitulo() {

            Optional<Usuario> result = usuarioServiceInternal.buscarOpt(TITULO_ADMIN);

            assertThat(result).isPresent();
            assertThat(result.get().getTituloEleitoral()).isEqualTo(TITULO_ADMIN);
            assertThat(result.get().getNome()).isEqualTo(NOME_ADMIN);
        }

        @Test
        @DisplayName("Deve buscar entidade usuário por login garantindo inicialização")
        void deveBuscarEntidadeUsuarioPorLogin() {

            var usuario = usuarioService.buscarPorLogin(TITULO_ADMIN);

            assertThat(usuario.getTituloEleitoral()).isEqualTo(TITULO_ADMIN);
        }

        @Test
        @DisplayName("Deve buscar usuários por lista de títulos")
        void deveBuscarUsuariosPorTitulos() {

            List<String> titulos = List.of(TITULO_CHEFE_UNIT2, TITULO_ADMIN);

            Map<String, Usuario> result = usuarioService.buscarUsuariosPorTitulos(titulos);

            assertThat(result).hasSize(2)
                    .containsKey(TITULO_CHEFE_UNIT2)
                    .containsKey(TITULO_ADMIN);
        }

        @Test
        @DisplayName("Deve pesquisar usuários por nome")
        void devePesquisarUsuariosPorNome() {
            List<UsuarioPesquisaDto> resultado = usuarioServiceInternal.pesquisarPorNome("Admin");

            assertThat(resultado).isNotEmpty();
            assertThat(resultado).extracting(UsuarioPesquisaDto::nome)
                    .anyMatch(nome -> nome.contains("Admin"));
        }

        @Test
        @DisplayName("Deve pesquisar usuários por título eleitoral")
        void devePesquisarUsuariosPorTituloEleitoral() {
            List<UsuarioPesquisaDto> resultado = usuarioServiceInternal.pesquisarPorNome("17");

            assertThat(resultado).isNotEmpty();
            assertThat(resultado).extracting(UsuarioPesquisaDto::tituloEleitoral)
                    .contains("17");
        }

        @Test
        @DisplayName("Deve retornar vazio ao pesquisar termo curto")
        void deveRetornarVazioTermoCurto() {
            List<UsuarioPesquisaDto> resultado = usuarioServiceInternal.pesquisarPorNome("a");
            assertThat(resultado).isEmpty();
        }

        @Test
        @DisplayName("Deve buscar com Optional")
        void deveBuscarComOptional() {
            Optional<Usuario> res = usuarioServiceInternal.buscarOpt(TITULO_ADMIN);
            assertThat(res).isPresent();

            Optional<Usuario> naoEncontrado = usuarioServiceInternal.buscarOpt("000");
            assertThat(naoEncontrado).isEmpty();
        }

        @Test
        @DisplayName("Deve buscar Consulta de Leitura por titulo")
        void deveBuscarConsultaLeituraPorTitulo() {
            Optional<UsuarioConsultaLeitura> res = usuarioServiceInternal.buscarConsultaPorTitulo(TITULO_ADMIN);
            assertThat(res).isPresent();
        }

        @Test
        @DisplayName("Deve buscar consultas de leitura por unidade lotação")
        void deveBuscarConsultasLeituraPorLotacao() {
            List<UsuarioConsultaLeitura> res = usuarioServiceInternal.buscarConsultasPorUnidadeLotacao(COD_UNIT_SEC1);
            assertThat(res).isNotEmpty();
        }

        @Test
        @DisplayName("Deve buscar com Opt por titulo e lotacao")
        void deveBuscarComOptPorTituloELotacao() {
            Optional<Usuario> res = usuarioServiceInternal.buscarOptComUnidadeLotacao(TITULO_ADMIN);
            assertThat(res).isPresent();
        }

        @Test
        @DisplayName("Deve buscar usuarios por titulos no Internal Service")
        void deveBuscarUsuariosPorTitulosInternal() {
            List<Usuario> res = usuarioServiceInternal.buscarPorTitulos(List.of(TITULO_ADMIN, TITULO_CHEFE_UNIT2));
            assertThat(res).isNotEmpty();
        }

        @Test
        @DisplayName("Deve buscar autorizações de perfil e perfis")
        void deveBuscarAutorizacoesEPerfis() {
            List<UsuarioPerfilAutorizacaoLeitura> auts = usuarioServiceInternal.buscarAutorizacoesPerfil(TITULO_ADMIN);
            assertThat(auts).isNotEmpty();

            List<Perfil> perfis = usuarioServiceInternal.buscarPerfisPorUsuarioTitulo(TITULO_ADMIN);
            assertThat(perfis).isNotEmpty();
        }

        @Test
        @DisplayName("Deve buscar administradores")
        void deveBuscarAdministradores() {
            List<Administrador> admins = usuarioServiceInternal.buscarAdministradores();
            assertThat(admins).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("Consultas de Unidade")
    class ConsultasUnidade {
        @Test
        @DisplayName("Deve buscar unidade por código")
        void deveBuscarUnidadePorCodigo() {

            Unidade result = unidadeService2.buscarPorCodigo(COD_UNIT_SEC1);

            assertThat(result.getCodigo()).isEqualTo(COD_UNIT_SEC1);
            assertThat(result.getNome()).isEqualTo(NOME_UNIT_SEC1);
        }

        @Test
        @DisplayName("Deve buscar unidades ativas")
        void deveBuscarUnidadesAtivas() {
            List<UnidadeDto> result = hierarquiaService.buscarArvoreHierarquica();

            assertThat(result).isNotEmpty();
        }

        @Test
        @DisplayName("Deve buscar subunidades")
        void deveBuscarSubunidades() {

            List<UnidadeDto> result = hierarquiaService.buscarSubordinadas(COD_UNIT_SEC1);

            assertThat(result).isNotEmpty();
            assertThat(result).allMatch(unidade -> COD_UNIT_SEC1.equals(unidade.getCodigoPai()));
        }

        @Test
        @DisplayName("Deve construir árvore hierárquica")
        void deveConstruirArvoreHierarquica() {

            List<UnidadeDto> result = hierarquiaService.buscarArvoreHierarquica();

            assertThat(result).isNotEmpty();
            assertThat(result).extracting(UnidadeDto::getCodigo).contains(1L);
            assertThat(result).filteredOn(u -> u.getCodigo().equals(1L))
                    .allMatch(u -> u.getCodigoPai() == null);
        }

        @Test
        @DisplayName("Deve buscar usuários por unidade de lotação")
        void deveBuscarPorUnidadeLotacao() {

            List<Usuario> res = usuarioServiceInternal.buscarPorUnidadeLotacao(2L);
            assertThat(res).isNotEmpty();
        }

        @Test
        @DisplayName("Deve lançar erro ao buscar unidade inexistente por código ou sigla")
        void deveRetornarErroAoBuscarUnidadeInexistente() {
            assertThatThrownBy(() -> unidadeService2.buscarPorCodigo(9999L))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class);
            assertThatThrownBy(() -> unidadeService2.buscarPorSigla("SIGLA_NAO_EXISTE"))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class);
        }
    }

    @Nested
    @DisplayName("Gestão de Responsáveis")
    class GestaoResponsaveis {
        @Test
        @DisplayName("Deve buscar responsável da unidade")
        void deveBuscarResponsavelUnidade() {

            UnidadeResponsavelDto result = responsavelService.buscarResponsavelUnidade(2L);

            assertThat(result.unidadeCodigo()).isEqualTo(2L);
            assertThat(result.titularTitulo()).isEqualTo(TITULO_CHEFE_UNIT2);
        }

        @Test
        @DisplayName("Deve buscar unidades onde usuário é responsável")
        void deveBuscarUnidadesOndeEhResponsavel() {

            List<Long> result = responsavelService.buscarUnidadesOndeEhResponsavel(TITULO_CHEFE_UNIT2);

            assertThat(result).isNotEmpty().contains(2L);
        }

        @Test
        @DisplayName("Deve buscar responsáveis de múltiplas unidades")
        void deveBuscarResponsaveisUnidades() {

            List<Long> unidades = List.of(2L, 9L);

            Map<Long, UnidadeResponsavelDto> result = responsavelService.buscarResponsaveisUnidades(unidades);

            assertThat(result).containsKey(2L).containsKey(9L);
            assertThat(result.get(2L).titularTitulo()).isEqualTo(TITULO_CHEFE_UNIT2);
            assertThat(result.get(9L).titularTitulo()).isEqualTo("333333333333");
        }
    }

    @Nested
    @DisplayName("Gestão de Perfis")
    class GestaoPerfis {
        @Test
        @DisplayName("Deve buscar perfis do usuário")
        void deveBuscarPerfisUsuario() {

            List<PerfilDto> result = usuarioService.buscarPerfisUsuario(TITULO_CHEFE_UNIT2);

            assertThat(result).isNotEmpty();
            assertThat(result).anyMatch(p -> p.perfil().equals("CHEFE") && Objects.equals(p.unidadeCodigo(), 2L));
        }
    }

    @Nested
    @DisplayName("Cobertura extra")
    class CoberturaExtra {
        @Test
        @DisplayName("Deve buscar responsáveis ignorando unidades sem chefe")
        void deveBuscarResponsaveisIgnorandoSemChefe() {

            Map<Long, UnidadeResponsavelDto> res = responsavelService.buscarResponsaveisUnidades(List.of(9999L));
            assertThat(res).isEmpty();
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
            assertThat(usuarioServiceInternal.isAdministrador(tituloNovoAdmin)).isTrue();

            // Listar
            List<AdministradorDto> admins = usuarioService.listarAdministradores();
            assertThat(admins).extracting(AdministradorDto::tituloEleitoral).contains(tituloNovoAdmin);

            // Falhar ao adicionar duplicado
            assertThatThrownBy(() -> usuarioService.adicionarAdministrador(tituloNovoAdmin))
                    .isInstanceOf(ErroValidacao.class);

            // Remover
            autenticarComo();
            usuarioService.removerAdministrador(tituloNovoAdmin);
            assertThat(usuarioServiceInternal.isAdministrador(tituloNovoAdmin)).isFalse();
        }

        @Test
        @DisplayName("Deve falhar ao remover a si mesmo")
        void deveFalharRemoverSiMesmo() {
            autenticarComo();
            assertThatThrownBy(() -> usuarioService.removerAdministrador(TITULO_ADMIN)).isInstanceOf(ErroValidacao.class);
        }

        @Test
        @DisplayName("Deve falhar ao remover único admin")
        void deveFalharRemoverUnicoAdmin() {

            autenticarComo();
            usuarioService.removerAdministrador("6");
            usuarioService.removerAdministrador("999999999999");

            assertThatThrownBy(() -> usuarioService.removerAdministrador(TITULO_ADMIN)).isInstanceOf(ErroValidacao.class);
        }
    }
}
