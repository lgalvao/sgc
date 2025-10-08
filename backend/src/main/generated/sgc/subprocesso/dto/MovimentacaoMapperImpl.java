package sgc.subprocesso.dto;

import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import sgc.subprocesso.modelo.Movimentacao;
import sgc.unidade.modelo.Unidade;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-10-08T14:30:14-0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 25 (Amazon.com Inc.)"
)
@Component
public class MovimentacaoMapperImpl implements MovimentacaoMapper {

    @Override
    public MovimentacaoDto toDTO(Movimentacao movimentacao) {
        if ( movimentacao == null ) {
            return null;
        }

        MovimentacaoDto movimentacaoDto = new MovimentacaoDto();

        movimentacaoDto.setUnidadeOrigemCodigo( movimentacaoUnidadeOrigemCodigo( movimentacao ) );
        movimentacaoDto.setUnidadeOrigemSigla( movimentacaoUnidadeOrigemSigla( movimentacao ) );
        movimentacaoDto.setUnidadeOrigemNome( movimentacaoUnidadeOrigemNome( movimentacao ) );
        movimentacaoDto.setUnidadeDestinoCodigo( movimentacaoUnidadeDestinoCodigo( movimentacao ) );
        movimentacaoDto.setUnidadeDestinoSigla( movimentacaoUnidadeDestinoSigla( movimentacao ) );
        movimentacaoDto.setUnidadeDestinoNome( movimentacaoUnidadeDestinoNome( movimentacao ) );
        movimentacaoDto.setCodigo( movimentacao.getCodigo() );
        movimentacaoDto.setDataHora( movimentacao.getDataHora() );
        movimentacaoDto.setDescricao( movimentacao.getDescricao() );

        return movimentacaoDto;
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
