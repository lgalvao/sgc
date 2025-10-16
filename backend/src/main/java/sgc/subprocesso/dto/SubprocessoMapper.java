package sgc.subprocesso.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sgc.mapa.modelo.Mapa;
import sgc.mapa.modelo.MapaRepo;
import sgc.processo.modelo.Processo;
import sgc.processo.modelo.ProcessoRepo;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.unidade.modelo.Unidade;
import sgc.unidade.modelo.UnidadeRepo;

/**
 * Mapper (usando MapStruct) entre a entidade Subprocesso e seu DTO.
 */
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
@Component
@Mapper(componentModel = "spring")
public abstract class SubprocessoMapper {
    @Autowired
    protected ProcessoRepo processoRepo;
    @Autowired
    protected UnidadeRepo unidadeRepo;
    @Autowired
    protected MapaRepo mapaRepo;

    @Mapping(source = "processo.codigo", target = "processoCodigo")
    @Mapping(source = "unidade.codigo", target = "unidadeCodigo")
    @Mapping(source = "mapa.codigo", target = "mapaCodigo")
    public abstract SubprocessoDto toDTO(Subprocesso subprocesso);

    @Mapping(source = "processoCodigo", target = "processo")
    @Mapping(source = "unidadeCodigo", target = "unidade")
    @Mapping(source = "mapaCodigo", target = "mapa")
    @Mapping(target = "dataFimEtapa2", ignore = true)

    public abstract Subprocesso toEntity(SubprocessoDto dto);

    public Processo mapProcesso(Long value) {
        if (value == null) {
            return null;
        }
        return processoRepo.findById(value)
                .orElseThrow(() -> new RuntimeException("Processo não encontrado com o código: " + value));
    }

    public Unidade mapUnidade(Long value) {
        if (value == null) {
            return null;
        }
        return unidadeRepo.findById(value)
                .orElseThrow(() -> new RuntimeException("Unidade não encontrada com o código: " + value));
    }

    public Mapa mapMapa(Long value) {
        if (value == null) {
            return null;
        }
        return mapaRepo.findById(value)
                .orElseThrow(() -> new RuntimeException("Mapa não encontrado com o código: " + value));
    }
}