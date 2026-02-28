package sgc.seguranca;

import lombok.extern.slf4j.*;
import org.jspecify.annotations.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import sgc.comum.erros.*;
import sgc.organizacao.*;
import sgc.organizacao.dto.*;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;
import sgc.seguranca.dto.*;
import sgc.seguranca.login.*;

import java.util.*;

import static sgc.organizacao.model.Perfil.*;

/**
 * Serviço responsável pelo fluxo de login: autenticação, autorização e geração de tokens.
 */
@Service
@Slf4j
public class LoginFacade {
    private final @Nullable ClienteAcessoAd clienteAcessoAd;

    private final UsuarioFacade usuarioFacade;
    private final OrganizacaoFacade organizacaoFacade;
    private final GerenciadorJwt gerenciadorJwt;
    private final UsuarioService usuarioService;

    @Value("${aplicacao.ambiente-testes:true}")
    private boolean ambienteTestes;

    public LoginFacade(UsuarioFacade usuarioFacade,
                       GerenciadorJwt gerenciadorJwt,
                       @Autowired(required = false) @Nullable ClienteAcessoAd clienteAcessoAd,
                       OrganizacaoFacade organizacaoFacade,
                       UsuarioService usuarioService) {

        this.usuarioFacade = usuarioFacade;
        this.gerenciadorJwt = gerenciadorJwt;
        this.clienteAcessoAd = clienteAcessoAd;
        this.organizacaoFacade = organizacaoFacade;
        this.usuarioService = usuarioService;
    }

    /**
     * Autentica um usuário com título de eleitor e senha.
     */
    public boolean autenticar(String tituloEleitoral, String senha) {
        if (ambienteTestes) {
            log.debug("Usuário autenticado: {}", tituloEleitoral);
            return true;
        }
        if (clienteAcessoAd == null) {
            log.error("ClienteAcessoAd não configurado em ambiente de produção");
            return false;
        }
        try {
            return clienteAcessoAd.autenticar(tituloEleitoral, senha);
        } catch (ErroAutenticacao e) {
            log.warn("Falha na autenticação do usuário {}: {}", tituloEleitoral, e.getMessage());
            return false;
        }
    }

    /**
     * Retorna os perfis e unidades que o usuário pode acessar.
     */
    @Transactional(readOnly = true)
    public List<PerfilUnidadeDto> buscarAutorizacoesUsuario(String tituloEleitoral) {
        return buscarAutorizacoes(tituloEleitoral);
    }

    /**
     * Finaliza o login gerando um token JWT para o perfil e unidade escolhidos.
     */
    @Transactional(readOnly = true)
    public String entrar(EntrarRequest request) {
        Long codUnidade = request.unidadeCodigo();
        organizacaoFacade.unidadePorCodigo(codUnidade);

        String tituloEleitoral = request.tituloEleitoral();
        List<PerfilUnidadeDto> autorizacoes = buscarAutorizacoes(tituloEleitoral);
        Perfil perfilSolicitado = Perfil.valueOf(request.perfil());

        if (perfilSolicitado == ADMIN) {
            boolean temPerfilAdmin = autorizacoes.stream().anyMatch(pu -> pu.perfil() == ADMIN);
            if (!temPerfilAdmin) {
                throw new ErroAcessoNegado("Usuário não tem permissão para acessar com perfil e unidade informados.");
            }
        } else {
            boolean autorizado = autorizacoes.stream()
                    .anyMatch(pu -> {
                        Perfil perfil = pu.perfil();
                        Long codigoUnidade = pu.unidade().getCodigo();
                        return perfil == perfilSolicitado && codigoUnidade.equals(codUnidade);
                    });
            if (!autorizado) {
                throw new ErroAcessoNegado("Usuário não tem permissão para acessar com perfil e unidade informados.");
            }
        }

        String siglaUnidade = organizacaoFacade.unidadePorCodigo(codUnidade).getSigla();
        log.info("Usuário {} autorizado: {}-{}", tituloEleitoral, perfilSolicitado, siglaUnidade);

        return gerenciadorJwt.gerarToken(
                tituloEleitoral,
                perfilSolicitado,
                codUnidade);
    }

    private List<PerfilUnidadeDto> buscarAutorizacoes(String tituloEleitoral) {
        Usuario usuario = usuarioFacade.carregarUsuarioParaAutenticacao(tituloEleitoral);
        if (usuario == null) {
            throw new ErroAutenticacao("Credenciais inválidas");
        }

        List<UsuarioPerfil> atribuicoes = usuarioService.buscarPerfis(usuario.getTituloEleitoral());
        return atribuicoes.stream()
                .filter(a -> a.getUnidade().getSituacao() == SituacaoUnidade.ATIVA)
                .map(atribuicao -> new PerfilUnidadeDto(
                        atribuicao.getPerfil(),
                        UnidadeDto.fromEntity(atribuicao.getUnidade())))
                .toList();
    }
}
