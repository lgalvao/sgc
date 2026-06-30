package sgc.integracao;

import jakarta.persistence.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import sgc.diagnostico.dto.*;
import sgc.diagnostico.model.*;
import sgc.diagnostico.service.*;
import sgc.fixture.*;
import sgc.mapa.model.*;
import sgc.organizacao.model.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

@Tag("integration")
@DisplayName("Budget de Queries da Consulta de Diagnóstico")
class DiagnosticoConsultaQueryBudgetIntegrationTest extends DiagnosticoCduIntegrationTestBase {

    @Autowired
    private DiagnosticoConsultaService diagnosticoConsultaService;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @BeforeEach
    void setUp() {
        criarCenarioDiagnosticoBase(9L, "50003", "50004");
        preencherAutoavaliacao("50003", 5, 3, 4, 2, SituacaoAvaliacaoServidor.AUTOAVALIACAO_CONCLUIDA);
        preencherConsenso("50003", 6, 4, 6, 4, 5, 3, 5, 3, SituacaoAvaliacaoServidor.CONSENSO_APROVADO);
        preencherAutoavaliacao("50004", 3, 3, 2, 2, SituacaoAvaliacaoServidor.AVALIACAO_IMPOSSIBILITADA);
        preencherSituacoesCapacitacao("50003", ValorSituacaoCapacitacao.EC, ValorSituacaoCapacitacao.C);
    }

    @Test
    @sgc.integracao.mocks.WithMockCustomUser(tituloEleitoral = "50003", unidadeId = 9L, perfis = {"SERVIDOR"})
    @DisplayName("obterConsenso de servidor não deve aumentar consultas ao crescer a quantidade de competências")
    void obterConsensoServidorNaoDeveAumentarConsultasQuandoCrescemAsCompetencias() {
        MetricasExecucaoTeste.ResultadoMedicao medicaoBase = medirObterConsenso();
        adicionarCompetenciasAoDiagnostico(6);
        MetricasExecucaoTeste.ResultadoMedicao medicaoAmpliada = medirObterConsenso();

        assertThat(medicaoBase.resumo()).isNotBlank();
        assertThat(medicaoAmpliada.resumo()).isNotBlank();
        medicaoBase.validarTempoSeEstrito(250);
        medicaoAmpliada.validarTempoSeEstrito(250);

        assertThat(medicaoAmpliada.preparedStatements())
                .as("""
                        A consulta de consenso deve executar a mesma quantidade de statements
                        mesmo após ampliar o número de competências do servidor.
                        Base: %s
                        Ampliada: %s
                        """.formatted(medicaoBase.resumo(), medicaoAmpliada.resumo()))
                .isEqualTo(medicaoBase.preparedStatements());
        assertThat(medicaoAmpliada.contagensPorTrecho())
                .as("""
                        As consultas estruturais do consenso não devem multiplicar com a quantidade
                        de competências carregadas.
                        Base: %s
                        Ampliada: %s
                        """.formatted(medicaoBase.resumo(), medicaoAmpliada.resumo()))
                .containsEntry("VW_USUARIO", medicaoBase.contagensPorTrecho().get("VW_USUARIO"))
                .containsEntry("AVALIACAO_SERVIDOR", medicaoBase.contagensPorTrecho().get("AVALIACAO_SERVIDOR"))
                .containsEntry("COMPETENCIA", medicaoBase.contagensPorTrecho().get("COMPETENCIA"));

        ConsensoDto dto = (ConsensoDto) medicaoAmpliada.resultado();
        assertThat(dto.competencias()).hasSize(8);
        assertThat(dto.servidorNome()).isEqualTo(servidor.getNome());
    }

