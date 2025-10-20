package sgc.mapa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.comum.erros.ErroDominioNaoEncontrado;
import sgc.mapa.modelo.Mapa;
import sgc.mapa.modelo.MapaRepo;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MapaCrudServiceTest {

    @InjectMocks
    private MapaCrudService service;

    @Mock
    private MapaRepo mapaRepo;

    private Mapa mapa;

    @BeforeEach
    void setUp() {
        mapa = new Mapa();
    }

    @Nested
    @DisplayName("Testes para criar mapa")
    class CriarMapaTests {
        @Test
        @DisplayName("Deve criar mapa com sucesso")
        void criar_Sucesso() {
            when(mapaRepo.save(any(Mapa.class))).thenReturn(mapa);
            service.criar(mapa);
            verify(mapaRepo).save(mapa);
        }
    }

    @Nested
    @DisplayName("Testes para atualizar mapa")
    class AtualizarMapaTests {
        @Test
        @DisplayName("Deve atualizar mapa com sucesso")
        void atualizar_Sucesso() {
            when(mapaRepo.findById(1L)).thenReturn(Optional.of(mapa));
            when(mapaRepo.save(any(Mapa.class))).thenReturn(mapa);
            service.atualizar(1L, mapa);
            verify(mapaRepo).save(mapa);
        }

        @Test
        @DisplayName("Deve lançar exceção ao atualizar mapa inexistente")
        void atualizar_Inexistente_LancaExcecao() {
            when(mapaRepo.findById(1L)).thenReturn(Optional.empty());
            assertThrows(ErroDominioNaoEncontrado.class, () -> service.atualizar(1L, mapa));
        }
    }

    @Nested
    @DisplayName("Testes para excluir mapa")
    class ExcluirMapaTests {
        @Test
        @DisplayName("Deve excluir mapa com sucesso")
        void excluir_Sucesso() {
            when(mapaRepo.existsById(1L)).thenReturn(true);
            doNothing().when(mapaRepo).deleteById(1L);
            service.excluir(1L);
            verify(mapaRepo).deleteById(1L);
        }

        @Test
        @DisplayName("Deve lançar exceção ao excluir mapa inexistente")
        void excluir_Inexistente_LancaExcecao() {
            when(mapaRepo.existsById(1L)).thenReturn(false);
            assertThrows(ErroDominioNaoEncontrado.class, () -> service.excluir(1L));
        }
    }
}
