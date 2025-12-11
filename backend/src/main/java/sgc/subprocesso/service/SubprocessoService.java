package sgc.subprocesso.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.atividade.model.Atividade;
import sgc.atividade.model.AtividadeRepo;
import sgc.atividade.model.Conhecimento;
import sgc.atividade.model.ConhecimentoRepo;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.comum.erros.ErroValidacao;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.CompetenciaRepo;
import sgc.mapa.model.Mapa;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;

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
    private final SubprocessoMapper subprocessoMapper;

    @Transactional(readOnly = true)
    public List<AtividadeVisualizacaoDto> listarAtividadesPorSubprocesso(Long codSubprocesso) {
        Subprocesso subprocesso = repositorioSubprocesso
                .findById(codSubprocesso)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Subprocesso", codSubprocesso));

        if (subprocesso.getMapa() == null) {
            return List.of();
        }

        List<Atividade> todasAtividades =
                atividadeRepo.findByMapaCodigo(subprocesso.getMapa().getCodigo());

        return todasAtividades.stream()
                .map(this::mapAtividadeToDto)
                .toList();
    }

    private AtividadeVisualizacaoDto mapAtividadeToDto(Atividade atividade) {
        List<Conhecimento> conhecimentos =
                repositorioConhecimento.findByAtividadeCodigo(atividade.getCodigo());

        List<ConhecimentoVisualizacaoDto> conhecimentosDto = conhecimentos.stream()
                .map(c -> ConhecimentoVisualizacaoDto.builder()
                        .codigo(c.getCodigo())
                        .descricao(c.getDescricao())
                        .build())
                .toList();

        return AtividadeVisualizacaoDto.builder()
                .codigo(atividade.getCodigo())
                .descricao(atividade.getDescricao())
                .conhecimentos(conhecimentosDto)
                .build();
    }

    /**
     * Obtém o status atual de um subprocesso de forma leve.
     *
     * @param codSubprocesso O código do subprocesso.
     * @return DTO com informações básicas de status.
     */
    @Transactional(readOnly = true)
    public SubprocessoStatusDto obterStatus(Long codSubprocesso) {
        Subprocesso subprocesso = repositorioSubprocesso
                .findById(codSubprocesso)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Subprocesso", codSubprocesso));

        return SubprocessoStatusDto.builder()
                .codigo(subprocesso.getCodigo())
                .situacao(subprocesso.getSituacao())
                .situacaoLabel(subprocesso.getSituacao() != null ? subprocesso.getSituacao().name() : null)
                .build();
    }

    /**
     * Busca um subprocesso pelo código do mapa e retorna a entidade.
     *
     * @param codMapa O código do mapa.
     * @return A entidade {@link Subprocesso} correspondente.
     * @throws ErroEntidadeNaoEncontrada se o subprocesso não for encontrado.
     */
    @Transactional(readOnly = true)
    public Subprocesso obterEntidadePorCodigoMapa(Long codMapa) {
        return repositorioSubprocesso
                .findByMapaCodigo(codMapa)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada(
                        "Subprocesso não encontrado para o mapa com código %d".formatted(codMapa))
                );
    }

    @Transactional(readOnly = true)
    public List<Atividade> obterAtividadesSemConhecimento(Long codSubprocesso) {
        Subprocesso sp =
                repositorioSubprocesso
                        .findById(codSubprocesso)
                        .orElseThrow(
                                () ->
                                        new ErroEntidadeNaoEncontrada(
                                                "Subprocesso não encontrado: %d"
                                                        .formatted(codSubprocesso)));

        if (sp.getMapa() == null || sp.getMapa().getCodigo() == null) {
            return emptyList();
        }

        List<Atividade> atividades = atividadeRepo.findByMapaCodigo(sp.getMapa().getCodigo());
        if (atividades == null || atividades.isEmpty()) {
            return emptyList();
        }

        return atividades.stream()
                .filter(
                        a -> {
                            if (a.getCodigo() == null) return true;
                            List<Conhecimento> ks =
                                    repositorioConhecimento.findByAtividadeCodigo(a.getCodigo());
                            return ks == null || ks.isEmpty();
                        })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public void validarExistenciaAtividades(Long codSubprocesso) {
        Subprocesso sp =
                repositorioSubprocesso
                        .findById(codSubprocesso)
                        .orElseThrow(
                                () ->
                                        new ErroEntidadeNaoEncontrada(
                                                "Subprocesso não encontrado: %d"
                                                        .formatted(codSubprocesso)));

        if (sp.getMapa() == null || sp.getMapa().getCodigo() == null) {
            throw new ErroValidacao("Subprocesso sem mapa associado.");
        }

        List<Atividade> atividades = atividadeRepo.findByMapaCodigo(sp.getMapa().getCodigo());
        if (atividades == null || atividades.isEmpty()) {
            throw new ErroValidacao(
                    "Pelo menos uma atividade deve ser cadastrada antes de disponibilizar.");
        }
    }


    public void validarAssociacoesMapa(Long mapaId) {
        List<Competencia> competencias = competenciaRepo.findByMapaCodigo(mapaId);
        List<String> competenciasSemAssociacao = new ArrayList<>();
        for (Competencia competencia : competencias) {
            if (competencia.getAtividades().isEmpty()) {
                competenciasSemAssociacao.add(competencia.getDescricao());
            }
        }
        if (!competenciasSemAssociacao.isEmpty()) {
            throw new ErroValidacao(
                    "Existem competências que não foram associadas a nenhuma atividade.",
                    Map.of("competenciasNaoAssociadas", competenciasSemAssociacao));
        }

        List<Atividade> atividades = atividadeRepo.findByMapaCodigo(mapaId);
        List<String> atividadesSemAssociacao = new ArrayList<>();
        for (Atividade atividade : atividades) {
            if (atividade.getCompetencias().isEmpty()) {
                atividadesSemAssociacao.add(atividade.getDescricao());
            }
        }
        if (!atividadesSemAssociacao.isEmpty()) {
            throw new ErroValidacao(
                    "Existem atividades que não foram associadas a nenhuma competência.",
                    Map.of("atividadesNaoAssociadas", atividadesSemAssociacao));
        }
    }

    @Transactional
    public SubprocessoDto criar(SubprocessoDto subprocessoDto) {
        var entity = subprocessoMapper.toEntity(subprocessoDto);
        var salvo = repositorioSubprocesso.save(entity);
        return subprocessoMapper.toDTO(salvo);
    }

    @Transactional
    public SubprocessoDto atualizar(Long codigo, SubprocessoDto subprocessoDto) {
        return repositorioSubprocesso
                .findById(codigo)
                .map(
                        subprocesso -> {
                            // NOTA: Processo e Unidade não podem ser alterados após criação
                            // pois fazem parte da identidade do subprocesso em UNIDADE_PROCESSO
                            
                            if (subprocessoDto.getCodMapa() != null) {
                                Mapa mapa = new Mapa();
                                mapa.setCodigo(subprocessoDto.getCodMapa());
                                subprocesso.setMapa(mapa);
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
                .orElseThrow(
                        () -> new ErroEntidadeNaoEncontrada("Subprocesso não encontrado", codigo));
    }

    @Transactional
    public void excluir(Long codigo) {
        if (!repositorioSubprocesso.existsById(codigo)) {
            throw new ErroEntidadeNaoEncontrada("Subprocesso não encontrado", codigo);
        }
        repositorioSubprocesso.deleteById(codigo);
    }
}
