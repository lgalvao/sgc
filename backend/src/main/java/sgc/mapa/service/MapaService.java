package sgc.mapa.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.AtividadeRepo;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.comum.erros.ErroValidacao;
import sgc.mapa.dto.CompetenciaMapaDto;
import sgc.mapa.dto.MapaCompletoDto;
import sgc.mapa.dto.SalvarMapaRequest;
import sgc.mapa.mapper.MapaCompletoMapper;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.CompetenciaRepo;
import sgc.mapa.model.Mapa;
import sgc.mapa.model.MapaRepo;
import sgc.seguranca.SanitizacaoUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class MapaService {

    private final MapaRepo mapaRepo;
    private final CompetenciaRepo competenciaRepo;
    private final AtividadeRepo atividadeRepo;
    private final MapaCompletoMapper mapaCompletoMapper;

    @Transactional(readOnly = true)
    public List<Mapa> listar() {
        return mapaRepo.findAll();
    }

    @Transactional(readOnly = true)
    public Mapa obterPorCodigo(Long codigo) {
        return mapaRepo.findById(codigo)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Mapa", codigo));
    }

    public Mapa salvar(Mapa mapa) {
        return mapaRepo.save(mapa);
    }

    public Mapa criar(Mapa mapa) {
        return mapaRepo.save(mapa);
    }

    public Mapa atualizar(Long codigo, Mapa mapa) {
        return mapaRepo.findById(codigo)
                .map(
                        existente -> {
                            existente.setDataHoraDisponibilizado(mapa.getDataHoraDisponibilizado());
                            existente.setObservacoesDisponibilizacao(
                                    mapa.getObservacoesDisponibilizacao());
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

    public java.util.Optional<Mapa> buscarMapaVigentePorUnidade(Long codigoUnidade) {
        return mapaRepo.findMapaVigenteByUnidade(codigoUnidade);
    }

    public java.util.Optional<Mapa> buscarPorSubprocessoCodigo(Long codSubprocesso) {
        return mapaRepo.findBySubprocessoCodigo(codSubprocesso);
    }

    @Transactional(readOnly = true)
    public MapaCompletoDto obterMapaCompleto(Long codMapa, Long codSubprocesso) {
        log.debug("Obtendo mapa completo: codigo={}, codSubprocesso={}", codMapa, codSubprocesso);

        Mapa mapa =
                mapaRepo.findById(codMapa)
                        .orElseThrow(
                                () ->
                                        new ErroEntidadeNaoEncontrada(
                                                "Mapa não encontrado: %d".formatted(codMapa)));

        List<Competencia> competencias = competenciaRepo.findByMapaCodigo(codMapa);

        return mapaCompletoMapper.toDto(mapa, codSubprocesso, competencias);
    }

    public MapaCompletoDto salvarMapaCompleto(
            Long codMapa, SalvarMapaRequest request, String usuarioTituloEleitoral) {
        log.info("Salvando mapa completo: codigo={}, usuario={}", codMapa, usuarioTituloEleitoral);

        Mapa mapa =
                mapaRepo.findById(codMapa)
                        .orElseThrow(
                                () ->
                                        new ErroEntidadeNaoEncontrada(
                                                "Mapa não encontrado: %d".formatted(codMapa)));

        var sanitizedObservacoes = SanitizacaoUtil.sanitizar(request.getObservacoes());
        mapa.setObservacoesDisponibilizacao(sanitizedObservacoes);
        mapa = mapaRepo.save(mapa);

        // 1. Carregar dados existentes
        // Carrega competências existentes
        List<Competencia> competenciasExistentes = competenciaRepo.findByMapaCodigo(codMapa);
        Map<Long, Competencia> mapCompetencias = competenciasExistentes.stream()
                .collect(Collectors.toMap(Competencia::getCodigo, Function.identity()));

        // Carrega todas as atividades do mapa para atualização de relacionamentos (lado Owning)
        // Usa fetch join para carregar relacionamentos atuais e evitar N+1 na verificação
        List<Atividade> atividadesDoMapa = atividadeRepo.findByMapaCodigo(codMapa);
        Map<Long, Atividade> mapAtividades = atividadesDoMapa.stream()
                .collect(Collectors.toMap(Atividade::getCodigo, Function.identity()));

        // 2. Identificar exclusões de competências
        Set<Long> idsNovos = request.getCompetencias().stream()
                .map(CompetenciaMapaDto::getCodigo)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<Competencia> toDelete = competenciasExistentes.stream()
                .filter(c -> !idsNovos.contains(c.getCodigo()))
                .toList();

        if (!toDelete.isEmpty()) {
            competenciaRepo.deleteAll(toDelete);
            toDelete.forEach(c -> mapCompetencias.remove(c.getCodigo()));
            log.debug("Removidas {} competências do mapa {}", toDelete.size(), codMapa);
        }

        // 3. Processar Inserções e Atualizações de Competências
        List<Competencia> competenciasParaSalvar = new ArrayList<>();
        // Lista paralela para manter a ordem dos DTOs e vincular corretamente após o saveAll
        List<CompetenciaMapaDto> dtosParaSalvar = new ArrayList<>();

        for (CompetenciaMapaDto dto : request.getCompetencias()) {
            Competencia competencia;
            if (dto.getCodigo() == null) {
                competencia = new Competencia();
                competencia.setMapa(mapa);
            } else {
                competencia = mapCompetencias.get(dto.getCodigo());
                if (competencia == null) {
                    throw new ErroEntidadeNaoEncontrada(
                            "Competência não encontrada no mapa: %d".formatted(dto.getCodigo()));
                }
            }
            competencia.setDescricao(dto.getDescricao());
            competenciasParaSalvar.add(competencia);
            dtosParaSalvar.add(dto);
        }

        // Salvar todas as competências (Batch Save). As novas ganharão IDs.
        List<Competencia> competenciasSalvas = competenciaRepo.saveAll(competenciasParaSalvar);

        // 4. Atualizar Relacionamentos (Lado Owning: Atividade)
        // Construir o estado desejado: AtividadeID -> Set<Competencia>
        Map<Long, Set<Competencia>> estadoDesejado = new HashMap<>();

        // Inicializa o mapa com conjuntos vazios para todas atividades do mapa
        for (Atividade ativ : atividadesDoMapa) {
            estadoDesejado.put(ativ.getCodigo(), new HashSet<>());
        }

        // Popula com o estado vindo do request
        for (int i = 0; i < competenciasSalvas.size(); i++) {
            Competencia comp = competenciasSalvas.get(i);
            CompetenciaMapaDto dto = dtosParaSalvar.get(i);

            if (dto.getAtividadesCodigos() != null) {
                for (Long ativId : dto.getAtividadesCodigos()) {
                    // Só considera atividades que pertencem ao mapa
                    if (estadoDesejado.containsKey(ativId)) {
                        estadoDesejado.get(ativId).add(comp);
                    } else {
                        // Se o ID da atividade não está no mapa, é um erro de validação
                        // pois atividades devem pertencer ao mesmo mapa da competência.
                        throw new ErroValidacao(
                            "Atividade %d não pertence ao mapa %d".formatted(ativId, codMapa));
                    }
                }
            }
        }

        // Aplica as mudanças nas Atividades
        List<Atividade> atividadesAlteradas = new ArrayList<>();
        for (Atividade ativ : atividadesDoMapa) {
            Set<Competencia> novoSet = estadoDesejado.get(ativ.getCodigo());
            // Se houver mudança, atualiza e marca para salvar
            // Usamos equals do Set que compara conteúdo
            if (!ativ.getCompetencias().equals(novoSet)) {
                ativ.setCompetencias(novoSet);
                atividadesAlteradas.add(ativ);
            }
        }

        // Salvar atividades alteradas (Batch Save)
        if (!atividadesAlteradas.isEmpty()) {
            atividadeRepo.saveAll(atividadesAlteradas);
            log.debug("Atualizados relacionamentos em {} atividades", atividadesAlteradas.size());
        }

        validarIntegridadeMapa(codMapa);

        // Recarrega para retorno (ou usa a lista salva se confiável, mas fetch pode ser necessário)
        List<Competencia> competenciasFinais = competenciaRepo.findByMapaCodigo(codMapa);

        return mapaCompletoMapper.toDto(mapa, null, competenciasFinais);
    }

    /**
     * Valida a integridade de um mapa, verificando se existem atividades ou competências órfãs.
     *
     * <p>Este método loga avisos (warnings) para:
     *
     * <ul>
     *   <li>Atividades que não estão vinculadas a nenhuma competência.
     *   <li>Competências que não estão vinculadas a nenhuma atividade.
     * </ul>
     *
     * <p>Nota: Esta é uma validação defensiva. Em operação normal, não deve haver atividades ou
     * competências órfãs se as camadas de negócio estiverem corretamente configuradas. Serve como
     * proteção contra dados inconsistentes e para diagnosticar problemas.
     *
     * @param codMapa O código do mapa a ser validado.
     */
    private void validarIntegridadeMapa(Long codMapa) {
        List<Atividade> atividades = atividadeRepo.findByMapaCodigo(codMapa);
        List<Competencia> competencias = competenciaRepo.findByMapaCodigo(codMapa);

        for (Atividade atividade : atividades) {
            if (atividade.getCompetencias().isEmpty()) {
                log.warn(
                        "Atividade {} não vinculada a nenhuma competência no mapa {}",
                        atividade.getCodigo(),
                        codMapa);
            }
        }

        for (Competencia competencia : competencias) {
            if (competencia.getAtividades().isEmpty()) {
                log.warn(
                        "Competência {} sem atividades vinculadas no mapa {}",
                        competencia.getCodigo(),
                        codMapa);
            }
        }
    }
}
