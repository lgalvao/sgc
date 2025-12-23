package sgc.sgrh.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import sgc.fixture.UnidadeFixture;
import sgc.fixture.UsuarioFixture;
import sgc.unidade.model.AtribuicaoTemporaria;
import sgc.unidade.model.Unidade;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Testes para Usuario")
class UsuarioTest {

    private Usuario usuario;
    private Unidade unidade;

    @BeforeEach
    void setUp() {
        unidade = UnidadeFixture.unidadePadrao();
        unidade.setCodigo(1L);
        usuario = UsuarioFixture.usuarioPadrao();
    }

    @Nested
    @DisplayName("Construtor")
    class Construtor {

        @Test
        @DisplayName("Deve criar usuário com construtor completo")
        void deveCriarUsuarioComConstrutorCompleto() {
            // Arrange & Act
            Usuario novoUsuario = new Usuario(
                    "123456789012",
                    "João Silva",
                    "joao@example.com",
                    "5555",
                    unidade
            );

            // Assert
            assertThat(novoUsuario.getTituloEleitoral()).isEqualTo("123456789012");
            assertThat(novoUsuario.getNome()).isEqualTo("João Silva");
            assertThat(novoUsuario.getEmail()).isEqualTo("joao@example.com");
            assertThat(novoUsuario.getRamal()).isEqualTo("5555");
            assertThat(novoUsuario.getUnidadeLotacao()).isEqualTo(unidade);
            assertThat(novoUsuario.getMatricula()).isNull();
            assertThat(novoUsuario.getUnidadeCompetencia()).isNull();
        }
    }

    @Nested
    @DisplayName("Atribuições de Perfil")
    class AtribuicoesPerfil {

        @Test
        @DisplayName("Deve retornar conjunto vazio quando não há atribuições")
        void deveRetornarConjuntoVazioQuandoNaoHaAtribuicoes() {
            // Arrange
            Usuario usuarioSemPerfil = new Usuario();

            // Act
            Set<UsuarioPerfil> atribuicoes = usuarioSemPerfil.getAtribuicoes();

            // Assert
            assertThat(atribuicoes).isEmpty();
        }

        @Test
        @DisplayName("Deve retornar atribuições configuradas")
        void deveRetornarAtribuicoesConfiguradas() {
            // Arrange
            Perfil perfil = Perfil.ADMIN;

            UsuarioPerfil up = new UsuarioPerfil();
            up.setUsuario(usuario);
            up.setUnidade(unidade);
            up.setPerfil(perfil);

            Set<UsuarioPerfil> atribuicoes = new HashSet<>();
            atribuicoes.add(up);
            usuario.setAtribuicoes(atribuicoes);

            // Act
            Set<UsuarioPerfil> resultado = usuario.getAtribuicoes();

            // Assert
            assertThat(resultado).hasSize(1);
            assertThat(resultado).contains(up);
        }
    }

    @Nested
    @DisplayName("Atribuições Temporárias")
    class AtribuicoesTemporarias {

        @Test
        @DisplayName("Deve incluir atribuição temporária ativa em todas atribuições")
        void deveIncluirAtribuicaoTemporariaAtivaEmTodasAtribuicoes() {
            // Arrange
            Perfil perfil = Perfil.GESTOR;

            AtribuicaoTemporaria atribuicaoTemp = new AtribuicaoTemporaria();
            atribuicaoTemp.setUsuario(usuario);
            atribuicaoTemp.setUnidade(unidade);
            atribuicaoTemp.setPerfil(perfil);
            atribuicaoTemp.setDataInicio(LocalDateTime.now().minusDays(1));
            atribuicaoTemp.setDataTermino(LocalDateTime.now().plusDays(1));

            Set<AtribuicaoTemporaria> temporarias = new HashSet<>();
            temporarias.add(atribuicaoTemp);
            usuario.setAtribuicoesTemporarias(temporarias);

            // Act
            Set<UsuarioPerfil> todasAtribuicoes = usuario.getTodasAtribuicoes();

            // Assert
            assertThat(todasAtribuicoes).hasSize(1);
            assertThat(todasAtribuicoes)
                    .extracting(UsuarioPerfil::getPerfil)
                    .contains(Perfil.GESTOR);
        }

        @Test
        @DisplayName("Não deve incluir atribuição temporária expirada")
        void naoDeveIncluirAtribuicaoTemporariaExpirada() {
            // Arrange
            Perfil perfil = Perfil.GESTOR;

            AtribuicaoTemporaria atribuicaoTemp = new AtribuicaoTemporaria();
            atribuicaoTemp.setUsuario(usuario);
            atribuicaoTemp.setUnidade(unidade);
            atribuicaoTemp.setPerfil(perfil);
            atribuicaoTemp.setDataInicio(LocalDateTime.now().minusDays(10));
            atribuicaoTemp.setDataTermino(LocalDateTime.now().minusDays(5));

            Set<AtribuicaoTemporaria> temporarias = new HashSet<>();
            temporarias.add(atribuicaoTemp);
            usuario.setAtribuicoesTemporarias(temporarias);

            // Act
            Set<UsuarioPerfil> todasAtribuicoes = usuario.getTodasAtribuicoes();

            // Assert
            assertThat(todasAtribuicoes).isEmpty();
        }

