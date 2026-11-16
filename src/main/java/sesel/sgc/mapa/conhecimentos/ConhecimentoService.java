package sesel.sgc.mapa.conhecimentos;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sesel.sgc.mapa.ValidacaoErro;
import sesel.sgc.mapa.atividades.Atividade;
import sesel.sgc.mapa.atividades.AtividadeDto;
import sesel.sgc.mapa.atividades.AtividadeRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Serviço para gerenciar operações relacionadas a Conhecimentos.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ConhecimentoService {
    private final ConhecimentoRepository conhecimentoRepository;
    private final AtividadeRepository atividadeRepository;

    /**
     * Salva um conhecimento para uma determinada atividade.
     *
     * @param atividadeId     o ID da atividade.
     * @param conhecimentoDto o DTO do conhecimento a ser salvo.
     * @return o DTO do conhecimento salvo.
     */
    public AtividadeDto.ConhecimentoDto salvarConhecimento(UUID atividadeId,
                                                           AtividadeDto.ConhecimentoDto conhecimentoDto) {
        Atividade atividade = buscarAtividadePorId(atividadeId);
        Conhecimento conhecimento;

        if (conhecimentoDto.getId() != null) {
            // Atualização de conhecimento existente
            conhecimento = buscarConhecimentoPorId(conhecimentoDto.getId());
            if (!conhecimento.getAtividade().getId().equals(atividadeId)) {
                throw new ValidacaoErro("O conhecimento não pertence à atividade informada");
            }
            conhecimentoDto.updateEntity(conhecimento);
        } else {
            // Criação de novo conhecimento
            conhecimento = conhecimentoDto.toEntity();
            conhecimento.setAtividade(atividade);
        }

        Conhecimento conhecimentoSalvo = conhecimentoRepository.save(conhecimento);
        return AtividadeDto.ConhecimentoDto.fromEntity(conhecimentoSalvo);
    }

    /**
     * Busca um conhecimento pelo seu ID e ID da atividade.
     *
     * @param conhecimentoId o ID do conhecimento.
     * @param atividadeId    o ID da atividade.
     * @return um Optional contendo o DTO do conhecimento, se encontrado.
     */
    @Transactional(readOnly = true)
    public Optional<AtividadeDto.ConhecimentoDto> buscarConhecimentoPorIdEAtividadeId(UUID conhecimentoId,
                                                                                      UUID atividadeId) {
        return conhecimentoRepository.findByIdAndAtividadeId(conhecimentoId, atividadeId)
                .map(AtividadeDto.ConhecimentoDto::fromEntity);
    }

    /**
     * Exclui um conhecimento pelo seu ID e ID da atividade.
     *
     * @param conhecimentoId o ID do conhecimento a ser excluído.
     * @param atividadeId    o ID da atividade.
     */
    public void excluirConhecimento(UUID conhecimentoId, UUID atividadeId) {
        Conhecimento conhecimento = buscarConhecimentoPorId(conhecimentoId);

        if (!conhecimento.getAtividade().getId().equals(atividadeId)) {
            throw new ValidacaoErro("Conhecimento não pertence à atividade informada");
        }

        conhecimentoRepository.delete(conhecimento);
    }

    /**
     * Busca uma atividade pelo ID ou lança uma exceção se não for encontrada.
     */
    private Atividade buscarAtividadePorId(UUID id) {
        return atividadeRepository.findById(id)
                .orElseThrow(() -> new ValidacaoErro("Atividade não encontrada com ID: %s".formatted(id)));
    }

    /**
     * Busca um conhecimento pelo ID ou lança uma exceção se não for encontrado.
     */
    private Conhecimento buscarConhecimentoPorId(UUID id) {
        return conhecimentoRepository.findById(id)
                .orElseThrow(() -> new ValidacaoErro("Conhecimento não encontrado com ID: %s".formatted(id)));
    }
}
