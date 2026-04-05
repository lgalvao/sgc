package sgc.subprocesso.dto;

import lombok.*;
import sgc.mapa.dto.*;
import sgc.organizacao.dto.*;
import sgc.organizacao.model.*;
import sgc.subprocesso.model.*;

import java.util.*;

@Builder
public record SubprocessoCadastroDto(
        Long codigo,
        UnidadeResumoDto unidade,
        List<AtividadeDto> atividades) {

    public static SubprocessoCadastroDto fromEntity(Subprocesso subprocesso, List<AtividadeDto> atividades) {
        Unidade unidade = subprocesso.getUnidade();
        if (unidade == null) {
            throw new IllegalStateException("Subprocesso deve possuir unidade associada");
        }

        return SubprocessoCadastroDto.builder()
                .codigo(subprocesso.getCodigo())
                .unidade(UnidadeResumoDto.fromEntityObrigatoria(unidade))
                .atividades(atividades)
                .build();
    }
}
