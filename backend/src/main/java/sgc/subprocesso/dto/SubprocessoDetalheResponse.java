package sgc.subprocesso.dto;

import lombok.Builder;
import org.jspecify.annotations.Nullable;
import sgc.organizacao.dto.ResponsavelDto;
import sgc.organizacao.dto.UsuarioResumoDto;

import java.util.List;

@Builder
public record SubprocessoDetalheResponse(
        SubprocessoResumoDto subprocesso,
        @Nullable ResponsavelDto responsavel,
        @Nullable UsuarioResumoDto titular,
        List<MovimentacaoDto> movimentacoes,
        String localizacaoAtual,
        PermissoesSubprocessoDto permissoes
) {
}
