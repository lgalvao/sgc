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
import org.owasp.html.PolicyFactory;
import org.owasp.html.HtmlPolicyBuilder;
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
public class AtividadeService {
    private static final PolicyFactory HTML_SANITIZER_POLICY = new HtmlPolicyBuilder()
            .toFactory();

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

    public AtividadeDto obterPorId(Long idAtividade) {
        return atividadeRepo.findById(idAtividade)
                .map(atividadeMapper::toDTO)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Atividade", idAtividade));
    }

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

        // Sanitize the description before saving
        var sanitizedDescricao = HTML_SANITIZER_POLICY.sanitize(atividadeDto.descricao());
        var sanitizedAtividadeDto = new AtividadeDto(atividadeDto.codigo(), atividadeDto.mapaCodigo(), sanitizedDescricao);

        var entidade = atividadeMapper.toEntity(sanitizedAtividadeDto);
        var salvo = atividadeRepo.save(entidade);
        return atividadeMapper.toDTO(salvo);
    }

    public AtividadeDto atualizar(Long id, AtividadeDto atividadeDto) {
        return atividadeRepo.findById(id)
                .map(existente -> {
                    // Sanitize the description before updating
                    var sanitizedDescricao = HTML_SANITIZER_POLICY.sanitize(atividadeDto.descricao());
                    var sanitizedAtividadeDto = new AtividadeDto(atividadeDto.codigo(), atividadeDto.mapaCodigo(), sanitizedDescricao);

                    var entidadeParaAtualizar = atividadeMapper.toEntity(sanitizedAtividadeDto);
                    existente.setDescricao(entidadeParaAtualizar.getDescricao());
                    existente.setMapa(entidadeParaAtualizar.getMapa());

                    var atualizado = atividadeRepo.save(existente);
                    return atividadeMapper.toDTO(atualizado);
                })
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Atividade", id));
    }

    public void excluir(Long idAtividade) {
        atividadeRepo.findById(idAtividade).ifPresentOrElse(atividade -> {
            var conhecimentos = conhecimentoRepo.findByAtividadeCodigo(atividade.getCodigo());
            conhecimentoRepo.deleteAll(conhecimentos);
            atividadeRepo.delete(atividade);
        }, () -> {
            throw new ErroDominioNaoEncontrado("Atividade", idAtividade);
        });
    }

    public List<ConhecimentoDto> listarConhecimentos(Long idAtividade) {
        if (!atividadeRepo.existsById(idAtividade)) {
            throw new ErroDominioNaoEncontrado("Atividade", idAtividade);
        }
        return conhecimentoRepo.findByAtividadeCodigo(idAtividade)
                .stream()
                .map(conhecimentoMapper::toDTO)
                .collect(Collectors.toList());
    }

    public ConhecimentoDto criarConhecimento(Long idAtividade, ConhecimentoDto conhecimentoDto) {
        return atividadeRepo.findById(idAtividade)
                .map(atividade -> {
                    // Sanitize the description before saving
                    var sanitizedDescricao = HTML_SANITIZER_POLICY.sanitize(conhecimentoDto.descricao());
                    var sanitizedConhecimentoDto = new ConhecimentoDto(conhecimentoDto.codigo(), conhecimentoDto.atividadeCodigo(), sanitizedDescricao);

                    var conhecimento = conhecimentoMapper.toEntity(sanitizedConhecimentoDto);
                    conhecimento.setAtividade(atividade);
                    var salvo = conhecimentoRepo.save(conhecimento);
                    return conhecimentoMapper.toDTO(salvo);
                })
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Atividade", idAtividade));
    }

    public ConhecimentoDto atualizarConhecimento(Long idAtividade, Long idConhecimento, ConhecimentoDto conhecimentoDto) {
        return conhecimentoRepo.findById(idConhecimento)
                .filter(conhecimento -> conhecimento.getCodigoAtividade().equals(idAtividade))
                .map(existente -> {
                    // Sanitize the description before updating
                    var sanitizedDescricao = HTML_SANITIZER_POLICY.sanitize(conhecimentoDto.descricao());
                    var sanitizedConhecimentoDto = new ConhecimentoDto(conhecimentoDto.codigo(), conhecimentoDto.atividadeCodigo(), sanitizedDescricao);

                    var paraAtualizar = conhecimentoMapper.toEntity(sanitizedConhecimentoDto);
                    existente.setDescricao(paraAtualizar.getDescricao());
                    var atualizado = conhecimentoRepo.save(existente);
                    return conhecimentoMapper.toDTO(atualizado);
                })
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Conhecimento", idConhecimento));
    }

    public void excluirConhecimento(Long idAtividade, Long idConhecimento) {
        conhecimentoRepo.findById(idConhecimento)
                .filter(conhecimento -> conhecimento.getCodigoAtividade().equals(idAtividade))
                .ifPresentOrElse(conhecimentoRepo::delete, () -> {
                    throw new ErroDominioNaoEncontrado("Conhecimento", idConhecimento);
                });
    }
}
