package sgc.organizacao.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.Mensagens;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.comum.erros.ErroValidacao;
import sgc.organizacao.dto.UsuarioPesquisaDto;
import sgc.organizacao.model.*;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UsuarioService {
    private static final int LIMITE_PESQUISA_USUARIO = 20;

    private final UsuarioRepo usuarioRepo;
    private final AdministradorRepo administradorRepo;
    private final CacheViewsOrganizacaoService cacheViewsOrganizacaoService;
    private final UsuarioPerfilCacheService usuarioPerfilCacheService;
    private final CacheOrganizacaoService cacheOrganizacaoService;

    public Usuario buscar(String titulo) {
        return usuarioRepo.buscarPorTitulo(titulo)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada(Usuario.class.getSimpleName(), titulo));
    }

    public Optional<Usuario> buscarOpt(String titulo) {
        return usuarioRepo.buscarPorTitulo(titulo);
    }

    public Optional<Usuario> buscarOptComUnidadeLotacao(String titulo) {
        return usuarioRepo.buscarPorTituloComUnidadeLotacao(titulo);
    }

    public List<Usuario> buscarPorUnidadeLotacao(Long codUnidade) {
        return usuarioRepo.listarPorCodigoUnidadeLotacao(codUnidade);
    }

    public Optional<UsuarioConsultaLeitura> buscarConsultaPorTitulo(String titulo) {
        return cacheViewsOrganizacaoService.listarTodosUsuarios().stream()
                .filter(usuario -> Objects.equals(usuario.tituloEleitoral(), titulo))
                .findFirst();
    }

    public List<UsuarioConsultaLeitura> buscarConsultasPorUnidadeLotacao(Long codUnidade) {
        return cacheViewsOrganizacaoService.listarTodosUsuarios().stream()
                .filter(usuario -> Objects.equals(usuario.unidadeCodigo(), codUnidade))
                .sorted(Comparator.comparing(UsuarioConsultaLeitura::nome, Comparator.nullsLast(String::compareTo)))
                .toList();
    }

    public List<Usuario> buscarPorTitulos(List<String> titulos) {
        return usuarioRepo.listarPorTitulosComUnidadeLotacao(titulos);
    }

    public List<UsuarioConsultaLeitura> buscarConsultasPorTitulos(Collection<String> titulos) {
        return cacheViewsOrganizacaoService.listarTodosUsuarios().stream()
                .filter(usuario -> titulos.contains(usuario.tituloEleitoral()))
                .toList();
    }


    public List<UsuarioPesquisaDto> pesquisarPorNome(String termo) {
        String termoNormalizado = termo.trim();
        if (termoNormalizado.length() < 2) {
            return List.of();
        }

        String termoBusca = termoNormalizado.toLowerCase(Locale.ROOT);
        return cacheViewsOrganizacaoService.listarTodosUsuarios().stream()
                .filter(usuario -> iniciaCom(usuario.nome(), termoBusca)
                        || iniciaCom(usuario.tituloEleitoral(), termoBusca))
                .sorted(Comparator.comparing(UsuarioConsultaLeitura::nome, Comparator.nullsLast(String::compareTo)))
                .limit(LIMITE_PESQUISA_USUARIO)
                .map(usuario -> new UsuarioPesquisaDto(usuario.tituloEleitoral(), usuario.nome()))
                .toList();
    }

    public List<UsuarioPerfilAutorizacaoLeitura> buscarAutorizacoesPerfil(String usuarioTitulo) {
        return usuarioPerfilCacheService.buscarAutorizacoesPerfil(usuarioTitulo);
    }

    public List<Perfil> buscarPerfisPorUsuarioTitulo(String usuarioTitulo) {
        return buscarAutorizacoesPerfil(usuarioTitulo).stream()
                .map(UsuarioPerfilAutorizacaoLeitura::perfil)
                .distinct()
                .toList();
    }

    public void carregarAuthorities(Usuario usuario) {
        List<Perfil> perfis = buscarPerfisPorUsuarioTitulo(usuario.getTituloEleitoral());

        Set<GrantedAuthority> authorities = perfis.stream()
                .map(Perfil::toGrantedAuthority)
                .collect(Collectors.toSet());

        usuario.setAuthorities(authorities);
    }

    public List<Administrador> buscarAdministradores() {
        return administradorRepo.findAll();
    }

    @Transactional
    public void adicionarAdministrador(String usuarioTitulo) {
        if (isAdministrador(usuarioTitulo)) {
            throw new ErroValidacao(Mensagens.USUARIO_JA_ADMINISTRADOR);
        }
        Administrador administrador = Administrador.builder()
                .usuarioTitulo(usuarioTitulo)
                .build();

        administradorRepo.save(administrador);
        cacheOrganizacaoService.invalidarAposCommit();
    }

    @Transactional
    public void removerAdministrador(String usuarioTitulo) {
        long totalAdministradores = administradorRepo.count();
        if (totalAdministradores <= 1) {
            throw new ErroValidacao(Mensagens.NAO_REMOVER_UNICO_ADMINISTRADOR);
        }
        administradorRepo.deleteById(usuarioTitulo);
        cacheOrganizacaoService.invalidarAposCommit();
    }

    public boolean isAdministrador(String usuarioTitulo) {
        return administradorRepo.existsById(usuarioTitulo);
    }

    private boolean iniciaCom(String valor, String termoBusca) {
        return valor.toLowerCase(Locale.ROOT).startsWith(termoBusca);
    }
}
