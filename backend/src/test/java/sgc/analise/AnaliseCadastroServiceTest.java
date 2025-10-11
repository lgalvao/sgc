package sgc.analise;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.analise.modelo.AnaliseCadastro;
import sgc.analise.modelo.AnaliseCadastroRepo;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.subprocesso.modelo.SubprocessoRepo;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para {@link AnaliseCadastroService}.
 * Cobrem criar, listar e remover análises de cadastro.
 */
@ExtendWith(MockitoExtension.class)
public class AnaliseCadastroServiceTest {

    @Mock
    private AnaliseCadastroRepo analiseCadastroRepo;

    @Mock
    private SubprocessoRepo subprocessoRepo;

    @InjectMocks
    private AnaliseCadastroService service;

    @Test
    void criarAnalise_persisteAnaliseQuandoSubprocessoExiste() {
        Long spId = 1L;
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(spId);

        when(subprocessoRepo.findById(spId)).thenReturn(Optional.of(sp));

        ArgumentCaptor<AnaliseCadastro> captor = ArgumentCaptor.forClass(AnaliseCadastro.class);
        when(analiseCadastroRepo.save(any(AnaliseCadastro.class))).thenAnswer(inv -> {
            AnaliseCadastro a = inv.getArgument(0);
            a.setCodigo(10L);
            return a;
        });

        AnaliseCadastro criado = service.criarAnalise(spId, "Observações de teste");

        verify(analiseCadastroRepo, times(1)).save(captor.capture());
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

        when(subprocessoRepo.findById(spId)).thenReturn(Optional.of(new Subprocesso()));
        when(analiseCadastroRepo.findBySubprocessoCodigoOrderByDataHoraDesc(spId)).thenReturn(List.of(a1, a2));

        List<AnaliseCadastro> lista = service.listarPorSubprocesso(spId);
        assertThat(lista).hasSize(2).extracting(AnaliseCadastro::getCodigo).containsExactlyInAnyOrder(101L, 102L);
    }

    @Test
    void removerPorSubprocesso_chamaRepositorio() {
        Long spId = 3L;
        doNothing().when(analiseCadastroRepo).deleteBySubprocessoCodigo(spId);

        service.removerPorSubprocesso(spId);

        verify(analiseCadastroRepo, times(1)).deleteBySubprocessoCodigo(spId);
    }
}