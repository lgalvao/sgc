package sgc.subprocesso.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.analise.AnaliseFacade;
import sgc.mapa.mapper.ConhecimentoMapper;
import sgc.mapa.service.AtividadeService;
import sgc.mapa.service.CompetenciaService;
import sgc.mapa.service.ConhecimentoService;
import sgc.mapa.service.MapaFacade;
import sgc.organizacao.UsuarioFacade;
import sgc.processo.model.Processo;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.mapper.MapaAjusteMapper;
import sgc.subprocesso.mapper.SubprocessoDetalheMapper;
import sgc.subprocesso.model.Movimentacao;
import sgc.subprocesso.model.MovimentacaoRepo;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.crud.SubprocessoCrudService;
import sgc.subprocesso.service.crud.SubprocessoValidacaoService;
import sgc.subprocesso.service.workflow.SubprocessoWorkflowService;
import sgc.mapa.model.Mapa;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubprocessoFacadeCoverageTest")
class SubprocessoFacadeCoverageTest {

    @Mock private SubprocessoCrudService crudService;
    @Mock private SubprocessoValidacaoService validacaoService;
    @Mock private SubprocessoWorkflowService workflowService;
    @Mock private UsuarioFacade usuarioService;
    @Mock private MapaFacade mapaFacade;
    @Mock private AtividadeService atividadeService;
    @Mock private MovimentacaoRepo repositorioMovimentacao;
    @Mock private SubprocessoDetalheMapper subprocessoDetalheMapper;
    @Mock private ConhecimentoMapper conhecimentoMapper;
    @Mock private AnaliseFacade analiseFacade;
    @Mock private CompetenciaService competenciaService;
    @Mock private ConhecimentoService conhecimentoService;
    @Mock private MapaAjusteMapper mapaAjusteMapper;
    @Mock private sgc.seguranca.acesso.AccessControlService accessControlService;
    @Mock private sgc.subprocesso.model.SubprocessoRepo subprocessoRepo;
    @Mock private sgc.subprocesso.model.SubprocessoMovimentacaoRepo movimentacaoRepo;
    @Mock private sgc.mapa.service.CopiaMapaService copiaMapaService;
    @Mock private sgc.mapa.mapper.AtividadeMapper atividadeMapper;
    @Mock private sgc.organizacao.UnidadeFacade unidadeFacade;

    @InjectMocks
    private SubprocessoFacade facade;

    @Test
    @DisplayName("importarAtividades - Tipo Processo Diagnostico (Default Case)")
    void importarAtividades_TipoProcessoDiagnostico() {
        // Covers lines 404-406 (default case in switch)
        Long codDestino = 1L;
        Long codOrigem = 2L;

        Subprocesso spDestino = new Subprocesso();
        spDestino.setCodigo(codDestino);
        spDestino.setSituacao(SituacaoSubprocesso.NAO_INICIADO);
        spDestino.setMapa(new Mapa());
        spDestino.getMapa().setCodigo(10L);
        spDestino.setUnidade(new sgc.organizacao.model.Unidade());
        spDestino.getUnidade().setSigla("DEST");

        Processo processo = new Processo();
        processo.setTipo(TipoProcesso.DIAGNOSTICO);
        spDestino.setProcesso(processo);

        Subprocesso spOrigem = new Subprocesso();
        spOrigem.setCodigo(codOrigem);
        spOrigem.setMapa(new Mapa());
        spOrigem.getMapa().setCodigo(20L);
        spOrigem.setUnidade(new sgc.organizacao.model.Unidade());
        spOrigem.getUnidade().setSigla("ORIG");

        when(subprocessoRepo.findById(codDestino)).thenReturn(Optional.of(spDestino));
        when(subprocessoRepo.findById(codOrigem)).thenReturn(Optional.of(spOrigem));

        facade.importarAtividades(codDestino, codOrigem);

        // Verify that copiaMapaService was called
        verify(copiaMapaService).importarAtividadesDeOutroMapa(20L, 10L);

        // Verify that Movimentacao was saved
        verify(movimentacaoRepo).save(any(Movimentacao.class));

        // Verify that situation did NOT change (default case)
        assert spDestino.getSituacao() == SituacaoSubprocesso.NAO_INICIADO;
    }

    @Test
    @DisplayName("listarEntidadesPorProcesso - Delegation")
    void listarEntidadesPorProcesso_Delegation() {
        facade.listarEntidadesPorProcesso(1L);
        verify(crudService).listarEntidadesPorProcesso(1L);
    }

    @Test
    @DisplayName("listarPorProcessoESituacao - Delegation")
    void listarPorProcessoESituacao_Delegation() {
        facade.listarPorProcessoESituacao(1L, SituacaoSubprocesso.NAO_INICIADO);
        verify(crudService).listarPorProcessoESituacao(1L, SituacaoSubprocesso.NAO_INICIADO);
    }

    @Test
    @DisplayName("listarPorProcessoUnidadeESituacoes - Delegation")
    void listarPorProcessoUnidadeESituacoes_Delegation() {
        facade.listarPorProcessoUnidadeESituacoes(1L, 2L, null);
        verify(crudService).listarPorProcessoUnidadeESituacoes(1L, 2L, null);
    }

    @Test
    @DisplayName("calcularPermissoes - Delegation")
    void calcularPermissoes_Delegation() {
        Subprocesso sp = new Subprocesso();
        sp.setProcesso(new Processo());
        sp.getProcesso().setTipo(TipoProcesso.MAPEAMENTO);
        sp.setSituacao(SituacaoSubprocesso.NAO_INICIADO);
        sp.setUnidade(new sgc.organizacao.model.Unidade());

        // Mock access control to avoid NPE
        when(accessControlService.podeExecutar(any(), any(), any())).thenReturn(false);

        facade.calcularPermissoes(sp, new sgc.organizacao.model.Usuario());

        verify(accessControlService, org.mockito.Mockito.atLeastOnce()).podeExecutar(any(), any(), any());
    }

    @Test
    @DisplayName("salvarAjustesMapa - Subprocesso Nao Encontrado")
    void salvarAjustesMapa_SubprocessoNaoEncontrado() {
        Long codSubprocesso = 999L;
        when(subprocessoRepo.findById(codSubprocesso)).thenReturn(Optional.empty());

        org.junit.jupiter.api.Assertions.assertThrows(sgc.comum.erros.ErroEntidadeNaoEncontrada.class, () -> {
            facade.salvarAjustesMapa(codSubprocesso, java.util.Collections.emptyList(), "123");
        });
    }
}
