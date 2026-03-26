package sgc.subprocesso.dto;

import lombok.*;
import sgc.organizacao.dto.*;
import sgc.organizacao.model.*;
import sgc.processo.model.*;
import sgc.subprocesso.model.*;

import java.time.*;

@Builder
public record SubprocessoListagemDto(
        Long codigo,
        UnidadeDto unidade,
        SituacaoSubprocesso situacao,
        LocalDateTime dataLimiteEtapa1,
        LocalDateTime dataFimEtapa1,
        LocalDateTime dataLimiteEtapa2,
        LocalDateTime dataFimEtapa2,
        Long codProcesso,
        Long codUnidade,
        Long codMapa,
        String processoDescricao,
        LocalDateTime dataCriacaoProcesso,
        TipoProcesso tipoProcesso,
        boolean isEmAndamento,
        Integer etapaAtual) {

    public static SubprocessoListagemDto fromEntity(Subprocesso subprocesso) {
        Processo processo = subprocesso.getProcesso();

        return SubprocessoListagemDto.builder()
                .codigo(subprocesso.getCodigo())
                .unidade(paraUnidadeResumo(subprocesso.getUnidade()))
                .situacao(subprocesso.getSituacao())
                .dataLimiteEtapa1(subprocesso.getDataLimiteEtapa1())
                .dataFimEtapa1(subprocesso.getDataFimEtapa1())
                .dataLimiteEtapa2(subprocesso.getDataLimiteEtapa2())
                .dataFimEtapa2(subprocesso.getDataFimEtapa2())
                .codProcesso(subprocesso.getCodProcesso())
                .codUnidade(subprocesso.getCodUnidade())
                .codMapa(subprocesso.getCodMapa())
                .processoDescricao(processo != null ? processo.getDescricao() : null)
                .dataCriacaoProcesso(processo != null ? processo.getDataCriacao() : null)
                .tipoProcesso(processo != null ? processo.getTipo() : null)
                .isEmAndamento(subprocesso.isEmAndamento())
                .etapaAtual(subprocesso.getEtapaAtual())
                .build();
    }

    private static UnidadeDto paraUnidadeResumo(Unidade unidade) {
        if (unidade == null) {
            return null;
        }

        return UnidadeDto.builder()
                .codigo(unidade.getCodigo())
                .nome(unidade.getNome())
                .sigla(unidade.getSigla())
                .tipo(unidade.getTipo() != null ? unidade.getTipo().name() : null)
                .tituloTitular(unidade.getTituloTitular())
                .build();
    }
}
