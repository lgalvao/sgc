package sgc.diagnostico.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import sgc.diagnostico.dto.AvaliacaoServidorDto;
import sgc.diagnostico.dto.ConcluirAutoavaliacaoRequest;
import sgc.diagnostico.dto.ConcluirDiagnosticoRequest;
import sgc.diagnostico.dto.DiagnosticoDto;
import sgc.diagnostico.dto.OcupacaoCriticaDto;
import sgc.diagnostico.dto.SalvarAvaliacaoRequest;
import sgc.diagnostico.dto.SalvarOcupacaoRequest;
import sgc.diagnostico.model.AvaliacaoServidor;
import sgc.diagnostico.model.AvaliacaoServidorRepo;
import sgc.diagnostico.model.Diagnostico;
import sgc.diagnostico.model.DiagnosticoRepo;
import sgc.diagnostico.model.NivelAvaliacao;
import sgc.diagnostico.model.OcupacaoCritica;
import sgc.diagnostico.model.OcupacaoCriticaRepo;
import sgc.diagnostico.model.SituacaoDiagnostico;
import sgc.diagnostico.model.SituacaoCapacitacao;
import sgc.diagnostico.model.SituacaoServidorDiagnostico;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.CompetenciaRepo;
import sgc.mapa.model.Mapa;
import sgc.unidade.model.Unidade;
import sgc.sgrh.model.Usuario;
import sgc.sgrh.model.UsuarioRepo;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;

@ExtendWith(MockitoExtension.class)
class DiagnosticoServiceTest {

    @InjectMocks
    private DiagnosticoService service;

    @Mock
    private DiagnosticoRepo diagnosticoRepo;
    @Mock
    private AvaliacaoServidorRepo avaliacaoServidorRepo;
    @Mock
    private OcupacaoCriticaRepo ocupacaoCriticaRepo;
    @Mock
    private SubprocessoRepo subprocessoRepo;
    @Mock
    private UsuarioRepo usuarioRepo;
    @Mock
    private DiagnosticoDtoService dtoService;
    @Mock
    private CompetenciaRepo competenciaRepo;

    @Mock
    private Diagnostico diagnostico;
    @Mock
    private Subprocesso subprocesso;
    @Mock
    private Unidade unidade;
    @Mock
    private Usuario usuario;
    @Mock
    private Mapa mapa;
    @Mock
    private Competencia competencia;
    @Mock
    private AvaliacaoServidor avaliacao;
    @Mock
    private OcupacaoCritica ocupacao;

    @Test
    @DisplayName("Deve buscar diagnostico completo criando novo se não existir")
    void deveBuscarDiagnosticoCompleto() {
        Long subprocessoCodigo = 1L;
        Long unidadeCodigo = 10L;

        when(diagnosticoRepo.findBySubprocessoCodigo(subprocessoCodigo)).thenReturn(Optional.of(diagnostico));
        when(diagnostico.getSubprocesso()).thenReturn(subprocesso);
        when(subprocesso.getUnidade()).thenReturn(unidade);
        when(unidade.getCodigo()).thenReturn(unidadeCodigo);

        when(usuarioRepo.findByUnidadeLotacaoCodigo(unidadeCodigo)).thenReturn(Collections.emptyList());
        when(avaliacaoServidorRepo.findByDiagnosticoCodigo(any())).thenReturn(Collections.emptyList());
        when(ocupacaoCriticaRepo.findByDiagnosticoCodigo(any())).thenReturn(Collections.emptyList());

        when(subprocesso.getMapa()).thenReturn(mapa);
        when(mapa.getCodigo()).thenReturn(5L);
        when(competenciaRepo.findByMapaCodigo(5L)).thenReturn(Collections.emptyList());

        DiagnosticoDto dtoMock = mock(DiagnosticoDto.class);
        when(dtoService.toDto(eq(diagnostico), anyList(), anyBoolean(), any())).thenReturn(dtoMock);

        DiagnosticoDto result = service.buscarDiagnosticoCompleto(subprocessoCodigo);

        assertThat(result).isNotNull();
        verify(diagnosticoRepo).findBySubprocessoCodigo(subprocessoCodigo);
        verify(dtoService).toDto(eq(diagnostico), anyList(), anyBoolean(), any());
    }

