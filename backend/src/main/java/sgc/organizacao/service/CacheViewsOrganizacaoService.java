package sgc.organizacao.service;

import lombok.*;
import org.springframework.beans.factory.*;
import org.springframework.cache.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import sgc.comum.config.CacheConfig;
import sgc.organizacao.model.*;

import java.util.*;

/**
 * Serviço responsável pelo cache de leitura das views organizacionais.
 *
 * <p>Cada método encapsula uma leitura completa de uma view Oracle:
 * VW_UNIDADE, VW_USUARIO, VW_RESPONSABILIDADE e VW_USUARIO_PERFIL_UNIDADE.
 *
 * <p>Os caches de view são invalidados e recarregados periodicamente
 * pelo {@code AgendadorRefreshCache} e manualmente pelo {@code CacheAdminController}.
 */
@SuppressWarnings("EmptyMethod")
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CacheViewsOrganizacaoService {

    private final UnidadeRepo unidadeRepo;
    private final UsuarioRepo usuarioRepo;
    private final ResponsabilidadeRepo responsabilidadeRepo;
    private final AdministradorRepo administradorRepo;
    private final ObjectProvider<CacheViewsOrganizacaoService> selfProvider;

    @Cacheable(cacheNames = CacheConfig.CACHE_VW_UNIDADE, sync = true)
    public List<UnidadeHierarquiaLeitura> listarTodasUnidades() {
        return List.copyOf(unidadeRepo.listarEstruturasAtivas());
    }

    @CacheEvict(cacheNames = CacheConfig.CACHE_VW_UNIDADE, allEntries = true)
    public void evictarUnidades() {
        // Apenas para anotação @CacheEvict
    }

    @Cacheable(cacheNames = CacheConfig.CACHE_VW_USUARIO, sync = true)
    public List<UsuarioConsultaLeitura> listarTodosUsuarios() {
        return List.copyOf(usuarioRepo.listarTodasConsultas());
    }

    @CacheEvict(cacheNames = CacheConfig.CACHE_VW_USUARIO, allEntries = true)
    public void evictarUsuarios() {
        // Apenas para anotação @CacheEvict
    }

    @Cacheable(cacheNames = CacheConfig.CACHE_VW_RESPONSABILIDADE, sync = true)
    public List<ResponsabilidadeLeitura> listarTodasResponsabilidades() {
        return List.copyOf(responsabilidadeRepo.findAll().stream()
                .filter(Objects::nonNull)
                .map(r -> new ResponsabilidadeLeitura(r.getUnidadeCodigo(), r.getUsuarioTitulo()))
                .toList());
    }

    @CacheEvict(cacheNames = CacheConfig.CACHE_VW_RESPONSABILIDADE, allEntries = true)
    public void evictarResponsabilidades() {
        // Apenas para anotação @CacheEvict
    }

    @Cacheable(cacheNames = CacheConfig.CACHE_VW_USUARIO_PERFIL, sync = true)
    public List<UsuarioPerfilLeitura> listarTodosPerfisUnidade() {
        CacheViewsOrganizacaoService self = self();
        Map<String, UsuarioConsultaLeitura> usuariosPorTitulo = self.listarTodosUsuarios().stream()
                .collect(java.util.stream.Collectors.toMap(
                        UsuarioConsultaLeitura::tituloEleitoral,
                        usuario -> usuario,
                        (primeiro, segundo) -> primeiro
                ));
        Map<Long, UnidadeHierarquiaLeitura> unidadesPorCodigo = self.listarTodasUnidades().stream()
                .collect(java.util.stream.Collectors.toMap(
                        UnidadeHierarquiaLeitura::codigo,
                        unidade -> unidade,
                        (primeira, segunda) -> primeira
                ));

        Set<UsuarioPerfilLeitura> perfis = new LinkedHashSet<>();

        administradorRepo.findAll().stream()
                .map(Administrador::getUsuarioTitulo)
                .filter(usuariosPorTitulo::containsKey)
                .map(titulo -> new UsuarioPerfilLeitura(titulo, 1L, Perfil.ADMIN))
                .forEach(perfis::add);

        self.listarTodasResponsabilidades().forEach(responsabilidade -> {
            UnidadeHierarquiaLeitura unidade = unidadesPorCodigo.get(responsabilidade.unidadeCodigo());
            if (unidade == null) {
                return;
            }
            if (unidade.tipo() == TipoUnidade.INTERMEDIARIA || unidade.tipo() == TipoUnidade.INTEROPERACIONAL) {
                perfis.add(new UsuarioPerfilLeitura(responsabilidade.usuarioTitulo(), responsabilidade.unidadeCodigo(), Perfil.GESTOR));
            }
            if (unidade.tipo() == TipoUnidade.INTEROPERACIONAL || unidade.tipo() == TipoUnidade.OPERACIONAL) {
                perfis.add(new UsuarioPerfilLeitura(responsabilidade.usuarioTitulo(), responsabilidade.unidadeCodigo(), Perfil.CHEFE));
            }
        });

        usuariosPorTitulo.values().forEach(usuario -> {
            Long codigoUnidadeCompetencia = usuario.unidadeCompetenciaCodigo();
            UnidadeHierarquiaLeitura unidadeCompetencia = unidadesPorCodigo.get(codigoUnidadeCompetencia);
            if (unidadeCompetencia != null && !Objects.equals(usuario.tituloEleitoral(), unidadeCompetencia.tituloTitular())) {
                perfis.add(new UsuarioPerfilLeitura(usuario.tituloEleitoral(), codigoUnidadeCompetencia, Perfil.SERVIDOR));
            }
        });

        return List.copyOf(perfis);
    }

    private CacheViewsOrganizacaoService self() {
        return selfProvider.getIfAvailable(() -> this);
    }

    @CacheEvict(cacheNames = CacheConfig.CACHE_VW_USUARIO_PERFIL, allEntries = true)
    public void evictarPerfisUnidade() {
        // Apenas para anotação @CacheEvict
    }
}
