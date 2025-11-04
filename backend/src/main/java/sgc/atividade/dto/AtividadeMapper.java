package sgc.atividade.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sgc.atividade.modelo.Atividade;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.modelo.Mapa;
import sgc.mapa.modelo.MapaRepo;

/**
 * Mapper (usando MapStruct) entre a entidade Atividade e seu DTO.
 */
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
@Component
@Mapper(componentModel = "spring")
public abstract class AtividadeMapper {
    /**
     * Repositório de mapas, injetado para buscar a entidade Mapa durante o mapeamento.
     */
    @Autowired
    protected MapaRepo mapaRepo;

    /**
     * Converte uma entidade {@link Atividade} em um DTO {@link AtividadeDto}.
     *
     * @param atividade A entidade a ser convertida.
     * @return O DTO correspondente.
     */
    @Mapping(source = "mapa.codigo", target = "mapaCodigo")
    public abstract AtividadeDto toDto(Atividade atividade);

    /**
     * Converte um DTO {@link AtividadeDto} em uma entidade {@link Atividade}.
     *
     * @param atividadeDto O DTO a ser convertido.
     * @return A entidade correspondente.
     */
    @Mapping(source = "mapaCodigo", target = "mapa")
    @Mapping(target = "conhecimentos", ignore = true)
    public abstract Atividade toEntity(AtividadeDto atividadeDto);

    /**
     * Mapeia um código de mapa para uma entidade {@link Mapa}.
     *
     * @param codMapa O código do mapa.
     * @return A entidade {@link Mapa} correspondente.
     * @throws ErroEntidadeNaoEncontrada se o mapa não for encontrado.
     */
    public Mapa map(Long codMapa) {
        if (codMapa == null) return null;
        return mapaRepo.findById(codMapa).orElseThrow(() -> new ErroEntidadeNaoEncontrada("Mapa", codMapa));
    }
}