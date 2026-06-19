package sgc.seguranca;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.Mensagens;
import sgc.comum.erros.ErroAcessoNegado;
import sgc.comum.erros.ErroAutenticacao;
import sgc.comum.util.MascaraUtil;
import sgc.organizacao.UsuarioAplicacaoService;
import sgc.organizacao.dto.UnidadeResumoDto;
import sgc.organizacao.model.*;
import sgc.organizacao.service.UnidadeService;
import sgc.organizacao.service.UsuarioService;
import sgc.seguranca.dto.EntrarRequest;
import sgc.seguranca.dto.PerfilUnidadeDto;
import sgc.seguranca.login.ClienteAcessoAd;
import sgc.seguranca.login.GerenciadorJwt;

import java.util.List;

import static sgc.organizacao.model.Perfil.ADMIN;

/**
 * Serviço responsável pelo fluxo de login: autenticação, autorização e geração de tokens.
 */
@Service
@Slf4j
public class LoginAplicacaoService {
    private final @Nullable ClienteAcessoAd clienteAcessoAd;
    private final UsuarioAplicacaoService usuarioAplicacaoService;
    private final UnidadeService unidadeService;
    private final GerenciadorJwt gerenciadorJwt;
    private final UsuarioService usuarioService;

    public LoginAplicacaoService(UsuarioAplicacaoService usuarioAplicacaoService,
                       GerenciadorJwt gerenciadorJwt,
                       @Autowired(required = false) @Nullable ClienteAcessoAd clienteAcessoAd,
                       UnidadeService unidadeService,
                       UsuarioService usuarioService) {

        this.usuarioAplicacaoService = usuarioAplicacaoService;
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
            log.warn("Falha na autenticação do usuário {}: {}", MascaraUtil.mascarar(tituloEleitoral), e.getMessage());
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
        return entrar(request, tituloEleitoral, null);
    }

    @Transactional(readOnly = true)
    public String entrar(
            EntrarRequest request,
            String tituloEleitoral,
            @Nullable List<PerfilUnidadeDto> autorizacoesPreCarregadas) {
        Long codUnidade = request.unidadeCodigo();
        Unidade unidade = unidadeService.buscarPorCodigo(codUnidade);

        List<PerfilUnidadeDto> autorizacoes = autorizacoesPreCarregadas != null
                ? autorizacoesPreCarregadas
                : buscarAutorizacoes(tituloEleitoral);
        Perfil perfilSolicitado = Perfil.valueOf(request.perfil());

        if (perfilSolicitado == ADMIN) {
            boolean temPerfilAdmin = autorizacoes.stream().anyMatch(pu -> ADMIN.name().equals(pu.perfil()));
            if (!temPerfilAdmin) {
                throw new ErroAcessoNegado(Mensagens.SEM_PERMISSAO_ACESSO_PERFIL);
            }
        } else {
            boolean autorizado = autorizacoes.stream()
                    .anyMatch(pu -> {
                        String perfil = pu.perfil();
                        Long codigoUnidade = pu.unidade().codigo();
                        return perfilSolicitado.name().equals(perfil) && codigoUnidade.equals(codUnidade);
                    });
            if (!autorizado) {
                throw new ErroAcessoNegado(Mensagens.SEM_PERMISSAO_ACESSO_PERFIL);
            }
        }

        String siglaUnidade = unidade.getSigla();
        log.info("Usuário {} autorizado: {}-{}", MascaraUtil.mascarar(tituloEleitoral), perfilSolicitado, siglaUnidade);

        return gerenciadorJwt.gerarToken(
                tituloEleitoral,
                perfilSolicitado,
                codUnidade);
    }

    private List<PerfilUnidadeDto> buscarAutorizacoes(String tituloEleitoral) {
        Usuario usuario = usuarioAplicacaoService.carregarUsuarioParaAutenticacao(tituloEleitoral);
        if (usuario == null) {
            throw new ErroAutenticacao(Mensagens.CREDENCIAIS_INVALIDAS);
        }

        List<UsuarioPerfilAutorizacaoLeitura> atribuicoes = usuarioService.buscarAutorizacoesPerfil(usuario.getTituloEleitoral());
        return atribuicoes.stream()
                .filter(a -> a.unidadeSituacao() == SituacaoUnidade.ATIVA)
                .map(atribuicao -> new PerfilUnidadeDto(
                        atribuicao.perfil().name(),
                        toUnidadeResumoObrigatoria(atribuicao)))
                .toList();
    }

    private UnidadeResumoDto toUnidadeResumoObrigatoria(UsuarioPerfilAutorizacaoLeitura atribuicao) {
        return UnidadeResumoDto.fromResumoObrigatorio(
                atribuicao.unidadeCodigo(),
                atribuicao.unidadeNome(),
                atribuicao.unidadeSigla(),
                atribuicao.unidadeTipo() != null ? atribuicao.unidadeTipo().name() : null,
                null
        );
    }
}
