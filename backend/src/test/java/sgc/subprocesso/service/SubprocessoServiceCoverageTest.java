package sgc.subprocesso.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import sgc.mapa.model.*;
import sgc.organizacao.service.*;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.model.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubprocessoService - Cobertura de Testes")
class SubprocessoServiceCoverageTest {

    @InjectMocks
    private SubprocessoService target;

    @Mock
    private SubprocessoRepo subprocessoRepo;

    @Mock
    private SubprocessoConsultaService consultaService;

    @Mock
    private UnidadeService unidadeService;

    @Test
    @DisplayName("atualizarEntidade - deve usar comandos vazios quando nulos")
    void atualizarEntidade_ComandosNulos() {
        Long cod = 1L;
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(cod);
        when(consultaService.buscarSubprocesso(cod)).thenReturn(sp);
        when(subprocessoRepo.save(any())).thenReturn(sp);

        AtualizarSubprocessoCommand command = AtualizarSubprocessoCommand.builder()
                .vinculos(null)
                .prazos(null)
                .build();

        target.atualizarEntidade(cod, command);

        verify(subprocessoRepo).save(sp);
    }

    @Test
    @DisplayName("obterMapaObrigatorio - deve lancar erro quando mapa for nulo")
    void obterMapaObrigatorio_MapaNulo() {
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(1L);
        sp.setMapa(null);

        assertThatThrownBy(() -> org.springframework.test.util.ReflectionTestUtils.invokeMethod(target, "obterMapaObrigatorio", sp))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sem mapa associado");
    }

    @Test
    @DisplayName("obterCodigoMapaObrigatorio - deve lancar erro quando codigo do mapa for nulo")
    void obterCodigoMapaObrigatorio_CodigoNulo() {
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(1L);
        Mapa m = new Mapa();
        m.setCodigo(null);
        sp.setMapa(m);

        assertThatThrownBy(() -> org.springframework.test.util.ReflectionTestUtils.invokeMethod(target, "obterCodigoMapaObrigatorio", sp))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("com mapa sem código associado");
    }
}
