package sgc.subprocesso;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.analise.mapper.AnaliseMapper;
import sgc.comum.erros.ErroValidacao;
import sgc.mapa.model.Atividade;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.model.Usuario;
import sgc.subprocesso.service.SubprocessoFacade;

import java.security.Principal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import sgc.analise.AnaliseFacade;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("Testes de Cobertura para SubprocessoCadastroController")
class SubprocessoCadastroControllerCoverageTest {

    @InjectMocks
    private SubprocessoCadastroController controller;

    @Mock
    private SubprocessoFacade subprocessoFacade;
    @Mock
    private AnaliseFacade analiseFacade;
    @Mock
    private AnaliseMapper analiseMapper;
    @Mock
    private UsuarioFacade usuarioService;

    @Test
    @DisplayName("disponibilizarRevisao deve validar atividades sem conhecimento")
    void disponibilizarRevisaoValidaAtividades() {
        Long codigo = 1L;
        Principal principal = mock(Principal.class);
        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral("123");

        when(usuarioService.extrairTituloUsuario(principal)).thenReturn("123");
        when(usuarioService.buscarPorLogin("123")).thenReturn(usuario);

        Atividade atividade = new Atividade();
        atividade.setCodigo(10L);
        atividade.setDescricao("Atividade sem conhecimento");
        when(subprocessoFacade.obterAtividadesSemConhecimento(codigo)).thenReturn(List.of(atividade));

        assertThrows(ErroValidacao.class, () ->
            controller.disponibilizarRevisao(codigo, principal)
        );
    }
}
