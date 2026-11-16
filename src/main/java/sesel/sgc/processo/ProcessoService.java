package sesel.sgc.processo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import sesel.sgc.mapa.MapaUnidadeSituacao;
import sesel.sgc.processounidade.ProcessoUnidade;
import sesel.sgc.processounidade.ProcessoUnidadeRepository;
import sesel.sgc.sgrh.SgrhService;
import sesel.sgc.unidade.Unidade;
import sesel.sgc.unidade.UnidadeRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static sesel.sgc.mapa.MapaUnidadeSituacao.FINALIZADA;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessoService {
    private final ProcessoRepository processoRepository;
    private final ProcessoUnidadeRepository processoUnidadeRepository;
    private final UnidadeRepository unidadeRepository;
    private final SgrhService sgrhService;

    @Transactional(readOnly = true)
    public List<ProcessoDto> listarTodosProcessosDTO() {
        return processoRepository.findAll().stream()
                .map(processo -> {
                    UUID processoId = processo.getId();
                    ProcessoDto dto = ProcessoDto.fromEntity(processo,
                            processoRepository.countProcessoUnidadesByProcessoId(processoId));
                    dto.setPercentualConclusao(calcularPercentualConclusao(processoId));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public Processo criarProcesso(Processo processo) {
        log.info("Criando novo processo");
        log.info("Processo recebido: nome={}, dataLimite={}, unidadesParticipantes={}", processo.getNome(),
                processo.getDataLimite(), processo.getUnidadesParticipantes());

        sgrhService.sincronizarDados();

        Set<UUID> unidadesIds = processo.getUnidadesParticipantes();
        if (unidadesIds == null || unidadesIds.isEmpty()) {
            log.warn("Nenhuma unidade participante foi selecionada!");
        }

        Set<Unidade> unidades = unidadesIds != null ? unidadesIds.stream()
                .map(id -> unidadeRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Unidade não encontrada: " + id)))
                .collect(Collectors.toSet()) : Set.of();

        log.info("Unidades: {}", unidades);

        unidades.forEach(unidade -> {
            ProcessoUnidade processoUnidade = new ProcessoUnidade();
            processoUnidade.setUnidade(unidade);
            processoUnidade.setProcesso(processo);
            processoUnidade.setSituacao(MapaUnidadeSituacao.NAO_INICIADA);
            processoUnidade.setDataHoraInicioRegistro(LocalDateTime.now());
            processo.getProcessosUnidades().add(processoUnidade);
        });

        Processo processoSalvo = processoRepository.saveAndFlush(processo);
        log.info("Processo criado: {}", processoSalvo);
        return processoSalvo;
    }

    @Transactional(readOnly = true)
    public List<Unidade> listarUnidadesDisponiveis() {
        return unidadeRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Processo buscarPorId(UUID id) {
        return processoRepository.findByIdWithProcessoUnidades(id)
                .orElseThrow(() -> new ProcessoNaoEncontradoErro(id));
    }

    @Transactional(readOnly = true)
    public ProcessoDto buscarProcessoDTO(UUID id) {
        Processo processo = processoRepository.findByIdWithProcessoUnidadesAndUnidades(id)
                .orElseThrow(() -> new ProcessoNaoEncontradoErro(id));

        long totalUnidades = processo.getProcessosUnidades() != null ? processo.getProcessosUnidades().size() : 0;
        ProcessoDto dto = ProcessoDto.fromEntity(processo, totalUnidades);
        dto.setPercentualConclusao(calcularPercentualConclusao(id));
        return dto;
    }

    @Transactional(readOnly = true)
    public int calcularPercentualConclusao(Processo processo) {
        Assert.notNull(processo, "Processo não pode ser nulo");
        return calcularPercentualConclusao(processo.getId());
    }

    @Transactional(readOnly = true)
    public int calcularPercentualConclusao(UUID processoId) {
        long totalUnidades = processoRepository.countProcessoUnidadesByProcessoId(processoId);
        if (totalUnidades == 0) return 0;

        long unidadesFinalizadas = processoRepository.countProcessoUnidadesByProcessoIdAndSituacao(processoId, FINALIZADA);
        return (int) Math.round((unidadesFinalizadas * 100.0) / totalUnidades);
    }

    @Transactional(readOnly = true)
    public ProcessoUnidade buscarProcessoUnidade(UUID processoId, UUID unidadeId) {
        log.debug("Buscando processoUnidade para processo {} e unidade {}", processoId, unidadeId);
        return processoUnidadeRepository.findByProcessoIdAndUnidadeId(processoId, unidadeId)
                .orElseThrow(() -> new IllegalArgumentException(String.format(
                        "ProcessoUnidade não encontrada para processo %s e unidade %s", processoId, unidadeId))
                );
    }

    @Transactional(readOnly = true)
    public long contarProcessos() {
        return processoRepository.count();
    }

    @Transactional(readOnly = true)
    public long contarProcessosFinalizados() {
        return processoRepository.countBySituacao(ProcessoSituacao.FINALIZADO);
    }

    @Transactional
    public void finalizarProcessoUnidade(UUID processoId, UUID unidadeId) {
        log.info("Finalizando processoUnidade do processo {} para unidade {}", processoId, unidadeId);

        ProcessoUnidade processoUnidade = buscarProcessoUnidade(processoId, unidadeId);
        processoUnidade.setSituacao(MapaUnidadeSituacao.FINALIZADA);
        processoUnidade.setDataHoraFimRegistro(LocalDateTime.now());
        processoUnidadeRepository.save(processoUnidade);
    }

    @Transactional
    public void reabrirProcessoUnidade(UUID processoId, UUID unidadeId) {
        log.info("Reabrindo processoUnidade do processo {} para unidade {}", processoId, unidadeId);

        ProcessoUnidade processoUnidade = buscarProcessoUnidade(processoId, unidadeId);
        processoUnidade.setSituacao(MapaUnidadeSituacao.EM_ANDAMENTO);
        processoUnidade.setDataHoraFimRegistro(null);
        processoUnidadeRepository.save(processoUnidade);
    }

    @Transactional(readOnly = true)
    public List<ProcessoUnidade> listarProcessosUnidade(UUID processoId) {
        return processoUnidadeRepository.findByProcessoId(processoId);
    }
}
