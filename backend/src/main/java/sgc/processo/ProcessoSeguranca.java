package sgc.processo;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import sgc.sgrh.SgrhService;
import sgc.subprocesso.modelo.SubprocessoRepo;
import sgc.unidade.modelo.Unidade;
import sgc.unidade.modelo.UnidadeRepo;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProcessoSeguranca {
    private final SubprocessoRepo subprocessoRepo;
    private final SgrhService sgrhService;
    private final UnidadeRepo unidadeRepo;

    /**
     * Verifica se o usuário autenticado tem permissão para acessar os detalhes de um processo.
     * <p>
     * A regra de acesso é a seguinte:
     * <ul>
     *     <li>O usuário deve estar autenticado.</li>
     *     <li>O usuário deve possuir o perfil 'GESTOR' ou 'CHEFE'.</li>
     *     <li>Pelo menos uma das unidades do usuário (direta ou subordinada)
     *         deve ser participante do processo (ou seja, deve existir um
     *         subprocesso vinculando a unidade ao processo).</li>
     * </ul>
     *
     * @param authentication O objeto de autenticação do Spring Security.
     * @param idProcesso     O ID do processo.
     * @return {@code true} se o acesso for permitido.
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

        List<sgc.sgrh.dto.PerfilDto> perfis = sgrhService.buscarPerfisUsuario(username);
        Long idUnidadePrincipal = perfis.stream()
                .findFirst()
                .map(sgc.sgrh.dto.PerfilDto::unidadeCodigo)
                .orElse(null);

        if (idUnidadePrincipal == null) {
            return false;
        }

        List<Long> unidadesAcessiveisIds = getUnidadesSubordinadas(idUnidadePrincipal)
            .stream()
            .map(Unidade::getCodigo)
            .collect(Collectors.toList());
        unidadesAcessiveisIds.add(idUnidadePrincipal);

        return subprocessoRepo.existsByProcessoCodigoAndUnidadeCodigoIn(idProcesso, unidadesAcessiveisIds);
    }

    private List<Unidade> getUnidadesSubordinadas(Long unidadeId) {
        List<Unidade> subordinadas = new ArrayList<>();
        List<Unidade> diretas = unidadeRepo.findByUnidadeSuperiorCodigo(unidadeId);
        subordinadas.addAll(diretas);
        for (Unidade u : diretas) {
            subordinadas.addAll(getUnidadesSubordinadas(u.getCodigo()));
        }
        return subordinadas;
    }
}