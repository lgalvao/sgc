package sgc.conhecimento;

import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import sgc.atividade.Atividade;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-10-07T13:39:14-0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 25 (Amazon.com Inc.)"
)
@Component
public class ConhecimentoMapperImpl implements ConhecimentoMapper {

    @Override
    public ConhecimentoDTO toDTO(Conhecimento conhecimento) {
        if ( conhecimento == null ) {
            return null;
        }

        ConhecimentoDTO conhecimentoDTO = new ConhecimentoDTO();

        conhecimentoDTO.setAtividadeCodigo( conhecimentoAtividadeCodigo( conhecimento ) );
        conhecimentoDTO.setCodigo( conhecimento.getCodigo() );
        conhecimentoDTO.setDescricao( conhecimento.getDescricao() );

        return conhecimentoDTO;
    }

    @Override
    public Conhecimento toEntity(ConhecimentoDTO conhecimentoDTO) {
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
        if ( conhecimento == null ) {
            return null;
        }
        Atividade atividade = conhecimento.getAtividade();
        if ( atividade == null ) {
            return null;
        }
        Long codigo = atividade.getCodigo();
        if ( codigo == null ) {
            return null;
        }
        return codigo;
    }
}
