package sgc.subprocesso.dto;

import lombok.*;
import sgc.organizacao.dto.*;
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
