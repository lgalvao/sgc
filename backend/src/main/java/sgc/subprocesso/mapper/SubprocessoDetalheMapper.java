package sgc.subprocesso.mapper;

import org.jspecify.annotations.Nullable;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.util.CollectionUtils;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.subprocesso.dto.SubprocessoDetalheDto;
import sgc.subprocesso.dto.UnidadeDetalheDto;
import sgc.subprocesso.dto.ResponsavelDetalheDto;
import sgc.subprocesso.dto.SubprocessoPermissoesDto;
import sgc.subprocesso.model.Movimentacao;
import sgc.subprocesso.model.Subprocesso;

import java.time.LocalDateTime;
import java.util.List;

@SuppressWarnings("unused")
@Mapper(componentModel = "spring", uses = MovimentacaoMapper.class)
public interface SubprocessoDetalheMapper {
    @Mapping(target = "codigo", source = "sp.codigo")
    @Mapping(target = "unidade", source = "sp.unidade")
    @Mapping(target = "titular", expression = "java(mapTitular(sp, titular, responsavel))")
    @Mapping(target = "responsavel", expression = "java(mapResponsavel(sp, responsavel))")
    @Mapping(target = "situacao", expression = "java(sp != null ? sp.getSituacao().name() : null)")
    @Mapping(target = "situacaoLabel", expression = "java(sp != null ? sp.getSituacao().getDescricao() : null)")
    @Mapping(target = "localizacaoAtual", expression = "java(mapLocalizacaoAtual(movimentacoes))")
    @Mapping(target = "processoDescricao", source = "sp.processo.descricao")
    @Mapping(target = "tipoProcesso", expression = "java(sp != null && sp.getProcesso() != null ? sp.getProcesso().getTipo().name() : null)")
    @Mapping(target = "prazoEtapaAtual", expression = "java(mapPrazoEtapaAtual(sp))")
    @Mapping(target = "isEmAndamento", expression = "java(sp != null && sp.isEmAndamento())")
    @Mapping(target = "etapaAtual", source = "sp.etapaAtual")
    @Mapping(target = "movimentacoes", source = "movimentacoes")
    @Mapping(target = "permissoes", source = "permissoes")
    SubprocessoDetalheDto toDto(@Nullable Subprocesso sp,
                                @Nullable Usuario responsavel,
                                @Nullable Usuario titular,
                                @Nullable List<Movimentacao> movimentacoes,
                                @Nullable SubprocessoPermissoesDto permissoes);

    @Mapping(target = "codigo", source = "codigo")
    @Mapping(target = "sigla", source = "sigla")
    @Mapping(target = "nome", source = "nome")
    UnidadeDetalheDto toUnidadeDto(@Nullable Unidade unidade);

    default @Nullable ResponsavelDetalheDto mapResponsavel(@Nullable Subprocesso sp, @Nullable Usuario responsavel) {
        if (responsavel == null || sp == null) return null;

        String tituloTitular = sp.getUnidade().getTituloTitular();
        String tipo = "Substituição";
        if (responsavel.getTituloEleitoral() != null && responsavel.getTituloEleitoral().equals(tituloTitular)) {
            tipo = "Titular";
        }

        return ResponsavelDetalheDto.builder()
                .nome(responsavel.getNome())
                .ramal(responsavel.getRamal())
                .email(responsavel.getEmail())
                .tipoResponsabilidade(tipo)
                .build();
    }

    default @Nullable ResponsavelDetalheDto mapTitular(@Nullable Subprocesso sp, @Nullable Usuario titular, @Nullable Usuario responsavel) {
        if (titular == null) return null;
        return ResponsavelDetalheDto.builder()
                .nome(titular.getNome())
                .tipoResponsabilidade("Titular")
                .ramal(titular.getRamal())
                .email(titular.getEmail())
                .build();
    }

    default String mapLocalizacaoAtual(List<Movimentacao> movimentacoes) {
        if (!CollectionUtils.isEmpty(movimentacoes)) {
            Movimentacao movimentacaoRecente = movimentacoes.getFirst();
            if (movimentacaoRecente.getUnidadeDestino() != null) {
                return movimentacaoRecente.getUnidadeDestino().getSigla();
            }
        }
        return "";
    }

    default @Nullable LocalDateTime mapPrazoEtapaAtual(@Nullable Subprocesso sp) {
        if (sp == null) return null;
        return sp.getDataLimiteEtapa1() != null
                ? sp.getDataLimiteEtapa1()
                : sp.getDataLimiteEtapa2();
    }
}
