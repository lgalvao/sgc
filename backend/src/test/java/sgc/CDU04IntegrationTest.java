package sgc;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import sgc.alerta.modelo.Alerta;
import sgc.alerta.modelo.AlertaRepo;
import sgc.comum.modelo.SituacaoProcesso;
import sgc.comum.modelo.SituacaoSubprocesso;
import sgc.notificacao.NotificacaoServico;
import sgc.processo.modelo.TipoProcesso;
import sgc.processo.modelo.Processo;
import sgc.processo.modelo.ProcessoRepo;
import sgc.processo.modelo.UnidadeProcesso;
import sgc.processo.modelo.UnidadeProcessoRepo;
import sgc.sgrh.SgrhService;
import sgc.sgrh.dto.ResponsavelDto;
import sgc.sgrh.dto.UnidadeDto;
import sgc.sgrh.dto.UsuarioDto;
import sgc.subprocesso.modelo.Movimentacao;
import sgc.subprocesso.modelo.MovimentacaoRepo;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.subprocesso.modelo.SubprocessoRepo;
import sgc.unidade.modelo.SituacaoUnidade;
import sgc.unidade.modelo.TipoUnidade;
import sgc.unidade.modelo.Unidade;
import sgc.unidade.modelo.UnidadeRepo;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CDU04IntegrationTest {
    @TestConfiguration
    @SuppressWarnings("PMD.TestClassWithoutTestCases")
    static class TestSecurityConfig {
        @Bean
        SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
            http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                    .csrf(AbstractHttpConfigurer::disable);
            return http.build();
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProcessoRepo processoRepo;

    @Autowired
    private UnidadeRepo unidadeRepo;

    @Autowired
    private SubprocessoRepo subprocessoRepo;

    @Autowired
    private MovimentacaoRepo movimentacaoRepo;

    @Autowired
    private AlertaRepo alertaRepo;

    @Autowired
    private UnidadeProcessoRepo unidadeProcessoRepo;

    @Autowired
    private EntityManager entityManager;

    @MockitoBean
    private SgrhService sgrhService;

    @MockitoBean
    private NotificacaoServico notificacaoServico;

    private Processo processo;
    private Unidade unidadeIntermediaria, unidadeOperacional, unidadeInteroperacional;

    @BeforeEach
    void setUp() {
        when(sgrhService.buscarUnidadePorCodigo(anyLong())).thenAnswer(i -> {
            Long id = i.getArgument(0);
            Unidade u = unidadeRepo.findById(id).orElseThrow();
            return Optional.of(new UnidadeDto(u.getCodigo(),
                    u.getNome(),
                    u.getSigla(),
                    u.getUnidadeSuperior() != null ? u.getUnidadeSuperior().getCodigo() : null,
                    u.getTipo().name()));
        });

        when(sgrhService.buscarResponsavelUnidade(anyLong())).thenAnswer(
            i -> Optional.of(
                new ResponsavelDto(
                    i.getArgument(0),
                    "T000000",
                    "Titular Fulano",
                    null,
                    null
                )
            )
        );

        when(sgrhService.buscarUsuarioPorTitulo(anyString()))
                .thenReturn(Optional.of(new UsuarioDto("T000000",
                        "Fulano de Tal",
                        "fulano@tre.jus.br",
                        "12345",
                        "Analista")));

        doNothing().when(notificacaoServico).enviarEmailHtml(anyString(), anyString(), anyString());

        unidadeIntermediaria = unidadeRepo.save(new Unidade("Unidade Intermediária",
                "UINT",
                null,
                TipoUnidade.INTERMEDIARIA,
                SituacaoUnidade.ATIVA,
                null));

        unidadeOperacional = unidadeRepo.save(new Unidade("Unidade Operacional",
                "UOP",
                null,
                TipoUnidade.OPERACIONAL,
                SituacaoUnidade.ATIVA,
                unidadeIntermediaria));

        unidadeInteroperacional = unidadeRepo.save(new Unidade("Unidade Interoperacional",
                "UINTER",
                null,
                TipoUnidade.INTEROPERACIONAL,
                SituacaoUnidade.ATIVA,
                unidadeIntermediaria));

        processo = new Processo();
        processo.setDescricao("Processo de Mapeamento Teste");
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        processo.setSituacao(SituacaoProcesso.CRIADO);
        processo.setDataLimite(LocalDate.now().plusDays(30));

        processo = processoRepo.save(processo);
    }

    @Test
    @DisplayName("CDU-04: Deve iniciar processo, criar subprocessos, alertas e movimentações corretamente")
    @WithMockUser(roles = "ADMIN")
    void iniciarProcesso_ComUnidadesDiversas_DeveRealizarTodasAsAcoesCorretamente() throws Exception {
        List<Long> codigosUnidades = List.of(
                unidadeIntermediaria.getCodigo(),
                unidadeOperacional.getCodigo(),
                unidadeInteroperacional.getCodigo()
        );

        Long codProcesso = processo.getCodigo();
        mockMvc.perform(
            post("/api/processos/{id}/iniciar", codProcesso)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(codigosUnidades))
        ).andExpect(status().isOk());

        entityManager.flush();
        entityManager.clear();

        Processo processoIniciado = processoRepo.findById(codProcesso).orElseThrow();
        assertThat(processoIniciado.getSituacao()).isEqualTo(SituacaoProcesso.EM_ANDAMENTO);

        List<UnidadeProcesso> snapshots = unidadeProcessoRepo.findByProcessoCodigo(codProcesso);
        assertThat(snapshots).hasSize(3);

        List<Subprocesso> subprocessos = subprocessoRepo.findByProcessoCodigoWithUnidade(codProcesso);
        assertThat(subprocessos).hasSize(2);
        assertThat(subprocessos).extracting(s -> s.getUnidade().getSigla()).containsExactlyInAnyOrder("UOP", "UINTER");

        subprocessos.forEach(sp -> {
            assertThat(sp.getMapa()).isNotNull();
            assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.NAO_INICIADO);
        });

        List<Movimentacao> movimentacoes = movimentacaoRepo.findAll().stream()
                .filter(m -> m.getSubprocesso().getProcesso().getCodigo().equals(codProcesso))
                .collect(Collectors.toList());

        assertThat(movimentacoes).hasSize(2);
        movimentacoes.forEach(m -> {
            assertThat(m.getDescricao()).isEqualTo("Processo iniciado");
            assertThat(m.getUnidadeOrigem()).isNull();
            assertThat(m.getUnidadeDestino()).isNotNull();
        });

        List<Alerta> alertas = alertaRepo.findAll().stream()
                .filter(a -> a.getProcesso().getCodigo().equals(codProcesso))
                .collect(Collectors.toList());

        assertThat(alertas).hasSize(4);
        assertThat(alertas.stream().filter(a -> a.getUnidadeDestino().getCodigo().equals(unidadeOperacional.getCodigo()))).hasSize(1);
        assertThat(
            alertas.stream().filter(a -> a.getUnidadeDestino().getCodigo().equals(unidadeInteroperacional.getCodigo())
                && a.getDescricao().contains("Início do processo") && !a.getDescricao().contains("subordinada"))
        ).hasSize(1);
    }
}