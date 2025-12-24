package sgc.processo.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import sgc.processo.api.IniciarProcessoReq;
import sgc.processo.api.ProcessoDto;
import sgc.processo.internal.model.TipoProcesso;
import sgc.processo.internal.service.ProcessoService;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class ProcessoControllerTest {

    @Mock
    private ProcessoService processoService;

    @InjectMocks
    private ProcessoController controller;

    @Nested
    @DisplayName("Iniciar")
    class IniciarTest {

        @Test
        @DisplayName("Deve iniciar processo de revisão com sucesso")
        void deveIniciarProcessoRevisao() {
            // Arrange
            Long codigo = 1L;
            List<Long> unidades = List.of(10L, 20L);
            IniciarProcessoReq req = new IniciarProcessoReq(TipoProcesso.REVISAO, unidades);

            when(processoService.iniciarProcessoRevisao(codigo, unidades)).thenReturn(Collections.emptyList());
            ProcessoDto dto = ProcessoDto.builder()
                    .codigo(codigo)
                    .descricao("Revisão")
                    .tipo("REVISAO")
                    .build();
            when(processoService.obterPorId(codigo)).thenReturn(java.util.Optional.of(dto));

            // Act
            ResponseEntity<?> response = controller.iniciar(codigo, req);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(dto, response.getBody());
        }

        @Test
        @DisplayName("Deve retornar erro se houver falhas na inicialização de revisão")
        void deveRetornarErroNaRevisao() {
            // Arrange
            Long codigo = 1L;
            List<Long> unidades = List.of(10L);
            IniciarProcessoReq req = new IniciarProcessoReq(TipoProcesso.REVISAO, unidades);

            when(processoService.iniciarProcessoRevisao(codigo, unidades)).thenReturn(List.of("Erro 1"));

            // Act
            ResponseEntity<?> response = controller.iniciar(codigo, req);

            // Assert
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            @SuppressWarnings("unchecked")
            Map<String, List<String>> body = (Map<String, List<String>>) response.getBody();
            assertEquals(List.of("Erro 1"), body.get("erros"));
        }

        @Test
        @DisplayName("Deve iniciar processo de mapeamento com sucesso")
        void deveIniciarProcessoMapeamento() {
            // Arrange
            Long codigo = 2L;
            List<Long> unidades = List.of(30L);
            IniciarProcessoReq req = new IniciarProcessoReq(TipoProcesso.MAPEAMENTO, unidades);

            when(processoService.iniciarProcessoMapeamento(codigo, unidades)).thenReturn(Collections.emptyList());
            ProcessoDto dto = ProcessoDto.builder()
                    .codigo(codigo)
                    .descricao("Mapeamento")
                    .tipo("MAPEAMENTO")
                    .build();
            when(processoService.obterPorId(codigo)).thenReturn(java.util.Optional.of(dto));

            // Act
            ResponseEntity<?> response = controller.iniciar(codigo, req);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(dto, response.getBody());
        }

        @Test
        @DisplayName("Deve iniciar processo de diagnóstico com sucesso")
        void deveIniciarProcessoDiagnostico() {
            // Arrange
            Long codigo = 3L;
            List<Long> unidades = List.of(40L);
            IniciarProcessoReq req = new IniciarProcessoReq(TipoProcesso.DIAGNOSTICO, unidades);

            when(processoService.iniciarProcessoDiagnostico(codigo, unidades)).thenReturn(Collections.emptyList());
            ProcessoDto dto = ProcessoDto.builder()
                    .codigo(codigo)
                    .descricao("Diagnóstico")
                    .tipo("DIAGNOSTICO")
                    .build();
            when(processoService.obterPorId(codigo)).thenReturn(java.util.Optional.of(dto));

            // Act
            ResponseEntity<?> response = controller.iniciar(codigo, req);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(dto, response.getBody());
        }

        @Test
        @DisplayName("Deve retornar bad request para tipo desconhecido (embora enum restrinja)")
        void deveRetornarBadRequestParaTipoDesconhecido() {
            // Este caso é difícil de alcançar com Enum mas pode ocorrer se null for passado e não validado antes
            // Mas o código no controller faz ifs explicitos. Se eu conseguir passar um req com tipo null...

            // Arrange
            Long codigo = 4L;
            IniciarProcessoReq req = new IniciarProcessoReq(null, List.of());

            // Act
            ResponseEntity<?> response = controller.iniciar(codigo, req);

            // Assert
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }
    }

    @Nested
    @DisplayName("Finalizar")
    class FinalizarTest {

        @Test
        @DisplayName("Deve finalizar processo com sucesso")
        void deveFinalizarProcesso() {
            // Arrange
            Long codigo = 1L;

            // Act
            ResponseEntity<?> response = controller.finalizar(codigo);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(processoService).finalizar(codigo);
        }
    }
}
