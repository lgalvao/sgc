package sgc.subprocesso;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.analise.AnaliseFacade;
import sgc.analise.mapper.AnaliseMapper;
import sgc.comum.erros.ErroAutenticacao;
import sgc.organizacao.UsuarioFacade;
import sgc.subprocesso.service.SubprocessoFacade;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubprocessoCadastroControllerCoverageTest {
    @InjectMocks
    private SubprocessoCadastroController controller;

    @Mock private SubprocessoFacade subprocessoFacade;
    @Mock private AnaliseFacade analiseFacade;
    @Mock private AnaliseMapper analiseMapper;
    @Mock private UsuarioFacade usuarioService;

    @Test
    @DisplayName("disponibilizarCadastro lança ErroAutenticacao se principal for nulo")
    void disponibilizarCadastro_PrincipalNulo() {
        Object principal = null;
        Long codigo = 1L;

        when(usuarioService.extrairTituloUsuario(principal)).thenReturn(null);

        assertThrows(ErroAutenticacao.class, 
            () -> controller.disponibilizarCadastro(codigo, principal));
    }
}
