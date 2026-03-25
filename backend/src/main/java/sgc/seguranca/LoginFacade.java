package sgc.seguranca;

import lombok.extern.slf4j.*;
import org.jspecify.annotations.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import sgc.comum.*;
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
    private final UnidadeService unidadeService;
    private final GerenciadorJwt gerenciadorJwt;
    private final UsuarioService usuarioService;

    public LoginFacade(UsuarioFacade usuarioFacade,
                       GerenciadorJwt gerenciadorJwt,
                       @Autowired(required = false) @Nullable ClienteAcessoAd clienteAcessoAd,
                       UnidadeService unidadeService,
                       UsuarioService usuarioService) {

        this.usuarioFacade = usuarioFacade;
        this.gerenciadorJwt = gerenciadorJwt;
        this.clienteAcessoAd = clienteAcessoAd;
        this.unidadeService = unidadeService;
        this.usuarioService = usuarioService;
    }

    /**
     * Autentica um usuário com título de eleitor e senha.
     */
    public boolean autenticar(String tituloEleitoral, String senha) {
        if (clienteAcessoAd == null) {
            log.error("ClienteAcessoAd não configurado em ambiente de produção");
            return false;
        }
        try {
            clienteAcessoAd.autenticar(tituloEleitoral, senha);
            return true;
        } catch (ErroAutenticacao e) {
            log.warn("Falha na autenticação do usuário {}: {}", mascarar(tituloEleitoral), e.getMessage());
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
    public String entrar(EntrarRequest request, String tituloEleitoral) {
        Long codUnidade = request.unidadeCodigo();
        unidadeService.buscarPorCodigo(codUnidade);

        List<PerfilUnidadeDto> autorizacoes = buscarAutorizacoes(tituloEleitoral);
        Perfil perfilSolicitado = Perfil.valueOf(request.perfil());

        if (perfilSolicitado == ADMIN) {
            boolean temPerfilAdmin = autorizacoes.stream().anyMatch(pu -> pu.perfil() == ADMIN);
            if (!temPerfilAdmin) {
                throw new ErroAcessoNegado(Mensagens.SEM_PERMISSAO_ACESSO_PERFIL);
            }
        } else {
            boolean autorizado = autorizacoes.stream()
                    .anyMatch(pu -> {
                        Perfil perfil = pu.perfil();
                        Long codigoUnidade = pu.unidade().getCodigo();
                        return perfil == perfilSolicitado && codigoUnidade.equals(codUnidade);
                    });
            if (!autorizado) {
                throw new ErroAcessoNegado(Mensagens.SEM_PERMISSAO_ACESSO_PERFIL);
            }
        }

        String siglaUnidade = unidadeService.buscarPorCodigo(codUnidade).getSigla();
        log.info("Usuário {} autorizado: {}-{}", mascarar(tituloEleitoral), perfilSolicitado, siglaUnidade);

        return gerenciadorJwt.gerarToken(
                tituloEleitoral,
                perfilSolicitado,
                codUnidade);
    }

    private List<PerfilUnidadeDto> buscarAutorizacoes(String tituloEleitoral) {
        Usuario usuario = usuarioFacade.carregarUsuarioParaAutenticacao(tituloEleitoral);
        if (usuario == null) {
            throw new ErroAutenticacao(Mensagens.CREDENCIAIS_INVALIDAS);
        }

        List<UsuarioPerfil> atribuicoes = usuarioService.buscarPerfis(usuario.getTituloEleitoral());
        return atribuicoes.stream()
                .filter(a -> a.getUnidade().getSituacao() == SituacaoUnidade.ATIVA)
                .map(atribuicao -> new PerfilUnidadeDto(
                        atribuicao.getPerfil(),
                        UnidadeDto.fromEntity(atribuicao.getUnidade())))
                .toList();
    }

    private String mascarar(String valor) {
        if (valor.length() <= 4) return "***";
        return "***" + valor.substring(valor.length() - 4);
    }
}
