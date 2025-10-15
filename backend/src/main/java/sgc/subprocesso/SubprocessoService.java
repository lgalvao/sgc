package sgc.subprocesso;

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

    @Transactional(readOnly = true)
    public List<Atividade> obterAtividadesSemConhecimento(Long idSubprocesso) {
        Subprocesso sp = repositorioSubprocesso.findById(idSubprocesso)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: %d".formatted(idSubprocesso)));

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

    public void validarAssociacoesMapa(Long mapaId) {
        // 1. Verificar se todas as competências estão associadas a pelo menos uma atividade
        List<Competencia> competencias = competenciaRepo.findByMapaCodigo(mapaId);
        List<String> competenciasSemAssociacao = new ArrayList<>();
        for (Competencia competencia : competencias) {
            if (competenciaAtividadeRepo.countByCompetenciaCodigo(competencia.getCodigo()) == 0) {
                competenciasSemAssociacao.add(competencia.getDescricao());
            }
        }

        if (!competenciasSemAssociacao.isEmpty()) {
            throw new ErroValidacao("Existem competências que não foram associadas a nenhuma atividade.", java.util.Map.of("competenciasNaoAssociadas", competenciasSemAssociacao));
        }

        // 2. Verificar se todas as atividades estão associadas a pelo menos uma competência
        List<Atividade> atividades = atividadeRepo.findByMapaCodigo(mapaId);
        List<String> atividadesSemAssociacao = new ArrayList<>();
        for (Atividade atividade : atividades) {
            if (competenciaAtividadeRepo.countByAtividadeCodigo(atividade.getCodigo()) == 0) {
                atividadesSemAssociacao.add(atividade.getDescricao());
            }
        }

        if (!atividadesSemAssociacao.isEmpty()) {
            throw new ErroValidacao("Existem atividades que não foram associadas a nenhuma competência.", java.util.Map.of("atividadesNaoAssociadas", atividadesSemAssociacao));
        }
    }


    @Transactional
    public SubprocessoDto criar(SubprocessoDto subprocessoDto) {
        var entity = subprocessoMapper.toEntity(subprocessoDto);
        var salvo = repositorioSubprocesso.save(entity);
        return subprocessoMapper.toDTO(salvo);
    }

    @Transactional
    public SubprocessoDto atualizar(Long id, SubprocessoDto subprocessoDto) {
        return repositorioSubprocesso.findById(id)
            .map(subprocesso -> {
                if (subprocessoDto.getProcessoCodigo() != null) {
                    Processo p = new Processo();
                    p.setCodigo(subprocessoDto.getProcessoCodigo());
                    subprocesso.setProcesso(p);
                } else {
                    subprocesso.setProcesso(null);
                }

                if (subprocessoDto.getUnidadeCodigo() != null) {
                    Unidade u = new Unidade();
                    u.setCodigo(subprocessoDto.getUnidadeCodigo());
                    subprocesso.setUnidade(u);
                } else {
                    subprocesso.setUnidade(null);
                }

                if (subprocessoDto.getMapaCodigo() != null) {
                    Mapa m = new Mapa();
                    m.setCodigo(subprocessoDto.getMapaCodigo());
                    subprocesso.setMapa(m);
                } else {
                    subprocesso.setMapa(null);
                }

                subprocesso.setDataLimiteEtapa1(subprocessoDto.getDataLimiteEtapa1());
                subprocesso.setDataFimEtapa1(subprocessoDto.getDataFimEtapa1());
                subprocesso.setDataLimiteEtapa2(subprocessoDto.getDataFimEtapa2());
                subprocesso.setDataFimEtapa2(subprocessoDto.getDataFimEtapa2() != null ? subprocessoDto.getDataFimEtapa2().atStartOfDay() : null);
                subprocesso.setSituacao(subprocessoDto.getSituacao());
                var atualizado = repositorioSubprocesso.save(subprocesso);
                return subprocessoMapper.toDTO(atualizado);
            })
            .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: " + id));
    }

    @Transactional
    public void excluir(Long id) {
        if (!repositorioSubprocesso.existsById(id)) {
            throw new ErroDominioNaoEncontrado("Subprocesso não encontrado: " + id);
        }
        repositorioSubprocesso.deleteById(id);
    }
}