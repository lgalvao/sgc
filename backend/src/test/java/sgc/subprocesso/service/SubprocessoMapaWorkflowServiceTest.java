package sgc.subprocesso.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.analise.AnaliseService;
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
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubprocessoMapaWorkflowService Test")
class SubprocessoMapaWorkflowServiceTest {

    @Mock
    private SubprocessoRepo subprocessoRepo;
    @Mock
    private CompetenciaService competenciaService;
    @Mock
    private AtividadeService atividadeService;
    @Mock
    private MapaService mapaService;
    @Mock
    private SubprocessoTransicaoService transicaoService;
    @Mock
    private AnaliseService analiseService;
    @Mock
    private UnidadeService unidadeService;
    @Mock
    private SubprocessoService subprocessoService;
    @Mock
    private SubprocessoWorkflowExecutor workflowExecutor;

    @InjectMocks
    private SubprocessoMapaWorkflowService service;

    @Test
    @DisplayName("Salvar mapa - mudar situação para MAPA_CRIADO se era vazio")
    void salvarMapaMudarSituacao() {
        Subprocesso sp = new Subprocesso();
        sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);
        sp.setMapa(new Mapa());
        sp.getMapa().setCodigo(10L);

        when(subprocessoRepo.findById(1L)).thenReturn(Optional.of(sp));
        when(competenciaService.buscarPorMapa(10L)).thenReturn(Collections.emptyList());
        when(mapaService.salvarMapaCompleto(anyLong(), any(), any())).thenReturn(new MapaCompletoDto());

        SalvarMapaRequest req = new SalvarMapaRequest();
        req.setCompetencias(List.of(new sgc.mapa.dto.CompetenciaMapaDto()));

        service.salvarMapaSubprocesso(1L, req, "user");

        assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
        verify(subprocessoRepo).save(sp);
    }

    @Test
    @DisplayName("Adicionar competencia - mudar situação")
    void adicionarCompetenciaMudarSituacao() {
        Subprocesso sp = new Subprocesso();
        sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);
        sp.setMapa(new Mapa());
        sp.getMapa().setCodigo(10L);

        when(subprocessoRepo.findById(1L)).thenReturn(Optional.of(sp));
        when(competenciaService.buscarPorMapa(10L)).thenReturn(Collections.emptyList());
        when(mapaService.obterMapaCompleto(10L, 1L)).thenReturn(new MapaCompletoDto());

        service.adicionarCompetencia(1L, new CompetenciaReq(), "user");

        assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
        verify(subprocessoRepo).save(sp);
    }

    @Test
    @DisplayName("Remover competencia - voltar para CADASTRO_HOMOLOGADO se ficar vazio")
    void removerCompetenciaMudarSituacao() {
        Subprocesso sp = new Subprocesso();
        sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
        sp.setMapa(new Mapa());
        sp.getMapa().setCodigo(10L);

        when(subprocessoRepo.findById(1L)).thenReturn(Optional.of(sp));
        when(competenciaService.buscarPorMapa(10L)).thenReturn(Collections.emptyList());
        when(mapaService.obterMapaCompleto(10L, 1L)).thenReturn(new MapaCompletoDto());

        service.removerCompetencia(1L, 100L, "user");

        assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);
        verify(subprocessoRepo).save(sp);
    }

    @Test
    @DisplayName("Disponibilizar mapa sucesso (Mapeamento)")
    void disponibilizarMapaMapeamento() {
        Subprocesso sp = new Subprocesso();
        sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
        sp.setProcesso(new Processo());
        sp.getProcesso().setTipo(TipoProcesso.MAPEAMENTO);
        sp.setMapa(new Mapa());
        sp.getMapa().setCodigo(10L);
        sp.setUnidade(new Unidade());

        when(subprocessoRepo.findById(1L)).thenReturn(Optional.of(sp));
        when(unidadeService.buscarPorSigla("SEDOC")).thenReturn(sgc.organizacao.dto.UnidadeDto.builder().build());
        when(unidadeService.buscarEntidadePorId(any())).thenReturn(new Unidade());

        DisponibilizarMapaRequest req = new DisponibilizarMapaRequest();
        req.setDataLimite(LocalDate.now());

        service.disponibilizarMapa(1L, req, new Usuario());

        assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO);
        verify(transicaoService).registrar(any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Apresentar sugestões")
    void apresentarSugestoes() {
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(1L); // Define ID for removerPorSubprocesso call
        sp.setProcesso(new Processo());
        sp.getProcesso().setTipo(TipoProcesso.MAPEAMENTO);
        sp.setUnidade(new Unidade());
        sp.getUnidade().setUnidadeSuperior(new Unidade());
        sp.setMapa(new Mapa());

        when(subprocessoRepo.findById(1L)).thenReturn(Optional.of(sp));

        service.apresentarSugestoes(1L, "Sugestoes", new Usuario());

        assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_MAPA_COM_SUGESTOES);
        verify(analiseService).removerPorSubprocesso(1L);
    }

    @Test
    @DisplayName("Validar mapa")
    void validarMapa() {
        Subprocesso sp = new Subprocesso();
        sp.setProcesso(new Processo());
        sp.getProcesso().setTipo(TipoProcesso.REVISAO);
        sp.setUnidade(new Unidade());
        sp.getUnidade().setUnidadeSuperior(new Unidade());

        when(subprocessoRepo.findById(1L)).thenReturn(Optional.of(sp));

        service.validarMapa(1L, new Usuario());

        assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.REVISAO_MAPA_VALIDADO);
    }

    @Test
    @DisplayName("Devolver validação")
    void devolverValidacao() {
        Subprocesso sp = new Subprocesso();
        sp.setProcesso(new Processo());
        sp.getProcesso().setTipo(TipoProcesso.MAPEAMENTO);
        sp.setUnidade(new Unidade());
        sp.getUnidade().setUnidadeSuperior(new Unidade());

        when(subprocessoRepo.findById(1L)).thenReturn(Optional.of(sp));

        service.devolverValidacao(1L, "Just", new Usuario());

        verify(workflowExecutor).registrarAnaliseETransicao(any(), eq(SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO), any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Homologar validação")
    void homologarValidacao() {
        Subprocesso sp = new Subprocesso();
        sp.setProcesso(new Processo());
        sp.getProcesso().setTipo(TipoProcesso.MAPEAMENTO);

        when(subprocessoRepo.findById(1L)).thenReturn(Optional.of(sp));
        when(unidadeService.buscarPorSigla("SEDOC")).thenReturn(sgc.organizacao.dto.UnidadeDto.builder().build());
        when(unidadeService.buscarEntidadePorId(any())).thenReturn(new Unidade());

        service.homologarValidacao(1L, new Usuario());

        assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO);
    }
}
