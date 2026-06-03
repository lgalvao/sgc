package sgc.diagnostico.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.alerta.model.Alerta;
import sgc.alerta.model.AlertaRepo;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.service.UnidadeService;
import sgc.processo.model.Processo;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class DiagnosticoAlertaService {
    private final AlertaRepo alertaRepo;
    private final UnidadeService unidadeService;

    public void criarAlertaPessoal(String usuarioTitulo, String descricao) {
        alertaRepo.save(Alerta.builder()
                .dataHora(LocalDateTime.now())
                .unidadeOrigem(unidadeRaiz())
                .usuarioDestinoTitulo(usuarioTitulo)
                .descricao(descricao)
                .build());
    }

    public void criarAlertaTransicao(Processo processo, String descricao, Unidade origem, Unidade destino) {
        alertaRepo.save(Alerta.builder()
                .processo(processo)
                .dataHora(LocalDateTime.now())
                .unidadeOrigem(origem)
                .unidadeDestino(destino)
                .descricao(descricao)
                .build());
    }

    private Unidade unidadeRaiz() {
        return unidadeService.buscarAdmin();
    }
}
