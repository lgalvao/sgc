package sesel.sgc.seguranca;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import sesel.sgc.servidor.ServidorRepository;
import sesel.sgc.unidade.UnidadeRepository;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {TestDataInitializer.class})
@ActiveProfiles("test")
class TestDataInitializerTest {
    @MockitoBean
    private ServidorRepository servidorRepository;

    @MockitoBean
    private UnidadeRepository unidadeRepository;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TestDataInitializer initializer;

    @Test
    void testInitDoesNotThrow() {
        // Mocks básicos para evitar NullPointerException
        when(unidadeRepository.findByNome(anyString())).thenReturn(Optional.empty());
        when(unidadeRepository.save(Mockito.any())).thenAnswer(i -> i.getArgument(0));
        when(servidorRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(servidorRepository.save(Mockito.any())).thenAnswer(i -> i.getArgument(0));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");

        initializer.init();
    }
}