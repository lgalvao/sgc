package sgc.atividade;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.atividade.dto.AtividadeDto;
import sgc.atividade.dto.AtividadeMapper;
import sgc.atividade.model.Atividade;
import sgc.atividade.model.AtividadeRepo;
import sgc.atividade.model.Conhecimento;
import sgc.atividade.model.ConhecimentoRepo;
import sgc.comum.erros.ErroAccessoNegado;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.sgrh.model.Usuario;
import sgc.sgrh.model.UsuarioRepo;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.unidade.model.Unidade;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AtividadeServiceTest {
    @InjectMocks
    private AtividadeService service;
    @Mock
    private AtividadeRepo atividadeRepo;
    @Mock
    private AtividadeMapper atividadeMapper;
    @Mock
    private ConhecimentoRepo conhecimentoRepo;
    @Mock
    private SubprocessoRepo subprocessoRepo;
    @Mock
    private UsuarioRepo usuarioRepo;

    private AtividadeDto atividadeDto;
    private Usuario usuario;
    private Subprocesso subprocesso;
    private Unidade unidade;

    @BeforeEach
    void setUp() {
        atividadeDto = new AtividadeDto(1L, 1L, "Descrição");
        usuario = new Usuario();
        usuario.setTituloEleitoral(123L);
        unidade = new Unidade();
        unidade.setTitular(usuario);
        subprocesso = new Subprocesso();
        subprocesso.setUnidade(unidade);
        subprocesso.setSituacao(SituacaoSubprocesso.CADASTRO_EM_ANDAMENTO);
    }

    @Nested
    @DisplayName("Testes para criar atividade")
    class CriarAtividadeTests {
        @Test
        @DisplayName("Deve criar atividade")
        void criar_Sucesso() {
            when(subprocessoRepo.findByMapaCodigo(1L)).thenReturn(Optional.of(subprocesso));
            when(usuarioRepo.findById(123L)).thenReturn(Optional.of(usuario));
            when(atividadeMapper.toEntity(atividadeDto)).thenReturn(new Atividade());
            when(atividadeRepo.save(any(Atividade.class))).thenReturn(new Atividade());

            service.criar(atividadeDto, "123");

            verify(atividadeRepo).save(any(Atividade.class));
        }

        @Test
        @DisplayName("Deve lançar exceção se usuário não for titular")
        void criar_NaoTitular_LancaExcecao() {
            Usuario outroUsuario = new Usuario();
            outroUsuario.setTituloEleitoral(456L);
            unidade.setTitular(outroUsuario);
            when(subprocessoRepo.findByMapaCodigo(1L)).thenReturn(Optional.of(subprocesso));
            when(usuarioRepo.findById(123L)).thenReturn(Optional.of(usuario));

            assertThrows(ErroAccessoNegado.class, () -> service.criar(atividadeDto, "123"));
        }

        @Test
        @DisplayName("Deve lançar exceção se subprocesso estiver finalizado")
        void criar_SubprocessoFinalizado_LancaExcecao() {
            subprocesso.setSituacao(SituacaoSubprocesso.MAPA_HOMOLOGADO);
            when(subprocessoRepo.findByMapaCodigo(1L)).thenReturn(Optional.of(subprocesso));
            when(usuarioRepo.findById(123L)).thenReturn(Optional.of(usuario));

            assertThrows(IllegalStateException.class, () -> service.criar(atividadeDto, "123"));
        }
    }

    @Test
    @DisplayName("Deve excluir conhecimento")
    void excluirConhecimento_Sucesso() {
        Conhecimento conhecimento = new Conhecimento();
        Atividade atividade = new Atividade();
        atividade.setCodigo(1L);
        conhecimento.setAtividade(atividade);
        when(conhecimentoRepo.findById(1L)).thenReturn(Optional.of(conhecimento));

        service.excluirConhecimento(1L, 1L);

        verify(conhecimentoRepo).delete(conhecimento);
    }

    @Test
    @DisplayName("Deve lançar exceção ao excluir conhecimento inexistente")
    void excluirConhecimento_Inexistente_LancaExcecao() {
        when(conhecimentoRepo.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ErroEntidadeNaoEncontrada.class, () -> service.excluirConhecimento(1L, 1L));
    }
}
