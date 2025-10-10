package sgc.processo.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import sgc.comum.enums.SituacaoProcesso;
import sgc.processo.enums.TipoProcesso;
import sgc.processo.modelo.Processo;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-10-10T13:02:30-0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 25 (Amazon.com Inc.)"
)
@Component
public class ProcessoMapperImpl implements ProcessoMapper {

    @Override
    public ProcessoDto toDTO(Processo processo) {
        if ( processo == null ) {
            return null;
        }

        Long codigo = null;
        LocalDateTime dataCriacao = null;
        LocalDateTime dataFinalizacao = null;
        LocalDate dataLimite = null;
        String descricao = null;
        SituacaoProcesso situacao = null;
        String tipo = null;

        codigo = processo.getCodigo();
        dataCriacao = processo.getDataCriacao();
        dataFinalizacao = processo.getDataFinalizacao();
        dataLimite = processo.getDataLimite();
        descricao = processo.getDescricao();
        situacao = processo.getSituacao();
        if ( processo.getTipo() != null ) {
            tipo = processo.getTipo().name();
        }

        ProcessoDto processoDto = new ProcessoDto( codigo, dataCriacao, dataFinalizacao, dataLimite, descricao, situacao, tipo );

        return processoDto;
    }

    @Override
    public Processo toEntity(ProcessoDto processoDTO) {
        if ( processoDTO == null ) {
            return null;
        }

        Processo processo = new Processo();

        processo.setCodigo( processoDTO.codigo() );
        processo.setDataCriacao( processoDTO.dataCriacao() );
        processo.setDataFinalizacao( processoDTO.dataFinalizacao() );
        processo.setDataLimite( processoDTO.dataLimite() );
        processo.setDescricao( processoDTO.descricao() );
        processo.setSituacao( processoDTO.situacao() );
        if ( processoDTO.tipo() != null ) {
            processo.setTipo( Enum.valueOf( TipoProcesso.class, processoDTO.tipo() ) );
        }

        return processo;
    }
}
