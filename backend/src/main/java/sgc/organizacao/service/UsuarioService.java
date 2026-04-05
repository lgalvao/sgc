package sgc.organizacao.service;

import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.*;
import org.springframework.security.core.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import sgc.comum.config.CacheConfig;
import sgc.comum.*;
import sgc.comum.erros.*;
import sgc.organizacao.dto.*;
import sgc.organizacao.model.*;

import java.util.*;
import java.util.stream.*;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UsuarioService {
    private static final int LIMITE_PESQUISA_USUARIO = 20;

    private final UsuarioRepo usuarioRepo;
    private final UsuarioPerfilRepo usuarioPerfilRepo;
    private final AdministradorRepo administradorRepo;

    public Usuario buscar(String titulo) {
        return usuarioRepo.buscarPorTitulo(titulo)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada(Usuario.class.getSimpleName(), titulo));
    }

    public Optional<Usuario> buscarOpt(String titulo) {
        return usuarioRepo.buscarPorTitulo(titulo);
    }

    public List<Usuario> buscarPorUnidadeLotacao(Long codUnidade) {
        return usuarioRepo.listarPorCodigoUnidadeLotacao(codUnidade);
    }

    public Optional<UsuarioConsultaLeitura> buscarConsultaPorTitulo(String titulo) {
        return usuarioRepo.buscarConsultaPorTitulo(titulo);
    }

    public List<UsuarioConsultaLeitura> buscarConsultasPorUnidadeLotacao(Long codUnidade) {
        return usuarioRepo.listarConsultasPorCodigoUnidadeLotacao(codUnidade);
    }

    public List<Usuario> buscarPorTitulos(List<String> titulos) {
        return usuarioRepo.listarPorTitulosComUnidadeLotacao(titulos);
    }

    public List<UsuarioPesquisaDto> pesquisarPorNome(String termo) {
        String termoNormalizado = termo.trim();
        if (termoNormalizado.length() < 2) {
            return List.of();
        }

        return usuarioRepo.pesquisarPorNome(
                termoNormalizado,
                PageRequest.of(0, LIMITE_PESQUISA_USUARIO)
        );
    }

    public List<UsuarioPerfil> buscarPerfis(String usuarioTitulo) {
        return usuarioPerfilRepo.findByUsuarioTitulo(usuarioTitulo);
    }

    public List<UsuarioPerfilAutorizacaoLeitura> buscarAutorizacoesPerfil(String usuarioTitulo) {
        return usuarioPerfilRepo.listarAutorizacoesPorUsuarioTitulo(usuarioTitulo);
    }

    public void carregarAuthorities(Usuario usuario) {
        List<Perfil> perfis = usuarioPerfilRepo.listarPerfisPorUsuarioTitulo(usuario.getTituloEleitoral());

        Set<GrantedAuthority> authorities = perfis.stream()
                .map(Perfil::toGrantedAuthority)
                .collect(Collectors.toSet());

        usuario.setAuthorities(authorities);
    }

    public List<Administrador> buscarAdministradores() {
        return administradorRepo.findAll();
    }

    @Transactional
    @CacheEvict(cacheNames = CacheConfig.CACHE_DIAGNOSTICO_ORGANIZACIONAL, allEntries = true)
    public void adicionarAdministrador(String usuarioTitulo) {
        if (isAdministrador(usuarioTitulo)) {
            throw new ErroValidacao(Mensagens.USUARIO_JA_ADMINISTRADOR);
        }
        Administrador administrador = Administrador.builder()
                .usuarioTitulo(usuarioTitulo)
                .build();

        administradorRepo.save(administrador);
    }

    @Transactional
    @CacheEvict(cacheNames = CacheConfig.CACHE_DIAGNOSTICO_ORGANIZACIONAL, allEntries = true)
    public void removerAdministrador(String usuarioTitulo) {
        long totalAdministradores = administradorRepo.count();
        if (totalAdministradores <= 1) {
            throw new ErroValidacao(Mensagens.NAO_REMOVER_UNICO_ADMINISTRADOR);
        }
        administradorRepo.deleteById(usuarioTitulo);
    }

    public boolean isAdministrador(String usuarioTitulo) {
        return administradorRepo.existsById(usuarioTitulo);
    }
}
