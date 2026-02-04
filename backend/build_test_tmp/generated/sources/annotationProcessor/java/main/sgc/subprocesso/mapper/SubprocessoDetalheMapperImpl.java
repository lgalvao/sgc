package sgc.subprocesso.mapper;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.processo.model.Processo;
import sgc.subprocesso.dto.MovimentacaoDto;
import sgc.subprocesso.dto.SubprocessoDetalheDto;
import sgc.subprocesso.dto.SubprocessoPermissoesDto;
import sgc.subprocesso.dto.UnidadeDetalheDto;
import sgc.subprocesso.model.Movimentacao;
import sgc.subprocesso.model.Subprocesso;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-02-04T16:19:37-0300",
    comments = "version: 1.6.3, compiler: IncrementalProcessingEnvironment from gradle-language-java-9.3.0.jar, environment: Java 25.0.1 (Amazon.com Inc.)"
)
@Component
public class SubprocessoDetalheMapperImpl implements SubprocessoDetalheMapper {

    @Autowired
    private MovimentacaoMapper movimentacaoMapper;

    @Override
    public SubprocessoDetalheDto toDto(Subprocesso sp, Usuario responsavel, Usuario titular, List<Movimentacao> movimentacoes, SubprocessoPermissoesDto permissoes) {
        if ( sp == null && responsavel == null && titular == null && movimentacoes == null && permissoes == null ) {
            return null;
        }

        SubprocessoDetalheDto.SubprocessoDetalheDtoBuilder subprocessoDetalheDto = SubprocessoDetalheDto.builder();

        if ( sp != null ) {
            subprocessoDetalheDto.unidade( toUnidadeDto( sp.getUnidade() ) );
            subprocessoDetalheDto.processoDescricao( spProcessoDescricao( sp ) );
            subprocessoDetalheDto.etapaAtual( sp.getEtapaAtual() );
        }
        subprocessoDetalheDto.movimentacoes( movimentacaoListToMovimentacaoDtoList( movimentacoes ) );
        subprocessoDetalheDto.permissoes( permissoes );
        subprocessoDetalheDto.titular( mapTitular(sp, titular, responsavel) );
        subprocessoDetalheDto.responsavel( mapResponsavel(sp, responsavel) );
        subprocessoDetalheDto.situacao( sp.getSituacao().name() );
        subprocessoDetalheDto.situacaoLabel( sp.getSituacao().getDescricao() );
        subprocessoDetalheDto.localizacaoAtual( mapLocalizacaoAtual(movimentacoes) );
        subprocessoDetalheDto.tipoProcesso( sp.getProcesso().getTipo().name() );
        subprocessoDetalheDto.prazoEtapaAtual( mapPrazoEtapaAtual(sp) );
        subprocessoDetalheDto.isEmAndamento( sp.isEmAndamento() );

        return subprocessoDetalheDto.build();
    }

    @Override
    public UnidadeDetalheDto toUnidadeDto(Unidade unidade) {
        if ( unidade == null ) {
            return null;
        }

        UnidadeDetalheDto.UnidadeDetalheDtoBuilder unidadeDetalheDto = UnidadeDetalheDto.builder();

        unidadeDetalheDto.codigo( unidade.getCodigo() );
        unidadeDetalheDto.sigla( unidade.getSigla() );
        unidadeDetalheDto.nome( unidade.getNome() );

        return unidadeDetalheDto.build();
    }

    private String spProcessoDescricao(Subprocesso subprocesso) {
        Processo processo = subprocesso.getProcesso();
        if ( processo == null ) {
            return null;
        }
        return processo.getDescricao();
    }

    protected List<MovimentacaoDto> movimentacaoListToMovimentacaoDtoList(List<Movimentacao> list) {
        if ( list == null ) {
            return null;
        }

        List<MovimentacaoDto> list1 = new ArrayList<MovimentacaoDto>( list.size() );
        for ( Movimentacao movimentacao : list ) {
            list1.add( movimentacaoMapper.toDto( movimentacao ) );
        }

        return list1;
    }
}
