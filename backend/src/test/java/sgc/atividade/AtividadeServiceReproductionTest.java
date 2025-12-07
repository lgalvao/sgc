package sgc.atividade;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.atividade.dto.AtividadeDto;
import sgc.atividade.dto.AtividadeMapper;
import sgc.atividade.model.AtividadeRepo;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.sgrh.model.Usuario;
import sgc.sgrh.model.UsuarioRepo;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.unidade.model.Unidade;

@ExtendWith(MockitoExtension.class)
class AtividadeServiceReproductionTest {

    @InjectMocks private AtividadeService atividadeService;

    @Mock private AtividadeRepo atividadeRepo;
    @Mock private AtividadeMapper atividadeMapper;
    @Mock private SubprocessoRepo subprocessoRepo;
    @Mock private UsuarioRepo usuarioRepo;

    @Test
    @DisplayName("REPRODUÇÃO: criar falha com NPE se unidade do subprocesso for nula")
    void criarComUnidadeNula() {
        Long mapaId = 10L;
        String usuarioId = "user1";
        AtividadeDto dto = new AtividadeDto();
        dto.setMapaCodigo(mapaId);

        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral(usuarioId);

        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setUnidade(null); // Simulate the data integrity issue

        when(subprocessoRepo.findByMapaCodigo(mapaId)).thenReturn(Optional.of(subprocesso));
        when(usuarioRepo.findById(usuarioId)).thenReturn(Optional.of(usuario));

        // This is expected to throw ErroEntidadeNaoEncontrada now
        assertThatThrownBy(() -> atividadeService.criar(dto, usuarioId))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }

    @Test
    @DisplayName(
            "REPRODUÇÃO: criar falha com NPE se processo do subprocesso for nulo durante"
                    + " atualização de situação")
    void criarComProcessoNulo() {
        Long mapaId = 10L;
        String usuarioId = "user1";
        AtividadeDto dto = new AtividadeDto();
        dto.setMapaCodigo(mapaId);

        Unidade unidade = new Unidade();
        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral(usuarioId);
        unidade.setTitular(usuario);

        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setUnidade(unidade);
        subprocesso.setSituacao(SituacaoSubprocesso.NAO_INICIADO);
        subprocesso.setProcesso(null); // Simulate missing process

        when(subprocessoRepo.findByMapaCodigo(mapaId)).thenReturn(Optional.of(subprocesso));
        when(usuarioRepo.findById(usuarioId)).thenReturn(Optional.of(usuario));

        // This is expected to throw ErroEntidadeNaoEncontrada now
        assertThatThrownBy(() -> atividadeService.criar(dto, usuarioId))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }
}
