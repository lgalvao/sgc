package sgc.subprocesso.dto;

import lombok.*;
import org.jspecify.annotations.*;
import sgc.mapa.dto.*;
import sgc.organizacao.dto.*;
import sgc.organizacao.model.*;
import sgc.subprocesso.model.*;

import java.util.*;

@Builder
public record SubprocessoCadastroDto(
        @Nullable Long codigo,
        @Nullable UnidadeDto unidade,
        List<AtividadeDto> atividades) {

    public static SubprocessoCadastroDto fromEntity(Subprocesso subprocesso, List<AtividadeDto> atividades) {
        return SubprocessoCadastroDto.builder()
                .codigo(subprocesso.getCodigo())
                .unidade(paraUnidadeResumo(subprocesso.getUnidade()))
                .atividades(atividades)
                .build();
    }

    private static @Nullable UnidadeDto paraUnidadeResumo(@Nullable Unidade unidade) {
        if (unidade == null) {
            return null;
        }

        return UnidadeDto.builder()
                .codigo(unidade.getCodigo())
                .nome(unidade.getNome())
                .sigla(unidade.getSigla())
                .tipo(unidade.getTipo().name())
                .tituloTitular(unidade.getTituloTitular())
                .build();
    }
}

