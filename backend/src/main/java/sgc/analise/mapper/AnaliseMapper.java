package sgc.analise.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import sgc.analise.dto.AnaliseHistoricoDto;
import sgc.analise.dto.AnaliseValidacaoHistoricoDto;
import sgc.analise.model.Analise;
import sgc.comum.util.FormatadorData;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.UnidadeRepo;

/**
 * Mapper para converter a entidade {@link Analise} em DTOs.
 */
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
@Mapper(componentModel = "spring", imports = FormatadorData.class)
public abstract class AnaliseMapper {
    @Autowired
    protected UnidadeRepo unidadeRepo;

    /**
     * Converte uma entidade {@link Analise} em um DTO {@link AnaliseHistoricoDto}.
     */
    @Mapping(target = "unidadeSigla", expression = "java(getUnidadeSigla(analise.getUnidadeCodigo()))")
    @Mapping(target = "unidadeNome", expression = "java(getUnidadeNome(analise.getUnidadeCodigo()))")
    @Mapping(target = "analistaUsuarioTitulo", source = "usuarioTitulo")
    @Mapping(target = "dataHoraFormatada", expression = "java(FormatadorData.formatarDataHora(analise.getDataHora()))")
    public abstract AnaliseHistoricoDto toAnaliseHistoricoDto(Analise analise);

    /**
     * Converte uma entidade {@link Analise} em um DTO {@link AnaliseValidacaoHistoricoDto}.
     */
    @Mapping(target = "unidadeSigla", expression = "java(getUnidadeSigla(analise.getUnidadeCodigo()))")
    @Mapping(target = "analistaUsuarioTitulo", source = "usuarioTitulo")
    public abstract AnaliseValidacaoHistoricoDto toAnaliseValidacaoHistoricoDto(Analise analise);

    protected String getUnidadeSigla(Long codUnidade) {
        if (codUnidade == null) return null;
        return unidadeRepo.findById(codUnidade)
                .map(Unidade::getSigla)
                .orElse(null);
    }

    protected String getUnidadeNome(Long codUnidade) {
        if (codUnidade == null) return null;
        return unidadeRepo.findById(codUnidade)
                .map(Unidade::getNome)
                .orElse(null);
    }
}
