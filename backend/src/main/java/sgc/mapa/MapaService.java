package sgc.mapa;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.competencia.modelo.Competencia;
import sgc.competencia.modelo.CompetenciaRepo;
import sgc.competencia.modelo.CompetenciaAtividadeRepo;
import sgc.comum.erros.ErroDominioNaoEncontrado;
import sgc.mapa.dto.CompetenciaMapaDto;
import sgc.mapa.dto.MapaCompletoDto;
import sgc.mapa.dto.SalvarMapaRequest;
import sgc.mapa.modelo.Mapa;
import sgc.mapa.modelo.MapaRepo;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.subprocesso.modelo.SubprocessoRepo;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class MapaService {
    private static final PolicyFactory HTML_SANITIZER_POLICY = new HtmlPolicyBuilder()
            .toFactory();

    private final MapaRepo repositorioMapa;
    private final CompetenciaRepo repositorioCompetencia;
    private final CompetenciaAtividadeRepo repositorioCompetenciaAtividade;
    private final SubprocessoRepo repositorioSubprocesso;
    private final MapaVinculoService mapaVinculoService;
    private final MapaIntegridadeService mapaIntegridadeService;

    @Transactional(readOnly = true)
    public MapaCompletoDto obterMapaCompleto(Long idMapa) {
        log.debug("Obtendo mapa completo: id={}", idMapa);

        Mapa mapa = repositorioMapa.findById(idMapa)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Mapa não encontrado: %d".formatted(idMapa)));

        Long idSubprocesso = buscarSubprocessoDoMapa(idMapa);
        List<Competencia> competencias = repositorioCompetencia.findByMapaCodigo(idMapa);

        List<CompetenciaMapaDto> competenciasDto = competencias.stream()
                .map(c -> {
                    List<Long> idsAtividades = repositorioCompetenciaAtividade.findByCompetenciaCodigo(c.getCodigo()).stream().map(ca -> ca.getId().getAtividadeCodigo()).collect(Collectors.toList());

                    return new CompetenciaMapaDto(
                            c.getCodigo(),
                            c.getDescricao(),
                            idsAtividades
                    );
                })
                .toList();

        return new MapaCompletoDto(
                mapa.getCodigo(),
                idSubprocesso,
                mapa.getObservacoesDisponibilizacao(),
                competenciasDto
        );
    }

    public MapaCompletoDto salvarMapaCompleto(Long idMapa, SalvarMapaRequest request, Long usuarioTituloEleitoral) {
        log.info("Salvando mapa completo: id={}, usuario={}", idMapa, usuarioTituloEleitoral);

        Mapa mapa = repositorioMapa.findById(idMapa)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Mapa não encontrado: %d".formatted(idMapa)));

        var sanitizedObservacoes = HTML_SANITIZER_POLICY.sanitize(request.observacoes());
        mapa.setObservacoesDisponibilizacao(sanitizedObservacoes);
        mapa = repositorioMapa.save(mapa);

        List<Competencia> competenciasAtuais = repositorioCompetencia.findByMapaCodigo(idMapa);
        Set<Long> idsAtuais = competenciasAtuais.stream()
                .map(Competencia::getCodigo)
                .collect(Collectors.toSet());

        Set<Long> idsNovos = request.competencias().stream()
                .map(CompetenciaMapaDto::codigo)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<Long> idsParaRemover = new HashSet<>(idsAtuais);
        idsParaRemover.removeAll(idsNovos);

        for (Long idParaRemover : idsParaRemover) {
            repositorioCompetenciaAtividade.deleteByCompetenciaCodigo(idParaRemover);
            repositorioCompetencia.deleteById(idParaRemover);
            log.debug("Competência {} removida do mapa {}", idParaRemover, idMapa);
        }

        for (CompetenciaMapaDto compDto : request.competencias()) {
            Competencia competencia;
            if (compDto.codigo() == null) {
                competencia = new Competencia();
                competencia.setMapa(mapa);
                competencia.setDescricao(compDto.descricao());
                competencia = repositorioCompetencia.save(competencia);
                log.debug("Nova competência criada: {}", competencia.getCodigo());
            } else {
                competencia = repositorioCompetencia.findById(compDto.codigo())
                        .orElseThrow(() -> new ErroDominioNaoEncontrado("Competência não encontrada: %d".formatted(compDto.codigo())));
                competencia.setDescricao(compDto.descricao());
                competencia = repositorioCompetencia.save(competencia);
                log.debug("Competência atualizada: {}", competencia.getCodigo());
            }
            mapaVinculoService.atualizarVinculosAtividades(competencia.getCodigo(), compDto.atividadesCodigos());
        }

        mapaIntegridadeService.validarIntegridadeMapa(idMapa);
        return obterMapaCompleto(idMapa);
    }

    private Long buscarSubprocessoDoMapa(Long idMapa) {
        return repositorioSubprocesso.findByMapaCodigo(idMapa)
                .map(Subprocesso::getCodigo)
                .orElse(null);
    }
}