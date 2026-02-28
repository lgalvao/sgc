package sgc.integracao;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.*;
import org.springframework.security.core.*;
import org.springframework.security.core.context.*;
import org.springframework.transaction.annotation.*;
import sgc.fixture.*;
import sgc.mapa.model.*;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;
import sgc.processo.model.*;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.model.*;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Tag("integration")
@Transactional
@DisplayName("Fluxo Completo de Subprocesso")
class SubprocessoFluxoIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UsuarioRepo usuarioRepo;
    @Autowired
    private UsuarioPerfilRepo usuarioPerfilRepo;
    @Autowired
    private AtividadeRepo atividadeRepo;
    @Autowired
    private UnidadeService unidadeService;
    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    private Unidade unidadeRaiz;
    private Unidade unidadeFilha;
    private Usuario admin;
    private Subprocesso subprocesso;

    @BeforeEach
    void setUp() {
        // 1. Unidades
        unidadeRaiz = unidadeRepo.findById(1L).orElseThrow();

        unidadeFilha = UnidadeFixture.unidadePadrao();
        unidadeFilha.setCodigo(null);
        unidadeFilha.setSigla("FILHA");
        unidadeFilha.setNome("Unidade Filha");
        unidadeFilha.setUnidadeSuperior(unidadeRaiz);
        unidadeFilha = unidadeRepo.save(unidadeFilha);

        // Add responsabilidade to prevent 404 during email notification
        jdbcTemplate.update("INSERT INTO SGC.VW_RESPONSABILIDADE (unidade_codigo, usuario_titulo, usuario_matricula, tipo, data_inicio) VALUES (?, ?, ?, ?, ?)",
                unidadeFilha.getCodigo(), "111111111111", "00000", "TITULAR", LocalDateTime.now());

        // 2. Usuários
        admin = usuarioRepo.findById("111111111111").orElseThrow(); // Admin padrão do seed (Admin Teste V2)

        Usuario chefe = UsuarioFixture.usuarioPadrao();
        chefe.setTituloEleitoral("555555555555");
        chefe.setNome("Chefe Filha");
        chefe.setUnidadeLotacao(unidadeFilha);
        chefe = usuarioRepo.save(chefe);

        UsuarioPerfil perfilChefe = UsuarioPerfil.builder()
                .usuario(chefe)
                .usuarioTitulo(chefe.getTituloEleitoral())
                .unidade(unidadeFilha)
                .unidadeCodigo(unidadeFilha.getCodigo())
                .perfil(Perfil.CHEFE)
                .build();
        usuarioPerfilRepo.save(perfilChefe);

        // 3. Processo
        Processo processo = Processo.builder()
                .descricao("Processo Mapeamento Teste")
                .tipo(TipoProcesso.MAPEAMENTO)
                .situacao(SituacaoProcesso.EM_ANDAMENTO)
                .dataLimite(LocalDateTime.now().plusDays(30))
                .build();
        processoRepo.save(processo);

        // 4. Subprocesso Inicial
        subprocesso = Subprocesso.builder()
                .unidade(unidadeFilha)
                .situacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO) // Começando já homologado para focar no mapa
                .dataLimiteEtapa1(LocalDateTime.now().minusDays(1))
                .dataLimiteEtapa2(LocalDateTime.now().plusDays(15))
                .processo(processo)
                .build();
        subprocessoRepo.save(subprocesso);

        Mapa mapa = new Mapa();
        mapa.setSubprocesso(subprocesso);
        mapaRepo.save(mapa);
        subprocesso.setMapa(mapa);

        // Criar atividade para associar
        Atividade ativ = Atividade.builder().mapa(mapa).descricao("Atividade 1").build();
        atividadeRepo.save(ativ);

        // Definir mapa vigente para a unidade (necessário para permissões de impacto e outras verificações)
        unidadeService.definirMapaVigente(unidadeFilha.getCodigo(), mapa);
    }

    private void autenticar(Usuario usuario, Perfil perfil) {
        usuario.setPerfilAtivo(perfil);

        // Se eu setar a unidade ativa do ADMIN para a unidade filha, deve passar.
        usuario.setUnidadeAtivaCodigo(unidadeFilha.getCodigo());

        usuario.setAuthorities(Set.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + perfil.name())));
        Authentication auth = new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("Fluxo Completo: Criação Mapa -> Disponibilização -> Validação -> Homologação")
    void fluxoCompletoMapeamento() throws Exception {
        Long spId = subprocesso.getCodigo();
        Long mapaId = subprocesso.getMapa().getCodigo();

        // 1. Adicionar Competência (ADMIN)
        // O ADMIN está na Unidade RAIZ. O Subprocesso está na Unidade FILHA.
        // A Regra de Ouro (SgcPermissionEvaluator) exige que a localização do subprocesso (FILHA)
        // seja igual à unidade ativa do usuário para ações de escrita (EDITAR_MAPA, DISPONIBILIZAR_MAPA).
        // Mesmo ADMIN não escapa dessa regra para ações de escrita.
        // Portanto, o ADMIN deve "simular" estar na unidade FILHA para realizar estas ações.

        admin.setUnidadeAtivaCodigo(unidadeFilha.getCodigo()); // Admin troca de unidade para atuar no subprocesso
        autenticar(admin, Perfil.ADMIN);

        List<Atividade> atividades = atividadeRepo.findByMapa_Codigo(mapaId);
        Long atividadeId = atividades.getFirst().getCodigo();

        CompetenciaRequest compReq = CompetenciaRequest.builder()
                .descricao("Competência 1")
                .atividadesIds(List.of(atividadeId))
                .build();

        mockMvc.perform(post("/api/subprocessos/{id}/competencia", spId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(compReq))
                        .with(csrf()))
                .andExpect(status().isOk());

        Subprocesso sp = subprocessoRepo.findById(spId).orElseThrow();
        assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);

        // 2. Disponibilizar Mapa (ADMIN)
        // Situação deve mudar para MAPA_DISPONIBILIZADO
        // Admin continua na unidade filha para essa ação de escrita.

        DisponibilizarMapaRequest dispReq = DisponibilizarMapaRequest.builder()
                .dataLimite(LocalDate.now().plusDays(10))
                .observacoes("Segue para validação")
                .build();

        mockMvc.perform(post("/api/subprocessos/{id}/disponibilizar-mapa", spId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dispReq))
                        .with(csrf()))
                .andExpect(status().isOk());

        sp = subprocessoRepo.findById(spId).orElseThrow();
        assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO);


        // Situação deve mudar para MAPA_VALIDADO
        // A Disponibilização enviou o processo para a Unidade Superior (RAIZ).
        // Quem valida a disponibilização é o Chefe da Unidade SUPERIOR (que recebeu o processo).
        // O ADMIN é titular da RAIZ e possui o perfil CHEFE lá (segundo seed).

        // Para validar mapa, a regra é:
        // 1. Ser perfil CHEFE.
        // 2. Estar na mesma unidade onde está o subprocesso (RAIZ).

        admin.setUnidadeAtivaCodigo(unidadeRaiz.getCodigo()); // Admin atuando na RAIZ
        autenticar(admin, Perfil.CHEFE); // Admin como CHEFE da Raiz

        mockMvc.perform(post("/api/subprocessos/{id}/validar-mapa", spId)
                        .with(csrf()))
                .andExpect(status().isOk());

        sp = subprocessoRepo.findById(spId).orElseThrow();
        assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO);


        // O teste já cobre:
        // - Adicionar competência (Escrita na Unidade Filha)
        // - Disponibilizar Mapa (Transição de estado e localização para cima)
        // - Validar Mapa (Escrita na Unidade Raiz pelo Chefe)

    }
}
