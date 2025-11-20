package sgc.subprocesso.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sgc.sgrh.model.Perfil;
import sgc.sgrh.model.Usuario;
import sgc.subprocesso.dto.SubprocessoPermissoesDto;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class SubprocessoPermissoesService {

    public SubprocessoPermissoesDto calcularPermissoes(Subprocesso subprocesso, Usuario usuario) {
        if (usuario == null || usuario.getPerfis() == null) {
            return construirPermissoesVazias();
        }

        boolean isAdmin = usuario.getPerfis().contains(Perfil.ADMIN);
        boolean isGestor = usuario.getPerfis().contains(Perfil.GESTOR);
        boolean isChefe = usuario.getPerfis().contains(Perfil.CHEFE);

        boolean isChefeDaUnidade = isChefe && Objects.equals(usuario.getUnidade().getCodigo(), subprocesso.getUnidade().getCodigo());

        SituacaoSubprocesso situacao = subprocesso.getSituacao();

        return SubprocessoPermissoesDto.builder()
            .podeVerPagina(isAdmin || isGestor || isChefeDaUnidade)
            .podeEditarMapa(
                isChefeDaUnidade &&
                    (situacao == SituacaoSubprocesso.MAPA_ELABORADO || situacao == SituacaoSubprocesso.MAPA_AJUSTADO))
            .podeVisualizarMapa(true)
            .podeDisponibilizarCadastro(
                isChefeDaUnidade && situacao == SituacaoSubprocesso.MAPA_ELABORADO)
            .podeDevolverCadastro(
                isGestor && situacao == SituacaoSubprocesso.MAPA_VALIDADO)
            .podeAceitarCadastro(
                isGestor && situacao == SituacaoSubprocesso.MAPA_VALIDADO)
            .podeVisualizarDiagnostico(
                isAdmin || isGestor || isChefeDaUnidade)
            .podeAlterarDataLimite(
                isAdmin || (isGestor && subprocesso.isEmAndamento()))
            .podeVisualizarImpacto(
                isAdmin && (situacao == SituacaoSubprocesso.ATIVIDADES_HOMOLOGADAS || situacao == SituacaoSubprocesso.MAPA_AJUSTADO))
            .build();
    }

    private SubprocessoPermissoesDto construirPermissoesVazias() {
        return SubprocessoPermissoesDto.builder()
            .podeVerPagina(false)
            .podeEditarMapa(false)
            .podeVisualizarMapa(false)
            .podeDisponibilizarCadastro(false)
            .podeDevolverCadastro(false)
            .podeAceitarCadastro(false)
            .podeVisualizarDiagnostico(false)
            .podeAlterarDataLimite(false)
            .podeVisualizarImpacto(false)
            .build();
    }
}
