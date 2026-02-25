package sgc.subprocesso.analise;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.subprocesso.AnaliseFacade;
import sgc.subprocesso.model.Analise;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.TipoAnalise;
import sgc.subprocesso.service.SubprocessoService;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para AnaliseFacade")
class AnaliseFacadeTest {
    @Mock
    private SubprocessoService subprocessoService;

    @InjectMocks
    private AnaliseFacade facade;

    @BeforeEach
    void setUp() {
    }

    @Nested
    @DisplayName("Testes para listarPorSubprocesso")
    class ListarPorSubprocesso {
        @Test
        @DisplayName("Deve delegar para SubprocessoService")
        void deveDelegarParaSubprocessoService() {
            Analise analise = new Analise();
            analise.setTipo(TipoAnalise.CADASTRO);
            when(subprocessoService.listarAnalisesPorSubprocesso(1L, TipoAnalise.CADASTRO))
                    .thenReturn(List.of(analise));

            List<Analise> resultado = facade.listarPorSubprocesso(1L, TipoAnalise.CADASTRO);

            assertThat(resultado)
                    .isNotEmpty()
                    .hasSize(1)
                    .first()
                    .isEqualTo(analise);
            verify(subprocessoService).listarAnalisesPorSubprocesso(1L, TipoAnalise.CADASTRO);
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando serviço retorna vazio")
        void deveRetornarListaVaziaQuandoServicoRetornaVazio() {
            when(subprocessoService.listarAnalisesPorSubprocesso(1L, TipoAnalise.CADASTRO))
                    .thenReturn(Collections.emptyList());

            List<Analise> resultado = facade.listarPorSubprocesso(1L, TipoAnalise.CADASTRO);

            assertThat(resultado)
                    .isNotNull()
                    .isEmpty();
        }
    }

    @Nested
    @DisplayName("Testes para removerPorSubprocesso")
    class RemoverPorSubprocesso {
        @Test
        @DisplayName("Deve remover análises por subprocesso")
        void deveRemoverAnalisesPorSubprocesso() {
            facade.removerPorSubprocesso(1L);

            verify(subprocessoService).removerAnalisesPorSubprocesso(1L);
        }
    }
}
