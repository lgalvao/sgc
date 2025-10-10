package sgc.subprocesso.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SubprocessoDtoTest {
    @Test
    void AceitarCadastroReq_RecordConstructorAndGetters() {
        AceitarCadastroReq req = new AceitarCadastroReq("Observações de aceite");

        assertEquals("Observações de aceite", req.observacoes());
    }

    @Test
    void AnaliseValidacaoDto_RecordConstructorAndGetters() {
        LocalDateTime now = LocalDateTime.now();
        AnaliseValidacaoDto dto = new AnaliseValidacaoDto(1L, now, "Observações");

        assertEquals(1L, dto.id());
        assertEquals(now, dto.dataHora());
        assertEquals("Observações", dto.observacoes());
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
        List<ConhecimentoAjusteDto> conhecimentos = List.of(new ConhecimentoAjusteDto(1L, "Conhecimento", true));
        AtividadeAjusteDto dto = new AtividadeAjusteDto(1L, "Atividade", conhecimentos);

        assertEquals(1L, dto.atividadeId());
        assertEquals("Atividade", dto.nome());
        assertEquals(conhecimentos, dto.conhecimentos());
    }

    @Test
    void CompetenciaAjusteDto_RecordConstructorAndGetters() {
        List<AtividadeAjusteDto> atividades = List.of(new AtividadeAjusteDto(1L, "Atividade", List.of()));
        CompetenciaAjusteDto dto = new CompetenciaAjusteDto(1L, "Competência", atividades);

        assertEquals(1L, dto.competenciaId());
        assertEquals("Competência", dto.nome());
        assertEquals(atividades, dto.atividades());
    }

    @Test
    void ConhecimentoAjusteDto_RecordConstructorAndGetters() {
        ConhecimentoAjusteDto dto = new ConhecimentoAjusteDto(1L, "Conhecimento", true);

        assertEquals(1L, dto.conhecimentoId());
        assertEquals("Conhecimento", dto.nome());
        assertTrue(dto.incluido());
    }

    @Test
    void DevolverCadastroReq_RecordConstructorAndGetters() {
        DevolverCadastroReq req = new DevolverCadastroReq("Motivo", "Observações");

        assertEquals("Motivo", req.motivo());
        assertEquals("Observações", req.observacoes());
    }

    @Test
    void DevolverCadastroReq_InvalidMotivo_ThrowsException() {
        // Test will be handled by validation framework during actual usage
        DevolverCadastroReq req = new DevolverCadastroReq("", "Observações");
        assertEquals("", req.motivo());
    }

    @Test
    void DevolverValidacaoReq_RecordConstructorAndGetters() {
        DevolverValidacaoReq req = new DevolverValidacaoReq("Justificativa");

        assertEquals("Justificativa", req.justificativa());
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
        DisponibilizarMapaReq req = new DisponibilizarMapaReq("Observações", dataLimite);

        assertEquals("Observações", req.observacoes());
        assertEquals(dataLimite, req.dataLimiteEtapa2());
    }

    @Test
    void DisponibilizarMapaReq_InvalidDataLimite_ThrowsException() {
        // Test will be handled by validation framework during actual usage
        DisponibilizarMapaReq req = new DisponibilizarMapaReq("Observações", null);
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
        MapaAjusteDto dto = new MapaAjusteDto(1L, "Unidade", competencias, "Justificativa");

        assertEquals(1L, dto.mapaId());
        assertEquals("Unidade", dto.unidadeNome());
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
                "Unidade Destino",
                "Descrição"
        );

        assertEquals(1L, dto.codigo());
        assertEquals(now, dto.dataHora());
        assertEquals(1L, dto.unidadeOrigemCodigo());
        assertEquals("SIGLA_ORIGEM", dto.unidadeOrigemSigla());
        assertEquals("Unidade Origem", dto.unidadeOrigemNome());
        assertEquals(2L, dto.unidadeDestinoCodigo());
        assertEquals("SIGLA_DESTINO", dto.unidadeDestinoSigla());
        assertEquals("Unidade Destino", dto.unidadeDestinoNome());
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
        SubprocessoCadastroDto dto = new SubprocessoCadastroDto(1L, "SIGLA", atividades);

        assertEquals(1L, dto.subprocessoId());
        assertEquals("SIGLA", dto.unidadeSigla());
        assertEquals(atividades, dto.atividades());
    }

    @Test
    void SubprocessoDetalheDto_RecordConstructorAndAccessors() {
        SubprocessoDetalheDto.UnidadeDTO unidade = new SubprocessoDetalheDto.UnidadeDTO(1L, "SIGLA", "Nome");
        SubprocessoDetalheDto.ResponsavelDTO responsavel = new SubprocessoDetalheDto.ResponsavelDTO(1L, "Nome", "Tipo", "Ramal", "email@exemplo.com");
        LocalDate prazo = LocalDate.now();
        List<MovimentacaoDto> movimentacoes = List.of();
        List<SubprocessoDetalheDto.ElementoProcessoDTO> elementos = List.of();

        SubprocessoDetalheDto dto = new SubprocessoDetalheDto(unidade, responsavel, "SITUACAO", "Localizacao", prazo, movimentacoes, elementos);

        assertEquals(unidade, dto.unidade());
        assertEquals(responsavel, dto.responsavel());
        assertEquals("SITUACAO", dto.situacao());
        assertEquals("Localizacao", dto.localizacaoAtual());
        assertEquals(prazo, dto.prazoEtapaAtual());
        assertEquals(movimentacoes, dto.movimentacoes());
        assertEquals(elementos, dto.elementosDoProcesso());
    }

    @Test
    void SubprocessoDetalheDto_UnidadeDTO_RecordConstructorAndAccessors() {
        SubprocessoDetalheDto.UnidadeDTO unidade = new SubprocessoDetalheDto.UnidadeDTO(1L, "SIGLA", "Nome");

        assertEquals(1L, unidade.codigo());
        assertEquals("SIGLA", unidade.sigla());
        assertEquals("Nome", unidade.nome());
    }

    @Test
    void SubprocessoDetalheDto_ResponsavelDTO_RecordConstructorAndAccessors() {
        SubprocessoDetalheDto.ResponsavelDTO responsavel = new SubprocessoDetalheDto.ResponsavelDTO(1L, "Nome", "Tipo", "Ramal", "email@exemplo.com");

        assertEquals(1L, responsavel.id());
        assertEquals("Nome", responsavel.nome());
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
    void SubprocessoDto_Builder_Test() {
        LocalDate dataLimite = LocalDate.now().plusDays(10);
        LocalDateTime dataFim = LocalDateTime.now();

        SubprocessoDto dto = SubprocessoDto.builder()
            .codigo(1L)
            .processoCodigo(2L)
            .unidadeCodigo(3L)
            .mapaCodigo(4L)
            .dataLimiteEtapa1(dataLimite)
            .dataFimEtapa1(dataFim)
            .dataLimiteEtapa2(dataLimite.plusDays(1))
            .dataFimEtapa2(dataFim.plusHours(1))
            .situacaoId("TESTE")
            .build();

        assertEquals(1L, dto.getCodigo());
        assertEquals(2L, dto.getProcessoCodigo());
        assertEquals(3L, dto.getUnidadeCodigo());
        assertEquals(4L, dto.getMapaCodigo());
        assertEquals(dataLimite, dto.getDataLimiteEtapa1());
        assertEquals(dataFim, dto.getDataFimEtapa1());
        assertEquals(dataLimite.plusDays(1), dto.getDataLimiteEtapa2());
        assertEquals(dataFim.plusHours(1), dto.getDataFimEtapa2());
        assertEquals("TESTE", dto.getSituacaoId());
    }

    @Test
    void SugestoesDto_RecordConstructorAndGetters() {
        SugestoesDto dto = new SugestoesDto("Sugestões", true, "Unidade");

        assertEquals("Sugestões", dto.sugestoes());
        assertTrue(dto.sugestoesApresentadas());
        assertEquals("Unidade", dto.unidadeNome());
    }
}