package sgc.integracao;

import jakarta.persistence.*;
import org.hibernate.*;
import org.hibernate.stat.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.data.domain.*;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

@Tag("integration")
@DisplayName("Budget de Queries das Views Organizacionais")
class OrganizacaoViewsQueryBudgetIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private EntityManager entityManager;
    @Autowired
    private EntityManagerFactory entityManagerFactory;
    @Autowired
    private UnidadeHierarquiaService unidadeHierarquiaService;
    @Autowired
    private UnidadeService unidadeService;
    @Autowired
    private UsuarioService usuarioService;
    @Autowired
    private ResponsavelUnidadeService responsavelUnidadeService;
    @Autowired
    private UsuarioRepo usuarioRepo;
    @Autowired
    private UsuarioPerfilRepo usuarioPerfilRepo;
    @Autowired
    private ResponsabilidadeRepo responsabilidadeRepo;
    @Autowired
    private UnidadeRepo unidadeRepo;

    @Test
    @DisplayName("Deve manter budget de queries nas leituras organizacionais mais comuns")
    void deveManterBudgetDeQueriesNasLeiturasOrganizacionaisMaisComuns() {
        AmostrasConsulta amostras = carregarAmostras();

        assertThat(contarQueries(() -> unidadeHierarquiaService.buscarArvoreHierarquica())).isLessThanOrEqualTo(1);
        assertThat(contarQueries(() -> unidadeHierarquiaService.buscarMapaHierarquia())).isLessThanOrEqualTo(1);
        assertThat(contarQueries(() -> usuarioService.buscarOpt(amostras.tituloUsuario()))).isLessThanOrEqualTo(2);
        assertThat(contarQueries(() -> usuarioService.buscarPorUnidadeLotacao(amostras.codigoUnidadeLotacao()))).isLessThanOrEqualTo(2);
        assertThat(contarQueries(() -> usuarioService.buscarPorNomeOuMatricula(amostras.termoBuscaUsuario()))).isLessThanOrEqualTo(5);
        assertThat(contarQueries(() -> usuarioService.buscarPerfis(amostras.tituloUsuarioComPerfil()))).isLessThanOrEqualTo(1);
        assertThat(contarQueries(() -> responsavelUnidadeService.buscarResponsavelAtual(amostras.siglaUnidadeComResponsavel()))).isLessThanOrEqualTo(3);
        assertThat(contarQueries(() -> responsavelUnidadeService.buscarResponsaveisUnidades(amostras.codigosUnidadesComResponsavel()))).isLessThanOrEqualTo(10);
    }

    @Test
    @DisplayName("Deve reutilizar cache sem preparar novas queries nos acessos repetidos")
    void deveReutilizarCacheSemPrepararNovasQueriesNosAcessosRepetidos() {
        assertThat(contarQueries(() -> unidadeHierarquiaService.buscarArvoreHierarquica())).isLessThanOrEqualTo(1);
        assertThat(contarQueries(() -> unidadeHierarquiaService.buscarArvoreHierarquica())).isZero();

        assertThat(contarQueries(() -> unidadeHierarquiaService.buscarMapaHierarquia())).isLessThanOrEqualTo(1);
        assertThat(contarQueries(() -> unidadeHierarquiaService.buscarMapaHierarquia())).isZero();

        assertThat(contarQueries(unidadeService::buscarAdmin)).isLessThanOrEqualTo(1);
        assertThat(contarQueries(unidadeService::buscarAdmin)).isZero();
    }

    private long contarQueries(Runnable acao) {
        Statistics estatisticas = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        estatisticas.setStatisticsEnabled(true);

        entityManager.clear();
        estatisticas.clear();

        acao.run();

        return estatisticas.getPrepareStatementCount();
    }

    private AmostrasConsulta carregarAmostras() {
        Usuario usuarioBase = usuarioRepo.findAll(PageRequest.of(0, 1)).getContent().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Nenhum usuario encontrado para budget de queries."));

        String tituloUsuario = usuarioBase.getTituloEleitoral();
        Long codigoUnidadeLotacao = usuarioBase.getUnidadeLotacao().getCodigo();

        String termoBusca = extrairTermoBusca(usuarioBase);

        String tituloUsuarioComPerfil = usuarioPerfilRepo.findAll(PageRequest.of(0, 1)).getContent().stream()
                .map(UsuarioPerfil::getUsuarioTitulo)
                .findFirst()
                .orElse(tituloUsuario);

        List<Responsabilidade> responsabilidades = responsabilidadeRepo.findAll(PageRequest.of(0, 10)).getContent();
        assertThat(responsabilidades).isNotEmpty();

        Responsabilidade responsabilidadeBase = responsabilidades.getFirst();
        String siglaUnidadeComResponsavel = unidadeRepo.findById(responsabilidadeBase.getUnidadeCodigo())
                .map(Unidade::getSigla)
                .orElseThrow(() -> new IllegalStateException("Nao foi possivel localizar unidade da amostra."));

        List<Long> codigosUnidadesComResponsavel = responsabilidades.stream()
                .map(Responsabilidade::getUnidadeCodigo)
                .distinct()
                .limit(10)
                .toList();

        return new AmostrasConsulta(
                tituloUsuario,
                codigoUnidadeLotacao,
                termoBusca,
                tituloUsuarioComPerfil,
                siglaUnidadeComResponsavel,
                codigosUnidadesComResponsavel
        );
    }

    private String extrairTermoBusca(Usuario usuario) {
        String nome = Optional.ofNullable(usuario.getNome()).orElse("").trim();
        if (!nome.isBlank()) {
            String primeiroToken = nome.split("\\s+")[0];
            if (primeiroToken.length() >= 3) {
                return primeiroToken.substring(0, 3);
            }
            return primeiroToken;
        }

        String matricula = Optional.ofNullable(usuario.getMatricula()).orElse("").trim();
        if (matricula.length() >= 3) {
            return matricula.substring(0, 3);
        }
        return matricula;
    }

    private record AmostrasConsulta(
            String tituloUsuario,
            Long codigoUnidadeLotacao,
            String termoBuscaUsuario,
            String tituloUsuarioComPerfil,
            String siglaUnidadeComResponsavel,
            List<Long> codigosUnidadesComResponsavel
    ) {
    }
}
