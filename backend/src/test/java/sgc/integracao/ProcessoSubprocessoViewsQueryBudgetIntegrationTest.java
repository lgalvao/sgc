package sgc.integracao;

import jakarta.persistence.*;
import org.hibernate.*;
import org.hibernate.stat.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.data.domain.*;
import org.springframework.transaction.annotation.*;
import sgc.mapa.model.*;
import sgc.organizacao.model.*;
import sgc.processo.dto.*;
import sgc.processo.model.*;
import sgc.processo.painel.*;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.model.*;
import sgc.subprocesso.service.*;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

@Tag("integration")
@Transactional
@DisplayName("Budget de Queries de Processo e Subprocesso")
class ProcessoSubprocessoViewsQueryBudgetIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private EntityManager entityManager;
    @Autowired
    private EntityManagerFactory entityManagerFactory;
    @Autowired
    private PainelFacade painelFacade;
    @Autowired
    private SubprocessoConsultaService subprocessoConsultaService;
    @Autowired
    private ResponsabilidadeRepo responsabilidadeRepo;

    private Unidade unidadeFilha;
    private Usuario usuarioAdmin;
    private Subprocesso subprocesso;

    @BeforeEach
    void setUp() {
        Unidade unidadeRaiz = criarUnidade("UNR", "Unidade raiz budget", null);
        unidadeFilha = criarUnidade("UNF", "Unidade filha budget", unidadeRaiz);

        Usuario usuarioChefe = usuarioRepo.findById("111111111111").orElseThrow();
        usuarioChefe.setPerfilAtivo(Perfil.CHEFE);
        usuarioChefe.setUnidadeAtivaCodigo(unidadeFilha.getCodigo());

        usuarioAdmin = usuarioRepo.findById("3").orElseThrow();
        usuarioAdmin.setPerfilAtivo(Perfil.ADMIN);
        usuarioAdmin.setUnidadeAtivaCodigo(unidadeFilha.getCodigo());

        criarProcessosPainel(unidadeFilha);
        subprocesso = criarSubprocessoDetalhe(unidadeFilha);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("Painel deve listar processos sem lookup unitario por item")
    void painelDeveListarProcessosSemLookupUnitarioPorItem() {
        long queriesViews = contarQueriesViews(() -> {
            Page<ProcessoResumoDto> pagina = painelFacade.listarProcessos(
                    Perfil.CHEFE,
                    unidadeFilha.getCodigo(),
                    PageRequest.of(0, 20)
            );

            assertThat(pagina.getContent()).hasSize(12);
        });

        assertThat(queriesViews).isLessThanOrEqualTo(2);
    }

    @Test
    @DisplayName("Detalhe de subprocesso deve manter budget estavel de consultas")
    void detalheDeSubprocessoDeveManterBudgetEstavel() {
        long queriesViews = contarQueriesViews(() -> {
            SubprocessoDetalheResponse detalhes = subprocessoConsultaService.obterDetalhes(subprocesso.getCodigo(), usuarioAdmin);

            assertThat(detalhes).isNotNull();
            assertThat(detalhes.subprocesso().codigo()).isEqualTo(subprocesso.getCodigo());
        });

        assertThat(queriesViews).isLessThanOrEqualTo(7);
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

    private Unidade criarUnidade(String sigla, String nome, Unidade superior) {
        Unidade unidade = new Unidade();
        unidade.setSigla(sigla);
        unidade.setNome(nome);
        unidade.setTipo(superior == null ? TipoUnidade.RAIZ : TipoUnidade.OPERACIONAL);
        unidade.setSituacao(SituacaoUnidade.ATIVA);
        unidade.setUnidadeSuperior(superior);
        unidade.setTituloTitular("111111111111");
        return unidadeRepo.save(unidade);
    }

    private void criarProcessosPainel(Unidade unidade) {
        for (int i = 0; i < 12; i++) {
            Processo processo = Processo.builder()
                    .descricao("Processo budget %02d".formatted(i))
                    .tipo(TipoProcesso.MAPEAMENTO)
                    .situacao(SituacaoProcesso.EM_ANDAMENTO)
                    .dataCriacao(LocalDateTime.now().minusDays(i))
                    .dataLimite(LocalDateTime.now().plusDays(30))
                    .build();
            processo.adicionarParticipantes(Set.of(unidade));
            processoRepo.save(processo);
        }
    }

    private Subprocesso criarSubprocessoDetalhe(Unidade unidade) {
        Processo processo = Processo.builder()
                .descricao("Processo detalhe budget")
                .tipo(TipoProcesso.MAPEAMENTO)
                .situacao(SituacaoProcesso.EM_ANDAMENTO)
                .dataLimite(LocalDateTime.now().plusDays(20))
                .build();
        processoRepo.save(processo);

        Subprocesso novoSubprocesso = Subprocesso.builder()
                .unidade(unidade)
                .situacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO)
                .processo(processo)
                .dataLimiteEtapa1(LocalDateTime.now().plusDays(10))
                .build();
        subprocessoRepo.save(novoSubprocesso);

        movimentacaoRepo.save(Movimentacao.builder()
                .subprocesso(novoSubprocesso)
                .unidadeOrigem(unidade)
                .unidadeDestino(unidade)
                .usuario(usuarioAdmin)
                .descricao("Movimentação budget")
                .build());

        Mapa mapa = new Mapa();
        mapa.setSubprocesso(novoSubprocesso);
        mapaRepo.save(mapa);
        novoSubprocesso.setMapa(mapa);

        Responsabilidade responsabilidade = new Responsabilidade();
        responsabilidade.setUnidadeCodigo(unidade.getCodigo());
        responsabilidade.setUsuarioTitulo(usuarioAdmin.getTituloEleitoral());
        responsabilidade.setTipo("TITULAR");
        responsabilidade.setDataInicio(LocalDateTime.now().minusDays(1));
        responsabilidadeRepo.save(responsabilidade);

        return novoSubprocesso;
    }
}
