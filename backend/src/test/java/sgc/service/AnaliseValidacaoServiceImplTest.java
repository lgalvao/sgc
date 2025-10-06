package sgc.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import sgc.atividade.AnaliseValidacao;
import sgc.atividade.AnaliseValidacaoServiceImpl;
import sgc.subprocesso.AnaliseValidacaoRepository;
import sgc.subprocesso.Subprocesso;
import sgc.subprocesso.SubprocessoRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para {@link AnaliseValidacaoServiceImpl}.
 * Cobrem criar, listar e remover análises de validação.
 */
public class AnaliseValidacaoServiceImplTest {

    private AnaliseValidacaoRepository analiseValidacaoRepository;
    private SubprocessoRepository subprocessoRepository;
    private AnaliseValidacaoServiceImpl service;

    @BeforeEach
    void setup() {
        analiseValidacaoRepository = mock(AnaliseValidacaoRepository.class);
        subprocessoRepository = mock(SubprocessoRepository.class);
        service = new AnaliseValidacaoServiceImpl(analiseValidacaoRepository, subprocessoRepository);
    }

    @Test
    void criarAnalise_persisteAnaliseQuandoSubprocessoExiste() {
        Long spId = 1L;
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(spId);

        when(subprocessoRepository.findById(spId)).thenReturn(Optional.of(sp));

        when(analiseValidacaoRepository.save(any(AnaliseValidacao.class))).thenAnswer(inv -> {
            AnaliseValidacao a = inv.getArgument(0);
            a.setCodigo(10L);
            return a;
        });

        AnaliseValidacao criado = service.criarAnalise(spId, "Observações validacao");

        ArgumentCaptor<AnaliseValidacao> captor = ArgumentCaptor.forClass(AnaliseValidacao.class);
        verify(analiseValidacaoRepository).save(captor.capture());
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

        when(subprocessoRepository.findById(spId)).thenReturn(Optional.of(new Subprocesso()));
        when(analiseValidacaoRepository.findBySubprocessoCodigo(spId)).thenReturn(List.of(a1, a2));

        List<AnaliseValidacao> lista = service.listarPorSubprocesso(spId);
        assertThat(lista).hasSize(2).extracting(AnaliseValidacao::getCodigo).containsExactlyInAnyOrder(201L, 202L);
    }

    @Test
    void removerPorSubprocesso_chamaRepositorio() {
        Long spId = 3L;
        doNothing().when(analiseValidacaoRepository).deleteBySubprocessoCodigo(spId);

        service.removerPorSubprocesso(spId);

        verify(analiseValidacaoRepository, times(1)).deleteBySubprocessoCodigo(spId);
    }
}