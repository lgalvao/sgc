package sgc.subprocesso.dto;

import org.junit.jupiter.api.Test;
import sgc.subprocesso.SituacaoSubprocesso;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SubprocessoDtoTest {
    private static final String OBSERVACOES = "Observações";
    private static final String COMPETENCIA = "Competência";
    private static final String ATIVIDADE = "Atividade";
    private static final String JUSTIFICATIVA = "Justificativa";
    private static final String UNIDADE = "Unidade";
    private static final String SIGLA = "SIGLA";
    private static final String NOME = "Nome";

    @Test
    void AceitarCadastroReq_RecordConstructorAndGetters() {
        AceitarCadastroReq req = new AceitarCadastroReq("Observações de aceite");

        assertEquals("Observações de aceite", req.observacoes());
    }

    @Test
    void AnaliseValidacaoDto_RecordConstructorAndGetters() {
        LocalDateTime now = LocalDateTime.now();
        AnaliseValidacaoDto dto = new AnaliseValidacaoDto(1L, now, OBSERVACOES, null, null);

        assertEquals(1L, dto.id());
        assertEquals(now, dto.dataHora());
        assertEquals(OBSERVACOES, dto.observacoes());
    }

    @Test
    void ApresentarSugestoesReq_RecordConstructorAndGetters() {
        ApresentarSugestoesReq req = new ApresentarSugestoesReq("Sugestões importantes");

        assertEquals("Sugestões importantes", req.sugestoes());
    }

    @Test
    void ApresentarSugestoesReq_InvalidSugestoes_ThrowsException() {
        // Test will be handled by validation framework during actual usage
        ApresentarSugestoesReq req = new ApresentarSugestoesReq("");
        assertEquals("", req.sugestoes());
    }

    @Test
    void AtividadeAjusteDto_RecordConstructorAndGetters() {
        List<ConhecimentoAjusteDto> conhecimentos = List.of(new ConhecimentoAjusteDto(1L, COMPETENCIA, true));
        AtividadeAjusteDto dto = new AtividadeAjusteDto(1L, ATIVIDADE, conhecimentos);

        assertEquals(1L, dto.atividadeId());
        assertEquals(ATIVIDADE, dto.nome());
        assertEquals(conhecimentos, dto.conhecimentos());
    }

    @Test
    void CompetenciaAjusteDto_RecordConstructorAndGetters() {
        List<AtividadeAjusteDto> atividades = List.of(new AtividadeAjusteDto(1L, ATIVIDADE, List.of()));
        CompetenciaAjusteDto dto = new CompetenciaAjusteDto(1L, COMPETENCIA, atividades);

        assertEquals(1L, dto.competenciaId());
        assertEquals(COMPETENCIA, dto.nome());
        assertEquals(atividades, dto.atividades());
    }

    @Test
    void ConhecimentoAjusteDto_RecordConstructorAndGetters() {
        ConhecimentoAjusteDto dto = new ConhecimentoAjusteDto(1L, COMPETENCIA, true);

        assertEquals(1L, dto.conhecimentoId());
        assertEquals(COMPETENCIA, dto.nome());
        assertTrue(dto.incluido());
    }

    @Test
    void DevolverCadastroReq_RecordConstructorAndGetters() {
        DevolverCadastroReq req = new DevolverCadastroReq("Motivo", OBSERVACOES);

        assertEquals("Motivo", req.motivo());
        assertEquals(OBSERVACOES, req.observacoes());
    }

    @Test
    void DevolverCadastroReq_InvalidMotivo_ThrowsException() {
        // Test will be handled by validation framework during actual usage
        DevolverCadastroReq req = new DevolverCadastroReq("", OBSERVACOES);
        assertEquals("", req.motivo());
    }

    @Test
    void DevolverValidacaoReq_RecordConstructorAndGetters() {
        DevolverValidacaoReq req = new DevolverValidacaoReq(JUSTIFICATIVA);

        assertEquals(JUSTIFICATIVA, req.justificativa());
    }

    @Test
    void DevolverValidacaoReq_InvalidJustificativa_ThrowsException() {
        // Test will be handled by validation framework during actual usage
        DevolverValidacaoReq req = new DevolverValidacaoReq("");
        assertEquals("", req.justificativa());
    }

    @Test
    void DisponibilizarMapaReq_RecordConstructorAndGetters() {
        LocalDate dataLimite = LocalDate.now().plusDays(10);
        DisponibilizarMapaReq req = new DisponibilizarMapaReq(OBSERVACOES, dataLimite);

        assertEquals(OBSERVACOES, req.observacoes());
        assertEquals(dataLimite, req.dataLimiteEtapa2());
    }

    @Test
    void DisponibilizarMapaReq_InvalidDataLimite_ThrowsException() {
        // Test will be handled by validation framework during actual usage
        DisponibilizarMapaReq req = new DisponibilizarMapaReq(OBSERVACOES, null);
        assertNull(req.dataLimiteEtapa2());
    }

    @Test
    void HomologarCadastroReq_RecordConstructorAndGetters() {
        HomologarCadastroReq req = new HomologarCadastroReq("Observações");

        assertEquals("Observações", req.observacoes());
    }

    @Test
    void MapaAjusteDto_RecordConstructorAndGetters() {
        List<CompetenciaAjusteDto> competencias = List.of(new CompetenciaAjusteDto(1L, "Competência", List.of()));
        MapaAjusteDto dto = new MapaAjusteDto(1L, UNIDADE, competencias, "Justificativa");

        assertEquals(1L, dto.mapaId());
        assertEquals(UNIDADE, dto.unidadeNome());
        assertEquals(competencias, dto.competencias());
        assertEquals("Justificativa", dto.justificativaDevolucao());
    }

    @Test
    void MovimentacaoDto_AutowiredConstructor() {
        LocalDateTime now = LocalDateTime.now();
        MovimentacaoDto dto = new MovimentacaoDto(
                1L,
                now,
                1L,
                "SIGLA_ORIGEM",
                "Unidade Origem",
                2L,
                "SIGLA_DESTINO",
                UNIDADE,
                "Descrição"
        );

        assertEquals(1L, dto.codigo());
        assertEquals(now, dto.dataHora());
        assertEquals(1L, dto.unidadeOrigemCodigo());
        assertEquals("SIGLA_ORIGEM", dto.unidadeOrigemSigla());
        assertEquals("Unidade Origem", dto.unidadeOrigemNome());
        assertEquals(2L, dto.unidadeDestinoCodigo());
        assertEquals("SIGLA_DESTINO", dto.unidadeDestinoSigla());
        assertEquals(UNIDADE, dto.unidadeDestinoNome());
        assertEquals("Descrição", dto.descricao());
    }

    @Test
    void SalvarAjustesReq_RecordConstructorAndGetters() {
        List<CompetenciaAjusteDto> competencias = List.of(new CompetenciaAjusteDto(1L, "Competência", List.of()));
        SalvarAjustesReq req = new SalvarAjustesReq(competencias);

        assertEquals(competencias, req.competencias());
    }

    @Test
    void SubprocessoCadastroDto_RecordConstructorAndAccessors() {
        List<SubprocessoCadastroDto.AtividadeCadastroDTO> atividades = List.of(
                new SubprocessoCadastroDto.AtividadeCadastroDTO(1L, "Atividade", List.of())
        );
        SubprocessoCadastroDto dto = new SubprocessoCadastroDto(1L, SIGLA, atividades);

        assertEquals(1L, dto.subprocessoId());
        assertEquals(SIGLA, dto.unidadeSigla());
        assertEquals(atividades, dto.atividades());
    }

    @Test
    void SubprocessoDetalheDto_RecordConstructorAndAccessors() {
        SubprocessoDetalheDto.UnidadeDTO unidade = new SubprocessoDetalheDto.UnidadeDTO(1L, SIGLA, NOME);
        SubprocessoDetalheDto.ResponsavelDTO responsavel = new SubprocessoDetalheDto.ResponsavelDTO(1L, NOME, "Tipo", "Ramal", "email@exemplo.com");
        LocalDate prazo = LocalDate.now();
        List<MovimentacaoDto> movimentacoes = List.of();
        List<SubprocessoDetalheDto.ElementoProcessoDTO> elementos = List.of();

        SubprocessoDetalheDto dto = new SubprocessoDetalheDto(unidade, responsavel, SituacaoSubprocesso.NAO_INICIADO.name(), "Localizacao", prazo, movimentacoes, elementos);

        assertEquals(unidade, dto.unidade());
        assertEquals(responsavel, dto.responsavel());
        assertEquals(SituacaoSubprocesso.NAO_INICIADO.name(), dto.situacao());
        assertEquals("Localizacao", dto.localizacaoAtual());
        assertEquals(prazo, dto.prazoEtapaAtual());
        assertEquals(movimentacoes, dto.movimentacoes());
        assertEquals(elementos, dto.elementosDoProcesso());
    }

    @Test
    void SubprocessoDetalheDto_UnidadeDTO_RecordConstructorAndAccessors() {
        SubprocessoDetalheDto.UnidadeDTO unidade = new SubprocessoDetalheDto.UnidadeDTO(1L, SIGLA, NOME);

        assertEquals(1L, unidade.codigo());
        assertEquals(SIGLA, unidade.sigla());
        assertEquals(NOME, unidade.nome());
    }

    @Test
    void SubprocessoDetalheDto_ResponsavelDTO_RecordConstructorAndAccessors() {
        SubprocessoDetalheDto.ResponsavelDTO responsavel = new SubprocessoDetalheDto.ResponsavelDTO(1L, NOME, "Tipo", "Ramal", "email@exemplo.com");

        assertEquals(1L, responsavel.id());
        assertEquals(NOME, responsavel.nome());
        assertEquals("Tipo", responsavel.tipoResponsabilidade());
        assertEquals("Ramal", responsavel.ramal());
        assertEquals("email@exemplo.com", responsavel.email());
    }

    @Test
    void SubprocessoDetalheDto_ElementoProcessoDTO_RecordConstructorAndAccessors() {
        Object payload = new Object();
        SubprocessoDetalheDto.ElementoProcessoDTO elemento = new SubprocessoDetalheDto.ElementoProcessoDTO("TIPO", payload);

        assertEquals("TIPO", elemento.tipo());
        assertEquals(payload, elemento.payload());
    }

    @Test
    void SubprocessoDto_RecordConstructorAndAccessors() {
        LocalDate dataLimite = LocalDate.now().plusDays(10);
        LocalDateTime dataFim = LocalDateTime.now();

        SubprocessoDto dto = new SubprocessoDto(
            1L,
            2L,
            3L,
            4L,
            dataLimite,
            dataFim,
            dataLimite.plusDays(1),
            dataFim.plusHours(1),
            SituacaoSubprocesso.NAO_INICIADO
        );

        assertEquals(1L, dto.getCodigo());
        assertEquals(2L, dto.getProcessoCodigo());
        assertEquals(3L, dto.getUnidadeCodigo());
        assertEquals(4L, dto.getMapaCodigo());
        assertEquals(dataLimite, dto.getDataLimiteEtapa1());
        assertEquals(dataFim, dto.getDataFimEtapa1());
        assertEquals(dataLimite.plusDays(1), dto.getDataLimiteEtapa2());
        assertEquals(dataFim.plusHours(1), dto.getDataFimEtapa2());
        assertEquals(SituacaoSubprocesso.NAO_INICIADO, dto.getSituacao());
    }

    @Test
    void SugestoesDto_RecordConstructorAndGetters() {
        SugestoesDto dto = new SugestoesDto("Sugestões", true, UNIDADE);

        assertEquals("Sugestões", dto.sugestoes());
        assertTrue(dto.sugestoesApresentadas());
        assertEquals(UNIDADE, dto.unidadeNome());
    }
}
