package sgc.diagnostico.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class AvaliacaoServidorTest {

    @Test
    @DisplayName("calculaGap: deve definir gap como nulo quando importancia ou dominio for nulo ou zero")
    void deveDefinirGapComoNuloQuandoCamposInvalidos() {
        // Cenário 1: Importância nula
        AvaliacaoServidor avaliacaoImportanciaNula = AvaliacaoServidor.builder()
                .importancia(null)
                .dominio(3)
                .build();
        avaliacaoImportanciaNula.calculaGap();
        assertThat(avaliacaoImportanciaNula.getGap()).isNull();

        // Cenário 2: Domínio nulo
        AvaliacaoServidor avaliacaoDominioNulo = AvaliacaoServidor.builder()
                .importancia(4)
                .dominio(null)
                .build();
        avaliacaoDominioNulo.calculaGap();
        assertThat(avaliacaoDominioNulo.getGap()).isNull();

        // Cenário 3: Importância zero
        AvaliacaoServidor avaliacaoImportanciaZero = AvaliacaoServidor.builder()
                .importancia(0)
                .dominio(3)
                .build();
        avaliacaoImportanciaZero.calculaGap();
        assertThat(avaliacaoImportanciaZero.getGap()).isNull();

        // Cenário 4: Domínio zero
        AvaliacaoServidor avaliacaoDominioZero = AvaliacaoServidor.builder()
                .importancia(4)
                .dominio(0)
                .build();
        avaliacaoDominioZero.calculaGap();
        assertThat(avaliacaoDominioZero.getGap()).isNull();
    }

    @Test
    @DisplayName("calculaGap: deve calcular a diferenca entre importancia e dominio quando ambos forem preenchidos e maiores que zero")
    void deveCalcularDiferencaEntreImportanciaEDominio() {
        AvaliacaoServidor avaliacaoValida = AvaliacaoServidor.builder()
                .importancia(4)
                .dominio(2)
                .build();

        avaliacaoValida.calculaGap();

        assertThat(avaliacaoValida.getGap()).isEqualTo(2);
    }
}