        @Test
        @DisplayName("Não deve incluir atribuição temporária futura")
        void naoDeveIncluirAtribuicaoTemporariaFutura() {
            // Arrange
            Perfil perfil = Perfil.GESTOR;

            AtribuicaoTemporaria atribuicaoTemp = new AtribuicaoTemporaria();
            atribuicaoTemp.setUsuario(usuario);
            atribuicaoTemp.setUnidade(unidade);
            atribuicaoTemp.setPerfil(perfil);
            atribuicaoTemp.setDataInicio(LocalDateTime.now().plusDays(5));
            atribuicaoTemp.setDataTermino(LocalDateTime.now().plusDays(10));

            Set<AtribuicaoTemporaria> temporarias = new HashSet<>();
            temporarias.add(atribuicaoTemp);
            usuario.setAtribuicoesTemporarias(temporarias);

            // Act
            Set<UsuarioPerfil> todasAtribuicoes = usuario.getTodasAtribuicoes();

            // Assert
            assertThat(todasAtribuicoes).isEmpty();
        }

        @Test
        @DisplayName("Deve combinar atribuições permanentes e temporárias")
        void deveCombinarAtribuicoesPermanentesETemporarias() {
            // Arrange
            Perfil perfilPermanente = Perfil.SERVIDOR;

            UsuarioPerfil upPermanente = new UsuarioPerfil();
            upPermanente.setUsuario(usuario);
            upPermanente.setUnidade(unidade);
            upPermanente.setPerfil(perfilPermanente);

            Set<UsuarioPerfil> atribuicoes = new HashSet<>();
            atribuicoes.add(upPermanente);
            usuario.setAtribuicoes(atribuicoes);

            Perfil perfilTemporario = Perfil.ADMIN;

            AtribuicaoTemporaria atribuicaoTemp = new AtribuicaoTemporaria();
            atribuicaoTemp.setUsuario(usuario);
            atribuicaoTemp.setUnidade(unidade);
            atribuicaoTemp.setPerfil(perfilTemporario);
            atribuicaoTemp.setDataInicio(LocalDateTime.now().minusDays(1));
            atribuicaoTemp.setDataTermino(LocalDateTime.now().plusDays(1));

            Set<AtribuicaoTemporaria> temporarias = new HashSet<>();
            temporarias.add(atribuicaoTemp);
            usuario.setAtribuicoesTemporarias(temporarias);

            // Act
            Set<UsuarioPerfil> todasAtribuicoes = usuario.getTodasAtribuicoes();

            // Assert
            assertThat(todasAtribuicoes).hasSize(2);
        }
    }

    @Nested
    @DisplayName("Spring Security UserDetails")
    class SpringSecurityUserDetails {

        @Test
        @DisplayName("Deve retornar authorities baseadas nos perfis")
        void deveRetornarAuthoritiesBaseadasNosPerfis() {
            // Arrange
            Perfil perfil = Perfil.ADMIN;

            Usuario usuarioComPerfil = UsuarioFixture.usuarioComPerfil(unidade, perfil);

            // Act
            Collection<? extends GrantedAuthority> authorities = usuarioComPerfil.getAuthorities();

            // Assert
            assertThat(authorities).isNotEmpty();
            assertThat(authorities)
                    .extracting(GrantedAuthority::getAuthority)
                    .contains("ROLE_ADMIN");
        }

        @Test
        @DisplayName("Deve retornar titulo eleitoral como username")
        void deveRetornarTituloEleitoralComoUsername() {
            // Arrange
            String tituloEsperado = "123456789012";
            usuario.setTituloEleitoral(tituloEsperado);

            // Act
            String username = usuario.getUsername();

            // Assert
            assertThat(username).isEqualTo(tituloEsperado);
        }

        @Test
        @DisplayName("Deve retornar null como password")
        void deveRetornarNullComoPassword() {
            // Act
            String password = usuario.getPassword();

            // Assert
            assertThat(password).isNull();
        }
    }

    @Nested
    @DisplayName("Equals e HashCode")
    class EqualsEHashCode {

        @Test
        @DisplayName("Deve considerar dois usuários iguais quando têm mesmo titulo eleitoral")
        void deveConsiderarDoisUsuariosIguaisQuandoTemMesmoTituloEleitoral() {
            // Arrange
            Usuario usuario1 = new Usuario();
            usuario1.setTituloEleitoral("123456789012");

            Usuario usuario2 = new Usuario();
            usuario2.setTituloEleitoral("123456789012");

            // Act & Assert
            assertThat(usuario1).isEqualTo(usuario2);
            assertThat(usuario1.hashCode()).isEqualTo(usuario2.hashCode());
        }

        @Test
        @DisplayName("Deve considerar dois usuários diferentes quando têm titulos diferentes")
        void deveConsiderarDoisUsuariosDiferentesQuandoTemTitulosDiferentes() {
            // Arrange
            Usuario usuario1 = new Usuario();
            usuario1.setTituloEleitoral("123456789012");

            Usuario usuario2 = new Usuario();
            usuario2.setTituloEleitoral("987654321098");

            // Act & Assert
            assertThat(usuario1).isNotEqualTo(usuario2);
        }

        @Test
        @DisplayName("Deve considerar usuário igual a si mesmo")
        void deveConsiderarUsuarioIgualASiMesmo() {
            // Act & Assert
            assertThat(usuario).isEqualTo(usuario);
        }

        @Test
        @DisplayName("Deve considerar usuário diferente de null")
        void deveConsiderarUsuarioDiferenteDeNull() {
            // Act & Assert
            assertThat(usuario).isNotEqualTo(null);
        }

        @Test
        @DisplayName("Deve considerar usuário diferente de objeto de outra classe")
        void deveConsiderarUsuarioDiferenteDeObjetoDeOutraClasse() {
            // Arrange
            String outroObjeto = "não é um usuário";

            // Act & Assert
            assertThat(usuario).isNotEqualTo(outroObjeto);
        }
    }
}
