package sgc.processo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.organizacao.UnidadeService;
import sgc.organizacao.UsuarioService;
import sgc.organizacao.dto.PerfilDto;
import sgc.organizacao.model.Unidade;
import sgc.subprocesso.service.SubprocessoFacade;

import java.util.*;

/**
 * Serviço responsável pelo controle de acesso a processos.
 * 
 * <p>Implementa a lógica de verificação hierárquica de acesso baseada
 * em unidades organizacionais e perfis de usuário.</p>
 * 
 * <p><b>Nota sobre Injeção de Dependências:</b>
 * SubprocessoFacade é injetado com @Lazy para quebrar dependência circular:
 * ProcessoFacade → ProcessoAcessoService → SubprocessoFacade → ... → ProcessoFacade
 */
@Service
@Slf4j
class ProcessoAcessoService {
    
    private final UnidadeService unidadeService;
    private final UsuarioService usuarioService;
    private final SubprocessoFacade subprocessoFacade;

    /**
     * Constructor com @Lazy para quebrar dependência circular.
     */
    public ProcessoAcessoService(
            UnidadeService unidadeService,
            UsuarioService usuarioService,
            @Lazy SubprocessoFacade subprocessoFacade) {
        this.unidadeService = unidadeService;
        this.usuarioService = usuarioService;
        this.subprocessoFacade = subprocessoFacade;
    }

    /**
     * Verifica se o usuário autenticado tem acesso ao processo.
     * 
     * @param authentication contexto de autenticação do usuário
     * @param codProcesso código do processo
     * @return true se o usuário tem acesso, false caso contrário
     */
    @Transactional(readOnly = true)
    public boolean checarAcesso(Authentication authentication, Long codProcesso) {
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName() == null) {
            return false;
        }

        String username = authentication.getName();
        boolean isGestorOuChefe = authentication.getAuthorities().stream()
                .anyMatch(
                        a -> "ROLE_GESTOR".equals(a.getAuthority())
                                || "ROLE_CHEFE".equals(a.getAuthority()));

        if (!isGestorOuChefe) {
            return false;
        }

        List<PerfilDto> perfis = usuarioService.buscarPerfisUsuario(username);
        Long codUnidadeUsuario = perfis.stream()
                .findFirst()
                .map(PerfilDto::getUnidadeCodigo)
                .orElse(null);

        if (codUnidadeUsuario == null) {
            return false;
        }

        List<Long> codigosUnidadesHierarquia = buscarCodigosDescendentes(codUnidadeUsuario);

        return subprocessoFacade.verificarAcessoUnidadeAoProcesso(
                codProcesso, codigosUnidadesHierarquia);
    }

    /**
     * Busca todos os códigos de unidades descendentes (hierarquia completa).
     * 
     * @param codUnidade código da unidade raiz
     * @return lista de códigos incluindo a unidade raiz e todos os descendentes
     */
    @Transactional(readOnly = true)
    public List<Long> buscarCodigosDescendentes(Long codUnidade) {
        List<Unidade> todasUnidades = unidadeService.buscarTodasEntidadesComHierarquia();

        Map<Long, List<Unidade>> mapaPorPai = new HashMap<>();
        for (Unidade u : todasUnidades) {
            if (u.getUnidadeSuperior() != null) {
                mapaPorPai.computeIfAbsent(u.getUnidadeSuperior().getCodigo(), k -> new ArrayList<>()).add(u);
            }
        }

        List<Long> resultado = new ArrayList<>();
        Queue<Long> fila = new LinkedList<>();
        Set<Long> visitados = new HashSet<>();

        fila.add(codUnidade);
        visitados.add(codUnidade);

        while (!fila.isEmpty()) {
            Long atual = fila.poll();
            resultado.add(atual);

            List<Unidade> filhos = mapaPorPai.get(atual);
            if (filhos != null) {
                for (Unidade filho : filhos) {
                    if (!visitados.contains(filho.getCodigo())) {
                        visitados.add(filho.getCodigo());
                        fila.add(filho.getCodigo());
                    }
                }
            }
        }

        return resultado;
    }
}
