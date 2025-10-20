package sgc.competencia;

import lombok.RequiredArgsConstructor;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
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
    private static final PolicyFactory HTML_SANITIZER_POLICY = new HtmlPolicyBuilder().toFactory();

    private final CompetenciaRepo competenciaRepo;
    private final CompetenciaMapper competenciaMapper;
    private final AtividadeRepo atividadeRepo;
    private final CompetenciaAtividadeRepo competenciaAtividadeRepo;

    /**
     * Retorna uma lista de todas as competências.
     *
     * @return Uma {@link List} de {@link CompetenciaDto}.
     */
    public List<CompetenciaDto> listarCompetencias() {
        return competenciaRepo.findAll()
                .stream()
                .map(competenciaMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Busca uma competência pelo seu ID.
     *
     * @param id O ID da competência.
     * @return O {@link CompetenciaDto} correspondente.
     * @throws ErroDominioNaoEncontrado se a competência não for encontrada.
     */
    public CompetenciaDto obterCompetencia(Long id) {
        return competenciaRepo.findById(id)
                .map(competenciaMapper::toDTO)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Competência não encontrada: ", id));
    }

    /**
     * Cria uma nova competência, sanitizando a descrição para remover HTML.
     *
     * @param competenciaDto O DTO com os dados da nova competência.
     * @return O {@link CompetenciaDto} da competência criada.
     */
    public CompetenciaDto criarCompetencia(CompetenciaDto competenciaDto) {
        var sanitizedDescricao = HTML_SANITIZER_POLICY.sanitize(competenciaDto.getDescricao());
        var sanitizedCompetenciaDto = new CompetenciaDto(
                competenciaDto.getCodigo(),
                competenciaDto.getMapaCodigo(),
                sanitizedDescricao
        );
        var entity = competenciaMapper.toEntity(sanitizedCompetenciaDto);
        var salvo = competenciaRepo.save(entity);
        return competenciaMapper.toDTO(salvo);
    }

    /**
     * Atualiza uma competência existente, sanitizando a nova descrição.
     *
     * @param idCompetencia  O ID da competência a ser atualizada.
     * @param competenciaDto O DTO com os novos dados.
     * @return O {@link CompetenciaDto} da competência atualizada.
     * @throws ErroDominioNaoEncontrado se a competência não for encontrada.
     */
    public CompetenciaDto atualizarCompetencia(Long idCompetencia, CompetenciaDto competenciaDto) {
        return competenciaRepo.findById(idCompetencia)
                .map(existing -> {
                    if (competenciaDto.getMapaCodigo() != null) {
                        Mapa m = new Mapa();
                        m.setCodigo(competenciaDto.getMapaCodigo());
                        existing.setMapa(m);
                    } else {
                        existing.setMapa(null);
                    }
                    var sanitizedDescricao = HTML_SANITIZER_POLICY.sanitize(competenciaDto.getDescricao());
                    existing.setDescricao(sanitizedDescricao);
                    var atualizado = competenciaRepo.save(existing);
                    return competenciaMapper.toDTO(atualizado);
                })
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Competência não encontrada", idCompetencia));
    }

    /**
     * Exclui uma competência.
     *
     * @param id O ID da competência a ser excluída.
     * @throws ErroDominioNaoEncontrado se a competência não for encontrada.
     */
    public void excluirCompetencia(Long id) {
        if (!competenciaRepo.existsById(id)) {
            throw new ErroDominioNaoEncontrado("Competência não encontrada", id);
        }
        competenciaRepo.deleteById(id);
    }

    /**
     * Lista todos os vínculos de atividades para uma competência específica.
     *
     * @param idCompetencia O ID da competência.
     * @return Uma {@link List} de {@link CompetenciaAtividade}.
     * @throws ErroDominioNaoEncontrado se a competência não for encontrada.
     */
    public List<CompetenciaAtividade> listarAtividadesVinculadas(Long idCompetencia) {
        if (!competenciaRepo.existsById(idCompetencia)) {
            throw new ErroDominioNaoEncontrado("Competência não encontrada", idCompetencia);
        }
        return competenciaAtividadeRepo.findAll()
                .stream()
                .filter(ca -> ca.getId() != null && idCompetencia.equals(ca.getId().getCompetenciaCodigo()))
                .collect(Collectors.toList());
    }

    /**
     * Cria um vínculo entre uma competência e uma atividade.
     *
     * @param idCompetencia O ID da competência.
     * @param idAtividade   O ID da atividade.
     * @return A entidade {@link CompetenciaAtividade} que representa o vínculo.
     * @throws ErroDominioNaoEncontrado se a competência ou a atividade não forem encontradas.
     * @throws IllegalStateException se o vínculo já existir.
     */
    public CompetenciaAtividade vincularAtividade(Long idCompetencia, Long idAtividade) {
        Competencia competencia = competenciaRepo.findById(idCompetencia)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Competência não encontrada", idCompetencia));

        Atividade atividade = atividadeRepo.findById(idAtividade)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Atividade não encontrada", idAtividade));

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

    /**
     * Remove o vínculo entre uma competência e uma atividade.
     *
     * @param idCompetencia O ID da competência.
     * @param idAtividade   O ID da atividade.
     * @throws ErroDominioNaoEncontrado se o vínculo não for encontrado.
     */
    public void desvincularAtividade(Long idCompetencia, Long idAtividade) {
        Id id = new Id(idAtividade, idCompetencia);
        if (!competenciaAtividadeRepo.existsById(id)) {
            throw new ErroDominioNaoEncontrado("Vínculo não encontrado.");
        }
        competenciaAtividadeRepo.deleteById(id);
    }
}