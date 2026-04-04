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

        assertThat(contarQueriesViews(() -> unidadeHierarquiaService.buscarArvoreHierarquica())).isLessThanOrEqualTo(1);
        assertThat(contarQueriesViews(() -> unidadeHierarquiaService.buscarMapaHierarquia())).isLessThanOrEqualTo(1);
        assertThat(contarQueriesViews(() -> usuarioService.buscarOpt(amostras.tituloUsuario()))).isLessThanOrEqualTo(2);
        assertThat(contarQueriesViews(() -> usuarioService.buscarPorUnidadeLotacao(amostras.codigoUnidadeLotacao()))).isLessThanOrEqualTo(2);
        assertThat(contarQueriesViews(() -> usuarioService.buscarPorNome(amostras.termoBuscaUsuario()))).isLessThanOrEqualTo(3);
        assertThat(contarQueriesViews(() -> usuarioService.buscarPerfis(amostras.tituloUsuarioComPerfil()))).isLessThanOrEqualTo(1);
        assertThat(contarQueriesViews(() -> responsavelUnidadeService.buscarResponsavelAtual(amostras.siglaUnidadeComResponsavel()))).isLessThanOrEqualTo(3);
        assertThat(contarQueriesViews(() -> responsavelUnidadeService.buscarResponsaveisUnidades(amostras.codigosUnidadesComResponsavel()))).isLessThanOrEqualTo(2);
    }

    @Test
    @DisplayName("Deve reutilizar cache sem preparar novas queries nos acessos repetidos")
    void deveReutilizarCacheSemPrepararNovasQueriesNosAcessosRepetidos() {
        assertThat(contarQueriesViews(() -> unidadeHierarquiaService.buscarArvoreHierarquica())).isLessThanOrEqualTo(1);
        assertThat(contarQueriesViews(() -> unidadeHierarquiaService.buscarArvoreHierarquica())).isZero();

        assertThat(contarQueriesViews(() -> unidadeHierarquiaService.buscarMapaHierarquia())).isLessThanOrEqualTo(1);
        assertThat(contarQueriesViews(() -> unidadeHierarquiaService.buscarMapaHierarquia())).isZero();

        assertThat(contarQueriesViews(unidadeService::buscarAdmin)).isLessThanOrEqualTo(1);
        assertThat(contarQueriesViews(unidadeService::buscarAdmin)).isZero();
    }

    private long contarQueries(Runnable acao) {
        Statistics estatisticas = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        estatisticas.setStatisticsEnabled(true);

        entityManager.clear();
        estatisticas.clear();
        sgc.integracao.mocks.ColetorSqlTeste.limpar();

        acao.run();

        return estatisticas.getPrepareStatementCount();
    }

    private long contarQueriesViews(Runnable acao) {
        contarQueries(acao);
        return sgc.integracao.mocks.ColetorSqlTeste.contarSqlsViewsOrganizacionais();
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
