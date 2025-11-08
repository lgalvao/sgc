package sgc.mapa.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.dto.CompetenciaMapaDto;
import sgc.mapa.dto.MapaCompletoDto;
import sgc.mapa.dto.SalvarMapaRequest;
import sgc.mapa.model.*;

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
    private static final PolicyFactory HTML_SANITIZER_POLICY = new HtmlPolicyBuilder().toFactory();

    private final MapaVinculoService mapaVinculoService;
    private final MapaIntegridadeService mapaIntegridadeService;

    private final MapaRepo mapaRepo;
    private final CompetenciaRepo competenciaRepo;
    private final CompetenciaAtividadeRepo competenciaAtividadeRepo;

    @Transactional(readOnly = true)
    public List<Mapa> listar() {
        return mapaRepo.findAll();
    }

    @Transactional(readOnly = true)
    public Mapa obterPorCodigo(Long codigo) {
        return mapaRepo.findById(codigo).orElseThrow(() -> new ErroEntidadeNaoEncontrada("Mapa", codigo));
    }

    public Mapa criar(Mapa mapa) {
        return mapaRepo.save(mapa);
    }

    public Mapa atualizar(Long codigo, Mapa mapa) {
        return mapaRepo.findById(codigo)
                .map(existente -> {
                    existente.setDataHoraDisponibilizado(mapa.getDataHoraDisponibilizado());
                    existente.setObservacoesDisponibilizacao(mapa.getObservacoesDisponibilizacao());
                    existente.setSugestoesApresentadas(mapa.getSugestoesApresentadas());
                    existente.setDataHoraHomologado(mapa.getDataHoraHomologado());
                    return mapaRepo.save(existente);
                })
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Mapa", codigo));
    }

    public void excluir(Long codigo) {
        if (!mapaRepo.existsById(codigo)) {
            throw new ErroEntidadeNaoEncontrada("Mapa", codigo);
        }
        mapaRepo.deleteById(codigo);
    }

    /**
     * Obtém uma visão completa de um mapa, incluindo suas competências e as
     * atividades vinculadas a cada uma.
     *
     * @param codMapa        O código do mapa a ser buscado.
     * @param codSubprocesso O código do subprocesso associado (usado para compor o DTO de retorno).
     * @return Um {@link MapaCompletoDto} com todos os detalhes do mapa.
     * @throws ErroEntidadeNaoEncontrada se o mapa não for encontrado.
     */
    @Transactional(readOnly = true)
    public MapaCompletoDto obterMapaCompleto(Long codMapa, Long codSubprocesso) {
        log.debug("Obtendo mapa completo: codigo={}, codSubprocesso={}", codMapa, codSubprocesso);

        Mapa mapa = mapaRepo.findById(codMapa)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Mapa não encontrado: %d".formatted(codMapa)));

        List<Competencia> competencias = competenciaRepo.findByMapaCodigo(codMapa);

        List<CompetenciaMapaDto> competenciasDto = competencias.stream()
                .map(c -> {
                    List<Long> idsAtividades = competenciaAtividadeRepo.findByCompetenciaCodigo(c.getCodigo())
                            .stream()
                            .map(ca -> ca.getId().getCodAtividade())
                            .toList();

                    return new CompetenciaMapaDto(
                            c.getCodigo(),
                            c.getDescricao(),
                            idsAtividades
                    );
                })
                .toList();

        return new MapaCompletoDto(
                mapa.getCodigo(),
                codSubprocesso,
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
     * @param codMapa                O código do mapa a ser salvo.
     * @param request                O DTO com o estado completo do mapa.
     * @param usuarioTituloEleitoral O título de eleitor do usuário que está realizando a operação.
     * @return Um {@link MapaCompletoDto} representando o estado final do mapa salvo.
     * @throws ErroEntidadeNaoEncontrada se o mapa ou uma competência a ser atualizada não forem encontrados.
     */
    public MapaCompletoDto salvarMapaCompleto(Long codMapa, SalvarMapaRequest request, String usuarioTituloEleitoral) {
        log.info("Salvando mapa completo: codigo={}, usuario={}", codMapa, usuarioTituloEleitoral);

        Mapa mapa = mapaRepo.findById(codMapa)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Mapa não encontrado: %d".formatted(codMapa)));

        var sanitizedObservacoes = HTML_SANITIZER_POLICY.sanitize(request.observacoes());
        mapa.setObservacoesDisponibilizacao(sanitizedObservacoes);
        mapa = mapaRepo.save(mapa);

        List<Competencia> competenciasAtuais = competenciaRepo.findByMapaCodigo(codMapa);
        Set<Long> idsAtuais = competenciasAtuais.stream()
                .map(Competencia::getCodigo)
                .collect(Collectors.toSet());

        Set<Long> idsNovos = request.competencias().stream()
                .map(CompetenciaMapaDto::codigo)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<Long> codsParaRemover = new HashSet<>(idsAtuais);
        codsParaRemover.removeAll(idsNovos);

        for (Long codParaRemover : codsParaRemover) {
            competenciaAtividadeRepo.deleteByCompetenciaCodigo(codParaRemover);
            competenciaRepo.deleteById(codParaRemover);
            log.debug("Competência {} removida do mapa {}", codParaRemover, codMapa);
        }

        for (CompetenciaMapaDto compDto : request.competencias()) {
            Competencia competencia;
            if (compDto.codigo() == null) {
                competencia = new Competencia();
                competencia.setMapa(mapa);
                competencia.setDescricao(compDto.descricao());
                competencia = competenciaRepo.save(competencia);
                log.debug("Nova competência criada: {}", competencia.getCodigo());
            } else {
                competencia = competenciaRepo.findById(compDto.codigo())
                        .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Competência não encontrada: %d".formatted(compDto.codigo())));

                competencia.setDescricao(compDto.descricao());
                competencia = competenciaRepo.save(competencia);
                log.debug("Competência atualizada: {}", competencia.getCodigo());
            }
            mapaVinculoService.atualizarVinculosAtividades(competencia.getCodigo(), compDto.atividadesCodigos());
        }

        mapaIntegridadeService.validarIntegridadeMapa(codMapa);

        // Reconstruir o MapaCompletoDto para retorno
        List<Competencia> competenciasFinais = competenciaRepo.findByMapaCodigo(codMapa);
        List<CompetenciaMapaDto> competenciasDtoFinais = competenciasFinais.stream()
                .map(c -> {
                    List<Long> idsAtividades = competenciaAtividadeRepo.findByCompetenciaCodigo(c.getCodigo())
                            .stream().map(ca -> ca.getId().getCodAtividade()).toList();

                    return new CompetenciaMapaDto(
                            c.getCodigo(),
                            c.getDescricao(),
                            idsAtividades
                    );
                })
                .toList();

        return new MapaCompletoDto(
                mapa.getCodigo(),
                null, // codSubprocesso não está disponível aqui, deve ser tratado pelo chamador
                mapa.getObservacoesDisponibilizacao(),
                competenciasDtoFinais
        );
    }
}