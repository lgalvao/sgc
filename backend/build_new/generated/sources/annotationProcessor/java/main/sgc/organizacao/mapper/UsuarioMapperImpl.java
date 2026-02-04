package sgc.organizacao.mapper;

import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import sgc.organizacao.dto.AtribuicaoTemporariaDto;
import sgc.organizacao.dto.UnidadeDto;
import sgc.organizacao.dto.UsuarioDto;
import sgc.organizacao.model.AtribuicaoTemporaria;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-02-04T16:19:25-0300",
    comments = "version: 1.6.3, compiler: IncrementalProcessingEnvironment from gradle-language-java-9.3.0.jar, environment: Java 25.0.1 (Amazon.com Inc.)"
)
@Component
public class UsuarioMapperImpl implements UsuarioMapper {

    @Override
    public UnidadeDto toUnidadeDto(Unidade unidade, boolean isElegivel) {
        if ( unidade == null ) {
            return null;
        }

        UnidadeDto.UnidadeDtoBuilder unidadeDto = UnidadeDto.builder();

        if ( unidade != null ) {
            unidadeDto.codigoPai( unidadeUnidadeSuperiorCodigo( unidade ) );
            unidadeDto.codigo( unidade.getCodigo() );
            unidadeDto.nome( unidade.getNome() );
            unidadeDto.sigla( unidade.getSigla() );
            unidadeDto.tituloTitular( unidade.getTituloTitular() );
        }
        unidadeDto.isElegivel( isElegivel );
        unidadeDto.tipo( unidade.getTipo() != null ? unidade.getTipo().name() : null );
        unidadeDto.subunidades( new java.util.ArrayList<>() );

        return unidadeDto.build();
    }

    @Override
    public UsuarioDto toUsuarioDto(Usuario usuario) {
        if ( usuario == null ) {
            return null;
        }

        UsuarioDto.UsuarioDtoBuilder usuarioDto = UsuarioDto.builder();

        usuarioDto.unidadeCodigo( usuarioUnidadeLotacaoCodigo( usuario ) );
        usuarioDto.tituloEleitoral( usuario.getTituloEleitoral() );
        usuarioDto.nome( usuario.getNome() );
        usuarioDto.email( usuario.getEmail() );
        usuarioDto.matricula( usuario.getMatricula() );

        return usuarioDto.build();
    }

    @Override
    public AtribuicaoTemporariaDto toAtribuicaoTemporariaDto(AtribuicaoTemporaria atribuicao) {
        if ( atribuicao == null ) {
            return null;
        }

        AtribuicaoTemporariaDto.AtribuicaoTemporariaDtoBuilder atribuicaoTemporariaDto = AtribuicaoTemporariaDto.builder();

        atribuicaoTemporariaDto.unidade( toUnidadeDtoSimples( atribuicao.getUnidade() ) );
        atribuicaoTemporariaDto.usuario( toUsuarioDto( atribuicao.getUsuario() ) );
        atribuicaoTemporariaDto.codigo( atribuicao.getCodigo() );
        atribuicaoTemporariaDto.dataInicio( atribuicao.getDataInicio() );
        atribuicaoTemporariaDto.dataTermino( atribuicao.getDataTermino() );
        atribuicaoTemporariaDto.justificativa( atribuicao.getJustificativa() );

        return atribuicaoTemporariaDto.build();
    }

    private Long unidadeUnidadeSuperiorCodigo(Unidade unidade) {
        Unidade unidadeSuperior = unidade.getUnidadeSuperior();
        if ( unidadeSuperior == null ) {
            return null;
        }
        return unidadeSuperior.getCodigo();
    }

    private Long usuarioUnidadeLotacaoCodigo(Usuario usuario) {
        Unidade unidadeLotacao = usuario.getUnidadeLotacao();
        if ( unidadeLotacao == null ) {
            return null;
        }
        return unidadeLotacao.getCodigo();
    }
}
