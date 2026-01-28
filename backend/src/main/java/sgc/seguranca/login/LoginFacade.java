package sgc.seguranca.login;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.erros.ErroAccessoNegado;
import sgc.comum.erros.ErroAutenticacao;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.organizacao.UnidadeFacade;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.Usuario;
import sgc.seguranca.login.dto.EntrarRequest;
import sgc.seguranca.login.dto.PerfilUnidadeDto;

import java.util.List;

/**
 * Serviço responsável pelo fluxo de login: autenticação, autorização e geração
 * de token.
 */
@Service
@Slf4j
public class LoginFacade {
    private static final String ENTIDADE_USUARIO = "Usuário";

    private final UsuarioFacade usuarioService;
    private final GerenciadorJwt gerenciadorJwt;
    private final ClienteAcessoAd clienteAcessoAd;
    private final UnidadeFacade unidadeService;
    private final sgc.organizacao.mapper.UsuarioMapper usuarioMapper;

    @Value("${aplicacao.ambiente-testes:false}")
    private boolean ambienteTestes;

    public LoginFacade(UsuarioFacade usuarioService,
            GerenciadorJwt gerenciadorJwt,
            @Autowired(required = false) ClienteAcessoAd clienteAcessoAd,
            UnidadeFacade unidadeService,
            sgc.organizacao.mapper.UsuarioMapper usuarioMapper) {
        this.usuarioService = usuarioService;
        this.gerenciadorJwt = gerenciadorJwt;
        this.clienteAcessoAd = clienteAcessoAd;
        this.unidadeService = unidadeService;
        this.usuarioMapper = usuarioMapper;
    }

    /**
     * Autentica um usuário com título de eleitor e senha.
     *
     * @param tituloEleitoral Título de eleitor do usuário
     * @param senha           Senha do usuário
     * @return true se a autenticação for bem-sucedida
     */
    public boolean autenticar(String tituloEleitoral, String senha) {
        if (ambienteTestes) {
            log.info("Autenticação em ambiente de testes/homologação para o usuário {}", tituloEleitoral);
            return true;
        }

        if (clienteAcessoAd == null) {
            log.warn("Cliente AD não configurado e ambiente não é de testes.");
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
     *
     * @param tituloEleitoral Título de eleitor do usuário
     * @return Lista de perfis e unidades disponíveis
     */
    @Transactional(readOnly = true)
    public List<PerfilUnidadeDto> autorizar(String tituloEleitoral) {
        return buscarAutorizacoesInterno(tituloEleitoral);
    }

    /**
     * Finaliza o login gerando um token JWT para o perfil e unidade escolhidos.
     *
     * @param request Dados da requisição de entrada
     * @return Token JWT
     */
    @Transactional(readOnly = true)
    public String entrar(EntrarRequest request) {
        Long codUnidade = request.unidadeCodigo();
        unidadeService.buscarEntidadePorId(codUnidade);

        List<PerfilUnidadeDto> autorizacoes = buscarAutorizacoesInterno(request.tituloEleitoral());

        boolean autorizado = autorizacoes.stream()
                .anyMatch(pu -> {
                    Perfil perfil = pu.perfil();
                    Long codigoUnidade = pu.unidade().getCodigo();
                    return perfil.name().equals(request.perfil()) && codigoUnidade.equals(codUnidade);
                });

        if (!autorizado) {
            throw new ErroAccessoNegado("Usuário não tem permissão para acessar com perfil e unidade informados.");
        }

        return gerenciadorJwt.gerarToken(
                request.tituloEleitoral(),
                Perfil.valueOf(request.perfil()),
                codUnidade);
    }

    private List<PerfilUnidadeDto> buscarAutorizacoesInterno(String tituloEleitoral) {
        Usuario usuario = usuarioService.carregarUsuarioParaAutenticacao(tituloEleitoral);
        if (usuario == null) {
            throw new ErroEntidadeNaoEncontrada(ENTIDADE_USUARIO, tituloEleitoral);
        }

        return usuario.getTodasAtribuicoes().stream()
                .filter(a -> a.getUnidade().getSituacao() == sgc.organizacao.model.SituacaoUnidade.ATIVA)
                .map(atribuicao -> new PerfilUnidadeDto(
                        atribuicao.getPerfil(),
                        usuarioMapper.toUnidadeDtoComElegibilidadeCalculada(atribuicao.getUnidade())))
                .toList();
    }

}
