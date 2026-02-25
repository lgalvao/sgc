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
import sgc.organizacao.service.ValidadorDadosOrgService;
import sgc.testutils.UnidadeTestBuilder;
import sgc.testutils.UsuarioTestBuilder;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do ValidadorDadosOrgService")
class ValidadorDadosOrgServiceTest {
    private final DefaultApplicationArguments args = new DefaultApplicationArguments();
    @Mock
    private UnidadeRepo unidadeRepo;
    @Mock
    private UsuarioRepo usuarioRepo;
    @Mock
    private ResponsabilidadeRepo responsabilidadeRepo;
    @InjectMocks
    private ValidadorDadosOrgService validador;

    private Unidade criarUnidadeValida(Long codigo, String sigla, TipoUnidade tipo) {
        return UnidadeTestBuilder.umaDe()
                .comCodigo(String.valueOf(codigo))
                .comSigla(sigla)
                .comTipo(tipo)
                .comTituloTitular("TITULO_" + codigo)
                .build();
    }

    private Usuario criarUsuarioValido(String titulo) {
        Usuario u = UsuarioTestBuilder.umDe()
                .comTitulo(titulo)
                .comNome("Nome " + titulo)
                .build();
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
            when(responsabilidadeRepo.findByUnidadeCodigoIn(anyList())).thenReturn(List.of(
                    Responsabilidade.builder().unidadeCodigo(1L).usuarioTitulo("TITULO_1").build()
            ));

            // Act & Assert
            assertThatCode(() -> validador.run(args)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Deve ignorar unidades inativas")
        void deveIgnorarUnidadesInativas() {
            // Arrange
            Unidade inativa = criarUnidadeValida(1L, "INATIVA", TipoUnidade.OPERACIONAL);
            inativa.setSituacao(SituacaoUnidade.INATIVA);
            inativa.setTituloTitular(""); // Sem titular, mas é inativa

            when(unidadeRepo.findAllWithHierarquia()).thenReturn(List.of(inativa));
            when(responsabilidadeRepo.findByUnidadeCodigoIn(anyList())).thenReturn(List.of());

            // Act & Assert
            assertThatCode(() -> validador.run(args)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Deve ignorar unidades SEM_EQUIPE e RAIZ")
        void deveIgnorarUnidadesNaoParticipantes() {
            // Arrange
            Unidade semEquipe = criarUnidadeValida(1L, "SEM", TipoUnidade.SEM_EQUIPE);
            semEquipe.setTituloTitular("");
            Unidade raiz = criarUnidadeValida(2L, "RAIZ", TipoUnidade.RAIZ);
            raiz.setTituloTitular("");

            when(unidadeRepo.findAllWithHierarquia()).thenReturn(List.of(semEquipe, raiz));
            when(responsabilidadeRepo.findByUnidadeCodigoIn(anyList())).thenReturn(List.of());

            // Act & Assert
            assertThatCode(() -> validador.run(args)).doesNotThrowAnyException();
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
            when(responsabilidadeRepo.findByUnidadeCodigoIn(anyList())).thenReturn(List.of(
                    Responsabilidade.builder().unidadeCodigo(1L).usuarioTitulo("TITULO_1").build(),
                    Responsabilidade.builder().unidadeCodigo(2L).usuarioTitulo("TITULO_2").build()
            ));

            // Act & Assert
            assertThatCode(() -> validador.run(args)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Deve lidar com lista de unidades sem titulares (não quebra e não gera erro na busca de usuarios, mas detecta violação)")
        void deveLidarComListaDeTitulosVazia() {
            // Arrange
            Unidade u = criarUnidadeValida(1L, "U1", TipoUnidade.OPERACIONAL);
            u.setTituloTitular(""); // Sem titular

            when(unidadeRepo.findAllWithHierarquia()).thenReturn(List.of(u));
            Responsabilidade r = Responsabilidade.builder()
                    .unidadeCodigo(1L)
                    .usuarioTitulo("OUTRO")
                    .build();
            when(responsabilidadeRepo.findByUnidadeCodigoIn(List.of(1L))).thenReturn(List.of(r));

            // Act & Assert
            assertThatThrownBy(() -> validador.run(args))
                    .isInstanceOf(ErroConfiguracao.class)
                    .hasMessageContaining("1 violação");
        }
        @Test
        @DisplayName("Deve lidar com duplicidade de usuários (merge function coverage)")
        void deveLidarComDuplicidadeUsuarios() {
            // Arrange
            Unidade u1 = criarUnidadeValida(1L, "U1", TipoUnidade.OPERACIONAL);
            Usuario titular1 = criarUsuarioValido("TITULO_1");
            Usuario titular2 = criarUsuarioValido("TITULO_1"); // Duplicado

            when(unidadeRepo.findAllWithHierarquia()).thenReturn(List.of(u1));
            when(usuarioRepo.findAllById(List.of("TITULO_1"))).thenReturn(List.of(titular1, titular2));
            when(responsabilidadeRepo.findByUnidadeCodigoIn(anyList())).thenReturn(List.of(
                    Responsabilidade.builder().unidadeCodigo(1L).usuarioTitulo("TITULO_1").build()
            ));

            // Act & Assert
            assertThatCode(() -> validador.run(args)).doesNotThrowAnyException();
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
            u.setTituloTitular("");

            when(unidadeRepo.findAllWithHierarquia()).thenReturn(List.of(u));
            // Fornece um responsável válido para não gerar uma segunda violação aqui
            when(responsabilidadeRepo.findByUnidadeCodigoIn(anyList())).thenReturn(List.of(
                    Responsabilidade.builder().unidadeCodigo(1L).usuarioTitulo("OUTRO").build()
            ));

            // Act & Assert
            assertThatThrownBy(() -> validador.run(args))
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
            Responsabilidade r = Responsabilidade.builder()
                    .unidadeCodigo(1L)
                    .usuarioTitulo("TITULO_1")
                    .build();
            when(responsabilidadeRepo.findByUnidadeCodigoIn(List.of(1L))).thenReturn(List.of(r));

            // Act & Assert
            assertThatThrownBy(() -> validador.run(args))
                    .isInstanceOf(ErroConfiguracao.class)
                    .hasMessageContaining("1 violação");
        }

        @Test
        @DisplayName("Deve falhar quando titular tem email em branco")
        void deveFailharTitularEmailEmBranco() {
            // Arrange
            Unidade u = criarUnidadeValida(1L, "U1", TipoUnidade.OPERACIONAL);
            Usuario titular = criarUsuarioValido("TITULO_1");
            titular.setEmail("   ");

            when(unidadeRepo.findAllWithHierarquia()).thenReturn(List.of(u));
            when(usuarioRepo.findAllById(List.of("TITULO_1"))).thenReturn(List.of(titular));
            Responsabilidade r = Responsabilidade.builder()
                    .unidadeCodigo(1L)
                    .usuarioTitulo("TITULO_1")
                    .build();
            when(responsabilidadeRepo.findByUnidadeCodigoIn(List.of(1L))).thenReturn(List.of(r));

            // Act & Assert
            assertThatThrownBy(() -> validador.run(args))
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
            when(responsabilidadeRepo.findByUnidadeCodigoIn(anyList())).thenReturn(List.of(
                    Responsabilidade.builder().unidadeCodigo(1L).usuarioTitulo("TITULO_1").build()
            ));

            // Act & Assert
            assertThatThrownBy(() -> validador.run(args))
                    .isInstanceOf(ErroConfiguracao.class)
                    .hasMessageContaining("1 violação");
        }

        @Test
        @DisplayName("Deve ignorar unidade sem titular no loop de validação de emails")
        void deveIgnorarUnidadeSemTitularNoLoopDeEmail() {
            // Arrange
            Unidade semTitular = criarUnidadeValida(2L, "SEM", TipoUnidade.OPERACIONAL);
            semTitular.setTituloTitular("");

            when(unidadeRepo.findAllWithHierarquia()).thenReturn(List.of(semTitular));
            Responsabilidade r = Responsabilidade.builder()
                    .unidadeCodigo(2L)
                    .usuarioTitulo("OUTRO")
                    .build();
            when(responsabilidadeRepo.findByUnidadeCodigoIn(List.of(2L))).thenReturn(List.of(r));

            // Act & Assert
            assertThatThrownBy(() -> validador.run(args))
                    .isInstanceOf(ErroConfiguracao.class)
                    .hasMessageContaining("1 violação");
        }

        @Test
        @DisplayName("Deve ignorar unidade com título em branco no loop de validação de emails")
        void deveIgnorarUnidadeComTituloEmBrancoNoLoopDeEmail() {
            Unidade uBranca = criarUnidadeValida(2L, "B", TipoUnidade.OPERACIONAL);
            uBranca.setTituloTitular("   ");

            when(unidadeRepo.findAllWithHierarquia()).thenReturn(List.of(uBranca));
            Responsabilidade r = Responsabilidade.builder()
                    .unidadeCodigo(2L)
                    .usuarioTitulo("OUTRO")
                    .build();
            when(responsabilidadeRepo.findByUnidadeCodigoIn(List.of(2L))).thenReturn(List.of(r));

            assertThatThrownBy(() -> validador.run(args))
                    .isInstanceOf(ErroConfiguracao.class)
                    .hasMessageContaining("1 violação");
        }

        @Test
        @DisplayName("Deve falhar com título vazio")
        void deveFalharComTituloNulo() {
            // Arrange
            Unidade u = criarUnidadeValida(1L, "U1", TipoUnidade.OPERACIONAL);
            u.setTituloTitular("");

            when(unidadeRepo.findAllWithHierarquia()).thenReturn(List.of(u));
            // Fornece um responsável válido para não gerar uma segunda violação aqui
            Responsabilidade r = Responsabilidade.builder()
                    .unidadeCodigo(1L)
                    .usuarioTitulo("OUTRO")
                    .build();
            when(responsabilidadeRepo.findByUnidadeCodigoIn(List.of(1L))).thenReturn(List.of(r));

            // Act & Assert
            assertThatThrownBy(() -> validador.run(args))
                    .isInstanceOf(ErroConfiguracao.class)
                    .hasMessageContaining("1 violação");
        }

        @Test
        @DisplayName("Deve limitar mensagem de erro quando houver muitas violações")
        void deveGerarExceptionComMuitasViolacoes() {
            // Arrange
            // Cria 4 unidades com problemas (sem titular)
            // Isso gerará pelo menos 4 violações (titular ausente) 
            // e possivelmente mais 4 (responsável atual ausente)
            List<Unidade> unidades = new ArrayList<>();
            for (long i = 1; i <= 4; i++) {
                Unidade u = criarUnidadeValida(i, "U" + i, TipoUnidade.OPERACIONAL);
                u.setTituloTitular(""); // Violação 1: Titular ausente
                unidades.add(u);
            }

            when(unidadeRepo.findAllWithHierarquia()).thenReturn(unidades);
            // Violação 2: Responsável atual ausente (mapa vazio)
            when(responsabilidadeRepo.findByUnidadeCodigoIn(anyList())).thenReturn(List.of());

            // Act & Assert
            // Deve conter a mensagem de resumo indicando o total de violações (8 neste caso)
            assertThatThrownBy(() -> validador.run(args))
                    .isInstanceOf(ErroConfiguracao.class)
                    .hasMessageContaining("violações encontradas")
                    .hasMessageContaining("+");
        }

        @Test
        @DisplayName("Deve falhar quando responsabilidade tem usuário em branco")
        void deveFalharResponsabilidadeUsuarioEmBranco() {
            // Arrange
            Unidade u = criarUnidadeValida(1L, "U1", TipoUnidade.OPERACIONAL);
            Usuario titular = criarUsuarioValido("TITULO_1");

            when(unidadeRepo.findAllWithHierarquia()).thenReturn(List.of(u));
            when(usuarioRepo.findAllById(anyList())).thenReturn(List.of(titular));

            // Responsabilidade presente mas com título em branco
            Responsabilidade r = Responsabilidade.builder()
                    .unidadeCodigo(1L)
                    .usuarioTitulo("   ")
                    .build();
            when(responsabilidadeRepo.findByUnidadeCodigoIn(anyList())).thenReturn(List.of(r));

            // Act & Assert
            assertThatThrownBy(() -> validador.run(args))
                    .isInstanceOf(ErroConfiguracao.class);
        }

        @Test
        @DisplayName("Deve falhar quando responsabilidade é nula")
        void deveFalharResponsabilidadeNula() {
            // Arrange
            Unidade u = criarUnidadeValida(1L, "U1", TipoUnidade.OPERACIONAL);
            Usuario titular = criarUsuarioValido("TITULO_1");

            when(unidadeRepo.findAllWithHierarquia()).thenReturn(List.of(u));
            when(usuarioRepo.findAllById(anyList())).thenReturn(List.of(titular));

            // Responsabilidades vazio -> r será nulo no loop
            when(responsabilidadeRepo.findByUnidadeCodigoIn(anyList())).thenReturn(List.of());

            // Act & Assert
            assertThatThrownBy(() -> validador.run(args))
                    .isInstanceOf(ErroConfiguracao.class);
        }
    }
}
