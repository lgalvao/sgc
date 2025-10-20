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
     * Verifica se o usuário autenticado tem permissão para acessar os detalhes de um processo.
     * <p>
     * A regra de acesso é a seguinte:
     * <ul>
     *     <li>O usuário deve estar autenticado.</li>
     *     <li>O usuário deve possuir o perfil 'GESTOR' ou 'CHEFE'.</li>
     *     <li>Pelo menos uma das unidades associadas ao usuário (via SGRH) deve ser
     *         participante do processo (ou seja, deve existir um subprocesso
     *         vinculando a unidade ao processo).</li>
     * </ul>
     *
     * @param authentication O objeto de autenticação do Spring Security, contendo os
     *                       detalhes do usuário logado.
     * @param idProcesso     O ID do processo cujo acesso está sendo verificado.
     * @return {@code true} se o acesso for permitido, {@code false} caso contrário.
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