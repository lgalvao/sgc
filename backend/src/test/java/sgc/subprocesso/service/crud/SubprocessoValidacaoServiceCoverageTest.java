package sgc.subprocesso.service.crud;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.mapa.service.MapaManutencaoService;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;

import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static sgc.subprocesso.model.SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes de Cobertura - SubprocessoValidacaoService")
class SubprocessoValidacaoServiceCoverageTest {

    @Mock
    private MapaManutencaoService mapaManutencaoService;
    @Mock
    private SubprocessoCrudService crudService;

    @InjectMocks
    private SubprocessoValidacaoService service;

    @Test
    @DisplayName("validarSituacaoPermitida(Set): deve lançar IllegalArgumentException se situacao for null")
    void validarSituacaoPermitidaSet_DeveLancarErroSeSituacaoNull() {
        Subprocesso sp = new Subprocesso();
        sp.setSituacaoForcada(null);
        Set<SituacaoSubprocesso> permitidas = Set.of(MAPEAMENTO_CADASTRO_EM_ANDAMENTO);

        assertThatThrownBy(() -> service.validarSituacaoPermitida(sp, permitidas))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Situação do subprocesso não pode ser nula");
    }

    @Test
    @DisplayName("validarSituacaoPermitida(Set): deve lançar IllegalArgumentException se conjunto permitidas for vazio")
    void validarSituacaoPermitidaSet_DeveLancarErroSePermitidasVazio() {
        Subprocesso sp = new Subprocesso();
        sp.setSituacaoForcada(MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        Set<SituacaoSubprocesso> permitidas = Collections.emptySet();

        assertThatThrownBy(() -> service.validarSituacaoPermitida(sp, permitidas))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Conjunto de situações permitidas não pode ser vazio");
    }

    @Test
    @DisplayName("validarSituacaoPermitida(Varargs): deve lançar IllegalArgumentException se permitidas for vazio")
    void validarSituacaoPermitidaVarargs_DeveLancarErroSePermitidasVazio() {
        Subprocesso sp = new Subprocesso();

        assertThatThrownBy(() -> service.validarSituacaoPermitida(sp))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Pelo menos uma situação permitida deve ser fornecida");
    }

    @Test
    @DisplayName("validarSituacaoPermitida(Msg, Varargs): deve lançar IllegalArgumentException se situacao for null")
    void validarSituacaoPermitidaMsg_DeveLancarErroSeSituacaoNull() {
        Subprocesso sp = new Subprocesso();
        sp.setSituacaoForcada(null);

        assertThatThrownBy(() -> service.validarSituacaoPermitida(sp, "Erro", MAPEAMENTO_CADASTRO_EM_ANDAMENTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Situação do subprocesso não pode ser nula");
    }

    @Test
    @DisplayName("validarSituacaoPermitida(Msg, Varargs): deve lançar IllegalArgumentException se permitidas for vazio")
    void validarSituacaoPermitidaMsg_DeveLancarErroSePermitidasVazio() {
        Subprocesso sp = new Subprocesso();
        sp.setSituacaoForcada(MAPEAMENTO_CADASTRO_EM_ANDAMENTO);

        assertThatThrownBy(() -> service.validarSituacaoPermitida(sp, "Erro"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Pelo menos uma situação permitida deve ser fornecida");
    }

    @Test
    @DisplayName("validarSituacaoMinima: deve lançar IllegalArgumentException se situacao for null")
    void validarSituacaoMinima_DeveLancarErroSeSituacaoNull() {
        Subprocesso sp = new Subprocesso();
        sp.setSituacaoForcada(null);

        assertThatThrownBy(() -> service.validarSituacaoMinima(sp, MAPEAMENTO_CADASTRO_EM_ANDAMENTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Situação do subprocesso não pode ser nula");
    }

    @Test
    @DisplayName("validarSituacaoMinima(Msg): deve lançar IllegalArgumentException se situacao for null")
    void validarSituacaoMinimaMsg_DeveLancarErroSeSituacaoNull() {
        Subprocesso sp = new Subprocesso();
        sp.setSituacaoForcada(null);

        assertThatThrownBy(() -> service.validarSituacaoMinima(sp, MAPEAMENTO_CADASTRO_EM_ANDAMENTO, "Erro"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Situação do subprocesso não pode ser nula");
    }
}
