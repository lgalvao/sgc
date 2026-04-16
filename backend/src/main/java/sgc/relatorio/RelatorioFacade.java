package sgc.relatorio;

import lombok.*;
import org.openpdf.text.*;
import org.openpdf.text.pdf.*;
import org.openpdf.text.pdf.draw.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import org.jspecify.annotations.*;
import org.springframework.core.io.*;
import sgc.mapa.model.*;
import sgc.mapa.service.*;
import sgc.organizacao.dto.*;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;
import sgc.processo.model.*;
import sgc.processo.service.*;
import sgc.subprocesso.model.*;
import sgc.subprocesso.service.*;

import java.io.*;
import java.awt.Color;
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RelatorioFacade {
    private static final String NOME_SISTEMA = "Sistema de Gestão de Conhecimentos";
    private static final DateTimeFormatter FORMATADOR_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATADOR_DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final Color COR_PRIMARIA = new Color(30, 64, 124);
    private static final Color COR_SECUNDARIA = new Color(86, 108, 141);
    private static final Color COR_TABELA_CABECALHO = new Color(218, 226, 239);
    private static final Color COR_TABELA_ALTERNADA = new Color(245, 247, 250);
    private static final Color COR_BORDA = new Color(194, 201, 209);
    private static final Font FONTE_TITULO = new Font(Font.HELVETICA, 18, Font.BOLD, COR_PRIMARIA);
    private static final Font FONTE_SUBTITULO = new Font(Font.HELVETICA, 11, Font.NORMAL, COR_SECUNDARIA);
    private static final Font FONTE_SECAO = new Font(Font.HELVETICA, 12, Font.BOLD, COR_PRIMARIA);
    private static final Font FONTE_TEXTO = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.DARK_GRAY);
    private static final Font FONTE_TEXTO_NEGRITO = new Font(Font.HELVETICA, 10, Font.BOLD, COR_PRIMARIA);
    private static final Font FONTE_TEXTO_SUAVE = new Font(Font.HELVETICA, 9, Font.NORMAL, COR_SECUNDARIA);

    private final ProcessoService processoService;
    private final SubprocessoConsultaService consultaService;
    private final ResponsavelUnidadeService responsavelService;
    private final MapaManutencaoService mapaManutencaoService;
    private final PdfFactory pdfFactory;

    @Transactional(readOnly = true)
    public List<RelatorioAndamentoDto> obterRelatorioAndamento(Long codProcesso) {
        List<Subprocesso> subprocessos = consultaService.listarEntidadesPorProcesso(codProcesso);
        Map<Long, UnidadeResponsavelDto> responsaveisPorUnidade = buscarResponsaveisPorUnidade(subprocessos);

        return subprocessos.stream()
                .map(sp -> criarRelatorioAndamentoDto(sp, responsaveisPorUnidade))
                .toList();
    }

    @Transactional(readOnly = true)
    public void gerarRelatorioAndamento(Long codProcesso, OutputStream outputStream) {
        Processo processo = processoService.buscarPorCodigo(codProcesso);
        List<Subprocesso> subprocessos = consultaService.listarEntidadesPorProcesso(codProcesso);
        Map<Long, UnidadeResponsavelDto> responsaveisPorUnidade = buscarResponsaveisPorUnidade(subprocessos);
        LocalDateTime dataGeracao = LocalDateTime.now();

        try (Document document = pdfFactory.createDocument()) {
            pdfFactory.createWriter(document, outputStream);
            document.open();
            List<RelatorioAndamentoDto> relatorios = subprocessos.stream()
                    .map(sp -> criarRelatorioAndamentoDto(sp, responsaveisPorUnidade))
                    .toList();

            adicionarCabecalhoRelatorio(document, new CabecalhoRelatorio(
                    "Relatório de Andamento",
                    processo.getDescricao(),
                    dataGeracao
            ));
            adicionarResumo(document, criarResumoAndamento(processo, relatorios, dataGeracao));
            document.add(criarTituloSecao("Andamento por unidade"));
            document.add(criarTabelaAndamento(relatorios));
        } catch (DocumentException | IOException e) {
            throw new RuntimeException("Erro ao gerar PDF", e);
        }
    }

    @Transactional(readOnly = true)
    public void gerarRelatorioMapas(Long codProcesso, Long codUnidade, OutputStream outputStream) {
        Processo processo = processoService.buscarPorCodigo(codProcesso);
        List<Subprocesso> subprocessos = consultaService.listarEntidadesPorProcesso(codProcesso);
        LocalDateTime dataGeracao = LocalDateTime.now();

        try (Document document = pdfFactory.createDocument()) {
            pdfFactory.createWriter(document, outputStream);
            document.open();
            adicionarCabecalhoRelatorio(document, new CabecalhoRelatorio(
                    "Relatório de Mapas Vigentes",
                    processo.getDescricao(),
                    dataGeracao
            ));
            adicionarResumo(document, criarResumoMapas(processo, subprocessos, codUnidade, dataGeracao));
            document.add(criarTituloSecao("Mapas consolidados"));

            for (Subprocesso sp : subprocessos) {
                List<Competencia> competencias = mapaManutencaoService.competenciasCodMapa(sp.getMapa().getCodigo());
                adicionarSecaoMapa(document, sp.getUnidade(), competencias);
            }
        } catch (DocumentException | IOException e) {
            throw new RuntimeException("Erro ao gerar PDF", e);
        }
    }

    private Map<Long, UnidadeResponsavelDto> buscarResponsaveisPorUnidade(List<Subprocesso> subprocessos) {
        List<Long> codigosUnidade = subprocessos.stream()
                .map(Subprocesso::getUnidade)
                .map(Unidade::getCodigo)
                .distinct()
                .toList();

        return new HashMap<>(responsavelService.buscarResponsaveisUnidades(codigosUnidade));
    }

    private RelatorioAndamentoDto criarRelatorioAndamentoDto(
            Subprocesso sp,
            Map<Long, UnidadeResponsavelDto> responsaveisPorUnidade
    ) {
        Unidade unidade = sp.getUnidade();
        UnidadeResponsavelDto respDto = responsaveisPorUnidade.get(unidade.getCodigo());
        String responsavel = respDto != null ? respDto.titularNome() : "Não designado";

        return RelatorioAndamentoDto.builder()
                .siglaUnidade(unidade.getSigla())
                .nomeUnidade(unidade.getNome())
                .situacaoAtual(sp.getSituacao().getDescricao())
                .dataLimite(obterDataLimite(sp))
                .dataFimEtapa1(sp.getDataFimEtapa1())
                .dataFimEtapa2(sp.getDataFimEtapa2())
                .dataUltimaMovimentacao(sp.getDataLimiteEtapa1())
                .responsavel(responsavel)
                .titular(responsavel)
                .build();
    }

    private LocalDateTime obterDataLimite(Subprocesso sp) {
        LocalDateTime dataLimiteEtapa1 = sp.getDataLimiteEtapa1();
        LocalDateTime dataLimiteEtapa2 = sp.getDataLimiteEtapa2();

        return Objects.requireNonNullElse(dataLimiteEtapa2, dataLimiteEtapa1);
    }

    private String formatarData(@Nullable LocalDateTime dataHora) {
        return dataHora == null ? "-" : dataHora.format(FORMATADOR_DATA);
    }

    private String formatarDataHora(@Nullable LocalDateTime dataHora) {
        return dataHora == null ? "-" : dataHora.format(FORMATADOR_DATA_HORA);
    }

    private void adicionarCabecalhoRelatorio(Document document, CabecalhoRelatorio cabecalho) throws DocumentException, IOException {
        PdfPTable tabelaCabecalho = new PdfPTable(new float[]{1.1f, 5f});
        tabelaCabecalho.setWidthPercentage(100f);
        tabelaCabecalho.setSpacingAfter(12f);

        PdfPCell celulaImagem = new PdfPCell();
        celulaImagem.setBorder(Rectangle.NO_BORDER);
        celulaImagem.setPadding(0f);
        celulaImagem.setVerticalAlignment(Element.ALIGN_MIDDLE);
        celulaImagem.addElement(carregarBrasao());
        tabelaCabecalho.addCell(celulaImagem);

        PdfPCell celulaTexto = new PdfPCell();
        celulaTexto.setBorder(Rectangle.NO_BORDER);
        celulaTexto.setPaddingLeft(10f);
        celulaTexto.setVerticalAlignment(Element.ALIGN_MIDDLE);
        celulaTexto.addElement(criarParagrafo(NOME_SISTEMA, FONTE_SUBTITULO, 0f));
        celulaTexto.addElement(criarParagrafo(cabecalho.titulo(), FONTE_TITULO, 0f));
        celulaTexto.addElement(criarParagrafo("Processo: %s".formatted(cabecalho.subtitulo()), FONTE_TEXTO, 0f));
        celulaTexto.addElement(criarParagrafo("Gerado em: %s".formatted(formatarDataHora(cabecalho.dataGeracao())), FONTE_TEXTO_SUAVE, 0f));
        tabelaCabecalho.addCell(celulaTexto);

        document.add(tabelaCabecalho);
        document.add(new Chunk(new LineSeparator(1f, 100f, COR_BORDA, Element.ALIGN_CENTER, 0f)));
        document.add(new Paragraph(" "));
    }

    private void adicionarResumo(Document document, LinkedHashMap<String, String> resumo) throws DocumentException {
        PdfPTable tabelaResumo = new PdfPTable(new float[]{1.4f, 3.6f, 1.4f, 3.6f});
        tabelaResumo.setWidthPercentage(100f);
        tabelaResumo.setSpacingAfter(14f);

        List<Map.Entry<String, String>> entradas = new ArrayList<>(resumo.entrySet());
        for (int i = 0; i < entradas.size(); i += 2) {
            adicionarLinhaResumo(tabelaResumo, entradas.get(i));
            if (i + 1 < entradas.size()) {
                adicionarLinhaResumo(tabelaResumo, entradas.get(i + 1));
                continue;
            }
            tabelaResumo.addCell(criarCelulaResumoRotulo(""));
            tabelaResumo.addCell(criarCelulaResumoValor(""));
        }

        document.add(tabelaResumo);
    }

    private LinkedHashMap<String, String> criarResumoAndamento(
            Processo processo,
            List<RelatorioAndamentoDto> relatorios,
            LocalDateTime dataGeracao
    ) {
        LinkedHashMap<String, String> resumo = new LinkedHashMap<>();
        resumo.put("Processo", processo.getDescricao());
        resumo.put("Subprocessos", Integer.toString(relatorios.size()));
        resumo.put("Gerado em", formatarDataHora(dataGeracao));
        resumo.put("Relatório", "Andamento por unidade");
        return resumo;
    }

    private LinkedHashMap<String, String> criarResumoMapas(
            Processo processo,
            List<Subprocesso> subprocessos,
            @Nullable Long codUnidade,
            LocalDateTime dataGeracao
    ) {
        LinkedHashMap<String, String> resumo = new LinkedHashMap<>();
        resumo.put("Processo", processo.getDescricao());
        resumo.put("Unidades", Integer.toString(subprocessos.size()));
        resumo.put("Gerado em", formatarDataHora(dataGeracao));
        resumo.put("Filtro", codUnidade == null ? "Todas as unidades" : "Unidade %d".formatted(codUnidade));
        return resumo;
    }

    private void adicionarLinhaResumo(PdfPTable tabelaResumo, Map.Entry<String, String> entrada) {
        tabelaResumo.addCell(criarCelulaResumoRotulo(entrada.getKey()));
        tabelaResumo.addCell(criarCelulaResumoValor(entrada.getValue()));
    }

    private PdfPTable criarTabelaAndamento(List<RelatorioAndamentoDto> relatorios) throws DocumentException {
        PdfPTable tabela = new PdfPTable(new float[]{1.3f, 1.8f, 1.8f, 1.5f, 1.5f, 1.8f});
        tabela.setWidthPercentage(100f);
        tabela.setHeaderRows(1);
        tabela.setSpacingAfter(8f);

        tabela.addCell(criarCelulaCabecalho("Unidade"));
        tabela.addCell(criarCelulaCabecalho("Situação"));
        tabela.addCell(criarCelulaCabecalho("Responsável"));
        tabela.addCell(criarCelulaCabecalho("Data limite"));
        tabela.addCell(criarCelulaCabecalho("Fim etapa 1"));
        tabela.addCell(criarCelulaCabecalho("Fim etapa 2"));

        for (int i = 0; i < relatorios.size(); i++) {
            RelatorioAndamentoDto relatorio = relatorios.get(i);
            Color corFundo = i % 2 == 0 ? Color.WHITE : COR_TABELA_ALTERNADA;
            tabela.addCell(criarCelulaConteudo("%s - %s".formatted(relatorio.siglaUnidade(), relatorio.nomeUnidade()), corFundo));
            tabela.addCell(criarCelulaConteudo(formatarSituacaoPdf(relatorio.situacaoAtual()), corFundo));
            tabela.addCell(criarCelulaConteudo(relatorio.responsavel(), corFundo));
            tabela.addCell(criarCelulaConteudo(formatarData(relatorio.dataLimite()), corFundo));
            tabela.addCell(criarCelulaConteudo(formatarData(relatorio.dataFimEtapa1()), corFundo));
            tabela.addCell(criarCelulaConteudo(formatarData(relatorio.dataFimEtapa2()), corFundo));
        }

        return tabela;
    }

    private void adicionarSecaoMapa(Document document, Unidade unidade, List<Competencia> competencias) throws DocumentException {
        PdfPTable cartao = new PdfPTable(1);
        cartao.setWidthPercentage(100f);
        cartao.setSpacingAfter(10f);

        PdfPCell cabecalho = new PdfPCell(new Phrase("%s - %s".formatted(unidade.getSigla(), unidade.getNome()), FONTE_SECAO));
        cabecalho.setBackgroundColor(COR_TABELA_CABECALHO);
        cabecalho.setBorderColor(COR_BORDA);
        cabecalho.setPadding(8f);
        cartao.addCell(cabecalho);

        PdfPCell conteudo = new PdfPCell();
        conteudo.setBorderColor(COR_BORDA);
        conteudo.setPadding(10f);
        conteudo.addElement(criarParagrafo("Competências: %d".formatted(competencias.size()), FONTE_TEXTO_NEGRITO, 0f));
        for (Competencia competencia : competencias) {
            conteudo.addElement(criarParagrafo("Competência: %s".formatted(competencia.getDescricao()), FONTE_TEXTO_NEGRITO, 0f));
            for (Atividade atividade : competencia.getAtividades()) {
                conteudo.addElement(criarParagrafo("Atividade: %s".formatted(atividade.getDescricao()), FONTE_TEXTO, 12f));
                for (Conhecimento conhecimento : atividade.getConhecimentos()) {
                    conteudo.addElement(criarParagrafo("• %s".formatted(conhecimento.getDescricao()), FONTE_TEXTO_SUAVE, 24f));
                }
            }
        }

        cartao.addCell(conteudo);
        document.add(cartao);
    }

    private PdfPCell criarCelulaCabecalho(String texto) {
        PdfPCell celula = new PdfPCell(new Phrase(texto, FONTE_TEXTO_NEGRITO));
        celula.setBackgroundColor(COR_TABELA_CABECALHO);
        celula.setBorderColor(COR_BORDA);
        celula.setHorizontalAlignment(Element.ALIGN_CENTER);
        celula.setVerticalAlignment(Element.ALIGN_MIDDLE);
        celula.setPadding(7f);
        return celula;
    }

    private PdfPCell criarCelulaConteudo(String texto, Color corFundo) {
        PdfPCell celula = new PdfPCell(new Phrase(texto, FONTE_TEXTO));
        celula.setBackgroundColor(corFundo);
        celula.setBorderColor(COR_BORDA);
        celula.setVerticalAlignment(Element.ALIGN_MIDDLE);
        celula.setPadding(7f);
        return celula;
    }

    private PdfPCell criarCelulaResumoRotulo(String texto) {
        PdfPCell celula = new PdfPCell(new Phrase(texto, FONTE_TEXTO_NEGRITO));
        celula.setBackgroundColor(Color.WHITE);
        celula.setBorderColor(COR_BORDA);
        celula.setPadding(7f);
        return celula;
    }

    private PdfPCell criarCelulaResumoValor(String texto) {
        PdfPCell celula = new PdfPCell(new Phrase(texto, FONTE_TEXTO));
        celula.setBackgroundColor(COR_TABELA_ALTERNADA);
        celula.setBorderColor(COR_BORDA);
        celula.setPadding(7f);
        return celula;
    }

    private Paragraph criarTituloSecao(String texto) {
        Paragraph paragrafo = new Paragraph(texto, FONTE_SECAO);
        paragrafo.setSpacingAfter(8f);
        return paragrafo;
    }

    private Paragraph criarParagrafo(String texto, Font fonte, float indentacao) {
        Paragraph paragrafo = new Paragraph(texto, fonte);
        paragrafo.setIndentationLeft(indentacao);
        paragrafo.setSpacingAfter(4f);
        return paragrafo;
    }

    private Image carregarBrasao() throws IOException, BadElementException {
        ClassPathResource recurso = new ClassPathResource("relatorio/brasao.png");
        Image imagem = Image.getInstance(recurso.getInputStream().readAllBytes());
        imagem.scaleToFit(54f, 54f);
        imagem.setAlignment(Image.ALIGN_LEFT);
        return imagem;
    }

    private String formatarSituacaoPdf(String situacao) {
        String[] partes = situacao.toLowerCase(Locale.ROOT).split("_");
        StringJoiner joiner = new StringJoiner(" ");
        for (String parte : partes) {
            if (parte.isBlank()) {
                continue;
            }
            joiner.add(Character.toUpperCase(parte.charAt(0)) + parte.substring(1));
        }
        return joiner.toString();
    }

    private record CabecalhoRelatorio(
            String titulo,
            String subtitulo,
            LocalDateTime dataGeracao
    ) {
    }
}

