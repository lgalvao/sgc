package sgc.subprocesso.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import sgc.subprocesso.model.*;
import sgc.comum.erros.*;
import sgc.mapa.model.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubprocessoValidacaoService - Cobertura adicional")
class SubprocessoValidacaoServiceCoverageTest {

    @InjectMocks
    private SubprocessoValidacaoService subprocessoValidacaoService;

    @Test
    @DisplayName("validarSituacaoMinima deve lancar excecao se situacao nula")
    void validarSituacaoMinimaDeveLancarExcecaoSeNula() {
        Subprocesso sp = new Subprocesso();
        sp.setSituacaoForcada(null);

        assertThatIllegalArgumentException()
            .isThrownBy(() -> subprocessoValidacaoService.validarSituacaoMinima(sp, SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO, "msg"))
            .withMessageContaining("não pode ser nula");
    }

    @Test
    @DisplayName("validarSituacaoPermitida deve lançar exceção se situacao atual for nula")
    void deveLancarExcecaoSeSituacaoNula() {
        Subprocesso sp = new Subprocesso();
        sp.setSituacaoForcada(null);

        assertThatIllegalArgumentException()
            .isThrownBy(() -> subprocessoValidacaoService.validarSituacaoPermitida(sp, SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO))
            .withMessageContaining("não pode ser nula");
    }

    @Test
    @DisplayName("validarSituacaoPermitida deve lançar exceção se lista de permitidas for vazia")
    void deveLancarExcecaoSePermitidasVazia() {
        Subprocesso sp = new Subprocesso();
        sp.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);

        assertThatIllegalArgumentException()
            .isThrownBy(() -> subprocessoValidacaoService.validarSituacaoPermitida(sp))
            .withMessageContaining("Pelo menos uma situação permitida");
    }

    @Test
    @DisplayName("validarRequisitosNegocioParaDisponibilizacao deve lançar exceção com atividades sem conhecimento")
    void deveLancarExcecaoSeAtividadesSemConhecimento() {
        Subprocesso sp = new Subprocesso();
        Mapa m = new Mapa();

        Atividade a = new Atividade(); a.setDescricao("Ativ");
        Set<Atividade> set = new HashSet<>();
        set.add(a);
        m.setAtividades(set);
        sp.setMapa(m);

        Atividade a1 = new Atividade(); a1.setCodigo(1L); a1.setDescricao("Ativ1");

        assertThatThrownBy(() -> subprocessoValidacaoService.validarRequisitosNegocioParaDisponibilizacao(sp, List.of(a1)))
            .isInstanceOf(ErroValidacao.class);
    }
}
