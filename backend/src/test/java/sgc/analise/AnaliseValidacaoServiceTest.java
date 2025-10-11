package sgc.analise;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.analise.modelo.AnaliseValidacao;
import sgc.analise.modelo.AnaliseValidacaoRepo;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.subprocesso.modelo.SubprocessoRepo;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para {@link AnaliseValidacaoService}.
 * Cobrem criar, listar e remover análises de validação.
 */
@ExtendWith(MockitoExtension.class)
public class AnaliseValidacaoServiceTest {

    @Mock
    private AnaliseValidacaoRepo analiseValidacaoRepo;

    @Mock
    private SubprocessoRepo subprocessoRepo;

    @InjectMocks
    private AnaliseValidacaoService service;

    @Test
    void criarAnalise_persisteAnaliseQuandoSubprocessoExiste() {
        Long spId = 1L;
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(spId);

        when(subprocessoRepo.findById(spId)).thenReturn(Optional.of(sp));

        when(analiseValidacaoRepo.save(any(AnaliseValidacao.class))).thenAnswer(inv -> {
            AnaliseValidacao a = inv.getArgument(0);
            a.setCodigo(10L);
            return a;
        });

        AnaliseValidacao criado = service.criarAnalise(spId, "Observações validacao");

        ArgumentCaptor<AnaliseValidacao> captor = ArgumentCaptor.forClass(AnaliseValidacao.class);
        verify(analiseValidacaoRepo).save(captor.capture());
        AnaliseValidacao salvo = captor.getValue();

        assertThat(salvo.getSubprocesso()).isEqualTo(sp);
        assertThat(salvo.getObservacoes()).isEqualTo("Observações validacao");
        assertThat(criado.getCodigo()).isEqualTo(10L);
    }

    @Test
    void listarPorSubprocesso_retornaListaQuandoExistir() {
        Long spId = 2L;
        AnaliseValidacao a1 = new AnaliseValidacao();
        a1.setCodigo(201L);
        AnaliseValidacao a2 = new AnaliseValidacao();
        a2.setCodigo(202L);

        when(subprocessoRepo.findById(spId)).thenReturn(Optional.of(new Subprocesso()));
        when(analiseValidacaoRepo.findBySubprocesso_Codigo(spId)).thenReturn(List.of(a1, a2));

        List<AnaliseValidacao> lista = service.listarPorSubprocesso(spId);
        assertThat(lista).hasSize(2).extracting(AnaliseValidacao::getCodigo).containsExactlyInAnyOrder(201L, 202L);
    }

    @Test
    void removerPorSubprocesso_chamaRepositorio() {
        Long spId = 3L;
        doNothing().when(analiseValidacaoRepo).deleteBySubprocesso_Codigo(spId);

        service.removerPorSubprocesso(spId);

        verify(analiseValidacaoRepo, times(1)).deleteBySubprocesso_Codigo(spId);
    }
}