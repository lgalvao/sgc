package sgc.mapa.service;

import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.dto.AtualizarConhecimentoRequest;
import sgc.mapa.dto.ConhecimentoResponse;
import sgc.mapa.dto.CriarConhecimentoRequest;
import sgc.mapa.evento.EventoMapaAlterado;
import sgc.mapa.mapper.ConhecimentoMapper;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.AtividadeRepo;
import sgc.mapa.model.Conhecimento;
import sgc.mapa.model.ConhecimentoRepo;

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
     * @return Uma {@link List} de {@link ConhecimentoResponse}.
     * @throws ErroEntidadeNaoEncontrada se a atividade não for encontrada.
     */
    @Transactional(readOnly = true)
    public List<ConhecimentoResponse> listarPorAtividade(Long codAtividade) {
        if (!atividadeRepo.existsById(codAtividade)) {
            throw new ErroEntidadeNaoEncontrada("Atividade", codAtividade);
        }
        return conhecimentoRepo.findByAtividadeCodigo(codAtividade).stream()
                .map(conhecimentoMapper::toResponse)
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

    public ConhecimentoResponse criar(Long codAtividade, CriarConhecimentoRequest request) {
        return atividadeRepo.findById(codAtividade)
                .map(atividade -> {
                    var mapa = atividade.getMapa();
                    if (mapa != null) {
                        notificarAlteracaoMapa(mapa.getCodigo());
                    }
                    var conhecimento = conhecimentoMapper.toEntity(request);
                    conhecimento.setAtividade(atividade);
                    var salvo = conhecimentoRepo.save(conhecimento);
                    return conhecimentoMapper.toResponse(salvo);
                })
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Atividade", codAtividade));
    }

    public void atualizar(Long codAtividade, Long codConhecimento, AtualizarConhecimentoRequest request) {
        conhecimentoRepo.findById(codConhecimento)
                .filter(conhecimento -> conhecimento.getCodigoAtividade().equals(codAtividade))
                .ifPresentOrElse(
                        existente -> {
                            var mapa = existente.getAtividade().getMapa();
                            if (mapa != null) {
                                notificarAlteracaoMapa(mapa.getCodigo());
                            }
                            var paraAtualizar = conhecimentoMapper.toEntity(request);
                            existente.setDescricao(paraAtualizar.getDescricao());
                            conhecimentoRepo.save(existente);
                        },
                        () -> {
                            throw new ErroEntidadeNaoEncontrada("Conhecimento", codConhecimento);
                        });
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
        var mapa = conhecimento.getAtividade().getMapa();
        if (mapa != null) {
            notificarAlteracaoMapa(mapa.getCodigo());
        }
        conhecimentoRepo.delete(conhecimento);
    }

    private void notificarAlteracaoMapa(Long mapaCodigo) {
        eventPublisher.publishEvent(new EventoMapaAlterado(mapaCodigo));
    }
}
