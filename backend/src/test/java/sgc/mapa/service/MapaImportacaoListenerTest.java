package sgc.mapa.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.mapa.eventos.EventoImportacaoAtividades;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("MapaImportacaoListener Suite")
class MapaImportacaoListenerTest {

    @InjectMocks
    private MapaImportacaoListener target;

    @Mock
    private CopiaMapaService copiaMapaService;

    @Test
    @DisplayName("Deve processar evento de importação de atividades")
    void deveProcessarEventoImportacao() {
        // Arrange
        EventoImportacaoAtividades evento = EventoImportacaoAtividades.builder()
                .codigoMapaOrigem(1L)
                .codigoMapaDestino(2L)
                .codigoSubprocesso(3L)
                .build();

        // Act
        target.aoImportarAtividades(evento);

        // Assert
        verify(copiaMapaService).importarAtividadesDeOutroMapa(1L, 2L);
    }
}
