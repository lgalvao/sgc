package sgc.diagnostico.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sgc.comum.Mensagens;
import sgc.comum.erros.ErroValidacao;
import sgc.diagnostico.model.AvaliacaoServidorRepo;
import sgc.diagnostico.model.SituacaoAvaliacaoServidor;
import sgc.subprocesso.model.TipoAcaoAnalise;
import sgc.subprocesso.model.TipoAnalise;
import sgc.subprocesso.service.SubprocessoVisualizacaoService;

import java.util.EnumSet;

@Service
@RequiredArgsConstructor
public class DiagnosticoValidacaoService {
    private static final EnumSet<SituacaoAvaliacaoServidor> SITUACOES_CONCLUIDAS_UNIDADE = EnumSet.of(
            SituacaoAvaliacaoServidor.CONSENSO_APROVADO,
            SituacaoAvaliacaoServidor.AVALIACAO_IMPOSSIBILITADA
    );

    private final AvaliacaoServidorRepo avaliacaoRepo;
    private final SubprocessoVisualizacaoService subprocessoVisualizacaoService;

    public void validarAutoavaliacaoCompleta(Long diagnosticoCodigo, String servidorTitulo) {
        var avaliacoes = avaliacaoRepo.buscarAvaliacoesDoServidor(diagnosticoCodigo, servidorTitulo);
        boolean incompleta = avaliacoes.isEmpty()
                || avaliacoes.stream().anyMatch(a -> a.getImportancia() == null || a.getDominio() == null);
        if (incompleta) {
            throw new ErroValidacao("Preencha importância e domínio para todas as competências.");
        }
    }

    public void validarConsensoCompleto(Long diagnosticoCodigo, String servidorTitulo) {
        var avaliacoes = avaliacaoRepo.buscarAvaliacoesDoServidor(diagnosticoCodigo, servidorTitulo);
        boolean incompleto = avaliacoes.isEmpty()
                || avaliacoes.stream().anyMatch(a ->
                a.getChefiaImportancia() == null
                        || a.getChefiaDominio() == null
                        || a.getConsensoImportancia() == null
                        || a.getConsensoDominio() == null
        );
        if (incompleto) {
            throw new ErroValidacao("Preencha todos os campos");
        }
    }

    public void validarConclusaoUnidade(Long diagnosticoCodigo) {
        boolean semAvaliacoes = !avaliacaoRepo.existsByDiagnosticoCodigo(diagnosticoCodigo);
        boolean possuiAvaliacoesPendentes = avaliacaoRepo.existsAvaliacaoPendentePorDiagnostico(
                diagnosticoCodigo,
                SITUACOES_CONCLUIDAS_UNIDADE
        );
        boolean possuiSituacaoCapacitacaoPendente = avaliacaoRepo.existsAvaliacaoAprovadaSemSituacaoCapacitacao(
                diagnosticoCodigo,
                SituacaoAvaliacaoServidor.CONSENSO_APROVADO
        );
        boolean possuiAvaliacaoAprovada = avaliacaoRepo.existsByDiagnosticoCodigoAndSituacaoServidor(
                diagnosticoCodigo,
                SituacaoAvaliacaoServidor.CONSENSO_APROVADO
        );

        if (semAvaliacoes || possuiAvaliacoesPendentes || possuiSituacaoCapacitacaoPendente) {
            throw new ErroValidacao(Mensagens.DIAGNOSTICO_PENDENTE);
        }
        if (!possuiAvaliacaoAprovada) {
            throw new ErroValidacao(Mensagens.DIAGNOSTICO_TODOS_SERVIDORES_IMPOSSIBILITADOS);
        }
    }

    public void validarDiagnosticoHomologavel(Long codSubprocesso) {
        boolean possuiAceite = subprocessoVisualizacaoService.possuiAnalise(
                codSubprocesso,
                TipoAnalise.DIAGNOSTICO,
                TipoAcaoAnalise.ACEITE_DIAGNOSTICO
        );
        if (!possuiAceite) {
            throw new ErroValidacao("Diagnóstico fora da situação esperada para esta ação.");
        }
    }
}
