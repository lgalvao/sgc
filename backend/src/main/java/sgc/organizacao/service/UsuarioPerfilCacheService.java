package sgc.organizacao.service;

import lombok.*;
import org.springframework.cache.annotation.*;
import org.springframework.stereotype.*;
import sgc.comum.config.CacheConfig;
import sgc.organizacao.model.*;

import java.util.*;

@Service
@RequiredArgsConstructor
public class UsuarioPerfilCacheService {

    private final UsuarioPerfilRepo usuarioPerfilRepo;

    @Cacheable(cacheNames = CacheConfig.CACHE_USUARIO_AUTORIZACOES, key = "#usuarioTitulo", sync = true)
    public List<UsuarioPerfilAutorizacaoLeitura> buscarAutorizacoesPerfil(String usuarioTitulo) {
        return usuarioPerfilRepo.listarAutorizacoesPorUsuarioTitulo(usuarioTitulo);
    }

    @Cacheable(cacheNames = CacheConfig.CACHE_USUARIO_PERFIS, key = "#usuarioTitulo", sync = true)
    public List<Perfil> buscarPerfisPorUsuarioTitulo(String usuarioTitulo) {
        return usuarioPerfilRepo.listarPerfisPorUsuarioTitulo(usuarioTitulo);
    }
}
