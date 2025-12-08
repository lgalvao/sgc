package sgc.diagnostico.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.diagnostico.dto.ConcluirAutoavaliacaoRequest;
import sgc.diagnostico.dto.SalvarAvaliacaoRequest;
import sgc.diagnostico.model.*;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.CompetenciaRepo;
import sgc.mapa.model.Mapa;
import sgc.sgrh.model.Usuario;
import sgc.sgrh.model.UsuarioRepo;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.unidade.model.Unidade;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiagnosticoServiceTest {

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
    private CompetenciaRepo competenciaRepo;
    // DtoService requerida mas difícil de mockar se tiver muita lógica.
    // Como DtoService não tem estado e só converte, vou mockar parcial ou usar real,
    // mas para simplificar aqui vou mockar e stubbar o toDto
    @Mock
    private DiagnosticoDtoService dtoService;

    @InjectMocks
    private DiagnosticoService diagnosticoService;

    private Subprocesso subprocesso;
    private Diagnostico diagnostico;
    private Usuario servidor;
    private Mapa mapa;
    private Competencia competencia;

    @BeforeEach
    void setUp() {
        Unidade unidade = new Unidade();
        unidade.setCodigo(100L);

        mapa = new Mapa();
        mapa.setCodigo(200L);
        mapa.setUnidade(unidade);

        subprocesso = new Subprocesso();
        subprocesso.setCodigo(1L);
        subprocesso.setUnidade(unidade);
        subprocesso.setMapa(mapa);

        diagnostico = new Diagnostico(subprocesso);
        diagnostico.setCodigo(10L);

        servidor = new Usuario();
        servidor.setTituloEleitoral("123456789012");

        competencia = new Competencia(300L, "Competência Teste", mapa);
    }

    @Test
    void buscarOuCriarDiagnosticoEntidade_deveCriarNovo_seNaoExistir() {
        when(diagnosticoRepo.findBySubprocessoCodigo(1L)).thenReturn(Optional.empty());
        when(subprocessoRepo.findById(1L)).thenReturn(Optional.of(subprocesso));
        when(diagnosticoRepo.save(any(Diagnostico.class))).thenAnswer(i -> i.getArgument(0));

        Diagnostico resultado = diagnosticoService.buscarOuCriarDiagnosticoEntidade(1L);

        assertNotNull(resultado);
        assertEquals(subprocesso, resultado.getSubprocesso());
        assertEquals(SituacaoDiagnostico.EM_ANDAMENTO, resultado.getSituacao());
        verify(diagnosticoRepo).save(any(Diagnostico.class));
    }

    @Test
    void salvarAvaliacao_deveSalvar_comDadosCorretos() {
        SalvarAvaliacaoRequest request = new SalvarAvaliacaoRequest(
                competencia.getCodigo(), NivelAvaliacao.N5, NivelAvaliacao.N3, "Obs");

        when(diagnosticoRepo.findBySubprocessoCodigo(1L)).thenReturn(Optional.of(diagnostico));
        when(usuarioRepo.findById("123456789012")).thenReturn(Optional.of(servidor));
        when(competenciaRepo.findByMapaCodigo(mapa.getCodigo())).thenReturn(List.of(competencia));

        when(avaliacaoServidorRepo.findByDiagnosticoCodigoAndServidorTituloEleitoralAndCompetenciaCodigo(
                diagnostico.getCodigo(), "123456789012", competencia.getCodigo()))
                .thenReturn(Optional.empty());

        when(avaliacaoServidorRepo.save(any(AvaliacaoServidor.class))).thenAnswer(i -> {
            AvaliacaoServidor a = i.getArgument(0);
            a.setCodigo(500L);
            return a;
        });

        diagnosticoService.salvarAvaliacao(1L, "123456789012", request);

        verify(avaliacaoServidorRepo).save(any(AvaliacaoServidor.class));
    }

    @Test
    void concluirAutoavaliacao_deveFalhar_seIncompleta() {
        when(diagnosticoRepo.findBySubprocessoCodigo(1L)).thenReturn(Optional.of(diagnostico));
        when(competenciaRepo.findByMapaCodigo(mapa.getCodigo())).thenReturn(List.of(competencia));

        // Retorna lista vazia de avaliações
        when(avaliacaoServidorRepo.findByDiagnosticoCodigoAndServidorTituloEleitoral(
                diagnostico.getCodigo(), "123456789012")).thenReturn(List.of());

        assertThrows(IllegalStateException.class, () ->
            diagnosticoService.concluirAutoavaliacao(1L, "123456789012", new ConcluirAutoavaliacaoRequest(null))
        );
    }

    @Test
    void concluirAutoavaliacao_deveSucesso_seCompleta() {
        when(diagnosticoRepo.findBySubprocessoCodigo(1L)).thenReturn(Optional.of(diagnostico));
        when(competenciaRepo.findByMapaCodigo(mapa.getCodigo())).thenReturn(List.of(competencia));

        AvaliacaoServidor av = new AvaliacaoServidor(diagnostico, servidor, competencia);
        av.setImportancia(NivelAvaliacao.N1);
        av.setDominio(NivelAvaliacao.N1);

        when(avaliacaoServidorRepo.findByDiagnosticoCodigoAndServidorTituloEleitoral(
                diagnostico.getCodigo(), "123456789012")).thenReturn(List.of(av));

        diagnosticoService.concluirAutoavaliacao(1L, "123456789012", new ConcluirAutoavaliacaoRequest(null));

        verify(avaliacaoServidorRepo).saveAll(any());
        assertEquals(SituacaoServidorDiagnostico.AUTOAVALIACAO_CONCLUIDA, av.getSituacao());
    }
}
