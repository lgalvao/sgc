package sgc.seguranca.login;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.erros.ErroAcessoNegado;
import sgc.comum.erros.ErroAutenticacao;
import sgc.organizacao.UnidadeFacade;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.mapper.UsuarioMapper;
import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.SituacaoUnidade;
import sgc.organizacao.model.Usuario;
import sgc.organizacao.model.UsuarioPerfil;
import sgc.organizacao.service.UsuarioPerfilService;
import sgc.seguranca.login.dto.EntrarRequest;
import sgc.seguranca.login.dto.PerfilUnidadeDto;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Serviço responsável pelo fluxo de login: autenticação, autorização e geração
 * de token.
 */
@Service
@Slf4j
public class LoginFacade {
    private final UsuarioFacade usuarioService;
    private final GerenciadorJwt gerenciadorJwt;
    private final ClienteAcessoAd clienteAcessoAd;
    private final UnidadeFacade unidadeService;
    private final UsuarioMapper usuarioMapper;
    private final UsuarioPerfilService usuarioPerfilService;

    @Value("${aplicacao.ambiente-testes:true}")
    private boolean ambienteTestes;

    public LoginFacade(UsuarioFacade usuarioService,
            GerenciadorJwt gerenciadorJwt,
            @Autowired(required = false) ClienteAcessoAd clienteAcessoAd,
            UnidadeFacade unidadeService,
            UsuarioMapper usuarioMapper,
            UsuarioPerfilService usuarioPerfilService) {
        this.usuarioService = usuarioService;
        this.gerenciadorJwt = gerenciadorJwt;
        this.clienteAcessoAd = clienteAcessoAd;
        this.unidadeService = unidadeService;
        this.usuarioMapper = usuarioMapper;
        this.usuarioPerfilService = usuarioPerfilService;
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
            log.info("Usuário autenticado: {}", tituloEleitoral);
            return true;
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
            throw new ErroAcessoNegado("Usuário não tem permissão para acessar com perfil e unidade informados.");
        }

        return gerenciadorJwt.gerarToken(
                request.tituloEleitoral(),
                Perfil.valueOf(request.perfil()),
                codUnidade);
    }

    private List<PerfilUnidadeDto> buscarAutorizacoesInterno(String tituloEleitoral) {
        Usuario usuario = usuarioService.carregarUsuarioParaAutenticacao(tituloEleitoral);
        if (usuario == null) {
            throw new ErroAutenticacao("Credenciais inválidas");
        }

        Set<UsuarioPerfil> atribuicoes = new HashSet<>(
                usuarioPerfilService.buscarPorUsuario(usuario.getTituloEleitoral())
        );
        return usuario.getTodasAtribuicoes(atribuicoes).stream()
                .filter(a -> a.getUnidade().getSituacao() == SituacaoUnidade.ATIVA)
                .map(atribuicao -> new PerfilUnidadeDto(
                        atribuicao.getPerfil(),
                        usuarioMapper.toUnidadeDtoComElegibilidadeCalculada(atribuicao.getUnidade())))
                .toList();
    }

}
