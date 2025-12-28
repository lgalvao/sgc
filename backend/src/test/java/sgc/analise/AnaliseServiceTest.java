package sgc.analise;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.analise.dto.CriarAnaliseRequest;
import sgc.analise.model.Analise;
import sgc.analise.model.AnaliseRepo;
import sgc.analise.model.TipoAnalise;
import sgc.subprocesso.model.Subprocesso;
import sgc.unidade.service.UnidadeService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para AnaliseService")
class AnaliseServiceTest {
    private static final String OBS = "Observação";

    @Mock
    private AnaliseRepo analiseRepo;

    @Mock
    private UnidadeService unidadeService;

    @InjectMocks
    private AnaliseService service;

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

        // Teste de exceção "deveLancarExcecaoSeSubprocessoNaoEncontrado" removido pois a validação foi movida para o Controller
    }

    @Nested
    @DisplayName("Testes para criarAnalise")
    class CriarAnalise {

        @Test
        @DisplayName("Deve criar uma análise de cadastro")
        void deveCriarAnaliseCadastro() {
            when(analiseRepo.save(any(Analise.class))).thenAnswer(i -> i.getArgument(0));

            Analise resultado =
                    service.criarAnalise(
                            subprocesso,
                            CriarAnaliseRequest.builder()
                                    .codSubprocesso(1L)
                                    .observacoes(OBS)
                                    .tipo(TipoAnalise.CADASTRO)
                                    .acao(null)
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

            Analise resultado =
                    service.criarAnalise(
                            subprocesso,
                            CriarAnaliseRequest.builder()
                                    .codSubprocesso(1L)
                                    .observacoes(OBS)
                                    .tipo(TipoAnalise.VALIDACAO)
                                    .acao(null)
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

        // Teste de exceção "deveLancarExcecaoSeSubprocessoNaoEncontradoAoCriar" removido pois o serviço não busca mais o subprocesso
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
    }
}
