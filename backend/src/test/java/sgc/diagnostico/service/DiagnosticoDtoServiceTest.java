package sgc.diagnostico.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import sgc.diagnostico.dto.AvaliacaoServidorDto;
import sgc.diagnostico.dto.DiagnosticoDto;
import sgc.diagnostico.dto.OcupacaoCriticaDto;
import sgc.diagnostico.dto.ServidorDiagnosticoDto;
import sgc.diagnostico.model.AvaliacaoServidor;
import sgc.diagnostico.model.Diagnostico;
import sgc.diagnostico.model.NivelAvaliacao;
import sgc.diagnostico.model.OcupacaoCritica;
import sgc.diagnostico.model.SituacaoCapacitacao;
import sgc.diagnostico.model.SituacaoDiagnostico;
import sgc.diagnostico.model.SituacaoServidorDiagnostico;
import sgc.mapa.model.Competencia;
import sgc.sgrh.model.Usuario;
import sgc.subprocesso.model.Subprocesso;

@ExtendWith(MockitoExtension.class)
class DiagnosticoDtoServiceTest {

    private DiagnosticoDtoService service;

    @Mock
    private Diagnostico diagnostico;
    @Mock
    private Subprocesso subprocesso;
    @Mock
    private Usuario servidor;
    @Mock
    private AvaliacaoServidor avaliacao;
    @Mock
    private OcupacaoCritica ocupacao;
    @Mock
    private Competencia competencia;

    @BeforeEach
    void setUp() {
        service = new DiagnosticoDtoService();
    }

    @Test
    @DisplayName("Deve converter Diagnostico para DiagnosticoDto corretamente")
    void deveConverterDiagnosticoParaDto() {
        when(diagnostico.getCodigo()).thenReturn(1L);
        when(diagnostico.getSubprocesso()).thenReturn(subprocesso);
        when(subprocesso.getCodigo()).thenReturn(2L);
        when(diagnostico.getSituacao()).thenReturn(SituacaoDiagnostico.EM_ANDAMENTO);
        when(diagnostico.getDataConclusao()).thenReturn(null);
        when(diagnostico.getJustificativaConclusao()).thenReturn("Justificativa");

        List<ServidorDiagnosticoDto> servidores = Collections.emptyList();
        DiagnosticoDto dto = service.toDto(diagnostico, servidores, true, null);

        assertThat(dto.codigo()).isEqualTo(1L);
        assertThat(dto.subprocessoCodigo()).isEqualTo(2L);
        assertThat(dto.situacao()).isEqualTo("EM_ANDAMENTO");
        assertThat(dto.situacaoLabel()).isEqualTo("Em andamento");
        assertThat(dto.servidores()).isEmpty();
        assertThat(dto.podeSerConcluido()).isTrue();
        assertThat(dto.motivoNaoPodeConcluir()).isNull();
    }

    @Test
    @DisplayName("Deve converter dados do Servidor para ServidorDiagnosticoDto")
    void deveConverterServidorParaDto() {
        when(servidor.getTituloEleitoral()).thenReturn("123");
        when(servidor.getNome()).thenReturn("João");
        when(avaliacao.getSituacao()).thenReturn(SituacaoServidorDiagnostico.AUTOAVALIACAO_NAO_REALIZADA);
        when(avaliacao.getImportancia()).thenReturn(NivelAvaliacao.N1);
        when(avaliacao.getDominio()).thenReturn(NivelAvaliacao.N3);
        when(avaliacao.getCompetencia()).thenReturn(competencia);
        when(competencia.getCodigo()).thenReturn(10L);
        when(competencia.getDescricao()).thenReturn("Competencia 1");

        List<AvaliacaoServidor> avaliacoes = List.of(avaliacao);
        List<OcupacaoCritica> ocupacoes = Collections.emptyList();

        ServidorDiagnosticoDto dto = service.toDto(servidor, avaliacoes, ocupacoes, 5);

        assertThat(dto.tituloEleitoral()).isEqualTo("123");
        assertThat(dto.nome()).isEqualTo("João");
        assertThat(dto.situacao()).isEqualTo("AUTOAVALIACAO_NAO_REALIZADA");
        assertThat(dto.situacaoLabel()).isEqualTo("Autoavaliação não realizada");
        assertThat(dto.avaliacoes()).hasSize(1);
        assertThat(dto.totalCompetencias()).isEqualTo(5);
        assertThat(dto.competenciasAvaliadas()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deve converter AvaliacaoServidor para AvaliacaoServidorDto")
    void deveConverterAvaliacaoParaDto() {
        when(avaliacao.getCodigo()).thenReturn(1L);
        when(avaliacao.getCompetencia()).thenReturn(competencia);
        when(competencia.getCodigo()).thenReturn(10L);
        when(competencia.getDescricao()).thenReturn("Competencia 1");
        when(avaliacao.getImportancia()).thenReturn(NivelAvaliacao.N1);
        when(avaliacao.getDominio()).thenReturn(NivelAvaliacao.N3);
        when(avaliacao.getGap()).thenReturn(1);
        when(avaliacao.getObservacoes()).thenReturn("Obs");

        AvaliacaoServidorDto dto = service.toDto(avaliacao);

        assertThat(dto.codigo()).isEqualTo(1L);
        assertThat(dto.competenciaCodigo()).isEqualTo(10L);
        assertThat(dto.competenciaDescricao()).isEqualTo("Competencia 1");
        assertThat(dto.importancia()).isEqualTo("N1");
        assertThat(dto.dominio()).isEqualTo("N3");
        assertThat(dto.gap()).isEqualTo(1);
        assertThat(dto.observacoes()).isEqualTo("Obs");
    }

    @Test
    @DisplayName("Deve converter OcupacaoCritica para OcupacaoCriticaDto")
    void deveConverterOcupacaoParaDto() {
        when(ocupacao.getCodigo()).thenReturn(1L);
        when(ocupacao.getCompetencia()).thenReturn(competencia);
        when(competencia.getCodigo()).thenReturn(10L);
        when(competencia.getDescricao()).thenReturn("Competencia 1");
        when(ocupacao.getSituacao()).thenReturn(SituacaoCapacitacao.AC);

        OcupacaoCriticaDto dto = service.toDto(ocupacao);

        assertThat(dto.codigo()).isEqualTo(1L);
        assertThat(dto.competenciaCodigo()).isEqualTo(10L);
        assertThat(dto.competenciaDescricao()).isEqualTo("Competencia 1");
        assertThat(dto.situacao()).isEqualTo("AC");
        assertThat(dto.situacaoLabel()).isEqualTo("A capacitar");
    }
}
