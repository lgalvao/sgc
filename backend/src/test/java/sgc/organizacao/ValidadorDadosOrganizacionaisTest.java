package sgc.organizacao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;
import sgc.comum.erros.ErroConfiguracao;
import sgc.organizacao.model.*;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do ValidadorDadosOrganizacionais")
class ValidadorDadosOrganizacionaisTest {

    @Mock
    private UnidadeRepo unidadeRepo;

    @Mock
    private UsuarioRepo usuarioRepo;

    @InjectMocks
    private ValidadorDadosOrganizacionais validador;

    private Unidade criarUnidadeValida(Long codigo, String sigla, TipoUnidade tipo) {
        Unidade u = new Unidade();
        u.setCodigo(codigo);
        u.setSigla(sigla);
        u.setTipo(tipo);
        u.setSituacao(SituacaoUnidade.ATIVA);
        u.setTituloTitular("TITULO_" + codigo);
        return u;
    }

    private Usuario criarUsuarioValido(String titulo) {
        Usuario u = new Usuario();
        u.setTituloEleitoral(titulo);
        u.setNome("Nome " + titulo);
        u.setEmail(titulo.toLowerCase() + "@email.com");
        return u;
    }

    @Nested
    @DisplayName("Cenários de Sucesso")
    class CenariosSucesso {

