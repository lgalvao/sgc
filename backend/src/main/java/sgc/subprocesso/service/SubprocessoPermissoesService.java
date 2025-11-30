package sgc.subprocesso.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sgc.sgrh.model.Perfil;
import sgc.sgrh.model.Usuario;
import sgc.subprocesso.dto.SubprocessoPermissoesDto;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;

@Service
@RequiredArgsConstructor
public class SubprocessoPermissoesService {

        public SubprocessoPermissoesDto calcularPermissoes(Subprocesso subprocesso, Usuario usuario) {
                if (usuario == null || usuario.getTodasAtribuicoes() == null) {
                        return construirPermissoesVazias();
                }

                boolean isAdmin = usuario.getTodasAtribuicoes().stream()
                                .anyMatch(a -> a.getPerfil() == Perfil.ADMIN);

                boolean isGestor = usuario.getTodasAtribuicoes().stream()
                                .anyMatch(a -> a.getPerfil() == Perfil.GESTOR);

                boolean isChefeDaUnidade = usuario.getTodasAtribuicoes().stream()
                                .anyMatch(a -> a.getPerfil() == Perfil.CHEFE &&
                                                a.getUnidade().getCodigo()
                                                                .equals(subprocesso.getUnidade().getCodigo()));

                SituacaoSubprocesso situacao = subprocesso.getSituacao();

                return SubprocessoPermissoesDto.builder()
                                .podeVerPagina(isAdmin || isGestor || isChefeDaUnidade)
                                .podeEditarMapa(
                                                isChefeDaUnidade &&
                                                                (situacao == SituacaoSubprocesso.NAO_INICIADO
                                                                                || situacao == SituacaoSubprocesso.MAPA_ELABORADO
                                                                                || situacao == SituacaoSubprocesso.MAPA_AJUSTADO
                                                                                || situacao == SituacaoSubprocesso.CADASTRO_EM_ANDAMENTO))
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
                                                isAdmin && (situacao == SituacaoSubprocesso.ATIVIDADES_HOMOLOGADAS
                                                                || situacao == SituacaoSubprocesso.MAPA_AJUSTADO))
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
