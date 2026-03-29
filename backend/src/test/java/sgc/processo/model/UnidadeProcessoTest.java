package sgc.processo.model;

import org.junit.jupiter.api.*;
import sgc.organizacao.model.*;

import java.time.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("UnidadeProcesso")
class UnidadeProcessoTest {

    @Test
    @DisplayName("deve criar snapshot da unidade para o processo")
    void deveCriarSnapshotDaUnidadeParaOProcesso() {
        Unidade superior = new Unidade();
        superior.setCodigo(6L);

        Unidade unidade = new Unidade();
        unidade.setCodigo(8L);
        unidade.setNome("Seção");
        unidade.setSigla("SEC");
        unidade.setMatriculaTitular("00000001");
        unidade.setTituloTitular("1");
        unidade.setDataInicioTitularidade(LocalDateTime.of(2025, 1, 1, 8, 0));
        unidade.setTipo(TipoUnidade.OPERACIONAL);
        unidade.setSituacao(SituacaoUnidade.ATIVA);
        unidade.setUnidadeSuperior(superior);

        Processo processo = new Processo();
        processo.setCodigo(50L);

        UnidadeProcesso snapshot = UnidadeProcesso.criarSnapshot(processo, unidade);

        assertThat(snapshot.getProcesso()).isSameAs(processo);
        assertThat(snapshot.getUnidadeCodigoPersistido()).isEqualTo(8L);
        assertThat(snapshot.getSigla()).isEqualTo("SEC");
        assertThat(snapshot.getSituacao()).isEqualTo("ATIVA");
        assertThat(snapshot.getUnidadeSuperiorCodigo()).isEqualTo(6L);
    }

    @Test
    @DisplayName("deve falhar quando snapshot estiver sem codigo persistido")
    void deveFalharQuandoSnapshotEstiverSemCodigoPersistido() {
        UnidadeProcesso snapshot = new UnidadeProcesso();

        assertThatThrownBy(snapshot::getUnidadeCodigoPersistido)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Snapshot de unidade sem codigo persistido");
    }
}
