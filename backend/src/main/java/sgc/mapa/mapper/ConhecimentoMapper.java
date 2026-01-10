package sgc.mapa.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sgc.comum.erros.ErroEntidadeDeveriaExistir;
import sgc.mapa.dto.ConhecimentoDto;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.AtividadeRepo;
import sgc.mapa.model.Conhecimento;

@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
@Component
@Mapper(componentModel = "spring")
public abstract class ConhecimentoMapper {
    @Autowired
    protected AtividadeRepo atividadeRepo;

    @Mapping(source = "atividade.codigo", target = "atividadeCodigo")
    public abstract ConhecimentoDto toDto(Conhecimento conhecimento);

    @Mapping(source = "atividadeCodigo", target = "atividade")
    public abstract Conhecimento toEntity(ConhecimentoDto conhecimentoDTO);

    public Atividade map(Long codigo) {
        return codigo != null
                ? atividadeRepo
                .findById(codigo)
                .orElseThrow(() -> ErroEntidadeDeveriaExistir.fkViolada(
                        "Atividade", codigo, "ConhecimentoMapper"))
                : null;
    }
}
