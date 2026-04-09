package sgc.subprocesso.service;

import lombok.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.model.*;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnaliseHistoricoService {

    private final UnidadeService unidadeService;

    public AnaliseHistoricoDto converter(Analise analise) {
        return converterLista(List.of(analise)).getFirst();
    }

    public List<AnaliseHistoricoDto> converterLista(List<Analise> analises) {
        if (analises.isEmpty()) {
            return List.of();
        }
        Map<Long, UnidadeResumoLeitura> unidadesPorCodigo = carregarUnidadesPorCodigo(analises);
        return analises.stream()
                .map(analise -> paraHistoricoDto(analise, unidadesPorCodigo))
                .toList();
    }

    private AnaliseHistoricoDto paraHistoricoDto(Analise analise, Map<Long, UnidadeResumoLeitura> unidadesPorCodigo) {
        UnidadeResumoLeitura unidade = Optional.ofNullable(unidadesPorCodigo.get(analise.getUnidadeCodigo()))
                .orElseThrow(() -> new IllegalStateException(
                        "Unidade %d ausente no histórico de análises".formatted(analise.getUnidadeCodigo())));

        return AnaliseHistoricoDto.builder()
                .dataHora(analise.getDataHora())
                .observacoes(analise.getObservacoes())
                .acao(analise.getAcao())
                .unidadeSigla(unidade.sigla())
                .unidadeNome(unidade.nome())
                .analistaUsuarioTitulo(analise.getUsuarioTitulo())
                .motivo(analise.getMotivo())
                .tipo(analise.getTipo())
                .build();
    }

    private Map<Long, UnidadeResumoLeitura> carregarUnidadesPorCodigo(List<Analise> analises) {
        List<Long> codigos = analises.stream()
                .map(Analise::getUnidadeCodigo)
                .distinct()
                .toList();

        return unidadeService.buscarResumosPorCodigos(codigos).stream()
                .collect(HashMap::new, (mapa, unidade) -> mapa.put(unidade.codigo(), unidade), HashMap::putAll);
    }
}
