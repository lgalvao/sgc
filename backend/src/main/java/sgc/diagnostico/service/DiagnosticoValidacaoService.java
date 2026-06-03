package sgc.diagnostico.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sgc.comum.erros.ErroValidacao;
import sgc.diagnostico.model.AvaliacaoServidorRepo;
import sgc.diagnostico.model.Diagnostico;
import sgc.diagnostico.model.OcupacaoCriticaRepo;
import sgc.diagnostico.model.SituacaoDiagnostico;

@Service
@RequiredArgsConstructor
public class DiagnosticoValidacaoService {
    private final AvaliacaoServidorRepo avaliacaoRepo;
    private final OcupacaoCriticaRepo ocupacaoRepo;

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

        var ocupacoes = ocupacaoRepo.listarPorDiagnostico(diagnosticoCodigo);
        boolean ocupacoesPendentes = ocupacoes.isEmpty()
                || ocupacoes.stream().anyMatch(o -> o.getSituacaoCapacitacao() == null);

        if (avaliacoesPendentes || ocupacoesPendentes) {
            throw new ErroValidacao("Ainda existem avaliações ou ocupações críticas pendentes.");
        }
    }

    public void validarSituacaoDiagnostico(Diagnostico diagnostico, SituacaoDiagnostico situacaoEsperada) {
        if (diagnostico.getSituacao() != situacaoEsperada) {
            throw new ErroValidacao("Diagnóstico fora da situação esperada para esta ação.");
        }
    }
}
