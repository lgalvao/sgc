package sesel.sgc.servidor;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import sesel.sgc.unidade.Unidade;
import sesel.sgc.unidade.UnidadeRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ServidorRepositoryTest {
    @Autowired
    private ServidorRepository servidorRepository;

    @Autowired
    private UnidadeRepository unidadeRepository;

    @Test
    @DisplayName("Deve encontrar servidores por unidadeId")
    void testFindByUnidadeId() {
        Unidade unidade = new Unidade();
        unidade.setNome("Unidade Teste " + UUID.randomUUID());
        unidade = unidadeRepository.save(unidade);

        Servidor servidor = new Servidor();
        servidor.setNome("Servidor Teste");
        servidor.setEmail("teste@exemplo.com");
        servidor.setSenha("senha");
        servidor.setUnidade(unidade);

        servidorRepository.save(servidor);

        List<Servidor> encontrados = servidorRepository.findByUnidadeId(unidade.getId());

        assertFalse(encontrados.isEmpty());
        assertEquals("Servidor Teste", encontrados.getFirst().getNome());
    }

    @Test
    @DisplayName("Deve encontrar servidor por email")
    void testFindByEmail() {
        Unidade unidade = new Unidade();
        unidade.setNome("Unidade Teste " + UUID.randomUUID());
        unidade = unidadeRepository.save(unidade);

        Servidor servidor = new Servidor();
        servidor.setNome("Servidor Teste");
        servidor.setEmail("email@exemplo.com");
        servidor.setSenha("senha");
        servidor.setUnidade(unidade);
        servidorRepository.save(servidor);

        Optional<Servidor> encontrado = servidorRepository.findByEmail("email@exemplo.com");
        assertTrue(encontrado.isPresent());
        assertEquals("Servidor Teste", encontrado.get().getNome());
    }
}