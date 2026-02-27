package sgc.subprocesso.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import sgc.alerta.*;
import sgc.comum.erros.*;
import sgc.comum.model.ComumRepo;
import sgc.mapa.dto.*;
import sgc.mapa.model.*;
import sgc.mapa.service.*;
import sgc.organizacao.*;
import sgc.organizacao.model.*;
import sgc.processo.model.*;
import sgc.seguranca.SgcPermissionEvaluator;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.model.*;
import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubprocessoService - Cobertura")
class SubprocessoServiceCoverageTest {

    @Mock private SubprocessoRepo subprocessoRepo;
    @Mock private MovimentacaoRepo movimentacaoRepo;
    @Mock private AnaliseRepo analiseRepo;
    @Mock private AlertaFacade alertaService;
    @Mock private OrganizacaoFacade organizacaoFacade;
    @Mock private UsuarioFacade usuarioFacade;
    @Mock private ImpactoMapaService impactoMapaService;
    @Mock private EmailService emailService;
    @Mock private MapaManutencaoService mapaManutencaoService;
    @Mock private MapaSalvamentoService mapaSalvamentoService;
    @Mock private SgcPermissionEvaluator permissionEvaluator;
    @Mock private ComumRepo comumRepo;
    @Mock private CopiaMapaService copiaMapaService;

    @InjectMocks private SubprocessoService service;

    @BeforeEach
    void setup() {
        service.setMapaManutencaoService(mapaManutencaoService);
        service.setSubprocessoRepo(subprocessoRepo);
        service.setMovimentacaoRepo(movimentacaoRepo);
        service.setCopiaMapaService(copiaMapaService);
    }

    private Subprocesso criarSubprocesso(Long id, SituacaoSubprocesso situacao) {
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(id);
        sp.setSituacaoForcada(situacao);
        sp.setUnidade(new Unidade());
        sp.getUnidade().setSigla("U1");
        sp.setProcesso(new Processo());
        sp.getProcesso().setDescricao("Processo 1");
        sp.getProcesso().setTipo(TipoProcesso.REVISAO); // Alterado para REVISAO para submeterMapaAjustado
        sp.setMapa(new Mapa());
        sp.getMapa().setCodigo(100L);
        return sp;
    }

