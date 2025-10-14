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
import sgc.conhecimento.modelo.Conhecimento;
import sgc.conhecimento.modelo.ConhecimentoRepo;
import sgc.sgrh.UsuarioRepo;
import sgc.subprocesso.modelo.SubprocessoRepo;

import java.util.List;
import java.util.stream.Collectors;

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

    public List<AtividadeDto> listar() {
        return atividadeRepo.findAll()
            .stream()
            .map(atividadeMapper::toDTO)
            .collect(Collectors.toList());
    }

    public AtividadeDto obterPorId(Long id) {
        return atividadeRepo.findById(id)
            .map(atividadeMapper::toDTO)
            .orElseThrow(() -> new ErroDominioNaoEncontrado("Atividade não encontrada: " + id));
    }

    public AtividadeDto criar(AtividadeDto atividadeDto, String username) {
        var subprocesso = subprocessoRepo.findByMapaCodigo(atividadeDto.mapaCodigo())
            .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado para o mapa com código " + atividadeDto.mapaCodigo()));

        var usuario = usuarioRepo.findByTituloEleitoral(Long.parseLong(username))
            .orElseThrow(() -> new ErroDominioNaoEncontrado("Usuário não encontrado: " + username));

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

    public AtividadeDto atualizar(Long id, AtividadeDto atividadeDto) {
        return atividadeRepo.findById(id)
            .map(existente -> {
                var entidadeParaAtualizar = atividadeMapper.toEntity(atividadeDto);
                existente.setDescricao(entidadeParaAtualizar.getDescricao());
                existente.setMapa(entidadeParaAtualizar.getMapa());

                var atualizado = atividadeRepo.save(existente);
                return atividadeMapper.toDTO(atualizado);
            })
            .orElseThrow(() -> new ErroDominioNaoEncontrado("Atividade não encontrada: " + id));
    }

    public void excluir(Long id) {
        atividadeRepo.findById(id).ifPresentOrElse(atividade -> {
            var conhecimentos = conhecimentoRepo.findByAtividadeCodigo(atividade.getCodigo());
            conhecimentoRepo.deleteAll(conhecimentos);
            atividadeRepo.delete(atividade);
        }, () -> {
            throw new ErroDominioNaoEncontrado("Atividade não encontrada: " + id);
        });
    }

    public List<ConhecimentoDto> listarConhecimentos(Long atividadeId) {
        if (!atividadeRepo.existsById(atividadeId)) {
            throw new ErroDominioNaoEncontrado("Atividade não encontrada: " + atividadeId);
        }
        return conhecimentoRepo.findByAtividadeCodigo(atividadeId)
            .stream()
            .map(conhecimentoMapper::toDTO)
            .collect(Collectors.toList());
    }

    public ConhecimentoDto criarConhecimento(Long atividadeId, ConhecimentoDto conhecimentoDto) {
        return atividadeRepo.findById(atividadeId)
            .map(atividade -> {
                var conhecimento = conhecimentoMapper.toEntity(conhecimentoDto);
                conhecimento.setAtividade(atividade);
                var salvo = conhecimentoRepo.save(conhecimento);
                return conhecimentoMapper.toDTO(salvo);
            })
            .orElseThrow(() -> new ErroDominioNaoEncontrado("Atividade não encontrada: " + atividadeId));
    }

    public ConhecimentoDto atualizarConhecimento(Long atividadeId, Long conhecimentoId, ConhecimentoDto conhecimentoDto) {
        return conhecimentoRepo.findById(conhecimentoId)
            .filter(conhecimento -> conhecimento.getAtividade().getCodigo().equals(atividadeId))
            .map(existente -> {
                var paraAtualizar = conhecimentoMapper.toEntity(conhecimentoDto);
                existente.setDescricao(paraAtualizar.getDescricao());
                var atualizado = conhecimentoRepo.save(existente);
                return conhecimentoMapper.toDTO(atualizado);
            })
            .orElseThrow(() -> new ErroDominioNaoEncontrado("Conhecimento não encontrado: " + conhecimentoId));
    }

    public void excluirConhecimento(Long atividadeId, Long conhecimentoId) {
        conhecimentoRepo.findById(conhecimentoId)
            .filter(conhecimento -> conhecimento.getAtividade().getCodigo().equals(atividadeId))
            .ifPresentOrElse(conhecimentoRepo::delete, () -> {
                throw new ErroDominioNaoEncontrado("Conhecimento não encontrado: " + conhecimentoId);
            });
    }
}