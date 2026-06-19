package sgc.organizacao.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import sgc.comum.config.CacheConfig;
import sgc.organizacao.model.UnidadeHierarquiaLeitura;
import sgc.organizacao.model.UsuarioPerfilAutorizacaoLeitura;
import sgc.organizacao.model.UsuarioPerfilLeitura;
import sgc.organizacao.model.UsuarioPerfilRepo;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UsuarioPerfilCacheService {

    private final CacheViewsOrganizacaoService cacheViewsOrganizacaoService;
    private final UsuarioPerfilRepo usuarioPerfilRepo;

    @Cacheable(cacheNames = CacheConfig.CACHE_USUARIO_AUTORIZACOES, key = "#usuarioTitulo", sync = true)
    public List<UsuarioPerfilAutorizacaoLeitura> buscarAutorizacoesPerfil(String usuarioTitulo) {
        Map<Long, UnidadeHierarquiaLeitura> unidadesPorCodigo = cacheViewsOrganizacaoService.listarTodasUnidades().stream()
                .collect(java.util.stream.Collectors.toMap(
                        UnidadeHierarquiaLeitura::codigo,
                        unidade -> unidade,
                        (primeira, segunda) -> primeira
                ));

        return usuarioPerfilRepo.findByUsuarioTitulo(usuarioTitulo).stream()
                .map(perfil -> new UsuarioPerfilLeitura(
                        perfil.getUsuarioTitulo(),
                        perfil.getUnidadeCodigo(),
                        perfil.getPerfil()
                ))
                .map(perfil -> {
                    UnidadeHierarquiaLeitura unidade = unidadesPorCodigo.get(perfil.unidadeCodigo());
                    if (unidade == null) {
                        return null;
                    }
                    return paraAutorizacao(perfil, unidade);
                })
                .filter(Objects::nonNull)
                .toList();
    }

    private UsuarioPerfilAutorizacaoLeitura paraAutorizacao(
            UsuarioPerfilLeitura perfil,
            UnidadeHierarquiaLeitura unidade
    ) {
        return new UsuarioPerfilAutorizacaoLeitura(
                perfil.usuarioTitulo(),
                perfil.perfil(),
                perfil.unidadeCodigo(),
                unidade.nome(),
                unidade.sigla(),
                unidade.tipo(),
                unidade.situacao()
        );
    }
}
