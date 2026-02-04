package sgc.subprocesso.mapper;

import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import sgc.comum.util.FormatadorData;
import sgc.organizacao.model.Unidade;
import sgc.subprocesso.dto.MovimentacaoDto;
import sgc.subprocesso.model.Movimentacao;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-02-04T16:19:25-0300",
    comments = "version: 1.6.3, compiler: IncrementalProcessingEnvironment from gradle-language-java-9.3.0.jar, environment: Java 25.0.1 (Amazon.com Inc.)"
)
@Component
public class MovimentacaoMapperImpl implements MovimentacaoMapper {

    @Override
    public MovimentacaoDto toDto(Movimentacao movimentacao) {
        if ( movimentacao == null ) {
            return null;
        }

        MovimentacaoDto.MovimentacaoDtoBuilder movimentacaoDto = MovimentacaoDto.builder();

        movimentacaoDto.unidadeOrigemCodigo( movimentacaoUnidadeOrigemCodigo( movimentacao ) );
        movimentacaoDto.unidadeOrigemSigla( movimentacaoUnidadeOrigemSigla( movimentacao ) );
        movimentacaoDto.unidadeOrigemNome( movimentacaoUnidadeOrigemNome( movimentacao ) );
        movimentacaoDto.unidadeDestinoCodigo( movimentacaoUnidadeDestinoCodigo( movimentacao ) );
        movimentacaoDto.unidadeDestinoSigla( movimentacaoUnidadeDestinoSigla( movimentacao ) );
        movimentacaoDto.unidadeDestinoNome( movimentacaoUnidadeDestinoNome( movimentacao ) );
        movimentacaoDto.codigo( movimentacao.getCodigo() );
        movimentacaoDto.dataHora( movimentacao.getDataHora() );
        movimentacaoDto.descricao( movimentacao.getDescricao() );

        movimentacaoDto.dataHoraFormatada( FormatadorData.formatarDataHora(movimentacao.getDataHora()) );

        return movimentacaoDto.build();
    }

    private Long movimentacaoUnidadeOrigemCodigo(Movimentacao movimentacao) {
        Unidade unidadeOrigem = movimentacao.getUnidadeOrigem();
        if ( unidadeOrigem == null ) {
            return null;
        }
        return unidadeOrigem.getCodigo();
    }

    private String movimentacaoUnidadeOrigemSigla(Movimentacao movimentacao) {
        Unidade unidadeOrigem = movimentacao.getUnidadeOrigem();
        if ( unidadeOrigem == null ) {
            return null;
        }
        return unidadeOrigem.getSigla();
    }

    private String movimentacaoUnidadeOrigemNome(Movimentacao movimentacao) {
        Unidade unidadeOrigem = movimentacao.getUnidadeOrigem();
        if ( unidadeOrigem == null ) {
            return null;
        }
        return unidadeOrigem.getNome();
    }

    private Long movimentacaoUnidadeDestinoCodigo(Movimentacao movimentacao) {
        Unidade unidadeDestino = movimentacao.getUnidadeDestino();
        if ( unidadeDestino == null ) {
            return null;
        }
        return unidadeDestino.getCodigo();
    }

    private String movimentacaoUnidadeDestinoSigla(Movimentacao movimentacao) {
        Unidade unidadeDestino = movimentacao.getUnidadeDestino();
        if ( unidadeDestino == null ) {
            return null;
        }
        return unidadeDestino.getSigla();
    }

    private String movimentacaoUnidadeDestinoNome(Movimentacao movimentacao) {
        Unidade unidadeDestino = movimentacao.getUnidadeDestino();
        if ( unidadeDestino == null ) {
            return null;
        }
        return unidadeDestino.getNome();
    }
}
