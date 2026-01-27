package sgc.subprocesso.service.workflow;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import sgc.alerta.AlertaFacade;
import sgc.analise.AnaliseFacade;
import sgc.comum.erros.ErroValidacao;
import sgc.organizacao.UnidadeFacade;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.processo.model.Processo;
import sgc.processo.model.TipoProcesso;
import sgc.seguranca.acesso.AccessControlService;
import sgc.subprocesso.model.MovimentacaoRepo;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.subprocesso.service.crud.SubprocessoCrudService;
import sgc.subprocesso.service.crud.SubprocessoValidacaoService;

@ExtendWith(MockitoExtension.class)
class SubprocessoWorkflowServiceTest {
    @InjectMocks
    private SubprocessoWorkflowService workflowService;

    @Mock
    private SubprocessoRepo repositorioSubprocesso;
    @Mock
    private SubprocessoCrudService crudService;
    @Mock
    private AlertaFacade alertaService;
    @Mock
    private UnidadeFacade unidadeService;
    @Mock
    private MovimentacaoRepo repositorioMovimentacao;
    @Mock
    private SubprocessoTransicaoService transicaoService;
    @Mock
    private AnaliseFacade analiseFacade;
    @Mock
    private SubprocessoValidacaoService validacaoService;
    @Mock
    private AccessControlService accessControlService;
    @Mock
    private sgc.comum.repo.RepositorioComum repo;
    @Mock
    private sgc.mapa.service.ImpactoMapaService impactoMapaService;
    @Mock
    private sgc.mapa.service.CompetenciaService competenciaService;
    @Mock
    private sgc.mapa.service.AtividadeService atividadeService;
    @Mock
    private sgc.mapa.service.MapaFacade mapaFacade;

    @Test
    @DisplayName("alterarDataLimite - Etapa 1")
    void alterarDataLimite_Etapa1() {
        Long codigo = 1L;
        LocalDate novaData = LocalDate.of(2023, 10, 1);
        Subprocesso sp = new Subprocesso();
        sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        sp.setUnidade(new Unidade());

        when(crudService.buscarSubprocesso(codigo)).thenReturn(sp);

        workflowService.alterarDataLimite(codigo, novaData);

        verify(repositorioSubprocesso).save(sp);
        assertEquals(novaData.atStartOfDay(), sp.getDataLimiteEtapa1());
        verify(alertaService).criarAlertaAlteracaoDataLimite(any(), any(), any(), eq(1));
    }

    @Test
    @DisplayName("alterarDataLimite - Etapa 2")
    void alterarDataLimite_Etapa2() {
        Long codigo = 1L;
        LocalDate novaData = LocalDate.of(2023, 10, 1);
        Subprocesso sp = new Subprocesso();
        sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
        sp.setUnidade(new Unidade());

        when(crudService.buscarSubprocesso(codigo)).thenReturn(sp);

        workflowService.alterarDataLimite(codigo, novaData);

        verify(repositorioSubprocesso).save(sp);
        assertEquals(novaData.atStartOfDay(), sp.getDataLimiteEtapa2());
        verify(alertaService).criarAlertaAlteracaoDataLimite(any(), any(), any(), eq(2));
    }

    @Test
    @DisplayName("atualizarSituacaoParaEmAndamento - Mapeamento")
    void atualizarSituacaoParaEmAndamento_Mapeamento() {
        Long codMapa = 100L;
        Subprocesso sp = new Subprocesso();
        sp.setSituacao(SituacaoSubprocesso.NAO_INICIADO);
        Processo p = new Processo();
        p.setTipo(TipoProcesso.MAPEAMENTO);
        sp.setProcesso(p);

        when(repositorioSubprocesso.findByMapaCodigo(codMapa)).thenReturn(Optional.of(sp));

        workflowService.atualizarSituacaoParaEmAndamento(codMapa);

        assertEquals(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO, sp.getSituacao());
        verify(repositorioSubprocesso).save(sp);
    }

    @Test
    @DisplayName("reabrirCadastro - Sucesso")
    void reabrirCadastro_Sucesso() {
        Long codigo = 1L;
        Subprocesso sp = new Subprocesso();
        Processo p = new Processo();
        p.setTipo(TipoProcesso.MAPEAMENTO);
        sp.setProcesso(p);
        sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);
        sp.setUnidade(new Unidade());

        when(crudService.buscarSubprocesso(codigo)).thenReturn(sp);
        when(unidadeService.buscarEntidadePorSigla("SEDOC")).thenReturn(new Unidade());

        workflowService.reabrirCadastro(codigo, "Justificativa");

