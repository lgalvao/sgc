package sgc.atividade;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.atividade.dto.AtividadeDto;
import sgc.atividade.dto.AtividadeMapper;
import sgc.atividade.modelo.AtividadeRepo;
import sgc.comum.erros.ErroDominioAccessoNegado;
import sgc.comum.erros.ErroDominioNaoEncontrado;
import sgc.conhecimento.dto.ConhecimentoDto;
import sgc.conhecimento.dto.ConhecimentoMapper;
import sgc.conhecimento.modelo.ConhecimentoRepo;
import sgc.sgrh.UsuarioRepo;
import sgc.subprocesso.modelo.SubprocessoRepo;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
/**
 * Serviço para gerenciar a lógica de negócios de Atividades e Conhecimentos.
 */
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
                .map(atividadeMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Busca uma atividade pelo seu ID.
     *
     * @param idAtividade O ID da atividade.
     * @return O {@link AtividadeDto} correspondente.
     * @throws ErroDominioNaoEncontrado se a atividade não for encontrada.
     */
    public AtividadeDto obterPorId(Long idAtividade) {
        return atividadeRepo.findById(idAtividade)
                .map(atividadeMapper::toDTO)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Atividade", idAtividade));
    }

    /**
     * Cria uma nova atividade, realizando validações de segurança e de estado do subprocesso.
     *
     * @param atividadeDto O DTO com os dados da nova atividade.
     * @param username     O nome de usuário (título de eleitor) do usuário que está criando a atividade.
     * @return O {@link AtividadeDto} da atividade criada.
     * @throws ErroDominioNaoEncontrado se o subprocesso ou o usuário não forem encontrados.
     * @throws ErroDominioAccessoNegado se o usuário não for o titular da unidade do subprocesso.
     * @throws IllegalStateException se o subprocesso já estiver finalizado.
     */
    public AtividadeDto criar(AtividadeDto atividadeDto, String username) {
        var subprocesso = subprocessoRepo.findByMapaCodigo(atividadeDto.mapaCodigo())
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado para o mapa com código %d".formatted(atividadeDto.mapaCodigo())));

        var usuario = usuarioRepo.findByTituloEleitoral(Long.parseLong(username))
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Usuário", username));

        if (!usuario.equals(subprocesso.getUnidade().getTitular())) {
            throw new ErroDominioAccessoNegado("Usuário não autorizado a criar atividades para este subprocesso.");
        }
        if (subprocesso.getSituacao().isFinalizado()) {
            throw new IllegalStateException("Subprocesso já está finalizado.");
        }

        var entidade = atividadeMapper.toEntity(atividadeDto);
        var salvo = atividadeRepo.save(entidade);
        return atividadeMapper.toDTO(salvo);
    }

    /**
     * Atualiza uma atividade existente.
     *
     * @param id           O ID da atividade a ser atualizada.
     * @param atividadeDto O DTO com os novos dados da atividade.
     * @return O {@link AtividadeDto} da atividade atualizada.
     * @throws ErroDominioNaoEncontrado se a atividade não for encontrada.
     */
    public AtividadeDto atualizar(Long id, AtividadeDto atividadeDto) {
        return atividadeRepo.findById(id)
                .map(existente -> {
                    var entidadeParaAtualizar = atividadeMapper.toEntity(atividadeDto);
                    existente.setDescricao(entidadeParaAtualizar.getDescricao());
                    existente.setMapa(entidadeParaAtualizar.getMapa());

                    var atualizado = atividadeRepo.save(existente);
                    return atividadeMapper.toDTO(atualizado);
                })
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Atividade", id));
    }

    /**
     * Exclui uma atividade e todos os seus conhecimentos associados.
     *
     * @param idAtividade O ID da atividade a ser excluída.
     * @throws ErroDominioNaoEncontrado se a atividade não for encontrada.
     */
    public void excluir(Long idAtividade) {
        atividadeRepo.findById(idAtividade).ifPresentOrElse(atividade -> {
            var conhecimentos = conhecimentoRepo.findByAtividadeCodigo(atividade.getCodigo());
            conhecimentoRepo.deleteAll(conhecimentos);
            atividadeRepo.delete(atividade);
        }, () -> {
            throw new ErroDominioNaoEncontrado("Atividade", idAtividade);
        });
    }

    /**
     * Lista todos os conhecimentos associados a uma atividade específica.
     *
     * @param idAtividade O ID da atividade.
     * @return Uma {@link List} de {@link ConhecimentoDto}.
     * @throws ErroDominioNaoEncontrado se a atividade não for encontrada.
     */
    public List<ConhecimentoDto> listarConhecimentos(Long idAtividade) {
        if (!atividadeRepo.existsById(idAtividade)) {
            throw new ErroDominioNaoEncontrado("Atividade", idAtividade);
        }
        return conhecimentoRepo.findByAtividadeCodigo(idAtividade)
                .stream()
                .map(conhecimentoMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Cria um novo conhecimento e o associa a uma atividade existente.
     *
     * @param idAtividade     O ID da atividade à qual o conhecimento será associado.
     * @param conhecimentoDto O DTO com os dados do novo conhecimento.
     * @return O {@link ConhecimentoDto} do conhecimento criado.
     * @throws ErroDominioNaoEncontrado se a atividade não for encontrada.
     */
    public ConhecimentoDto criarConhecimento(Long idAtividade, ConhecimentoDto conhecimentoDto) {
        return atividadeRepo.findById(idAtividade)
                .map(atividade -> {
                    var conhecimento = conhecimentoMapper.toEntity(conhecimentoDto);
                    conhecimento.setAtividade(atividade);
                    var salvo = conhecimentoRepo.save(conhecimento);
                    return conhecimentoMapper.toDTO(salvo);
                })
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Atividade", idAtividade));
    }

    /**
     * Atualiza um conhecimento existente, verificando se ele pertence à atividade especificada.
     *
     * @param idAtividade     O ID da atividade pai.
     * @param idConhecimento  O ID do conhecimento a ser atualizado.
     * @param conhecimentoDto O DTO com os novos dados do conhecimento.
     * @return O {@link ConhecimentoDto} do conhecimento atualizado.
     * @throws ErroDominioNaoEncontrado se o conhecimento não for encontrado ou não pertencer à atividade.
     */
    public ConhecimentoDto atualizarConhecimento(Long idAtividade, Long idConhecimento, ConhecimentoDto conhecimentoDto) {
        return conhecimentoRepo.findById(idConhecimento)
                .filter(conhecimento -> conhecimento.getCodigoAtividade().equals(idAtividade))
                .map(existente -> {
                    var paraAtualizar = conhecimentoMapper.toEntity(conhecimentoDto);
                    existente.setDescricao(paraAtualizar.getDescricao());
                    var atualizado = conhecimentoRepo.save(existente);
                    return conhecimentoMapper.toDTO(atualizado);
                })
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Conhecimento", idConhecimento));
    }

    /**
     * Exclui um conhecimento, verificando se ele pertence à atividade especificada.
     *
     * @param idAtividade    O ID da atividade pai.
     * @param idConhecimento O ID do conhecimento a ser excluído.
     * @throws ErroDominioNaoEncontrado se o conhecimento não for encontrado ou não pertencer à atividade.
     */
    public void excluirConhecimento(Long idAtividade, Long idConhecimento) {
        conhecimentoRepo.findById(idConhecimento)
                .filter(conhecimento -> conhecimento.getCodigoAtividade().equals(idAtividade))
                .ifPresentOrElse(conhecimentoRepo::delete, () -> {
                    throw new ErroDominioNaoEncontrado("Conhecimento", idConhecimento);
                });
    }
}
