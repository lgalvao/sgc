package sgc.seguranca.login;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

    private final Map<String, LocalDateTime> autenticacoesRecentes = new ConcurrentHashMap<>();

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
     * Remove autenticações pendentes expiradas a cada minuto.
     * O tempo limite é de 5 minutos.
     */
    @Scheduled(fixedRate = 60000)
    public void limparAutenticacoesExpiradas() {
        LocalDateTime limite = LocalDateTime.now().minusMinutes(5);
        autenticacoesRecentes.entrySet().removeIf(entry -> entry.getValue().isBefore(limite));
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
            autenticacoesRecentes.put(tituloEleitoral, LocalDateTime.now());
            return true;
        }

        if (clienteAcessoAd == null) {
            log.warn("Cliente AD não configurado e ambiente não é de testes.");
            return false;
        }

        boolean autenticado;
        try {
            autenticado = clienteAcessoAd.autenticar(tituloEleitoral, senha);
        } catch (ErroAutenticacao e) {
            log.warn("Falha na autenticação do usuário {}: {}", tituloEleitoral, e.getMessage());
            autenticado = false;
        }

        if (autenticado) {
            autenticacoesRecentes.put(tituloEleitoral, LocalDateTime.now());
        }
        return autenticado;
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
        if (!autenticacoesRecentes.containsKey(tituloEleitoral)) {
            log.warn("Tentativa de autorização sem autenticação prévia para usuário {}", tituloEleitoral);
            throw new ErroAutenticacao("É necessário autenticar-se antes de consultar autorizações.");
        }

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
        LocalDateTime ultimoAcesso = autenticacoesRecentes.get(request.getTituloEleitoral());

        if (ultimoAcesso == null || ultimoAcesso.isBefore(LocalDateTime.now().minusMinutes(5))) {
            log.warn("Tentativa de acesso não autorizada (sem login prévio) para usuário {}",
                    request.getTituloEleitoral());
            throw new ErroAutenticacao("Sessão de login expirada ou inválida. Por favor, autentique-se novamente.");
        }

        Long codUnidade = request.getUnidadeCodigo();
        unidadeService.buscarEntidadePorId(codUnidade);

        List<PerfilUnidadeDto> autorizacoes = buscarAutorizacoesInterno(request.getTituloEleitoral());

        autenticacoesRecentes.remove(request.getTituloEleitoral());

        boolean autorizado = autorizacoes.stream()
                .anyMatch(pu -> {
                    Perfil perfil = pu.getPerfil();
                    Long codigoUnidade = pu.getUnidade().getCodigo();
                    return perfil.name().equals(request.getPerfil()) && codigoUnidade.equals(codUnidade);
                });

        if (!autorizado) {
            throw new ErroAccessoNegado("Usuário não tem permissão para acessar com perfil e unidade informados.");
        }

        return gerenciadorJwt.gerarToken(
                request.getTituloEleitoral(),
                Perfil.valueOf(request.getPerfil()),
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

    // Métodos para testes
    int getAutenticacoesRecentesSize() {
        return autenticacoesRecentes.size();
    }

    void expireAllAuthenticationsForTest() {
        LocalDateTime passado = LocalDateTime.now().minusMinutes(10);
        autenticacoesRecentes.replaceAll((k, v) -> passado);
    }
}
