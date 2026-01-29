package sgc.organizacao.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.organizacao.model.Usuario;
import sgc.organizacao.model.UsuarioPerfilRepo;
import sgc.organizacao.model.UsuarioRepo;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioRepositoryServiceTest {

    @Mock private UsuarioRepo repo;
    @Mock private UsuarioPerfilRepo perfilRepo;
    @InjectMocks private UsuarioRepositoryService service;

    @Test
    void methodsDelegateToRepo() {
        service.findById("123");
        verify(repo).findById("123");

        service.findByIdWithAtribuicoes("123");
        verify(repo).findByIdWithAtribuicoes("123");

        service.findByIdInWithAtribuicoes(Collections.emptyList());
        verify(repo).findByIdInWithAtribuicoes(Collections.emptyList());

        service.findAllById(Collections.emptyList());
        verify(repo).findAllById(Collections.emptyList());

        service.findByUnidadeLotacaoCodigo(1L);
        verify(repo).findByUnidadeLotacaoCodigo(1L);

        service.findByEmail("email");
        verify(repo).findByEmail("email");

        service.findAll();
        verify(repo).findAll();

        service.chefePorCodUnidade(1L);
        verify(repo).chefePorCodUnidade(1L);

        service.findChefesByUnidadesCodigos(Collections.emptyList());
        verify(repo).findChefesByUnidadesCodigos(Collections.emptyList());
        
        Usuario u = new Usuario();
        service.salvar(u);
        verify(repo).save(u);

        service.deletar("123");
        verify(repo).deleteById("123");
    }

    @Test
    void methodsDelegateToPerfilRepo() {
        service.findByUsuarioTitulo("123");
        verify(perfilRepo).findByUsuarioTitulo("123");

        service.findByUsuarioTituloIn(Collections.emptyList());
        verify(perfilRepo).findByUsuarioTituloIn(Collections.emptyList());
    }

    @Test
    void buscarPorIdThrowsIfNotFound() {
        when(repo.findById("123")).thenReturn(Optional.empty());
        assertThrows(ErroEntidadeNaoEncontrada.class, () -> service.buscarPorId("123"));
    }
}
