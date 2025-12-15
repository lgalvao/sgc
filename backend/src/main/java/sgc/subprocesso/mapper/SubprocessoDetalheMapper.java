package sgc.subprocesso.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sgc.sgrh.model.Usuario;
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
    SubprocessoDetalheDto toDto(Subprocesso sp, Usuario responsavel, Usuario titular, List<Movimentacao> movimentacoes, SubprocessoPermissoesDto permissoes);

    @Mapping(target = "codigo", source = "codigo")
    @Mapping(target = "sigla", source = "sigla")
    @Mapping(target = "nome", source = "nome")
    SubprocessoDetalheDto.UnidadeDto toUnidadeDto(sgc.unidade.model.Unidade unidade);

    default SubprocessoDetalheDto.ResponsavelDto mapResponsavel(Subprocesso sp, Usuario responsavel) {
        if (responsavel == null) return null;

        String tituloTitular = (sp.getUnidade() != null) ? sp.getUnidade().getTituloTitular() : null;
        String tipo = "Substituição";

        if (tituloTitular != null && tituloTitular.equals(responsavel.getTituloEleitoral())) {
            tipo = "Titular";
        }

        return SubprocessoDetalheDto.ResponsavelDto.builder()
                .nome(responsavel.getNome())
                .ramal(responsavel.getRamal())
                .email(responsavel.getEmail())
                .tipoResponsabilidade(tipo)
                .build();
    }

    default SubprocessoDetalheDto.ResponsavelDto mapTitular(Subprocesso sp, Usuario titular, Usuario responsavel) {
        String tituloTitular = (sp.getUnidade() != null) ? sp.getUnidade().getTituloTitular() : null;
        boolean isTitularResponsavel = false;
        if (responsavel != null && tituloTitular != null && tituloTitular.equals(responsavel.getTituloEleitoral())) {
            isTitularResponsavel = true;
        }

        if (tituloTitular != null && !isTitularResponsavel && titular != null) {
            return SubprocessoDetalheDto.ResponsavelDto.builder()
                    .codigo(null)
                    .nome(titular.getNome())
                    .tipoResponsabilidade("Titular")
                    .ramal(titular.getRamal())
                    .email(titular.getEmail())
                    .build();
        }
        return null;
    }

    default String mapLocalizacaoAtual(List<Movimentacao> movimentacoes) {
        if (movimentacoes != null && !movimentacoes.isEmpty()) {
            Movimentacao movimentacaoRecente = movimentacoes.get(0);
            if (movimentacaoRecente.getUnidadeDestino() != null) {
                return movimentacaoRecente.getUnidadeDestino().getSigla();
            }
        }
        return null;
    }

    default LocalDateTime mapPrazoEtapaAtual(Subprocesso sp) {
        return sp.getDataLimiteEtapa1() != null
                ? sp.getDataLimiteEtapa1()
                : sp.getDataLimiteEtapa2();
    }
}
