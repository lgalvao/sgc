package sgc.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import sgc.atividade.AnaliseCadastro;
import sgc.atividade.AnaliseCadastroServiceImpl;
import sgc.subprocesso.AnaliseCadastroRepository;
import sgc.subprocesso.Subprocesso;
import sgc.subprocesso.SubprocessoRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para {@link AnaliseCadastroServiceImpl}.
 * Cobrem criar, listar e remover análises de cadastro.
 */
public class AnaliseCadastroServiceImplTest {

    private AnaliseCadastroRepository analiseCadastroRepository;
    private SubprocessoRepository subprocessoRepository;
    private AnaliseCadastroServiceImpl service;

    @BeforeEach
    void setup() {
        analiseCadastroRepository = mock(AnaliseCadastroRepository.class);
        subprocessoRepository = mock(SubprocessoRepository.class);
        service = new AnaliseCadastroServiceImpl(analiseCadastroRepository, subprocessoRepository);
    }

    @Test
    void criarAnalise_persisteAnaliseQuandoSubprocessoExiste() {
        Long spId = 1L;
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(spId);

        when(subprocessoRepository.findById(spId)).thenReturn(Optional.of(sp));

        ArgumentCaptor<AnaliseCadastro> captor = ArgumentCaptor.forClass(AnaliseCadastro.class);
        when(analiseCadastroRepository.save(any(AnaliseCadastro.class))).thenAnswer(inv -> {
            AnaliseCadastro a = inv.getArgument(0);
            a.setCodigo(10L);
            return a;
        });

        AnaliseCadastro criado = service.criarAnalise(spId, "Observações de teste");

        verify(analiseCadastroRepository, times(1)).save(captor.capture());
        AnaliseCadastro salvo = captor.getValue();

        assertThat(salvo.getSubprocesso()).isEqualTo(sp);
        assertThat(salvo.getObservacoes()).isEqualTo("Observações de teste");
        assertThat(criado.getCodigo()).isEqualTo(10L);
    }

    @Test
    void listarPorSubprocesso_retornaListaQuandoExistir() {
        Long spId = 2L;
        AnaliseCadastro a1 = new AnaliseCadastro();
        a1.setCodigo(101L);
        AnaliseCadastro a2 = new AnaliseCadastro();
        a2.setCodigo(102L);

        when(subprocessoRepository.findById(spId)).thenReturn(Optional.of(new Subprocesso()));
        when(analiseCadastroRepository.findBySubprocessoCodigo(spId)).thenReturn(List.of(a1, a2));

        List<AnaliseCadastro> lista = service.listarPorSubprocesso(spId);
        assertThat(lista).hasSize(2).extracting(AnaliseCadastro::getCodigo).containsExactlyInAnyOrder(101L, 102L);
    }

    @Test
    void removerPorSubprocesso_chamaRepositorio() {
        Long spId = 3L;
        doNothing().when(analiseCadastroRepository).deleteBySubprocessoCodigo(spId);

        service.removerPorSubprocesso(spId);

        verify(analiseCadastroRepository, times(1)).deleteBySubprocessoCodigo(spId);
    }
}