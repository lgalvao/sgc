package sgc.diagnostico.service;

import lombok.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import sgc.alerta.*;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;
import sgc.processo.model.*;

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
