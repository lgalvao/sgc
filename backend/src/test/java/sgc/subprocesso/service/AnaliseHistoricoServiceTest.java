package sgc.subprocesso.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.organizacao.model.*;
import sgc.organizacao.service.UnidadeService;
import sgc.subprocesso.dto.AnaliseHistoricoDto;
import sgc.subprocesso.model.*;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AnaliseHistoricoService - Testes Unitários")
class AnaliseHistoricoServiceTest {

    @Mock
    private UnidadeService unidadeService;

    @InjectMocks
    private AnaliseHistoricoService analiseHistoricoService;

    @Test
    @DisplayName("deve retornar lista vazia quando nenhuma análise for fornecida")
    void deveRetornarListaVaziaQuandoNenhumaAnaliseForFornecida() {
        List<AnaliseHistoricoDto> resultado = analiseHistoricoService.converterLista(List.of());

        assertThat(resultado).isEmpty();
        verifyNoInteractions(unidadeService);
    }

    @Test
    @DisplayName("deve converter uma análise corretamente")
    void deveConverterUmaAnaliseCorretamente() {
        Analise analise = Analise.builder()
                .unidadeCodigo(10L)
                .dataHora(LocalDateTime.of(2025, 4, 10, 10, 0))
                .observacoes("Tudo certo")
                .acao(TipoAcaoAnalise.ACEITE_MAPEAMENTO)
                .usuarioTitulo("analista1")
                .motivo("Motivo A")
                .tipo(TipoAnalise.CADASTRO)
                .build();

        UnidadeResumoLeitura unidade = new UnidadeResumoLeitura(10L, "Unidade Teste", "UT", TipoUnidade.OPERACIONAL);
        when(unidadeService.buscarResumosPorCodigos(List.of(10L))).thenReturn(List.of(unidade));

        AnaliseHistoricoDto dto = analiseHistoricoService.converter(analise);

        assertThat(dto.dataHora()).isEqualTo(LocalDateTime.of(2025, 4, 10, 10, 0));
        assertThat(dto.observacoes()).isEqualTo("Tudo certo");
        assertThat(dto.acao()).isEqualTo(TipoAcaoAnalise.ACEITE_MAPEAMENTO);
        assertThat(dto.unidadeSigla()).isEqualTo("UT");
        assertThat(dto.unidadeNome()).isEqualTo("Unidade Teste");
        assertThat(dto.analistaUsuarioTitulo()).isEqualTo("analista1");
        assertThat(dto.motivo()).isEqualTo("Motivo A");
        assertThat(dto.tipo()).isEqualTo(TipoAnalise.CADASTRO);
    }

    @Test
    @DisplayName("deve converter múltiplas análises corretamente")
    void deveConverterMultiplasAnalisesCorretamente() {
        Analise analise1 = Analise.builder()
                .unidadeCodigo(10L)
                .dataHora(LocalDateTime.of(2025, 4, 10, 10, 0))
                .acao(TipoAcaoAnalise.ACEITE_MAPEAMENTO)
                .usuarioTitulo("analista1")
                .tipo(TipoAnalise.CADASTRO)
                .build();

        Analise analise2 = Analise.builder()
                .unidadeCodigo(20L)
                .dataHora(LocalDateTime.of(2025, 4, 11, 10, 0))
                .acao(TipoAcaoAnalise.DEVOLUCAO_MAPEAMENTO)
                .usuarioTitulo("analista2")
                .tipo(TipoAnalise.VALIDACAO)
                .build();

        UnidadeResumoLeitura unidade1 = new UnidadeResumoLeitura(10L, "Unidade Teste 1", "UT1", TipoUnidade.OPERACIONAL);
        UnidadeResumoLeitura unidade2 = new UnidadeResumoLeitura(20L, "Unidade Teste 2", "UT2", TipoUnidade.INTERMEDIARIA);

        when(unidadeService.buscarResumosPorCodigos(argThat(lista -> lista.containsAll(List.of(10L, 20L)))))
                .thenReturn(List.of(unidade1, unidade2));

        List<AnaliseHistoricoDto> resultado = analiseHistoricoService.converterLista(List.of(analise1, analise2));

        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).unidadeSigla()).isEqualTo("UT1");
        assertThat(resultado.get(1).unidadeSigla()).isEqualTo("UT2");
    }

    @Test
    @DisplayName("deve lançar exceção quando a unidade da análise não for encontrada")
    void deveLancarExcecaoQuandoUnidadeNaoForEncontrada() {
        Analise analise = Analise.builder()
                .unidadeCodigo(99L)
                .build();

        when(unidadeService.buscarResumosPorCodigos(List.of(99L))).thenReturn(List.of());

        assertThatThrownBy(() -> analiseHistoricoService.converter(analise))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Unidade 99 ausente no histórico de análises");
    }
}
