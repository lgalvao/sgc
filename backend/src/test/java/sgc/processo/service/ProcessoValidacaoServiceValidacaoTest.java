package sgc.processo.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import sgc.comum.erros.*;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;
import sgc.processo.model.*;
import sgc.subprocesso.service.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProcessoValidacaoService - Validações")
class ProcessoValidacaoServiceValidacaoTest {

    @Mock
    private UnidadeService unidadeService;

    @Mock
    private SubprocessoValidacaoService validacaoService;

    @InjectMocks
    private ProcessoValidacaoService validador;

    @Test
    @DisplayName("getMensagemErroUnidadesSemMapa deve retornar vazio se lista nula ou vazia")
    void getMensagemErroUnidadesSemMapaListaVazia() {
        assertThat(validador.getMensagemErroUnidadesSemMapa(null)).isEmpty();
        assertThat(validador.getMensagemErroUnidadesSemMapa(Collections.emptyList())).isEmpty();
    }

    @Test
    @DisplayName("getMensagemErroUnidadesSemMapa deve retornar erro se unidade sem mapa")
    void getMensagemErroUnidadesSemMapaComErro() {
        Unidade u = new Unidade();
        u.setCodigo(1L);
        when(unidadeService.verificarMapaVigente(1L)).thenReturn(false);
        when(unidadeService.buscarSiglasPorIds(List.of(1L))).thenReturn(List.of("SIGLA"));

        Optional<String> msg = validador.getMensagemErroUnidadesSemMapa(List.of(1L));
        assertThat(msg).isPresent();
        assertThat(msg.get()).contains("SIGLA");
    }

    @Test
    @DisplayName("getMensagemErroUnidadesSemMapa deve retornar vazio se todas unidades possuem mapa")
    void getMensagemErroUnidadesSemMapaSucesso() {
        Unidade u = new Unidade();
        u.setCodigo(1L);
        when(unidadeService.verificarMapaVigente(1L)).thenReturn(true);

        Optional<String> msg = validador.getMensagemErroUnidadesSemMapa(List.of(1L));
        assertThat(msg).isEmpty();
    }

    @Test
    @DisplayName("validarFinalizacaoProcesso sucesso")
    void validarFinalizacaoProcessoSucesso() {
        Processo p = new Processo();
        p.setCodigo(1L);
        p.setSituacao(SituacaoProcesso.EM_ANDAMENTO);

        when(validacaoService.validarSubprocessosParaFinalizacao(1L))
                .thenReturn(SubprocessoValidacaoService.ValidationResult.ofValido());

        Assertions.assertDoesNotThrow(() -> validador.validarFinalizacaoProcesso(p));
        verify(validacaoService).validarSubprocessosParaFinalizacao(1L);
    }

    @Test
    @DisplayName("validarFinalizacaoProcesso falha se situação inválida")
    void validarFinalizacaoProcessoSituacaoInvalida() {
        Processo p = new Processo();
        p.setSituacao(SituacaoProcesso.CRIADO);

        assertThatThrownBy(() -> validador.validarFinalizacaoProcesso(p))
                .isInstanceOf(ErroValidacao.class)
                .hasMessageContaining("EM ANDAMENTO");
    }

    @Test
    @DisplayName("validarTodosSubprocessosHomologados sucesso")
    void validarTodosSubprocessosHomologadosSucesso() {
        Processo p = new Processo();
        p.setCodigo(1L);

        when(validacaoService.validarSubprocessosParaFinalizacao(1L))
                .thenReturn(SubprocessoValidacaoService.ValidationResult.ofValido());

        // Should not throw exception
        Assertions.assertDoesNotThrow(() -> validador.validarTodosSubprocessosHomologados(p));
    }

    @Test
    @DisplayName("validarTodosSubprocessosHomologados deve lançar erro se algum não homologado")
    void validarTodosSubprocessosHomologadosErro() {
        Processo p = new Processo();
        p.setCodigo(1L);

        when(validacaoService.validarSubprocessosParaFinalizacao(1L))
                .thenReturn(SubprocessoValidacaoService.ValidationResult.ofInvalido("Erro de validação"));

        assertThatThrownBy(() -> validador.validarTodosSubprocessosHomologados(p))
                .isInstanceOf(ErroValidacao.class)
                .hasMessage("Erro de validação");
    }

    @Test
    @DisplayName("validarTiposUnidades deve retornar vazio se lista vazia")
    void validarTiposUnidadesVazio() {
        assertThat(validador.validarTiposUnidades(Collections.emptyList())).isEmpty();
    }

    @Test
    @DisplayName("validarTiposUnidades deve retornar erro para unidade INTERMEDIARIA")
    void validarTiposUnidadesErro() {
        Unidade u1 = new Unidade();
        u1.setTipo(TipoUnidade.INTERMEDIARIA);
        u1.setSigla("U1");

        Unidade u2 = new Unidade();
        u2.setTipo(TipoUnidade.OPERACIONAL);
        u2.setSigla("U2");

        Optional<String> msg = validador.validarTiposUnidades(List.of(u1, u2));
        assertThat(msg).isPresent()
                .get().asString().contains("INTERMEDIARIA").contains("U1");
    }

    @Test
    @DisplayName("validarTiposUnidades deve retornar vazio se todas validas")
    void validarTiposUnidadesSucesso() {
        Unidade u = new Unidade();
        u.setTipo(TipoUnidade.OPERACIONAL);
        assertThat(validador.validarTiposUnidades(List.of(u))).isEmpty();
    }
}
