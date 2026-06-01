package sgc.organizacao;

import org.jspecify.annotations.*;
import org.springframework.stereotype.*;
import sgc.organizacao.dto.*;
import sgc.organizacao.model.*;

import java.util.*;

@Component
public class OrganizacaoDtoMapper {

    public @Nullable UsuarioResumoDto paraUsuarioResumo(@Nullable Usuario usuario) {
        if (usuario == null) {
            return null;
        }
        return paraUsuarioResumoObrigatorio(usuario);
    }

    public UsuarioResumoDto paraUsuarioResumoObrigatorio(Usuario usuario) {
        return UsuarioResumoDto.builder()
                .tituloEleitoral(usuario.getTituloEleitoral())
                .matricula(usuario.getMatricula())
                .nome(usuario.getNome())
                .email(usuario.getEmail())
                .ramal(usuario.getRamal())
                .build();
    }

    public UnidadeResumoDto paraUnidadeResumoObrigatoria(Unidade unidade) {
        Objects.requireNonNull(unidade, "Unidade obrigatoria para resumo");
        return UnidadeResumoDto.fromResumoObrigatorio(
                unidade.getCodigo(),
                unidade.getNome(),
                unidade.getSigla(),
                unidade.getTipo() != null ? unidade.getTipo().name() : null,
                unidade.getTituloTitular()
        );
    }

    public @Nullable UnidadeDto paraUnidadeDto(@Nullable Unidade unidade) {
        if (unidade == null) {
            return null;
        }
        return paraUnidadeDtoObrigatoria(unidade);
    }

    public UnidadeDto paraUnidadeDtoObrigatoria(Unidade unidade) {
        Objects.requireNonNull(unidade, "Unidade obrigatoria para montagem do DTO");

        UnidadeDto dto = UnidadeDto.fromResumoObrigatorio(
                unidade.getCodigo(),
                unidade.getNome(),
                unidade.getSigla(),
                unidade.getUnidadeSuperior() != null ? unidade.getUnidadeSuperior().getCodigo() : null,
                unidade.getTipo() != null ? unidade.getTipo().name() : null,
                unidade.getTituloTitular()
        );

        dto.setTitular(paraUsuarioResumo(unidade.getTitular()));

        Responsabilidade responsabilidade = unidade.getResponsabilidade();
        if (responsabilidade != null) {
            dto.setResponsavel(paraUsuarioResumo(responsabilidade.getUsuario()));
            dto.setTipoResponsabilidade(responsabilidade.getTipo());
            dto.setDataInicioResponsabilidade(responsabilidade.getDataInicio());
            dto.setDataFimResponsabilidade(responsabilidade.getDataFim());
        }

        return dto;
    }

    public UnidadeDto paraUnidadeDtoResumoObrigatoria(Unidade unidade) {
        Objects.requireNonNull(unidade, "Unidade obrigatoria para resumo");
        return UnidadeDto.fromResumoObrigatorio(
                unidade.getCodigo(),
                unidade.getNome(),
                unidade.getSigla(),
                null,
                unidade.getTipo() != null ? unidade.getTipo().name() : null,
                unidade.getTituloTitular()
        );
    }

    public UsuarioConsultaDto paraUsuarioConsultaDto(UsuarioConsultaLeitura usuario) {
        UnidadeResumoDto unidade = UnidadeResumoDto.fromResumoObrigatorio(
                usuario.unidadeCodigo(),
                usuario.unidadeNome(),
                usuario.unidadeSigla(),
                usuario.unidadeTipo() != null ? usuario.unidadeTipo().name() : null,
                usuario.unidadeTituloTitular()
        );

        return UsuarioConsultaDto.builder()
                .tituloEleitoral(usuario.tituloEleitoral())
                .matricula(usuario.matricula())
                .nome(usuario.nome())
                .email(usuario.email())
                .ramal(usuario.ramal())
                .unidade(unidade)
                .perfis(List.of())
                .build();
    }
}
