package sgc.analise.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import sgc.analise.model.Analise;
import sgc.organizacao.model.UnidadeRepo;

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

    /**
     * Busca a sigla da unidade pelo código.
     *
     * @param unidadeCodigo O código da unidade.
     * @return A sigla da unidade ou null se não encontrada.
     */
    protected String getUnidadeSigla(Long unidadeCodigo) {
        if (unidadeCodigo == null) {
            return null;
        }
        return unidadeRepo.findById(unidadeCodigo)
                .map(sgc.organizacao.model.Unidade::getSigla)
                .orElse(null);
    }
}
