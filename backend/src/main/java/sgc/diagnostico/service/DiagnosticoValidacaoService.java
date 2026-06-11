package sgc.diagnostico.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sgc.comum.erros.ErroValidacao;
import sgc.diagnostico.model.AvaliacaoServidorRepo;
import sgc.diagnostico.model.SituacaoCapacitacaoRepo;
import sgc.subprocesso.model.TipoAcaoAnalise;
import sgc.subprocesso.model.TipoAnalise;
import sgc.subprocesso.service.SubprocessoVisualizacaoService;

@Service
@RequiredArgsConstructor
public class DiagnosticoValidacaoService {
    private final AvaliacaoServidorRepo avaliacaoRepo;
    private final SituacaoCapacitacaoRepo situacaoCapacitacaoRepo;
    private final SubprocessoVisualizacaoService subprocessoVisualizacaoService;

    public void validarAutoavaliacaoCompleta(Long diagnosticoCodigo, String servidorTitulo) {
        var avaliacoes = avaliacaoRepo.buscarAvaliacoesDoServidor(diagnosticoCodigo, servidorTitulo);
        boolean incompleta = avaliacoes.isEmpty()
                || avaliacoes.stream().anyMatch(a -> a.getImportancia() == null || a.getDominio() == null);
        if (incompleta) {
            throw new ErroValidacao("Preencha importância e domínio para todas as competências.");
        }
    }

    public void validarConclusaoUnidade(Long diagnosticoCodigo) {
        var avaliacoes = avaliacaoRepo.listarPorDiagnostico(diagnosticoCodigo);
        boolean avaliacoesPendentes = avaliacoes.isEmpty()
                || avaliacoes.stream().anyMatch(a ->
                a.getSituacaoServidor() != sgc.diagnostico.model.SituacaoAvaliacaoServidor.CONSENSO_APROVADO
                        && a.getSituacaoServidor() != sgc.diagnostico.model.SituacaoAvaliacaoServidor.AVALIACAO_IMPOSSIBILITADA);

        var situacoes = situacaoCapacitacaoRepo.listarPorDiagnostico(diagnosticoCodigo);
        boolean situacoesPendentes = situacoes.isEmpty()
                || situacoes.stream().anyMatch(o -> o.getSituacaoCapacitacao() == null);

        if (avaliacoesPendentes || situacoesPendentes) {
            throw new ErroValidacao(Mensagens.DIAGNOSTICO_PENDENTE);
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
