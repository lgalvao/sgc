package sgc.analise.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import sgc.analise.dto.AnaliseHistoricoDto;
import sgc.analise.dto.AnaliseValidacaoHistoricoDto;
import sgc.analise.model.Analise;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.UnidadeRepo;

/**
 * Mapper para converter a entidade {@link Analise} em DTOs.
 */
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
@Mapper(componentModel = "spring")
public abstract class AnaliseMapper {
    @Autowired
    protected UnidadeRepo unidadeRepo;

    /**
     * Converte uma entidade {@link Analise} em um DTO {@link AnaliseHistoricoDto}.
     *
     * @param analise A entidade a ser convertida.
     * @return O DTO correspondente.
     */
    @Mapping(target = "unidadeSigla", expression = "java(getUnidadeSigla(analise.getUnidadeCodigo()))")
    @Mapping(target = "analistaUsuarioTitulo", source = "usuarioTitulo")
    public abstract AnaliseHistoricoDto toAnaliseHistoricoDto(Analise analise);

    /**
     * Converte uma entidade {@link Analise} em um DTO {@link AnaliseValidacaoHistoricoDto}.
     *
     * @param analise A entidade a ser convertida.
     * @return O DTO correspondente.
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
}
