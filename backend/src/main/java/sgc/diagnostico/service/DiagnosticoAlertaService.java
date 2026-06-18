package sgc.diagnostico.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.alerta.AlertaAplicacaoService;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.service.UnidadeService;
import sgc.processo.model.Processo;

@Service
@RequiredArgsConstructor
@Transactional
public class DiagnosticoAlertaService {
    private final AlertaAplicacaoService alertaAplicacaoService;
    private final UnidadeService unidadeService;

    public void criarAlertaPessoal(String usuarioTitulo, String descricao) {
        alertaAplicacaoService.criarAlertaPessoal(usuarioTitulo, descricao);
    }

    public void criarAlertaPessoal(Processo processo, Unidade origem, Unidade destino, String usuarioTitulo, String descricao) {
        alertaAplicacaoService.criarAlertaPessoal(processo, origem, destino, usuarioTitulo, descricao);
    }

    public void criarAlertaTransicao(Processo processo, String descricao, Unidade origem, Unidade destino) {
        alertaAplicacaoService.criarAlertaTransicao(processo, descricao, origem, destino);
    }

    private Unidade unidadeRaiz() {
        return unidadeService.buscarAdmin();
    }
}
