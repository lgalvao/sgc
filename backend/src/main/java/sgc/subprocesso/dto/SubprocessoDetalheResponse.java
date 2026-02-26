package sgc.subprocesso.dto;

import lombok.*;
import sgc.organizacao.model.*;
import sgc.subprocesso.model.*;

import java.util.*;

@Builder
public record SubprocessoDetalheResponse(
    Subprocesso subprocesso,
    Usuario responsavel,
    Usuario titular,
    List<Movimentacao> movimentacoes,
    String localizacaoAtual,
    PermissoesSubprocessoDto permissoes
) {}
