package sgc.mapa;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.atividade.Atividade;
import sgc.atividade.AtividadeRepository;
import sgc.competencia.Competencia;
import sgc.competencia.CompetenciaAtividade;
import sgc.competencia.CompetenciaAtividadeRepository;
import sgc.competencia.CompetenciaRepository;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.dto.CompetenciaMapaDto;
import sgc.mapa.dto.MapaCompletoDto;
import sgc.mapa.dto.SalvarMapaRequest;
import sgc.subprocesso.Subprocesso;
import sgc.subprocesso.SubprocessoRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementação do serviço de negócio para Mapas de Competências.
 * <p>
 * Gerencia operações agregadas sobre mapas, competências e vínculos com atividades.
 */
@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class MapaServiceImpl implements MapaService {
    private final MapaRepository mapaRepository;
    private final CompetenciaRepository competenciaRepository;
    private final CompetenciaAtividadeRepository competenciaAtividadeRepository;
    private final AtividadeRepository atividadeRepository;
    private final SubprocessoRepository subprocessoRepository;

    @Override
    @Transactional(readOnly = true)
    public MapaCompletoDto obterMapaCompleto(Long idMapa) {
        log.debug("Obtendo mapa completo: id={}", idMapa);

        // 1. Buscar mapa
        Mapa mapa = mapaRepository.findById(idMapa)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Mapa não encontrado: %d".formatted(idMapa)));

        // 2. Buscar subprocesso vinculado ao mapa
        Long subprocessoCodigo = buscarSubprocessoDoMapa(idMapa);

        // 3. Buscar todas competências do mapa
        List<Competencia> competencias = competenciaRepository.findByMapaCodigo(idMapa);

        // 4. Para cada competência, buscar atividades vinculadas
        List<CompetenciaMapaDto> competenciasDto = competencias.stream()
                .map(c -> {
                    List<Long> atividadesCodigos = competenciaAtividadeRepository
                            .findByCompetenciaCodigo(c.getCodigo())
                            .stream()
                            .map(ca -> ca.getId().getAtividadeCodigo())
                            .toList();

                    return new CompetenciaMapaDto(
                            c.getCodigo(),
                            c.getDescricao(),
                            atividadesCodigos
                    );
                })
                .toList();

        // 5. Retornar DTO agregado
        return new MapaCompletoDto(
                mapa.getCodigo(),
                subprocessoCodigo,
                mapa.getObservacoesDisponibilizacao(),
                competenciasDto
        );
    }

    @Override
    public MapaCompletoDto salvarMapaCompleto(Long idMapa, SalvarMapaRequest request, String usuarioTitulo) {
        log.info("Salvando mapa completo: id={}, usuario={}", idMapa, usuarioTitulo);

        // 1. Buscar mapa
        Mapa mapa = mapaRepository.findById(idMapa)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Mapa não encontrado: %d".formatted(idMapa)));

        // 2. Atualizar observações do mapa
        mapa.setObservacoesDisponibilizacao(request.observacoes());
        mapa = mapaRepository.save(mapa);

        // 3. Buscar competências atuais do mapa
        List<Competencia> competenciasAtuais = competenciaRepository.findByMapaCodigo(idMapa);
        Set<Long> codigosAtuais = competenciasAtuais.stream()
                .map(Competencia::getCodigo)
                .collect(Collectors.toSet());

        Set<Long> codigosNovos = request.competencias().stream()
                .map(CompetenciaMapaDto::codigo)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 4. Remover competências excluídas
        Set<Long> codigosRemover = new HashSet<>(codigosAtuais);
        codigosRemover.removeAll(codigosNovos);

        for (Long codigoRemover : codigosRemover) {
            // Remover vínculos primeiro
            competenciaAtividadeRepository.deleteByCompetenciaCodigo(codigoRemover);
            // Remover competência
            competenciaRepository.deleteById(codigoRemover);
            log.debug("Competência {} removida do mapa {}", codigoRemover, idMapa);
        }

        // 5. Processar cada competência do request
        for (CompetenciaMapaDto compDto : request.competencias()) {
            Competencia competencia;
            if (compDto.codigo() == null) {
                // NOVA competência
                competencia = new Competencia();
                competencia.setMapa(mapa);
                competencia.setDescricao(compDto.descricao());
                competencia = competenciaRepository.save(competencia);
                log.debug("Nova competência criada: {}", competencia.getCodigo());
            } else {
                // ATUALIZAR competência existente
                competencia = competenciaRepository.findById(compDto.codigo())
                        .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Competência não encontrada: %d".formatted(compDto.codigo())));

                competencia.setDescricao(compDto.descricao());
                competencia = competenciaRepository.save(competencia);
                log.debug("Competência atualizada: {}", competencia.getCodigo());
            }

            // 6. Atualizar vínculos com atividades
            atualizarVinculosAtividades(competencia.getCodigo(), compDto.atividadesCodigos());
        }

        // 7. Validar integridade do mapa (apenas warnings, não bloqueia)
        validarIntegridadeMapa(idMapa);

        // 8. Retornar mapa atualizado
        return obterMapaCompleto(idMapa);
    }

    @Override
    @Transactional(readOnly = true)
    public MapaCompletoDto obterMapaSubprocesso(Long idSuprocesso) {
        log.debug("Obtendo mapa do subprocesso: id={}", idSuprocesso);

        // Buscar subprocesso
        Subprocesso subprocesso = subprocessoRepository.findById(idSuprocesso)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Subprocesso não encontrado: %d".formatted(idSuprocesso)));

        // Verificar se subprocesso tem mapa
        if (subprocesso.getMapa() == null) {
            throw new ErroEntidadeNaoEncontrada("Subprocesso não possui mapa associado");
        }

        return obterMapaCompleto(subprocesso.getMapa().getCodigo());
    }

    @Override
    public MapaCompletoDto salvarMapaSubprocesso(Long codSubprocesso, SalvarMapaRequest request, String usuarioTitulo) {
        log.info("Salvando mapa do subprocesso: codSubprocesso={}, usuario={}", codSubprocesso, usuarioTitulo);

        // 1. Buscar subprocesso
        Subprocesso subprocesso = subprocessoRepository.findById(codSubprocesso)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Subprocesso não encontrado: %d".formatted(codSubprocesso)));

        // 2. Validar situação (deve ser CADASTRO_HOMOLOGADO ou MAPA_CRIADO)
        String situacao = subprocesso.getSituacaoId();
        if (!"CADASTRO_HOMOLOGADO".equals(situacao) && !"MAPA_CRIADO".equals(situacao)) {
            throw new IllegalStateException("Mapa só pode ser editado com cadastro homologado ou mapa criado. Situação atual: %s".formatted(situacao));
        }

        // 3. Verificar se subprocesso tem mapa
        if (subprocesso.getMapa() == null) {
            throw new ErroEntidadeNaoEncontrada("Subprocesso não possui mapa associado");
        }

        Long codMapa = subprocesso.getMapa().getCodigo();

        // 4. Verificar se é primeira vez criando competências
        boolean eraVazio = competenciaRepository.findByMapaCodigo(codMapa).isEmpty();
        boolean temNovasCompetencias = !request.competencias().isEmpty();

        // 5. Salvar mapa completo
        MapaCompletoDto mapaDto = salvarMapaCompleto(codMapa, request, usuarioTitulo);

        // 6. Se era vazio e agora tem competências, mudar situação para MAPA_CRIADO
        if (eraVazio && temNovasCompetencias && "CADASTRO_HOMOLOGADO".equals(situacao)) {
            subprocesso.setSituacaoId("MAPA_CRIADO");
            subprocessoRepository.save(subprocesso);
            log.info("Situação do subprocesso {} alterada para MAPA_CRIADO", codSubprocesso);
        }

        return mapaDto;
    }

    @Override
    @Transactional(readOnly = true)
    public void validarMapaCompleto(Long mapaId) {
        log.debug("Validando integridade do mapa: mapaId={}", mapaId);

        MapaCompletoDto mapa = obterMapaCompleto(mapaId);

        // Validar: todas competências têm atividades
        for (CompetenciaMapaDto comp : mapa.competencias()) {
            if (comp.atividadesCodigos().isEmpty()) {
                throw new IllegalStateException("A competência '%s' não possui atividades vinculadas".formatted(comp.descricao()));
            }
        }

        // Buscar todas atividades do mapa
        List<Atividade> atividades = atividadeRepository.findByMapaCodigo(mapaId);

        // Validar: todas atividades estão em alguma competência
        for (Atividade atividade : atividades) {
            boolean temVinculo = competenciaAtividadeRepository.existsByAtividadeCodigo(atividade.getCodigo());
            if (!temVinculo) {
                throw new IllegalStateException("A atividade '%s' não está vinculada a nenhuma competência".formatted(atividade.getDescricao()));
            }
        }

        log.debug("Mapa {} validado com sucesso", mapaId);
    }

    /**
     * Atualiza os vínculos entre competência e atividades.
     * Remove vínculos antigos e cria novos conforme lista.
     */
    private void atualizarVinculosAtividades(Long competenciaCodigo, List<Long> atividadesCodigos) {
        // 1. Remover todos vínculos atuais
        competenciaAtividadeRepository.deleteByCompetenciaCodigo(competenciaCodigo);

        // 2. Criar novos vínculos
        for (Long atividadeCodigo : atividadesCodigos) {
            // Validar que atividade existe
            if (!atividadeRepository.existsById(atividadeCodigo)) {
                log.warn("Tentativa de vincular atividade inexistente: {}", atividadeCodigo);
                continue;
            }

            CompetenciaAtividade vinculo = new CompetenciaAtividade();
            CompetenciaAtividade.Id id = new CompetenciaAtividade.Id(atividadeCodigo, competenciaCodigo);
            vinculo.setId(id);
            competenciaAtividadeRepository.save(vinculo);
        }

        log.debug("Atualizados {} vínculos para competência {}",
                atividadesCodigos.size(), competenciaCodigo);
    }

    /**
     * Valida integridade do mapa com warnings (não bloqueia operação).
     * <p>
     * Verifica:
     * - Competências sem atividades
     * - Atividades sem competências
     */
    private void validarIntegridadeMapa(Long idMapa) {
        // Buscar todas atividades do mapa
        List<Atividade> atividades = atividadeRepository.findByMapaCodigo(idMapa);

        // Buscar todas competências do mapa
        List<Competencia> competencias = competenciaRepository.findByMapaCodigo(idMapa);

        // Verificar atividades sem competência
        for (Atividade atividade : atividades) {
            Long codAtividade = atividade.getCodigo();
            boolean temVinculo = competenciaAtividadeRepository.existsByAtividadeCodigo(codAtividade);
            if (!temVinculo) {
                log.warn("Atividade {} não vinculada a nenhuma competência no mapa {}", codAtividade, idMapa);
            }
        }

        // Verificar competências sem atividades
        for (Competencia competencia : competencias) {
            Long codCompetencia = competencia.getCodigo();
            List<CompetenciaAtividade> vinculos = competenciaAtividadeRepository.findByCompetenciaCodigo(codCompetencia);
            if (vinculos.isEmpty()) {
                log.warn("Competência {} sem atividades vinculadas no mapa {}", codCompetencia, idMapa);
            }
        }
    }

    /**
     * Busca o código do subprocesso associado a um mapa.
     */
    private Long buscarSubprocessoDoMapa(Long idMapa) {
        List<Subprocesso> subprocessos = subprocessoRepository.findAll();
        return subprocessos.stream()
                .filter(s -> s.getMapa() != null && s.getMapa().getCodigo().equals(idMapa))
                .map(Subprocesso::getCodigo)
                .findFirst()
                .orElse(null);
    }
}