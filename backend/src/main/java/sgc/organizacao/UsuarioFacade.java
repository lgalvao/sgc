package sgc.organizacao;

import lombok.*;
import lombok.extern.slf4j.*;
import org.jspecify.annotations.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.*;
import org.springframework.security.core.context.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import sgc.comum.erros.*;
import sgc.organizacao.dto.*;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;

import java.util.*;

import static java.util.stream.Collectors.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class UsuarioFacade {
    private final UsuarioService usuarioService;
    private final ResponsavelUnidadeService responsavelUnidadeService;

    @Transactional(readOnly = true)
    public @Nullable Usuario carregarUsuarioParaAutenticacao(String titulo) {
        Usuario usuario = usuarioService.buscarComAtribuicoesOpt(titulo).orElse(null);
        if (usuario != null) {
            carregarAtribuicoes(usuario);
        }
        return usuario;
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorLogin(String login) {
        Usuario usuario = usuarioService.buscarComAtribuicoes(login);
        carregarAtribuicoes(usuario);
        return usuario;
    }

    private Optional<String> tituloUsuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }
        return Optional.of(authentication.getName());
    }

    @Transactional(readOnly = true)
    public Usuario usuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Usuario usuario) {
            return usuario;
        }
        return tituloUsuarioAutenticado()
                .map(login -> {
                    Usuario usuario = usuarioService.buscarComAtribuicoes(login);
                    carregarAtribuicoes(usuario);
                    return usuario;
                })
                .orElseThrow(() -> new ErroAcessoNegado("Nenhum usuário autenticado no contexto"));
    }

    @Transactional(readOnly = true)
    public Usuario buscarResponsavelAtual(String sigla) {
        return responsavelUnidadeService.buscarResponsavelAtual(sigla);
    }

    @Transactional(readOnly = true)
    public ResponsavelDto buscarResponsabilidadeDetalhadaAtual(String sigla) {
        return responsavelUnidadeService.buscarResponsabilidadeDetalhadaAtual(sigla);
    }

    @Transactional(readOnly = true)
    public List<PerfilDto> buscarPerfisUsuario(String titulo) {
        return usuarioService.buscarComAtribuicoesOpt(titulo)
                .map(usuario -> {
                    List<UsuarioPerfil> atribuicoes = usuarioService.buscarPerfis(usuario.getTituloEleitoral());
                    return atribuicoes.stream()
                            .filter(a -> a.getUnidade().getSituacao() == SituacaoUnidade.ATIVA)
                            .map(this::toPerfilDto)
                            .toList();
                })
                .orElse(Collections.emptyList());
    }

    private void carregarAtribuicoes(Usuario usuario) {
        usuarioService.carregarAuthorities(usuario);
    }

    public Map<String, Usuario> buscarUsuariosPorTitulos(List<String> titulos) {
        return usuarioService.buscarPorTitulos(titulos).stream()
                .collect(toMap(Usuario::getTituloEleitoral, u -> u, (u1, u2) -> u1));
    }


    private PerfilDto toPerfilDto(UsuarioPerfil atribuicao) {
        return PerfilDto.builder()
                .usuarioTitulo(atribuicao.getUsuario().getTituloEleitoral())
                .unidadeCodigo(atribuicao.getUnidade().getCodigo())
                .unidadeNome(atribuicao.getUnidade().getNome())
                .perfil(atribuicao.getPerfil().name())
                .build();
    }

    @Transactional(readOnly = true)
    public List<AdministradorDto> listarAdministradores() {
        return usuarioService.buscarAdministradores().stream()
                .flatMap(admin -> usuarioService.buscarOpt(admin.getUsuarioTitulo())
                        .map(this::toAdministradorDto)
                        .stream())
                .toList();
    }

    @Transactional
    public AdministradorDto adicionarAdministrador(String usuarioTitulo) {
        Usuario usuario = usuarioService.buscar(usuarioTitulo);

        usuarioService.adicionarAdministrador(usuarioTitulo);

        log.info("Administrador {} adicionado", usuarioTitulo);
        return toAdministradorDto(usuario);
    }

    @Transactional
    public void removerAdministrador(String usuarioTitulo, String usuarioAtualTitulo) {
        if (usuarioTitulo.equals(usuarioAtualTitulo)) {
            throw new ErroValidacao("Não é permitido remover a si mesmo como administrador");
        }

        usuarioService.removerAdministrador(usuarioTitulo);
        log.info("Administrador {} removido com sucesso", usuarioTitulo);
    }

    private AdministradorDto toAdministradorDto(Usuario usuario) {
        Unidade unidadeLotacao = usuario.getUnidadeLotacao();

        return AdministradorDto.builder()
                .tituloEleitoral(usuario.getTituloEleitoral())
                .nome(usuario.getNome())
                .matricula(usuario.getMatricula())
                .unidadeCodigo(unidadeLotacao.getCodigo())
                .unidadeSigla(unidadeLotacao.getSigla())
                .build();
    }
}
