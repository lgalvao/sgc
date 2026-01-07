package sgc.processo.service;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.processo.dto.AtualizarProcessoReq;
import sgc.processo.erros.ErroProcessoEmSituacaoInvalida;
import sgc.processo.model.Processo;
import sgc.processo.model.ProcessoRepo;
import sgc.processo.model.SituacaoProcesso;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static sgc.processo.model.SituacaoProcesso.CRIADO;

/**
 * Testes baseados em propriedade para verificar invariantes de estado do ProcessoService.
 * Mockamos o repositório para fornecer processos em diferentes estados.
 */
@ExtendWith(MockitoExtension.class)
class ProcessoServicePropertyTest {

    @Mock
    private ProcessoRepo processoRepo;

    // Dependências secundárias que não devem ser chamadas se a validação de estado falhar antes
    // Se forem chamadas (o que indicaria falha no teste), o Mockito lançará NullPointerException ou retornará null,
    // mas o foco aqui é a exceção de validação inicial.
    @InjectMocks
    private ProcessoService processoService;

    @Property
    void processoNaoPodeSerEditadoSeNaoEstiverCriado(@ForAll SituacaoProcesso situacao) {
        // Invariante: Se situacao != CRIADO, atualizar() deve lançar ErroProcessoEmSituacaoInvalida
        if (situacao == CRIADO) return;

        // Setup
        Processo processoSimulado = new Processo();
        processoSimulado.setSituacao(situacao);

        // Mock do repositório
        // Nota: Como JQwik executa muitas vezes, precisamos garantir que o mock seja resetado ou configurado a cada execução.
        // O @ExtendWith(MockitoExtension.class) lida com o ciclo de vida do JUnit, mas o @Property roda multiplas vezes dentro de um @Test (ou metodo de teste).
        // JQwik não injeta automaticamente mocks a cada sample.
        // Solução: Configurar o mock DENTRO do método da propriedade.
        ProcessoRepo mockRepo = org.mockito.Mockito.mock(ProcessoRepo.class);
        ProcessoService service = new ProcessoService(mockRepo, null, null, null, null, null, null, null, null, null);

        when(mockRepo.findById(anyLong())).thenReturn(Optional.of(processoSimulado));

        AtualizarProcessoReq req = new AtualizarProcessoReq(); // Request vazio é suficiente para cair na validação de estado

        // Act & Assert
        try {
            service.atualizar(1L, req);
            throw new AssertionError("Deveria ter lançado exceção para situação: " + situacao);
        } catch (ErroProcessoEmSituacaoInvalida e) {
            // Sucesso esperado
        } catch (Exception e) {
            // Outras exceções (NullPointer por dependencias null) significam que passou da validação de estado
            // O que seria uma falha do teste, pois deveria ter bloqueado antes.
            // Mas cuidado: se validar input antes do estado, pode falhar.
            // No código: findById -> validaSituacao -> validaReq.
            // Então se chegar aqui, é porque passou da validação.
             throw new AssertionError("Falhou com exceção inesperada: " + e.getClass().getSimpleName(), e);
        }
    }
}
