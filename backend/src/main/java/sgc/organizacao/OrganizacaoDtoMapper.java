package sgc.organizacao;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import sgc.organizacao.dto.*;
import sgc.organizacao.model.*;

import java.util.List;
import java.util.Objects;

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
                unidade.getTipo().name(),
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
                unidade.getTipo().name(),
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
                unidade.getTipo().name(),
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

    public PerfilDto paraPerfilDto(UsuarioPerfilAutorizacaoLeitura atribuicao) {
        return PerfilDto.builder()
                .usuarioTitulo(atribuicao.usuarioTitulo())
                .unidadeCodigo(atribuicao.unidadeCodigo())
                .unidadeNome(atribuicao.unidadeNome())
                .perfil(atribuicao.perfil().name())
                .descricao(atribuicao.perfil().name())
                .build();
    }

    public AdministradorDto paraAdministradorDto(Usuario usuario) {
        Unidade unidadeLotacao = usuario.getUnidadeLotacao();

        return AdministradorDto.builder()
                .tituloEleitoral(usuario.getTituloEleitoral())
                .nome(usuario.getNome())
                .matricula(usuario.getMatricula())
                .unidadeCodigo(unidadeLotacao.getCodigo())
                .unidadeSigla(unidadeLotacao.getSigla())
                .build();
    }

    public AtribuicaoDto paraAtribuicaoDto(AtribuicaoTemporaria atribuicao, Usuario usuario) {
        return AtribuicaoDto.builder()
                .codigo(atribuicao.getCodigo())
                .unidadeCodigo(atribuicao.getUnidade().getCodigo())
                .unidadeSigla(atribuicao.getUnidade().getSigla())
                .usuario(paraUsuarioResumoObrigatorio(usuario))
                .dataInicio(atribuicao.getDataInicio())
                .dataTermino(atribuicao.getDataTermino())
                .justificativa(atribuicao.getJustificativa())
                .build();
    }
}
