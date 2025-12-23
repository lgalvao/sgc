package sgc.subprocesso.internal.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.context.ApplicationEventPublisher;
import sgc.analise.AnaliseService;
import sgc.atividade.internal.model.AtividadeRepo;
import sgc.comum.erros.ErroValidacao;
import sgc.mapa.api.MapaCompletoDto;
import sgc.mapa.internal.model.Competencia;
import sgc.mapa.internal.model.CompetenciaRepo;
import sgc.mapa.internal.model.Mapa;
import sgc.mapa.internal.service.CompetenciaService;
import sgc.mapa.MapaService;
import sgc.processo.api.eventos.*;
import sgc.processo.internal.model.Processo;
import sgc.processo.internal.model.TipoProcesso;
import sgc.sgrh.internal.model.Usuario;
import sgc.subprocesso.api.DisponibilizarMapaRequest;
import sgc.subprocesso.api.SubmeterMapaAjustadoReq;
import sgc.subprocesso.internal.erros.ErroMapaNaoAssociado;
import sgc.subprocesso.internal.model.SituacaoSubprocesso;
import sgc.subprocesso.internal.model.Subprocesso;
import sgc.subprocesso.internal.model.SubprocessoRepo;
import sgc.unidade.internal.model.Unidade;
import sgc.unidade.internal.model.UnidadeRepo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubprocessoMapaWorkflowServiceTest {

    @Mock private SubprocessoRepo subprocessoRepo;
    @Mock private CompetenciaRepo competenciaRepo;
    @Mock private AtividadeRepo atividadeRepo;
    @Mock private MapaService mapaService;
    @Mock private CompetenciaService competenciaService;
    @Mock private ApplicationEventPublisher publicadorDeEventos;
    @Mock private AnaliseService analiseService;
    @Mock private UnidadeRepo unidadeRepo;
    @Mock private SubprocessoService subprocessoService;

    @InjectMocks
    private SubprocessoMapaWorkflowService service;

    // --- Disponibilizar Mapa ---

    @Test
    @DisplayName("disponibilizarMapa sucesso")
    void disponibilizarMapa() {
        Long id = 1L;
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(id);
        sp.setSituacao(SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA);
        Mapa mapa = new Mapa();
        mapa.setCodigo(10L);
        sp.setMapa(mapa);
        Unidade u = new Unidade();
        sp.setUnidade(u);

        Processo p = new Processo();
        p.setTipo(TipoProcesso.MAPEAMENTO);
        sp.setProcesso(p);

        Usuario user = new Usuario();
        Unidade sedoc = new Unidade();

        DisponibilizarMapaRequest request = DisponibilizarMapaRequest.builder()
                .dataLimite(LocalDate.now().plusDays(10))
                .observacoes("obs")
                .build();

        when(subprocessoRepo.findById(id)).thenReturn(Optional.of(sp));
        when(unidadeRepo.findBySigla("SEDOC")).thenReturn(Optional.of(sedoc));
        // Mocks para validação interna de mapa
        when(competenciaRepo.findByMapaCodigo(10L)).thenReturn(Collections.emptyList());
        when(atividadeRepo.findBySubprocessoCodigo(id)).thenReturn(Collections.emptyList());

        service.disponibilizarMapa(id, request, user);

        assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO);
        verify(publicadorDeEventos).publishEvent(any(EventoSubprocessoMapaDisponibilizado.class));
    }

    @Test
    @DisplayName("disponibilizarMapa falha estado invalido")
    void disponibilizarMapaEstadoInvalido() {
        Long id = 1L;
        Subprocesso sp = new Subprocesso();
        sp.setSituacao(SituacaoSubprocesso.NAO_INICIADO);

        when(subprocessoRepo.findById(id)).thenReturn(Optional.of(sp));

        DisponibilizarMapaRequest request = DisponibilizarMapaRequest.builder()
                .dataLimite(LocalDate.now().plusDays(10))
                .build();

        assertThatThrownBy(() -> service.disponibilizarMapa(id, request, new Usuario()))
                .isInstanceOf(sgc.subprocesso.internal.erros.ErroMapaEmSituacaoInvalida.class);
    }

    // --- Apresentar Sugestões ---

    @Test
    @DisplayName("apresentarSugestoes sucesso")
    void apresentarSugestoes() {
        Long id = 1L;
        Subprocesso sp = new Subprocesso();
        sp.setUnidade(new Unidade());
        sp.getUnidade().setUnidadeSuperior(new Unidade());
        sp.setMapa(new Mapa());

        Processo p = new Processo();
        p.setTipo(TipoProcesso.MAPEAMENTO);
        sp.setProcesso(p);

        Usuario user = new Usuario();

        when(subprocessoRepo.findById(id)).thenReturn(Optional.of(sp));

        service.apresentarSugestoes(id, "sug", user);

        assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_MAPA_COM_SUGESTOES);
        assertThat(sp.getMapa().getSugestoes()).isEqualTo("sug");
        verify(publicadorDeEventos).publishEvent(any(EventoSubprocessoMapaComSugestoes.class));
    }

    // --- Validar Mapa ---

    @Test
    @DisplayName("validarMapa sucesso")
    void validarMapa() {
        Long id = 1L;
        Subprocesso sp = new Subprocesso();
        sp.setUnidade(new Unidade());

        Processo p = new Processo();
        p.setTipo(TipoProcesso.MAPEAMENTO);
        sp.setProcesso(p);

        Usuario user = new Usuario();

        when(subprocessoRepo.findById(id)).thenReturn(Optional.of(sp));

        service.validarMapa(id, user);

        assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO);
        verify(publicadorDeEventos).publishEvent(any(EventoSubprocessoMapaValidado.class));
    }

    // --- Devolver Validação ---

    @Test
    @DisplayName("devolverValidacao sucesso")
    void devolverValidacao() {
        Long id = 1L;
        Subprocesso sp = new Subprocesso();
        Unidade u = new Unidade();
        u.setSigla("U1");
        Unidade sup = new Unidade();
        sup.setSigla("SUP");
        u.setUnidadeSuperior(sup);
        sp.setUnidade(u);

        Processo p = new Processo();
        p.setTipo(TipoProcesso.MAPEAMENTO);
        sp.setProcesso(p);

        Usuario user = new Usuario();

        when(subprocessoRepo.findById(id)).thenReturn(Optional.of(sp));

        service.devolverValidacao(id, "justificativa", user);

        assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO);
        verify(analiseService).criarAnalise(any());
        verify(publicadorDeEventos).publishEvent(any(EventoSubprocessoMapaDevolvido.class));
    }

    // --- Aceitar Validação ---

    @Test
    @DisplayName("aceitarValidacao homologado se nao houver proxima unidade")
    void aceitarValidacaoHomologado() {
        Long id = 1L;
        Subprocesso sp = new Subprocesso();
        Unidade u = new Unidade();
        Unidade sup = new Unidade();
        sup.setSigla("SUP");
        u.setUnidadeSuperior(sup); // Sup nao tem superior
        sp.setUnidade(u);

        Processo p = new Processo();
        p.setTipo(TipoProcesso.MAPEAMENTO);
        sp.setProcesso(p);

        Usuario user = new Usuario();

        when(subprocessoRepo.findById(id)).thenReturn(Optional.of(sp));

        service.aceitarValidacao(id, user);

        assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO);
    }

    @Test
    @DisplayName("aceitarValidacao validado se houver proxima unidade")
    void aceitarValidacaoProximaEtapa() {
        Long id = 1L;
        Subprocesso sp = new Subprocesso();
        Unidade u = new Unidade();
        Unidade sup = new Unidade();
        sup.setSigla("SUP");
        Unidade sup2 = new Unidade();
        sup.setUnidadeSuperior(sup2);
        u.setUnidadeSuperior(sup);
        sp.setUnidade(u);

        Processo p = new Processo();
        p.setTipo(TipoProcesso.MAPEAMENTO);
        sp.setProcesso(p);

        Usuario user = new Usuario();

        when(subprocessoRepo.findById(id)).thenReturn(Optional.of(sp));

        service.aceitarValidacao(id, user);

        assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO);
        verify(publicadorDeEventos).publishEvent(any(EventoSubprocessoMapaAceito.class));
    }

    // --- Homologar Validação ---

    @Test
    @DisplayName("homologarValidacao sucesso")
    void homologarValidacao() {
        Long id = 1L;
        Subprocesso sp = new Subprocesso();

        Processo p = new Processo();
        p.setTipo(TipoProcesso.MAPEAMENTO);
        sp.setProcesso(p);

        Usuario user = new Usuario();
        Unidade sedoc = new Unidade();

        when(subprocessoRepo.findById(id)).thenReturn(Optional.of(sp));
        when(unidadeRepo.findBySigla("SEDOC")).thenReturn(Optional.of(sedoc));

        service.homologarValidacao(id, user);

        assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO);
        verify(publicadorDeEventos).publishEvent(any(EventoSubprocessoMapaHomologado.class));
    }

    // --- Submeter Mapa Ajustado ---

    @Test
    @DisplayName("submeterMapaAjustado sucesso")
    void submeterMapaAjustado() {
        Long id = 1L;
        Subprocesso sp = new Subprocesso();
        sp.setUnidade(new Unidade());
        sp.setMapa(new Mapa());
        sp.getMapa().setCodigo(10L);

        Processo p = new Processo();
        p.setTipo(TipoProcesso.MAPEAMENTO);
        sp.setProcesso(p);

        Usuario user = new Usuario();
        SubmeterMapaAjustadoReq req = new SubmeterMapaAjustadoReq();
        req.setDataLimiteEtapa2(LocalDateTime.now());

        when(subprocessoRepo.findById(id)).thenReturn(Optional.of(sp));

        service.submeterMapaAjustado(id, req, user);

        assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO);
        verify(publicadorDeEventos).publishEvent(any(EventoSubprocessoMapaAjustadoSubmetido.class));
    }
}