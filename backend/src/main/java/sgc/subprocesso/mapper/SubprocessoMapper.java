package sgc.subprocesso.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sgc.mapa.model.Mapa;
import sgc.mapa.model.MapaRepo;
import sgc.processo.model.Processo;
import sgc.processo.model.ProcessoRepo;
import sgc.subprocesso.dto.SubprocessoDto;
import sgc.subprocesso.model.Subprocesso;
import sgc.unidade.model.Unidade;
import sgc.unidade.model.UnidadeRepo;

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

    @Mapping(source = "processo.codigo", target = "codProcesso")
    @Mapping(source = "unidade.codigo", target = "codUnidade")
    @Mapping(source = "mapa.codigo", target = "codMapa")
    public abstract SubprocessoDto toDTO(Subprocesso subprocesso);

    @Mapping(source = "codProcesso", target = "processo")
    @Mapping(source = "codUnidade", target = "unidade")
    @Mapping(source = "codMapa", target = "mapa")
    @Mapping(target = "dataFimEtapa2", ignore = true)
    public abstract Subprocesso toEntity(SubprocessoDto dto);

    public Processo mapProcesso(Long value) {
        if (value == null) {
            return null;
        }
        return processoRepo
                .findById(value)
                .orElseThrow(
                        () ->
                                new RuntimeException(
                                        "Processo não encontrado com o código: %d"
                                                .formatted(value)));
    }

    public Unidade mapUnidade(Long value) {
        if (value == null) {
            return null;
        }
        return unidadeRepo
                .findById(value)
                .orElseThrow(
                        () ->
                                new RuntimeException(
                                        "Unidade não encontrada com o código: %d"
                                                .formatted(value)));
    }

    public Mapa mapMapa(Long value) {
        if (value == null) {
            return null;
        }
        return mapaRepo.findById(value)
                .orElseThrow(
                        () ->
                                new RuntimeException(
                                        "Mapa não encontrado com o código: %d".formatted(value)));
    }
}
