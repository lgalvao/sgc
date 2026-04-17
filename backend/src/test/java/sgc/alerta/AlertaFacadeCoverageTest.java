package sgc.alerta;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import sgc.alerta.model.*;
import sgc.organizacao.*;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AlertaFacade - Cobertura de Testes")
class AlertaFacadeCoverageTest {

    @InjectMocks
    private AlertaFacade target;

    @Mock
    private AlertaService alertaService;
    @Mock
    private UsuarioService usuarioService;

    @Test
    @DisplayName("obterMapaDataHoraLeitura - deve cobrir merge function com duplicatas")
    void obterMapaDataHoraLeitura_Duplicatas() {
        String usuario = "U1";
        Long codAlerta = 100L;
        
        AlertaUsuario au1 = mock(AlertaUsuario.class);
        AlertaUsuario.Chave chave = mock(AlertaUsuario.Chave.class);
        when(chave.getAlertaCodigo()).thenReturn(codAlerta);
        when(au1.getCodigo()).thenReturn(chave);
        when(au1.getDataHoraLeitura()).thenReturn(LocalDateTime.now());
        
        AlertaUsuario au2 = mock(AlertaUsuario.class);
        when(au2.getCodigo()).thenReturn(chave);
        when(au2.getDataHoraLeitura()).thenReturn(LocalDateTime.now().plusHours(1));

        when(alertaService.alertasUsuarios(usuario, List.of(codAlerta))).thenReturn(List.of(au1, au2));

        Map<Long, LocalDateTime> result = target.obterMapaDataHoraLeitura(usuario, List.of(codAlerta));
        assertThat(result).containsEntry(codAlerta, au1.getDataHoraLeitura());
    }

    @Test
    @DisplayName("marcarComoLidos - deve cobrir merge function com duplicatas e alertas ausentes")
    void marcarComoLidos_Duplicatas() {
        ContextoUsuarioAutenticado contexto = mock(ContextoUsuarioAutenticado.class);
        when(contexto.usuarioTitulo()).thenReturn("U1");
        
        Long cod1 = 1L;
        Long cod2 = 2L;
        
        // Simular duplicata em alertasUsuarios
        AlertaUsuario au1 = mock(AlertaUsuario.class);
        AlertaUsuario.Chave chave1 = mock(AlertaUsuario.Chave.class);
        when(chave1.getAlertaCodigo()).thenReturn(cod1);
        when(au1.getCodigo()).thenReturn(chave1);
        
        AlertaUsuario au1Dup = mock(AlertaUsuario.class);
        when(au1Dup.getCodigo()).thenReturn(chave1);

        when(alertaService.alertasUsuarios(anyString(), anyList())).thenReturn(List.of(au1, au1Dup));
        
        // Alerta 2 está ausente em existentes, vai para listarPorCodigos
        Alerta a2 = new Alerta(); a2.setCodigo(cod2);
        Alerta a2Dup = new Alerta(); a2Dup.setCodigo(cod2); // Duplicata proposital

        when(alertaService.listarPorCodigos(anyList())).thenReturn(List.of(a2, a2Dup));
        when(usuarioService.buscar("U1")).thenReturn(new Usuario());

        target.marcarComoLidos(contexto, List.of(cod1, cod2));

        verify(alertaService).salvarAlertasUsuarios(anyList());
    }
}
