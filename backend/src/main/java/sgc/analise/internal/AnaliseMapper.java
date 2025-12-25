package sgc.analise.internal;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import sgc.analise.api.AnaliseHistoricoDto;
import sgc.analise.api.AnaliseValidacaoHistoricoDto;
import sgc.analise.internal.model.Analise;
import sgc.unidade.api.model.UnidadeRepo;

/**
 * Mapper para converter a entidade {@link Analise} em DTOs.
 */
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

    protected String getUnidadeSigla(Long codigo) {
        if (codigo == null) return null;
        return unidadeRepo.findById(codigo)
                .map(sgc.unidade.api.model.Unidade::getSigla)
                .orElse(null);
    }
}
