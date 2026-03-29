package sgc.subprocesso.dto;

import org.junit.jupiter.api.*;
import sgc.mapa.model.*;
import sgc.organizacao.model.*;
import sgc.processo.model.*;
import sgc.subprocesso.model.*;

import java.time.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("SubprocessoListagemDto")
class SubprocessoListagemDtoTest {

    @Test
    @DisplayName("deve mapear subprocesso para listagem")
    void deveMapearSubprocessoParaListagem() {
        Unidade unidade = new Unidade();
        unidade.setCodigo(10L);
        unidade.setNome("Unidade");
        unidade.setSigla("UND");
        unidade.setTipo(TipoUnidade.OPERACIONAL);
        unidade.setTituloTitular("999");

        Processo processo = Processo.builder()
                .descricao("Processo")
                .situacao(SituacaoProcesso.EM_ANDAMENTO)
                .tipo(TipoProcesso.MAPEAMENTO)
                .dataCriacao(LocalDateTime.of(2025, 1, 1, 10, 0))
                .dataLimite(LocalDateTime.of(2025, 1, 30, 10, 0))
                .build();
        processo.setCodigo(50L);

        Mapa mapa = new Mapa();
        mapa.setCodigo(60L);

        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(20L);
        subprocesso.setProcesso(processo);
        subprocesso.setUnidade(unidade);
        subprocesso.setMapa(mapa);
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);
        subprocesso.setDataLimiteEtapa1(LocalDateTime.of(2025, 1, 10, 10, 0));

        SubprocessoListagemDto dto = SubprocessoListagemDto.fromEntity(subprocesso);

        assertThat(dto.codigo()).isEqualTo(20L);
        assertThat(dto.codProcesso()).isEqualTo(50L);
        assertThat(dto.codMapa()).isEqualTo(60L);
        assertThat(dto.processoDescricao()).isEqualTo("Processo");
        assertThat(dto.isEmAndamento()).isTrue();
        assertThat(dto.etapaAtual()).isEqualTo(1);
    }
}
