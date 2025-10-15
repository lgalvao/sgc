package sgc.analise;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.analise.modelo.Analise;
import sgc.analise.modelo.AnaliseRepo;
import sgc.analise.modelo.TipoAnalise;
import sgc.comum.erros.ErroDominioNaoEncontrado;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.subprocesso.modelo.SubprocessoRepo;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para AnaliseService")
class AnaliseServiceTest {

    private static final String OBSERVACAO = "Observação";
    @Mock
    private AnaliseRepo analiseRepo;

    @Mock
    private SubprocessoRepo subprocessoRepo;

    @InjectMocks
    private AnaliseService service;

    private Subprocesso subprocesso;

    @BeforeEach
    void setUp() {
        subprocesso = new Subprocesso();
        subprocesso.setCodigo(1L);
    }

    @Nested
    @DisplayName("Testes para listarPorSubprocesso")
    class ListarPorSubprocesso {

        @Test
        @DisplayName("Deve retornar lista de análises de cadastro")
        void deveRetornarListaDeAnalisesCadastro() {
            when(subprocessoRepo.findById(1L)).thenReturn(Optional.of(subprocesso));
            Analise analise = new Analise();
            analise.setTipo(TipoAnalise.CADASTRO);
            when(analiseRepo.findBySubprocessoCodigoOrderByDataHoraDesc(1L)).thenReturn(List.of(analise));

            List<Analise> resultado = service.listarPorSubprocesso(1L, TipoAnalise.CADASTRO);

            assertFalse(resultado.isEmpty());
            assertEquals(1, resultado.size());
            assertEquals(TipoAnalise.CADASTRO, resultado.getFirst().getTipo());
            verify(analiseRepo).findBySubprocessoCodigoOrderByDataHoraDesc(1L);
        }

        @Test
        @DisplayName("Deve retornar lista de análises de validação")
        void deveRetornarListaDeAnalisesValidacao() {
            when(subprocessoRepo.findById(1L)).thenReturn(Optional.of(subprocesso));
            Analise analise = new Analise();
            analise.setTipo(TipoAnalise.VALIDACAO);
            when(analiseRepo.findBySubprocessoCodigoOrderByDataHoraDesc(1L)).thenReturn(List.of(analise));

            List<Analise> resultado = service.listarPorSubprocesso(1L, TipoAnalise.VALIDACAO);

            assertFalse(resultado.isEmpty());
            assertEquals(1, resultado.size());
            assertEquals(TipoAnalise.VALIDACAO, resultado.getFirst().getTipo());
            verify(analiseRepo).findBySubprocessoCodigoOrderByDataHoraDesc(1L);
        }

        @Test
        @DisplayName("Deve lançar exceção se o subprocesso não for encontrado")
        void deveLancarExcecaoSeSubprocessoNaoEncontrado() {
            when(subprocessoRepo.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ErroDominioNaoEncontrado.class, () -> service.listarPorSubprocesso(99L, TipoAnalise.CADASTRO));
        }
    }

    @Nested
    @DisplayName("Testes para criarAnalise")
    class CriarAnalise {

        @Test
        @DisplayName("Deve criar uma análise de cadastro")
        void deveCriarAnaliseCadastro() {
            when(subprocessoRepo.findById(1L)).thenReturn(Optional.of(subprocesso));
            when(analiseRepo.save(any(Analise.class))).thenAnswer(i -> i.getArgument(0));

            Analise resultado = service.criarAnalise(1L, OBSERVACAO, TipoAnalise.CADASTRO, null, null, null, null);

            assertNotNull(resultado);
            assertEquals(subprocesso, resultado.getSubprocesso());
            assertEquals(OBSERVACAO, resultado.getObservacoes());
            assertEquals(TipoAnalise.CADASTRO, resultado.getTipo());
            verify(analiseRepo).save(any(Analise.class));
        }

        @Test
        @DisplayName("Deve criar uma análise de validação")
        void deveCriarAnaliseValidacao() {
            when(subprocessoRepo.findById(1L)).thenReturn(Optional.of(subprocesso));
            when(analiseRepo.save(any(Analise.class))).thenAnswer(i -> i.getArgument(0));

            Analise resultado = service.criarAnalise(1L, OBSERVACAO, TipoAnalise.VALIDACAO, null, null, null, null);

            assertNotNull(resultado);
            assertEquals(subprocesso, resultado.getSubprocesso());
            assertEquals(OBSERVACAO, resultado.getObservacoes());
            assertEquals(TipoAnalise.VALIDACAO, resultado.getTipo());
            verify(analiseRepo).save(any(Analise.class));
        }

        @Test
        @DisplayName("Deve lançar exceção se o subprocesso não for encontrado ao criar")
        void deveLancarExcecaoSeSubprocessoNaoEncontradoAoCriar() {
            when(subprocessoRepo.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ErroDominioNaoEncontrado.class, () -> service.criarAnalise(99L, "Obs", TipoAnalise.CADASTRO, null, null, null, null));
        }
    }

    @Nested
    @DisplayName("Testes para removerPorSubprocesso")
    class RemoverPorSubprocesso {

        @Test
        @DisplayName("Deve remover análises por subprocesso")
        void deveRemoverAnalisesPorSubprocesso() {
            doNothing().when(analiseRepo).deleteBySubprocessoCodigo(1L);
            service.removerPorSubprocesso(1L);
            verify(analiseRepo).deleteBySubprocessoCodigo(1L);
        }
    }
}