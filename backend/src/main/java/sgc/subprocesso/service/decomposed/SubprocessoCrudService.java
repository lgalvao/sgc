package sgc.subprocesso.service.decomposed;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.model.Mapa;
import sgc.mapa.service.MapaService;
import sgc.subprocesso.dto.SubprocessoDto;
import sgc.subprocesso.dto.SubprocessoSituacaoDto;
import sgc.subprocesso.mapper.SubprocessoMapper;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SubprocessoCrudService {
    private final SubprocessoRepo repositorioSubprocesso;
    private final SubprocessoMapper subprocessoMapper;
    private final MapaService mapaService;

    public Subprocesso buscarSubprocesso(Long codigo) {
        return repositorioSubprocesso
                .findById(codigo)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Subprocesso não encontrado", codigo));
    }

    public Subprocesso buscarSubprocessoComMapa(Long codigo) {
        Subprocesso subprocesso = buscarSubprocesso(codigo);
        if (subprocesso.getMapa() == null) {
            throw new ErroEntidadeNaoEncontrada("Subprocesso não possui mapa associado");
        }
        return subprocesso;
    }

    @Transactional(readOnly = true)
    public List<Subprocesso> listarEntidadesPorProcesso(Long codProcesso) {
        return repositorioSubprocesso.findByProcessoCodigoWithUnidade(codProcesso);
    }

    @Transactional(readOnly = true)
    public SubprocessoSituacaoDto obterStatus(Long codSubprocesso) {
        Subprocesso subprocesso = buscarSubprocesso(codSubprocesso);
        return SubprocessoSituacaoDto.builder()
                .codigo(subprocesso.getCodigo())
                .situacao(subprocesso.getSituacao())
                .situacaoLabel(subprocesso.getSituacao() != null ? subprocesso.getSituacao().name() : null)
                .build();
    }

    @Transactional(readOnly = true)
    public Subprocesso obterEntidadePorCodigoMapa(Long codMapa) {
        return repositorioSubprocesso
                .findByMapaCodigo(codMapa)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada(
                        "Subprocesso não encontrado para o mapa com código %d".formatted(codMapa)));
    }

    public SubprocessoDto criar(SubprocessoDto subprocessoDto) {
        var entity = subprocessoMapper.toEntity(subprocessoDto);
        entity.setMapa(null);
        var subprocessoSalvo = repositorioSubprocesso.save(entity);

        Mapa mapa = new Mapa();
        mapa.setSubprocesso(subprocessoSalvo);
        Mapa mapaSalvo = mapaService.salvar(mapa);

        subprocessoSalvo.setMapa(mapaSalvo);
        var salvo = repositorioSubprocesso.save(subprocessoSalvo);

        return subprocessoMapper.toDTO(salvo);
    }

    public SubprocessoDto atualizar(Long codigo, SubprocessoDto subprocessoDto) {
        return repositorioSubprocesso.findById(codigo)
                .map(subprocesso -> {
                    if (subprocessoDto.getCodMapa() != null) {
                        Mapa mapa = new Mapa();
                        mapa.setCodigo(subprocessoDto.getCodMapa());
                        subprocesso.setMapa(mapa);
                    } else {
                        subprocesso.setMapa(null);
                    }
                    subprocesso.setDataLimiteEtapa1(subprocessoDto.getDataLimiteEtapa1());
                    subprocesso.setDataFimEtapa1(subprocessoDto.getDataFimEtapa1());
                    subprocesso.setDataFimEtapa2(subprocessoDto.getDataFimEtapa2());
                    subprocesso.setSituacao(subprocessoDto.getSituacao());
                    return subprocessoMapper.toDTO(repositorioSubprocesso.save(subprocesso));
                })
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Subprocesso não encontrado", codigo));
    }

    public void excluir(Long codigo) {
        if (!repositorioSubprocesso.existsById(codigo)) {
            throw new ErroEntidadeNaoEncontrada("Subprocesso não encontrado", codigo);
        }
        repositorioSubprocesso.deleteById(codigo);
    }

    @Transactional(readOnly = true)
    public List<SubprocessoDto> listar() {
        return repositorioSubprocesso.findAllComFetch().stream().map(subprocessoMapper::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public SubprocessoDto obterPorProcessoEUnidade(Long codProcesso, Long codUnidade) {
        Subprocesso sp = repositorioSubprocesso
                .findByProcessoCodigoAndUnidadeCodigo(codProcesso, codUnidade)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Subprocesso não encontrado para o processo %d e unidade %d".formatted(codProcesso, codUnidade)));
        return subprocessoMapper.toDTO(sp);
    }

    @Transactional(readOnly = true)
    public boolean verificarAcessoUnidadeAoProcesso(Long codProcesso, List<Long> codigosUnidadesHierarquia) {
        return repositorioSubprocesso.existsByProcessoCodigoAndUnidadeCodigoIn(codProcesso, codigosUnidadesHierarquia);
    }
}