    @Test
    @DisplayName("obterDiagnosticoUnidade não deve aumentar consultas ao crescer o volume de competências")
    void obterDiagnosticoUnidadeNaoDeveAumentarConsultasQuandoCresceOVolumeDeCompetencias() {
        MetricasExecucaoTeste.ResultadoMedicao medicaoBase = medirObterDiagnosticoUnidade();
        adicionarCompetenciasAoDiagnostico(6);
        MetricasExecucaoTeste.ResultadoMedicao medicaoAmpliada = medirObterDiagnosticoUnidade();

        assertThat(medicaoBase.resumo()).isNotBlank();
        assertThat(medicaoAmpliada.resumo()).isNotBlank();
        medicaoBase.validarTempoSeEstrito(400);
        medicaoAmpliada.validarTempoSeEstrito(400);

        assertThat(medicaoAmpliada.preparedStatements())
                .as("""
                        A carga da visão de diagnóstico da unidade não deve disparar mais statements
                        só porque existem mais competências e mais linhas associadas no diagnóstico.
                        Base: %s
                        Ampliada: %s
                        """.formatted(medicaoBase.resumo(), medicaoAmpliada.resumo()))
                .isEqualTo(medicaoBase.preparedStatements());
        assertThat(medicaoAmpliada.contagensPorTrecho())
                .as("""
                        As consultas principais do diagnóstico da unidade devem permanecer constantes
                        quando o volume de competências aumenta.
                        Base: %s
                        Ampliada: %s
                        """.formatted(medicaoBase.resumo(), medicaoAmpliada.resumo()))
                .containsEntry("VW_USUARIO", medicaoBase.contagensPorTrecho().get("VW_USUARIO"))
                .containsEntry("AVALIACAO_SERVIDOR", medicaoBase.contagensPorTrecho().get("AVALIACAO_SERVIDOR"))
                .containsEntry("SITUACAO_CAPACITACAO", medicaoBase.contagensPorTrecho().get("SITUACAO_CAPACITACAO"))
                .containsEntry("MOVIMENTACAO", medicaoBase.contagensPorTrecho().get("MOVIMENTACAO"));

        DiagnosticoUnidadeDto dto = (DiagnosticoUnidadeDto) medicaoAmpliada.resultado();
        assertThat(dto.servidores()).hasSize(2);
        assertThat(dto.situacoesCapacitacao()).hasSize(16);
        assertThat(dto.movimentacoes()).isNotEmpty();
    }

    @Test
    @DisplayName("obterDiagnosticoUnidade não deve aumentar consultas ao crescer a quantidade de servidores")
    void obterDiagnosticoUnidadeNaoDeveAumentarConsultasQuandoCresceAQuantidadeDeServidores() {
        MetricasExecucaoTeste.ResultadoMedicao medicaoBase = medirObterDiagnosticoUnidade();
        adicionarServidoresAoDiagnostico(4);
        MetricasExecucaoTeste.ResultadoMedicao medicaoAmpliada = medirObterDiagnosticoUnidade();

        assertThat(medicaoAmpliada.preparedStatements())
                .as("""
                        A visão de diagnóstico da unidade não deve adicionar novas consultas
                        estruturais só porque mais servidores foram incluídos no diagnóstico.
                        Base: %s
                        Ampliada: %s
                        """.formatted(medicaoBase.resumo(), medicaoAmpliada.resumo()))
                .isEqualTo(medicaoBase.preparedStatements());
        assertThat(medicaoAmpliada.contagensPorTrecho())
                .as("""
                        As consultas principais da visão da unidade devem permanecer constantes
                        quando o número de servidores cresce.
                        Base: %s
                        Ampliada: %s
                        """.formatted(medicaoBase.resumo(), medicaoAmpliada.resumo()))
                .containsEntry("VW_USUARIO", medicaoBase.contagensPorTrecho().get("VW_USUARIO"))
                .containsEntry("AVALIACAO_SERVIDOR", medicaoBase.contagensPorTrecho().get("AVALIACAO_SERVIDOR"))
                .containsEntry("SITUACAO_CAPACITACAO", medicaoBase.contagensPorTrecho().get("SITUACAO_CAPACITACAO"))
                .containsEntry("MOVIMENTACAO", medicaoBase.contagensPorTrecho().get("MOVIMENTACAO"));

        DiagnosticoUnidadeDto dto = (DiagnosticoUnidadeDto) medicaoAmpliada.resultado();
        assertThat(dto.servidores()).hasSize(6);
        assertThat(dto.situacoesCapacitacao()).hasSize(12);
    }

    private MetricasExecucaoTeste.ResultadoMedicao medirObterConsenso() {
        return MetricasExecucaoTeste.medir(
                entityManager,
                entityManagerFactory,
                "diagnostico-consenso-servidor",
                () -> diagnosticoConsultaService.obterConsenso(subprocesso.getCodigo(), "50003"),
                "VW_USUARIO",
                "AVALIACAO_SERVIDOR",
                "COMPETENCIA"
        );

    }

    private MetricasExecucaoTeste.ResultadoMedicao medirObterDiagnosticoUnidade() {
        return MetricasExecucaoTeste.medir(
                entityManager,
                entityManagerFactory,
                "diagnostico-unidade",
                () -> diagnosticoConsultaService.obterDiagnosticoUnidade(subprocesso.getCodigo()),
                "VW_USUARIO",
                "AVALIACAO_SERVIDOR",
                "SITUACAO_CAPACITACAO",
                "MOVIMENTACAO"
        );
    }

