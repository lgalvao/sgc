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
    private final MapaVinculoService mapaVinculoService;
    private final MapaIntegridadeService mapaIntegridadeService;

    /**
     * Obtém uma visão completa de um mapa, incluindo suas competências e as
     * atividades vinculadas a cada uma.
     *
     * @param idMapa        O ID do mapa a ser buscado.
     * @param idSubprocesso O ID do subprocesso associado (usado para compor o DTO de retorno).
     * @return Um {@link MapaCompletoDto} com todos os detalhes do mapa.
     * @throws ErroDominioNaoEncontrado se o mapa não for encontrado.
     */
    @Transactional(readOnly = true)
    public MapaCompletoDto obterMapaCompleto(Long idMapa, Long idSubprocesso) {
        log.debug("Obtendo mapa completo: id={}, idSubprocesso={}", idMapa, idSubprocesso);

        Mapa mapa = repositorioMapa.findById(idMapa)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Mapa não encontrado: %d".formatted(idMapa)));

        List<Competencia> competencias = repositorioCompetencia.findByMapaCodigo(idMapa);

        List<CompetenciaMapaDto> competenciasDto = competencias.stream()
                .map(c -> {
                    List<Long> idsAtividades = repositorioCompetenciaAtividade.findByCompetenciaCodigo(c.getCodigo()).stream().map(ca -> ca.getId().getAtividadeCodigo()).toList();

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

    /**
     * Salva o estado completo de um mapa.
     * <p>
     * Este método realiza uma operação de "upsert" para as competências e seus
     * vínculos com atividades. Ele compara o estado recebido com o estado atual
     * no banco de dados para:
     * <ul>
     *     <li>Criar novas competências.</li>
     *     <li>Atualizar competências existentes.</li>
     *     <li>Remover competências que não estão na requisição.</li>
     *     <li>Atualizar os vínculos entre competências e atividades.</li>
     * </ul>
     * Ao final, executa uma validação de integridade.
     *
     * @param idMapa                 O ID do mapa a ser salvo.
     * @param request                O DTO com o estado completo do mapa.
     * @param usuarioTituloEleitoral O título de eleitor do usuário que está realizando a operação.
     * @return Um {@link MapaCompletoDto} representando o estado final do mapa salvo.
     * @throws ErroDominioNaoEncontrado se o mapa ou uma competência a ser atualizada não forem encontrados.
     */
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
        // Reconstruir o MapaCompletoDto para retorno
        List<Competencia> competenciasFinais = repositorioCompetencia.findByMapaCodigo(idMapa);
        List<CompetenciaMapaDto> competenciasDtoFinais = competenciasFinais.stream()
                .map(c -> {
                    List<Long> idsAtividades = repositorioCompetenciaAtividade.findByCompetenciaCodigo(c.getCodigo()).stream().map(ca -> ca.getId().getAtividadeCodigo()).toList();
                    return new CompetenciaMapaDto(
                            c.getCodigo(),
                            c.getDescricao(),
                            idsAtividades
                    );
                })
                .toList();

        return new MapaCompletoDto(
                mapa.getCodigo(),
                null, // idSubprocesso não está disponível aqui, deve ser tratado pelo chamador
                mapa.getObservacoesDisponibilizacao(),
                competenciasDtoFinais
        );
    }

}