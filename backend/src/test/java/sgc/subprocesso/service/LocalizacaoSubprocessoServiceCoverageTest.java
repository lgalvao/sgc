package sgc.subprocesso.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import sgc.subprocesso.model.*;
import sgc.organizacao.model.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LocalizacaoSubprocessoService - Cobertura de Testes")
class LocalizacaoSubprocessoServiceCoverageTest {

    @InjectMocks
    private LocalizacaoSubprocessoService target;

    @Mock
    private MovimentacaoRepo movimentacaoRepo;

    @Test
    @DisplayName("obterLocalizacoesAtuais - deve cobrir merge function com duplicatas")
    void obterLocalizacoesAtuais_Duplicatas() {
        Subprocesso sp1 = new Subprocesso(); sp1.setCodigo(1L);
        Unidade u1 = new Unidade(); u1.setCodigo(10L);
        Unidade u2 = new Unidade(); u2.setCodigo(20L);
        
        Movimentacao m1 = mock(Movimentacao.class);
        when(m1.getSubprocesso()).thenReturn(sp1);
        when(m1.getUnidadeDestino()).thenReturn(u1);
        
        Movimentacao m2 = mock(Movimentacao.class);
        when(m2.getSubprocesso()).thenReturn(sp1);
        when(m2.getUnidadeDestino()).thenReturn(u2);

        when(movimentacaoRepo.listarUltimasPorSubprocessos(anyList())).thenReturn(List.of(m1, m2));

        Map<Long, Unidade> result = target.obterLocalizacoesAtuais(List.of(sp1));

        assertThat(result).hasSize(1);
        assertThat(result.get(1L)).isEqualTo(u1);
    }
}
