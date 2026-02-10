package sgc.subprocesso.service.crud;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.repo.ComumRepo;
import sgc.mapa.model.Mapa;
import sgc.subprocesso.dto.AtualizarSubprocessoRequest;
import sgc.subprocesso.dto.CriarSubprocessoRequest;
import sgc.subprocesso.dto.SubprocessoDto;
import sgc.subprocesso.dto.SubprocessoSituacaoDto;
import sgc.subprocesso.mapper.SubprocessoMapper;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.subprocesso.service.factory.SubprocessoFactory;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

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
    private final SubprocessoMapper subprocessoMapper;
    private final SubprocessoFactory subprocessoFactory;

    public Subprocesso buscarSubprocesso(Long codigo) {
        return repositorioComum.buscar(Subprocesso.class, codigo);
    }

    /**
     * Busca subprocesso e seu mapa associado.
     */
    public Subprocesso buscarSubprocessoComMapa(Long codigo) {
        return repositorioComum.buscar(Subprocesso.class, codigo);
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

    public SubprocessoDto criar(CriarSubprocessoRequest request) {
        Subprocesso salvo = subprocessoFactory.criar(request);
        return subprocessoMapper.toDto(salvo);
    }

    public SubprocessoDto atualizar(Long codigo, AtualizarSubprocessoRequest request) {
        Subprocesso subprocesso = buscarSubprocesso(codigo);
        processarAlteracoes(subprocesso, request);

        Subprocesso salvo = subprocessoRepo.save(subprocesso);
        return subprocessoMapper.toDto(salvo);
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
    public List<SubprocessoDto> listar() {
        return subprocessoRepo.findAllComFetch().stream()
                .flatMap(s -> Stream.ofNullable(subprocessoMapper.toDto(s)))
                .toList();
    }

    @Transactional(readOnly = true)
    public SubprocessoDto obterPorProcessoEUnidade(Long codProcesso, Long codUnidade) {
        // Substituído por repo.buscar para centralizar tratamento de exceção (evitar explicit throw)
        java.util.Map<String, Object> filtros = java.util.Map.of(
                "processo.codigo", codProcesso,
                "unidade.codigo", codUnidade
        );
        Subprocesso sp = repositorioComum.buscar(Subprocesso.class, filtros);
        return subprocessoMapper.toDto(sp);
    }

    @Transactional(readOnly = true)
    public boolean verificarAcessoUnidadeAoProcesso(Long codProcesso, List<Long> codigosUnidadesHierarquia) {
        return subprocessoRepo.existsByProcessoCodigoAndUnidadeCodigoIn(codProcesso, codigosUnidadesHierarquia);
    }

    @Transactional(readOnly = true)
    public List<SubprocessoDto> listarPorProcessoEUnidades(Long codProcesso, @Nullable List<Long> codUnidades) {
        if (codUnidades == null || codUnidades.isEmpty()) {
            return List.of();
        }
        return subprocessoRepo.findByProcessoCodigoAndUnidadeCodigoInWithUnidade(codProcesso, codUnidades)
                .stream()
                .flatMap(s -> Stream.ofNullable(subprocessoMapper.toDto(s)))
                .toList();
    }
}
