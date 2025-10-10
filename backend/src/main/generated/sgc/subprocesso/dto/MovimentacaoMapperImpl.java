package sgc.subprocesso.dto;

import java.time.LocalDateTime;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import sgc.subprocesso.modelo.Movimentacao;
import sgc.unidade.modelo.Unidade;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-10-10T13:02:29-0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 25 (Amazon.com Inc.)"
)
@Component
public class MovimentacaoMapperImpl implements MovimentacaoMapper {

    @Override
    public MovimentacaoDto toDTO(Movimentacao movimentacao) {
        if ( movimentacao == null ) {
            return null;
        }

        Long unidadeOrigemCodigo = null;
        String unidadeOrigemSigla = null;
        String unidadeOrigemNome = null;
        Long unidadeDestinoCodigo = null;
        String unidadeDestinoSigla = null;
        String unidadeDestinoNome = null;
        Long codigo = null;
        LocalDateTime dataHora = null;
        String descricao = null;

        unidadeOrigemCodigo = movimentacaoUnidadeOrigemCodigo( movimentacao );
        unidadeOrigemSigla = movimentacaoUnidadeOrigemSigla( movimentacao );
        unidadeOrigemNome = movimentacaoUnidadeOrigemNome( movimentacao );
        unidadeDestinoCodigo = movimentacaoUnidadeDestinoCodigo( movimentacao );
        unidadeDestinoSigla = movimentacaoUnidadeDestinoSigla( movimentacao );
        unidadeDestinoNome = movimentacaoUnidadeDestinoNome( movimentacao );
        codigo = movimentacao.getCodigo();
        dataHora = movimentacao.getDataHora();
        descricao = movimentacao.getDescricao();

        MovimentacaoDto movimentacaoDto = new MovimentacaoDto( codigo, dataHora, unidadeOrigemCodigo, unidadeOrigemSigla, unidadeOrigemNome, unidadeDestinoCodigo, unidadeDestinoSigla, unidadeDestinoNome, descricao );

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
