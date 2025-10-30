package sgc.subprocesso.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.atividade.modelo.Atividade;
import sgc.atividade.modelo.AtividadeRepo;
import sgc.competencia.modelo.Competencia;
import sgc.competencia.modelo.CompetenciaAtividadeRepo;
import sgc.competencia.modelo.CompetenciaRepo;
import sgc.comum.erros.ErroDominioNaoEncontrado;
import sgc.comum.erros.ErroValidacao;
import sgc.conhecimento.modelo.Conhecimento;
import sgc.conhecimento.modelo.ConhecimentoRepo;
import sgc.mapa.modelo.Mapa;
import sgc.processo.modelo.Processo;
import sgc.subprocesso.dto.SubprocessoDto;
import sgc.subprocesso.dto.SubprocessoMapper;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.subprocesso.modelo.SubprocessoRepo;
import sgc.unidade.modelo.Unidade;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubprocessoService {
    private final SubprocessoRepo repositorioSubprocesso;
    private final AtividadeRepo atividadeRepo;
    private final ConhecimentoRepo repositorioConhecimento;
    private final CompetenciaRepo competenciaRepo;
    private final CompetenciaAtividadeRepo competenciaAtividadeRepo;
    private final SubprocessoMapper subprocessoMapper;

    /**
     * Busca, dentro de um subprocesso, todas as atividades que ainda não possuem
     * nenhum conhecimento associado.
     *
     * @param codSubprocesso O código do subprocesso a ser verificado.
     * @return Uma {@link List} de {@link Atividade}s que não têm conhecimentos.
     * Retorna uma lista vazia se o subprocesso não for encontrado ou
     * não tiver um mapa associado.
     */
    @Transactional(readOnly = true)
    public List<Atividade> obterAtividadesSemConhecimento(Long codSubprocesso) {
        Subprocesso sp = repositorioSubprocesso.findById(codSubprocesso)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: %d".formatted(codSubprocesso)));

        if (sp.getMapa() == null || sp.getMapa().getCodigo() == null) {
            return emptyList();
        }

        List<Atividade> atividades = atividadeRepo.findByMapaCodigo(sp.getMapa().getCodigo());
        if (atividades == null || atividades.isEmpty()) {
            return emptyList();
        }

        return atividades.stream()
                .filter(a -> {
                    if (a.getCodigo() == null) return true;
                    List<Conhecimento> ks = repositorioConhecimento.findByAtividadeCodigo(a.getCodigo());
                    return ks == null || ks.isEmpty();
                })
                .collect(Collectors.toList());
    }

    /**
     * Valida a integridade das associações de um mapa de competências.
     * <p>
     * Garante que não hajam competências ou atividades "órfãs" dentro do mapa.
     * A validação verifica duas condições:
     * <ol>
     *     <li>Se todas as competências do mapa estão associadas a, no mínimo, uma atividade.</li>
     *     <li>Se todas as atividades do mapa estão associadas a, no mínimo, uma competência.</li>
     * </ol>
     *
     * @param mapaId O código do mapa a ser validado.
     * @throws ErroValidacao se alguma das condições de integridade não for atendida.
     *                       A exceção contém detalhes sobre as entidades problemáticas.
     */
    public void validarAssociacoesMapa(Long mapaId) {
        // Verificar se todas as competências estão associadas a pelo menos uma atividade
        List<Competencia> competencias = competenciaRepo.findByMapaCodigo(mapaId);
        List<String> competenciasSemAssociacao = new ArrayList<>();
        for (Competencia competencia : competencias) {
            if (competenciaAtividadeRepo.countByCompetenciaCodigo(competencia.getCodigo()) == 0) {
                competenciasSemAssociacao.add(competencia.getDescricao());
            }
        }
        if (!competenciasSemAssociacao.isEmpty()) {
            throw new ErroValidacao(
                    "Existem competências que não foram associadas a nenhuma atividade.",
                    Map.of("competenciasNaoAssociadas", competenciasSemAssociacao)
            );
        }

        // Verificar se todas as atividades estão associadas a pelo menos uma competência
        List<Atividade> atividades = atividadeRepo.findByMapaCodigo(mapaId);
        List<String> atividadesSemAssociacao = new ArrayList<>();
        for (Atividade atividade : atividades) {
            if (competenciaAtividadeRepo.countByAtividadeCodigo(atividade.getCodigo()) == 0) {
                atividadesSemAssociacao.add(atividade.getDescricao());
            }
        }
        if (!atividadesSemAssociacao.isEmpty()) {
            throw new ErroValidacao("Existem atividades que não foram associadas a nenhuma competência.", Map.of("atividadesNaoAssociadas", atividadesSemAssociacao));
        }
    }


    /**
     * Cria um novo subprocesso.
     *
     * @param subprocessoDto O DTO com os dados do subprocesso a ser criado.
     * @return O {@link SubprocessoDto} da entidade criada.
     */
    @Transactional
    public SubprocessoDto criar(SubprocessoDto subprocessoDto) {
        var entity = subprocessoMapper.toEntity(subprocessoDto);
        var salvo = repositorioSubprocesso.save(entity);
        return subprocessoMapper.toDTO(salvo);
    }

    /**
     * Atualiza um subprocesso existente.
     *
     * @param codigo             O código do subprocesso a ser atualizado.
     * @param subprocessoDto O DTO com os novos dados.
     * @return O {@link SubprocessoDto} da entidade atualizada.
     * @throws ErroDominioNaoEncontrado se o subprocesso não for encontrado.
     */
    @Transactional
    public SubprocessoDto atualizar(Long codigo, SubprocessoDto subprocessoDto) {
        return repositorioSubprocesso.findById(codigo)
                .map(subprocesso -> {
                    if (subprocessoDto.getCodProcesso() != null) {
                        Processo p = new Processo();
                        p.setCodigo(subprocessoDto.getCodProcesso());
                        subprocesso.setProcesso(p);
                    } else {
                        subprocesso.setProcesso(null);
                    }

                    if (subprocessoDto.getCodUnidade() != null) {
                        Unidade u = new Unidade();
                        u.setCodigo(subprocessoDto.getCodUnidade());
                        subprocesso.setUnidade(u);
                    } else {
                        subprocesso.setUnidade(null);
                    }

                    if (subprocessoDto.getCodMapa() != null) {
                        Mapa m = new Mapa();
                        m.setCodigo(subprocessoDto.getCodMapa());
                        subprocesso.setMapa(m);
                    } else {
                        subprocesso.setMapa(null);
                    }

                    subprocesso.setDataLimiteEtapa1(subprocessoDto.getDataLimiteEtapa1());
                    subprocesso.setDataFimEtapa1(subprocessoDto.getDataFimEtapa1());
                    var dataFimEtapa2 = subprocessoDto.getDataFimEtapa2();
                    subprocesso.setDataFimEtapa2(dataFimEtapa2);
                    subprocesso.setSituacao(subprocessoDto.getSituacao());
                    var atualizado = repositorioSubprocesso.save(subprocesso);
                    return subprocessoMapper.toDTO(atualizado);
                })
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado", codigo));
    }

    /**
     * Exclui um subprocesso.
     *
     * @param codigo O código do subprocesso a ser excluído.
     * @throws ErroDominioNaoEncontrado se o subprocesso não for encontrado.
     */
    @Transactional
    public void excluir(Long codigo) {
        if (!repositorioSubprocesso.existsById(codigo)) {
            throw new ErroDominioNaoEncontrado("Subprocesso não encontrado", codigo);
        }
        repositorioSubprocesso.deleteById(codigo);
    }
}