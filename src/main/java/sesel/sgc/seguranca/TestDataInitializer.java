package sesel.sgc.seguranca;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import sesel.sgc.servidor.Servidor;
import sesel.sgc.servidor.ServidorRepository;
import sesel.sgc.unidade.Unidade;
import sesel.sgc.unidade.UnidadeRepository;

import java.util.Arrays;
import java.util.List;

/**
 * Initializes test data for development and testing environments.
 * Creates sample units and users with different roles for testing purposes.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Profile("!prod") // Only run this in non-production environments
public class TestDataInitializer {
    private final ServidorRepository servidorRepository;
    private final UnidadeRepository unidadeRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Initializes test data when the application starts.
     * Creates sample units and users if they don't already exist.
     */
    @PostConstruct
    @Transactional
    public void init() {
        log.info("Inicializando dados de teste...");

        // Create or get the main unit (SEDOC)
        Unidade sedoc = createOrGetUnidade("SEDOC", "Secretaria de Documentação", null);

        // Create or get a sub-unit
        Unidade subUnidade = createOrGetUnidade("SGP", "Secretaria de Gestão de Pessoas", sedoc);

        // Create or get users with different roles
        createUserIfNotExists(
                "sedoc@tre-pe.jus.br",
                "Administrador do Sistema",
                "Gerente de TI",
                sedoc,
                Arrays.asList(Perfil.SEDOC, Perfil.GESTOR),
                "admin123"
        );

        createUserIfNotExists(
                "gestor@tre-pe.jus.br",
                "Gestor de Unidade",
                "Coordenador de TI",
                sedoc,
                List.of(Perfil.GESTOR),
                "gestor123"
        );

        createUserIfNotExists(
                "chefe@tre-pe.jus.br",
                "Chefe de Seção",
                "Chefe de Seção de Desenvolvimento",
                subUnidade,
                List.of(Perfil.CHEFE),
                "chefe123"
        );

        createUserIfNotExists(
                "usuario@example.com",
                "Usuário Comum",
                "Analista de Sistemas",
                subUnidade,
                List.of(Perfil.SERVIDOR),
                "usuario123"
        );

        log.info("Dados de teste inicializados com sucesso!");
    }

    /**
     * Creates a unit if it doesn't exist, or returns the existing one.
     *
     * @param nome            The name of the unit
     * @param descricao       The description of the unit
     * @param unidadeSuperior The parent unit (can be null for top-level units)
     * @return The created or existing unit
     */
    private Unidade createOrGetUnidade(String nome, String descricao, Unidade unidadeSuperior) {
        return unidadeRepository.findByNome(nome)
                .orElseGet(() -> {
                    Unidade unidade = new Unidade();
                    unidade.setNome(nome);
                    unidade.setDescricao(descricao);
                    if (unidadeSuperior != null) {
                        unidade.setUnidadeSuperior(unidadeSuperior);
                    }
                    log.info("Criando unidade: {}", nome);
                    return unidadeRepository.save(unidade);
                });
    }

    /**
     * Creates a user if it doesn't already exist.
     *
     * @param email   The user's email (must be unique)
     * @param nome    The user's full name
     * @param cargo   The user's job position
     * @param unidade The unit the user belongs to
     * @param perfis  The user's security profiles/roles
     * @param senha   The user's password (will be hashed)
     */
    private void createUserIfNotExists(String email, String nome, String cargo,
                                       Unidade unidade, List<Perfil> perfis, String senha) {
        if (servidorRepository.findByEmail(email).isEmpty()) {
            Servidor usuario = new Servidor();
            usuario.setNome(nome);
            usuario.setEmail(email);
            usuario.setCargo(cargo);
            usuario.setUnidade(unidade);
            usuario.setSenha(passwordEncoder.encode(senha));
            usuario.setPerfis(perfis);

            servidorRepository.save(usuario);
            log.info("Usuário criado: {} ({})", nome, email);
        }
    }
}
