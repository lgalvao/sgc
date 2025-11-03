package sgc.mapa.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.competencia.modelo.Competencia;
import sgc.competencia.modelo.CompetenciaAtividadeRepo;
import sgc.competencia.modelo.CompetenciaRepo;
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
    private final CompetenciaAtividadeRepo competenciaAtividadeRepo;
    private final MapaVinculoService mapaVinculoService;
    private final MapaIntegridadeService mapaIntegridadeService;

    // Métodos CRUD consolidados
    @Transactional(readOnly = true)
    public List<Mapa> listar() {
        return repositorioMapa.findAll();
    }

    @Transactional(readOnly = true)
    public Mapa obterPorCodigo(Long codigo) {
        return repositorioMapa.findById(codigo).orElseThrow(() -> new ErroDominioNaoEncontrado("Mapa", codigo));
    }

    public Mapa criar(Mapa mapa) {
        return repositorioMapa.save(mapa);
    }

    public Mapa atualizar(Long codigo, Mapa mapa) {
        return repositorioMapa.findById(codigo)
                .map(existente -> {
                    existente.setDataHoraDisponibilizado(mapa.getDataHoraDisponibilizado());
                    existente.setObservacoesDisponibilizacao(mapa.getObservacoesDisponibilizacao());
                    existente.setSugestoesApresentadas(mapa.getSugestoesApresentadas());
                    existente.setDataHoraHomologado(mapa.getDataHoraHomologado());
                    return repositorioMapa.save(existente);
                })
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Mapa", codigo));
    }

    public void excluir(Long codigo) {
        if (!repositorioMapa.existsById(codigo)) {
            throw new ErroDominioNaoEncontrado("Mapa", codigo);
        }
        repositorioMapa.deleteById(codigo);
    }

    /**
     * Obtém uma visão completa de um mapa, incluindo suas competências e as
     * atividades vinculadas a cada uma.
     *
     * @param codMapa        O código do mapa a ser buscado.
     * @param codSubprocesso O código do subprocesso associado (usado para compor o DTO de retorno).
     * @return Um {@link MapaCompletoDto} com todos os detalhes do mapa.
     * @throws ErroDominioNaoEncontrado se o mapa não for encontrado.
     */
    @Transactional(readOnly = true)
    public MapaCompletoDto obterMapaCompleto(Long codMapa, Long codSubprocesso) {
        log.debug("Obtendo mapa completo: codigo={}, codSubprocesso={}", codMapa, codSubprocesso);

        Mapa mapa = repositorioMapa.findById(codMapa)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Mapa não encontrado: %d".formatted(codMapa)));

        List<Competencia> competencias = repositorioCompetencia.findByMapaCodigo(codMapa);

        List<CompetenciaMapaDto> competenciasDto = competencias.stream()
                .map(c -> {
                    List<Long> idsAtividades = competenciaAtividadeRepo.findByCompetencia_Codigo(c.getCodigo()).stream().map(ca -> ca.getId().getCodAtividade()).toList();

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
     * @param codMapa                 O código do mapa a ser salvo.
     * @param request                O DTO com o estado completo do mapa.
     * @param usuarioTituloEleitoral O título de eleitor do usuário que está realizando a operação.
     * @return Um {@link MapaCompletoDto} representando o estado final do mapa salvo.
     * @throws ErroDominioNaoEncontrado se o mapa ou uma competência a ser atualizada não forem encontrados.
     */
    public MapaCompletoDto salvarMapaCompleto(Long codMapa, SalvarMapaRequest request, Long usuarioTituloEleitoral) {
        log.info("Salvando mapa completo: codigo={}, usuario={}", codMapa, usuarioTituloEleitoral);

        Mapa mapa = repositorioMapa.findById(codMapa)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Mapa não encontrado: %d".formatted(codMapa)));

        var sanitizedObservacoes = HTML_SANITIZER_POLICY.sanitize(request.observacoes());
        mapa.setObservacoesDisponibilizacao(sanitizedObservacoes);
        mapa = repositorioMapa.save(mapa);

        List<Competencia> competenciasAtuais = repositorioCompetencia.findByMapaCodigo(codMapa);
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
            repositorioCompetencia.deleteById(codParaRemover);
            log.debug("Competência {} removida do mapa {}", codParaRemover, codMapa);
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

        mapaIntegridadeService.validarIntegridadeMapa(codMapa);
        // Reconstruir o MapaCompletoDto para retorno
        List<Competencia> competenciasFinais = repositorioCompetencia.findByMapaCodigo(codMapa);
        List<CompetenciaMapaDto> competenciasDtoFinais = competenciasFinais.stream()
                .map(c -> {
                    List<Long> idsAtividades = competenciaAtividadeRepo.findByCompetencia_Codigo(c.getCodigo()).stream().map(ca -> ca.getId().getCodAtividade()).toList();
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