    @Test
    @DisplayName("validarExistenciaAtividades - deve lançar erro se mapa sem atividades")
    void validarExistenciaAtividades_SemAtividades() {
        Long codigo = 1L;
        Subprocesso sp = criarSubprocesso(codigo, SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);

        when(subprocessoRepo.findByIdWithMapaAndAtividades(codigo)).thenReturn(Optional.of(sp));
        when(mapaManutencaoService.buscarAtividadesPorMapaCodigoComConhecimentos(100L)).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> service.validarExistenciaAtividades(codigo))
                .isInstanceOf(ErroValidacao.class)
                .hasMessage("O mapa de competências deve ter ao menos uma atividade cadastrada.");
    }

    @Test
    @DisplayName("validarExistenciaAtividades - deve lançar erro se atividades sem conhecimento")
    void validarExistenciaAtividades_SemConhecimento() {
        Long codigo = 1L;
        Subprocesso sp = criarSubprocesso(codigo, SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        Atividade a = new Atividade();
        a.setConhecimentos(Collections.emptySet());

        when(subprocessoRepo.findByIdWithMapaAndAtividades(codigo)).thenReturn(Optional.of(sp));
        when(mapaManutencaoService.buscarAtividadesPorMapaCodigoComConhecimentos(100L)).thenReturn(List.of(a));

        assertThatThrownBy(() -> service.validarExistenciaAtividades(codigo))
                .isInstanceOf(ErroValidacao.class)
                .hasMessage("Todas as atividades devem possuir conhecimentos vinculados. Verifique as atividades pendentes.");
    }

    @Test
    @DisplayName("validarSituacaoPermitida - deve lançar erro com mensagem correta")
    void validarSituacaoPermitida_Erro() {
        Subprocesso sp = new Subprocesso();
        sp.setSituacaoForcada(SituacaoSubprocesso.NAO_INICIADO);

        assertThatThrownBy(() -> service.validarSituacaoPermitida(sp, SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO))
                .isInstanceOf(ErroValidacao.class)
                .hasMessageContaining("Situação do subprocesso não permite esta operação");
    }

    @Test
    @DisplayName("validarSituacaoPermitida - deve lançar erro se situacao nula")
    void validarSituacaoPermitida_SituacaoNula() {
        Subprocesso sp = new Subprocesso();
        sp.setSituacaoForcada(null);
        assertThatThrownBy(() -> service.validarSituacaoPermitida(sp, SituacaoSubprocesso.NAO_INICIADO))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("validarSituacaoPermitida - deve lançar erro se permitidas vazio")
    void validarSituacaoPermitida_PermitidasVazio() {
        Subprocesso sp = new Subprocesso();
        sp.setSituacaoForcada(SituacaoSubprocesso.NAO_INICIADO);
        assertThatThrownBy(() -> service.validarSituacaoPermitida(sp))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("alterarDataLimite - deve alterar data etapa 2 se situacao for MAPA")
    void alterarDataLimite_Etapa2() {
        Long codigo = 1L;
        Subprocesso sp = criarSubprocesso(codigo, SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
        when(subprocessoRepo.findByIdWithMapaAndAtividades(codigo)).thenReturn(Optional.of(sp));

        LocalDate novaData = LocalDate.of(2025, 1, 1);
        service.alterarDataLimite(codigo, novaData);

        assertThat(sp.getDataLimiteEtapa2()).isEqualTo(novaData.atStartOfDay());
        verify(subprocessoRepo).save(sp);
    }

    @Test
    @DisplayName("removerCompetencia - deve voltar para CADASTRO_HOMOLOGADO se mapa ficar vazio (Mapeamento)")
    void removerCompetencia_MapaFicaVazio_Mapeamento() {
        Long codigo = 1L;
        Subprocesso sp = criarSubprocesso(codigo, SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);

        when(subprocessoRepo.findByIdWithMapaAndAtividades(codigo)).thenReturn(Optional.of(sp));
        when(mapaManutencaoService.buscarCompetenciasPorCodMapa(100L)).thenReturn(Collections.emptyList());

        service.removerCompetencia(codigo, 10L);

        assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);
        verify(subprocessoRepo).save(sp);
    }

    @Test
    @DisplayName("removerCompetencia - deve voltar para REVISAO_CADASTRO_HOMOLOGADA se mapa ficar vazio (Revisao)")
    void removerCompetencia_MapaFicaVazio_Revisao() {
        Long codigo = 1L;
        Subprocesso sp = criarSubprocesso(codigo, SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO);

        when(subprocessoRepo.findByIdWithMapaAndAtividades(codigo)).thenReturn(Optional.of(sp));
        when(mapaManutencaoService.buscarCompetenciasPorCodMapa(100L)).thenReturn(Collections.emptyList());

        service.removerCompetencia(codigo, 10L);

        assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA);
        verify(subprocessoRepo).save(sp);
    }

    @Test
    @DisplayName("salvarMapaSubprocesso - deve avancar situacao se mapa era vazio")
    void salvarMapaSubprocesso_EraVazio() {
        Long codigo = 1L;
        Subprocesso sp = criarSubprocesso(codigo, SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);

        when(subprocessoRepo.findByIdWithMapaAndAtividades(codigo)).thenReturn(Optional.of(sp));
        when(mapaManutencaoService.buscarCompetenciasPorCodMapa(100L)).thenReturn(Collections.emptyList());

        SalvarMapaRequest req = new SalvarMapaRequest("Obs", List.of(new SalvarMapaRequest.CompetenciaRequest(null, "C1", List.of())));
        service.salvarMapaSubprocesso(codigo, req);

        assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
        verify(subprocessoRepo).save(sp);
    }

    @Test
    @DisplayName("executarAceiteValidacao - deve homologar se nao houver unidade superior")
    void executarAceiteValidacao_SemSuperior() {
        Long codigo = 1L;
        Subprocesso sp = criarSubprocesso(codigo, SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO);
        sp.getProcesso().setTipo(TipoProcesso.MAPEAMENTO); // Resetting for this test
        // Unidade sem superior
        sp.getUnidade().setUnidadeSuperior(null);

        Usuario user = new Usuario();
        user.setTituloEleitoral("123");

        when(subprocessoRepo.findByIdWithMapaAndAtividades(codigo)).thenReturn(Optional.of(sp));
        when(organizacaoFacade.buscarPorSigla(any())).thenReturn(new sgc.organizacao.dto.UnidadeDto());

        service.aceitarValidacao(codigo, user);

        assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO);
        verify(subprocessoRepo).save(sp);
        verify(analiseRepo).save(any(Analise.class)); // Cria analise final
    }

    @Test
    @DisplayName("disponibilizar - deve usar unidade origem como destino se superior for nulo")
    void disponibilizar_SemSuperior() {
        Long codigo = 1L;
        Usuario user = new Usuario();

        Subprocesso sp = criarSubprocesso(codigo, SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        sp.getProcesso().setTipo(TipoProcesso.MAPEAMENTO);
        sp.getUnidade().setUnidadeSuperior(null);

        when(subprocessoRepo.findByIdWithMapaAndAtividades(codigo)).thenReturn(Optional.of(sp));
        // Mock validacoes
        Atividade a = new Atividade();
        a.setConhecimentos(Set.of(new Conhecimento()));
        when(mapaManutencaoService.buscarAtividadesPorMapaCodigoComConhecimentos(100L)).thenReturn(List.of(a));

        service.disponibilizarCadastro(codigo, user);

        verify(movimentacaoRepo).save(argThat(m -> m.getUnidadeDestino().equals(sp.getUnidade())));
    }

    @Test
    @DisplayName("validarAssociacoesMapa - deve lançar erro se competencias sem atividades")
    void validarAssociacoesMapa_CompetenciaSemAtividade() {
        Long mapaId = 100L;
        Competencia c = new Competencia();
        c.setDescricao("Comp 1");
        c.setAtividades(Collections.emptySet());

        when(mapaManutencaoService.buscarCompetenciasPorCodMapa(mapaId)).thenReturn(List.of(c));

        assertThatThrownBy(() -> service.validarAssociacoesMapa(mapaId))
            .isInstanceOf(ErroValidacao.class)
            .hasMessageContaining("Existem competências que não foram associadas a nenhuma atividade");
    }

    @Test
    @DisplayName("validarAssociacoesMapa - deve lançar erro se atividades sem competencia")
    void validarAssociacoesMapa_AtividadeSemCompetencia() {
        Long mapaId = 100L;
        Competencia c = new Competencia();
        c.setAtividades(Set.of(new Atividade())); // ok

        Atividade a = new Atividade();
        a.setDescricao("Ativ 1");
        a.setCompetencias(Collections.emptySet());

        when(mapaManutencaoService.buscarCompetenciasPorCodMapa(mapaId)).thenReturn(List.of(c));
        when(mapaManutencaoService.buscarAtividadesPorMapaCodigo(mapaId)).thenReturn(List.of(a));

        assertThatThrownBy(() -> service.validarAssociacoesMapa(mapaId))
            .isInstanceOf(ErroValidacao.class)
            .hasMessageContaining("Existem atividades que não foram associadas a nenhuma competência");
    }

    @Test
    @DisplayName("validarCadastro - deve retornar valido se tudo ok")
    void validarCadastro_Valido() {
        Long codigo = 1L;
        Subprocesso sp = criarSubprocesso(codigo, SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        Atividade a = new Atividade();
        a.setConhecimentos(Set.of(new Conhecimento()));

        when(subprocessoRepo.findByIdWithMapaAndAtividades(codigo)).thenReturn(Optional.of(sp));
        when(mapaManutencaoService.buscarAtividadesPorMapaCodigoComConhecimentos(100L)).thenReturn(List.of(a));

        ValidacaoCadastroDto result = service.validarCadastro(codigo);
        assertThat(result.valido()).isTrue();
    }

    @Test
    @DisplayName("processarAlteracoes - deve atualizar todos os campos")
    void processarAlteracoes_TodosCampos() {
        Long codigo = 1L;
        Subprocesso sp = criarSubprocesso(codigo, SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        when(subprocessoRepo.findByIdWithMapaAndAtividades(codigo)).thenReturn(Optional.of(sp));

        LocalDateTime now = LocalDateTime.now();
        AtualizarSubprocessoRequest req = new AtualizarSubprocessoRequest(
            200L, 200L, now, now, now, now
        );

        service.atualizarEntidade(codigo, req);

        assertThat(sp.getMapa().getCodigo()).isEqualTo(200L);
        assertThat(sp.getDataLimiteEtapa1()).isEqualTo(now);
        assertThat(sp.getDataFimEtapa1()).isEqualTo(now);
        assertThat(sp.getDataLimiteEtapa2()).isEqualTo(now);
        assertThat(sp.getDataFimEtapa2()).isEqualTo(now);
        verify(subprocessoRepo).save(sp);
    }

    @Test
    @DisplayName("importarAtividades - sucesso")
    void importarAtividades_Sucesso() {
        Long destId = 1L;
        Long origId = 2L;
        Subprocesso dest = criarSubprocesso(destId, SituacaoSubprocesso.NAO_INICIADO);
        dest.getProcesso().setTipo(TipoProcesso.MAPEAMENTO); // Mapeamento
        Subprocesso orig = criarSubprocesso(origId, SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);

        Usuario user = new Usuario();
        when(usuarioFacade.usuarioAutenticado()).thenReturn(user);
        when(comumRepo.buscar(Subprocesso.class, destId)).thenReturn(dest);
        when(comumRepo.buscar(Subprocesso.class, origId)).thenReturn(orig);

        when(permissionEvaluator.checkPermission(user, dest, "EDITAR_CADASTRO")).thenReturn(true);
        when(permissionEvaluator.checkPermission(user, orig, "CONSULTAR_PARA_IMPORTACAO")).thenReturn(true);

        service.importarAtividades(destId, origId);

        assertThat(dest.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        verify(copiaMapaService).importarAtividadesDeOutroMapa(orig.getMapa().getCodigo(), dest.getMapa().getCodigo());
    }

    @Test
    @DisplayName("importarAtividades - erro permissao destino")
    void importarAtividades_SemPermissaoDestino() {
        Long destId = 1L;
        Long origId = 2L;
        Subprocesso dest = criarSubprocesso(destId, SituacaoSubprocesso.NAO_INICIADO);

        Usuario user = new Usuario();
        when(usuarioFacade.usuarioAutenticado()).thenReturn(user);
        when(comumRepo.buscar(Subprocesso.class, destId)).thenReturn(dest);

        when(permissionEvaluator.checkPermission(user, dest, "EDITAR_CADASTRO")).thenReturn(false);

        assertThatThrownBy(() -> service.importarAtividades(destId, origId))
            .isInstanceOf(ErroAcessoNegado.class);
    }

    @Test
    @DisplayName("importarAtividades - erro permissao origem")
    void importarAtividades_SemPermissaoOrigem() {
        Long destId = 1L;
        Long origId = 2L;
        Subprocesso dest = criarSubprocesso(destId, SituacaoSubprocesso.NAO_INICIADO);
        Subprocesso orig = criarSubprocesso(origId, SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);

        Usuario user = new Usuario();
        when(usuarioFacade.usuarioAutenticado()).thenReturn(user);
        when(comumRepo.buscar(Subprocesso.class, destId)).thenReturn(dest);
        when(comumRepo.buscar(Subprocesso.class, origId)).thenReturn(orig);

        when(permissionEvaluator.checkPermission(user, dest, "EDITAR_CADASTRO")).thenReturn(true);
        when(permissionEvaluator.checkPermission(user, orig, "CONSULTAR_PARA_IMPORTACAO")).thenReturn(false);

        assertThatThrownBy(() -> service.importarAtividades(destId, origId))
            .isInstanceOf(ErroAcessoNegado.class);
    }

    @Test
    @DisplayName("importarAtividades - erro situacao invalida")
    void importarAtividades_SituacaoInvalida() {
        Long destId = 1L;
        Long origId = 2L;
        Subprocesso dest = criarSubprocesso(destId, SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO); // Situacao nao permitida

        Usuario user = new Usuario();
        when(usuarioFacade.usuarioAutenticado()).thenReturn(user);
        when(comumRepo.buscar(Subprocesso.class, destId)).thenReturn(dest);

        when(permissionEvaluator.checkPermission(user, dest, "EDITAR_CADASTRO")).thenReturn(true);

        assertThatThrownBy(() -> service.importarAtividades(destId, origId))
            .isInstanceOf(ErroValidacao.class);
    }

    @Test
    @DisplayName("salvarAjustesMapa - sucesso")
    void salvarAjustesMapa_Sucesso() {
        Long codigo = 1L;
        Subprocesso sp = criarSubprocesso(codigo, SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA);
        sp.getProcesso().setTipo(TipoProcesso.REVISAO);

        when(comumRepo.buscar(Subprocesso.class, codigo)).thenReturn(sp);

        // Mocking dependencies for updates
        when(mapaManutencaoService.buscarCompetenciasPorCodigos(any())).thenReturn(Collections.emptyList());
        when(mapaManutencaoService.buscarAtividadesPorCodigos(any())).thenReturn(Collections.emptyList());

        List<CompetenciaAjusteDto> dtos = List.of();
        service.salvarAjustesMapa(codigo, dtos);

        assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO);
        verify(subprocessoRepo).save(sp);
    }

    @Test
    @DisplayName("submeterMapaAjustado - sucesso")
    void submeterMapaAjustado_Sucesso() {
        Long codigo = 1L;
        Subprocesso sp = criarSubprocesso(codigo, SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO);
        // sp.getProcesso().setTipo(TipoProcesso.REVISAO); // Already set in helper
        Usuario user = new Usuario();

        when(subprocessoRepo.findByIdWithMapaAndAtividades(codigo)).thenReturn(Optional.of(sp));

        // Validation mocks
        Competencia c = new Competencia();
        c.setAtividades(Set.of(new Atividade())); // Valid
        when(mapaManutencaoService.buscarCompetenciasPorCodMapa(100L)).thenReturn(List.of(c));
        when(mapaManutencaoService.buscarAtividadesPorMapaCodigo(100L)).thenReturn(Collections.emptyList());

        SubmeterMapaAjustadoRequest req = new SubmeterMapaAjustadoRequest("Just", null, null);
        service.submeterMapaAjustado(codigo, req, user);

        assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.REVISAO_MAPA_DISPONIBILIZADO);
        verify(movimentacaoRepo).save(any());
    }

    @Test
    @DisplayName("obterPermissoesUI - admin tem permissoes extras")
    void obterPermissoesUI_Admin() {
        Subprocesso sp = criarSubprocesso(1L, SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);
        Usuario user = new Usuario();
        user.setPerfilAtivo(Perfil.ADMIN);

        PermissoesSubprocessoDto perms = service.obterPermissoesUI(sp, user);

        assertThat(perms.podeReabrirCadastro()).isTrue();
        assertThat(perms.podeAlterarDataLimite()).isTrue();
    }
}
