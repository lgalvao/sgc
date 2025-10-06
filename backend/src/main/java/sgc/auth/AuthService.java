package sgc.auth;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import sgc.auth.dto.LoginRequest;
import sgc.auth.dto.LoginResponse;
import sgc.auth.dto.PerfilDto;
import sgc.auth.dto.ServidorDto;
import sgc.comum.Usuario;
import sgc.comum.erros.ErroCredenciaisInvalidas;
import sgc.sgrh.service.SgrhService;
import sgc.sgrh.dto.UsuarioDto;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Serviço de autenticação responsável pela lógica de negócio do login.
 * Integra autenticação AD, busca de perfis e geração de tokens JWT.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final SgrhService sgrhService;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Autentica um usuário e retorna o token JWT com seus perfis.
     *
     * @param request Requisição de login contendo título e senha
     * @return LoginResponse com token, perfis e dados do servidor
     * @throws ErroCredenciaisInvalidas se as credenciais forem inválidas
     */
    public LoginResponse authenticate(LoginRequest request) {
        try {
            log.info("Iniciando autenticação para usuário: {}", request.titulo());

            // 1. Autentica via AD usando o CustomAuthenticationProvider
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.titulo(), request.senha())
            );

            log.debug("Autenticação AD bem-sucedida para: {}", request.titulo());

            // 2. Busca perfis do usuário via SGRH
            List<PerfilDto> perfis = buscarPerfisUsuario(request.titulo());

            // 3. Busca dados do servidor via SGRH
            ServidorDto servidor = buscarDadosServidor(request.titulo());

            // 4. Gera token JWT
            String token = jwtService.generateToken(request.titulo(), perfis);

            log.info("Login bem-sucedido para usuário: {} com {} perfis", request.titulo(), perfis.size());

            return new LoginResponse(token, perfis, servidor);

        } catch (org.springframework.security.core.AuthenticationException e) {
            log.warn("Falha na autenticação para usuário: {}", request.titulo());
            throw new ErroCredenciaisInvalidas("Usuário ou senha inválidos");
        }
    }

    /**
     * Busca perfis do usuário autenticado via SGRH.
     * <p>
     * Integra com o SgrhService para buscar perfis nas views do Oracle.
     * Atualmente usa dados MOCK do SgrhService até a conexão real estar disponível.
     *
     * @param titulo Título do usuário
     * @return Lista de perfis do usuário
     */
    private List<PerfilDto> buscarPerfisUsuario(String titulo) {
        log.debug("Buscando perfis para usuário via SGRH: {}", titulo);

        // Busca perfis via SgrhService
        List<sgc.sgrh.dto.PerfilDto> perfisSgrh = sgrhService.buscarPerfisUsuario(titulo);
        
        // Converte para DTO do auth
        List<PerfilDto> perfis = perfisSgrh.stream()
            .map(p -> new PerfilDto(p.perfil(), p.unidadeCodigo().toString()))
            .collect(Collectors.toList());
        
        log.debug("Perfis encontrados para {}: {} perfis", titulo, perfis.size());
        
        return perfis;
    }

    /**
     * Busca dados do servidor autenticado via SGRH.
     * <p>
     * Integra com o SgrhService para buscar dados nas views do Oracle.
     * Atualmente usa dados MOCK do SgrhService até a conexão real estar disponível.
     *
     * @param titulo Título do servidor
     * @return ServidorDto com dados do servidor
     */
    private ServidorDto buscarDadosServidor(String titulo) {
        log.debug("Buscando dados do servidor via SGRH: {}", titulo);

        // Tenta buscar no SGRH primeiro
        UsuarioDto usuarioSgrh = sgrhService.buscarUsuarioPorTitulo(titulo)
            .orElse(null);
        
        if (usuarioSgrh != null) {
            log.debug("Servidor encontrado no SGRH: {}", usuarioSgrh.nome());
            return new ServidorDto(
                usuarioSgrh.titulo(),
                usuarioSgrh.nome(),
                usuarioSgrh.email(),
                null, // ramal não vem do SGRH
                null  // unidadeCodigo será determinado pelos perfis
            );
        }

        // Fallback: busca na base local do SGC
        Usuario usuario = entityManager.find(Usuario.class, titulo);

        if (usuario == null) {
            log.warn("Servidor não encontrado nem no SGRH nem na base local: {}", titulo);
            // Retorna dados básicos se não encontrar (permitir login mesmo sem cadastro completo)
            return new ServidorDto(titulo, "Usuário " + titulo, null, null, null);
        }

        String unidadeCodigo = usuario.getUnidade() != null
                ? usuario.getUnidade().getCodigo().toString()
                : null;

        return new ServidorDto(
                usuario.getTitulo(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getRamal(),
                unidadeCodigo
        );
    }

    /**
     * Retorna os perfis do usuário autenticado atual.
     *
     * @param titulo Título do usuário autenticado
     * @return Lista de perfis
     */
    public List<PerfilDto> getPerfisUsuarioAutenticado(String titulo) {
        log.debug("Recuperando perfis do usuário autenticado: {}", titulo);
        return buscarPerfisUsuario(titulo);
    }
}