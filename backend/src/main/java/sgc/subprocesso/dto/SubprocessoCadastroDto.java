package sgc.subprocesso.dto;

import lombok.*;
import sgc.mapa.dto.*;
import sgc.organizacao.dto.*;
import sgc.subprocesso.model.*;

import java.util.*;

@Builder
public record SubprocessoCadastroDto(
        Long codigo,
        UnidadeDto unidade,
        List<AtividadeDto> atividades) {

    public static SubprocessoCadastroDto fromEntity(Subprocesso subprocesso, List<AtividadeDto> atividades) {
        return SubprocessoCadastroDto.builder()
                .codigo(subprocesso.getCodigo())
                .unidade(paraUnidadeResumo(subprocesso.getUnidade()))
                .atividades(atividades)
                .build();
    }

    private static UnidadeDto paraUnidadeResumo(sgc.organizacao.model.Unidade unidade) {
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
