package sgc.subprocesso.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.analise.AnaliseService;
import sgc.comum.erros.ErroValidacao;
import sgc.mapa.dto.MapaCompletoDto;
import sgc.mapa.dto.SalvarMapaRequest;
import sgc.mapa.model.Mapa;
import sgc.mapa.service.AtividadeService;
import sgc.mapa.service.CompetenciaService;
import sgc.mapa.service.MapaService;
import sgc.organizacao.UnidadeService;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.processo.model.Processo;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.dto.CompetenciaReq;
import sgc.subprocesso.dto.DisponibilizarMapaRequest;
import sgc.subprocesso.erros.ErroMapaEmSituacaoInvalida;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubprocessoMapaWorkflowServiceCoverageTest {

    @InjectMocks
    private SubprocessoMapaWorkflowService service;

    @Mock private SubprocessoRepo subprocessoRepo;
    @Mock private CompetenciaService competenciaService;
    @Mock private AtividadeService atividadeService;
    @Mock private MapaService mapaService;
    @Mock private SubprocessoTransicaoService transicaoService;
    @Mock private AnaliseService analiseService;
    @Mock private UnidadeService unidadeService;
    @Mock private sgc.subprocesso.service.decomposed.SubprocessoValidacaoService validacaoService;
    @Mock private SubprocessoWorkflowExecutor workflowExecutor;

    // --- SALVAR MAPA ---

    @Test
    @DisplayName("salvarMapaSubprocesso: erro se situacao invalida")
    void salvarMapaSubprocesso_ErroSituacao() {
        Subprocesso sp = new Subprocesso();
        sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO); // Invalido

        when(subprocessoRepo.findById(1L)).thenReturn(Optional.of(sp));

        SalvarMapaRequest request = new SalvarMapaRequest();
        assertThatThrownBy(() -> service.salvarMapaSubprocesso(1L, request))
            .isInstanceOf(ErroMapaEmSituacaoInvalida.class);
    }

    @Test
    @DisplayName("salvarMapaSubprocesso: erro se mapa null")
    void salvarMapaSubprocesso_ErroMapaNull() {
        Subprocesso sp = new Subprocesso();
        sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
        sp.setMapa(null);

        when(subprocessoRepo.findById(1L)).thenReturn(Optional.of(sp));

        SalvarMapaRequest request = new SalvarMapaRequest();
        assertThatThrownBy(() -> service.salvarMapaSubprocesso(1L, request))
            .isInstanceOf(sgc.comum.erros.ErroEstadoImpossivel.class);
    }

    @Test
    @DisplayName("salvarMapaSubprocesso: muda situacao se era vazio e adicionou competencias")
    void salvarMapaSubprocesso_MudaSituacao() {
        Subprocesso sp = new Subprocesso();
        sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);
        Mapa mapa = new Mapa(); mapa.setCodigo(10L);
        sp.setMapa(mapa);

        when(subprocessoRepo.findById(1L)).thenReturn(Optional.of(sp));
        when(competenciaService.buscarPorCodMapa(10L)).thenReturn(Collections.emptyList());

        SalvarMapaRequest req = new SalvarMapaRequest();
        req.setCompetencias(List.of(new sgc.mapa.dto.CompetenciaMapaDto())); // Tem competencia

        when(mapaService.salvarMapaCompleto(any(), any())).thenReturn(new MapaCompletoDto());

        service.salvarMapaSubprocesso(1L, req);

        assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
        verify(subprocessoRepo).save(sp);
    }

    // --- ADICIONAR COMPETENCIA ---

    @Test
    @DisplayName("adicionarCompetencia: muda situacao se era vazio")
    void adicionarCompetencia_MudaSituacao() {
        Subprocesso sp = new Subprocesso();
        sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);
        Mapa mapa = new Mapa(); mapa.setCodigo(10L);
        sp.setMapa(mapa);

        when(subprocessoRepo.findById(1L)).thenReturn(Optional.of(sp));
        when(competenciaService.buscarPorCodMapa(10L)).thenReturn(Collections.emptyList());

        CompetenciaReq req = new CompetenciaReq();
        req.setDescricao("Nova");

        when(mapaService.obterMapaCompleto(any(), any())).thenReturn(new MapaCompletoDto());

        service.adicionarCompetencia(1L, req);

        assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
        verify(subprocessoRepo).save(sp);
    }

    // --- REMOVER COMPETENCIA ---

    @Test
    @DisplayName("removerCompetencia: volta situacao se ficou vazio")
    void removerCompetencia_VoltaSituacao() {
        Subprocesso sp = new Subprocesso();
        sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
        Mapa mapa = new Mapa(); mapa.setCodigo(10L);
        sp.setMapa(mapa);

        when(subprocessoRepo.findById(1L)).thenReturn(Optional.of(sp));

        // Simula que ficou vazio apos remover
        when(competenciaService.buscarPorCodMapa(10L)).thenReturn(Collections.emptyList());

        when(mapaService.obterMapaCompleto(any(), any())).thenReturn(new MapaCompletoDto());

        service.removerCompetencia(1L, 100L);

        assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);
        verify(subprocessoRepo).save(sp);
    }

    // --- DISPONIBILIZAR MAPA ---

    @Test
    @DisplayName("disponibilizarMapa: erro se data limite null")
    void disponibilizarMapa_ErroData() {
        Subprocesso sp = new Subprocesso();
        sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
        Mapa mapa = new Mapa(); mapa.setCodigo(10L);
        sp.setMapa(mapa);

        when(subprocessoRepo.findById(1L)).thenReturn(Optional.of(sp));
        // Validacoes de mapa passam
        when(competenciaService.buscarPorCodMapa(10L)).thenReturn(Collections.emptyList());
        when(atividadeService.buscarPorMapaCodigo(10L)).thenReturn(Collections.emptyList());

        DisponibilizarMapaRequest req = new DisponibilizarMapaRequest();
        Usuario user = new Usuario();

        assertThatThrownBy(() -> service.disponibilizarMapa(1L, req, user))
            .isInstanceOf(ErroValidacao.class)
            .hasMessageContaining("data limite");
    }

    @Test
    @DisplayName("validarMapaParaDisponibilizacao: erro se competencia sem atividade")
    void validarMapa_ErroCompetenciaSemAtividade() {
        Subprocesso sp = new Subprocesso();
        sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
        Mapa mapa = new Mapa(); mapa.setCodigo(10L);
        sp.setMapa(mapa);

        when(subprocessoRepo.findById(1L)).thenReturn(Optional.of(sp));

        sgc.mapa.model.Competencia comp = new sgc.mapa.model.Competencia();
        comp.setAtividades(Collections.emptySet()); // Sem atividade

        when(competenciaService.buscarPorCodMapa(10L)).thenReturn(List.of(comp));

        DisponibilizarMapaRequest request = new DisponibilizarMapaRequest();
        Usuario user = new Usuario();
        assertThatThrownBy(() -> service.disponibilizarMapa(1L, request, user))
            .isInstanceOf(ErroValidacao.class)
            .hasMessageContaining("pelo menos uma atividade");
    }

    @Test
    @DisplayName("validarMapaParaDisponibilizacao: erro se atividade sem competencia")
    void validarMapa_ErroAtividadeSemCompetencia() {
        Subprocesso sp = new Subprocesso();
        sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
        Mapa mapa = new Mapa(); mapa.setCodigo(10L);
        sp.setMapa(mapa);

        when(subprocessoRepo.findById(1L)).thenReturn(Optional.of(sp));

        // Competencia com atividade 1
        sgc.mapa.model.Competencia comp = new sgc.mapa.model.Competencia();
        sgc.mapa.model.Atividade a1 = new sgc.mapa.model.Atividade(); a1.setCodigo(100L);
        comp.setAtividades(java.util.Set.of(a1));

        when(competenciaService.buscarPorCodMapa(10L)).thenReturn(List.of(comp));

        // Atividades do mapa: 1 e 2. A 2 nao esta associada
        sgc.mapa.model.Atividade a2 = new sgc.mapa.model.Atividade(); a2.setCodigo(200L); a2.setDescricao("A2");
        when(atividadeService.buscarPorMapaCodigo(10L)).thenReturn(List.of(a1, a2));

        DisponibilizarMapaRequest request = new DisponibilizarMapaRequest();
        Usuario user = new Usuario();
        assertThatThrownBy(() -> service.disponibilizarMapa(1L, request, user))
            .isInstanceOf(ErroValidacao.class)
            .hasMessageContaining("Atividades pendentes: A2");
    }

    // --- OUTROS FLUXOS DE WORKFLOW ---

    @Test
    @DisplayName("apresentarSugestoes: sucesso")
    void apresentarSugestoes() {
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(1L);
        sp.setMapa(new Mapa());
        Processo p = new Processo(); p.setTipo(TipoProcesso.MAPEAMENTO);
        sp.setProcesso(p);
        Unidade u = new Unidade(); u.setUnidadeSuperior(new Unidade());
        sp.setUnidade(u);

        when(subprocessoRepo.findById(1L)).thenReturn(Optional.of(sp));

        service.apresentarSugestoes(1L, "Obs", new Usuario());

        assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_MAPA_COM_SUGESTOES);
    }

    @Test
    @DisplayName("aceitarValidacao: fim da cadeia (homologacao implicita)")
    void aceitarValidacao_FimCadeia() {
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(1L);
        Processo p = new Processo(); p.setTipo(TipoProcesso.MAPEAMENTO);
        sp.setProcesso(p);

        Unidade pai = new Unidade(); pai.setSigla("PAI");
        Unidade u = new Unidade(); u.setUnidadeSuperior(pai);
        sp.setUnidade(u);
        // pai.unidadeSuperior é null -> fim da cadeia

        when(subprocessoRepo.findById(1L)).thenReturn(Optional.of(sp));
        Usuario user = new Usuario(); user.setTituloEleitoral("123");

        service.aceitarValidacao(1L, user);

        assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO);
        verify(analiseService).criarAnalise(any(), any());
    }
}
