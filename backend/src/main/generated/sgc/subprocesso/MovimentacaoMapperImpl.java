package sgc.subprocesso;

import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import sgc.unidade.Unidade;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-10-07T13:39:14-0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 25 (Amazon.com Inc.)"
)
@Component
public class MovimentacaoMapperImpl implements MovimentacaoMapper {

    @Override
    public MovimentacaoDTO toDTO(Movimentacao movimentacao) {
        if ( movimentacao == null ) {
            return null;
        }

        MovimentacaoDTO movimentacaoDTO = new MovimentacaoDTO();

        movimentacaoDTO.setUnidadeOrigemCodigo( movimentacaoUnidadeOrigemCodigo( movimentacao ) );
        movimentacaoDTO.setUnidadeOrigemSigla( movimentacaoUnidadeOrigemSigla( movimentacao ) );
        movimentacaoDTO.setUnidadeOrigemNome( movimentacaoUnidadeOrigemNome( movimentacao ) );
        movimentacaoDTO.setUnidadeDestinoCodigo( movimentacaoUnidadeDestinoCodigo( movimentacao ) );
        movimentacaoDTO.setUnidadeDestinoSigla( movimentacaoUnidadeDestinoSigla( movimentacao ) );
        movimentacaoDTO.setUnidadeDestinoNome( movimentacaoUnidadeDestinoNome( movimentacao ) );
        movimentacaoDTO.setCodigo( movimentacao.getCodigo() );
        movimentacaoDTO.setDataHora( movimentacao.getDataHora() );
        movimentacaoDTO.setDescricao( movimentacao.getDescricao() );

        return movimentacaoDTO;
    }

    private Long movimentacaoUnidadeOrigemCodigo(Movimentacao movimentacao) {
        if ( movimentacao == null ) {
            return null;
        }
        Unidade unidadeOrigem = movimentacao.getUnidadeOrigem();
        if ( unidadeOrigem == null ) {
            return null;
        }
        Long codigo = unidadeOrigem.getCodigo();
        if ( codigo == null ) {
            return null;
        }
        return codigo;
    }

    private String movimentacaoUnidadeOrigemSigla(Movimentacao movimentacao) {
        if ( movimentacao == null ) {
            return null;
        }
        Unidade unidadeOrigem = movimentacao.getUnidadeOrigem();
        if ( unidadeOrigem == null ) {
            return null;
        }
        String sigla = unidadeOrigem.getSigla();
        if ( sigla == null ) {
            return null;
        }
        return sigla;
    }

    private String movimentacaoUnidadeOrigemNome(Movimentacao movimentacao) {
        if ( movimentacao == null ) {
            return null;
        }
        Unidade unidadeOrigem = movimentacao.getUnidadeOrigem();
        if ( unidadeOrigem == null ) {
            return null;
        }
        String nome = unidadeOrigem.getNome();
        if ( nome == null ) {
            return null;
        }
        return nome;
    }

    private Long movimentacaoUnidadeDestinoCodigo(Movimentacao movimentacao) {
        if ( movimentacao == null ) {
            return null;
        }
        Unidade unidadeDestino = movimentacao.getUnidadeDestino();
        if ( unidadeDestino == null ) {
            return null;
        }
        Long codigo = unidadeDestino.getCodigo();
        if ( codigo == null ) {
            return null;
        }
        return codigo;
    }

    private String movimentacaoUnidadeDestinoSigla(Movimentacao movimentacao) {
        if ( movimentacao == null ) {
            return null;
        }
        Unidade unidadeDestino = movimentacao.getUnidadeDestino();
        if ( unidadeDestino == null ) {
            return null;
        }
        String sigla = unidadeDestino.getSigla();
        if ( sigla == null ) {
            return null;
        }
        return sigla;
    }

    private String movimentacaoUnidadeDestinoNome(Movimentacao movimentacao) {
        if ( movimentacao == null ) {
            return null;
        }
        Unidade unidadeDestino = movimentacao.getUnidadeDestino();
        if ( unidadeDestino == null ) {
            return null;
        }
        String nome = unidadeDestino.getNome();
        if ( nome == null ) {
            return null;
        }
        return nome;
    }
}
