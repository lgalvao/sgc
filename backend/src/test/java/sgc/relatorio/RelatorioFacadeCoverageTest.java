package sgc.relatorio;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.openpdf.text.Document;
import sgc.mapa.service.*;
import sgc.organizacao.dto.*;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;
import sgc.processo.model.*;
import sgc.processo.service.*;
import sgc.subprocesso.model.*;
import sgc.subprocesso.service.*;

import java.io.*;
import java.util.*;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RelatorioFacade - Cobertura de Testes")
class RelatorioFacadeCoverageTest {

    @InjectMocks
    private RelatorioFacade target;

    @Mock
    private ProcessoService processoService;
    @Mock
    private SubprocessoConsultaService consultaService;
    @Mock
    private ResponsavelUnidadeService responsavelService;
    @Mock
    private MapaManutencaoService mapaManutencaoService;
    @Mock
    private PdfFactory pdfFactory;

    @Test
    @DisplayName("obterRelatorioAndamento - deve cobrir substituto e titular diferente")
    void obterRelatorioAndamento_ComSubstituto() {
        Long cod = 1L;
        Unidade u = new Unidade(); u.setCodigo(10L); u.setSigla("U1"); u.setNome("N1");
        Subprocesso sp = new Subprocesso(); sp.setCodigo(100L); sp.setUnidade(u); sp.setSituacao(SituacaoSubprocesso.NAO_INICIADO);
        sp.setProcesso(new Processo());
        
        when(consultaService.listarEntidadesPorProcesso(cod)).thenReturn(java.util.List.of(sp));
        
        UnidadeResponsavelDto resp = UnidadeResponsavelDto.builder()
                .unidadeCodigo(10L)
                .titularNome("Titular")
                .substitutoNome("Substituto")
                .build();
        when(responsavelService.buscarResponsaveisUnidades(anyList())).thenReturn(Map.of(10L, resp));

        java.util.List<RelatorioAndamentoDto> result = target.obterRelatorioAndamento(cod);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().responsavel()).contains("Substituto");
        assertThat(result.getFirst().titular()).isEqualTo("Titular");
    }

    @Test
    @DisplayName("gerarRelatorioAndamento - deve cobrir impressão de responsável diferente do titular")
    void gerarRelatorioAndamento_ComResponsavelDiferente() {
        Long cod = 1L;
        Processo p = new Processo(); p.setCodigo(cod); p.setTipo(TipoProcesso.MAPEAMENTO);
        Unidade u = new Unidade(); u.setCodigo(10L); u.setSigla("U1");
        Subprocesso sp = new Subprocesso(); sp.setCodigo(100L); sp.setUnidade(u); sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        sp.setProcesso(p);
        
        when(processoService.buscarPorCodigo(cod)).thenReturn(p);
        when(consultaService.listarEntidadesPorProcesso(cod)).thenReturn(List.of(sp));
        when(responsavelService.buscarResponsaveisUnidades(anyList())).thenReturn(Map.of(10L, 
            UnidadeResponsavelDto.builder().unidadeCodigo(10L).titularNome("TIT").substitutoNome("SUB").build()));
            
        Document doc = mock(Document.class);
        when(pdfFactory.createDocument()).thenReturn(doc);
        
        target.gerarRelatorioAndamento(cod, new ByteArrayOutputStream());
        
        // Deve ter processado adicionarCartoesAndamento e deve ter passado pela linha 281
        verify(doc, atLeastOnce()).add(any());
    }

    @Test
    @DisplayName("adicionarResumo - deve cobrir número ímpar de entradas")
    void adicionarResumo_Impar() {
        Document doc = mock(Document.class);
        LinkedHashMap<String, String> resumo = new LinkedHashMap<>();
        resumo.put("K1", "V1");
        resumo.put("K2", "V2");
        resumo.put("K3", "V3");

        invokeMethod(target, "adicionarResumo", doc, resumo);

        verify(doc).add(any(org.openpdf.text.pdf.PdfPTable.class));
    }

    @Test
    @DisplayName("formatarSituacaoPdf - deve cobrir partes vazias")
    void formatarSituacaoPdf_PartesVazias() {
        String res = invokeMethod(target, "formatarSituacaoPdf", "__SITUACAO_T_");
        assertThat(res).isEqualTo("Situacao T");
    }

    @Test
    @DisplayName("gerarRelatorioAndamento - deve cobrir erro de IO/Document")
    void gerarRelatorioAndamento_Erro() {
        when(pdfFactory.createDocument()).thenThrow(new RuntimeException("Erro proposital"));
        
        assertThatThrownBy(() -> target.gerarRelatorioAndamento(1L, new ByteArrayOutputStream()))
                .isInstanceOf(RuntimeException.class);
    }
}
