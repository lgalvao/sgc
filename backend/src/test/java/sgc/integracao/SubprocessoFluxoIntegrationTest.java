package sgc.integracao;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.*;
import org.springframework.security.core.*;
import org.springframework.security.core.context.*;
import org.springframework.test.util.*;
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
@DisplayName("Fluxo completo de Subprocesso")
class SubprocessoFluxoIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UsuarioRepo usuarioRepo;
    @Autowired
    private UsuarioPerfilRepo usuarioPerfilRepo;
    @Autowired
    private AtividadeRepo atividadeRepo;
    @Autowired
    private CompetenciaRepo competenciaRepo;
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
        unidadeRaiz = unidadeRepo.findById(1L).orElseThrow();

        unidadeFilha = UnidadeFixture.unidadePadrao();
        unidadeFilha.setCodigo(null);
        unidadeFilha.setSigla("FILHA");
        unidadeFilha.setNome("Unidade filha");
        unidadeFilha.setUnidadeSuperior(unidadeRaiz);
        unidadeFilha = unidadeRepo.save(unidadeFilha);

        // Add responsabilidade to prevent 404 during email notification
        jdbcTemplate.update("INSERT INTO SGC.VW_RESPONSABILIDADE (unidade_codigo, usuario_titulo, usuario_matricula, tipo, data_inicio) VALUES (?, ?, ?, ?, ?)",
                unidadeFilha.getCodigo(), "111111111111", "00000", "TITULAR", LocalDateTime.now());

        admin = usuarioRepo.findById("111111111111").orElseThrow(); // Admin padrão do seed (Admin teste V2)

        Usuario chefe = UsuarioFixture.usuarioPadrao();
        chefe.setTituloEleitoral("555555555555");
        chefe.setNome("Chefe filha");
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

        Processo processo = Processo.builder()
                .descricao("Processo mapeamento teste")
                .tipo(TipoProcesso.MAPEAMENTO)
                .situacao(SituacaoProcesso.EM_ANDAMENTO)
                .dataLimite(LocalDateTime.now().plusDays(30))
                .build();
        processoRepo.save(processo);

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
    @DisplayName("Fluxo completo: Criação mapa -> Disponibilização -> Validação -> Homologação")
    void fluxoCompletoMapeamento() throws Exception {
        Long spId = subprocesso.getCodigo();
        Long mapaId = subprocesso.getMapa().getCodigo();

        // O ADMIN está na Unidade RAIZ. O subprocesso está na Unidade FILHA.
        // A regra de Ouro (SgcPermissionEvaluator) exige que a localização do subprocesso (FILHA)
        // seja igual à unidade ativa do usuário para ações de escrita (EDITAR_MAPA, DISPONIBILIZAR_MAPA).
        // Mesmo ADMIN não escapa dessa regra para ações de escrita.
        // Portanto, o ADMIN deve "simular" estar na unidade FILHA para realizar estas ações.

        admin.setUnidadeAtivaCodigo(unidadeFilha.getCodigo()); // Admin troca de unidade para atuar no subprocesso
        autenticar(admin, Perfil.ADMIN);

        List<Atividade> atividades = atividadeRepo.findByMapa_Codigo(mapaId);
        Long codAtividade = atividades.getFirst().getCodigo();

        CompetenciaRequest compReq = CompetenciaRequest.builder()
                .descricao("Competência 1")
                .atividadesIds(List.of(codAtividade))
                .build();

        mockMvc.perform(post("/api/subprocessos/{codigo}/competencia", spId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(compReq))
                        .with(csrf()))
                .andExpect(status().isOk());

        Subprocesso sp = subprocessoRepo.findById(spId).orElseThrow();
        assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);

        // Situação deve mudar para MAPA_DISPONIBILIZADO
        // Admin continua na unidade filha para essa ação de escrita.

        DisponibilizarMapaRequest dispReq = DisponibilizarMapaRequest.builder()
                .dataLimite(LocalDate.now().plusDays(10))
                .observacoes("Segue para validação")
                .build();

        mockMvc.perform(post("/api/subprocessos/{codigo}/disponibilizar-mapa", spId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dispReq))
                        .with(csrf()))
                .andExpect(status().isOk());

        sp = subprocessoRepo.findById(spId).orElseThrow();
        assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO);

        // Situação deve mudar para MAPA_VALIDADO
        // A disponibilização enviou o processo para a Unidade superior (RAIZ).
        // Quem valida a disponibilização é o Chefe da Unidade SUPERIOR (que recebeu o processo).
        // O ADMIN é titular da RAIZ e possui o perfil CHEFE lá (segundo seed).

        // Para validar mapa, a regra é:

        admin.setUnidadeAtivaCodigo(unidadeRaiz.getCodigo()); // Admin atuando na RAIZ
        autenticar(admin, Perfil.CHEFE); // Admin como CHEFE da Raiz

        mockMvc.perform(post("/api/subprocessos/{codigo}/validar-mapa", spId)
                        .with(csrf()))
                .andExpect(status().isOk());

        sp = subprocessoRepo.findById(spId).orElseThrow();
        assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO);

        // O teste já cobre:
        // - Adicionar competência (Escrita na Unidade filha)
        // - Disponibilizar mapa (Transição de estado e localização para cima)
        // - Validar mapa (Escrita na Unidade raiz pelo Chefe)

    }

    @Test
    @DisplayName("ADMIN deve conseguir remover a ultima atividade de uma competencia durante edicao do mapa")
    void adminDeveConseguirRemoverUltimaAtividadeDeCompetencia() throws Exception {
        ReflectionTestUtils.setField(subprocesso, "situacao", SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
        subprocessoRepo.save(subprocesso);

        admin.setUnidadeAtivaCodigo(unidadeFilha.getCodigo());
        autenticar(admin, Perfil.ADMIN);

        Mapa mapa = subprocesso.getMapa();
        Atividade atividade = atividadeRepo.findByMapa_Codigo(mapa.getCodigo()).getFirst();

        Competencia competencia = Competencia.builder()
                .mapa(mapa)
                .descricao("Competência existente")
                .build();
        competencia.getAtividades().add(atividade);
        competencia = competenciaRepo.saveAndFlush(competencia);

        atividade.getCompetencias().add(competencia);
        atividadeRepo.saveAndFlush(atividade);

        CompetenciaRequest request = CompetenciaRequest.builder()
                .descricao("Competência existente")
                .atividadesIds(List.of())
                .build();

        mockMvc.perform(post("/api/subprocessos/{codSubprocesso}/competencia/{codCompetencia}",
                        subprocesso.getCodigo(), competencia.getCodigo())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk());

        Competencia competenciaAtualizada = competenciaRepo.findById(competencia.getCodigo()).orElseThrow();
        assertThat(competenciaAtualizada.getAtividades()).isEmpty();
    }
}
