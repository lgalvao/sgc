package sgc.subprocesso;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.atividade.modelo.Atividade;
import sgc.atividade.modelo.AtividadeRepo;
import sgc.competencia.modelo.Competencia;
import sgc.competencia.modelo.CompetenciaAtividadeRepo;
import sgc.competencia.modelo.CompetenciaRepo;
import sgc.comum.erros.ErroValidacao;
import sgc.conhecimento.modelo.ConhecimentoRepo;
import sgc.mapa.modelo.Mapa;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.subprocesso.modelo.SubprocessoRepo;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubprocessoServiceTest {

    @InjectMocks
    private SubprocessoService service;

    @Mock
    private SubprocessoRepo subprocessoRepo;
    @Mock
    private AtividadeRepo atividadeRepo;
    @Mock
    private ConhecimentoRepo conhecimentoRepo;
    @Mock
    private CompetenciaRepo competenciaRepo;
    @Mock
    private CompetenciaAtividadeRepo competenciaAtividadeRepo;

    private Subprocesso subprocesso;

    @BeforeEach
    void setUp() {
        subprocesso = new Subprocesso();
        Mapa mapa = new Mapa();
        mapa.setCodigo(1L);
        subprocesso.setMapa(mapa);
    }

    @Nested
    @DisplayName("Testes para obterAtividadesSemConhecimento")
    class ObterAtividadesSemConhecimentoTests {

        @Test
        @DisplayName("Deve retornar lista vazia se não houver atividades")
        void obterAtividadesSemConhecimento_SemAtividades_RetornaVazio() {
            when(subprocessoRepo.findById(1L)).thenReturn(Optional.of(subprocesso));
            when(atividadeRepo.findByMapaCodigo(1L)).thenReturn(Collections.emptyList());

            List<Atividade> result = service.obterAtividadesSemConhecimento(1L);
            assert (result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Testes para validarAssociacoesMapa")
    class ValidarAssociacoesMapaTests {
        @Test
        @DisplayName("Deve lançar exceção se competência não estiver associada")
        void validarAssociacoesMapa_CompetenciaNaoAssociada_LancaExcecao() {
            when(competenciaRepo.findByMapaCodigo(1L)).thenReturn(Collections.singletonList(new Competencia()));
            when(competenciaAtividadeRepo.countByCompetenciaCodigo(null)).thenReturn(0L);

            assertThrows(ErroValidacao.class, () -> service.validarAssociacoesMapa(1L));
        }

        @Test
        @DisplayName("Deve lançar exceção se atividade não estiver associada")
        void validarAssociacoesMapa_AtividadeNaoAssociada_LancaExcecao() {
            when(atividadeRepo.findByMapaCodigo(1L)).thenReturn(Collections.singletonList(new Atividade()));
            when(competenciaAtividadeRepo.countByAtividadeCodigo(null)).thenReturn(0L);

            assertThrows(ErroValidacao.class, () -> service.validarAssociacoesMapa(1L));
        }
    }
}
