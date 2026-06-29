package sgc.subprocesso.service;

import lombok.*;
import org.jspecify.annotations.*;
import org.springframework.stereotype.*;
import sgc.comum.erros.*;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;
import sgc.subprocesso.model.*;

import java.util.*;

@Service
@RequiredArgsConstructor
public class SubprocessoFluxoContextoService {

    private final SubprocessoConsultaService subprocessoConsultaService;
    private final UnidadeService unidadeService;
    private final HierarquiaService hierarquiaService;
    private final UnidadeHierarquiaService unidadeHierarquiaService;

    public @Nullable Unidade buscarSuperiorImediato(Long codigoUnidade) {
        Long codigoPai = unidadeHierarquiaService.buscarCodigoPai(codigoUnidade);
        if (codigoPai == null) {
            return null;
        }
        return unidadeService.buscarPorCodigo(codigoPai);
    }

    public Optional<Unidade> buscarUnidadeDevolucao(Subprocesso subprocesso, Unidade unidadeAnalise) {
        return subprocessoConsultaService.listarMovimentacoesOrdenadas(subprocesso.getCodigo()).stream()
                .filter(movimentacao -> Objects.equals(movimentacao.getUnidadeDestino().getCodigo(), unidadeAnalise.getCodigo()))
                .map(Movimentacao::getUnidadeOrigem)
                .filter(unidadeOrigem -> hierarquiaService.isSubordinada(unidadeOrigem, unidadeAnalise))
                .findFirst();
    }

    public Unidade buscarUnidadeDevolucaoObrigatoria(Subprocesso subprocesso, Unidade unidadeAnalise) {
        return buscarUnidadeDevolucao(subprocesso, unidadeAnalise)
                .orElseThrow(() -> new ErroInconsistenciaInterna(
                        "Historico de movimentacoes inconsistente para devolucao do subprocesso %s na unidade %s"
                                .formatted(subprocesso.getCodigo(), unidadeAnalise.getCodigo())
                ));
    }
}
