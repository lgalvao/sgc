package sgc.conhecimento.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import sgc.atividade.modelo.Atividade;
import sgc.atividade.modelo.AtividadeRepo;
import sgc.conhecimento.modelo.Conhecimento;

@Mapper(componentModel = "spring")
public abstract class ConhecimentoMapper {

    @Autowired
    private AtividadeRepo atividadeRepo;

    @Mapping(source = "atividade.codigo", target = "atividadeCodigo")
    public abstract ConhecimentoDto toDTO(Conhecimento conhecimento);

    @Mapping(source = "atividadeCodigo", target = "atividade")
    public abstract Conhecimento toEntity(ConhecimentoDto conhecimentoDTO);

    public Atividade map(Long value) {
        if (value == null) {
            return null;
        }
        return atividadeRepo.findById(value)
            .orElseThrow(() -> new RuntimeException("Atividade não encontrada com o código: " + value));
    }
}
