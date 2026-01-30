package sgc.analise;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.analise.dto.CriarAnaliseCommand;
import sgc.analise.model.Analise;
import sgc.analise.model.TipoAnalise;
import sgc.organizacao.UnidadeFacade;
import sgc.subprocesso.model.Subprocesso;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import sgc.organizacao.dto.UnidadeDto;
import sgc.organizacao.model.Unidade;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("Testes para AnaliseFacade")
class AnaliseFacadeTest {
    private static final String OBS = "Observação";

    @Mock
    private AnaliseService analiseService;

    @Mock
    private UnidadeFacade unidadeService;

    @InjectMocks
    private AnaliseFacade facade;

    private Subprocesso subprocesso;

    @BeforeEach
    void setUp() {
        subprocesso = new Subprocesso();
        subprocesso.setCodigo(1L);
    }

    @Nested
    @DisplayName("Testes para listarPorSubprocesso")
    class ListarPorSubprocesso {
        @Test
        @DisplayName("Deve retornar lista de análises de cadastro")
        void deveRetornarListaDeAnalisesCadastro() {
            Analise analise = new Analise();
            analise.setTipo(TipoAnalise.CADASTRO);
            when(analiseService.listarPorSubprocesso(1L))
                    .thenReturn(List.of(analise));

            List<Analise> resultado = facade.listarPorSubprocesso(1L, TipoAnalise.CADASTRO);

            assertThat(resultado)
                    .isNotEmpty()
                    .hasSize(1)
                    .first()
                    .extracting(Analise::getTipo)
                    .isEqualTo(TipoAnalise.CADASTRO);
            verify(analiseService).listarPorSubprocesso(1L);
        }

        @Test
        @DisplayName("Deve retornar lista de análises de validação")
        void deveRetornarListaDeAnalisesValidacao() {
            Analise analise = new Analise();
            analise.setTipo(TipoAnalise.VALIDACAO);
            when(analiseService.listarPorSubprocesso(1L))
                    .thenReturn(List.of(analise));

            List<Analise> resultado = facade.listarPorSubprocesso(1L, TipoAnalise.VALIDACAO);

            assertThat(resultado)
                    .isNotEmpty()
                    .hasSize(1)
                    .first()
                    .extracting(Analise::getTipo)
                    .isEqualTo(TipoAnalise.VALIDACAO);
            verify(analiseService).listarPorSubprocesso(1L);
        }

        @Test
        @DisplayName("Deve filtrar análises de outro tipo")
        void deveFiltrarAnalisesDeOutroTipo() {
            Analise analiseCadastro = new Analise();
            analiseCadastro.setTipo(TipoAnalise.CADASTRO);
            Analise analiseValidacao = new Analise();
            analiseValidacao.setTipo(TipoAnalise.VALIDACAO);

            when(analiseService.listarPorSubprocesso(1L))
                    .thenReturn(List.of(analiseCadastro, analiseValidacao));

            List<Analise> resultado = facade.listarPorSubprocesso(1L, TipoAnalise.CADASTRO);

            assertThat(resultado)
                    .hasSize(1)
                    .extracting(Analise::getTipo)
                    .containsExactly(TipoAnalise.CADASTRO);
        }
    }

    @Nested
    @DisplayName("Testes para criarAnalise")
    class CriarAnalise {
        @Test
        @DisplayName("Deve criar uma análise de cadastro")
        void deveCriarAnaliseCadastro() {
            when(analiseService.salvar(any(Analise.class))).thenAnswer(i -> i.getArgument(0));

            Analise resultado = facade.criarAnalise(
                    subprocesso,
                    CriarAnaliseCommand.builder()
                            .tipo(TipoAnalise.CADASTRO)
                            .acao(null)
                            .observacoes(OBS)
                            .siglaUnidade(null)
                            .tituloUsuario(null)
                            .motivo(null)
                            .build());

            assertThat(resultado).isNotNull();
            assertThat(resultado.getSubprocesso()).isEqualTo(subprocesso);
            assertThat(resultado.getObservacoes()).isEqualTo(OBS);
            assertThat(resultado.getTipo()).isEqualTo(TipoAnalise.CADASTRO);
            verify(analiseService).salvar(any(Analise.class));
        }

        @Test
        @DisplayName("Deve criar uma análise de validação")
        void deveCriarAnaliseValidacao() {
            when(analiseService.salvar(any(Analise.class))).thenAnswer(i -> i.getArgument(0));

            Analise resultado = facade.criarAnalise(
                    subprocesso,
                    CriarAnaliseCommand.builder()
                            .tipo(TipoAnalise.VALIDACAO)
                            .acao(null)
                            .observacoes(OBS)
                            .siglaUnidade(null)
                            .tituloUsuario(null)
                            .motivo(null)
                            .build());

            assertThat(resultado).isNotNull();
            assertThat(resultado.getSubprocesso()).isEqualTo(subprocesso);
            assertThat(resultado.getObservacoes()).isEqualTo(OBS);
            assertThat(resultado.getTipo()).isEqualTo(TipoAnalise.VALIDACAO);
            verify(analiseService).salvar(any(Analise.class));
        }

        @Test
        @DisplayName("Deve criar uma análise com sigla de unidade")
        void deveCriarAnaliseComSiglaUnidade() {
            String sigla = "UNIDADE1";
            UnidadeDto unidadeDto = new UnidadeDto();
            unidadeDto.setCodigo(10L);
            Unidade unidade = new Unidade();
            unidade.setCodigo(10L);

            when(unidadeService.buscarPorSigla(sigla)).thenReturn(unidadeDto);
            when(unidadeService.buscarEntidadePorId(10L)).thenReturn(unidade);
            when(analiseService.salvar(any(Analise.class))).thenAnswer(i -> i.getArgument(0));

            Analise resultado = facade.criarAnalise(
                    subprocesso,
                    CriarAnaliseCommand.builder()
                            .tipo(TipoAnalise.VALIDACAO)
                            .siglaUnidade(sigla)
                            .build());

            assertThat(resultado.getUnidadeCodigo()).isEqualTo(10L);
            verify(unidadeService).buscarPorSigla(sigla);
            verify(unidadeService).buscarEntidadePorId(10L);
        }
    }

    @Nested
    @DisplayName("Testes para removerPorSubprocesso")
    class RemoverPorSubprocesso {
        @Test
        @DisplayName("Deve remover análises por subprocesso")
        void deveRemoverAnalisesPorSubprocesso() {
            facade.removerPorSubprocesso(1L);

            verify(analiseService).removerPorSubprocesso(1L);
        }
    }
}
