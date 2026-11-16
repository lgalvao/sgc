package sesel.sgc.atribuicao;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sesel.sgc.notificacao.NotificacaoService;
import sesel.sgc.processo.Processo;
import sesel.sgc.servidor.Servidor;
import sesel.sgc.unidade.Unidade;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AtribuicaoService {
    private final AtribuicaoRepository atribuicaoRepository;
    private final NotificacaoService notificacaoService;

    @Transactional
    public Atribuicao criarAtribuicao(Unidade unidade, Servidor servidor, Processo processo,
                                      LocalDate dataInicio, LocalDate dataTermino, String justificativa) {

        Atribuicao atribuicao = Atribuicao.builder()
                .unidade(unidade)
                .servidor(servidor)
                .processo(processo)
                .dataInicio(dataInicio)
                .dataTermino(dataTermino)
                .justificativa(justificativa)
                .build();

        atribuicao = atribuicaoRepository.save(atribuicao);

        // Envia notificação para o servidor
        notificacaoService.notificarAtribuicaoTemporaria(atribuicao);

        return atribuicao;
    }

    public List<Atribuicao> buscarAtribuicoesPorUnidade(UUID unidadeId) {
        return atribuicaoRepository.findByUnidadeId(unidadeId);
    }

    public List<Atribuicao> buscarAtribuicoesPorServidor(UUID servidorId) {
        return atribuicaoRepository.findByServidorId(servidorId);
    }

    @Transactional
    public void removerAtribuicao(UUID id) {
        Atribuicao atribuicao = atribuicaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Atribuição não encontrada"));

        atribuicaoRepository.delete(atribuicao);
    }
}