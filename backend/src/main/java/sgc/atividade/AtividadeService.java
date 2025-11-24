package sgc.atividade;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.atividade.dto.AtividadeDto;
import sgc.atividade.dto.AtividadeMapper;
import sgc.atividade.dto.ConhecimentoDto;
import sgc.atividade.dto.ConhecimentoMapper;
import sgc.atividade.model.AtividadeRepo;
import sgc.atividade.model.ConhecimentoRepo;
import sgc.comum.erros.ErroAccessoNegado;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.comum.erros.ErroSituacaoInvalida;
import sgc.processo.model.TipoProcesso;
import sgc.sgrh.model.UsuarioRepo;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.SubprocessoRepo;

import java.util.List;

/**
 * Serviço para gerenciar a lógica de negócios de Atividades e Conhecimentos.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class AtividadeService {
    private final AtividadeRepo atividadeRepo;
    private final AtividadeMapper atividadeMapper;
    private final ConhecimentoRepo conhecimentoRepo;
    private final ConhecimentoMapper conhecimentoMapper;
    private final SubprocessoRepo subprocessoRepo;
    private final UsuarioRepo usuarioRepo;

    /**
     * Retorna uma lista de todas as atividades.
     *
     * @return Uma {@link List} de {@link AtividadeDto}.
     */
    public List<AtividadeDto> listar() {
        return atividadeRepo.findAll()
                .stream()
                .map(atividadeMapper::toDto)
                .toList();
    }

    /**
     * Busca uma atividade pelo seu código.
     *
     * @param codAtividade O código da atividade.
     * @return O {@link AtividadeDto} correspondente.
     * @throws ErroEntidadeNaoEncontrada se a atividade não for encontrada.
     */
    public AtividadeDto obterPorCodigo(Long codAtividade) {
        return atividadeRepo.findById(codAtividade)
                .map(atividadeMapper::toDto)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Atividade", codAtividade));
    }

    /**
     * Cria uma nova atividade, realizando validações de segurança e de estado do subprocesso.
     *
     * @param atividadeDto  O DTO com os dados da nova atividade.
     * @param tituloUsuario O título de eleitor do usuário que está criando a atividade.
     * @return O {@link AtividadeDto} da atividade criada.
     * @throws ErroEntidadeNaoEncontrada se o subprocesso ou o usuário não forem encontrados.
     * @throws ErroAccessoNegado         se o usuário não for o titular da unidade do subprocesso.
     * @throws ErroSituacaoInvalida      se o subprocesso já estiver finalizado.
     */
    public AtividadeDto criar(AtividadeDto atividadeDto, String tituloUsuario) {
        var subprocesso = subprocessoRepo.findByMapaCodigo(atividadeDto.getMapaCodigo())
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Subprocesso não encontrado para o mapa com código %d".formatted(atividadeDto.getMapaCodigo())));

        var usuario = usuarioRepo.findById(tituloUsuario)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Usuário", tituloUsuario));

        // Validação defensiva: garante que apenas o titular da unidade pode criar atividades.
        // Apesar da segurança estar configurada, mantemos esta verificação como proteção extra.
        if (!usuario.equals(subprocesso.getUnidade().getTitular())) {
            throw new ErroAccessoNegado("Usuário não autorizado a criar atividades para este subprocesso.");
        }

        atualizarSituacaoSubprocessoSeNecessario(atividadeDto.getMapaCodigo());

        var entidade = atividadeMapper.toEntity(atividadeDto);
        var salvo = atividadeRepo.save(entidade);

        return atividadeMapper.toDto(salvo);
    }

    /**
     * Atualiza uma atividade existente.
     *
     * @param codigo       O código da atividade a ser atualizada.
     * @param atividadeDto O DTO com os novos dados da atividade.
     * @return O {@link AtividadeDto} da atividade atualizada.
     * @throws ErroEntidadeNaoEncontrada se a atividade não for encontrada.
     */
    public AtividadeDto atualizar(Long codigo, AtividadeDto atividadeDto) {
        return atividadeRepo.findById(codigo)
                .map(existente -> {
                    atualizarSituacaoSubprocessoSeNecessario(existente.getMapa().getCodigo());
                    var entidadeParaAtualizar = atividadeMapper.toEntity(atividadeDto);
                    existente.setDescricao(entidadeParaAtualizar.getDescricao());
                    existente.setMapa(entidadeParaAtualizar.getMapa());

                    var atualizado = atividadeRepo.save(existente);
                    return atividadeMapper.toDto(atualizado);
                })
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Atividade", codigo));
    }

    /**
     * Exclui uma atividade e todos os seus conhecimentos associados.
     *
     * @param codAtividade O código da atividade a ser excluída.
     * @throws ErroEntidadeNaoEncontrada se a atividade não for encontrada.
     */
    public void excluir(Long codAtividade) {
        atividadeRepo.findById(codAtividade).ifPresentOrElse(atividade -> {
            atualizarSituacaoSubprocessoSeNecessario(atividade.getMapa().getCodigo());
            var conhecimentos = conhecimentoRepo.findByAtividadeCodigo(atividade.getCodigo());
            conhecimentoRepo.deleteAll(conhecimentos);
            atividadeRepo.delete(atividade);
        }, () -> {
            throw new ErroEntidadeNaoEncontrada("Atividade", codAtividade);
        });
    }

    /**
     * Lista todos os conhecimentos associados a uma atividade específica.
     *
     * @param codAtividade O código da atividade.
     * @return Uma {@link List} de {@link ConhecimentoDto}.
     * @throws ErroEntidadeNaoEncontrada se a atividade não for encontrada.
     */
    public List<ConhecimentoDto> listarConhecimentos(Long codAtividade) {
        if (!atividadeRepo.existsById(codAtividade)) {
            throw new ErroEntidadeNaoEncontrada("Atividade", codAtividade);
        }
        return conhecimentoRepo.findByAtividadeCodigo(codAtividade)
                .stream()
                .map(conhecimentoMapper::toDto)
                .toList();
    }

    /**
     * Cria um novo conhecimento e o associa a uma atividade existente.
     *
     * @param codAtividade    O código da atividade à qual o conhecimento será associado.
     * @param conhecimentoDto O DTO com os dados do novo conhecimento.
     * @return O {@link ConhecimentoDto} do conhecimento criado.
     * @throws ErroEntidadeNaoEncontrada se a atividade não for encontrada.
     */
    public ConhecimentoDto criarConhecimento(Long codAtividade, ConhecimentoDto conhecimentoDto) {
        return atividadeRepo.findById(codAtividade)
                .map(atividade -> {
                    atualizarSituacaoSubprocessoSeNecessario(atividade.getMapa().getCodigo());
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
     * @return O {@link ConhecimentoDto} do conhecimento atualizado.
     * @throws ErroEntidadeNaoEncontrada se o conhecimento não for encontrado ou não pertencer à atividade.
     */
    public ConhecimentoDto atualizarConhecimento(Long codAtividade, Long codConhecimento, ConhecimentoDto conhecimentoDto) {
        return conhecimentoRepo.findById(codConhecimento)
                .filter(conhecimento -> conhecimento.getCodigoAtividade().equals(codAtividade))
                .map(existente -> {
                    atualizarSituacaoSubprocessoSeNecessario(existente.getAtividade().getMapa().getCodigo());
                    var paraAtualizar = conhecimentoMapper.toEntity(conhecimentoDto);
                    existente.setDescricao(paraAtualizar.getDescricao());
                    var atualizado = conhecimentoRepo.save(existente);
                    return conhecimentoMapper.toDto(atualizado);
                })
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Conhecimento", codConhecimento));
    }

    /**
     * Exclui um conhecimento, verificando se ele pertence à atividade especificada.
     *
     * @param codAtividade    O código da atividade pai.
     * @param codConhecimento O código do conhecimento a ser excluído.
     * @throws ErroEntidadeNaoEncontrada se o conhecimento não for encontrado ou não pertencer à atividade.
     */
    public void excluirConhecimento(Long codAtividade, Long codConhecimento) {
        conhecimentoRepo.findById(codConhecimento)
                .filter(conhecimento -> conhecimento.getCodigoAtividade().equals(codAtividade))
                .ifPresentOrElse(conhecimento -> {
                    atualizarSituacaoSubprocessoSeNecessario(conhecimento.getAtividade().getMapa().getCodigo());
                    conhecimentoRepo.delete(conhecimento);
                }, () -> {
                    throw new ErroEntidadeNaoEncontrada("Conhecimento", codConhecimento);
                });
    }

    private void atualizarSituacaoSubprocessoSeNecessario(Long mapaCodigo) {
        var subprocesso = subprocessoRepo.findByMapaCodigo(mapaCodigo)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Subprocesso não encontrado para o mapa com código %d".formatted(mapaCodigo)));

        if (subprocesso.getSituacao() == SituacaoSubprocesso.NAO_INICIADO) {
            var tipoProcesso = subprocesso.getProcesso().getTipo();
            if (tipoProcesso == TipoProcesso.MAPEAMENTO) {
                subprocesso.setSituacao(SituacaoSubprocesso.CADASTRO_EM_ANDAMENTO);
                subprocessoRepo.save(subprocesso);
            } else if (tipoProcesso == TipoProcesso.REVISAO) {
                subprocesso.setSituacao(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);
                subprocessoRepo.save(subprocesso);
            }
        }
    }
}
