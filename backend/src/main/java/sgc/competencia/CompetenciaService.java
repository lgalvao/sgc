package sgc.competencia;

import org.owasp.html.PolicyFactory;
import org.owasp.html.HtmlPolicyBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.atividade.modelo.Atividade;
import sgc.atividade.modelo.AtividadeRepo;
import sgc.competencia.dto.CompetenciaDto;
import sgc.competencia.dto.CompetenciaMapper;
import sgc.competencia.modelo.Competencia;
import sgc.competencia.modelo.CompetenciaAtividade;
import sgc.competencia.modelo.CompetenciaAtividade.Id;
import sgc.competencia.modelo.CompetenciaAtividadeRepo;
import sgc.competencia.modelo.CompetenciaRepo;
import sgc.comum.erros.ErroDominioNaoEncontrado;
import sgc.mapa.modelo.Mapa;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class CompetenciaService {

    private static final PolicyFactory HTML_SANITIZER_POLICY = new HtmlPolicyBuilder()
            .toFactory();

    private final CompetenciaRepo competenciaRepo;
    private final CompetenciaMapper competenciaMapper;
    private final AtividadeRepo atividadeRepo;
    private final CompetenciaAtividadeRepo competenciaAtividadeRepo;

    public List<CompetenciaDto> listarCompetencias() {
        return competenciaRepo.findAll()
            .stream()
            .map(competenciaMapper::toDTO)
            .collect(Collectors.toList());
    }

    public CompetenciaDto obterCompetencia(Long id) {
        return competenciaRepo.findById(id)
            .map(competenciaMapper::toDTO)
            .orElseThrow(() -> new ErroDominioNaoEncontrado("Competência não encontrada: " + id));
    }

    public CompetenciaDto criarCompetencia(CompetenciaDto competenciaDto) {
        // Sanitize the description before saving
        var sanitizedDescricao = HTML_SANITIZER_POLICY.sanitize(competenciaDto.descricao());
        var sanitizedCompetenciaDto = new CompetenciaDto(competenciaDto.codigo(), competenciaDto.mapaCodigo(), sanitizedDescricao);

        var entity = competenciaMapper.toEntity(sanitizedCompetenciaDto);
        var salvo = competenciaRepo.save(entity);
        return competenciaMapper.toDTO(salvo);
    }

    public CompetenciaDto atualizarCompetencia(Long id, CompetenciaDto competenciaDto) {
        return competenciaRepo.findById(id)
            .map(existing -> {
                if (competenciaDto.mapaCodigo() != null) {
                    Mapa m = new Mapa();
                    m.setCodigo(competenciaDto.mapaCodigo());
                    existing.setMapa(m);
                } else {
                    existing.setMapa(null);
                }
                // Sanitize the description before updating
                var sanitizedDescricao = HTML_SANITIZER_POLICY.sanitize(competenciaDto.descricao());
                existing.setDescricao(sanitizedDescricao);
                var atualizado = competenciaRepo.save(existing);
                return competenciaMapper.toDTO(atualizado);
            })
            .orElseThrow(() -> new ErroDominioNaoEncontrado("Competência não encontrada: " + id));
    }

    public void excluirCompetencia(Long id) {
        if (!competenciaRepo.existsById(id)) {
            throw new ErroDominioNaoEncontrado("Competência não encontrada: " + id);
        }
        competenciaRepo.deleteById(id);
    }

    public List<CompetenciaAtividade> listarAtividadesVinculadas(Long idCompetencia) {
        if (!competenciaRepo.existsById(idCompetencia)) {
            throw new ErroDominioNaoEncontrado("Competência não encontrada: " + idCompetencia);
        }
        return competenciaAtividadeRepo.findAll()
            .stream()
            .filter(ca -> ca.getId() != null && idCompetencia.equals(ca.getId().getCompetenciaCodigo()))
            .collect(Collectors.toList());
    }

    public CompetenciaAtividade vincularAtividade(Long idCompetencia, Long idAtividade) {
        Competencia competencia = competenciaRepo.findById(idCompetencia)
            .orElseThrow(() -> new ErroDominioNaoEncontrado("Competência não encontrada: " + idCompetencia));

        Atividade atividade = atividadeRepo.findById(idAtividade)
            .orElseThrow(() -> new ErroDominioNaoEncontrado("Atividade não encontrada: " + idAtividade));

        Id id = new Id(idAtividade, idCompetencia);
        if (competenciaAtividadeRepo.existsById(id)) {
            throw new IllegalStateException("Vínculo já existe.");
        }

        CompetenciaAtividade vinculo = new CompetenciaAtividade();
        vinculo.setId(id);
        vinculo.setAtividade(atividade);
        vinculo.setCompetencia(competencia);

        return competenciaAtividadeRepo.save(vinculo);
    }

    public void desvincularAtividade(Long idCompetencia, Long idAtividade) {
        Id id = new Id(idAtividade, idCompetencia);
        if (!competenciaAtividadeRepo.existsById(id)) {
            throw new ErroDominioNaoEncontrado("Vínculo não encontrado.");
        }
        competenciaAtividadeRepo.deleteById(id);
    }
}