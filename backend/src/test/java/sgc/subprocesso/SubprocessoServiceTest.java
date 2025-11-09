package sgc.subprocesso;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.atividade.model.Atividade;
import sgc.atividade.model.AtividadeRepo;
import sgc.atividade.model.ConhecimentoRepo;
import sgc.comum.erros.ErroValidacao;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.CompetenciaRepo;
import sgc.mapa.model.Mapa;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.subprocesso.service.SubprocessoService;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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

            assertThrows(ErroValidacao.class, () -> service.validarAssociacoesMapa(1L));
        }

        @Test
        @DisplayName("Deve lançar exceção se atividade não estiver associada")
        void validarAssociacoesMapa_AtividadeNaoAssociada_LancaExcecao() {
            Atividade atividade = new Atividade();
            Competencia competencia = new Competencia();
            competencia.setAtividades(Set.of(atividade));
            when(competenciaRepo.findByMapaCodigo(1L)).thenReturn(Collections.singletonList(competencia));
            when(atividadeRepo.findByMapaCodigo(1L)).thenReturn(Collections.singletonList(new Atividade()));

            assertThrows(ErroValidacao.class, () -> service.validarAssociacoesMapa(1L));
        }
    }
}
