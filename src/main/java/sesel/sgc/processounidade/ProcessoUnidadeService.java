package sesel.sgc.processounidade;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sesel.sgc.mapa.MapaUnidadeSituacao;
import sesel.sgc.mapa.atividades.AtividadeRepository;
import sesel.sgc.processo.ProcessoDto;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProcessoUnidadeService {
    private final ProcessoUnidadeRepository processoUnidadeRepository;
    private final AtividadeRepository atividadeRepository;

    @Transactional(readOnly = true)
    public ProcessoUnidade buscarProcessoUnidade(UUID processoId, UUID unidadeId) {
        return processoUnidadeRepository.findByProcessoIdAndUnidadeId(processoId, unidadeId)
                .orElseThrow(() -> new ProcessoUnidadeNaoEncontradoErro(
                        String.format("Processo de unidade não encontrada para o processo %s e unidade %s",
                                processoId != null ? processoId.toString() : "null",
                                unidadeId != null ? unidadeId.toString() : "null")));
    }

    @Transactional(readOnly = true)
    public ProcessoUnidade buscarProcessoUnidadeDetalhada(UUID processoId, UUID unidadeId) {
        return buscarProcessoUnidade(processoId, unidadeId);
    }

    @Transactional
    public void finalizarProcessoUnidade(UUID processoId, UUID unidadeId) {
        ProcessoUnidade processoUnidade = buscarProcessoUnidade(processoId, unidadeId);

        // Validação: só permite finalizar se houver pelo menos uma atividade
        var atividades = atividadeRepository.findByProcessoUnidadeIdComConhecimentos(processoUnidade.getId());
        if (atividades == null || atividades.isEmpty()) {
            throw new IllegalStateException("Não é possível finalizar: cadastre pelo menos uma atividade.");
        }

        processoUnidade.setSituacao(MapaUnidadeSituacao.FINALIZADA);
        processoUnidade.setDataHoraFimRegistro(LocalDateTime.now());
        processoUnidadeRepository.save(processoUnidade);
    }

    @Transactional
    public void reabrirProcessoUnidade(UUID processoId, UUID unidadeId) {
        ProcessoUnidade processoUnidade = buscarProcessoUnidade(processoId, unidadeId);
        processoUnidade.setSituacao(MapaUnidadeSituacao.EM_ANDAMENTO);
        processoUnidade.setDataHoraFimRegistro(null);
        processoUnidadeRepository.save(processoUnidade);
    }

    @Transactional(readOnly = true)
    public ProcessoDto.ProcessoUnidadeDto toProcessoUnidadeDto(ProcessoUnidade processoUnidade) {
        return ProcessoDto.ProcessoUnidadeDto.fromEntity(processoUnidade);
    }

    /**
     * Busca uma Processo de unidade pelo ID.
     *
     * @param id ID da Processo de unidade
     * @return A Processo de unidade encontrada
     * @throws ProcessoUnidadeNaoEncontradoErro Se a Processo de unidade não for
     *                                          encontrada
     */
    @Transactional(readOnly = true)
    public ProcessoUnidade buscarProcessoUnidadePorId(UUID id) {
        return processoUnidadeRepository.findById(id).orElseThrow(() -> new ProcessoUnidadeNaoEncontradoErro(
                String.format("Processo de unidade não encontrada com o ID: %s", id)));
    }

    /**
     * Atualiza uma Processo de unidade existente.
     *
     * @param processoUnidade A Processo de unidade com os dados atualizados
     */
    @Transactional
    public void atualizarProcessoUnidade(ProcessoUnidade processoUnidade) {
        if (processoUnidade == null || processoUnidade.getId() == null) {
            throw new IllegalArgumentException("Processo de unidade inválida");
        }

        // Verifica se a Processo de unidade existe
        if (!processoUnidadeRepository.existsById(processoUnidade.getId())) {
            throw new ProcessoUnidadeNaoEncontradoErro(
                    String.format("Processo de unidade não encontrada com o ID: %s", processoUnidade.getId()));
        }

        processoUnidadeRepository.save(processoUnidade);
    }
}
