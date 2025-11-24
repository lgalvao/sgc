package sgc.subprocesso.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import sgc.atividade.model.Atividade;
import sgc.atividade.model.AtividadeRepo;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.comum.erros.ErroValidacao;
import sgc.mapa.dto.MapaCompletoDto;
import sgc.mapa.dto.SalvarMapaRequest;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.CompetenciaRepo;
import sgc.mapa.model.Mapa;
import sgc.mapa.service.CompetenciaService;
import sgc.mapa.service.MapaService;
import sgc.processo.eventos.EventoSubprocessoMapaDisponibilizado;
import sgc.sgrh.model.Usuario;
import sgc.subprocesso.dto.CompetenciaReq;
import sgc.subprocesso.dto.DisponibilizarMapaRequest;
import sgc.subprocesso.erros.ErroMapaEmSituacaoInvalida;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.unidade.model.Unidade;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubprocessoMapaWorkflowServiceTest {

    @Mock private SubprocessoRepo repositorioSubprocesso;
    @Mock private CompetenciaRepo repositorioCompetencia;
    @Mock private AtividadeRepo atividadeRepo;
    @Mock private MapaService mapaService;
    @Mock private CompetenciaService competenciaService;
    @Mock private ApplicationEventPublisher publicadorDeEventos;

    @InjectMocks private SubprocessoMapaWorkflowService service;

    @Test
    @DisplayName("salvarMapaSubprocesso sucesso transição estado")
    void salvarMapaSubprocesso() {
        Long id = 1L;
        Subprocesso sp = new Subprocesso();
        sp.setSituacao(SituacaoSubprocesso.CADASTRO_HOMOLOGADO);
        sp.setMapa(new Mapa());
        sp.getMapa().setCodigo(10L);

        SalvarMapaRequest req = new SalvarMapaRequest();
        req.setCompetencias(List.of(new sgc.mapa.dto.CompetenciaMapaDto())); // tem novas

        when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(sp));
        when(repositorioCompetencia.findByMapaCodigo(10L)).thenReturn(Collections.emptyList()); // era vazio
        when(mapaService.salvarMapaCompleto(any(), any(), any())).thenReturn(new MapaCompletoDto());

        service.salvarMapaSubprocesso(id, req, "user");

        assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPA_CRIADO);
        verify(repositorioSubprocesso).save(sp);
    }

    @Test
    @DisplayName("salvarMapaSubprocesso falha situação inválida")
    void salvarMapaSubprocessoFalha() {
        Long id = 1L;
        Subprocesso sp = new Subprocesso();
        sp.setSituacao(SituacaoSubprocesso.MAPA_DISPONIBILIZADO);

        when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(sp));

        assertThatThrownBy(() -> service.salvarMapaSubprocesso(id, new SalvarMapaRequest(), "user"))
            .isInstanceOf(ErroMapaEmSituacaoInvalida.class);
    }

    @Test
    @DisplayName("Delegation methods")
    void delegationMethods() {
        Long id = 1L;
        Subprocesso sp = new Subprocesso();
        sp.setSituacao(SituacaoSubprocesso.MAPA_CRIADO);
        sp.setMapa(new Mapa());
        sp.getMapa().setCodigo(10L);

        when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(sp));
        when(mapaService.obterMapaCompleto(10L, id)).thenReturn(new MapaCompletoDto());

        service.adicionarCompetencia(id, new CompetenciaReq(), "user");
        service.atualizarCompetencia(id, 100L, new CompetenciaReq(), "user");
        service.removerCompetencia(id, 100L, "user");

        verify(competenciaService).adicionarCompetencia(any(), any(), any());
        verify(competenciaService).atualizarCompetencia(any(), any(), any());
        verify(competenciaService).removerCompetencia(any());
    }

    @Test
    @DisplayName("disponibilizarMapa sucesso")
    void disponibilizarMapa() {
        Long id = 1L;
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(id);
        sp.setSituacao(SituacaoSubprocesso.MAPA_CRIADO);
        sp.setMapa(new Mapa());
        sp.getMapa().setCodigo(10L);
        sp.setUnidade(new Unidade());

        Competencia comp = new Competencia();
        comp.setAtividades(Set.of(new Atividade()));
        Atividade ativ = new Atividade();
        ativ.setCodigo(100L);
        comp.getAtividades().iterator().next().setCodigo(100L);

        when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(sp));
        when(repositorioCompetencia.findByMapaCodigo(10L)).thenReturn(List.of(comp));
        when(atividadeRepo.findBySubprocessoCodigo(id)).thenReturn(List.of(ativ));

        service.disponibilizarMapa(id, new DisponibilizarMapaRequest(), new Usuario());

        assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPA_DISPONIBILIZADO);
        verify(publicadorDeEventos).publishEvent(any(EventoSubprocessoMapaDisponibilizado.class));
    }

    @Test
    @DisplayName("disponibilizarMapa falha validação")
    void disponibilizarMapaFalha() {
        Long id = 1L;
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(id);
        sp.setSituacao(SituacaoSubprocesso.MAPA_CRIADO);
        sp.setMapa(new Mapa());
        sp.getMapa().setCodigo(10L);

        Competencia comp = new Competencia();
        comp.setAtividades(Collections.emptySet()); // sem atividade

        when(repositorioSubprocesso.findById(id)).thenReturn(Optional.of(sp));
        when(repositorioCompetencia.findByMapaCodigo(10L)).thenReturn(List.of(comp));

        assertThatThrownBy(() -> service.disponibilizarMapa(id, new DisponibilizarMapaRequest(), new Usuario()))
            .isInstanceOf(ErroValidacao.class);
    }
}
