package sgc.processo.service;

import lombok.*;
import lombok.extern.slf4j.*;
import org.jspecify.annotations.*;
import org.springframework.security.core.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import sgc.organizacao.*;
import sgc.organizacao.dto.*;
import sgc.organizacao.model.*;
import sgc.subprocesso.service.*;

import java.util.*;
import java.util.stream.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProcessoAcessoService {
    private final OrganizacaoFacade organizacaoFacade;
    private final UsuarioFacade usuarioService;
    private final ConsultasSubprocessoService consultas;

    @Transactional(readOnly = true)
    public boolean checarAcesso(@Nullable Authentication authentication, Long codProcesso) {
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName() == null) {
            return false;
        }

        String username = authentication.getName();
        boolean isGestorOuChefe = authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_GESTOR".equals(a.getAuthority()) || "ROLE_CHEFE".equals(a.getAuthority()));
        if (!isGestorOuChefe) return false;

        List<PerfilDto> perfis = usuarioService.buscarPerfisUsuario(username);
        if (perfis.isEmpty()) return false;

        Set<Long> unidadesUsuario = perfis.stream()
                .map(PerfilDto::unidadeCodigo)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (unidadesUsuario.isEmpty()) return false;

        List<Unidade> todasUnidades = organizacaoFacade.unidadesComHierarquia();
        Map<Long, List<Unidade>> mapaPorPai = buildMapaPorPai(todasUnidades);

        Set<Long> todasUnidadesAcesso = new HashSet<>();
        for (Long codUnidade : unidadesUsuario) {
            todasUnidadesAcesso.addAll(buscarDescendentesNoMapa(codUnidade, mapaPorPai));
        }

        return consultas.verificarAcessoUnidadeAoProcesso(codProcesso, new ArrayList<>(todasUnidadesAcesso));
    }

    @Transactional(readOnly = true)
    public List<Long> buscarCodigosDescendentes(Long codUnidade) {
        List<Unidade> todasUnidades = organizacaoFacade.unidadesComHierarquia();
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
