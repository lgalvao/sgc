package sgc.integracao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import sgc.alerta.model.Alerta;
import sgc.alerta.model.AlertaRepo;
import sgc.alerta.model.AlertaUsuario;
import sgc.alerta.model.AlertaUsuarioRepo;
import sgc.mapa.model.Mapa;
import sgc.organizacao.ContextoUsuarioAutenticado;
import sgc.organizacao.model.*;
import sgc.processo.dto.ProcessoResumoDto;
import sgc.processo.model.Processo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.processo.painel.PainelService;
import sgc.subprocesso.dto.AnaliseHistoricoDto;
import sgc.subprocesso.dto.SubprocessoDetalheResponse;
import sgc.subprocesso.model.*;
import sgc.subprocesso.service.SubprocessoConsultaService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("integration")
@Transactional
@DisplayName("Budget de Queries de Processo e Subprocesso")
class ProcessoSubprocessoViewsQueryBudgetIntegrationTest extends BaseIntegrationTest {
    private static final Logger logger = LoggerFactory.getLogger(ProcessoSubprocessoViewsQueryBudgetIntegrationTest.class);
    private static final String TITULO_USUARIO_CHEFE = "111111111111";

    @Autowired
    private EntityManager entityManager;
    @Autowired
    private EntityManagerFactory entityManagerFactory;
    @Autowired
    private PainelService painelService;
    @Autowired
    private SubprocessoConsultaService subprocessoConsultaService;
    @Autowired
    private ResponsabilidadeRepo responsabilidadeRepo;
    @Autowired
    private AlertaRepo alertaRepo;
    @Autowired
    private AlertaUsuarioRepo alertaUsuarioRepo;

    private Unidade unidadeFilha;
    private Usuario usuarioAdmin;
    private Usuario usuarioChefe;
    private Subprocesso subprocesso;

