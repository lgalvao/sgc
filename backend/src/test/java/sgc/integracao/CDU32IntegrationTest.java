package sgc.integracao;

import jakarta.persistence.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.*;
import org.springframework.transaction.annotation.*;
import sgc.alerta.model.*;
import sgc.comum.ComumDtos.*;
import sgc.fixture.*;
import sgc.integracao.mocks.*;
import sgc.organizacao.model.*;
import sgc.processo.model.*;
import sgc.subprocesso.model.*;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Tag("integration")
@Transactional
@DisplayName("CDU-32: Reabrir cadastro")
class CDU32IntegrationTest extends BaseIntegrationTest {
    private static final String API_REABRIR_CADASTRO = "/api/subprocessos/{codigo}/reabrir-cadastro";

    @Autowired
    private MovimentacaoRepo movimentacaoRepo;

    @Autowired
    private AlertaRepo alertaRepo;

    @Autowired
    private EntityManager entityManager;

    private Subprocesso subprocesso;

    @BeforeEach
    void setUp() {
        // Garantir que ADMIN existe
        if (unidadeRepo.findBySigla("ADMIN").isEmpty()) {
            Unidade admin = new Unidade();
            admin.setSigla("ADMIN");
            admin.setNome("Administração");
            admin.setSituacao(SituacaoUnidade.ATIVA);
            admin.setTipo(TipoUnidade.RAIZ);
            unidadeRepo.save(admin);
        }

        Unidade unidade = buscarOuCriarUnidadeComDoisSuperiores("CDU32");

        // Criar processo
        Processo processo = ProcessoFixture.processoPadrao();
        processo.setCodigo(null);
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        processo.setDescricao("Processo CDU-32");
        processo = processoRepo.save(processo);

        // Criar subprocesso em estado que permite reabertura (MAPEAMENTO_MAPA_HOMOLOGADO)
        subprocesso = SubprocessoFixture.subprocessoPadrao(processo, unidade);
        subprocesso.setCodigo(null);
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO);
        subprocesso.setDataLimiteEtapa1(LocalDateTime.now().plusDays(10));
        subprocesso = subprocessoRepo.save(subprocesso);

        entityManager.flush();
        entityManager.clear();

