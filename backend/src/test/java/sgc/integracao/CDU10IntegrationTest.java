package sgc.integracao;

import jakarta.persistence.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.core.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.*;
import org.springframework.security.core.authority.*;
import org.springframework.security.core.context.*;
import org.springframework.transaction.annotation.*;
import sgc.fixture.*;
import sgc.integracao.mocks.*;
import sgc.mapa.model.*;
import sgc.organizacao.model.*;
import sgc.processo.model.*;
import sgc.subprocesso.model.*;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Tag("integration")
@Import({
        WithMockChefeSecurityContextFactory.class,
})
@Transactional
@DisplayName("CDU-10: Disponibilizar Revisão do Cadastro de Atividades e Conhecimentos")
class CDU10IntegrationTest extends BaseIntegrationTest {
    @Autowired
    private ConhecimentoRepo conhecimentoRepo;
    @Autowired
    private CompetenciaRepo competenciaRepo;
    @Autowired
    private AnaliseRepo analiseRepo;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private UsuarioRepo usuarioRepo;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private MovimentacaoRepo movimentacaoRepo;


    private Unidade unidadeSuperior;
    private Subprocesso subprocessoRevisao;

    @BeforeEach
    void setUp() {

        // 1. Criar Unidades (Superior e Chefe)
        unidadeSuperior = UnidadeFixture.unidadeComSigla("COSIS");
        unidadeSuperior.setCodigo(null);
        unidadeSuperior = unidadeRepo.save(unidadeSuperior);

        Unidade unidadeChefe = UnidadeFixture.unidadeComSigla("SESEL");
        unidadeChefe.setCodigo(null);
        unidadeChefe.setUnidadeSuperior(unidadeSuperior);
        unidadeChefe = unidadeRepo.save(unidadeChefe);

        // 2. Criar Usuários
        Usuario usuarioChefe = UsuarioFixture.usuarioComTitulo("333333333333");
        usuarioChefe.setUnidadeLotacao(unidadeChefe);
        usuarioChefe = usuarioRepo.saveAndFlush(usuarioChefe);

        Usuario usuarioSuperior = UsuarioFixture.usuarioComTitulo("666666666666");
        usuarioSuperior.setUnidadeLotacao(unidadeSuperior);
        usuarioSuperior = usuarioRepo.saveAndFlush(usuarioSuperior);

        // 3. Configurar Perfil e Titularidade
        setupUsuarioPerfil(usuarioChefe, unidadeChefe, Perfil.CHEFE);
        definirTitular(unidadeChefe, usuarioChefe);
        definirTitular(unidadeSuperior, usuarioSuperior);

        // 4. Criar Processo, Mapa e Subprocesso
        Processo processoRevisao = ProcessoFixture.processoPadrao();
        processoRevisao.setCodigo(null);
        processoRevisao.setTipo(TipoProcesso.REVISAO);
        processoRevisao.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        processoRevisao.setDataLimite(LocalDateTime.now().plusDays(30));
        processoRevisao = processoRepo.save(processoRevisao);

        var mapa = mapaRepo.save(new Mapa());

        subprocessoRevisao = SubprocessoFixture.subprocessoPadrao(processoRevisao, unidadeChefe);
        subprocessoRevisao.setCodigo(null);
        subprocessoRevisao.setSituacaoForcada(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);
        subprocessoRevisao.setDataLimiteEtapa1(processoRevisao.getDataLimite());
        subprocessoRevisao = subprocessoRepo.save(subprocessoRevisao);

        mapa.setSubprocesso(subprocessoRevisao);
        mapa = mapaRepo.save(mapa);
        subprocessoRevisao.setMapa(mapa);

        // Garante que existe movimentação para a unidade do Chefe
        Movimentacao mov = Movimentacao.builder()
                .subprocesso(subprocessoRevisao)
                .unidadeOrigem(unidadeChefe)
                .unidadeDestino(unidadeChefe)
                .descricao("Inicialização")
                .dataHora(LocalDateTime.now())
                .build();
        movimentacaoRepo.save(mov);

        // 5. Autenticar
        autenticarUsuario(usuarioChefe, Perfil.CHEFE);
    }

    private void setupUsuarioPerfil(Usuario usuario, Unidade unidade, Perfil perfil) {
        jdbcTemplate.update(
                "INSERT INTO SGC.VW_USUARIO_PERFIL_UNIDADE (usuario_titulo, unidade_codigo, perfil) VALUES (?, ?, ?)",
                usuario.getTituloEleitoral(), unidade.getCodigo(), perfil.name());

        // Agora o perfil é configurado na sessão ativa via autenticarUsuario
    }

    private void definirTitular(Unidade unidade, Usuario usuario) {
        jdbcTemplate.update("UPDATE SGC.VW_UNIDADE SET titulo_titular = ? WHERE codigo = ?",
                usuario.getTituloEleitoral(), unidade.getCodigo());

        jdbcTemplate.update("INSERT INTO SGC.VW_RESPONSABILIDADE (unidade_codigo, usuario_titulo, usuario_matricula, tipo, data_inicio) VALUES (?, ?, ?, 'TITULAR', ?)",
                unidade.getCodigo(), usuario.getTituloEleitoral(), usuario.getMatricula(), LocalDateTime.now());

        unidade.setTituloTitular(usuario.getTituloEleitoral());
        unidade.setMatriculaTitular(usuario.getMatricula());
    }

