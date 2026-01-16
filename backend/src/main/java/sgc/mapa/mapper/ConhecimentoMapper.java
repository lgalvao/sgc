package sgc.mapa.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sgc.comum.repo.RepositorioComum;
import sgc.mapa.dto.AtualizarConhecimentoRequest;
import sgc.mapa.dto.ConhecimentoDto;
import sgc.mapa.dto.ConhecimentoResponse;
import sgc.mapa.dto.CriarConhecimentoRequest;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.AtividadeRepo;
import sgc.mapa.model.Conhecimento;

@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
@Component
@Mapper(componentModel = "spring")
public abstract class ConhecimentoMapper {
    @Autowired
    protected AtividadeRepo atividadeRepo;

    @Autowired
    protected RepositorioComum repo;

    @Mapping(source = "atividade.codigo", target = "atividadeCodigo")
    public abstract ConhecimentoDto toDto(Conhecimento conhecimento);

    @Mapping(source = "atividadeCodigo", target = "atividade")
    public abstract Conhecimento toEntity(ConhecimentoDto conhecimentoDTO);

    @Mapping(source = "atividade.codigo", target = "atividadeCodigo")
    public abstract ConhecimentoResponse toResponse(Conhecimento conhecimento);

    @Mapping(source = "atividadeCodigo", target = "atividade")
    @Mapping(target = "codigo", ignore = true)
    public abstract Conhecimento toEntity(CriarConhecimentoRequest request);

    @Mapping(target = "codigo", ignore = true)
    @Mapping(target = "atividade", ignore = true)
    public abstract Conhecimento toEntity(AtualizarConhecimentoRequest request);

    public Atividade map(Long codigo) {
        return codigo != null
                ? repo.buscar(Atividade.class, codigo)
                : null;
    }
}
