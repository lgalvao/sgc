package sgc.processo;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import sgc.sgrh.SgrhService;
import sgc.subprocesso.modelo.SubprocessoRepo;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ProcessoSeguranca {
    private final SubprocessoRepo subprocessoRepo;
    private final SgrhService sgrhService;

    /**
     * Verifica se o usuário autenticado tem permissão para acessar um processo.
     * A permissão é concedida se o usuário for um GESTOR de uma unidade que participa do processo.
     *
     * @param authentication O objeto de autenticação do Spring Security.
     * @param idProcesso     O ID do processo a ser verificado.
     * @return a verificação de acesso do usuário ao processo.
     */
    public boolean checarAcesso(Authentication authentication, Long idProcesso) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        String username = authentication.getName();
        boolean isGestorOuChefe = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_GESTOR") || a.getAuthority().equals("ROLE_CHEFE"));

        if (!isGestorOuChefe) {
            return false;
        }

        // Para gestores, verifica se a unidade dele participa do processo.
        List<sgc.sgrh.dto.PerfilDto> perfis = sgrhService.buscarPerfisUsuario(username);
        Long idUnidadeUsuario = perfis.stream()
                .findFirst()
                .map(sgc.sgrh.dto.PerfilDto::unidadeCodigo)
                .orElse(null);

        if (idUnidadeUsuario == null) {
            return false;
        }

        return subprocessoRepo.existsByProcessoCodigoAndUnidadeCodigo(idProcesso, idUnidadeUsuario);
    }
}