package sgc.mapa.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.dto.ConhecimentoDto;
import sgc.mapa.evento.EventoMapaAlterado;
import sgc.mapa.mapper.ConhecimentoMapper;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.AtividadeRepo;
import sgc.mapa.model.Conhecimento;
import sgc.mapa.model.ConhecimentoRepo;

import java.util.List;

/**
 * Serviço responsável pela lógica de negócios de Conhecimentos.
 *
 * <p>Gerencia o CRUD de conhecimentos associados a atividades, garantindo a
 * consistência do modelo de dados e notificando alterações no mapa.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ConhecimentoService {
    private final ConhecimentoRepo conhecimentoRepo;
    private final AtividadeRepo atividadeRepo;
    private final ConhecimentoMapper conhecimentoMapper;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Lista todos os conhecimentos associados a uma atividade específica.
     *
     * @param codAtividade O código da atividade.
     * @return Uma {@link List} de {@link ConhecimentoDto}.
     * @throws ErroEntidadeNaoEncontrada se a atividade não for encontrada.
     */
    @Transactional(readOnly = true)
    public List<ConhecimentoDto> listarPorAtividade(Long codAtividade) {
        if (!atividadeRepo.existsById(codAtividade)) {
            throw new ErroEntidadeNaoEncontrada("Atividade", codAtividade);
        }
        return conhecimentoRepo.findByAtividadeCodigo(codAtividade).stream()
                .map(conhecimentoMapper::toDto)
                .toList();
    }

    /**
     * Lista conhecimentos de uma atividade (Entidades).
     */
    @Transactional(readOnly = true)
    public List<Conhecimento> listarEntidadesPorAtividade(Long codAtividade) {
        return conhecimentoRepo.findByAtividadeCodigo(codAtividade);
    }

    /**
     * Lista conhecimentos de um mapa (Entidades).
     */
    @Transactional(readOnly = true)
    public List<Conhecimento> listarPorMapa(Long codMapa) {
        return conhecimentoRepo.findByMapaCodigo(codMapa);
    }

    /**
     * Cria um novo conhecimento e o associa a uma atividade existente.
     *
     * @param codAtividade    O código da atividade à qual o conhecimento será associado.
     * @param conhecimentoDto O DTO com os dados do novo conhecimento.
     * @return O {@link ConhecimentoDto} do conhecimento criado.
     * @throws ErroEntidadeNaoEncontrada se a atividade não for encontrada.
     */
    public ConhecimentoDto criar(Long codAtividade, ConhecimentoDto conhecimentoDto) {
        return atividadeRepo.findById(codAtividade)
                .map(atividade -> {
                    notificarAlteracaoMapa(atividade.getMapa().getCodigo());
                    var conhecimento = conhecimentoMapper.toEntity(conhecimentoDto);
                    conhecimento.setAtividade(atividade);
                    var salvo = conhecimentoRepo.save(conhecimento);
                    return conhecimentoMapper.toDto(salvo);
                })
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Atividade", codAtividade));
    }

    /**
     * Atualiza um conhecimento existente, verificando se ele pertence à atividade especificada.
     *
     * @param codAtividade    O código da atividade pai.
     * @param codConhecimento O código do conhecimento a ser atualizado.
     * @param conhecimentoDto O DTO com os novos dados do conhecimento.
     * @throws ErroEntidadeNaoEncontrada se o conhecimento não for encontrado ou não pertencer à
     *                                   atividade.
     */
    public void atualizar(Long codAtividade, Long codConhecimento, ConhecimentoDto conhecimentoDto) {
        conhecimentoRepo.findById(codConhecimento)
                .filter(conhecimento -> conhecimento.getCodigoAtividade().equals(codAtividade))
                .map(existente -> atualizarExistente(conhecimentoDto, existente))
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Conhecimento", codConhecimento));
    }

    private ConhecimentoDto atualizarExistente(ConhecimentoDto dto, Conhecimento existente) {
        notificarAlteracaoMapa(existente.getAtividade().getMapa().getCodigo());

        var paraAtualizar = conhecimentoMapper.toEntity(dto);
        existente.setDescricao(paraAtualizar.getDescricao());

        var atualizado = conhecimentoRepo.save(existente);
        return conhecimentoMapper.toDto(atualizado);
    }

    /**
     * Exclui um conhecimento, verificando se ele pertence à atividade especificada.
     *
     * @param codAtividade    O código da atividade pai.
     * @param codConhecimento O código do conhecimento a ser excluído.
     * @throws ErroEntidadeNaoEncontrada se o conhecimento não for encontrado ou não pertencer à
     *                                   atividade.
     */
    public void excluir(Long codAtividade, Long codConhecimento) {
        conhecimentoRepo.findById(codConhecimento)
                .filter(conhecimento -> conhecimento.getCodigoAtividade().equals(codAtividade))
                .ifPresentOrElse(
                        this::executarExclusao,
                        () -> {
                            throw new ErroEntidadeNaoEncontrada("Conhecimento", codConhecimento);
                        });
    }

    /**
     * Exclui todos os conhecimentos de uma atividade.
     *
     * @param atividade A atividade cujos conhecimentos serão excluídos.
     */
    public void excluirTodosDaAtividade(Atividade atividade) {
        var conhecimentos = conhecimentoRepo.findByAtividadeCodigo(atividade.getCodigo());
        conhecimentoRepo.deleteAll(conhecimentos);
    }

    private void executarExclusao(Conhecimento conhecimento) {
        notificarAlteracaoMapa(conhecimento.getAtividade().getMapa().getCodigo());
        conhecimentoRepo.delete(conhecimento);
    }

    private void notificarAlteracaoMapa(Long mapaCodigo) {
        eventPublisher.publishEvent(new EventoMapaAlterado(mapaCodigo));
    }
}
