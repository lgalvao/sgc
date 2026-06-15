package sgc.diagnostico.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sgc.comum.Mensagens;
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
        
        // 1. Todos os servidores devem ter a avaliação concluída (Aprovada ou Impossibilitada)
        boolean avaliacoesIncompletas = avaliacoes.isEmpty()
                || avaliacoes.stream().anyMatch(a ->
                a.getSituacaoServidor() != sgc.diagnostico.model.SituacaoAvaliacaoServidor.CONSENSO_APROVADO
                        && a.getSituacaoServidor() != sgc.diagnostico.model.SituacaoAvaliacaoServidor.AVALIACAO_IMPOSSIBILITADA);

        if (avaliacoesIncompletas) {
            throw new ErroValidacao(Mensagens.DIAGNOSTICO_PENDENTE);
        }

        // 2. Servidores com consenso aprovado devem ter todas as situações de capacitação preenchidas
        var titulosAprovados = avaliacoes.stream()
                .filter(a -> a.getSituacaoServidor() == sgc.diagnostico.model.SituacaoAvaliacaoServidor.CONSENSO_APROVADO)
                .map(a -> a.getServidor().getTituloEleitoral())
                .distinct()
                .toList();

        if (!titulosAprovados.isEmpty()) {
            var situacoes = situacaoCapacitacaoRepo.listarPorDiagnostico(diagnosticoCodigo);
            
            // Mapeia situações preenchidas (não nulas)
            var preenchidas = situacoes.stream()
                    .filter(s -> s.getSituacaoCapacitacao() != null)
                    .map(s -> s.getServidor().getTituloEleitoral() + "-" + s.getCompetencia().getCodigo())
                    .toList();

            // Verifica se alguma avaliação de servidor aprovado não tem sua situação de capacitação correspondente preenchida
            boolean pendente = avaliacoes.stream()
                    .filter(a -> titulosAprovados.contains(a.getServidor().getTituloEleitoral()))
                    .anyMatch(a -> !preenchidas.contains(a.getServidor().getTituloEleitoral() + "-" + a.getCompetencia().getCodigo()));

            if (pendente) {
                throw new ErroValidacao(Mensagens.DIAGNOSTICO_PENDENTE);
            }
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
