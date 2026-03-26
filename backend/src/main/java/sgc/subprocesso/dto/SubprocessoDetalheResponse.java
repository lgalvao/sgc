package sgc.subprocesso.dto;

import lombok.*;
import sgc.organizacao.dto.*;
import sgc.organizacao.model.*;
import sgc.subprocesso.model.*;

import java.util.*;

@Builder
public record SubprocessoDetalheResponse(
        SubprocessoResumoDto subprocesso,
        ResponsavelDto responsavel,
        UsuarioResumoDto titular,
        List<MovimentacaoDto> movimentacoes,
        String localizacaoAtual,
        PermissoesSubprocessoDto permissoes
) {
}
