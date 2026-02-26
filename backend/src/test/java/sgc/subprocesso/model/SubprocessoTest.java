package sgc.subprocesso.model;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;
import sgc.mapa.model.*;
import sgc.organizacao.model.*;
import sgc.processo.model.*;
import sgc.subprocesso.erros.*;

import java.time.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Entidade: Subprocesso")
class SubprocessoTest {

    @Test
    @DisplayName("Deve lançar erro ao tentar transição inválida")
    void deveLancarErroTransicaoInvalida() {
        Processo p = Processo.builder().tipo(TipoProcesso.MAPEAMENTO).build();
        Subprocesso sp = Subprocesso.builder()
                .processo(p)
                .situacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO)
                .build();

        assertThatThrownBy(() -> sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO))
                .isInstanceOf(ErroTransicaoInvalida.class)
                .hasMessageContaining("Transição de situação inválida");
    }

    @Test
    @DisplayName("Deve permitir transição válida")
    void devePermitirTransicaoValida() {
        Processo p = Processo.builder().tipo(TipoProcesso.MAPEAMENTO).build();
        Subprocesso sp = Subprocesso.builder()
                .processo(p)
                .situacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO)
                .build();

        sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
        assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
    }

    @Test
    @DisplayName("Deve permitir mudar situação se processo for nulo")
    void devePermitirMudarSituacaoSeProcessoNulo() {
        Subprocesso sp = new Subprocesso();
        sp.setProcesso(null);
        sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO);
        assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO);
    }

    @Test
    @DisplayName("Deve permitir mudar situação se situação atual for nula")
    void devePermitirMudarSituacaoSeSituacaoAtualNula() {
        Processo p = Processo.builder().tipo(TipoProcesso.MAPEAMENTO).build();
        Subprocesso sp = new Subprocesso();
        sp.setProcesso(p);
        sp.setSituacaoForcada(null);
        sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO);
        assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO);
    }

    @Test
    @DisplayName("Deve permitir mudar situação se for a mesma")
    void devePermitirMudarSituacaoSeForAMesma() {
        Processo p = Processo.builder().tipo(TipoProcesso.MAPEAMENTO).build();
        Subprocesso sp = Subprocesso.builder()
                .processo(p)
                .situacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO)
                .build();
        sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO);
        assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO);
    }

    @Test
    @DisplayName("Deve permitir forçar situação para testes")
    void devePermitirForcarSituacao() {
        Subprocesso sp = new Subprocesso();
        sp.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO);
        assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO);
    }

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