        assertEquals(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO, sp.getSituacao());
        verify(repositorioSubprocesso).save(sp);
        verify(repositorioMovimentacao).save(any());
    }

    @Test
    @DisplayName("reabrirCadastro - Erro Tipo Processo")
    void reabrirCadastro_ErroTipo() {
        Long codigo = 1L;
        Subprocesso sp = new Subprocesso();
        Processo p = new Processo();
        p.setTipo(TipoProcesso.REVISAO);
        sp.setProcesso(p);

        when(crudService.buscarSubprocesso(codigo)).thenReturn(sp);

        var exception = assertThrows(ErroValidacao.class, () -> workflowService.reabrirCadastro(codigo, "J"));
        assertNotNull(exception);
    }

    @Test
    @DisplayName("disponibilizarCadastro - Sucesso")
    void disponibilizarCadastro_Sucesso() {
        Long codigo = 1L;
        Usuario usuario = new Usuario();

        Subprocesso sp = new Subprocesso();
        sp.setCodigo(codigo);
        sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        sGC_Mapa(sp);
        sp.getMapa().setCodigo(100L);
        sp.setUnidade(new Unidade());
        sp.getUnidade().setUnidadeSuperior(new Unidade());

        when(repo.buscar(Subprocesso.class, codigo)).thenReturn(sp);

        workflowService.disponibilizarCadastro(codigo, usuario);

        verify(accessControlService).verificarPermissao(eq(usuario), any(), eq(sp));
        verify(validacaoService).validarExistenciaAtividades(codigo);
        verify(repositorioSubprocesso).save(sp);
        verify(analiseFacade).removerPorSubprocesso(codigo);
        verify(transicaoService).registrar(eq(sp), any(), any(), any(), eq(usuario));
    }

    @Test
    @DisplayName("devolverCadastro - Sucesso")
    void devolverCadastro_Sucesso() {
        Long codigo = 1L;
        Usuario usuario = new Usuario();

        Subprocesso sp = new Subprocesso();
        sp.setCodigo(codigo);
        sp.setUnidade(new Unidade());
        sp.getUnidade().setUnidadeSuperior(new Unidade());

        when(repo.buscar(Subprocesso.class, codigo)).thenReturn(sp);

        workflowService.devolverCadastro(codigo, "Obs", usuario);

        verify(transicaoService).registrarAnaliseETransicao(any());
    }

    @Test
    @DisplayName("aceitarCadastro - Sucesso")
    void aceitarCadastro_Sucesso() {
        Long codigo = 1L;
        Usuario usuario = new Usuario();

        Subprocesso sp = new Subprocesso();
        sp.setCodigo(codigo);
        sp.setUnidade(new Unidade());
        sp.getUnidade().setUnidadeSuperior(new Unidade());

        when(repo.buscar(Subprocesso.class, codigo)).thenReturn(sp);

        workflowService.aceitarCadastro(codigo, "Ok", usuario);

        verify(transicaoService).registrarAnaliseETransicao(any());
    }

    @Test
    @DisplayName("homologarCadastro - Sucesso")
    void homologarCadastro_Sucesso() {
        Long codigo = 1L;
        Usuario usuario = new Usuario();

        Subprocesso sp = new Subprocesso();
        sp.setCodigo(codigo);

        when(repo.buscar(Subprocesso.class, codigo)).thenReturn(sp);
        when(unidadeService.buscarEntidadePorSigla("SEDOC")).thenReturn(new Unidade());

        workflowService.homologarCadastro(codigo, "Ok", usuario);

        verify(repositorioSubprocesso).save(sp);
        verify(transicaoService).registrar(eq(sp), any(), any(), any(), eq(usuario), eq("Ok"));
    }


    @Test
    @DisplayName("aceitarCadastro - Erro Unidade Superior Nula")
    void aceitarCadastro_ErroUnidadeSuperiorNula() {
        Long codigo = 1L;
        Usuario usuario = new Usuario();

        Subprocesso sp = new Subprocesso();
        sp.setCodigo(codigo);
        sp.setUnidade(new Unidade());
        sp.getUnidade().setUnidadeSuperior(null); // Null superior

        when(repo.buscar(Subprocesso.class, codigo)).thenReturn(sp);

        var exception = assertThrows(sgc.comum.erros.ErroInvarianteViolada.class, () ->
                workflowService.aceitarCadastro(codigo, "Ok", usuario)
        );
        assertNotNull(exception);
    }

    @Test
    @DisplayName("devolverRevisaoCadastro - Erro Unidade Superior Nula")
    void devolverRevisaoCadastro_ErroUnidadeSuperiorNula() {
        Long codigo = 1L;
        Usuario usuario = new Usuario();

        Subprocesso sp = new Subprocesso();
        sp.setCodigo(codigo);
        sp.setUnidade(new Unidade());
        sp.getUnidade().setUnidadeSuperior(null); // Null superior

        when(repo.buscar(Subprocesso.class, codigo)).thenReturn(sp);

        var exception = assertThrows(sgc.comum.erros.ErroInvarianteViolada.class, () ->
                workflowService.devolverRevisaoCadastro(codigo, "Obs", usuario)
        );
        assertNotNull(exception);
    }

    private void sGC_Mapa(Subprocesso sp) {
        sgc.mapa.model.Mapa m = new sgc.mapa.model.Mapa();
        sp.setMapa(m);
    }
}
