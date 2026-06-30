package sgc.subprocesso.service;

import lombok.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import sgc.comum.erros.*;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.model.*;

import java.util.*;
import java.util.stream.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnaliseHistoricoService {

    private final UnidadeService unidadeService;
    private final UsuarioService usuarioService;

    public AnaliseHistoricoDto converter(Analise analise) {
        return converterLista(List.of(analise)).getFirst();
    }

    public List<AnaliseHistoricoDto> converterLista(List<Analise> analises) {
        if (analises.isEmpty()) {
            return List.of();
        }
        Map<Long, UnidadeResumoLeitura> unidadesPorCodigo = carregarUnidadesPorCodigo(analises);
        Map<String, String> nomesUsuariosPorTitulo = carregarNomesUsuariosPorTitulo(analises);
        return analises.stream()
                .map(analise -> paraHistoricoDto(analise, unidadesPorCodigo, nomesUsuariosPorTitulo))
                .toList();
    }

    private AnaliseHistoricoDto paraHistoricoDto(
            Analise analise,
            Map<Long, UnidadeResumoLeitura> unidadesPorCodigo,
            Map<String, String> nomesUsuariosPorTitulo
    ) {
        UnidadeResumoLeitura unidade = Optional.ofNullable(unidadesPorCodigo.get(analise.getUnidadeCodigo()))
                .orElseThrow(() -> new ErroInconsistenciaInterna(
                        "Unidade %d ausente no histórico de análises".formatted(analise.getUnidadeCodigo())));
        String usuarioTitulo = analise.getUsuarioTitulo();
        String usuarioNome = Optional.ofNullable(usuarioTitulo)
                .map(nomesUsuariosPorTitulo::get)
                .filter(nome -> !nome.isBlank())
                .orElseThrow(() -> new ErroInconsistenciaInterna(
                        "Usuário %s ausente ou sem nome no histórico de análises".formatted(usuarioTitulo)));

        return AnaliseHistoricoDto.builder()
                .dataHora(analise.getDataHora())
                .observacoes(analise.getObservacoes())
                .acao(analise.getAcao().name())
                .acaoDescricao(formatarAcaoDescricao(analise.getAcao()))
                .unidadeSigla(unidade.sigla())
                .unidadeNome(unidade.nome())
                .analistaUsuarioTitulo(usuarioTitulo)
                .usuarioNome(usuarioNome)
                .motivo(analise.getMotivo())
                .tipo(analise.getTipo().name())
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

    private Map<String, String> carregarNomesUsuariosPorTitulo(List<Analise> analises) {
        Set<String> titulos = analises.stream()
                .map(Analise::getUsuarioTitulo)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (titulos.isEmpty()) {
            return Map.of();
        }

        return usuarioService.buscarConsultasPorTitulos(titulos).stream()
                .collect(Collectors.toMap(
                        UsuarioConsultaLeitura::tituloEleitoral,
                        UsuarioConsultaLeitura::nome,
                        (n1, n2) -> n1
                ));
    }

    private String formatarAcaoDescricao(TipoAcaoAnalise acao) {
        return switch (acao) {
            case ACEITE_MAPEAMENTO, ACEITE_REVISAO, ACEITE_DIAGNOSTICO -> "Aceite";
            case DEVOLUCAO_MAPEAMENTO, DEVOLUCAO_REVISAO, DEVOLUCAO_DIAGNOSTICO -> "Devolução";
            case HOMOLOGACAO_DIAGNOSTICO -> "Homologação";
        };
    }
}
