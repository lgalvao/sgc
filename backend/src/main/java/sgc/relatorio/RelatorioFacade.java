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
                    dataGeracao,
                    processo.getTipo().name(),
                    relatorios.size(),
                    processo.getDataLimite()
            ));
            
            adicionarCartoesAndamento(document, relatorios);
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
                    dataGeracao,
                    null,
                    0,
                    null
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
        
        String titular = respDto != null && respDto.titularNome() != null ? respDto.titularNome() : "Não designado";
        String responsavel = titular;
        if (respDto != null && respDto.substitutoNome() != null) {
            responsavel = respDto.substitutoNome() + " (Substituição)";
        }

        String localizacao = unidade.getUnidadeSuperior() != null ? unidade.getUnidadeSuperior().getSigla() : "-";
        
        LocalDateTime ultimaMov = sp.getDataFimEtapa2() != null ? sp.getDataFimEtapa2() : (sp.getDataFimEtapa1() != null ? sp.getDataFimEtapa1() : sp.getDataLimiteEtapa1());

        return RelatorioAndamentoDto.builder()
                .siglaUnidade(unidade.getSigla())
                .nomeUnidade(unidade.getNome())
                .situacaoAtual(sp.getSituacao().getDescricao())
                .localizacao(localizacao)
                .dataLimiteEtapa1(sp.getDataLimiteEtapa1())
                .dataLimiteEtapa2(sp.getDataLimiteEtapa2())
                .dataFimEtapa1(sp.getDataFimEtapa1())
                .dataFimEtapa2(sp.getDataFimEtapa2())
                .dataUltimaMovimentacao(ultimaMov)
                .responsavel(responsavel)
                .titular(titular)
                .build();
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
        
        if (cabecalho.tipoProcesso() != null) {
            String subtituloDetalhe = "TIPO: %s | UNIDADES: %d | DATA LIMITE GERAL: %s".formatted(
                cabecalho.tipoProcesso(),
                cabecalho.quantidadeUnidades(),
                formatarData(cabecalho.dataLimiteGeral())
            );
            celulaTexto.addElement(criarParagrafo(subtituloDetalhe, FONTE_TEXTO_SUAVE, 0f));
        }
        
        celulaTexto.addElement(criarParagrafo("Gerado em: %s".formatted(formatarDataHora(cabecalho.dataGeracao())), FONTE_TEXTO_SUAVE, 0f));
        tabelaCabecalho.addCell(celulaTexto);

        document.add(tabelaCabecalho);
        document.add(new Chunk(new LineSeparator(1f, 100f, COR_BORDA, Element.ALIGN_CENTER, 0f)));
        document.add(new Paragraph(" "));
    }

    private void adicionarCartoesAndamento(Document document, List<RelatorioAndamentoDto> relatorios) throws DocumentException {
        for (RelatorioAndamentoDto relatorio : relatorios) {
            PdfPTable cartao = new PdfPTable(1);
            cartao.setWidthPercentage(100f);
            cartao.setSpacingAfter(20f);

            PdfPCell cardCell = new PdfPCell();
            cardCell.setBorder(Rectangle.NO_BORDER);
            cardCell.setBorderWidthLeft(4f);
            cardCell.setBorderColorLeft(COR_SECUNDARIA);
            cardCell.setPaddingLeft(10f);
            cardCell.setPaddingTop(5f);
            cardCell.setPaddingBottom(10f);

            Paragraph titulo = new Paragraph("▌ " + relatorio.siglaUnidade() + " - " + relatorio.nomeUnidade(), FONTE_SECAO);
            titulo.setSpacingAfter(4f);
            cardCell.addElement(titulo);
            
            PdfPTable linhaDiv = new PdfPTable(1);
            linhaDiv.setWidthPercentage(100f);
            PdfPCell celDiv = new PdfPCell();
            celDiv.setBorder(Rectangle.BOTTOM);
            celDiv.setBorderColor(COR_BORDA);
            linhaDiv.addCell(celDiv);
            cardCell.addElement(linhaDiv);
            cardCell.addElement(new Paragraph(" ", new Font(Font.HELVETICA, 4)));

            PdfPTable infoGeral = new PdfPTable(new float[]{2f, 1f});
            infoGeral.setWidthPercentage(100f);
            infoGeral.addCell(criarCelulaRotuloValor("Situação:", formatarSituacaoPdf(relatorio.situacaoAtual())));
            infoGeral.addCell(criarCelulaRotuloValor("Localização:", relatorio.localizacao()));
            cardCell.addElement(infoGeral);
            
            Paragraph ultMov = criarParagrafoRotuloValor("Última movimentação:", formatarDataHora(relatorio.dataUltimaMovimentacao()));
            ultMov.setSpacingAfter(10f);
            cardCell.addElement(ultMov);

            PdfPTable etapas = new PdfPTable(new float[]{1f, 1f});
            etapas.setWidthPercentage(100f);
            
            PdfPCell celEtapa1 = new PdfPCell();
            celEtapa1.setBorder(Rectangle.NO_BORDER);
            Paragraph tituloEtapa1 = new Paragraph("ETAPA 1: CADASTRO", FONTE_TEXTO_NEGRITO);
            tituloEtapa1.setSpacingAfter(4f);
            celEtapa1.addElement(tituloEtapa1);
            PdfPTable dtEtapa1 = new PdfPTable(new float[]{1f, 1f});
            dtEtapa1.setWidthPercentage(100f);
            dtEtapa1.addCell(criarCelulaRotuloValor("Data limite:", formatarData(relatorio.dataLimiteEtapa1())));
            dtEtapa1.addCell(criarCelulaRotuloValor("Conclusão:", formatarData(relatorio.dataFimEtapa1())));
            celEtapa1.addElement(dtEtapa1);
            etapas.addCell(celEtapa1);

            PdfPCell celEtapa2 = new PdfPCell();
            celEtapa2.setBorder(Rectangle.NO_BORDER);
            Paragraph tituloEtapa2 = new Paragraph("ETAPA 2: MAPA", FONTE_TEXTO_NEGRITO);
            tituloEtapa2.setSpacingAfter(4f);
            celEtapa2.addElement(tituloEtapa2);
            PdfPTable dtEtapa2 = new PdfPTable(new float[]{1f, 1f});
            dtEtapa2.setWidthPercentage(100f);
            String dtLim2 = formatarData(relatorio.dataLimiteEtapa2());
            if (relatorio.dataLimiteEtapa2() != null && relatorio.dataLimiteEtapa1() != null && !relatorio.dataLimiteEtapa2().equals(relatorio.dataLimiteEtapa1())) {
                dtLim2 += " (Prazo ajustado)";
            }
            dtEtapa2.addCell(criarCelulaRotuloValor("Data limite:", dtLim2));
            dtEtapa2.addCell(criarCelulaRotuloValor("Conclusão:", formatarData(relatorio.dataFimEtapa2())));
            celEtapa2.addElement(dtEtapa2);
            etapas.addCell(celEtapa2);

            cardCell.addElement(etapas);
            cardCell.addElement(new Paragraph(" ", new Font(Font.HELVETICA, 6)));

            cardCell.addElement(criarParagrafoRotuloValor("Titular:", relatorio.titular()));
            if (!relatorio.titular().equals(relatorio.responsavel())) {
                cardCell.addElement(criarParagrafoRotuloValor("Responsável atual:", relatorio.responsavel()));
            }

            cartao.addCell(cardCell);
            document.add(cartao);
        }
    }
    
    private PdfPCell criarCelulaRotuloValor(String rotulo, String valor) {
        PdfPCell celula = new PdfPCell();
        celula.setBorder(Rectangle.NO_BORDER);
        celula.setPadding(2f);
        Paragraph p = new Paragraph();
        p.add(new Chunk(rotulo + " ", FONTE_TEXTO_SUAVE));
        p.add(new Chunk(valor, FONTE_TEXTO_NEGRITO));
        celula.addElement(p);
        return celula;
    }
    
    private Paragraph criarParagrafoRotuloValor(String rotulo, String valor) {
        Paragraph p = new Paragraph();
        p.setSpacingAfter(4f);
        p.add(new Chunk(rotulo + " ", FONTE_TEXTO_SUAVE));
        p.add(new Chunk(valor, FONTE_TEXTO_NEGRITO));
        return p;
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
            LocalDateTime dataGeracao,
            @Nullable String tipoProcesso,
            int quantidadeUnidades,
            @Nullable LocalDateTime dataLimiteGeral
    ) {
    }
}