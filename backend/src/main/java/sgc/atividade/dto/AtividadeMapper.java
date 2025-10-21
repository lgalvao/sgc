package sgc.atividade.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sgc.atividade.modelo.Atividade;
import sgc.comum.erros.ErroDominioNaoEncontrado;
import sgc.mapa.modelo.Mapa;
import sgc.mapa.modelo.MapaRepo;

/**
 * Mapper (usando MapStruct) entre a entidade Atividade e seu DTO.
 */
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
@Component
@Mapper(componentModel = "spring")
/**
 * Mapper (usando MapStruct) entre a entidade {@link Atividade} e seu DTO.
 */
public abstract class AtividadeMapper {
    /**
     * Repositório de mapas, injetado para buscar a entidade Mapa durante o mapeamento.
     */
    @Autowired
    protected MapaRepo mapaRepo;

    /**
     * Converte uma entidade {@link Atividade} em um DTO {@link AtividadeDto}.
     * @param atividade A entidade a ser convertida.
     * @return O DTO correspondente.
     */
    @Mapping(source = "mapa.codigo", target = "mapaCodigo")
    public abstract AtividadeDto toDTO(Atividade atividade);

    /**
     * Converte um DTO {@link AtividadeDto} em uma entidade {@link Atividade}.
     * @param atividadeDTO O DTO a ser convertido.
     * @return A entidade correspondente.
     */
    @Mapping(source = "mapaCodigo", target = "mapa")
    @Mapping(target = "conhecimentos", ignore = true)
    public abstract Atividade toEntity(AtividadeDto atividadeDTO);

    /**
     * Mapeia um ID de mapa para uma entidade {@link Mapa}.
     * @param idMapa O ID do mapa.
     * @return A entidade {@link Mapa} correspondente.
     * @throws ErroDominioNaoEncontrado se o mapa não for encontrado.
     */
    public Mapa map(Long idMapa) {
        if (idMapa == null) return null;
        return mapaRepo.findById(idMapa).orElseThrow(() -> new ErroDominioNaoEncontrado("Mapa", idMapa));
    }
}