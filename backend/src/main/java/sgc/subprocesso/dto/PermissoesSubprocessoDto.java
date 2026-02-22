package sgc.subprocesso.dto;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Builder;
import sgc.subprocesso.model.SubprocessoViews;

/**
 * DTO que consolida as permissões de UI para um subprocesso,
 * calculado pelo backend com base no perfil do usuário e situação do workflow.
 */
@Builder
public record PermissoesSubprocessoDto(
    @JsonView(SubprocessoViews.Publica.class) boolean podeEditarCadastro,
    @JsonView(SubprocessoViews.Publica.class) boolean podeDisponibilizarCadastro,
    @JsonView(SubprocessoViews.Publica.class) boolean podeDevolverCadastro,
    @JsonView(SubprocessoViews.Publica.class) boolean podeAceitarCadastro,
    @JsonView(SubprocessoViews.Publica.class) boolean podeHomologarCadastro,
    
    @JsonView(SubprocessoViews.Publica.class) boolean podeEditarMapa,
    @JsonView(SubprocessoViews.Publica.class) boolean podeDisponibilizarMapa,
    @JsonView(SubprocessoViews.Publica.class) boolean podeValidarMapa,
    @JsonView(SubprocessoViews.Publica.class) boolean podeApresentarSugestoes,
    @JsonView(SubprocessoViews.Publica.class) boolean podeDevolverMapa,
    @JsonView(SubprocessoViews.Publica.class) boolean podeAceitarMapa,
    @JsonView(SubprocessoViews.Publica.class) boolean podeHomologarMapa,
    
    @JsonView(SubprocessoViews.Publica.class) boolean podeVisualizarImpacto,
    
    @JsonView(SubprocessoViews.Publica.class) boolean podeAlterarDataLimite,
    @JsonView(SubprocessoViews.Publica.class) boolean podeReabrirCadastro,
    @JsonView(SubprocessoViews.Publica.class) boolean podeReabrirRevisao,
    @JsonView(SubprocessoViews.Publica.class) boolean podeEnviarLembrete
) {}
