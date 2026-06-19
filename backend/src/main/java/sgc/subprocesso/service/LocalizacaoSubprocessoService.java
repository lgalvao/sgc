package sgc.subprocesso.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.erros.ErroValidacao;
import sgc.organizacao.model.Unidade;
import sgc.subprocesso.model.Movimentacao;
import sgc.subprocesso.model.MovimentacaoRepo;
import sgc.subprocesso.model.Subprocesso;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import static sgc.subprocesso.model.SituacaoSubprocesso.NAO_INICIADO;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LocalizacaoSubprocessoService {
    private static final Pageable PRIMEIRO_RESULTADO = PageRequest.of(0, 1);

    private final MovimentacaoRepo movimentacaoRepo;

    public Unidade obterLocalizacaoAtual(Subprocesso subprocesso) {
        Unidade unidadeBase = subprocesso.getUnidade();
        if (subprocesso.getCodigo() == null) {
            return unidadeBase;
        }
        if (subprocesso.getSituacao() == NAO_INICIADO) {
            return unidadeBase;
        }

        return movimentacaoRepo.listarUltimasUnidadesDestinoPorSubprocesso(subprocesso.getCodigo(), PRIMEIRO_RESULTADO)
                .stream()
                .findFirst()
                .orElseGet(() -> obterLocalizacaoSemMovimentacao(subprocesso, unidadeBase));
    }

    public Map<Long, Unidade> obterLocalizacoesAtuais(Collection<Subprocesso> subprocessos) {
        if (subprocessos.isEmpty()) {
            return Map.of();
        }

        Map<Long, Unidade> localizacoes = movimentacaoRepo.listarUltimasPorSubprocessos(subprocessos.stream()
                        .map(Subprocesso::getCodigo)
                        .filter(Objects::nonNull)
                        .toList())
                .stream()
                .collect(java.util.stream.Collectors.toMap(
                        movimentacao -> movimentacao.getSubprocesso().getCodigo(),
                        Movimentacao::getUnidadeDestino,
                        (primeira, ignorada) -> primeira
                ));

        for (Subprocesso subprocesso : subprocessos) {
            Long codigo = subprocesso.getCodigo();
            if (codigo == null || localizacoes.containsKey(codigo)) {
                continue;
            }
            localizacoes.put(codigo, obterLocalizacaoSemMovimentacao(subprocesso, subprocesso.getUnidade()));
        }

        return localizacoes;
    }

    private Unidade obterLocalizacaoSemMovimentacao(Subprocesso subprocesso, Unidade unidadeBase) {
        if (subprocesso.getSituacao() == NAO_INICIADO) {
            return unidadeBase;
        }
        throw new ErroValidacao("Subprocesso persistido sem movimentação em situação inválida: %s".formatted(subprocesso.getSituacao()));
    }
}
