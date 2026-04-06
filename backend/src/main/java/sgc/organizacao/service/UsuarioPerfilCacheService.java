package sgc.organizacao.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import sgc.comum.config.CacheConfig;
import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.UsuarioPerfilAutorizacaoLeitura;
import sgc.organizacao.model.UsuarioPerfilRepo;

import java.util.List;

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
