package sgc.conhecimento.dto;

import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import sgc.atividade.modelo.Atividade;
import sgc.conhecimento.modelo.Conhecimento;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-10-08T14:30:14-0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 25 (Amazon.com Inc.)"
)
@Component
public class ConhecimentoMapperImpl implements ConhecimentoMapper {

    @Override
    public ConhecimentoDto toDTO(Conhecimento conhecimento) {
        if ( conhecimento == null ) {
            return null;
        }

        ConhecimentoDto conhecimentoDto = new ConhecimentoDto();

        conhecimentoDto.setAtividadeCodigo( conhecimentoAtividadeCodigo( conhecimento ) );
        conhecimentoDto.setCodigo( conhecimento.getCodigo() );
        conhecimentoDto.setDescricao( conhecimento.getDescricao() );

        return conhecimentoDto;
    }

    @Override
    public Conhecimento toEntity(ConhecimentoDto conhecimentoDTO) {
        if ( conhecimentoDTO == null ) {
            return null;
        }

        Conhecimento conhecimento = new Conhecimento();

        conhecimento.setAtividade( map( conhecimentoDTO.getAtividadeCodigo() ) );
        conhecimento.setCodigo( conhecimentoDTO.getCodigo() );
        conhecimento.setDescricao( conhecimentoDTO.getDescricao() );

        return conhecimento;
    }

    private Long conhecimentoAtividadeCodigo(Conhecimento conhecimento) {
        Atividade atividade = conhecimento.getAtividade();
        if ( atividade == null ) {
            return null;
        }
        return atividade.getCodigo();
    }
}
