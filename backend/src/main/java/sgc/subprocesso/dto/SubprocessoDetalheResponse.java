package sgc.subprocesso.dto;

import lombok.Builder;
import sgc.organizacao.model.Usuario;
import sgc.subprocesso.model.Movimentacao;
import sgc.subprocesso.model.Subprocesso;

import java.util.List;

@Builder
public record SubprocessoDetalheResponse(
    Subprocesso subprocesso,
    Usuario responsavel,
    Usuario titular,
    List<Movimentacao> movimentacoes,
    String localizacaoAtual,
    PermissoesSubprocessoDto permissoes
) {}
