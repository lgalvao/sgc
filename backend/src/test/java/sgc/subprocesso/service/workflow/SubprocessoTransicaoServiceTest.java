package sgc.subprocesso.service.workflow;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.subprocesso.eventos.EventoTransicaoSubprocesso;
import sgc.subprocesso.eventos.TipoTransicao;
import sgc.subprocesso.model.Movimentacao;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.MovimentacaoRepo;
import sgc.subprocesso.service.SubprocessoRepositoryService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("Testes Unitários para SubprocessoTransicaoService")
class SubprocessoTransicaoServiceTest {

    @Mock
    private MovimentacaoRepo movimentacaoRepo;

    @Mock
    private SubprocessoRepositoryService subprocessoService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private SubprocessoTransicaoService service;

    @Test
    @DisplayName("Deve registrar transição com observações e publicar evento")
    void deveRegistrarComObservacoes() {
        // Arrange
        Subprocesso subprocesso = mock(Subprocesso.class);
        Unidade origem = mock(Unidade.class);
        Unidade destino = mock(Unidade.class);
        Usuario usuario = new Usuario();

        // Act
        service.registrar(
                subprocesso,
                TipoTransicao.CADASTRO_DISPONIBILIZADO,
                origem,
                destino,
                usuario,
                "OBS"
        );

        // Assert
        verify(movimentacaoRepo).save(any(Movimentacao.class));
        verify(eventPublisher).publishEvent(any(EventoTransicaoSubprocesso.class));
    }

    @Test
    @DisplayName("Deve registrar transição sem observações (sobrecarga)")
    void deveRegistrarSemObservacoes() {
        // Arrange
        Subprocesso subprocesso = mock(Subprocesso.class);
        Unidade origem = mock(Unidade.class);
        Unidade destino = mock(Unidade.class);
        Usuario usuario = new Usuario();

        // Act
        service.registrar(
                subprocesso,
                TipoTransicao.CADASTRO_DEVOLVIDO,
                origem,
                destino,
                usuario
        );

        // Assert
        verify(movimentacaoRepo).save(any(Movimentacao.class));
        verify(eventPublisher).publishEvent(any(EventoTransicaoSubprocesso.class));
    }

    @Test
    @DisplayName("Deve lidar com unidades nulas no log")
    void deveLidarComUnidadesNulas() {
        // Arrange
        Subprocesso subprocesso = mock(Subprocesso.class);
        Usuario usuario = new Usuario();

        // Act
        service.registrar(
                subprocesso,
                TipoTransicao.CADASTRO_DISPONIBILIZADO,
                null,
                null,
                usuario,
                null
        );

        // Assert
        verify(movimentacaoRepo).save(any(Movimentacao.class));
        verify(eventPublisher).publishEvent(any(EventoTransicaoSubprocesso.class));
    }
}
