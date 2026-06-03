package sgc.subprocesso.dto;

import lombok.*;
import sgc.mapa.dto.*;
import sgc.organizacao.dto.*;

import java.util.*;

@Builder
public record SubprocessoCadastroDto(
        Long codigo,
        UnidadeResumoDto unidade,
        List<AtividadeDto> atividades) {
}