    @Test
    @DisplayName("Deve salvar avaliação corretamente")
    void deveSalvarAvaliacao() {
        Long subprocessoCodigo = 1L;
        String servidorTitulo = "123";
        Long competenciaCodigo = 10L;
        SalvarAvaliacaoRequest request = new SalvarAvaliacaoRequest(competenciaCodigo, NivelAvaliacao.N1, NivelAvaliacao.N3, "Obs");

        when(diagnosticoRepo.findBySubprocessoCodigo(subprocessoCodigo)).thenReturn(Optional.of(diagnostico));
        when(usuarioRepo.findById(servidorTitulo)).thenReturn(Optional.of(usuario));
        when(diagnostico.getSubprocesso()).thenReturn(subprocesso);
        when(subprocesso.getMapa()).thenReturn(mapa);
        when(mapa.getCodigo()).thenReturn(5L);
        when(competenciaRepo.findByMapaCodigo(5L)).thenReturn(List.of(competencia));
        when(competencia.getCodigo()).thenReturn(competenciaCodigo);

        when(avaliacaoServidorRepo.findByDiagnosticoCodigoAndServidorTituloEleitoralAndCompetenciaCodigo(
                any(), eq(servidorTitulo), eq(competenciaCodigo))).thenReturn(Optional.empty());

        when(avaliacaoServidorRepo.save(any(AvaliacaoServidor.class))).thenAnswer(i -> i.getArguments()[0]);
        when(dtoService.toDto(any(AvaliacaoServidor.class))).thenReturn(mock(AvaliacaoServidorDto.class));

        AvaliacaoServidorDto result = service.salvarAvaliacao(subprocessoCodigo, servidorTitulo, request);

        assertThat(result).isNotNull();
        verify(avaliacaoServidorRepo).save(any(AvaliacaoServidor.class));
    }

    @Test
    @DisplayName("Deve concluir autoavaliação com sucesso")
    void deveConcluirAutoavaliacao() {
        Long subprocessoCodigo = 1L;
        String servidorTitulo = "123";
        ConcluirAutoavaliacaoRequest request = new ConcluirAutoavaliacaoRequest("Ok");

        when(diagnosticoRepo.findBySubprocessoCodigo(subprocessoCodigo)).thenReturn(Optional.of(diagnostico));
        when(diagnostico.getSubprocesso()).thenReturn(subprocesso);
        when(subprocesso.getMapa()).thenReturn(mapa);
        when(mapa.getCodigo()).thenReturn(5L);
        when(competenciaRepo.findByMapaCodigo(5L)).thenReturn(List.of(competencia));

        when(avaliacaoServidorRepo.findByDiagnosticoCodigoAndServidorTituloEleitoral(any(), eq(servidorTitulo)))
                .thenReturn(List.of(avaliacao));

        when(avaliacao.getImportancia()).thenReturn(NivelAvaliacao.N1);
        when(avaliacao.getDominio()).thenReturn(NivelAvaliacao.N1);

        service.concluirAutoavaliacao(subprocessoCodigo, servidorTitulo, request);

        verify(avaliacaoServidorRepo).saveAll(anyList());
        verify(avaliacao).setSituacao(SituacaoServidorDiagnostico.AUTOAVALIACAO_CONCLUIDA);
    }

