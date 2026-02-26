package sgc.organizacao;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.springframework.security.core.*;
import org.springframework.security.core.authority.*;
import sgc.organizacao.model.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ModelExtraTest - Cobertura de métodos default e Security")
class ModelExtraTest {

    @Mock
    private UnidadeRepo unidadeRepo;

    @Test
    @DisplayName("UnidadeRepo.findBySigla - Deve chamar findBySiglaAndSituacao")
    void unidadeRepo_findBySigla() {
        // Prepare
        String sigla = "U1";
        Unidade u = new Unidade();
        u.setSigla(sigla);
        
        // Mocking the interface method that the default method calls
        when(unidadeRepo.findBySiglaAndSituacao(sigla, SituacaoUnidade.ATIVA))
                .thenReturn(Optional.of(u));
        
        // Call the default method
        doCallRealMethod().when(unidadeRepo).findBySigla(anyString());
        
        Optional<Unidade> result = unidadeRepo.findBySigla(sigla);
        
        assertThat(result).isPresent();
        assertThat(result.get().getSigla()).isEqualTo(sigla);
    }

    @Test
    @DisplayName("UnidadeRepo.findByUnidadeSuperiorCodigo - Deve chamar findByUnidadeSuperiorCodigoAndSituacao")
    void unidadeRepo_findByUnidadeSuperiorCodigo() {
        Long cod = 1L;
        doCallRealMethod().when(unidadeRepo).findByUnidadeSuperiorCodigo(anyLong());
        
        unidadeRepo.findByUnidadeSuperiorCodigo(cod);
        
        verify(unidadeRepo).findByUnidadeSuperiorCodigoAndSituacao(cod, SituacaoUnidade.ATIVA);
    }

    @Test
    @DisplayName("UnidadeRepo.findByTituloTitular - Deve chamar findByTituloTitularAndSituacao")
    void unidadeRepo_findByTituloTitular() {
        String titulo = "123";
        doCallRealMethod().when(unidadeRepo).findByTituloTitular(anyString());
        
        unidadeRepo.findByTituloTitular(titulo);
        
        verify(unidadeRepo).findByTituloTitularAndSituacao(titulo, SituacaoUnidade.ATIVA);
    }

    @Test
    @DisplayName("Usuario Security Methods - Teste de cobertura")
    void usuarioSecurityMethods() {
        Usuario u = new Usuario();
        u.setTituloEleitoral("123");
        
        assertThat(u.getUsername()).isEqualTo("123");
        assertThat(u.getPassword()).isNull();
        assertThat(u.getAuthorities()).isEmpty();
        
        u.setAuthorities(Set.of(new SimpleGrantedAuthority("ROLE_USER")));
        Collection<? extends GrantedAuthority> authorities = u.getAuthorities();
        assertThat(authorities).hasSize(1);
        assertThat(authorities.iterator().next().getAuthority()).isEqualTo("ROLE_USER");
    }
}
