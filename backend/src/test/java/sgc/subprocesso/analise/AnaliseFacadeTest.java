package sgc.subprocesso.analise;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.organizacao.OrganizacaoFacade;
import sgc.subprocesso.AnaliseFacade;
import sgc.subprocesso.model.Analise;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.TipoAnalise;
import sgc.subprocesso.service.AnaliseService;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para AnaliseFacade")
class AnaliseFacadeTest {
    @Mock
    private AnaliseService analiseService;

    @Mock
    private OrganizacaoFacade unidadeService;

    @InjectMocks
    private AnaliseFacade facade;

    @BeforeEach
    void setUp() {
        Subprocesso subprocesso = new Subprocesso();
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

        @Test
        @DisplayName("Deve retornar lista vazia quando não há análises do tipo solicitado")
        void deveRetornarListaVaziaQuandoNaoHaAnalisesDoTipo() {
            // Pattern 1: Empty list validation
            when(analiseService.listarPorSubprocesso(1L))
                    .thenReturn(Collections.emptyList());

            List<Analise> resultado = facade.listarPorSubprocesso(1L, TipoAnalise.CADASTRO);

            assertThat(resultado)
                    .isNotNull()
                    .isEmpty();
            verify(analiseService).listarPorSubprocesso(1L);
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
