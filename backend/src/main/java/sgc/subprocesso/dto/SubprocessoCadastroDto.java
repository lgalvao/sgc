package sgc.subprocesso.dto;

import lombok.Builder;
import sgc.mapa.dto.AtividadeDto;
import sgc.organizacao.dto.UnidadeResumoDto;

import java.util.List;

@Builder
public record SubprocessoCadastroDto(
        Long codigo,
        UnidadeResumoDto unidade,
        List<AtividadeDto> atividades) {
}