    @Test
    @DisplayName("Deve falhar concluir autoavaliação se houver pendencias")
    void naoDeveConcluirAutoavaliacaoComPendencias() {
        Long subprocessoCodigo = 1L;
        String servidorTitulo = "123";
        ConcluirAutoavaliacaoRequest request = new ConcluirAutoavaliacaoRequest("Ok");

        when(diagnosticoRepo.findBySubprocessoCodigo(subprocessoCodigo)).thenReturn(Optional.of(diagnostico));
        when(diagnostico.getSubprocesso()).thenReturn(subprocesso);
        when(subprocesso.getMapa()).thenReturn(mapa);
        when(mapa.getCodigo()).thenReturn(5L);
        when(competenciaRepo.findByMapaCodigo(5L)).thenReturn(List.of(competencia));

        when(avaliacaoServidorRepo.findByDiagnosticoCodigoAndServidorTituloEleitoral(any(), eq(servidorTitulo)))
                .thenReturn(List.of(avaliacao));

        when(avaliacao.getImportancia()).thenReturn(null); // Pendente

        assertThatThrownBy(() -> service.concluirAutoavaliacao(subprocessoCodigo, servidorTitulo, request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("incompletas");
    }

    @Test
    @DisplayName("Deve salvar ocupação crítica")
    void deveSalvarOcupacao() {
        Long subprocessoCodigo = 1L;
        Long competenciaCodigo = 10L;
        String servidorTitulo = "123";
        SalvarOcupacaoRequest request = new SalvarOcupacaoRequest(servidorTitulo, competenciaCodigo, SituacaoCapacitacao.AC);

        when(diagnosticoRepo.findBySubprocessoCodigo(subprocessoCodigo)).thenReturn(Optional.of(diagnostico));
        when(usuarioRepo.findById(servidorTitulo)).thenReturn(Optional.of(usuario));
        when(diagnostico.getSubprocesso()).thenReturn(subprocesso);
        when(subprocesso.getMapa()).thenReturn(mapa);
        when(mapa.getCodigo()).thenReturn(5L);
        when(competenciaRepo.findByMapaCodigo(5L)).thenReturn(List.of(competencia));
        when(competencia.getCodigo()).thenReturn(competenciaCodigo);

        when(ocupacaoCriticaRepo.findByDiagnosticoCodigoAndServidorTituloEleitoralAndCompetenciaCodigo(
                any(), eq(servidorTitulo), eq(competenciaCodigo))).thenReturn(Optional.empty());

        when(ocupacaoCriticaRepo.save(any(OcupacaoCritica.class))).thenAnswer(i -> i.getArguments()[0]);
        when(dtoService.toDto(any(OcupacaoCritica.class))).thenReturn(mock(OcupacaoCriticaDto.class));

        OcupacaoCriticaDto result = service.salvarOcupacao(subprocessoCodigo, request);

        assertThat(result).isNotNull();
        verify(ocupacaoCriticaRepo).save(any(OcupacaoCritica.class));
    }

    @Test
    @DisplayName("Deve concluir diagnóstico")
    void deveConcluirDiagnostico() {
        Long subprocessoCodigo = 1L;
        ConcluirDiagnosticoRequest request = new ConcluirDiagnosticoRequest("Justificativa");

        when(diagnosticoRepo.findBySubprocessoCodigo(subprocessoCodigo)).thenReturn(Optional.of(diagnostico));

        // Mocking buscarDiagnosticoCompleto internals somewhat redundantly to ensure logic passes
        when(diagnostico.getSubprocesso()).thenReturn(subprocesso);
        when(subprocesso.getUnidade()).thenReturn(unidade);
        when(unidade.getCodigo()).thenReturn(10L);
        when(usuarioRepo.findByUnidadeLotacaoCodigo(10L)).thenReturn(Collections.emptyList());
        when(avaliacaoServidorRepo.findByDiagnosticoCodigo(any())).thenReturn(Collections.emptyList());
        when(ocupacaoCriticaRepo.findByDiagnosticoCodigo(any())).thenReturn(Collections.emptyList());

        when(subprocesso.getMapa()).thenReturn(mapa);
        when(mapa.getCodigo()).thenReturn(5L);
        when(competenciaRepo.findByMapaCodigo(5L)).thenReturn(Collections.emptyList());

        DiagnosticoDto overview = mock(DiagnosticoDto.class);
        when(overview.servidores()).thenReturn(Collections.emptyList());

        when(dtoService.toDto(eq(diagnostico), anyList(), anyBoolean(), any())).thenReturn(overview);

        service.concluirDiagnostico(subprocessoCodigo, request);

        verify(diagnostico).setSituacao(SituacaoDiagnostico.CONCLUIDO);
        verify(diagnosticoRepo).save(diagnostico);
        verify(subprocessoRepo).save(subprocesso);
    }
}
