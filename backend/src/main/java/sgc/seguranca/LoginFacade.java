package sgc.seguranca;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.erros.ErroAcessoNegado;
import sgc.comum.erros.ErroAutenticacao;
import sgc.organizacao.UnidadeFacade;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.dto.UnidadeDto;
import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.SituacaoUnidade;
import sgc.organizacao.model.Usuario;
import sgc.organizacao.model.UsuarioPerfil;
import sgc.organizacao.service.UsuarioService;
import sgc.seguranca.login.ClienteAcessoAd;
import sgc.seguranca.login.GerenciadorJwt;
import sgc.seguranca.dto.EntrarRequest;
import sgc.seguranca.dto.PerfilUnidadeDto;

import java.util.List;

/**
 * Serviço responsável pelo fluxo de login: autenticação, autorização e geração
 * de token.
 */
@Service
@Slf4j
public class LoginFacade {
    private final UsuarioFacade usuarioService;
    private final GerenciadorJwt gerenciadorJwt;
    private final @Nullable ClienteAcessoAd clienteAcessoAd;
    private final UnidadeFacade unidadeService;
    private final UsuarioService usuarioServiceInterno;

    @Value("${aplicacao.ambiente-testes:true}")
    private boolean ambienteTestes;

    public LoginFacade(UsuarioFacade usuarioService,
                       GerenciadorJwt gerenciadorJwt,
                       @Autowired(required = false) @Nullable ClienteAcessoAd clienteAcessoAd,
                       UnidadeFacade unidadeService,
                       UsuarioService usuarioServiceInterno) {
        this.usuarioService = usuarioService;
        this.gerenciadorJwt = gerenciadorJwt;
        this.clienteAcessoAd = clienteAcessoAd;
        this.unidadeService = unidadeService;
        this.usuarioServiceInterno = usuarioServiceInterno;
    }

    /**
     * Autentica um usuário com título de eleitor e senha.
     */
    public boolean autenticar(String tituloEleitoral, String senha) {
        if (ambienteTestes) {
            log.info("Usuário autenticado: {}", tituloEleitoral);
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
     * Requer autenticação prévia.
     */
    @Transactional(readOnly = true)
    public List<PerfilUnidadeDto> autorizar(String tituloEleitoral) {
        return buscarAutorizacoesInterno(tituloEleitoral);
    }

    /**
     * Finaliza o login gerando um token JWT para o perfil e unidade escolhidos.
     */
    @Transactional(readOnly = true)
    public String entrar(EntrarRequest request) {
        Long codUnidade = request.unidadeCodigo();
        unidadeService.porCodigo(codUnidade);

        List<PerfilUnidadeDto> autorizacoes = buscarAutorizacoesInterno(request.tituloEleitoral());
        Perfil perfilSolicitado = Perfil.valueOf(request.perfil());

        if (perfilSolicitado == Perfil.ADMIN) {
            boolean temPerfilAdmin = autorizacoes.stream().anyMatch(pu -> pu.perfil() == Perfil.ADMIN);
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

        return gerenciadorJwt.gerarToken(
                request.tituloEleitoral(),
                perfilSolicitado,
                codUnidade);
    }

    private List<PerfilUnidadeDto> buscarAutorizacoesInterno(String tituloEleitoral) {
        Usuario usuario = usuarioService.carregarUsuarioParaAutenticacao(tituloEleitoral);
        if (usuario == null) {
            throw new ErroAutenticacao("Credenciais inválidas");
        }

        List<UsuarioPerfil> atribuicoes = usuarioServiceInterno.buscarPerfis(usuario.getTituloEleitoral());
        return atribuicoes.stream()
                .filter(a -> a.getUnidade().getSituacao() == SituacaoUnidade.ATIVA)
                .map(atribuicao -> new PerfilUnidadeDto(
                        atribuicao.getPerfil(),
                        UnidadeDto.fromEntity(atribuicao.getUnidade())))
                .toList();
    }
}
