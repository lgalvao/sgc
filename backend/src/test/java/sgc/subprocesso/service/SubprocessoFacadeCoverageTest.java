package sgc.subprocesso.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.analise.AnaliseFacade;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.dto.AtualizarAtividadeRequest;
import sgc.mapa.mapper.AtividadeMapper;
import sgc.mapa.mapper.ConhecimentoMapper;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.Mapa;
import sgc.mapa.service.AtividadeService;
import sgc.mapa.service.CompetenciaService;
import sgc.mapa.service.ConhecimentoService;
import sgc.mapa.service.CopiaMapaService;
import sgc.mapa.service.MapaFacade;
import sgc.organizacao.UnidadeFacade;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.processo.model.Processo;
import sgc.processo.model.TipoProcesso;
import sgc.seguranca.acesso.AccessControlService;
import sgc.subprocesso.dto.AtividadeAjusteDto;
import sgc.subprocesso.dto.CompetenciaAjusteDto;
import sgc.subprocesso.erros.ErroAtividadesEmSituacaoInvalida;
import sgc.subprocesso.erros.ErroMapaEmSituacaoInvalida;
import sgc.subprocesso.mapper.MapaAjusteMapper;
import sgc.subprocesso.mapper.SubprocessoDetalheMapper;
import sgc.subprocesso.model.MovimentacaoRepo;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoMovimentacaoRepo;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.subprocesso.service.crud.SubprocessoCrudService;
import sgc.subprocesso.service.crud.SubprocessoValidacaoService;
import sgc.subprocesso.service.workflow.SubprocessoWorkflowService;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubprocessoFacadeCoverageTest {

    @InjectMocks
    private SubprocessoFacade subprocessoFacade;

    @Mock
    private SubprocessoCrudService crudService;
    @Mock
    private SubprocessoValidacaoService validacaoService;
    @Mock
    private SubprocessoWorkflowService workflowService;
    @Mock
    private UsuarioFacade usuarioService;
    @Mock
    private UnidadeFacade unidadeFacade;
    @Mock
    private MapaFacade mapaFacade;
    @Mock
    private AtividadeService atividadeService;
    @Mock
    private MovimentacaoRepo repositorioMovimentacao;
    @Mock
    private SubprocessoDetalheMapper subprocessoDetalheMapper;
    @Mock
    private ConhecimentoMapper conhecimentoMapper;
    @Mock
    private AnaliseFacade analiseFacade;
    @Mock
    private CompetenciaService competenciaService;
    @Mock
    private ConhecimentoService conhecimentoService;
    @Mock
    private MapaAjusteMapper mapaAjusteMapper;
    @Mock
    private AccessControlService accessControlService;
    @Mock
    private SubprocessoRepo subprocessoRepo;
    @Mock
    private SubprocessoMovimentacaoRepo movimentacaoRepo;
    @Mock
    private CopiaMapaService copiaMapaService;
    @Mock
    private AtividadeMapper atividadeMapper;

    @Test
    @DisplayName("salvarAjustesMapa - Sucesso")
    void salvarAjustesMapa_Sucesso() {
        Long codSubprocesso = 1L;
        String usuarioTitulo = "123456789012";

        Subprocesso sp = new Subprocesso();
        sp.setCodigo(codSubprocesso);
        sp.setSituacao(SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA);

        when(subprocessoRepo.findById(codSubprocesso)).thenReturn(Optional.of(sp));

        CompetenciaAjusteDto compDto = CompetenciaAjusteDto.builder()
                .codCompetencia(10L)
                .nome("Competencia Ajustada")
                .atividades(List.of(
                        AtividadeAjusteDto.builder().codAtividade(100L).nome("Atividade Ajustada").build()
                ))
                .build();

        Competencia competencia = new Competencia();
        competencia.setCodigo(10L);
        when(competenciaService.buscarPorCodigos(List.of(10L))).thenReturn(List.of(competencia));

        subprocessoFacade.salvarAjustesMapa(codSubprocesso, List.of(compDto), usuarioTitulo);

        verify(atividadeService).atualizarDescricoesEmLote(anyMap());
        verify(competenciaService).salvarTodas(anyList());
        verify(subprocessoRepo).save(sp);
        assertEquals(SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO, sp.getSituacao());
    }

    @Test
    @DisplayName("salvarAjustesMapa - Erro Situação Inválida")
    void salvarAjustesMapa_ErroSituacao() {
        Long codSubprocesso = 1L;
        Subprocesso sp = new Subprocesso();
        sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);

        when(subprocessoRepo.findById(codSubprocesso)).thenReturn(Optional.of(sp));

        List<CompetenciaAjusteDto> lista = Collections.emptyList();

        assertThrows(ErroMapaEmSituacaoInvalida.class, () ->
            subprocessoFacade.salvarAjustesMapa(codSubprocesso, lista, "user")
        );
    }

    @Test
    @DisplayName("importarAtividades - Sucesso Mapeamento")
    void importarAtividades_SucessoMapeamento() {
        Long codDestino = 2L;
        Long codOrigem = 1L;

        Subprocesso spDestino = new Subprocesso();
        spDestino.setCodigo(codDestino);
        spDestino.setSituacao(SituacaoSubprocesso.NAO_INICIADO);
        Processo proc = new Processo();
        proc.setTipo(TipoProcesso.MAPEAMENTO);
        spDestino.setProcesso(proc);
        Mapa mapaDestino = new Mapa();
        mapaDestino.setCodigo(20L);
        spDestino.setMapa(mapaDestino);
        Unidade unidadeDestino = new Unidade();
        unidadeDestino.setSigla("DEST");
        spDestino.setUnidade(unidadeDestino);

        Subprocesso spOrigem = new Subprocesso();
        spOrigem.setCodigo(codOrigem);
        Mapa mapaOrigem = new Mapa();
        mapaOrigem.setCodigo(10L);
        spOrigem.setMapa(mapaOrigem);
        Unidade unidadeOrigem = new Unidade();
        unidadeOrigem.setSigla("ORIG");
        spOrigem.setUnidade(unidadeOrigem);

        when(subprocessoRepo.findById(codDestino)).thenReturn(Optional.of(spDestino));
        when(subprocessoRepo.findById(codOrigem)).thenReturn(Optional.of(spOrigem));

        subprocessoFacade.importarAtividades(codDestino, codOrigem);

        verify(copiaMapaService).importarAtividadesDeOutroMapa(10L, 20L);
        verify(subprocessoRepo).save(spDestino);
        assertEquals(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO, spDestino.getSituacao());
        verify(movimentacaoRepo).save(any());
    }

    @Test
    @DisplayName("importarAtividades - Erro Situação Inválida")
    void importarAtividades_ErroSituacao() {
        Long codDestino = 2L;
        Subprocesso spDestino = new Subprocesso();
        spDestino.setSituacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO);

        when(subprocessoRepo.findById(codDestino)).thenReturn(Optional.of(spDestino));

        assertThrows(ErroAtividadesEmSituacaoInvalida.class, () ->
            subprocessoFacade.importarAtividades(codDestino, 1L)
        );
    }

    @Test
    @DisplayName("importarAtividades - Sucesso Revisão")
    void importarAtividades_SucessoRevisao() {
        Long codDestino = 2L;
        Long codOrigem = 1L;

        Subprocesso spDestino = new Subprocesso();
        spDestino.setCodigo(codDestino);
        spDestino.setSituacao(SituacaoSubprocesso.NAO_INICIADO);
        Processo proc = new Processo();
        proc.setTipo(TipoProcesso.REVISAO);
        spDestino.setProcesso(proc);
        Mapa mapaDestino = new Mapa();
        mapaDestino.setCodigo(20L);
        spDestino.setMapa(mapaDestino);
        Unidade unidadeDestino = new Unidade();
        unidadeDestino.setSigla("DEST");
        spDestino.setUnidade(unidadeDestino);

        Subprocesso spOrigem = new Subprocesso();
        spOrigem.setCodigo(codOrigem);
        Mapa mapaOrigem = new Mapa();
        mapaOrigem.setCodigo(10L);
        spOrigem.setMapa(mapaOrigem);
        Unidade unidadeOrigem = new Unidade();
        unidadeOrigem.setSigla("ORIG");
        spOrigem.setUnidade(unidadeOrigem);

        when(subprocessoRepo.findById(codDestino)).thenReturn(Optional.of(spDestino));
        when(subprocessoRepo.findById(codOrigem)).thenReturn(Optional.of(spOrigem));

        subprocessoFacade.importarAtividades(codDestino, codOrigem);

        verify(copiaMapaService).importarAtividadesDeOutroMapa(10L, 20L);
        verify(subprocessoRepo).save(spDestino);
        assertEquals(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO, spDestino.getSituacao());
    }

    @Test
    @DisplayName("importarAtividades - Sucesso Sem Atualizar Situacao")
    void importarAtividades_SucessoSemAtualizarSituacao() {
        Long codDestino = 2L;
        Long codOrigem = 1L;

        Subprocesso spDestino = new Subprocesso();
        spDestino.setCodigo(codDestino);
        spDestino.setSituacao(SituacaoSubprocesso.NAO_INICIADO);
        Processo proc = new Processo();
        // Tipo null ou outro que não seja MAPEAMENTO ou REVISAO
        proc.setTipo(null);
        spDestino.setProcesso(proc);
        Mapa mapaDestino = new Mapa();
        mapaDestino.setCodigo(20L);
        spDestino.setMapa(mapaDestino);
        Unidade unidadeDestino = new Unidade();
        unidadeDestino.setSigla("DEST");
        spDestino.setUnidade(unidadeDestino);

        Subprocesso spOrigem = new Subprocesso();
        spOrigem.setCodigo(codOrigem);
        Mapa mapaOrigem = new Mapa();
        mapaOrigem.setCodigo(10L);
        spOrigem.setMapa(mapaOrigem);
        Unidade unidadeOrigem = new Unidade();
        unidadeOrigem.setSigla("ORIG");
        spOrigem.setUnidade(unidadeOrigem);

        when(subprocessoRepo.findById(codDestino)).thenReturn(Optional.of(spDestino));
        when(subprocessoRepo.findById(codOrigem)).thenReturn(Optional.of(spOrigem));

        subprocessoFacade.importarAtividades(codDestino, codOrigem);

        verify(copiaMapaService).importarAtividadesDeOutroMapa(10L, 20L);
        verify(subprocessoRepo).save(spDestino);
        assertEquals(SituacaoSubprocesso.NAO_INICIADO, spDestino.getSituacao());
    }

    @Test
    @DisplayName("salvarAjustesMapa - Sucesso Cadastro Homologado")
    void salvarAjustesMapa_SucessoCadastroHomologado() {
        Long codSubprocesso = 1L;
        String usuarioTitulo = "123456789012";

        Subprocesso sp = new Subprocesso();
        sp.setCodigo(codSubprocesso);
        sp.setSituacao(SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA);

        when(subprocessoRepo.findById(codSubprocesso)).thenReturn(Optional.of(sp));

        List<CompetenciaAjusteDto> lista = Collections.emptyList();

        subprocessoFacade.salvarAjustesMapa(codSubprocesso, lista, usuarioTitulo);

        verify(subprocessoRepo).save(sp);
        assertEquals(SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO, sp.getSituacao());
    }

    @Test
    @DisplayName("obterDetalhes - Sucesso")
    void obterDetalhes_Sucesso() {
        Long codigo = 1L;
        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral("123");
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(codigo);
        Unidade unidade = new Unidade();
        unidade.setSigla("SIGLA");
        unidade.setTituloTitular("123");
        sp.setUnidade(unidade);
        Processo processo = new Processo();
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        sp.setProcesso(processo);

        when(usuarioService.obterUsuarioAutenticado()).thenReturn(usuario);
        when(crudService.buscarSubprocesso(codigo)).thenReturn(sp);
        when(usuarioService.buscarResponsavelAtual("SIGLA")).thenReturn(usuario);
        // titular check skips if titualr logic handled
        when(usuarioService.buscarPorLogin("123")).thenReturn(usuario);
        when(repositorioMovimentacao.findBySubprocessoCodigoOrderByDataHoraDesc(codigo)).thenReturn(Collections.emptyList());
        when(accessControlService.podeExecutar(eq(usuario), any(), eq(sp))).thenReturn(true);

        subprocessoFacade.obterDetalhes(codigo, null);

        verify(subprocessoDetalheMapper).toDto(eq(sp), eq(usuario), eq(usuario), anyList(), any());
    }

    @Test
    @DisplayName("obterCadastro - Sucesso")
    void obterCadastro_Sucesso() {
        Long codigo = 1L;
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(codigo);
        Mapa mapa = new Mapa();
        mapa.setCodigo(100L);
        sp.setMapa(mapa);
        Unidade unidade = new Unidade();
        unidade.setSigla("SIGLA");
        sp.setUnidade(unidade);

        when(crudService.buscarSubprocesso(codigo)).thenReturn(sp);
        when(atividadeService.buscarPorMapaCodigoComConhecimentos(100L)).thenReturn(Collections.emptyList());

        var result = subprocessoFacade.obterCadastro(codigo);

        assertNotNull(result);
        assertEquals(codigo, result.getSubprocessoCodigo());
    }

    @Test
    @DisplayName("obterSugestoes - Sucesso")
    void obterSugestoes_Sucesso() {
        Long codigo = 1L;
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(codigo);
        when(crudService.buscarSubprocesso(codigo)).thenReturn(sp);
        var result = subprocessoFacade.obterSugestoes(codigo);
        assertNotNull(result);
    }

    @Test
    @DisplayName("obterMapaParaAjuste - Sucesso")
    void obterMapaParaAjuste_Sucesso() {
        Long codigo = 1L;
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(codigo);
        Mapa mapa = new Mapa();
        mapa.setCodigo(100L);
        sp.setMapa(mapa);

        when(crudService.buscarSubprocessoComMapa(codigo)).thenReturn(sp);
        when(competenciaService.buscarPorCodMapa(100L)).thenReturn(Collections.emptyList());
        when(atividadeService.buscarPorMapaCodigo(100L)).thenReturn(Collections.emptyList());
        when(conhecimentoService.listarPorMapa(100L)).thenReturn(Collections.emptyList());

        subprocessoFacade.obterMapaParaAjuste(codigo);

        verify(mapaAjusteMapper).toDto(eq(sp), any(), anyList(), anyList(), anyList());
    }

    @Test
    @DisplayName("obterPermissoes - Sucesso")
    void obterPermissoes_Sucesso() {
        Long codigo = 1L;
        Usuario usuario = new Usuario();
        Subprocesso sp = new Subprocesso();
        Processo processo = new Processo();
        processo.setTipo(TipoProcesso.REVISAO);
        sp.setProcesso(processo);

        when(usuarioService.obterUsuarioAutenticado()).thenReturn(usuario);
        when(crudService.buscarSubprocesso(codigo)).thenReturn(sp);
        when(accessControlService.podeExecutar(eq(usuario), any(), eq(sp))).thenReturn(true);

        var result = subprocessoFacade.obterPermissoes(codigo);
        assertNotNull(result);
    }

    @Test
    @DisplayName("obterContextoEdicao - Sucesso")
    void obterContextoEdicao_Sucesso() {
        Long codigo = 1L;
        Usuario usuario = new Usuario();
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(codigo);
        Unidade unidade = new Unidade();
        unidade.setSigla("SIGLA");
        sp.setUnidade(unidade);
        Processo processo = new Processo();
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        sp.setProcesso(processo);
        Mapa mapa = new Mapa();
        mapa.setCodigo(100L);
        sp.setMapa(mapa);

        when(usuarioService.obterUsuarioAutenticado()).thenReturn(usuario);
        when(crudService.buscarSubprocesso(codigo)).thenReturn(sp);
        when(usuarioService.buscarResponsavelAtual("SIGLA")).thenReturn(usuario);

        // Mock obterDetalhesInterno dependencies
        when(subprocessoDetalheMapper.toDto(any(), any(), any(), any(), any())).thenReturn(
            sgc.subprocesso.dto.SubprocessoDetalheDto.builder()
                .unidade(sgc.subprocesso.dto.SubprocessoDetalheDto.UnidadeDto.builder().sigla("SIGLA").build())
                .build()
        );

        when(unidadeFacade.buscarPorSigla("SIGLA")).thenReturn(new sgc.organizacao.dto.UnidadeDto());
        when(mapaFacade.obterMapaCompleto(100L, codigo)).thenReturn(new sgc.mapa.dto.MapaCompletoDto());
        when(atividadeService.buscarPorMapaCodigoComConhecimentos(100L)).thenReturn(Collections.emptyList());

        subprocessoFacade.obterContextoEdicao(codigo, null);

        verify(mapaFacade).obterMapaCompleto(100L, codigo);
    }
}
