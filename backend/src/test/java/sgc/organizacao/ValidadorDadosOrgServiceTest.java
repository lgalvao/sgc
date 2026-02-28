package sgc.organizacao;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.springframework.boot.*;
import sgc.comum.erros.*;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;
import sgc.testutils.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do ValidadorDadosOrgService")
class ValidadorDadosOrgServiceTest {
    private final DefaultApplicationArguments args = new DefaultApplicationArguments();
    @Mock
    private UnidadeRepo unidadeRepo;
    @Mock
    private UsuarioRepo usuarioRepo;
    @InjectMocks
    private ValidadorDadosOrgService validador;

    private Unidade criarUnidadeValida(Long codigo, String sigla, TipoUnidade tipo) {
        Unidade u = UnidadeTestBuilder.umaDe()
                .comCodigo(String.valueOf(codigo))
                .comSigla(sigla)
                .comTipo(tipo)
                .comTituloTitular("TITULO_" + codigo)
                .build();

        u.setResponsabilidade(Responsabilidade.builder()
                .unidadeCodigo(codigo)
                .usuarioTitulo("TITULO_" + codigo)
                .build());

        return u;
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

            Unidade u1 = criarUnidadeValida(1L, "U1", TipoUnidade.OPERACIONAL);
            Usuario titular = criarUsuarioValido("TITULO_1");

            when(unidadeRepo.findBySituacaoAtivaAndTipoIn(anySet())).thenReturn(List.of(u1));
            when(usuarioRepo.findAllById(List.of("TITULO_1"))).thenReturn(List.of(titular));

            // Act & Assert
            assertThatCode(() -> validador.run(args)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Deve ignorar unidades inativas")
        void deveIgnorarUnidadesInativas() {

            // Como o repositório agora filtra por situação ATIVA no banco,
            // unidades inativas não seriam retornadas pelo método simulado.
            when(unidadeRepo.findBySituacaoAtivaAndTipoIn(anySet())).thenReturn(List.of());

            // Act & Assert
            assertThatCode(() -> validador.run(args)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Deve ignorar unidades SEM_EQUIPE e RAIZ")
        void deveIgnorarUnidadesNaoParticipantes() {

            // Como o repositório agora filtra por tipo participante no banco,
            // unidades SEM_EQUIPE e RAIZ não seriam retornadas.
            when(unidadeRepo.findBySituacaoAtivaAndTipoIn(anySet())).thenReturn(List.of());

            // Act & Assert
            assertThatCode(() -> validador.run(args)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Deve validar intermediária com subordinadas")
        void deveValidarIntermediariaComSubordinadas() {

            Unidade pai = criarUnidadeValida(1L, "PAI", TipoUnidade.INTERMEDIARIA);
            Unidade filha = criarUnidadeValida(2L, "FILHA", TipoUnidade.OPERACIONAL);
            filha.setUnidadeSuperior(pai);

            Usuario titularPai = criarUsuarioValido("TITULO_1");
            Usuario titularFilha = criarUsuarioValido("TITULO_2");

            when(unidadeRepo.findBySituacaoAtivaAndTipoIn(anySet())).thenReturn(List.of(pai, filha));
            when(usuarioRepo.findAllById(List.of("TITULO_1", "TITULO_2")))
                    .thenReturn(List.of(titularPai, titularFilha));

            // Act & Assert
            assertThatCode(() -> validador.run(args)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Deve lidar com lista de unidades sem titulares (não quebra e não gera erro na busca de usuarios, mas detecta violação)")
        void deveLidarComListaDeTitulosVazia() {

            Unidade u = criarUnidadeValida(1L, "U1", TipoUnidade.OPERACIONAL);
            u.setTituloTitular(""); // Sem titular

            when(unidadeRepo.findBySituacaoAtivaAndTipoIn(anySet())).thenReturn(List.of(u));

            // Act & Assert
            assertThatThrownBy(() -> validador.run(args))
                    .isInstanceOf(ErroConfiguracao.class)
                    .hasMessageContaining("1 violação");
        }

        @Test
        @DisplayName("Deve lidar com duplicidade de usuários (merge function coverage)")
        void deveLidarComDuplicidadeUsuarios() {

            Unidade u1 = criarUnidadeValida(1L, "U1", TipoUnidade.OPERACIONAL);
            Usuario titular1 = criarUsuarioValido("TITULO_1");
            Usuario titular2 = criarUsuarioValido("TITULO_1"); // Duplicado

            when(unidadeRepo.findBySituacaoAtivaAndTipoIn(anySet())).thenReturn(List.of(u1));
            when(usuarioRepo.findAllById(List.of("TITULO_1"))).thenReturn(List.of(titular1, titular2));

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

            Unidade u = criarUnidadeValida(1L, "U1", TipoUnidade.OPERACIONAL);
            u.setTituloTitular("");

            when(unidadeRepo.findBySituacaoAtivaAndTipoIn(anySet())).thenReturn(List.of(u));

            // Act & Assert
            assertThatThrownBy(() -> validador.run(args))
                    .isInstanceOf(ErroConfiguracao.class)
                    .hasMessageContaining("1 violação");
        }

        @Test
        @DisplayName("Deve falhar quando titular não existe na base de usuários")
        void deveFalharTitularNaoEncontrado() {

            Unidade u = criarUnidadeValida(1L, "U1", TipoUnidade.OPERACIONAL);

            when(unidadeRepo.findBySituacaoAtivaAndTipoIn(anySet())).thenReturn(List.of(u));
            when(usuarioRepo.findAllById(List.of("TITULO_1"))).thenReturn(List.of()); // Usuário não existe

            // Act & Assert
            assertThatThrownBy(() -> validador.run(args))
                    .isInstanceOf(ErroConfiguracao.class)
                    .hasMessageContaining("1 violação");
        }

        @Test
        @DisplayName("Deve falhar quando titular tem email em branco")
        void deveFailharTitularEmailEmBranco() {

            Unidade u = criarUnidadeValida(1L, "U1", TipoUnidade.OPERACIONAL);
            Usuario titular = criarUsuarioValido("TITULO_1");
            titular.setEmail("   ");

            when(unidadeRepo.findBySituacaoAtivaAndTipoIn(anySet())).thenReturn(List.of(u));
            when(usuarioRepo.findAllById(List.of("TITULO_1"))).thenReturn(List.of(titular));

            // Act & Assert
            assertThatThrownBy(() -> validador.run(args))
                    .isInstanceOf(ErroConfiguracao.class);
        }

        @Test
        @DisplayName("Deve falhar quando intermediária não tem subordinadas")
        void deveFalharIntermediariaSemSubordinadas() {

            Unidade intermediaria = criarUnidadeValida(1L, "INT", TipoUnidade.INTERMEDIARIA);
            Usuario titular = criarUsuarioValido("TITULO_1");

            when(unidadeRepo.findBySituacaoAtivaAndTipoIn(anySet())).thenReturn(List.of(intermediaria));
            when(usuarioRepo.findAllById(List.of("TITULO_1"))).thenReturn(List.of(titular));

            // Act & Assert
            assertThatThrownBy(() -> validador.run(args))
                    .isInstanceOf(ErroConfiguracao.class)
                    .hasMessageContaining("1 violação");
        }

        @Test
        @DisplayName("Deve ignorar unidade sem titular no loop de validação de emails")
        void deveIgnorarUnidadeSemTitularNoLoopDeEmail() {

            Unidade semTitular = criarUnidadeValida(2L, "SEM", TipoUnidade.OPERACIONAL);
            semTitular.setTituloTitular("");

            when(unidadeRepo.findBySituacaoAtivaAndTipoIn(anySet())).thenReturn(List.of(semTitular));

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

            when(unidadeRepo.findBySituacaoAtivaAndTipoIn(anySet())).thenReturn(List.of(uBranca));

            assertThatThrownBy(() -> validador.run(args))
                    .isInstanceOf(ErroConfiguracao.class)
                    .hasMessageContaining("1 violação");
        }

        @Test
        @DisplayName("Deve falhar com título vazio")
        void deveFalharComTituloNulo() {

            Unidade u = criarUnidadeValida(1L, "U1", TipoUnidade.OPERACIONAL);
            u.setTituloTitular("");

            when(unidadeRepo.findBySituacaoAtivaAndTipoIn(anySet())).thenReturn(List.of(u));

            // Act & Assert
            assertThatThrownBy(() -> validador.run(args))
                    .isInstanceOf(ErroConfiguracao.class)
                    .hasMessageContaining("1 violação");
        }

        @Test
        @DisplayName("Deve limitar mensagem de erro quando houver muitas violações")
        void deveGerarExceptionComMuitasViolacoes() {

            List<Unidade> unidades = new ArrayList<>();
            for (long i = 1; i <= 4; i++) {
                Unidade u = criarUnidadeValida(i, "U" + i, TipoUnidade.OPERACIONAL);
                u.setTituloTitular(""); // Violação 1: Titular ausente
                // Forçamos a responsabilidade ser nula para gerar Violação 2
                u.setResponsabilidade(null);
                unidades.add(u);
            }

            when(unidadeRepo.findBySituacaoAtivaAndTipoIn(anySet())).thenReturn(unidades);

            // Act & Assert
            assertThatThrownBy(() -> validador.run(args))
                    .isInstanceOf(ErroConfiguracao.class)
                    .hasMessageContaining("violações encontradas")
                    .hasMessageContaining("+");
        }

        @Test
        @DisplayName("Deve falhar quando responsabilidade tem usuário em branco")
        void deveFalharResponsabilidadeUsuarioEmBranco() {

            Unidade u = criarUnidadeValida(1L, "U1", TipoUnidade.OPERACIONAL);
            Usuario titular = criarUsuarioValido("TITULO_1");

            when(unidadeRepo.findBySituacaoAtivaAndTipoIn(anySet())).thenReturn(List.of(u));
            when(usuarioRepo.findAllById(anyList())).thenReturn(List.of(titular));

            // Responsabilidade presente mas com título em branco
            u.getResponsabilidade().setUsuarioTitulo("   ");

            // Act & Assert
            assertThatThrownBy(() -> validador.run(args))
                    .isInstanceOf(ErroConfiguracao.class);
        }

        @Test
        @DisplayName("Deve falhar quando responsabilidade é nula")
        void deveFalharResponsabilidadeNula() {

            Unidade u = criarUnidadeValida(1L, "U1", TipoUnidade.OPERACIONAL);
            Usuario titular = criarUsuarioValido("TITULO_1");

            when(unidadeRepo.findBySituacaoAtivaAndTipoIn(anySet())).thenReturn(List.of(u));
            when(usuarioRepo.findAllById(anyList())).thenReturn(List.of(titular));

            // Responsabilidade nula
            u.setResponsabilidade(null);

            // Act & Assert
            assertThatThrownBy(() -> validador.run(args))
                    .isInstanceOf(ErroConfiguracao.class);
        }
    }
}
