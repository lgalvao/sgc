package sgc.organizacao.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.mapa.model.Mapa;
import sgc.organizacao.model.UnidadeMapa;
import sgc.organizacao.model.UnidadeMapaRepo;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UnidadeMapaServiceTest {

    @Mock
    private UnidadeMapaRepo unidadeMapaRepo;

    @InjectMocks
    private UnidadeMapaService unidadeMapaService;

    @Test
    @DisplayName("Deve verificar se existe mapa vigente")
    void deveVerificarMapaVigente() {
        Long codigoUnidade = 1L;
        when(unidadeMapaRepo.existsById(codigoUnidade)).thenReturn(true);

        boolean result = unidadeMapaService.verificarMapaVigente(codigoUnidade);

        assertTrue(result);
        verify(unidadeMapaRepo).existsById(codigoUnidade);
    }

    @Test
    @DisplayName("Deve definir mapa vigente criando novo registro se não existir")
    void deveDefinirMapaVigente() {
        Long codigoUnidade = 1L;
        Mapa mapa = new Mapa();
        when(unidadeMapaRepo.findById(codigoUnidade)).thenReturn(Optional.empty());

        unidadeMapaService.definirMapaVigente(codigoUnidade, mapa);

        verify(unidadeMapaRepo).save(any(UnidadeMapa.class));
    }

    @Test
    @DisplayName("Deve atualizar mapa vigente se já existir")
    void deveAtualizarMapaVigente() {
        Long codigoUnidade = 1L;
        Mapa mapa = new Mapa();
        UnidadeMapa unidadeMapaExistente = new UnidadeMapa();
        when(unidadeMapaRepo.findById(codigoUnidade)).thenReturn(Optional.of(unidadeMapaExistente));

        unidadeMapaService.definirMapaVigente(codigoUnidade, mapa);

        verify(unidadeMapaRepo).save(unidadeMapaExistente);
    }

    @Test
    @DisplayName("Deve buscar todos os códigos de unidades que possuem mapa vigente")
    void deveBuscarTodosCodigosUnidades() {
        unidadeMapaService.buscarTodosCodigosUnidades();
        verify(unidadeMapaRepo).findAllUnidadeCodigos();
    }
}
