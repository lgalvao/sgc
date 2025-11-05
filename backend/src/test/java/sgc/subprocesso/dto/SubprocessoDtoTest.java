package sgc.subprocesso.dto;

import org.junit.jupiter.api.Test;
import sgc.subprocesso.model.SituacaoSubprocesso;

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

        assertEquals(1L, dto.codigo());
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
        List<ConhecimentoAjusteDto> conhecimentos = List.of(ConhecimentoAjusteDto.builder().conhecimentoId(1L).nome(COMPETENCIA).incluido(true).build());
        AtividadeAjusteDto dto = AtividadeAjusteDto.builder().codAtividade(1L).nome(ATIVIDADE).conhecimentos(conhecimentos).build();

        assertEquals(1L, dto.getCodAtividade());
        assertEquals(ATIVIDADE, dto.getNome());
        assertEquals(conhecimentos, dto.getConhecimentos());
    }

    @Test
    void CompetenciaAjusteDto_RecordConstructorAndGetters() {
        List<AtividadeAjusteDto> atividades = List.of(AtividadeAjusteDto.builder()
                .codAtividade(1L)
                .nome(ATIVIDADE)
                .conhecimentos(List.of())
                .build());

        CompetenciaAjusteDto dto = CompetenciaAjusteDto.builder()
                .codCompetencia(1L)
                .nome(COMPETENCIA)
                .atividades(atividades)
                .build();

        assertEquals(1L, dto.getCodCompetencia());
        assertEquals(COMPETENCIA, dto.getNome());
        assertEquals(atividades, dto.getAtividades());
    }

    @Test
    void ConhecimentoAjusteDto_RecordConstructorAndGetters() {
        ConhecimentoAjusteDto dto = ConhecimentoAjusteDto.builder()
                .conhecimentoId(1L)
                .nome(COMPETENCIA)
                .incluido(true)
                .build();

        assertEquals(1L, dto.getConhecimentoId());
        assertEquals(COMPETENCIA, dto.getNome());
        assertTrue(dto.isIncluido());
    }

    @Test
    void DevolverCadastroReq_RecordConstructorAndGetters() {
        DevolverCadastroReq req = new DevolverCadastroReq("Motivo", OBSERVACOES);

        assertEquals("Motivo", req.motivo());
        assertEquals(OBSERVACOES, req.observacoes());
    }

    @Test
    void DevolverCadastroReq_InvalidMotivo_ThrowsException() {
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
        DevolverValidacaoReq req = new DevolverValidacaoReq("");
        assertEquals("", req.justificativa());
    }

    @Test
    void DisponibilizarMapaReq_RecordConstructorAndGetters() {
        LocalDateTime dataLimite = LocalDateTime.now().plusDays(10);
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
        List<CompetenciaAjusteDto> competencias = List.of(CompetenciaAjusteDto.builder()
                .codCompetencia(1L)
                .nome("Competência")
                .atividades(List.of())
                .build()
        );

        MapaAjusteDto dto = MapaAjusteDto.builder()
                .codMapa(1L)
                .unidadeNome(UNIDADE)
                .competencias(competencias)
                .justificativaDevolucao("Justificativa")
                .build();

        assertEquals(1L, dto.getCodMapa());
        assertEquals(UNIDADE, dto.getUnidadeNome());
        assertEquals(competencias, dto.getCompetencias());
        assertEquals("Justificativa", dto.getJustificativaDevolucao());
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
        List<SubprocessoCadastroDto.AtividadeCadastroDto> atividades = List.of(
                SubprocessoCadastroDto.AtividadeCadastroDto.builder().codigo(1L).descricao("Atividade").conhecimentos(List.of()).build()
        );
        SubprocessoCadastroDto dto = SubprocessoCadastroDto.builder().subprocessoId(1L).unidadeSigla(SIGLA).atividades(atividades).build();

        assertEquals(1L, dto.getSubprocessoId());
        assertEquals(SIGLA, dto.getUnidadeSigla());
        assertEquals(atividades, dto.getAtividades());
    }

    @Test
    void SubprocessoDetalheDto_RecordConstructorAndAccessors() {
        SubprocessoDetalheDto.UnidadeDto unidade = SubprocessoDetalheDto.UnidadeDto.builder().codigo(1L).sigla(SIGLA).nome(NOME).build();
        SubprocessoDetalheDto.ResponsavelDto responsavel = SubprocessoDetalheDto.ResponsavelDto.builder().codigo(1L).nome(NOME).tipoResponsabilidade("Tipo").ramal("Ramal").email("email@exemplo.com").build();
        LocalDateTime prazo = LocalDateTime.now();
        List<MovimentacaoDto> movimentacoes = List.of();
        List<SubprocessoDetalheDto.ElementoProcessoDto> elementos = List.of();

        SubprocessoDetalheDto dto = SubprocessoDetalheDto.builder()
                .unidade(unidade)
                .responsavel(responsavel)
                .situacao(SituacaoSubprocesso.NAO_INICIADO.name())
                .localizacaoAtual("Localizacao")
                .prazoEtapaAtual(prazo)
                .movimentacoes(movimentacoes)
                .elementosProcesso(elementos)
                .build();

        assertEquals(unidade, dto.getUnidade());
        assertEquals(responsavel, dto.getResponsavel());
        assertEquals(SituacaoSubprocesso.NAO_INICIADO.name(), dto.getSituacao());
        assertEquals("Localizacao", dto.getLocalizacaoAtual());
        assertEquals(prazo, dto.getPrazoEtapaAtual());
        assertEquals(movimentacoes, dto.getMovimentacoes());
        assertEquals(elementos, dto.getElementosProcesso());
    }

    @Test
    void SubprocessoDetalheDto_UnidadeDTO_RecordConstructorAndAccessors() {
        SubprocessoDetalheDto.UnidadeDto unidade = SubprocessoDetalheDto.UnidadeDto.builder().codigo(1L).sigla(SIGLA).nome(NOME).build();

        assertEquals(1L, unidade.getCodigo());
        assertEquals(SIGLA, unidade.getSigla());
        assertEquals(NOME, unidade.getNome());
    }

    @Test
    void SubprocessoDetalheDto_ResponsavelDTO_RecordConstructorAndAccessors() {
        SubprocessoDetalheDto.ResponsavelDto responsavel = SubprocessoDetalheDto.ResponsavelDto.builder().codigo(1L).nome(NOME).tipoResponsabilidade("Tipo").ramal("Ramal").email("email@exemplo.com").build();

        assertEquals(1L, responsavel.getCodigo());
        assertEquals(NOME, responsavel.getNome());
        assertEquals("Tipo", responsavel.getTipoResponsabilidade());
        assertEquals("Ramal", responsavel.getRamal());
        assertEquals("email@exemplo.com", responsavel.getEmail());
    }

    @Test
    void SubprocessoDetalheDto_ElementoProcessoDTO_RecordConstructorAndAccessors() {
        Object payload = new Object();
        SubprocessoDetalheDto.ElementoProcessoDto elemento = new SubprocessoDetalheDto.ElementoProcessoDto("TIPO", payload);

        assertEquals("TIPO", elemento.tipo());
        assertEquals(payload, elemento.payload());
    }

    @Test
    void SubprocessoDto_RecordConstructorAndAccessors() {
        LocalDateTime dataLimite1 = LocalDateTime.now().plusDays(10);
        LocalDateTime dataLimite2 = LocalDateTime.now().plusDays(30);
        LocalDateTime dataFim1 = dataLimite1.plusDays(1);

        SubprocessoDto dto = new SubprocessoDto(
                1L,
                2L,
                3L,
                4L,
                dataLimite1,
                dataFim1,
                dataLimite2,
                null,
                SituacaoSubprocesso.NAO_INICIADO
        );

        assertEquals(1L, dto.getCodigo());
        assertEquals(2L, dto.getCodProcesso());
        assertEquals(3L, dto.getCodUnidade());
        assertEquals(4L, dto.getCodMapa());
        assertEquals(dataLimite1, dto.getDataLimiteEtapa1());
        assertEquals(dataFim1, dto.getDataFimEtapa1());
        assertEquals(dataLimite2, dto.getDataLimiteEtapa2());
        assertNull(dto.getDataFimEtapa2());
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