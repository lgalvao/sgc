package sgc.conhecimento.dto;

import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import sgc.atividade.modelo.Atividade;
import sgc.conhecimento.modelo.Conhecimento;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-10-10T13:02:29-0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 25 (Amazon.com Inc.)"
)
@Component
public class ConhecimentoMapperImpl implements ConhecimentoMapper {

    @Override
    public ConhecimentoDto toDTO(Conhecimento conhecimento) {
        if ( conhecimento == null ) {
            return null;
        }

        Long atividadeCodigo = null;
        Long codigo = null;
        String descricao = null;

        atividadeCodigo = conhecimentoAtividadeCodigo( conhecimento );
        codigo = conhecimento.getCodigo();
        descricao = conhecimento.getDescricao();

        ConhecimentoDto conhecimentoDto = new ConhecimentoDto( codigo, atividadeCodigo, descricao );

        return conhecimentoDto;
    }

    @Override
    public Conhecimento toEntity(ConhecimentoDto conhecimentoDTO) {
        if ( conhecimentoDTO == null ) {
            return null;
        }

        Conhecimento conhecimento = new Conhecimento();

        conhecimento.setAtividade( map( conhecimentoDTO.atividadeCodigo() ) );
        conhecimento.setCodigo( conhecimentoDTO.codigo() );
        conhecimento.setDescricao( conhecimentoDTO.descricao() );

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