    private void adicionarCompetenciasAoDiagnostico(int quantidadeNovasCompetencias) {
        Mapa mapa = subprocesso.getMapa();
        List<AvaliacaoServidor> novasAvaliacoes = new ArrayList<>();
        List<SituacaoCapacitacao> novasSituacoes = new ArrayList<>();

        for (int indice = 0; indice < quantidadeNovasCompetencias; indice++) {
            Competencia competencia = competenciaRepo.saveAndFlush(Competencia.builder()
                    .mapa(mapa)
                    .descricao("Competência adicional %d %d".formatted(indice + 1, System.nanoTime()))
                    .build());

            novasAvaliacoes.add(novaAvaliacao(servidor, competencia, 6, 4, 5, 3, SituacaoAvaliacaoServidor.CONSENSO_APROVADO));
            novasAvaliacoes.add(novaAvaliacao(outroServidor, competencia, 3, 3, 3, 3, SituacaoAvaliacaoServidor.AVALIACAO_IMPOSSIBILITADA));
            novasSituacoes.add(novaSituacaoCapacitacao(servidor, competencia, ValorSituacaoCapacitacao.EC));
            novasSituacoes.add(novaSituacaoCapacitacao(outroServidor, competencia, ValorSituacaoCapacitacao.C));
        }

        avaliacaoServidorRepo.saveAllAndFlush(novasAvaliacoes);
        situacaoCapacitacaoRepo.saveAllAndFlush(novasSituacoes);
        recarregarContexto();
    }

    private void adicionarServidoresAoDiagnostico(int quantidadeNovosServidores) {
        List<Usuario> novosServidores = new ArrayList<>();
        List<AvaliacaoServidor> novasAvaliacoes = new ArrayList<>();
        List<SituacaoCapacitacao> novasSituacoes = new ArrayList<>();
        List<Competencia> competencias = List.of(competencia1, competencia2);

        for (int indice = 0; indice < quantidadeNovosServidores; indice++) {
            Usuario usuario = UsuarioFixture.usuarioComTitulo("9100000%02d".formatted(indice));
            usuario.setNome("Servidor adicional %d".formatted(indice + 1));
            usuario.setEmail("servidor.adicional.%d@tre-pe.jus.br".formatted(indice + 1));
            usuario.setRamal("91%02d".formatted(indice));
            usuario.setUnidadeLotacao(unidade);
            novosServidores.add(usuarioRepo.saveAndFlush(usuario));
        }

        for (Usuario usuario : novosServidores) {
            for (Competencia competencia : competencias) {
                novasAvaliacoes.add(novaAvaliacao(
                        usuario,
                        competencia,
                        4,
                        3,
                        4,
                        3,
                        SituacaoAvaliacaoServidor.CONSENSO_APROVADO
                ));
                novasSituacoes.add(novaSituacaoCapacitacao(
                        usuario,
                        competencia,
                        ValorSituacaoCapacitacao.AC
                ));
            }
        }

        avaliacaoServidorRepo.saveAllAndFlush(novasAvaliacoes);
        situacaoCapacitacaoRepo.saveAllAndFlush(novasSituacoes);
        recarregarContexto();
    }

    private AvaliacaoServidor novaAvaliacao(
            sgc.organizacao.model.Usuario usuario,
            Competencia competencia,
            int importancia,
            int dominio,
            int autoimportancia,
            int autodominio,
            SituacaoAvaliacaoServidor situacao
    ) {
        AvaliacaoServidor avaliacao = AvaliacaoServidor.builder()
                .diagnostico(diagnostico)
                .servidor(usuario)
                .servidorNomeSnapshot(usuario.getNome())
                .competencia(competencia)
                .autoimportancia(autoimportancia)
                .autodominio(autodominio)
                .chefiaImportancia(importancia)
                .chefiaDominio(dominio)
                .importancia(importancia)
                .dominio(dominio)
                .situacaoServidor(situacao)
                .build();
        avaliacao.calculaGap();
        return avaliacao;
    }

    private SituacaoCapacitacao novaSituacaoCapacitacao(
            sgc.organizacao.model.Usuario usuario,
            Competencia competencia,
            ValorSituacaoCapacitacao situacao
    ) {
        return SituacaoCapacitacao.builder()
                .diagnostico(diagnostico)
                .servidor(usuario)
                .servidorNomeSnapshot(usuario.getNome())
                .unidadeCodigoSnapshot(unidade.getCodigo())
                .unidadeSiglaSnapshot(unidade.getSigla())
                .unidadeNomeSnapshot(unidade.getNome())
                .competencia(competencia)
                .situacaoCapacitacao(situacao)
                .build();
    }
}
