package sgc.subprocesso.mapper;

import org.jspecify.annotations.Nullable;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sgc.organizacao.model.Usuario;
import sgc.subprocesso.dto.SubprocessoDetalheDto;
import sgc.subprocesso.dto.SubprocessoPermissoesDto;
import sgc.subprocesso.model.Movimentacao;
import sgc.subprocesso.model.Subprocesso;

import java.time.LocalDateTime;
import java.util.List;

@Mapper(componentModel = "spring", uses = MovimentacaoMapper.class)
public interface SubprocessoDetalheMapper {
    @Mapping(target = "unidade", source = "sp.unidade")
    @Mapping(target = "titular", expression = "java(mapTitular(sp, titular, responsavel))")
    @Mapping(target = "responsavel", expression = "java(mapResponsavel(sp, responsavel))")
    @Mapping(target = "situacao", expression = "java(sp.getSituacao().name())")
    @Mapping(target = "situacaoLabel", expression = "java(sp.getSituacao().getDescricao())")
    @Mapping(target = "localizacaoAtual", expression = "java(mapLocalizacaoAtual(movimentacoes))")
    @Mapping(target = "processoDescricao", source = "sp.processo.descricao")
    @Mapping(target = "tipoProcesso", expression = "java(sp.getProcesso() != null ? sp.getProcesso().getTipo().name() : null)")
    @Mapping(target = "prazoEtapaAtual", expression = "java(mapPrazoEtapaAtual(sp))")
    @Mapping(target = "isEmAndamento", expression = "java(sp.isEmAndamento())")
    @Mapping(target = "etapaAtual", source = "sp.etapaAtual")
    @Mapping(target = "movimentacoes", source = "movimentacoes")
    @Mapping(target = "permissoes", source = "permissoes")
    SubprocessoDetalheDto toDto(Subprocesso sp, @Nullable Usuario responsavel, @Nullable Usuario titular, List<Movimentacao> movimentacoes, @Nullable SubprocessoPermissoesDto permissoes);

    @Mapping(target = "codigo", source = "codigo")
    @Mapping(target = "sigla", source = "sigla")
    @Mapping(target = "nome", source = "nome")
    SubprocessoDetalheDto.UnidadeDto toUnidadeDto(sgc.organizacao.model.Unidade unidade);

    default SubprocessoDetalheDto.@Nullable ResponsavelDto mapResponsavel(Subprocesso sp, @Nullable Usuario responsavel) {
        if (responsavel == null) return null;

        String tituloTitular = sp.getUnidade().getTituloTitular();
        String tipo = "Substituição";
        if (responsavel.getTituloEleitoral().equals(tituloTitular)) {
            tipo = "Titular";
        }

        return SubprocessoDetalheDto.ResponsavelDto.builder()
                .nome(responsavel.getNome())
                .ramal(responsavel.getRamal())
                .email(responsavel.getEmail())
                .tipoResponsabilidade(tipo)
                .build();
    }

    default SubprocessoDetalheDto.@Nullable ResponsavelDto mapTitular(Subprocesso sp, @Nullable Usuario titular, @Nullable Usuario responsavel) {
        if (titular == null) return null;
        return SubprocessoDetalheDto.ResponsavelDto.builder()
                .nome(titular.getNome())
                .tipoResponsabilidade("Titular")
                .ramal(titular.getRamal())
                .email(titular.getEmail())
                .build();
    }

    default String mapLocalizacaoAtual(List<Movimentacao> movimentacoes) {
        if (!org.springframework.util.CollectionUtils.isEmpty(movimentacoes)) {
            Movimentacao movimentacaoRecente = movimentacoes.get(0);
            if (movimentacaoRecente.getUnidadeDestino() != null) {
                return movimentacaoRecente.getUnidadeDestino().getSigla();
            }
        }
        return "";
    }

    default LocalDateTime mapPrazoEtapaAtual(Subprocesso sp) {
        return sp.getDataLimiteEtapa1() != null
                ? sp.getDataLimiteEtapa1()
                : sp.getDataLimiteEtapa2();
    }
}