        @Test
        @DisplayName("Deve validar com sucesso quando todos os dados estão corretos")
        void deveValidarComSucesso() {
            // Arrange
            Unidade u1 = criarUnidadeValida(1L, "U1", TipoUnidade.OPERACIONAL);
            Usuario titular = criarUsuarioValido("TITULO_1");

            when(unidadeRepo.findAllWithHierarquia()).thenReturn(List.of(u1));
            when(usuarioRepo.findAllById(List.of("TITULO_1"))).thenReturn(List.of(titular));

            // Act & Assert
            assertThatCode(() -> validador.run(new DefaultApplicationArguments()))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Deve ignorar unidades inativas")
        void deveIgnorarUnidadesInativas() {
            // Arrange
            Unidade inativa = criarUnidadeValida(1L, "INATIVA", TipoUnidade.OPERACIONAL);
            inativa.setSituacao(SituacaoUnidade.INATIVA);
            inativa.setTituloTitular(null); // Sem titular, mas é inativa

            when(unidadeRepo.findAllWithHierarquia()).thenReturn(List.of(inativa));

            // Act & Assert
            assertThatCode(() -> validador.run(new DefaultApplicationArguments()))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Deve ignorar unidades SEM_EQUIPE e RAIZ")
        void deveIgnorarUnidadesNaoParticipantes() {
            // Arrange
            Unidade semEquipe = criarUnidadeValida(1L, "SEM", TipoUnidade.SEM_EQUIPE);
            semEquipe.setTituloTitular(null);
            Unidade raiz = criarUnidadeValida(2L, "RAIZ", TipoUnidade.RAIZ);
            raiz.setTituloTitular(null);

            when(unidadeRepo.findAllWithHierarquia()).thenReturn(List.of(semEquipe, raiz));

            // Act & Assert
            assertThatCode(() -> validador.run(new DefaultApplicationArguments()))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Deve validar intermediária com subordinadas")
        void deveValidarIntermediariaComSubordinadas() {
            // Arrange
            Unidade pai = criarUnidadeValida(1L, "PAI", TipoUnidade.INTERMEDIARIA);
            Unidade filha = criarUnidadeValida(2L, "FILHA", TipoUnidade.OPERACIONAL);
            filha.setUnidadeSuperior(pai);

            Usuario titularPai = criarUsuarioValido("TITULO_1");
            Usuario titularFilha = criarUsuarioValido("TITULO_2");

            when(unidadeRepo.findAllWithHierarquia()).thenReturn(List.of(pai, filha));
            when(usuarioRepo.findAllById(List.of("TITULO_1", "TITULO_2")))
                    .thenReturn(List.of(titularPai, titularFilha));

            // Act & Assert
            assertThatCode(() -> validador.run(new DefaultApplicationArguments()))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Cenários de Violação")
    class CenariosViolacao {

        @Test
        @DisplayName("Deve falhar quando unidade não tem titular")
        void deveFalharSemTitular() {
            // Arrange
            Unidade u = criarUnidadeValida(1L, "U1", TipoUnidade.OPERACIONAL);
            u.setTituloTitular(null);

            when(unidadeRepo.findAllWithHierarquia()).thenReturn(List.of(u));

            // Act & Assert
            assertThatThrownBy(() -> validador.run(new DefaultApplicationArguments()))
                    .isInstanceOf(ErroConfiguracao.class)
                    .hasMessageContaining("1 violação");
        }

        @Test
        @DisplayName("Deve falhar quando titular não existe na base de usuários")
        void deveFalharTitularNaoEncontrado() {
            // Arrange
            Unidade u = criarUnidadeValida(1L, "U1", TipoUnidade.OPERACIONAL);

            when(unidadeRepo.findAllWithHierarquia()).thenReturn(List.of(u));
            when(usuarioRepo.findAllById(List.of("TITULO_1"))).thenReturn(List.of()); // Usuário não existe

            // Act & Assert
            assertThatThrownBy(() -> validador.run(new DefaultApplicationArguments()))
                    .isInstanceOf(ErroConfiguracao.class)
                    .hasMessageContaining("1 violação");
        }

        @Test
        @DisplayName("Deve falhar quando titular não tem email")
        void deveFalharTitularSemEmail() {
            // Arrange
            Unidade u = criarUnidadeValida(1L, "U1", TipoUnidade.OPERACIONAL);
            Usuario titular = criarUsuarioValido("TITULO_1");
            titular.setEmail(null);

            when(unidadeRepo.findAllWithHierarquia()).thenReturn(List.of(u));
            when(usuarioRepo.findAllById(List.of("TITULO_1"))).thenReturn(List.of(titular));

            // Act & Assert
            assertThatThrownBy(() -> validador.run(new DefaultApplicationArguments()))
                    .isInstanceOf(ErroConfiguracao.class)
                    .hasMessageContaining("1 violação");
        }

        @Test
        @DisplayName("Deve falhar quando titular tem email em branco")
        void deveFalharTitularEmailEmBranco() {
            // Arrange
            Unidade u = criarUnidadeValida(1L, "U1", TipoUnidade.OPERACIONAL);
            Usuario titular = criarUsuarioValido("TITULO_1");
            titular.setEmail("   ");

            when(unidadeRepo.findAllWithHierarquia()).thenReturn(List.of(u));
            when(usuarioRepo.findAllById(List.of("TITULO_1"))).thenReturn(List.of(titular));

            // Act & Assert
            assertThatThrownBy(() -> validador.run(new DefaultApplicationArguments()))
                    .isInstanceOf(ErroConfiguracao.class);
        }

        @Test
        @DisplayName("Deve falhar quando intermediária não tem subordinadas")
        void deveFalharIntermediariaSemSubordinadas() {
            // Arrange
            Unidade intermediaria = criarUnidadeValida(1L, "INT", TipoUnidade.INTERMEDIARIA);
            Usuario titular = criarUsuarioValido("TITULO_1");

            when(unidadeRepo.findAllWithHierarquia()).thenReturn(List.of(intermediaria));
            when(usuarioRepo.findAllById(List.of("TITULO_1"))).thenReturn(List.of(titular));

            // Act & Assert
            assertThatThrownBy(() -> validador.run(new DefaultApplicationArguments()))
                    .isInstanceOf(ErroConfiguracao.class)
                    .hasMessageContaining("1 violação");
        }

        @Test
        @DisplayName("Deve acumular múltiplas violações")
        void deveAcumularMultiplasViolacoes() {
            // Arrange
            Unidade u1 = criarUnidadeValida(1L, "U1", TipoUnidade.OPERACIONAL);
            u1.setTituloTitular(null); // Violação 1

            Unidade u2 = criarUnidadeValida(2L, "U2", TipoUnidade.OPERACIONAL);
            u2.setTituloTitular(null); // Violação 2

            when(unidadeRepo.findAllWithHierarquia()).thenReturn(List.of(u1, u2));

            // Act & Assert
            assertThatThrownBy(() -> validador.run(new DefaultApplicationArguments()))
                    .isInstanceOf(ErroConfiguracao.class)
                    .hasMessageContaining("2 violações");
        }
    }
}
