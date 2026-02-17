package sgc.processo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.organizacao.UnidadeFacade;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.dto.PerfilDto;
import sgc.organizacao.model.Unidade;
import sgc.subprocesso.service.query.ConsultasSubprocessoService;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Serviço responsável pelo controle de acesso a processos.
 *
 * Implementa a lógica de verificação hierárquica de acesso baseada
 * em unidades organizacionais e perfis de usuário.
 */
@Service
@Slf4j
@RequiredArgsConstructor
class ProcessoAcessoService {
    private final UnidadeFacade unidadeService;
    private final UsuarioFacade usuarioService;
    private final ConsultasSubprocessoService queryService;

    /**
     * Verifica se o usuário autenticado tem acesso ao processo.
     *
     * @param authentication contexto de autenticação do usuário
     * @param codProcesso    código do processo
     * @return true se o usuário tem acesso, false caso contrário
     */
    @Transactional(readOnly = true)
    public boolean checarAcesso(@Nullable Authentication authentication, Long codProcesso) {
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName() == null) {
            return false;
        }

        String username = authentication.getName();
        boolean isGestorOuChefe = authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_GESTOR".equals(a.getAuthority()) || "ROLE_CHEFE".equals(a.getAuthority()));

        if (!isGestorOuChefe) {
            return false;
        }

        List<PerfilDto> perfis = usuarioService.buscarPerfisUsuario(username);
        if (perfis.isEmpty()) {
            return false;
        }

        Set<Long> unidadesUsuario = perfis.stream()
                .map(PerfilDto::unidadeCodigo)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (unidadesUsuario.isEmpty()) {
            return false;
        }

        List<Unidade> todasUnidades = unidadeService.buscarTodasEntidadesComHierarquia();
        Map<Long, List<Unidade>> mapaPorPai = buildMapaPorPai(todasUnidades);

        Set<Long> todasUnidadesAcesso = new HashSet<>();
        for (Long codUnidade : unidadesUsuario) {
            todasUnidadesAcesso.addAll(buscarDescendentesNoMapa(codUnidade, mapaPorPai));
        }

        return queryService.verificarAcessoUnidadeAoProcesso(codProcesso, new ArrayList<>(todasUnidadesAcesso));
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
        Map<Long, List<Unidade>> mapaPorPai = buildMapaPorPai(todasUnidades);
        return new ArrayList<>(buscarDescendentesNoMapa(codUnidade, mapaPorPai));
    }

    private Map<Long, List<Unidade>> buildMapaPorPai(List<Unidade> todasUnidades) {
        Map<Long, List<Unidade>> mapaPorPai = new HashMap<>();
        for (Unidade u : todasUnidades) {
            Unidade unidadeSuperior = u.getUnidadeSuperior();
            if (unidadeSuperior != null) {
                mapaPorPai.computeIfAbsent(unidadeSuperior.getCodigo(), k -> new ArrayList<>()).add(u);
            }
        }
        return mapaPorPai;
    }

    private Set<Long> buscarDescendentesNoMapa(Long codUnidade, Map<Long, List<Unidade>> mapaPorPai) {
        Set<Long> resultado = new HashSet<>();
        Queue<Long> fila = new LinkedList<>();

        fila.add(codUnidade);
        resultado.add(codUnidade);

        while (!fila.isEmpty()) {
            Long atual = fila.poll();

            List<Unidade> filhos = mapaPorPai.get(atual);
            if (filhos != null) {
                for (Unidade filho : filhos) {
                    Long codigo = filho.getCodigo();
                    if (!resultado.contains(codigo)) {
                        resultado.add(codigo);
                        fila.add(codigo);
                    }
                }
            }
        }
        return resultado;
    }
}
