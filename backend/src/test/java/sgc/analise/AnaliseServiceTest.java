package sgc.analise;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.analise.dto.CriarAnaliseRequest;
import sgc.analise.model.Analise;
import sgc.analise.model.AnaliseRepo;
import sgc.analise.model.TipoAnalise;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para AnaliseService")
class AnaliseServiceTest {
    private static final String OBS = "Observação";

    @Mock private AnaliseRepo analiseRepo;

    @Mock private SubprocessoRepo subprocessoRepo;

    @InjectMocks private AnaliseService service;

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
            when(analiseRepo.findBySubprocessoCodigoOrderByDataHoraDesc(1L))
                    .thenReturn(List.of(analise));

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
            when(analiseRepo.findBySubprocessoCodigoOrderByDataHoraDesc(1L))
                    .thenReturn(List.of(analise));

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

            assertThrows(
                    ErroEntidadeNaoEncontrada.class,
                    () -> service.listarPorSubprocesso(99L, TipoAnalise.CADASTRO));
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

            Analise resultado =
                    service.criarAnalise(
                            CriarAnaliseRequest.builder()
                                    .codSubprocesso(1L)
                                    .observacoes(OBS)
                                    .tipo(TipoAnalise.CADASTRO)
                                    .acao(null)
                                    .siglaUnidade(null)
                                    .tituloUsuario(null)
                                    .motivo(null)
                                    .build());

            assertNotNull(resultado);
            assertEquals(subprocesso, resultado.getSubprocesso());
            assertEquals(OBS, resultado.getObservacoes());
            assertEquals(TipoAnalise.CADASTRO, resultado.getTipo());
            verify(analiseRepo).save(any(Analise.class));
        }

        @Test
        @DisplayName("Deve criar uma análise de validação")
        void deveCriarAnaliseValidacao() {
            when(subprocessoRepo.findById(1L)).thenReturn(Optional.of(subprocesso));
            when(analiseRepo.save(any(Analise.class))).thenAnswer(i -> i.getArgument(0));

            Analise resultado =
                    service.criarAnalise(
                            CriarAnaliseRequest.builder()
                                    .codSubprocesso(1L)
                                    .observacoes(OBS)
                                    .tipo(TipoAnalise.VALIDACAO)
                                    .acao(null)
                                    .siglaUnidade(null)
                                    .tituloUsuario(null)
                                    .motivo(null)
                                    .build());

            assertNotNull(resultado);
            assertEquals(subprocesso, resultado.getSubprocesso());
            assertEquals(OBS, resultado.getObservacoes());
            assertEquals(TipoAnalise.VALIDACAO, resultado.getTipo());
            verify(analiseRepo).save(any(Analise.class));
        }

        @Test
        @DisplayName("Deve lançar exceção se o subprocesso não for encontrado ao criar")
        void deveLancarExcecaoSeSubprocessoNaoEncontradoAoCriar() {
            when(subprocessoRepo.findById(99L)).thenReturn(Optional.empty());

            assertThrows(
                    ErroEntidadeNaoEncontrada.class,
                    () ->
                            service.criarAnalise(
                                    CriarAnaliseRequest.builder()
                                            .codSubprocesso(99L)
                                            .observacoes("Obs")
                                            .tipo(TipoAnalise.CADASTRO)
                                            .acao(null)
                                            .siglaUnidade(null)
                                            .tituloUsuario(null)
                                            .motivo(null)
                                            .build()));
        }
    }

    @Nested
    @DisplayName("Testes para removerPorSubprocesso")
    class RemoverPorSubprocesso {
        @Test
        @DisplayName("Deve remover análises por subprocesso")
        void deveRemoverAnalisesPorSubprocesso() {
            Analise analise = new Analise();
            List<Analise> analises = List.of(analise);
            when(analiseRepo.findBySubprocessoCodigo(1L)).thenReturn(analises);

            service.removerPorSubprocesso(1L);

            verify(analiseRepo).findBySubprocessoCodigo(1L);
            verify(analiseRepo).deleteAll(analises);
        }
    }
}
