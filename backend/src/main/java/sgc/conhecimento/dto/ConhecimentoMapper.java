package sgc.conhecimento.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sgc.atividade.modelo.Atividade;
import sgc.atividade.modelo.AtividadeRepo;
import sgc.comum.erros.ErroDominioNaoEncontrado;
import sgc.conhecimento.modelo.Conhecimento;

@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
@Component
@Mapper(componentModel = "spring")
public abstract class ConhecimentoMapper {
    @Autowired
    protected AtividadeRepo atividadeRepo;

    @Mapping(source = "atividade.codigo", target = "atividadeCodigo")
    public abstract ConhecimentoDto toDTO(Conhecimento conhecimento);

    @Mapping(source = "atividadeCodigo", target = "atividade")
    public abstract Conhecimento toEntity(ConhecimentoDto conhecimentoDTO);

    public Atividade map(Long codigo) {
        return codigo != null ? atividadeRepo.findById(codigo).orElseThrow(() -> new ErroDominioNaoEncontrado("Atividade não encontrada com o código", codigo)) : null;
    }
}
