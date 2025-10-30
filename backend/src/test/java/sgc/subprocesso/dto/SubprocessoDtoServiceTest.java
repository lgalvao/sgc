package sgc.subprocesso.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.analise.AnaliseService;
import sgc.atividade.dto.AtividadeMapper;
import sgc.atividade.modelo.AtividadeRepo;
import sgc.comum.erros.ErroDominioAccessoNegado;
import sgc.conhecimento.dto.ConhecimentoMapper;
import sgc.conhecimento.modelo.ConhecimentoRepo;
import sgc.sgrh.modelo.Perfil;
import sgc.subprocesso.service.SubprocessoDtoService;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.subprocesso.modelo.SubprocessoRepo;
import sgc.unidade.modelo.Unidade;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubprocessoDtoServiceTest {

    @InjectMocks
    private SubprocessoDtoService service;

    @Mock
    private SubprocessoRepo subprocessoRepo;
    @Mock
    private AtividadeMapper atividadeMapper;
    @Mock
    private ConhecimentoMapper conhecimentoMapper;
    @Mock
    private AnaliseService analiseService;
    @Mock
    private AtividadeRepo atividadeRepo;
    @Mock
    private ConhecimentoRepo conhecimentoRepo;

    private Subprocesso subprocesso;

    @BeforeEach
    void setUp() {
        subprocesso = new Subprocesso();
        Unidade unidade = new Unidade();
        unidade.setCodigo(1L);
        subprocesso.setUnidade(unidade);
    }

    @Nested
    @DisplayName("Testes para obterDetalhes")
    class ObterDetalhesTests {

        @Test
        @DisplayName("Deve lançar exceção se perfil for nulo")
        void obterDetalhes_PerfilNulo_LancaExcecao() {
            assertThrows(ErroDominioAccessoNegado.class, () -> service.obterDetalhes(1L, null, 1L));
        }


        @Test
        @DisplayName("Deve lançar exceção se usuário não tiver permissão")
        void obterDetalhes_SemPermissao_LancaExcecao() {
            when(subprocessoRepo.findById(1L)).thenReturn(Optional.of(subprocesso));
            assertThrows(ErroDominioAccessoNegado.class, () -> service.obterDetalhes(1L, Perfil.GESTOR, 2L));
        }
    }
}
