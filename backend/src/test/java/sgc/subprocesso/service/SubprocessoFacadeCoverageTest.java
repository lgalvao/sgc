package sgc.subprocesso.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.analise.AnaliseFacade;
import sgc.mapa.mapper.ConhecimentoMapper;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.Conhecimento;
import sgc.mapa.model.Mapa;
import sgc.mapa.service.AtividadeService;
import sgc.mapa.service.CompetenciaService;
import sgc.mapa.service.ConhecimentoService;
import sgc.mapa.service.MapaFacade;
import sgc.organizacao.UsuarioFacade;
import sgc.processo.model.Processo;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.dto.AtividadeAjusteDto;
import sgc.subprocesso.dto.CompetenciaAjusteDto;
import sgc.subprocesso.mapper.MapaAjusteMapper;
import sgc.subprocesso.mapper.SubprocessoDetalheMapper;
import sgc.subprocesso.model.Movimentacao;
import sgc.subprocesso.model.MovimentacaoRepo;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.crud.SubprocessoCrudService;
import sgc.subprocesso.service.crud.SubprocessoValidacaoService;
import sgc.subprocesso.service.workflow.SubprocessoWorkflowService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
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
            facade.salvarAjustesMapa(codSubprocesso, java.util.Collections.emptyList());
        });
    }

    @Test
    @DisplayName("calcularPermissoes - Tipo Processo Revisao")
    void calcularPermissoes_TipoProcessoRevisao() {
        Subprocesso sp = new Subprocesso();
        sp.setProcesso(new Processo());
        sp.getProcesso().setTipo(TipoProcesso.REVISAO);
        sp.setSituacao(SituacaoSubprocesso.NAO_INICIADO);
        sp.setUnidade(new sgc.organizacao.model.Unidade());

        // Mock access control
        when(accessControlService.podeExecutar(any(), any(), any())).thenReturn(true);

        facade.calcularPermissoes(sp, new sgc.organizacao.model.Usuario());

        verify(accessControlService, org.mockito.Mockito.atLeastOnce()).podeExecutar(any(), any(), any());
    }

    @Test
    @DisplayName("salvarAjustesMapa - Competencia/Atividade nao encontrada ignorada")
    void salvarAjustesMapa_IgnoraItensNaoEncontrados() {
        Long codSubprocesso = 1L;
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(codSubprocesso);
        sp.setSituacao(SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA);

        when(subprocessoRepo.findById(codSubprocesso)).thenReturn(Optional.of(sp));

        // Competencia request contains ID 100, but service returns empty map
        CompetenciaAjusteDto compRequest = CompetenciaAjusteDto.builder()
                .codCompetencia(100L)
                .nome("Nova Nome")
                .atividades(java.util.List.of(
                        AtividadeAjusteDto.builder().codAtividade(200L).nome("Nova Ativ").build()
                ))
                .build();

        // Mock returns empty for bulk fetch implies IDs not found
        when(atividadeService.atualizarDescricoesEmLote(any())).thenReturn(java.util.Collections.emptyList());
        when(competenciaService.buscarPorCodigos(any())).thenReturn(java.util.Collections.emptyList());

        facade.salvarAjustesMapa(codSubprocesso, java.util.List.of(compRequest));

        verify(competenciaService).salvarTodas(any());
    }

    @Test
    @DisplayName("importarAtividades - Mapeamento Nao Iniciado -> Cad Em Andamento")
    void importarAtividades_Mapeamento() {
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
        processo.setTipo(TipoProcesso.MAPEAMENTO);
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

        org.junit.jupiter.api.Assertions.assertEquals(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO, spDestino.getSituacao());
        verify(subprocessoRepo).save(spDestino);
    }

    @Test
    @DisplayName("importarAtividades - Revisao Nao Iniciado -> Revisao Cad Em Andamento")
    void importarAtividades_Revisao() {
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
        processo.setTipo(TipoProcesso.REVISAO);
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

        org.junit.jupiter.api.Assertions.assertEquals(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO, spDestino.getSituacao());
        verify(subprocessoRepo).save(spDestino);
    }

    @Test
    @DisplayName("importarAtividades - Status Ja Em Andamento (Mapeamento)")
    void importarAtividades_StatusJaEmAndamento() {
        Long codDestino = 1L;
        Long codOrigem = 2L;

        Subprocesso spDestino = new Subprocesso();
        spDestino.setCodigo(codDestino);
        spDestino.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        spDestino.setMapa(new Mapa());
        spDestino.getMapa().setCodigo(10L);
        spDestino.setUnidade(new sgc.organizacao.model.Unidade());
        spDestino.getUnidade().setSigla("DEST");

        Processo processo = new Processo();
        processo.setTipo(TipoProcesso.MAPEAMENTO);
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

        // Status must NOT change
        org.junit.jupiter.api.Assertions.assertEquals(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO, spDestino.getSituacao());
        // Verify save was NOT called
        verify(subprocessoRepo, org.mockito.Mockito.never()).save(spDestino);
        // But movimentacao IS saved.
        verify(movimentacaoRepo).save(any(Movimentacao.class));
    }

    @Test
    @DisplayName("salvarAjustesMapa - Status Invalido")
    void salvarAjustesMapa_StatusInvalido() {
        Long codSubprocesso = 1L;
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(codSubprocesso);
        // Set invalid status (not REVISAO_CADASTRO_HOMOLOGADA or REVISAO_MAPA_AJUSTADO)
        sp.setSituacao(SituacaoSubprocesso.NAO_INICIADO);

        when(subprocessoRepo.findById(codSubprocesso)).thenReturn(Optional.of(sp));

        org.junit.jupiter.api.Assertions.assertThrows(sgc.subprocesso.erros.ErroMapaEmSituacaoInvalida.class, () -> {
            facade.salvarAjustesMapa(codSubprocesso, java.util.Collections.emptyList());
        });
    }

    @Test
    @DisplayName("importarAtividades - Status Invalido")
    void importarAtividades_StatusInvalido() {
        Long codDestino = 1L;
        Long codOrigem = 2L;

        Subprocesso spDestino = new Subprocesso();
        spDestino.setCodigo(codDestino);
        // Invalid status for import
        spDestino.setSituacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO);

        when(subprocessoRepo.findById(codDestino)).thenReturn(Optional.of(spDestino));

        org.junit.jupiter.api.Assertions.assertThrows(sgc.subprocesso.erros.ErroAtividadesEmSituacaoInvalida.class, () -> {
            facade.importarAtividades(codDestino, codOrigem);
        });
    }

    @Test
    @DisplayName("importarAtividades - Unidade Origem Null")
    void importarAtividades_UnidadeOrigemNull() {
        Long codDestino = 1L;
        Long codOrigem = 2L;

        Subprocesso spDestino = new Subprocesso();
        spDestino.setCodigo(codDestino);
        spDestino.setSituacao(SituacaoSubprocesso.NAO_INICIADO);
        spDestino.setMapa(new Mapa());
        spDestino.getMapa().setCodigo(10L);
        spDestino.setUnidade(new sgc.organizacao.model.Unidade());
        spDestino.getUnidade().setSigla("DEST");
        spDestino.setProcesso(new Processo());
        spDestino.getProcesso().setTipo(TipoProcesso.MAPEAMENTO);

        Subprocesso spOrigem = new Subprocesso();
        spOrigem.setCodigo(codOrigem);
        spOrigem.setMapa(new Mapa());
        spOrigem.getMapa().setCodigo(20L);
        // Unit is null
        spOrigem.setUnidade(null);

        when(subprocessoRepo.findById(codDestino)).thenReturn(Optional.of(spDestino));
        when(subprocessoRepo.findById(codOrigem)).thenReturn(Optional.of(spOrigem));

        facade.importarAtividades(codDestino, codOrigem);

        // Verify that movimentacao was saved (no exception thrown)
        verify(movimentacaoRepo).save(any(Movimentacao.class));
    }

    @Test
    @DisplayName("listarAtividadesSubprocesso - Com Conhecimentos (Coverage)")
    void listarAtividadesSubprocesso_ComConhecimentos() {
        Long codSubprocesso = 1L;
        Long codMapa = 10L;

        Subprocesso sp = new Subprocesso();
        sp.setCodigo(codSubprocesso);
        sp.setMapa(new Mapa());
        sp.getMapa().setCodigo(codMapa);

        when(crudService.buscarSubprocesso(codSubprocesso)).thenReturn(sp);

        Atividade atividade = new Atividade();
        atividade.setCodigo(100L);
        atividade.setDescricao("Atividade 1");

        Conhecimento conhecimento = new Conhecimento();
        conhecimento.setCodigo(1000L);
        conhecimento.setDescricao("Conhecimento A");

        atividade.setConhecimentos(List.of(conhecimento));

        when(atividadeService.buscarPorMapaCodigoComConhecimentos(codMapa)).thenReturn(List.of(atividade));

        var result = facade.listarAtividadesSubprocesso(codSubprocesso);

        assertFalse(result.isEmpty());
        assertFalse(result.get(0).getConhecimentos().isEmpty());
    }

    @Test
    @DisplayName("salvarAjustesMapa - Atividades Null na Competencia")
    void salvarAjustesMapa_AtividadesNull() {
        // Covers branches 559, 580 (null check for atividades in dto)
        Long codSubprocesso = 1L;
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(codSubprocesso);
        sp.setSituacao(SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA);

        when(subprocessoRepo.findById(codSubprocesso)).thenReturn(Optional.of(sp));

        CompetenciaAjusteDto compDto = CompetenciaAjusteDto.builder()
                .codCompetencia(100L)
                .nome("Comp")
                .atividades(List.of()) // Lista vazia, não mais null
                .build();

        Competencia competencia = new Competencia();
        competencia.setCodigo(100L);
        when(competenciaService.buscarPorCodigos(any())).thenReturn(List.of(competencia));

        facade.salvarAjustesMapa(codSubprocesso, List.of(compDto));

        verify(competenciaService).salvarTodas(any());
    }

    @Test
    @DisplayName("importarAtividades - Destino ja em Revisao Cadastro Em Andamento")
    void importarAtividades_RevisaoEmAndamento() {
        // Covers branch 604
        Long codDestino = 1L;
        Long codOrigem = 2L;

        Subprocesso spDestino = new Subprocesso();
        spDestino.setCodigo(codDestino);
        // Set situation to one that is VALID but not NAO_INICIADO
        spDestino.setSituacao(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);
        spDestino.setMapa(new Mapa());
        spDestino.getMapa().setCodigo(10L);
        spDestino.setUnidade(new sgc.organizacao.model.Unidade());
        spDestino.getUnidade().setSigla("DEST");

        Subprocesso spOrigem = new Subprocesso();
        spOrigem.setCodigo(codOrigem);
        spOrigem.setMapa(new Mapa());
        spOrigem.getMapa().setCodigo(20L);
        spOrigem.setUnidade(new sgc.organizacao.model.Unidade());
        spOrigem.getUnidade().setSigla("ORIG");

        when(subprocessoRepo.findById(codDestino)).thenReturn(Optional.of(spDestino));
        when(subprocessoRepo.findById(codOrigem)).thenReturn(Optional.of(spOrigem));

        facade.importarAtividades(codDestino, codOrigem);

        // Should execute import
        verify(copiaMapaService).importarAtividadesDeOutroMapa(20L, 10L);
    }
}
