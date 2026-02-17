package sgc.subprocesso.service.crud;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.comum.repo.ComumRepo;
import sgc.mapa.model.Mapa;
import sgc.subprocesso.dto.AtualizarSubprocessoRequest;
import sgc.subprocesso.dto.CriarSubprocessoRequest;
import sgc.subprocesso.dto.SubprocessoSituacaoDto;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.subprocesso.service.factory.SubprocessoFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Serviço especializado para operações CRUD básicas de Subprocesso.
 *
 * <p>
 * Responsável pelas operações de criação, leitura, atualização e exclusão
 * de subprocessos. Parte da decomposição arquitetural do módulo subprocesso.
 *
 * <p>
 * <b>Visibilidade:</b> Package-private - uso interno ao módulo subprocesso.
 * Acesso externo deve ser feito via
 * {@link sgc.subprocesso.service.SubprocessoFacade}.
 *
 * <p><b>Refatoração v3.0:</b> Removido uso de @Lazy e dependência em MapaFacade.
 * A criação do mapa agora é responsabilidade de SubprocessoFactory.</p>
 *
 * @since 3.0.0 - Removido @Lazy e MapaFacade
 */
@Service
@Transactional
@RequiredArgsConstructor
public class SubprocessoCrudService {
    private final SubprocessoRepo subprocessoRepo;
    private final ComumRepo repositorioComum;
    private final SubprocessoFactory subprocessoFactory;

    public Subprocesso buscarSubprocesso(Long codigo) {
        return subprocessoRepo.findByIdWithMapaAndAtividades(codigo)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Subprocesso", codigo));
    }

    /**
     * Busca subprocesso e seu mapa associado.
     */
    public Subprocesso buscarSubprocessoComMapa(Long codigo) {
        return buscarSubprocesso(codigo);
    }

    @Transactional(readOnly = true)
    public List<Subprocesso> listarEntidadesPorProcesso(Long codProcesso) {
        return subprocessoRepo.findByProcessoCodigoWithUnidade(codProcesso);
    }

    @Transactional(readOnly = true)
    public SubprocessoSituacaoDto obterStatus(Long codSubprocesso) {
        Subprocesso subprocesso = buscarSubprocesso(codSubprocesso);
        return SubprocessoSituacaoDto.builder()
                .codigo(subprocesso.getCodigo())
                .situacao(subprocesso.getSituacao())
                .situacaoLabel(subprocesso.getSituacao().getDescricao())
                .build();
    }

    @Transactional(readOnly = true)
    public Subprocesso obterEntidadePorCodigoMapa(Long codMapa) {
        return repositorioComum.buscar(Subprocesso.class, "mapa.codigo", codMapa);
    }

    private void processarAlteracoes(Subprocesso subprocesso, AtualizarSubprocessoRequest request) {

        Optional.ofNullable(request.codMapa()).ifPresent(cod -> {
            Mapa m = Mapa.builder()
                    .codigo(cod)
                    .build();
            Long codAtual = subprocesso.getMapa() != null ? subprocesso.getMapa().getCodigo() : null;
            if (!Objects.equals(codAtual, cod)) {
                subprocesso.setMapa(m);
            }
        });

        if (!Objects.equals(subprocesso.getDataLimiteEtapa1(), request.dataLimiteEtapa1())) {
            subprocesso.setDataLimiteEtapa1(request.dataLimiteEtapa1());
        }
        if (!Objects.equals(subprocesso.getDataFimEtapa1(), request.dataFimEtapa1())) {
            subprocesso.setDataFimEtapa1(request.dataFimEtapa1());
        }
        if (!Objects.equals(subprocesso.getDataFimEtapa2(), request.dataFimEtapa2())) {
            subprocesso.setDataFimEtapa2(request.dataFimEtapa2());
        }
    }

    public void excluir(Long codigo) {
        buscarSubprocesso(codigo);

        subprocessoRepo.deleteById(codigo);
    }

    @Transactional(readOnly = true)
    public List<Subprocesso> listarEntidades() {
        return subprocessoRepo.findAllComFetch();
    }

    @Transactional(readOnly = true)
    public Subprocesso obterEntidadePorProcessoEUnidade(Long codProcesso, Long codUnidade) {
        Map<String, Object> filtros = Map.of(
                "processo.codigo", codProcesso,
                "unidade.codigo", codUnidade
        );
        return repositorioComum.buscar(Subprocesso.class, filtros);
    }

    @Transactional
    public Subprocesso criarEntidade(CriarSubprocessoRequest request) {
        return subprocessoFactory.criar(request);
    }

    @Transactional
    public Subprocesso atualizarEntidade(Long codigo, AtualizarSubprocessoRequest request) {
        Subprocesso subprocesso = buscarSubprocesso(codigo);
        processarAlteracoes(subprocesso, request);
        return subprocessoRepo.save(subprocesso);
    }

    @Transactional(readOnly = true)
    public boolean verificarAcessoUnidadeAoProcesso(Long codProcesso, List<Long> codigosUnidadesHierarquia) {
        return subprocessoRepo.existsByProcessoCodigoAndUnidadeCodigoIn(codProcesso, codigosUnidadesHierarquia);
    }

    @Transactional(readOnly = true)
    public List<Subprocesso> listarEntidadesPorProcessoEUnidades(Long codProcesso, @Nullable List<Long> codUnidades) {
        if (codUnidades == null || codUnidades.isEmpty()) {
            return List.of();
        }
        return subprocessoRepo.findByProcessoCodigoAndUnidadeCodigoInWithUnidade(codProcesso, codUnidades);
    }
}
