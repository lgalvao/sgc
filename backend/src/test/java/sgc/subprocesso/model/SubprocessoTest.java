package sgc.subprocesso.model;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import sgc.mapa.model.Mapa;
import sgc.organizacao.model.Unidade;
import sgc.processo.model.Processo;

@Tag("unit")
@DisplayName("Entidade: Subprocesso")
class SubprocessoTest {

    @ParameterizedTest
    @EnumSource(value = SituacaoSubprocesso.class, names = {
            "MAPEAMENTO_CADASTRO_EM_ANDAMENTO",
            "MAPEAMENTO_MAPA_DISPONIBILIZADO",
            "MAPEAMENTO_MAPA_VALIDADO",
            "REVISAO_CADASTRO_EM_ANDAMENTO",
            "REVISAO_MAPA_DISPONIBILIZADO"
    })
    @DisplayName("Deve estar em andamento quando a situação for de trabalho ativo")
    void deveEstarEmAndamento(SituacaoSubprocesso situacao) {
        Subprocesso sp = Subprocesso.builder().situacao(situacao).build();
        assertThat(sp.isEmAndamento()).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = SituacaoSubprocesso.class, names = {
            "MAPEAMENTO_MAPA_HOMOLOGADO",
            "REVISAO_MAPA_HOMOLOGADO",
            "NAO_INICIADO"
    })
    @DisplayName("Não deve estar em andamento quando a situação for finalizada ou não iniciada")
    void naoDeveEstarEmAndamento(SituacaoSubprocesso situacao) {
        Subprocesso sp = Subprocesso.builder().situacao(situacao).build();
        assertThat(sp.isEmAndamento()).isFalse();
    }

    @Test
    @DisplayName("Deve retornar etapa 1 se não estiver homologado")
    void deveRetornarEtapa1() {
        Subprocesso sp = Subprocesso.builder().situacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO).build();
        assertThat(sp.getEtapaAtual()).isEqualTo(1);
    }

    @ParameterizedTest
    @EnumSource(value = SituacaoSubprocesso.class, names = {
            "MAPEAMENTO_MAPA_HOMOLOGADO",
            "REVISAO_MAPA_HOMOLOGADO"
    })
    @DisplayName("Deve retornar etapa null se estiver homologado")
    void deveRetornarEtapaNullQuandoHomologado(SituacaoSubprocesso situacao) {
        Subprocesso sp = Subprocesso.builder().situacao(situacao).build();
        assertThat(sp.getEtapaAtual()).isNull();
    }

    @Test
    @DisplayName("Deve instanciar via Builder")
    void deveInstanciarViaBuilder() {
        Processo p = new Processo();
        Unidade u = new Unidade();
        Mapa m = new Mapa();
        LocalDateTime dt = LocalDateTime.now();
        Subprocesso sp = Subprocesso.builder()
                .processo(p)
                .unidade(u)
                .mapa(m)
                .situacao(SituacaoSubprocesso.NAO_INICIADO)
                .dataLimiteEtapa1(dt)
                .build();
        sp.setCodigo(1L);

        assertThat(sp.getCodigo()).isEqualTo(1L);
        assertThat(sp.getProcesso()).isEqualTo(p);
        assertThat(sp.getUnidade()).isEqualTo(u);
        assertThat(sp.getMapa()).isEqualTo(m);
        assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.NAO_INICIADO);
        assertThat(sp.getDataLimiteEtapa1()).isEqualTo(dt);
    }


    @Test
    @DisplayName("Getters NonNull should return values")
    void gettersNonNull() {
        Processo p = new Processo();
        Unidade u = new Unidade();
        Mapa m = new Mapa();
        Subprocesso sp = Subprocesso.builder().processo(p).unidade(u).mapa(m).build();

        assertThat(sp.getProcesso()).isEqualTo(p);
        assertThat(sp.getUnidade()).isEqualTo(u);
        assertThat(sp.getMapa()).isEqualTo(m);
    }
}
