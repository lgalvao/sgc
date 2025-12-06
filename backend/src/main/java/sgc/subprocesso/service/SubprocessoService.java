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
import sgc.processo.model.Processo;
import sgc.subprocesso.dto.SubprocessoDto;
import sgc.subprocesso.dto.SubprocessoMapper;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.unidade.model.Unidade;

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
    public List<Atividade> obterAtividadesSemConhecimento(Long codSubprocesso) {
        Subprocesso sp = repositorioSubprocesso.findById(codSubprocesso)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Subprocesso não encontrado: %d".formatted(codSubprocesso)));

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
                    Map.of("competenciasNaoAssociadas", competenciasSemAssociacao)
            );
        }

        List<Atividade> atividades = atividadeRepo.findByMapaCodigo(mapaId);
        List<String> atividadesSemAssociacao = new ArrayList<>();
        for (Atividade atividade : atividades) {
            if (atividade.getCompetencias().isEmpty()) {
                atividadesSemAssociacao.add(atividade.getDescricao());
            }
        }
        if (!atividadesSemAssociacao.isEmpty()) {
            throw new ErroValidacao("Existem atividades que não foram associadas a nenhuma competência.", Map.of("atividadesNaoAssociadas", atividadesSemAssociacao));
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
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Subprocesso não encontrado", codigo));
    }

    @Transactional
    public void excluir(Long codigo) {
        if (!repositorioSubprocesso.existsById(codigo)) {
            throw new ErroEntidadeNaoEncontrada("Subprocesso não encontrado", codigo);
        }
        repositorioSubprocesso.deleteById(codigo);
    }
}
