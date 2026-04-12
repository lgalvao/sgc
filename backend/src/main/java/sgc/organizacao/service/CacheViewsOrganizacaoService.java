package sgc.organizacao.service;

import lombok.*;
import org.springframework.cache.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import sgc.comum.config.CacheConfig;
import sgc.organizacao.dto.*;
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
    private final UsuarioPerfilRepo usuarioPerfilRepo;

    @Cacheable(cacheNames = CacheConfig.CACHE_VW_UNIDADE, sync = true)
    public List<UnidadeHierarquiaLeitura> listarTodasUnidades() {
        return unidadeRepo.listarEstruturasAtivas();
    }

    @CacheEvict(cacheNames = CacheConfig.CACHE_VW_UNIDADE, allEntries = true)
    public void evictarUnidades() {
        // Apenas para anotação @CacheEvict
    }

    @Cacheable(cacheNames = CacheConfig.CACHE_VW_USUARIO, sync = true)
    public List<UsuarioResumoDto> listarTodosUsuarios() {
        return usuarioRepo.findAll().stream()
                .map(UsuarioResumoDto::fromEntityObrigatorio)
                .toList();
    }

    @CacheEvict(cacheNames = CacheConfig.CACHE_VW_USUARIO, allEntries = true)
    public void evictarUsuarios() {
        // Apenas para anotação @CacheEvict
    }

    @Cacheable(cacheNames = CacheConfig.CACHE_VW_RESPONSABILIDADE, sync = true)
    public List<ResponsabilidadeLeitura> listarTodasResponsabilidades() {
        return responsabilidadeRepo.findAll().stream()
                .map(r -> new ResponsabilidadeLeitura(r.getUnidadeCodigo(), r.getUsuarioTitulo()))
                .toList();
    }

    @CacheEvict(cacheNames = CacheConfig.CACHE_VW_RESPONSABILIDADE, allEntries = true)
    public void evictarResponsabilidades() {
        // Apenas para anotação @CacheEvict
    }

    @Cacheable(cacheNames = CacheConfig.CACHE_VW_USUARIO_PERFIL, sync = true)
    public List<UsuarioPerfil> listarTodosPerfisUnidade() {
        return usuarioPerfilRepo.findAll();
    }

    @CacheEvict(cacheNames = CacheConfig.CACHE_VW_USUARIO_PERFIL, allEntries = true)
    public void evictarPerfisUnidade() {
        // Apenas para anotação @CacheEvict
    }
}
