package sgc.subprocesso.dto;

import lombok.*;
import org.jspecify.annotations.*;
import sgc.organizacao.dto.*;
import java.util.*;

@Builder
public record SubprocessoDetalheResponse(
        SubprocessoResumoDto subprocesso,
        ResponsavelDto responsavel,
        @Nullable UsuarioResumoDto titular,
        List<MovimentacaoDto> movimentacoes,
        String localizacaoAtual,
        PermissoesSubprocessoDto permissoes
) {
}
