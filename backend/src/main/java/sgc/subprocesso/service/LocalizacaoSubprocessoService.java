package sgc.subprocesso.service;

import lombok.*;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import sgc.comum.config.CacheConfig;
import sgc.comum.erros.*;
import sgc.organizacao.model.*;
import sgc.subprocesso.model.*;

import java.util.*;

import static sgc.subprocesso.model.SituacaoSubprocesso.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LocalizacaoSubprocessoService {

    private final MovimentacaoRepo movimentacaoRepo;

    @Cacheable(
            cacheNames = CacheConfig.CACHE_LOCALIZACAO_SUBPROCESSO,
            key = "#subprocesso.codigo",
            condition = "#subprocesso.codigo != null",
            sync = true)
    public Unidade obterLocalizacaoAtual(Subprocesso subprocesso) {
        Unidade unidadeBase = subprocesso.getUnidade();
        if (subprocesso.getCodigo() == null) {
            return unidadeBase;
        }

        return movimentacaoRepo.buscarUltimaPorSubprocesso(subprocesso.getCodigo())
                .map(Movimentacao::getUnidadeDestino)
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
