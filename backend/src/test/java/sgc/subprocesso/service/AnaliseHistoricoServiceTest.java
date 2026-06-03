package sgc.subprocesso.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.model.*;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AnaliseHistoricoService - Testes Unitários")
class AnaliseHistoricoServiceTest {

    @Mock
    private UnidadeService unidadeService;
    @Mock
    private UsuarioService usuarioService;

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
        when(usuarioService.buscarConsultasPorTitulos(anyCollection()))
                .thenReturn(List.of(new UsuarioConsultaLeitura("analista1", "mat1", "Analista Um", "email", "ramal", 10L, "UT", "UT", TipoUnidade.OPERACIONAL, "tit1", 10L)));

        AnaliseHistoricoDto dto = analiseHistoricoService.converter(analise);

        assertThat(dto.dataHora()).isEqualTo(LocalDateTime.of(2025, 4, 10, 10, 0));
        assertThat(dto.observacoes()).isEqualTo("Tudo certo");
        assertThat(dto.acao()).isEqualTo(TipoAcaoAnalise.ACEITE_MAPEAMENTO.name());
        assertThat(dto.acaoDescricao()).isEqualTo("Aceite");
        assertThat(dto.unidadeSigla()).isEqualTo("UT");
        assertThat(dto.unidadeNome()).isEqualTo("Unidade Teste");
        assertThat(dto.analistaUsuarioTitulo()).isEqualTo("analista1");
        assertThat(dto.usuarioNome()).isEqualTo("Analista Um");
        assertThat(dto.motivo()).isEqualTo("Motivo A");
        assertThat(dto.tipo()).isEqualTo(TipoAnalise.CADASTRO.name());
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
        when(usuarioService.buscarConsultasPorTitulos(anyCollection()))
                .thenReturn(List.of(
                        new UsuarioConsultaLeitura("analista1", "mat1", "Analista Um", "email", "ramal", 10L, "UT1", "UT1", TipoUnidade.OPERACIONAL, "tit1", 10L),
                        new UsuarioConsultaLeitura("analista2", "mat2", "Analista Dois", "email", "ramal", 20L, "UT2", "UT2", TipoUnidade.INTERMEDIARIA, "tit2", 20L)
                ));

        List<AnaliseHistoricoDto> resultado = analiseHistoricoService.converterLista(List.of(analise1, analise2));

        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).unidadeSigla()).isEqualTo("UT1");
        assertThat(resultado.get(0).usuarioNome()).isEqualTo("Analista Um");
        assertThat(resultado.get(1).unidadeSigla()).isEqualTo("UT2");
        assertThat(resultado.get(1).acaoDescricao()).isEqualTo("Devolução");
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

    @Test
    @DisplayName("deve lançar exceção quando o nome do analista for nulo ou em branco")
    void deveLancarExcecaoQuandoNomeAnalistaForBrancoOuNulo() {
        Analise analise = Analise.builder()
                .unidadeCodigo(10L)
                .usuarioTitulo("analista1")
                .build();

        UnidadeResumoLeitura unidade = new UnidadeResumoLeitura(10L, "Unidade Teste", "UT", TipoUnidade.OPERACIONAL);
        when(unidadeService.buscarResumosPorCodigos(List.of(10L))).thenReturn(List.of(unidade));

        when(usuarioService.buscarConsultasPorTitulos(anyCollection()))
                .thenReturn(List.of(new UsuarioConsultaLeitura("analista1", "mat1", "   ", "email", "ramal", 10L, "UT", "UT", TipoUnidade.OPERACIONAL, "tit1", 10L)));

        assertThatThrownBy(() -> analiseHistoricoService.converter(analise))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Usuário analista1 ausente ou sem nome no histórico de análises");
    }

    @Test
    @DisplayName("deve lidar com duplicidades de consultas de usuários na merge function do toMap")
    void deveLidarComDuplicidadesDeConsultasDeUsuarios() {
        Analise analise = Analise.builder()
                .unidadeCodigo(10L)
                .tipo(TipoAnalise.CADASTRO)
                .acao(TipoAcaoAnalise.ACEITE_MAPEAMENTO)
                .usuarioTitulo("analista1")
                .build();

        UnidadeResumoLeitura unidade = new UnidadeResumoLeitura(10L, "Unidade Teste", "UT", TipoUnidade.OPERACIONAL);
        when(unidadeService.buscarResumosPorCodigos(List.of(10L))).thenReturn(List.of(unidade));

        UsuarioConsultaLeitura u1 = new UsuarioConsultaLeitura("analista1", "mat1", "Nome A", "email", "ramal", 10L, "UT", "UT", TipoUnidade.OPERACIONAL, "tit1", 10L);
        UsuarioConsultaLeitura u2 = new UsuarioConsultaLeitura("analista1", "mat1", "Nome B", "email", "ramal", 10L, "UT", "UT", TipoUnidade.OPERACIONAL, "tit1", 10L);
        when(usuarioService.buscarConsultasPorTitulos(anyCollection()))
                .thenReturn(List.of(u1, u2));

        AnaliseHistoricoDto dto = analiseHistoricoService.converter(analise);

        assertThat(dto.usuarioNome()).isEqualTo("Nome A");
    }
}
