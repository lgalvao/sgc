package sgc.competencia;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.atividade.modelo.Atividade;
import sgc.atividade.modelo.AtividadeRepo;
import sgc.competencia.dto.CompetenciaDto;
import sgc.competencia.dto.CompetenciaMapper;
import sgc.competencia.modelo.Competencia;
import sgc.competencia.modelo.CompetenciaAtividade;
import sgc.competencia.modelo.CompetenciaAtividadeRepo;
import sgc.competencia.modelo.CompetenciaRepo;
import sgc.comum.erros.ErroDominioNaoEncontrado;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompetenciaServiceTest {

    @InjectMocks
    private CompetenciaService competenciaService;

    @Mock
    private CompetenciaRepo competenciaRepo;
    @Mock
    private CompetenciaMapper competenciaMapper;
    @Mock
    private AtividadeRepo atividadeRepo;
    @Mock
    private CompetenciaAtividadeRepo competenciaAtividadeRepo;

    @Test
    @DisplayName("Deve listar todas as competências")
    void listarCompetencias_DeveRetornarLista() {
        when(competenciaRepo.findAll()).thenReturn(Collections.singletonList(new Competencia()));
        when(competenciaMapper.toDTO(any(Competencia.class))).thenReturn(new CompetenciaDto(1L, 1L, "Teste"));

        List<CompetenciaDto> result = competenciaService.listarCompetencias();

        assertFalse(result.isEmpty());
        verify(competenciaRepo).findAll();
    }

    @Nested
    @DisplayName("Testes para obterCompetencia")
    class ObterCompetenciaTests {
        @Test
        @DisplayName("Deve retornar competência quando encontrada")
        void obterCompetencia_QuandoEncontrada_DeveRetornarDto() {
            when(competenciaRepo.findById(1L)).thenReturn(Optional.of(new Competencia()));
            when(competenciaMapper.toDTO(any(Competencia.class))).thenReturn(new CompetenciaDto(1L, 1L, "Teste"));

            CompetenciaDto result = competenciaService.obterCompetencia(1L);

            assertNotNull(result);
            verify(competenciaRepo).findById(1L);
        }

        @Test
        @DisplayName("Deve lançar exceção quando não encontrada")
        void obterCompetencia_QuandoNaoEncontrada_DeveLancarExcecao() {
            when(competenciaRepo.findById(1L)).thenReturn(Optional.empty());

            assertThrows(ErroDominioNaoEncontrado.class, () -> competenciaService.obterCompetencia(1L));
        }
    }

    @Test
    @DisplayName("Deve criar uma nova competência")
    void criarCompetencia_DeveSalvarERetornarDto() {
        CompetenciaDto dto = new CompetenciaDto(null, 1L, "<b>Teste</b>");
        Competencia entity = new Competencia();
        when(competenciaMapper.toEntity(any(CompetenciaDto.class))).thenReturn(entity);
        when(competenciaRepo.save(entity)).thenReturn(entity);
        when(competenciaMapper.toDTO(entity)).thenReturn(new CompetenciaDto(1L, 1L, "Teste"));

        CompetenciaDto result = competenciaService.criarCompetencia(dto);

        assertNotNull(result.getCodigo());
        verify(competenciaRepo).save(any(Competencia.class));
    }

    @Nested
    @DisplayName("Testes para atualizarCompetencia")
    class AtualizarCompetenciaTests {
        @Test
        @DisplayName("Deve atualizar competência existente")
        void atualizarCompetencia_QuandoExistente_DeveAtualizar() {
            CompetenciaDto dto = new CompetenciaDto(1L, 1L, "Nova Descrição");
            Competencia existing = new Competencia();
            when(competenciaRepo.findById(1L)).thenReturn(Optional.of(existing));
            when(competenciaRepo.save(existing)).thenReturn(existing);
            when(competenciaMapper.toDTO(existing)).thenReturn(dto);

            CompetenciaDto result = competenciaService.atualizarCompetencia(1L, dto);

            assertEquals("Nova Descrição", result.getDescricao());
            verify(competenciaRepo).save(existing);
        }

        @Test
        @DisplayName("Deve lançar exceção ao atualizar competência inexistente")
        void atualizarCompetencia_QuandoInexistente_DeveLancarExcecao() {
            when(competenciaRepo.findById(1L)).thenReturn(Optional.empty());
            assertThrows(ErroDominioNaoEncontrado.class, () -> competenciaService.atualizarCompetencia(1L, new CompetenciaDto(1L, 1L, "")));
        }
    }

    @Nested
    @DisplayName("Testes para excluirCompetencia")
    class ExcluirCompetenciaTests {
        @Test
        @DisplayName("Deve excluir competência existente")
        void excluirCompetencia_QuandoExistente_DeveExcluir() {
            when(competenciaRepo.existsById(1L)).thenReturn(true);
            doNothing().when(competenciaRepo).deleteById(1L);
            competenciaService.excluirCompetencia(1L);
            verify(competenciaRepo).deleteById(1L);
        }

        @Test
        @DisplayName("Deve lançar exceção ao excluir competência inexistente")
        void excluirCompetencia_QuandoInexistente_DeveLancarExcecao() {
            when(competenciaRepo.existsById(1L)).thenReturn(false);
            assertThrows(ErroDominioNaoEncontrado.class, () -> competenciaService.excluirCompetencia(1L));
        }
    }

    @Nested
    @DisplayName("Testes para vincularAtividade")
    class VincularAtividadeTests {

        @Test
        @DisplayName("Deve vincular atividade com sucesso")
        void vincularAtividade_QuandoValido_DeveCriarVinculo() {
            when(competenciaRepo.findById(1L)).thenReturn(Optional.of(new Competencia()));
            when(atividadeRepo.findById(1L)).thenReturn(Optional.of(new Atividade()));
            when(competenciaAtividadeRepo.existsById(any())).thenReturn(false);
            when(competenciaAtividadeRepo.save(any(CompetenciaAtividade.class))).thenReturn(new CompetenciaAtividade());

            competenciaService.vincularAtividade(1L, 1L);

            verify(competenciaAtividadeRepo).save(any(CompetenciaAtividade.class));
        }

        @Test
        @DisplayName("Deve lançar exceção se o vínculo já existe")
        void vincularAtividade_QuandoVinculoExiste_DeveLancarExcecao() {
            when(competenciaRepo.findById(1L)).thenReturn(Optional.of(new Competencia()));
            when(atividadeRepo.findById(1L)).thenReturn(Optional.of(new Atividade()));
            when(competenciaAtividadeRepo.existsById(any())).thenReturn(true);

            assertThrows(IllegalStateException.class, () -> competenciaService.vincularAtividade(1L, 1L));
        }
    }

    @Nested
    @DisplayName("Testes para desvincularAtividade")
    class DesvincularAtividadeTests {

        @Test
        @DisplayName("Deve desvincular atividade com sucesso")
        void desvincularAtividade_QuandoVinculoExiste_DeveRemover() {
            when(competenciaAtividadeRepo.existsById(any())).thenReturn(true);
            doNothing().when(competenciaAtividadeRepo).deleteById(any());

            competenciaService.desvincularAtividade(1L, 1L);

            verify(competenciaAtividadeRepo).deleteById(any());
        }

        @Test
        @DisplayName("Deve lançar exceção se o vínculo não existe")
        void desvincularAtividade_QuandoVinculoNaoExiste_DeveLancarExcecao() {
            when(competenciaAtividadeRepo.existsById(any())).thenReturn(false);
            assertThrows(ErroDominioNaoEncontrado.class, () -> competenciaService.desvincularAtividade(1L, 1L));
        }
    }
}