    @BeforeEach
    void setUp() {
        Unidade unidadeRaiz = criarUnidade("UNR", "Unidade raiz budget", null);
        unidadeFilha = criarUnidade("UNF", "Unidade filha budget", unidadeRaiz);

        usuarioChefe = usuarioRepo.findById(TITULO_USUARIO_CHEFE).orElseThrow();
        usuarioChefe.setPerfilAtivo(Perfil.CHEFE);
        usuarioChefe.setUnidadeAtivaCodigo(unidadeFilha.getCodigo());

        usuarioAdmin = usuarioRepo.findById("3").orElseThrow();
        usuarioAdmin.setPerfilAtivo(Perfil.ADMIN);
        usuarioAdmin.setUnidadeAtivaCodigo(unidadeFilha.getCodigo());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        usuarioAdmin,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
                )
        );

        criarProcessosPainel(unidadeFilha);
        subprocesso = criarSubprocessoDetalhe(unidadeFilha);
        criarAlertasPainel(unidadeFilha);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("Painel deve listar processos sem lookup unitario por item")
    void painelDeveListarProcessosSemLookupUnitarioPorItem() {
        MetricasExecucaoTeste.ResultadoMedicao medicao = medir("painel.listarProcessos", () -> {
            Page<ProcessoResumoDto> pagina = painelService.listarProcessos(
                    new ContextoUsuarioAutenticado(
                            usuarioChefe.getTituloEleitoral(),
                            unidadeFilha.getCodigo(),
                            Perfil.CHEFE
                    ),
                    PageRequest.of(0, 20)
            );

            assertThat(pagina.getContent())
                    .filteredOn(dto -> dto.descricao().startsWith("Processo budget"))
                    .hasSize(12);
            return pagina;
        }, "VW_UNIDADE", "PROCESSO");

        assertThat(medicao.sqlsViewsOrganizacionais()).isLessThanOrEqualTo(2);
        medicao.validarTempoSeEstrito(500);
    }

    @Test
    @DisplayName("Detalhe de subprocesso deve manter budget estavel de consultas")
    void detalheDeSubprocessoDeveManterBudgetEstavel() {
        MetricasExecucaoTeste.ResultadoMedicao medicao = medir("subprocesso.obterDetalhes", () -> {
            SubprocessoDetalheResponse detalhes = subprocessoConsultaService.obterDetalhes(subprocesso.getCodigo());

            assertThat(detalhes).isNotNull();
            assertThat(detalhes.subprocesso().codigo()).isEqualTo(subprocesso.getCodigo());
            return detalhes;
        }, "VW_UNIDADE", "SUBPROCESSO", "MOVIMENTACAO");

        assertThat(medicao.sqlsViewsOrganizacionais()).isLessThanOrEqualTo(7);
        medicao.validarTempoSeEstrito(800);
    }

    @Test
    @DisplayName("Painel deve listar alertas sem lookup unitario de leitura")
    void painelDeveListarAlertasSemLookupUnitarioDeLeitura() {
        MetricasExecucaoTeste.ResultadoMedicao medicao = medir("painel.listarAlertas", () -> {
            Page<Alerta> pagina = painelService.listarAlertas(
                    new ContextoUsuarioAutenticado(
                            usuarioAdmin.getTituloEleitoral(),
                            unidadeFilha.getCodigo(),
                            Perfil.ADMIN
                    ),
                    PageRequest.of(0, 10)
            );

            assertThat(pagina.getContent()).hasSize(8);
            assertThat(pagina.getContent()).allMatch(alerta -> alerta.getDataHoraLeitura() != null);
            return pagina;
        }, "ALERTA", "ALERTA_USUARIO");

        assertThat(medicao.contagensPorTrecho().get("ALERTA_USUARIO")).isLessThanOrEqualTo(1);
        assertThat(medicao.totalSqls()).isLessThanOrEqualTo(4);
        medicao.validarTempoSeEstrito(500);
    }

    @Test
    @DisplayName("Historico de validacao deve carregar unidades em lote")
    void historicoDeValidacaoDeveCarregarUnidadesEmLote() {
        MetricasExecucaoTeste.ResultadoMedicao medicao = medir("subprocesso.listarHistoricoValidacao", () -> {
            List<AnaliseHistoricoDto> historico = subprocessoConsultaService.listarHistoricoValidacao(subprocesso.getCodigo());
            assertThat(historico).hasSize(2);
            return historico;
        }, "ANALISE", "VW_UNIDADE");

        assertThat(medicao.sqlsViewsOrganizacionais()).isLessThanOrEqualTo(2);
        assertThat(medicao.totalSqls()).isLessThanOrEqualTo(3);
        medicao.validarTempoSeEstrito(300);
    }

    private <T> MetricasExecucaoTeste.ResultadoMedicao medir(String nome, java.util.function.Supplier<T> acao, String... trechosSql) {
        MetricasExecucaoTeste.ResultadoMedicao medicao = MetricasExecucaoTeste.medir(
                entityManager,
                entityManagerFactory,
                nome,
                acao,
                trechosSql
        );
        logger.info("Medicao budget: {}", medicao.resumo());
        return medicao;
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
            processo = processoRepo.save(processo);
            subprocessoRepo.save(Subprocesso.builder()
                    .unidade(unidade)
                    .situacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO)
                    .processo(processo)
                    .dataLimiteEtapa1(LocalDateTime.now().plusDays(10))
                    .build());
        }
    }

    private Subprocesso criarSubprocessoDetalhe(Unidade unidade) {
        Processo processo = Processo.builder()
                .descricao("Processo detalhe budget")
                .tipo(TipoProcesso.MAPEAMENTO)
                .situacao(SituacaoProcesso.EM_ANDAMENTO)
                .dataLimite(LocalDateTime.now().plusDays(20))
                .build();
        processo.adicionarParticipantes(Set.of(unidade));
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

        entityManager.persist(Analise.builder()
                .subprocesso(novoSubprocesso)
                .dataHora(LocalDateTime.now().minusHours(1))
                .observacoes("Validacao 1")
                .acao(TipoAcaoAnalise.ACEITE_MAPEAMENTO)
                .unidadeCodigo(unidade.getCodigo())
                .usuarioTitulo(usuarioAdmin.getTituloEleitoral())
                .tipo(TipoAnalise.VALIDACAO)
                .build());
        entityManager.persist(Analise.builder()
                .subprocesso(novoSubprocesso)
                .dataHora(LocalDateTime.now().minusHours(2))
                .observacoes("Validacao 2")
                .acao(TipoAcaoAnalise.DEVOLUCAO_MAPEAMENTO)
                .unidadeCodigo(unidade.getCodigo())
                .usuarioTitulo(usuarioAdmin.getTituloEleitoral())
                .tipo(TipoAnalise.VALIDACAO)
                .build());

        return novoSubprocesso;
    }

    private void criarAlertasPainel(Unidade unidade) {
        Processo processoAlerta = Processo.builder()
                .descricao("Processo alerta budget")
                .tipo(TipoProcesso.MAPEAMENTO)
                .situacao(SituacaoProcesso.EM_ANDAMENTO)
                .dataCriacao(LocalDateTime.now())
                .dataLimite(LocalDateTime.now().plusDays(15))
                .build();
        processoAlerta.adicionarParticipantes(Set.of(unidade));
        processoRepo.save(processoAlerta);

        for (int indice = 0; indice < 8; indice++) {
            Alerta alerta = Alerta.builder()
                    .processo(processoAlerta)
                    .dataHora(LocalDateTime.now().minusMinutes(indice))
                    .unidadeOrigem(unidade)
                    .unidadeDestino(unidade)
                    .descricao("Alerta budget %d".formatted(indice))
                    .build();
            alertaRepo.save(alerta);
            alertaUsuarioRepo.save(AlertaUsuario.builder()
                    .codigo(AlertaUsuario.Chave.builder()
                            .alertaCodigo(alerta.getCodigo())
                            .usuarioTitulo(usuarioAdmin.getTituloEleitoral())
                            .build())
                    .alerta(alerta)
                    .usuario(usuarioAdmin)
                    .dataHoraLeitura(LocalDateTime.now().minusMinutes(1))
                    .build());
        }
    }
}
