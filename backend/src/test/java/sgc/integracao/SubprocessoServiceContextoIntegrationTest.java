package sgc.integracao;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.authority.*;
import org.springframework.security.core.context.*;
import org.springframework.transaction.annotation.*;
import sgc.mapa.dto.*;
import sgc.mapa.model.*;
import sgc.mapa.service.*;
import sgc.organizacao.model.*;
import sgc.processo.model.*;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.model.*;
import sgc.subprocesso.service.*;

import java.time.*;
import java.util.*;
import java.util.stream.*;

import static org.assertj.core.api.Assertions.*;

@Tag("integration")
@Transactional
@DisplayName("Integração: SubprocessoService - Obtenção de Contexto/Detalhes")
class SubprocessoServiceContextoIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private SubprocessoConsultaService consultaService;

    @Autowired
    private SubprocessoService subprocessoService;

    @Autowired
    private UsuarioRepo usuarioRepo;

    @Autowired
    private MapaManutencaoService mapaManutencaoService;

    @Autowired
    private UnidadeMapaRepo unidadeMapaRepo;

    @Autowired
    private ConhecimentoRepo conhecimentoRepo;

    @Autowired
    private MovimentacaoRepo movimentacaoRepo;

    private Usuario admin;
    private Subprocesso subprocesso;
    private Processo processo;

    @BeforeEach
    void setUp() {
        // Unidade 8 (SEDESENV) has titular '3' (00000003) in data.sql
        Unidade unidade = unidadeRepo.findById(8L).orElseThrow();

        admin = usuarioRepo.findById("3").orElseThrow();
        admin.setUnidadeAtivaCodigo(unidade.getCodigo());
        admin.setPerfilAtivo(Perfil.ADMIN);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(admin, null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
        );

        processo = Processo.builder()
                .descricao("Processo teste contexto")
                .tipo(TipoProcesso.MAPEAMENTO)
                .situacao(SituacaoProcesso.EM_ANDAMENTO)
                .dataLimite(LocalDateTime.now().plusDays(30))
                .build();
        processoRepo.save(processo);

        subprocesso = Subprocesso.builder()
                .unidade(unidade)
                .situacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO)
                .processo(processo)
                .dataLimiteEtapa1(LocalDateTime.now().plusDays(20))
                .build();
        subprocessoRepo.save(subprocesso);

        movimentacaoRepo.save(Movimentacao.builder()
                .subprocesso(subprocesso)
                .unidadeOrigem(unidade)
                .unidadeDestino(unidade)
                .usuario(admin)
                .descricao("Movimentação inicial de teste")
                .build());

        Mapa mapa = new Mapa();
        mapa.setSubprocesso(subprocesso);
        mapaRepo.save(mapa);
        subprocesso.setMapa(mapa);
    }

    @Test
    @DisplayName("obterDetalhes: Deve obter os detalhes de um subprocesso")
    void obterDetalhes_Sucesso() {
        SubprocessoDetalheResponse detalhes = consultaService.obterDetalhes(subprocesso.getCodigo());

        assertThat(detalhes).isNotNull();
        assertThat(detalhes.subprocesso().codigo()).isEqualTo(subprocesso.getCodigo());
        assertThat(detalhes.permissoes()).isNotNull();
    }

    @Test
    @DisplayName("obterContextoEdicao: Deve obter o contexto completo para edição")
    void obterContextoEdicao_Sucesso() {
        ContextoEdicaoResponse contexto = consultaService.obterContextoEdicao(subprocesso.getCodigo());

        assertThat(contexto).isNotNull();
        assertThat(contexto.subprocesso().codigo()).isEqualTo(subprocesso.getCodigo());
        assertThat(contexto.detalhes()).isNotNull();
        assertThat(contexto.mapa()).isNotNull();
        assertThat(contexto.mapa().atividades()).isNotNull();
    }

    @Test
    @DisplayName("obterContextoEdicao: Deve corrigir situacao para cadastro em andamento quando houver atividades")
    void obterContextoEdicao_DeveReconciliarSituacaoComAtividades() {
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.NAO_INICIADO);
        subprocessoRepo.saveAndFlush(subprocesso);
        mapaManutencaoService.criarAtividade(new CriarAtividadeRequest(subprocesso.getMapa().getCodigo(), "Atividade existente"));

        ContextoEdicaoResponse contexto = consultaService.obterContextoEdicao(subprocesso.getCodigo());

        assertThat(contexto.detalhes().subprocesso().situacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO.name());
        assertThat(consultaService.buscarSubprocesso(subprocesso.getCodigo()).getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
    }

    @Test
    @DisplayName("obterContextoEdicao: Nao deve promover revisao para em andamento apenas por haver atividades copiadas")
    void obterContextoEdicao_NaoDevePromoverRevisaoComAtividadesCopiadas() {
        processo.setTipo(TipoProcesso.REVISAO);
        processoRepo.saveAndFlush(processo);
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.NAO_INICIADO);
        subprocessoRepo.saveAndFlush(subprocesso);
        Atividade atividade = Atividade.builder()
                .descricao("Atividade copiada")
                .mapa(subprocesso.getMapa())
                .build();
        atividadeRepo.saveAndFlush(atividade);

        ContextoEdicaoResponse contexto = consultaService.obterContextoEdicao(subprocesso.getCodigo());

        assertThat(contexto.detalhes().subprocesso().situacao()).isEqualTo(SituacaoSubprocesso.NAO_INICIADO.name());
        assertThat(consultaService.buscarSubprocesso(subprocesso.getCodigo()).getSituacao()).isEqualTo(SituacaoSubprocesso.NAO_INICIADO);
    }

    @Test
    @DisplayName("obterContextoCadastroAtividades: Revisão criada a partir do mapa vigente deve manter assinatura idêntica")
    void obterContextoCadastroAtividades_RevisaoCriadaDoMapaVigenteDeveManterAssinaturaIdentica() {
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        processo.setSituacao(SituacaoProcesso.FINALIZADO);
        processo.setDataFinalizacao(LocalDateTime.now().minusDays(1));
        processoRepo.saveAndFlush(processo);

        subprocesso.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO);
        subprocessoRepo.saveAndFlush(subprocesso);

        Atividade atividadeA = atividadeRepo.saveAndFlush(Atividade.builder()
                .mapa(subprocesso.getMapa())
                .descricao("Atividade vigente A")
                .build());
        Atividade atividadeB = atividadeRepo.saveAndFlush(Atividade.builder()
                .mapa(subprocesso.getMapa())
                .descricao("Atividade vigente B")
                .build());

        conhecimentoRepo.saveAndFlush(Conhecimento.builder()
                .atividade(atividadeA)
                .descricao("Conhecimento 2")
                .build());
        conhecimentoRepo.saveAndFlush(Conhecimento.builder()
                .atividade(atividadeA)
                .descricao("Conhecimento 1")
                .build());
        conhecimentoRepo.saveAndFlush(Conhecimento.builder()
                .atividade(atividadeB)
                .descricao("Conhecimento único")
                .build());

        unidadeMapaRepo.saveAndFlush(UnidadeMapa.builder()
                .unidadeCodigo(subprocesso.getUnidade().getCodigo())
                .mapaVigente(subprocesso.getMapa())
                .build());

        Processo processoRevisao = Processo.builder()
                .descricao("Processo revisão contexto")
                .tipo(TipoProcesso.REVISAO)
                .situacao(SituacaoProcesso.EM_ANDAMENTO)
                .dataLimite(LocalDateTime.now().plusDays(30))
                .build();
        processoRepo.saveAndFlush(processoRevisao);

        UnidadeMapa unidadeMapa = unidadeMapaRepo.findById(subprocesso.getUnidade().getCodigo()).orElseThrow();

        subprocessoService.criarParaRevisao(new SubprocessoService.CriarSubprocessoComMapaCommand(
                processoRevisao,
                subprocesso.getUnidade(),
                unidadeMapa,
                subprocesso.getUnidade()
        ));

        Subprocesso subprocessoRevisao = subprocessoRepo.listarPorProcessoComUnidade(processoRevisao.getCodigo())
                .stream()
                .findFirst()
                .orElseThrow();

        ContextoCadastroAtividadesResponse contexto = consultaService.obterContextoCadastroAtividades(subprocessoRevisao.getCodigo());

        assertThat(contexto.atividadesDisponiveis()).isNotEmpty();
        assertThat(contexto.assinaturaCadastroReferencia()).isEqualTo(calcularAssinatura(contexto.atividadesDisponiveis()));
    }

    @Test
    @DisplayName("obterPermissoesUI: Processo FINALIZADO deve manter ações de ADMIN visíveis, mas desabilitadas")
    void obterPermissoesUI_ProcessoFinalizado() {
        processo.setSituacao(SituacaoProcesso.FINALIZADO);
        processoRepo.save(processo);

        PermissoesSubprocessoDto permissoes = consultaService.obterPermissoesUI(subprocesso);

        // Como o perfil é ADMIN, as ações que o ADMIN "pode" devem ser true
        assertThat(permissoes.podeHomologarCadastro()).isTrue();
        assertThat(permissoes.podeEditarMapa()).isTrue();
        assertThat(permissoes.podeHomologarMapa()).isTrue();
        
        // Porém, as ações devem estar desabilitadas por estar FINALIZADO
        assertThat(permissoes.habilitarHomologarCadastro()).isFalse();
        assertThat(permissoes.habilitarEditarMapa()).isFalse();
        assertThat(permissoes.habilitarHomologarMapa()).isFalse();
        
        assertThat(permissoes.podeReabrirCadastro()).isTrue();
        assertThat(permissoes.habilitarReabrirCadastro()).isFalse();
    }

    @Test
    @DisplayName("obterPermissoesUI: Deve testar permissões de ADMIN na mesma unidade")
    void obterPermissoesUI_AdminMesmaUnidade() {
        admin.setPerfilAtivo(Perfil.ADMIN);
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO);

        PermissoesSubprocessoDto permissoes = consultaService.obterPermissoesUI(subprocesso);

        assertThat(permissoes.podeHomologarCadastro()).isTrue();
        assertThat(permissoes.podeDevolverCadastro()).isTrue();
        assertThat(permissoes.podeAlterarDataLimite()).isTrue();
        assertThat(permissoes.podeReabrirCadastro()).isTrue();
        assertThat(permissoes.habilitarReabrirCadastro()).isFalse();
        assertThat(permissoes.podeEnviarLembrete()).isTrue();
    }

    @Test
    @DisplayName("obterPermissoesUI: ADMIN não deve ver autoavaliação no diagnóstico")
    void obterPermissoesUI_AdminNaoDeveVerAutoavaliacaoNoDiagnostico() {
        processo.setTipo(TipoProcesso.DIAGNOSTICO);
        processoRepo.saveAndFlush(processo);
        admin.setPerfilAtivo(Perfil.ADMIN);
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.DIAGNOSTICO_EM_ANDAMENTO);

        PermissoesSubprocessoDto permissoes = consultaService.obterPermissoesUI(subprocesso);

        assertThat(permissoes.podePreencherAutoavaliacao()).isFalse();
        assertThat(permissoes.habilitarPreencherAutoavaliacao()).isFalse();
    }

    @Test
    @DisplayName("obterPermissoesUI: Deve permitir reabrir cadastro quando em MAPA_HOMOLOGADO")
    void obterPermissoesUI_AdminPodeReabrirNoEstadoCorreto() {
        admin.setPerfilAtivo(Perfil.ADMIN);
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO);

        PermissoesSubprocessoDto permissoes = consultaService.obterPermissoesUI(subprocesso);

        assertThat(permissoes.podeReabrirCadastro()).isTrue();
    }

    @Test
    @DisplayName("obterPermissoesUI: Deve testar permissões de CHEFE na mesma unidade")
    void obterPermissoesUI_ChefeMesmaUnidade() {
        admin.setPerfilAtivo(Perfil.CHEFE);
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);

        PermissoesSubprocessoDto permissoes = consultaService.obterPermissoesUI(subprocesso);

        assertThat(permissoes.podeEditarCadastro()).isTrue();
        assertThat(permissoes.podeDisponibilizarCadastro()).isTrue();
        assertThat(permissoes.podeAlterarDataLimite()).isFalse();
    }

    @Test
    @DisplayName("obterPermissoesUI: CHEFE deve editar revisão em andamento")
    void obterPermissoesUI_ChefePodeEditarRevisaoEmAndamento() {
        processo.setTipo(TipoProcesso.REVISAO);
        processoRepo.saveAndFlush(processo);
        admin.setPerfilAtivo(Perfil.CHEFE);
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);

        PermissoesSubprocessoDto permissoes = consultaService.obterPermissoesUI(subprocesso);

        assertThat(permissoes.podeEditarCadastro()).isTrue();
        assertThat(permissoes.podeDisponibilizarCadastro()).isTrue();
        assertThat(permissoes.habilitarAcessoCadastro()).isTrue();
    }

    @Test
    @DisplayName("obterPermissoesUI: Deve testar permissões de GESTOR na mesma unidade")
    void obterPermissoesUI_GestorMesmaUnidade() {
        admin.setPerfilAtivo(Perfil.GESTOR);
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO);

        PermissoesSubprocessoDto permissoes = consultaService.obterPermissoesUI(subprocesso);

        assertThat(permissoes.podeAceitarCadastro()).isTrue();
        assertThat(permissoes.podeDevolverCadastro()).isTrue();
        assertThat(permissoes.podeEnviarLembrete()).isFalse(); // Agora apenas ADMIN
    }

    @Test
    @DisplayName("obterPermissoesUI: GESTOR deve visualizar revisão disponibilizada sem edição")
    void obterPermissoesUI_GestorVisualizaRevisaoDisponibilizada() {
        processo.setTipo(TipoProcesso.REVISAO);
        processoRepo.saveAndFlush(processo);
        admin.setPerfilAtivo(Perfil.GESTOR);
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA);

        PermissoesSubprocessoDto permissoes = consultaService.obterPermissoesUI(subprocesso);

        assertThat(permissoes.podeEditarCadastro()).isFalse(); // GESTOR nunca edita cadastro
        assertThat(permissoes.podeAceitarCadastro()).isTrue();
        assertThat(permissoes.podeDevolverCadastro()).isTrue();
        assertThat(permissoes.habilitarAcessoCadastro()).isTrue();
    }

    @Test
    @DisplayName("obterPermissoesUI: CHEFE deve visualizar revisão disponibilizada sem edição")
    void obterPermissoesUI_ChefeVisualizaRevisaoDisponibilizada() {
        processo.setTipo(TipoProcesso.REVISAO);
        processoRepo.saveAndFlush(processo);
        admin.setPerfilAtivo(Perfil.CHEFE);
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA);

        PermissoesSubprocessoDto permissoes = consultaService.obterPermissoesUI(subprocesso);

        assertThat(permissoes.podeEditarCadastro()).isTrue(); // CHEFE podeEditar, mas situação não permite habilitar
        assertThat(permissoes.habilitarEditarCadastro()).isFalse();
        assertThat(permissoes.podeDisponibilizarCadastro()).isTrue();
        assertThat(permissoes.habilitarDisponibilizarCadastro()).isFalse();
        assertThat(permissoes.habilitarAcessoCadastro()).isTrue();
    }

    @Test
    @DisplayName("obterPermissoesUI: Revisão em processo finalizado mantém apenas visualização")
    void obterPermissoesUI_RevisaoProcessoFinalizadoMantemVisualizacao() {
        processo.setTipo(TipoProcesso.REVISAO);
        processo.setSituacao(SituacaoProcesso.FINALIZADO);
        processoRepo.saveAndFlush(processo);
        admin.setPerfilAtivo(Perfil.ADMIN);
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA);

        PermissoesSubprocessoDto permissoes = consultaService.obterPermissoesUI(subprocesso);

        assertThat(permissoes.podeEditarCadastro()).isFalse(); // ADMIN não edita
        assertThat(permissoes.podeHomologarCadastro()).isTrue(); // ADMIN homologa, mas...
        assertThat(permissoes.habilitarHomologarCadastro()).isFalse(); // está finalizado
        assertThat(permissoes.habilitarAcessoCadastro()).isTrue();
    }

    @Test
    @DisplayName("obterPermissoesUI: Deve testar visualização de impacto em REVISAO")
    void obterPermissoesUI_VisualizarImpacto() {

        admin.setPerfilAtivo(Perfil.ADMIN);
        processo.setTipo(TipoProcesso.REVISAO);
        processoRepo.saveAndFlush(processo);
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA);

        PermissoesSubprocessoDto permissoes = consultaService.obterPermissoesUI(subprocesso);

        assertThat(permissoes.podeVisualizarImpacto()).isTrue();
    }

    private String calcularAssinatura(List<AtividadeDto> atividades) {
        return atividades.stream()
                .map(atividade -> {
                    String descricao = atividade.descricao().trim();
                    String conhecimentos = atividade.conhecimentos().stream()
                            .map(ConhecimentoResumoDto::descricao)
                            .map(String::trim)
                            .sorted()
                            .collect(Collectors.joining("\u0001"));
                    return descricao + "\u0002" + conhecimentos;
                })
                .sorted()
                .collect(Collectors.joining("\u0003"));
    }

    @Test
    @DisplayName("obterPermissoesUI: ADMIN deve poder editar mapa em situação MAPEAMENTO_MAPA_COM_SUGESTOES")
    void obterPermissoesUI_AdminPodeEditarMapaComSugestoes() {
        admin.setPerfilAtivo(Perfil.ADMIN);
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_MAPA_COM_SUGESTOES);

        PermissoesSubprocessoDto permissoes = consultaService.obterPermissoesUI(subprocesso);

        assertThat(permissoes.podeEditarMapa()).isTrue();
        assertThat(permissoes.podeDisponibilizarMapa()).isTrue();
        assertThat(permissoes.podeHomologarMapa()).isTrue();
        assertThat(permissoes.habilitarHomologarMapa()).isFalse();
    }

    @Test
    @DisplayName("obterPermissoesUI: ADMIN deve poder editar mapa em situação REVISAO_MAPA_COM_SUGESTOES")
    void obterPermissoesUI_AdminPodeEditarRevisaoMapaComSugestoes() {
        admin.setPerfilAtivo(Perfil.ADMIN);
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.REVISAO_MAPA_COM_SUGESTOES);

        PermissoesSubprocessoDto permissoes = consultaService.obterPermissoesUI(subprocesso);

        assertThat(permissoes.podeEditarMapa()).isTrue();
        assertThat(permissoes.podeDisponibilizarMapa()).isTrue();
        assertThat(permissoes.podeHomologarMapa()).isTrue();
        assertThat(permissoes.habilitarHomologarMapa()).isFalse();
    }

    @Test
    @DisplayName("obterPermissoesUI: ADMIN deve ficar em somente leitura quando revisão estiver em MAPA_VALIDADO")
    void obterPermissoesUI_AdminRevisaoMapaValidadoSomenteLeitura() {
        processo.setTipo(TipoProcesso.REVISAO);
        processoRepo.saveAndFlush(processo);
        admin.setPerfilAtivo(Perfil.ADMIN);
        admin.setUnidadeAtivaCodigo(1L);
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.REVISAO_MAPA_VALIDADO);
        movimentacaoRepo.saveAndFlush(Movimentacao.builder()
                .subprocesso(subprocesso)
                .unidadeOrigem(subprocesso.getUnidade())
                .unidadeDestino(unidadeRepo.findById(1L).orElseThrow())
                .usuario(admin)
                .descricao("Mapa validado e enviado para homologação")
                .build());

        PermissoesSubprocessoDto permissoes = consultaService.obterPermissoesUI(subprocesso);

        assertThat(permissoes.podeEditarMapa()).isTrue();
        assertThat(permissoes.habilitarEditarMapa()).isFalse();
        assertThat(permissoes.podeDevolverMapa()).isTrue();
        assertThat(permissoes.habilitarDevolverMapa()).isFalse();
        assertThat(permissoes.podeHomologarMapa()).isTrue();
        assertThat(permissoes.habilitarHomologarMapa()).isTrue();
    }
}
