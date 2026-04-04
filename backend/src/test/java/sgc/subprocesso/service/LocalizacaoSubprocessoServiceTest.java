package sgc.subprocesso.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import sgc.comum.erros.*;
import sgc.organizacao.model.*;
import sgc.subprocesso.model.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LocalizacaoSubprocessoService")
class LocalizacaoSubprocessoServiceTest {

    @Mock
    private MovimentacaoRepo movimentacaoRepo;

    @InjectMocks
    private LocalizacaoSubprocessoService service;

    @Test
    @DisplayName("deve usar a unidade do subprocesso quando ele ainda nao foi persistido")
    void deveUsarUnidadeQuandoSubprocessoNaoPersistido() {
        Unidade unidade = Unidade.builder().codigo(10L).build();
        Subprocesso subprocesso = Subprocesso.builder()
                .unidade(unidade)
                .situacao(SituacaoSubprocesso.NAO_INICIADO)
                .build();

        Unidade localizacao = service.obterLocalizacaoAtual(subprocesso);

        assertThat(localizacao).isEqualTo(unidade);
        verifyNoInteractions(movimentacaoRepo);
    }

    @Test
    @DisplayName("deve usar o destino da ultima movimentacao para subprocesso persistido")
    void deveUsarDestinoDaUltimaMovimentacao() {
        Unidade unidadeOriginal = Unidade.builder().codigo(10L).build();
        Unidade unidadeAtual = Unidade.builder().codigo(20L).build();
        Subprocesso subprocesso = Subprocesso.builder()
                .codigo(1L)
                .unidade(unidadeOriginal)
                .situacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO)
                .build();
        Movimentacao movimentacao = Movimentacao.builder()
                .subprocesso(subprocesso)
                .unidadeOrigem(unidadeOriginal)
                .unidadeDestino(unidadeAtual)
                .build();

        when(movimentacaoRepo.buscarUltimaPorSubprocesso(1L)).thenReturn(Optional.of(movimentacao));

        Unidade localizacao = service.obterLocalizacaoAtual(subprocesso);

        assertThat(localizacao).isEqualTo(unidadeAtual);
    }

    @Test
    @DisplayName("deve aceitar ausencia de movimentacao apenas para subprocesso persistido nao iniciado")
    void deveAceitarAusenciaDeMovimentacaoEmNaoIniciado() {
        Unidade unidade = Unidade.builder().codigo(10L).build();
        Subprocesso subprocesso = Subprocesso.builder()
                .codigo(1L)
                .unidade(unidade)
                .situacao(SituacaoSubprocesso.NAO_INICIADO)
                .build();

        when(movimentacaoRepo.buscarUltimaPorSubprocesso(1L)).thenReturn(Optional.empty());

        Unidade localizacao = service.obterLocalizacaoAtual(subprocesso);

        assertThat(localizacao).isEqualTo(unidade);
    }

    @Test
    @DisplayName("deve falhar quando subprocesso persistido em estado avancado nao possui movimentacao")
    void deveFalharSemMovimentacaoEmEstadoAvancado() {
        Subprocesso subprocesso = Subprocesso.builder()
                .codigo(1L)
                .unidade(Unidade.builder().codigo(10L).build())
                .situacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO)
                .build();

        when(movimentacaoRepo.buscarUltimaPorSubprocesso(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obterLocalizacaoAtual(subprocesso))
                .isInstanceOf(ErroValidacao.class)
                .hasMessageContaining("Subprocesso persistido sem movimentação em situação inválida")
                .hasMessageContaining("MAPEAMENTO_MAPA_HOMOLOGADO");
    }
}