        subprocesso = subprocessoRepo.findById(subprocesso.getCodigo()).orElseThrow();
    }

    @Test
    @DisplayName("Deve reabrir cadastro com justificativa válida quando ADMIN")
    @WithMockAdmin
    void reabrirCadastro_comoAdmin_sucesso() throws Exception {
        JustificativaRequest request = new JustificativaRequest(
                "Necessário ajustar informações do cadastro");

        mockMvc.perform(post(API_REABRIR_CADASTRO, subprocesso.getCodigo())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        entityManager.flush();
        entityManager.clear();

        Subprocesso reaberto = subprocessoRepo.findById(subprocesso.getCodigo()).orElseThrow();
        assertThat(reaberto.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);

        // Verificar se foi criada uma movimentação
        List<Movimentacao> movimentacoes = movimentacaoRepo
                .listarPorSubprocessoOrdenadasPorDataHoraDesc(subprocesso.getCodigo());

        assertThat(movimentacoes).isNotEmpty();

        boolean movimentacaoExiste = movimentacoes.stream().anyMatch(m -> m.getDescricao().contains("Reabertura de cadastro"));
        assertThat(movimentacaoExiste).isTrue();

        // Verificar se foi criado um alerta
        List<Alerta> alerts = alertaRepo.findAll();
        assertThat(alerts).isNotEmpty();
        boolean alertaExiste = alerts.stream()
                .anyMatch(a -> a.getUnidadeDestino() != null &&
                        a.getUnidadeDestino().getCodigo().equals(reaberto.getUnidade().getCodigo()) &&
                        a.getDescricao().contains("reaberto"));
        assertThat(alertaExiste).isTrue();

        Long codigoUnidadeSuperior = Optional.ofNullable(reaberto.getUnidade().getUnidadeSuperior())
                .map(Unidade::getCodigo)
                .orElse(null);
        assertThat(codigoUnidadeSuperior).isNotNull();

        boolean alertaSuperiorExiste = alerts.stream()
                .anyMatch(a -> a.getUnidadeDestino() != null &&
                        Objects.equals(a.getUnidadeDestino().getCodigo(), codigoUnidadeSuperior) &&
                        a.getDescricao().contains("Cadastro da unidade %s reaberto".formatted(reaberto.getUnidade().getSigla())));
        assertThat(alertaSuperiorExiste).isTrue();

        List<Long> codigosEsperadosSuperiores = coletarCodigosSuperiores(reaberto.getUnidade());
        assertThat(codigosEsperadosSuperiores)
                .as("CDU-32 deve propagar alerta para toda a cadeia hierárquica superior")
                .hasSizeGreaterThanOrEqualTo(2);
        long quantidadeAlertasSuperiores = alerts.stream()
                .filter(a -> a.getUnidadeDestino() != null)
                .filter(a -> codigosEsperadosSuperiores.contains(a.getUnidadeDestino().getCodigo()))
                .filter(a -> a.getDescricao().contains("Cadastro da unidade %s reaberto".formatted(reaberto.getUnidade().getSigla())))
                .count();
        assertThat(quantidadeAlertasSuperiores).isEqualTo(codigosEsperadosSuperiores.size());
    }

    @Test
    @DisplayName("Não deve permitir reabrir cadastro sem ser ADMIN")
    @WithMockUser(roles = "GESTOR")
    void reabrirCadastro_semPermissao_proibido() throws Exception {
        JustificativaRequest request = new JustificativaRequest("Tentativa sem permissão");

        mockMvc.perform(post(API_REABRIR_CADASTRO, subprocesso.getCodigo())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Não deve permitir reabrir cadastro sem justificativa")
    @WithMockAdmin
    void reabrirCadastro_semJustificativa_erro() throws Exception {
        JustificativaRequest request = new JustificativaRequest("");

        mockMvc.perform(post(API_REABRIR_CADASTRO, subprocesso.getCodigo())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Não deve permitir reabrir cadastro quando em situação insuficiente (ex: Cadastro homologado)")
    @WithMockAdmin
    void reabrirCadastro_SituacaoInsuficiente_Erro() throws Exception {
        // Forçar situação que ainda não atingiu MAPA_HOMOLOGADO
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);
        subprocessoRepo.save(subprocesso);
        entityManager.flush();
        entityManager.clear();

        JustificativaRequest request = new JustificativaRequest("Tentativa em estado precoce");

        mockMvc.perform(post(API_REABRIR_CADASTRO, subprocesso.getCodigo())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableContent());
    }

    private List<Long> coletarCodigosSuperiores(Unidade unidade) {
        List<Long> codigos = new ArrayList<>();
        Unidade superiorAtual = unidade.getUnidadeSuperior();
        while (superiorAtual != null) {
            codigos.add(superiorAtual.getCodigo());
            superiorAtual = superiorAtual.getUnidadeSuperior();
        }
        return codigos;
    }

    private Unidade buscarOuCriarUnidadeComDoisSuperiores(String sufixo) {
        String siglaSecao = "SEC_T_%s".formatted(sufixo);
        Optional<Unidade> secaoExistente = unidadeRepo.findBySigla(siglaSecao);
        if (secaoExistente.isPresent()) {
            return secaoExistente.get();
        }

        Unidade secretaria = new Unidade();
        secretaria.setSigla("SECRET_T_%s".formatted(sufixo));
        secretaria.setNome("Secretaria teste %s".formatted(sufixo));
        secretaria.setSituacao(SituacaoUnidade.ATIVA);
        secretaria.setTipo(TipoUnidade.INTEROPERACIONAL);
        secretaria = unidadeRepo.save(secretaria);

        Unidade coordenadoria = new Unidade();
        coordenadoria.setSigla("COORD_T_%s".formatted(sufixo));
        coordenadoria.setNome("Coordenadoria teste %s".formatted(sufixo));
        coordenadoria.setSituacao(SituacaoUnidade.ATIVA);
        coordenadoria.setTipo(TipoUnidade.INTERMEDIARIA);
        coordenadoria.setUnidadeSuperior(secretaria);
        coordenadoria = unidadeRepo.save(coordenadoria);

        Unidade secao = new Unidade();
        secao.setSigla(siglaSecao);
        secao.setNome("Seção teste %s".formatted(sufixo));
        secao.setSituacao(SituacaoUnidade.ATIVA);
        secao.setTipo(TipoUnidade.OPERACIONAL);
        secao.setUnidadeSuperior(coordenadoria);
        return unidadeRepo.save(secao);
    }
}
