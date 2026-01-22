package sgc.analise;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.analise.dto.CriarAnaliseCommand;
import sgc.analise.model.Analise;
import sgc.analise.model.AnaliseRepo;
import sgc.analise.model.TipoAnalise;
import sgc.organizacao.UnidadeFacade;
import sgc.subprocesso.model.Subprocesso;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("Testes para AnaliseFacade")
class AnaliseFacadeTest {
    private static final String OBS = "Observação";

    @Mock
    private AnaliseRepo analiseRepo;

    @Mock
    private UnidadeFacade unidadeService;

    @InjectMocks
    private AnaliseFacade service;

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
            when(analiseRepo.findBySubprocessoCodigoOrderByDataHoraDesc(1L))
                    .thenReturn(List.of(analise));

            List<Analise> resultado = service.listarPorSubprocesso(1L, TipoAnalise.CADASTRO);

            assertThat(resultado)
                    .isNotEmpty()
                    .hasSize(1)
                    .first()
                    .extracting(Analise::getTipo)
                    .isEqualTo(TipoAnalise.CADASTRO);
            verify(analiseRepo).findBySubprocessoCodigoOrderByDataHoraDesc(1L);
        }

        @Test
        @DisplayName("Deve retornar lista de análises de validação")
        void deveRetornarListaDeAnalisesValidacao() {
            Analise analise = new Analise();
            analise.setTipo(TipoAnalise.VALIDACAO);
            when(analiseRepo.findBySubprocessoCodigoOrderByDataHoraDesc(1L))
                    .thenReturn(List.of(analise));

            List<Analise> resultado = service.listarPorSubprocesso(1L, TipoAnalise.VALIDACAO);

            assertThat(resultado)
                    .isNotEmpty()
                    .hasSize(1)
                    .first()
                    .extracting(Analise::getTipo)
                    .isEqualTo(TipoAnalise.VALIDACAO);
            verify(analiseRepo).findBySubprocessoCodigoOrderByDataHoraDesc(1L);
        }
    }

    @Nested
    @DisplayName("Testes para criarAnalise")
    class CriarAnalise {

        @Test
        @DisplayName("Deve criar uma análise de cadastro")
        void deveCriarAnaliseCadastro() {
            when(analiseRepo.save(any(Analise.class))).thenAnswer(i -> i.getArgument(0));

            Analise resultado = service.criarAnalise(
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
            verify(analiseRepo).save(any(Analise.class));
        }

        @Test
        @DisplayName("Deve criar uma análise de validação")
        void deveCriarAnaliseValidacao() {
            when(analiseRepo.save(any(Analise.class))).thenAnswer(i -> i.getArgument(0));

            Analise resultado = service.criarAnalise(
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
            verify(analiseRepo).save(any(Analise.class));
        }

        @Test
        @DisplayName("Deve criar uma análise com sigla de unidade")
        void deveCriarAnaliseComSiglaUnidade() {
            String sigla = "UNIDADE1";
            sgc.organizacao.dto.UnidadeDto unidadeDto = new sgc.organizacao.dto.UnidadeDto();
            unidadeDto.setCodigo(10L);
            sgc.organizacao.model.Unidade unidade = new sgc.organizacao.model.Unidade();
            unidade.setCodigo(10L);

            when(unidadeService.buscarPorSigla(sigla)).thenReturn(unidadeDto);
            when(unidadeService.buscarEntidadePorId(10L)).thenReturn(unidade);
            when(analiseRepo.save(any(Analise.class))).thenAnswer(i -> i.getArgument(0));

            Analise resultado = service.criarAnalise(
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
            Analise analise = new Analise();
            List<Analise> analises = List.of(analise);
            when(analiseRepo.findBySubprocessoCodigo(1L)).thenReturn(analises);

            service.removerPorSubprocesso(1L);

            verify(analiseRepo).findBySubprocessoCodigo(1L);
            verify(analiseRepo).deleteAll(analises);
        }

        @Test
        @DisplayName("Não deve tentar remover se lista estiver vazia")
        void naoDeveRemoverSeListaVazia() {
            when(analiseRepo.findBySubprocessoCodigo(1L)).thenReturn(Collections.emptyList());

            service.removerPorSubprocesso(1L);

            verify(analiseRepo).findBySubprocessoCodigo(1L);
            verify(analiseRepo, never()).deleteAll(any());
        }
    }
}
