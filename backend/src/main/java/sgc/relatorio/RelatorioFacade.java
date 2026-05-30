package sgc.relatorio;

import lombok.*;
import org.jspecify.annotations.*;
import org.openpdf.text.*;
import org.openpdf.text.Font;
import org.openpdf.text.Image;
import org.openpdf.text.Rectangle;
import org.openpdf.text.pdf.*;
import org.openpdf.text.pdf.draw.*;
import org.springframework.core.io.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import sgc.comum.erros.*;
import sgc.mapa.model.*;
import sgc.mapa.service.*;
import sgc.organizacao.*;
import sgc.organizacao.dto.*;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;
import sgc.processo.model.*;
import sgc.processo.service.*;
import sgc.subprocesso.model.*;
import sgc.subprocesso.service.*;

import java.awt.*;
import java.io.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RelatorioFacade {
    private static final String NOME_SISTEMA = "Sistema de Gestão de Competências";
    private static final DateTimeFormatter FORMATADOR_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATADOR_DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final Color COR_PRIMARIA = new Color(30, 64, 124);
    private static final Color COR_SECUNDARIA = new Color(86, 108, 141);
    private static final Color COR_BORDA = new Color(194, 201, 209);
    private static final Font FONTE_TITULO = new Font(Font.HELVETICA, 18, Font.BOLD, COR_PRIMARIA);
    private static final Font FONTE_SUBTITULO = new Font(Font.HELVETICA, 11, Font.NORMAL, COR_SECUNDARIA);
    private static final Font FONTE_SECAO = new Font(Font.HELVETICA, 12, Font.BOLD, COR_PRIMARIA);
    private static final Font FONTE_TEXTO = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.DARK_GRAY);
    private static final Font FONTE_TEXTO_NEGRITO = new Font(Font.HELVETICA, 10, Font.BOLD, COR_PRIMARIA);
    private static final Font FONTE_TEXTO_SUAVE = new Font(Font.HELVETICA, 9, Font.NORMAL, COR_SECUNDARIA);
    private static final Font FONTE_TEXTO_CORPO = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.BLACK);
    private static final Font FONTE_TEXTO_CORPO_SUAVE = new Font(Font.HELVETICA, 9, Font.NORMAL, Color.BLACK);
    private static final String TITULO_GRUPO_ZONAS_ELEITORAIS = "ZONAS ELEITORAIS";
    private static final String TIPO_ZONA_ELEITORAL = "ZONA ELEITORAL";
    private static final String TERMO_SECRETARIA = "SECRETARIA";

    private final ProcessoService processoService;
    private final SubprocessoConsultaService consultaService;
    private final ResponsavelUnidadeService responsavelService;
    private final MapaManutencaoService mapaManutencaoService;
    private final UnidadeService unidadeService;
    private final UnidadeHierarquiaService unidadeHierarquiaService;
    private final LocalizacaoSubprocessoService localizacaoSubprocessoService;
    private final UsuarioFacade usuarioFacade;
    private final PdfFactory pdfFactory;

    @Transactional(readOnly = true)
    public List<RelatorioAndamentoDto> obterRelatorioAndamento(Long codProcesso) {
        List<Subprocesso> subprocessos = consultaService.listarEntidadesPorProcesso(codProcesso);
        Map<Long, UnidadeResponsavelDto> responsaveisPorUnidade = buscarResponsaveisPorUnidade(subprocessos);
        Map<Long, Unidade> localizacoes = localizacaoSubprocessoService.obterLocalizacoesAtuais(subprocessos);

        return subprocessos.stream()
                .map(sp -> criarRelatorioAndamentoDto(sp, responsaveisPorUnidade, localizacoes.get(sp.getCodigo())))
                .toList();
    }

    @Transactional(readOnly = true)
    public void gerarRelatorioAndamento(Long codProcesso, OutputStream outputStream) {
        Processo processo = processoService.buscarPorCodigo(codProcesso);
        List<Subprocesso> subprocessos = consultaService.listarEntidadesPorProcesso(codProcesso);
        Map<Long, UnidadeResponsavelDto> responsaveisPorUnidade = buscarResponsaveisPorUnidade(subprocessos);
        Map<Long, Unidade> localizacoes = localizacaoSubprocessoService.obterLocalizacoesAtuais(subprocessos);
        LocalDateTime dataGeracao = LocalDateTime.now();

        try (Document document = pdfFactory.createDocument()) {
            pdfFactory.createWriter(document, outputStream);
            document.open();
            List<RelatorioAndamentoDto> relatorios = subprocessos.stream()
                    .map(sp -> criarRelatorioAndamentoDto(sp, responsaveisPorUnidade, localizacoes.get(sp.getCodigo())))
                    .toList();

            adicionarCabecalhoRelatorio(document, new CabecalhoRelatorio(
                    "Relatório de Andamento",
                    "Processo",
                    processo.getDescricao(),
                    dataGeracao,
                    processo.getTipo().name(),
                    relatorios.size()
            ));

            adicionarCartoesAndamento(document, relatorios);
        } catch (DocumentException | IOException e) {
            throw new IllegalStateException("Erro ao gerar PDF", e);
        }
    }

    @Transactional(readOnly = true)
    public void gerarRelatorioMapas(List<Long> codigosUnidades, OutputStream outputStream) {
        List<RelatorioMapaDto> relatorios = obterRelatorioMapas(codigosUnidades);
        gerarRelatorioMapasPdf(
                relatorios,
                new CabecalhoRelatorio(
                        "Relatório de Mapas Vigentes",
                        "Escopo",
                        "Unidades selecionadas",
                        LocalDateTime.now(),
                        null,
                        relatorios.size()
                ),
                outputStream,
                this::adicionarSecaoMapaCompleta
        );
    }

    @Transactional(readOnly = true)
    public List<RelatorioMapaDto> obterRelatorioMapas(List<Long> codigosUnidades) {
        List<Subprocesso> subprocessos = buscarSubprocessosMapasVigentes(codigosUnidades);

        return subprocessos.stream()
                .map(this::criarRelatorioMapaDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public RelatorioMapaDto obterRelatorioMapaAtual(Long codSubprocesso) {
        Subprocesso subprocesso = consultaService.buscarSubprocessoComMapa(codSubprocesso);
        return criarRelatorioMapaDto(subprocesso);
    }

    @Transactional(readOnly = true)
    public void gerarRelatorioMapaAtual(Long codSubprocesso, OutputStream outputStream) {
        RelatorioMapaDto relatorio = obterRelatorioMapaAtual(codSubprocesso);
        gerarRelatorioMapasPdf(
                List.of(relatorio),
                new CabecalhoRelatorio(
                        "Relatório de Mapa Atual",
                        "Unidade",
                        "%s - %s".formatted(relatorio.siglaUnidade(), relatorio.nomeUnidade()),
                        LocalDateTime.now(),
                        null,
                        1
                ),
                outputStream,
                this::adicionarConteudoMapa
        );
    }

    @Transactional(readOnly = true)
    public RelatorioMapaDto obterRelatorioMapaVigenteUnidade(Long codUnidade) {
        validarEscopoRelatorioMapaVigenteUnidade(codUnidade);
        Unidade unidade = unidadeService.buscarPorCodigo(codUnidade);
        Mapa mapaVigente = unidadeService.buscarMapaVigente(codUnidade)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("MapaVigente", codUnidade));
        return criarRelatorioMapaDto(unidade, mapaVigente);
    }

    @Transactional(readOnly = true)
    public void gerarRelatorioMapaVigenteUnidade(Long codUnidade, OutputStream outputStream) {
        RelatorioMapaDto relatorio = obterRelatorioMapaVigenteUnidade(codUnidade);
        gerarRelatorioMapasPdf(
                List.of(relatorio),
                new CabecalhoRelatorio(
                        "Relatório de Mapa Vigente",
                        "Unidade",
                        "%s - %s".formatted(relatorio.siglaUnidade(), relatorio.nomeUnidade()),
                        LocalDateTime.now(),
                        null,
                        1
                ),
                outputStream,
                this::adicionarConteudoMapa
        );
    }

    @Transactional(readOnly = true)
    public void gerarRelatorioUnidadesSemMapasVigentes(OutputStream outputStream) {
        ResultadoUnidadesSemMapaVigente resultado = obterResultadoUnidadesSemMapasVigentes();

        try (Document document = pdfFactory.createDocument()) {
            pdfFactory.createWriter(document, outputStream);
            document.open();
            adicionarCabecalhoRelatorio(document, new CabecalhoRelatorio(
                    "Relatório de Unidades sem Mapas Vigentes",
                    "Escopo",
                    "Todas as unidades",
                    LocalDateTime.now(),
                    null,
                    resultado.codigosSemMapaVigente().size()
            ));

            if (resultado.codigosSemMapaVigente().isEmpty()) {
                document.add(criarParagrafo("Não há unidades sem mapa vigente.", FONTE_TEXTO, 0f));
                return;
            }

            for (UnidadeRelatorioSemMapa card : resultado.arvoreOrganizada()) {
                adicionarSecaoUnidadesSemMapa(document, card);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Erro ao gerar PDF", e);
        }
    }

    @Transactional(readOnly = true)
    public List<RelatorioUnidadeSemMapaVigenteDto> obterRelatorioUnidadesSemMapasVigentes() {
        return obterResultadoUnidadesSemMapasVigentes().arvoreOrganizada().stream()
                .map(this::criarRelatorioUnidadeSemMapaVigenteDto)
                .toList();
    }

    private Map<Long, UnidadeResponsavelDto> buscarResponsaveisPorUnidade(List<Subprocesso> subprocessos) {
        List<Long> codigosUnidade = subprocessos.stream()
                .map(Subprocesso::getUnidade)
                .map(Unidade::getCodigo)
                .distinct()
                .toList();

        return new HashMap<>(responsavelService.buscarResponsaveisUnidades(codigosUnidade));
    }

    private List<Subprocesso> buscarSubprocessosMapasVigentes(List<Long> codigosUnidades) {
        if (codigosUnidades.isEmpty()) {
            return List.of();
        }

        Set<Long> codigosNormalizados = validarEscopoRelatorioMapas(codigosUnidades);

        return unidadeService.buscarMapasPorUnidades(new ArrayList<>(codigosNormalizados)).stream()
                .map(UnidadeMapa::getMapaVigente)
                .filter(Objects::nonNull)
                .map(Mapa::getSubprocesso)
                .toList();
    }

    private Set<Long> validarEscopoRelatorioMapas(List<Long> codigosUnidades) {
        Set<Long> codigosNormalizados = new LinkedHashSet<>(codigosUnidades);
        ContextoUsuarioAutenticado contextoUsuario = usuarioFacade.contextoAutenticado();

        if (contextoUsuario.perfil() == Perfil.ADMIN) {
            return codigosNormalizados;
        }

        if (contextoUsuario.perfil() != Perfil.GESTOR) {
            throw new ErroAcessoNegado("Usuário não possui permissão para gerar relatório de mapas vigentes.");
        }

        Set<Long> codigosPermitidos = new HashSet<>(unidadeHierarquiaService.buscarIdsDescendentes(contextoUsuario.unidadeAtivaCodigo()));
        codigosPermitidos.add(contextoUsuario.unidadeAtivaCodigo());

        boolean possuiCodigoForaDaHierarquia = codigosNormalizados.stream()
                .anyMatch(codigo -> !codigosPermitidos.contains(codigo));
        if (possuiCodigoForaDaHierarquia) {
            throw new ErroAcessoNegado("Usuário não possui permissão para gerar relatório para uma ou mais unidades selecionadas.");
        }

        return codigosNormalizados;
    }

    private void validarEscopoRelatorioMapaVigenteUnidade(Long codUnidade) {
        ContextoUsuarioAutenticado contextoUsuario = usuarioFacade.contextoAutenticado();

        if (contextoUsuario.perfil() == Perfil.ADMIN) {
            return;
        }

        if (contextoUsuario.perfil() == Perfil.GESTOR) {
            Set<Long> codigosPermitidos = new HashSet<>(unidadeHierarquiaService.buscarIdsDescendentes(contextoUsuario.unidadeAtivaCodigo()));
            codigosPermitidos.add(contextoUsuario.unidadeAtivaCodigo());
            if (codigosPermitidos.contains(codUnidade)) {
                return;
            }
            throw new ErroAcessoNegado("Usuário não possui permissão para gerar relatório de mapa vigente desta unidade.");
        }

        if (contextoUsuario.perfil() == Perfil.CHEFE && Objects.equals(contextoUsuario.unidadeAtivaCodigo(), codUnidade)) {
            return;
        }

        throw new ErroAcessoNegado("Usuário não possui permissão para gerar relatório de mapa vigente desta unidade.");
    }

    private RelatorioMapaDto criarRelatorioMapaDto(Subprocesso subprocesso) {
        return criarRelatorioMapaDto(subprocesso.getUnidade(), subprocesso.getMapa());
    }

    private RelatorioMapaDto criarRelatorioMapaDto(Unidade unidade, Mapa mapa) {
        List<Competencia> competencias = mapaManutencaoService.competenciasCodMapa(mapa.getCodigo());
        List<RelatorioMapaCompetenciaDto> competenciasDto = competencias.stream()
                .map(this::criarRelatorioMapaCompetenciaDto)
                .toList();

        return new RelatorioMapaDto(
                unidade.getCodigo(),
                unidade.getSigla(),
                unidade.getNome(),
                competenciasDto.size(),
                competenciasDto
        );
    }

    private RelatorioMapaCompetenciaDto criarRelatorioMapaCompetenciaDto(Competencia competencia) {
        return new RelatorioMapaCompetenciaDto(
                competencia.getCodigo(),
                competencia.getDescricao(),
                competencia.getAtividades().stream()
                        .map(this::criarRelatorioMapaAtividadeDto)
                        .toList()
        );
    }

    private RelatorioMapaAtividadeDto criarRelatorioMapaAtividadeDto(Atividade atividade) {
        return new RelatorioMapaAtividadeDto(
                atividade.getCodigo(),
                atividade.getDescricao(),
                atividade.getConhecimentos().stream()
                        .map(conhecimento -> new RelatorioMapaConhecimentoDto(
                                conhecimento.getCodigo(),
                                conhecimento.getDescricao()
                        ))
                        .toList()
        );
    }

    private RelatorioAndamentoDto criarRelatorioAndamentoDto(
            Subprocesso sp,
            Map<Long, UnidadeResponsavelDto> responsaveisPorUnidade,
            @Nullable Unidade localizacaoUnidade
    ) {
        Unidade unidade = sp.getUnidade();
        UnidadeResponsavelDto respDto = responsaveisPorUnidade.get(unidade.getCodigo());

        String titular = respDto != null && respDto.titularNome() != null ? respDto.titularNome() : "Não designado";
        String responsavel = titular;
        if (respDto != null && respDto.substitutoNome() != null) {
            responsavel = respDto.substitutoNome() + " (Substituição)";
        }

        String localizacao = localizacaoUnidade != null ? localizacaoUnidade.getSigla() : "-";

        LocalDateTime ultimaMov = sp.getDataLimiteEtapa1();
        if (sp.getDataFimEtapa1() != null) {
            ultimaMov = sp.getDataFimEtapa1();
        }
        if (sp.getDataFimEtapa2() != null) {
            ultimaMov = sp.getDataFimEtapa2();
        }

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

    private void gerarRelatorioMapasPdf(
            List<RelatorioMapaDto> relatorios,
            CabecalhoRelatorio cabecalho,
            OutputStream outputStream,
            SecaoPdfConsumer secaoConsumer
    ) {
        try (Document document = pdfFactory.createDocument()) {
            pdfFactory.createWriter(document, outputStream);
            document.open();
            adicionarCabecalhoRelatorio(document, cabecalho);

            for (RelatorioMapaDto relatorio : relatorios) {
                secaoConsumer.accept(document, relatorio);
            }
        } catch (DocumentException | IOException e) {
            throw new IllegalStateException("Erro ao gerar PDF", e);
        }
    }

    @FunctionalInterface
    private interface SecaoPdfConsumer {
        void accept(Document document, RelatorioMapaDto relatorio) throws DocumentException;
    }

    private void adicionarSecaoMapaCompleta(Document document, RelatorioMapaDto relatorio) throws DocumentException {
        adicionarIdentificacaoUnidade(document, relatorio);
        adicionarConteudoMapa(document, relatorio);
    }

    private void adicionarIdentificacaoUnidade(Document document, RelatorioMapaDto relatorio) throws DocumentException {
        Paragraph sigla = new Paragraph(relatorio.siglaUnidade(), new Font(Font.HELVETICA, 15, Font.BOLD, COR_PRIMARIA));
        sigla.setSpacingAfter(2f);
        document.add(sigla);
        Paragraph nome = criarParagrafo(relatorio.nomeUnidade(), new Font(Font.HELVETICA, 10, Font.BOLD, COR_SECUNDARIA), 0f);
        nome.setSpacingAfter(4f);
        document.add(nome);
        document.add(new Chunk(new LineSeparator(0.8f, 100f, COR_BORDA, Element.ALIGN_CENTER, 0f)));
        document.add(new Paragraph(" ", new Font(Font.HELVETICA, 2)));
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
        celulaTexto.addElement(criarParagrafo("%s: %s".formatted(cabecalho.rotuloSubtitulo(), cabecalho.subtitulo()), FONTE_TEXTO, 0f));

        if (cabecalho.tipoProcesso() != null) {
            String subtituloDetalhe = "TIPO: %s | UNIDADES: %d".formatted(
                    cabecalho.tipoProcesso(),
                    cabecalho.quantidadeUnidades()
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
            cardCell.setPaddingLeft(10f);
            cardCell.setPaddingTop(6f);
            cardCell.setPaddingBottom(10f);

            Paragraph titulo = new Paragraph(relatorio.siglaUnidade() + " - " + relatorio.nomeUnidade(), FONTE_SECAO);
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

            PdfPTable infoGeral = new PdfPTable(new float[]{1.2f, 1.2f, 1.6f});
            infoGeral.setWidthPercentage(100f);
            infoGeral.addCell(criarCelulaRotuloValor("Situação:", formatarSituacaoPdf(relatorio.situacaoAtual())));
            infoGeral.addCell(criarCelulaRotuloValor("Localização:", relatorio.localizacao()));
            infoGeral.addCell(criarCelulaRotuloValor("Última movimentação:", formatarDataHora(relatorio.dataUltimaMovimentacao())));
            cardCell.addElement(infoGeral);

            PdfPTable etapas = new PdfPTable(new float[]{1f, 1f});
            etapas.setWidthPercentage(100f);
            etapas.setSpacingBefore(8f);
            etapas.setSpacingAfter(8f);

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
            if (relatorio.dataLimiteEtapa2() != null && !Objects.equals(relatorio.dataLimiteEtapa2(), relatorio.dataLimiteEtapa1())) {
                dtLim2 += " (Prazo ajustado)";
            }
            dtEtapa2.addCell(criarCelulaRotuloValor("Data limite:", dtLim2));
            dtEtapa2.addCell(criarCelulaRotuloValor("Conclusão:", formatarData(relatorio.dataFimEtapa2())));
            celEtapa2.addElement(dtEtapa2);
            etapas.addCell(celEtapa2);

            cardCell.addElement(etapas);

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

    private void adicionarConteudoMapa(Document document, RelatorioMapaDto relatorio) throws DocumentException {
        for (RelatorioMapaCompetenciaDto competencia : relatorio.competencias()) {
            Paragraph tituloCompetencia = criarParagrafo(competencia.descricao(), FONTE_TEXTO_NEGRITO, 0f);
            tituloCompetencia.setSpacingAfter(4f);
            document.add(tituloCompetencia);
            for (RelatorioMapaAtividadeDto atividade : competencia.atividades()) {
                Paragraph tituloAtividade = criarParagrafo(atividade.descricao(), FONTE_TEXTO_CORPO, 10f);
                tituloAtividade.setSpacingAfter(2f);
                document.add(tituloAtividade);
                for (RelatorioMapaConhecimentoDto conhecimento : atividade.conhecimentos()) {
                    Paragraph itemConhecimento = criarParagrafo("• %s".formatted(conhecimento.descricao()), FONTE_TEXTO_CORPO_SUAVE, 20f);
                    itemConhecimento.setSpacingAfter(2f);
                    document.add(itemConhecimento);
                }
                document.add(new Paragraph(" ", new Font(Font.HELVETICA, 2)));
            }
            document.add(new Paragraph(" ", new Font(Font.HELVETICA, 3)));
        }
        document.add(new Paragraph(" ", new Font(Font.HELVETICA, 4)));
    }

    private Paragraph criarParagrafo(String texto, Font fonte, float indentacao) {
        Paragraph paragrafo = new Paragraph(texto, fonte);
        paragrafo.setIndentationLeft(indentacao);
        paragrafo.setSpacingAfter(4f);
        return paragrafo;
    }

    private void adicionarSecaoUnidadesSemMapa(Document document, UnidadeRelatorioSemMapa card) throws DocumentException {
        adicionarIdentificacaoUnidadeSemMapa(document, card);
        adicionarListaUnidadesSemMapa(document, card.filhas(), 0);
    }

    private void adicionarIdentificacaoUnidadeSemMapa(Document document, UnidadeRelatorioSemMapa card) throws DocumentException {
        String sigla = card.sigla();
        String nome = card.nome();
        String tituloTexto = !textoEmBranco(sigla) ? sigla : (textoEmBranco(nome) ? "-" : nome);
        String subtituloTexto = !textoEmBranco(nome) && !Objects.equals(sigla, nome) ? nome : null;

        Paragraph titulo = new Paragraph(tituloTexto, new Font(Font.HELVETICA, 14, Font.BOLD, COR_PRIMARIA));
        if (subtituloTexto != null) {
            titulo.add(new Chunk(" - ", FONTE_TEXTO_SUAVE));
            titulo.add(new Chunk(subtituloTexto, new Font(Font.HELVETICA, 9, Font.BOLD, COR_SECUNDARIA)));
        }
        document.add(titulo);

        document.add(new Paragraph(" ", new Font(Font.HELVETICA, 1)));

    }

    private void adicionarListaUnidadesSemMapa(Document document, List<UnidadeRelatorioSemMapa> unidades, int nivel)
            throws DocumentException {
        for (UnidadeRelatorioSemMapa unidade : unidades) {
            Paragraph item = new Paragraph();
            item.setIndentationLeft(10f + (nivel * 12f));
            item.setSpacingAfter(2f);

            String sigla = unidade.sigla();
            String nome = unidade.nome();
            boolean temSigla = !textoEmBranco(sigla);
            boolean temNome = !textoEmBranco(nome);

            if (temSigla) {
                item.add(new Chunk(sigla, FONTE_TEXTO_NEGRITO));
                if (temNome) {
                    item.add(new Chunk(" - ", FONTE_TEXTO_SUAVE));
                }
            }
            if (temNome) {
                item.add(new Chunk(nome, FONTE_TEXTO_CORPO));
            }
            if (!temSigla && !temNome) {
                item.add(new Chunk("-", FONTE_TEXTO_CORPO));
            }

            document.add(item);

            if (!unidade.filhas().isEmpty()) {
                adicionarListaUnidadesSemMapa(document, unidade.filhas(), nivel + 1);
            }
        }
    }

    private boolean textoEmBranco(@Nullable String texto) {
        return texto == null || texto.trim().isEmpty();
    }

    private List<UnidadeRelatorioSemMapa> filtrarUnidadesExibidas(List<UnidadeDto> arvore) {
        List<UnidadeRelatorioSemMapa> unidadesExibidas = new ArrayList<>();

        for (UnidadeDto unidade : arvore) {
            List<UnidadeDto> filhas = unidade.getSubunidades();
            if (filhas != null && !filhas.isEmpty()) {
                unidadesExibidas.addAll(mapearUnidades(filhas));
            }
        }

        return unidadesExibidas;
    }

    private List<UnidadeRelatorioSemMapa> mapearUnidades(List<UnidadeDto> unidades) {
        return unidades.stream()
                .map(unidade -> new UnidadeRelatorioSemMapa(
                        unidade.getCodigo(),
                        unidade.getSigla(),
                        unidade.getNome(),
                        unidade.getTipo(),
                        mapearUnidades(Optional.ofNullable(unidade.getSubunidades()).orElseGet(List::of))
                ))
                .toList();
    }

    private List<UnidadeRelatorioSemMapa> filtrarArvoreSemMapaVigente(
            List<UnidadeRelatorioSemMapa> unidades,
            Set<Long> codigosSemMapaVigente
    ) {
        List<UnidadeRelatorioSemMapa> resultado = new ArrayList<>();

        for (UnidadeRelatorioSemMapa unidade : unidades) {
            List<UnidadeRelatorioSemMapa> filhasFiltradas = filtrarArvoreSemMapaVigente(
                    unidade.filhas(),
                    codigosSemMapaVigente
            );
            boolean unidadeSemMapa = codigosSemMapaVigente.contains(unidade.codigo());

            if (!unidadeSemMapa && filhasFiltradas.isEmpty()) {
                continue;
            }

            resultado.add(unidade.comFilhas(filhasFiltradas));
        }

        return resultado;
    }

    private List<UnidadeRelatorioSemMapa> organizarArvoreUnidades(List<UnidadeRelatorioSemMapa> unidades, String identificadorGrupo) {
        List<UnidadeRelatorioSemMapa> secretarias = new ArrayList<>();
        List<UnidadeRelatorioSemMapa> zonasEleitorais = new ArrayList<>();
        List<UnidadeRelatorioSemMapa> demais = new ArrayList<>();

        for (UnidadeRelatorioSemMapa unidade : unidades) {
            List<UnidadeRelatorioSemMapa> filhas = organizarArvoreUnidades(
                    unidade.filhas(),
                    String.valueOf(unidade.codigo())
            );
            UnidadeRelatorioSemMapa unidadeNormalizada = unidade.comFilhas(filhas);
            String nome = Optional.ofNullable(unidadeNormalizada.nome()).orElse("");

            if (ehZonaEleitoralPorMetadados(unidadeNormalizada, nome)) {
                zonasEleitorais.add(unidadeNormalizada);
                continue;
            }

            if (ehSecretariaPorMetadados(nome)) {
                secretarias.add(unidadeNormalizada);
                continue;
            }

            demais.add(unidadeNormalizada);
        }

        ordenarAlfabeticamente(secretarias);
        ordenarAlfabeticamente(zonasEleitorais);
        ordenarAlfabeticamente(demais);

        List<UnidadeRelatorioSemMapa> resultado = new ArrayList<>(secretarias);
        if (!zonasEleitorais.isEmpty()) {
            resultado.add(criarGrupoZonasEleitorais(identificadorGrupo, zonasEleitorais));
        }
        resultado.addAll(demais);
        return resultado;
    }

    private ResultadoUnidadesSemMapaVigente obterResultadoUnidadesSemMapasVigentes() {
        Set<Long> codigosSemMapaVigente = new HashSet<>(unidadeService.buscarCodigosUnidadesSemMapaVigente());
        List<UnidadeDto> arvoreCompleta = unidadeHierarquiaService.buscarArvoreHierarquica();
        List<UnidadeRelatorioSemMapa> unidadesExibidas = filtrarUnidadesExibidas(arvoreCompleta);
        List<UnidadeRelatorioSemMapa> arvoreFiltrada = filtrarArvoreSemMapaVigente(
                unidadesExibidas,
                codigosSemMapaVigente
        );
        List<UnidadeRelatorioSemMapa> arvoreOrganizada = organizarArvoreUnidades(arvoreFiltrada, "raiz");
        return new ResultadoUnidadesSemMapaVigente(codigosSemMapaVigente, arvoreOrganizada);
    }

    private RelatorioUnidadeSemMapaVigenteDto criarRelatorioUnidadeSemMapaVigenteDto(UnidadeRelatorioSemMapa unidade) {
        return new RelatorioUnidadeSemMapaVigenteDto(
                unidade.codigo(),
                unidade.sigla(),
                unidade.nome(),
                unidade.tipo(),
                unidade.filhas().stream().map(this::criarRelatorioUnidadeSemMapaVigenteDto).toList()
        );
    }

    private void ordenarAlfabeticamente(List<UnidadeRelatorioSemMapa> unidades) {
        unidades.sort((a, b) -> compararTextoPtBr(obterTextoOrdenacao(a), obterTextoOrdenacao(b)));
    }

    private String obterTextoOrdenacao(UnidadeRelatorioSemMapa unidade) {
        if (unidade.sigla() != null && !unidade.sigla().isBlank()) {
            return unidade.sigla();
        }
        return Optional.ofNullable(unidade.nome()).orElse("");
    }

    private boolean ehZonaEleitoralPorMetadados(UnidadeRelatorioSemMapa unidade, String nome) {
        return ehTextoZonaEleitoral(unidade.tipo())
                || ehSiglaZonaEleitoral(unidade.sigla())
                || ehTextoZonaEleitoral(nome);
    }

    private boolean ehSecretariaPorMetadados(String nome) {
        return ehTextoSecretaria(nome);
    }

    private boolean ehTextoZonaEleitoral(@Nullable String valor) {
        return valor != null && valor.trim().toUpperCase(Locale.ROOT).contains(TIPO_ZONA_ELEITORAL);
    }

    private boolean ehSiglaZonaEleitoral(@Nullable String valor) {
        return valor != null && valor.trim().matches("(?i)Z\\.?\\s*E\\.?");
    }

    private boolean ehTextoSecretaria(@Nullable String valor) {
        return valor != null && valor.trim().toUpperCase(Locale.ROOT).contains(TERMO_SECRETARIA);
    }

    private UnidadeRelatorioSemMapa criarGrupoZonasEleitorais(
            String identificadorGrupo,
            List<UnidadeRelatorioSemMapa> zonasEleitorais
    ) {
        return new UnidadeRelatorioSemMapa(
                (identificadorGrupo.hashCode() & 0x7fffffff) + 1L,
                TITULO_GRUPO_ZONAS_ELEITORAIS,
                TITULO_GRUPO_ZONAS_ELEITORAIS,
                "AGRUPADOR_VISUAL",
                zonasEleitorais
        );
    }

    private int compararTextoPtBr(String a, String b) {
        return compararSegmentosTexto(a, b);
    }

    private int compararSegmentosTexto(String a, String b) {
        List<String> partesA = separarSegmentos(a);
        List<String> partesB = separarSegmentos(b);
        int limite = Math.min(partesA.size(), partesB.size());

        for (int i = 0; i < limite; i++) {
            String segmentoA = partesA.get(i);
            String segmentoB = partesB.get(i);
            boolean aNumero = segmentoA.chars().allMatch(Character::isDigit);
            boolean bNumero = segmentoB.chars().allMatch(Character::isDigit);

            if (aNumero && bNumero) {
                int comparacao = compararNumeros(segmentoA, segmentoB);
                if (comparacao != 0) {
                    return comparacao;
                }
                continue;
            }

            int comparacaoTexto = compararTextoSegmento(segmentoA, segmentoB);
            if (comparacaoTexto != 0) {
                return comparacaoTexto;
            }
        }

        return Integer.compare(partesA.size(), partesB.size());
    }

    private List<String> separarSegmentos(String texto) {
        if (texto == null || texto.isBlank()) {
            return List.of("");
        }

        List<String> segmentos = new ArrayList<>();
        StringBuilder atual = new StringBuilder();
        boolean numeroAtual = Character.isDigit(texto.charAt(0));

        for (char caractere : texto.toCharArray()) {
            boolean numero = Character.isDigit(caractere);
            if (numero == numeroAtual) {
                atual.append(caractere);
                continue;
            }
            segmentos.add(atual.toString());
            atual = new StringBuilder().append(caractere);
            numeroAtual = numero;
        }
        segmentos.add(atual.toString());
        return segmentos;
    }

    private int compararNumeros(String a, String b) {
        try {
            int numeroA = Integer.parseInt(a);
            int numeroB = Integer.parseInt(b);
            return Integer.compare(numeroA, numeroB);
        } catch (NumberFormatException ex) {
            return a.compareTo(b);
        }
    }

    private int compararTextoSegmento(String a, String b) {
        java.text.Collator collator = java.text.Collator.getInstance(Locale.forLanguageTag("pt-BR"));
        collator.setStrength(java.text.Collator.PRIMARY);
        return collator.compare(a, b);
    }

    private Image carregarBrasao() throws IOException, BadElementException {
        ClassPathResource recurso = new ClassPathResource("relatorio/brasao.png");
        Image imagem = Image.getInstance(recurso.getInputStream().readAllBytes());
        imagem.scaleToFit(54f, 54f);
        imagem.setAlignment(Element.ALIGN_LEFT);
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
            String rotuloSubtitulo,
            String subtitulo,
            LocalDateTime dataGeracao,
            @Nullable String tipoProcesso,
            int quantidadeUnidades
    ) {
    }

    private record UnidadeRelatorioSemMapa(
            Long codigo,
            String sigla,
            String nome,
            String tipo,
            List<UnidadeRelatorioSemMapa> filhas
    ) {
        UnidadeRelatorioSemMapa comFilhas(List<UnidadeRelatorioSemMapa> filhas) {
            return new UnidadeRelatorioSemMapa(codigo, sigla, nome, tipo, filhas);
        }
    }

    private record ResultadoUnidadesSemMapaVigente(
            Set<Long> codigosSemMapaVigente,
            List<UnidadeRelatorioSemMapa> arvoreOrganizada
    ) {
    }
}