    private void autenticarUsuario(Usuario usuario, Perfil perfil) {
        usuario.setPerfilAtivo(perfil);
        // Tenta pegar o código da unidade do usuário ou da unidade de lotação
        Long codUnidade = usuario.getUnidadeLotacao() != null ? usuario.getUnidadeLotacao().getCodigo() : 1L;
        usuario.setUnidadeAtivaCodigo(codUnidade);

        usuario.setAuthorities(Set.of(new SimpleGrantedAuthority("ROLE_" + perfil.name())));
        Authentication auth = new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("Não deve permitir que um CHEFE de outra unidade disponibilize a revisão")
    void naoDevePermitirChefeDeOutraUnidadeDisponibilizar() throws Exception {
        // Autenticar com outro usuário
        Usuario outroChefe = UsuarioFixture.usuarioComTitulo("999999999999");
        outroChefe = usuarioRepo.save(outroChefe);

        Unidade outraUnidade = UnidadeFixture.unidadeComSigla("OUTRA");
        outraUnidade.setCodigo(null);
        outraUnidade = unidadeRepo.save(outraUnidade);

        setupUsuarioPerfil(outroChefe, outraUnidade, Perfil.CHEFE);
        autenticarUsuario(outroChefe, Perfil.CHEFE);

        mockMvc.perform(post("/api/subprocessos/{id}/disponibilizar-revisao", subprocessoRevisao.getCodigo()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Deve excluir histórico de análises anteriores quando disponibilizar revisão novamente")
    void deveExcluirHistoricoAposNovaDisponibilizacao() throws Exception {
        // Preparar: criar atividade com conhecimento para permitir disponibilização
        var competencia = competenciaRepo.save(CompetenciaFixture.competenciaPadrao(subprocessoRevisao.getMapa()));
        var atividade = AtividadeFixture.atividadePadrao(subprocessoRevisao.getMapa());

        atividade.getCompetencias().add(competencia);
        atividade = atividadeRepo.save(atividade);
        competencia.getAtividades().add(atividade);
        competenciaRepo.save(competencia);

        conhecimentoRepo.save(Conhecimento.builder().descricao("Conhecimento de Teste")
                .atividade(atividade).build());

        entityManager.flush();
        entityManager.clear();

        Long subprocessoId = subprocessoRevisao.getCodigo();

        // 1. Primeira disponibilização
        mockMvc.perform(post("/api/subprocessos/{id}/disponibilizar-revisao", subprocessoId))
                .andExpect(status().isOk());

        // 2. Criar análise de "Aceite" manualmente
        Analise analiseAceite = new Analise();
        analiseAceite.setSubprocesso(subprocessoRevisao);
        analiseAceite.setUnidadeCodigo(unidadeSuperior.getCodigo());
        analiseAceite.setAcao(TipoAcaoAnalise.ACEITE_REVISAO);
        analiseAceite.setDataHora(LocalDateTime.now());
        analiseRepo.saveAndFlush(analiseAceite);

        // 3. Criar análise de "Devolução" manualmente
        Analise analiseDevolucao = new Analise();
        analiseDevolucao.setSubprocesso(subprocessoRevisao);
        analiseDevolucao.setUnidadeCodigo(unidadeSuperior.getCodigo());
        analiseDevolucao.setAcao(TipoAcaoAnalise.DEVOLUCAO_REVISAO);
        analiseDevolucao.setObservacoes("Segunda devolução");
        analiseDevolucao.setDataHora(LocalDateTime.now());
        analiseRepo.saveAndFlush(analiseDevolucao);

        // 4. Simular devolução
        Subprocesso sp = subprocessoRepo.findById(subprocessoId).get();
        sp.setSituacaoForcada(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);
        sp.setDataFimEtapa1(null);
        subprocessoRepo.saveAndFlush(sp);

        Movimentacao mov = new Movimentacao();
        mov.setSubprocesso(sp);
        mov.setUnidadeOrigem(unidadeSuperior);
        mov.setUnidadeDestino(sp.getUnidade());
        mov.setDataHora(LocalDateTime.now().plusSeconds(1)); // Ensure it's later
        mov.setDescricao("Devolução simulada");
        // Usuario Superior
        Usuario usuarioSuperior = usuarioRepo.findById("666666666666").get();
        mov.setUsuario(usuarioSuperior);
        movimentacaoRepo.saveAndFlush(mov);

        entityManager.flush();
        entityManager.clear();

        // 5. Nova disponibilização
        mockMvc.perform(post("/api/subprocessos/{id}/disponibilizar-revisao", subprocessoId))
                .andExpect(status().isOk());

        // 6. Verificar que histórico foi excluído
        List<Analise> analisesDepois = analiseRepo.findBySubprocessoCodigoOrderByDataHoraDesc(subprocessoId);
        assertThat(analisesDepois).isEmpty();
    }
}
