package sgc.subprocesso.service;

import lombok.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import sgc.organizacao.*;
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
    private final UsuarioFacade usuarioFacade;

    public AnaliseHistoricoDto converter(Analise analise) {
        return converterLista(List.of(analise)).getFirst();
    }

    public List<AnaliseHistoricoDto> converterLista(List<Analise> analises) {
        if (analises.isEmpty()) {
            return List.of();
        }
        Map<Long, UnidadeResumoLeitura> unidadesPorCodigo = carregarUnidadesPorCodigo(analises);
        Map<String, Usuario> usuariosPorTitulo = carregarUsuariosPorTitulo(analises);
        return analises.stream()
                .map(analise -> paraHistoricoDto(analise, unidadesPorCodigo, usuariosPorTitulo))
                .toList();
    }

    private AnaliseHistoricoDto paraHistoricoDto(
            Analise analise,
            Map<Long, UnidadeResumoLeitura> unidadesPorCodigo,
            Map<String, Usuario> usuariosPorTitulo
    ) {
        UnidadeResumoLeitura unidade = Optional.ofNullable(unidadesPorCodigo.get(analise.getUnidadeCodigo()))
                .orElseThrow(() -> new IllegalStateException(
                        "Unidade %d ausente no histórico de análises".formatted(analise.getUnidadeCodigo())));
        String usuarioTitulo = analise.getUsuarioTitulo();
        Usuario usuario = Optional.ofNullable(usuarioTitulo)
                .map(usuariosPorTitulo::get)
                .orElseThrow(() -> new IllegalStateException(
                        "Usuário %s ausente no histórico de análises".formatted(usuarioTitulo)));
        String usuarioNome = Optional.of(usuario)
                .map(Usuario::getNome)
                .filter(nome -> !nome.isBlank())
                .orElseThrow(() -> new IllegalStateException(
                        "Usuário %s sem nome no histórico de análises".formatted(usuarioTitulo)));

        return AnaliseHistoricoDto.builder()
                .dataHora(analise.getDataHora())
                .observacoes(analise.getObservacoes())
                .acao(analise.getAcao())
                .acaoDescricao(formatarAcaoDescricao(analise.getAcao()))
                .unidadeSigla(unidade.sigla())
                .unidadeNome(unidade.nome())
                .analistaUsuarioTitulo(usuarioTitulo)
                .usuarioNome(usuarioNome)
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

    private Map<String, Usuario> carregarUsuariosPorTitulo(List<Analise> analises) {
        List<String> titulos = analises.stream()
                .map(Analise::getUsuarioTitulo)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (titulos.isEmpty()) {
            return Map.of();
        }

        return usuarioFacade.buscarUsuariosPorTitulos(titulos);
    }

    private String formatarAcaoDescricao(TipoAcaoAnalise acao) {
        return switch (acao) {
            case ACEITE_MAPEAMENTO, ACEITE_REVISAO -> "Aceite";
            case DEVOLUCAO_MAPEAMENTO, DEVOLUCAO_REVISAO -> "Devolução";
        };
    }
}
