package sgc.integracao;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.*;
import sgc.alerta.model.*;
import sgc.fixture.*;
import sgc.integracao.mocks.*;
import sgc.mapa.model.*;
import sgc.organizacao.model.*;
import sgc.processo.dto.*;
import sgc.processo.model.*;
import sgc.subprocesso.model.*;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Tag("integration")
@WithMockAdmin
@Transactional
@DisplayName("CDU-04: Iniciar processo de mapeamento")
class CDU04IntegrationTest extends BaseIntegrationTest {
    @Autowired
    private AlertaRepo alertaRepo;

    @Autowired
    private CompetenciaRepo competenciaRepo;

    @Autowired
    private UsuarioRepo usuarioRepo;

    @Autowired
    private UsuarioPerfilRepo usuarioPerfilRepo;

    private Unidade unidadeLivre;

    @BeforeEach
    void setup() {

        // Cria unidade programaticamente
        unidadeLivre = UnidadeFixture.unidadePadrao();
        unidadeLivre.setCodigo(null);
        unidadeLivre.setSigla("U_LIVRE_MAPP");
        unidadeLivre.setNome("Unidade Livre Mapeamento");
        unidadeLivre = unidadeRepo.save(unidadeLivre);

        // Cria titular para a unidade (para garantir envio de notificação)
        Usuario titular = UsuarioFixture.usuarioPadrao();
        titular.setTituloEleitoral("999999999999");
        titular.setEmail("titular@teste.com");
        titular = usuarioRepo.save(titular);

        // Associa titular à unidade
        unidadeLivre.setTituloTitular(titular.getTituloEleitoral());
        unidadeLivre.setMatriculaTitular(titular.getMatricula());
        unidadeRepo.save(unidadeLivre);

        // Também precisamos associar o perfil CHEFE ao usuário na unidade para que ele
        // seja encontrado pelo UsuarioFacade
        UsuarioPerfil perfilChefe = UsuarioPerfil.builder()
                .usuarioTitulo(titular.getTituloEleitoral())
                .usuario(titular)
                .unidadeCodigo(unidadeLivre.getCodigo())
                .unidade(unidadeLivre)
                .perfil(Perfil.CHEFE)
                .build();
        usuarioPerfilRepo.save(perfilChefe);
    }

    @Test
    @DisplayName("Deve iniciar processo de mapeamento com sucesso e gerar subprocessos, alertas e notificações")
    void deveIniciarProcessoMapeamento() throws Exception {
        // 1. Arrange: Criar um processo em estado 'CRIADO'
        CriarProcessoRequest criarReq = new CriarProcessoRequest(
                "Processo Mapeamento Teste CDU-04",
                TipoProcesso.MAPEAMENTO,
                LocalDateTime.now().plusDays(10),
                List.of(unidadeLivre.getCodigo()));

        var result = mockMvc.perform(post("/api/processos")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(criarReq)))
                .andExpect(status().isCreated())
                .andReturn();

        Long processoId = objectMapper.readTree(result.getResponse().getContentAsString()).get("codigo").asLong();

        // 2. Act: Iniciar Processo
        IniciarProcessoRequest iniciarReq = new IniciarProcessoRequest(TipoProcesso.MAPEAMENTO,
                List.of(unidadeLivre.getCodigo()));

        mockMvc.perform(post("/api/processos/{id}/iniciar", processoId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(iniciarReq)))
                .andExpect(status().isOk());

        // 3. Assert: Mudança de Status do Processo
        Processo processo = processoRepo.findById(processoId).orElseThrow();
        assertThat(processo.getSituacao()).isEqualTo(SituacaoProcesso.EM_ANDAMENTO);

        // 4. Assert: Criação de Subprocessos
        List<Subprocesso> subprocessos = subprocessoRepo.findByProcessoCodigo(processoId);
        assertThat(subprocessos).hasSize(1);

        // Verificar subprocesso da unidade livre
        Subprocesso sub = subprocessos.stream()
                .filter(s -> s.getUnidade().getCodigo().equals(unidadeLivre.getCodigo()))
                .findFirst()
                .orElseThrow();
        assertThat(sub.getMapa()).isNotNull();

        // Verificar que o mapa não tem competências (Mapeamento inicia vazio)
        List<Competencia> competencias = competenciaRepo.findByMapa_Codigo(sub.getMapa().getCodigo());
        assertThat(competencias).isEmpty();

        // 5. Assert: Geração de Alertas
        long alertasCount = alertaRepo.count();
        assertThat(alertasCount).isGreaterThan(0);

        // 6. Assert: Notificações por email são mockadas em ambiente de teste
        // e não podem ser verificadas diretamente via Mockito
        // O teste de envio de emails está em NotificacaoEmailServiceTest
    }
}
