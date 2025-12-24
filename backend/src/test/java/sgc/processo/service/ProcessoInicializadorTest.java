package sgc.processo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import sgc.processo.internal.model.Processo;
import sgc.processo.internal.model.ProcessoRepo;
import sgc.processo.internal.model.SituacaoProcesso;
import sgc.processo.internal.model.TipoProcesso;
import sgc.processo.internal.service.ProcessoInicializador;
import sgc.subprocesso.internal.service.SubprocessoFactory;
import sgc.unidade.internal.model.UnidadeRepo;
import sgc.unidade.internal.model.UnidadeMapaRepo;

@ExtendWith(MockitoExtension.class)
class ProcessoInicializadorTest {

    @Mock private ProcessoRepo processoRepo;
    @Mock private UnidadeRepo unidadeRepo;
    @Mock private UnidadeMapaRepo unidadeMapaRepo;
    @Mock private ApplicationEventPublisher publicadorEventos;
    @Mock private SubprocessoFactory subprocessoFactory;

    @InjectMocks private ProcessoInicializador inicializador;

    @Nested
    @DisplayName("Validação de unidades")
    class ValidacaoUnidadesTest {

        @Test
        @DisplayName("Deve retornar erro se unidade já está em processo ativo")
        void deveRetornarErroSeUnidadeEmProcessoAtivo() {
            // Arrange
            Long codigo = 1L;
            List<Long> unidades = List.of(10L);

            Processo processo = mock(Processo.class);
            when(processoRepo.findById(codigo)).thenReturn(Optional.of(processo));
            when(processo.getSituacao()).thenReturn(SituacaoProcesso.CRIADO);
            when(processo.getTipo()).thenReturn(TipoProcesso.REVISAO);

            sgc.unidade.internal.model.Unidade u10 = mock(sgc.unidade.internal.model.Unidade.class);
            when(u10.getCodigo()).thenReturn(10L);
            when(unidadeRepo.findAllById(unidades)).thenReturn(List.of(u10));
            when(unidadeMapaRepo.existsById(10L)).thenReturn(true); // Tem mapa, ok para revisão

            when(processoRepo.findUnidadeCodigosBySituacaoAndUnidadeCodigosIn(SituacaoProcesso.EM_ANDAMENTO, unidades))
                .thenReturn(List.of(10L));
            when(unidadeRepo.findSiglasByCodigos(List.of(10L))).thenReturn(List.of("U10"));

            // Act
            List<String> erros = inicializador.iniciar(codigo, unidades);

            // Assert
            assertEquals(1, erros.size());
            assertTrue(erros.get(0).contains("U10"));
        }

        @Test
        @DisplayName("Deve validar corretamente unidades sem mapa para revisão")
        void deveValidarUnidadesSemMapa() {
             // Arrange
            Long codigo = 1L;
            List<Long> unidades = List.of(10L);

            Processo processo = mock(Processo.class);
            when(processoRepo.findById(codigo)).thenReturn(Optional.of(processo));
            when(processo.getSituacao()).thenReturn(SituacaoProcesso.CRIADO);
            when(processo.getTipo()).thenReturn(TipoProcesso.REVISAO);

            sgc.unidade.internal.model.Unidade u10 = mock(sgc.unidade.internal.model.Unidade.class);
            when(u10.getCodigo()).thenReturn(10L);
            when(unidadeRepo.findAllById(unidades)).thenReturn(List.of(u10));
            when(unidadeMapaRepo.existsById(10L)).thenReturn(false); // Não tem mapa
            when(unidadeRepo.findSiglasByCodigos(List.of(10L))).thenReturn(List.of("U10"));

            // Act
            List<String> erros = inicializador.iniciar(codigo, unidades);

            // Assert
            assertEquals(1, erros.size());
            assertTrue(erros.get(0).contains("U10"));
        }
    }
}
