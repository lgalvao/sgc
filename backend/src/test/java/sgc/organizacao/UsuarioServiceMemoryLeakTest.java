package sgc.organizacao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import sgc.organizacao.model.Usuario;
import sgc.organizacao.model.UsuarioRepo;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("integration")
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Verificação de Correção de Vulnerabilidade de Memória")
class UsuarioServiceMemoryLeakTest {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepo usuarioRepo;

    @Test
    @DisplayName("Deve limpar autenticações expiradas")
    void deveLimparAutenticacoesExpiradas() {
        // Arrange
        int tamanhoInicial = usuarioService.getAutenticacoesRecentesSize();

        // Act - Simula 100 autenticações
        for (int i = 0; i < 100; i++) {
            // Usa prefixo curto para caber em 12 chars (TLEAK_ + 3 digits fits)
            String tituloFalso = String.format("TLEAK_%03d", i);

            if (!usuarioRepo.existsById(tituloFalso)) {
                Usuario u = Usuario.builder()
                    .tituloEleitoral(tituloFalso)
                    .matricula("M" + i)
                    .nome("Nome " + i)
                    .email("e" + i + "@t.com")
                    .ramal("1")
                    .build();
                usuarioRepo.save(u);
            }

            usuarioService.autenticar(tituloFalso, "senha");
        }

        // Assert - O mapa deve ter crescido
        int tamanhoAposAutenticacao = usuarioService.getAutenticacoesRecentesSize();
        assertTrue(tamanhoAposAutenticacao >= tamanhoInicial + 100,
            "Mapa deveria ter crescido. Inicial: " + tamanhoInicial + ", Atual: " + tamanhoAposAutenticacao);

        // Manipula os tempos para simular expiração usando método de teste
        usuarioService.expireAllAuthenticationsForTest();

        // Executa a limpeza
        usuarioService.limparAutenticacoesExpiradas();

        // Assert - O mapa deve ter reduzido (pelo menos os 100 removidos, possivelmente mais se outros expiraram)
        int tamanhoFinal = usuarioService.getAutenticacoesRecentesSize();
        assertTrue(tamanhoFinal < tamanhoAposAutenticacao,
            "A limpeza deveria ter removido itens expirados. Antes: " + tamanhoAposAutenticacao + ", Depois: " + tamanhoFinal);

        // Limpeza de dados
        for (int i = 0; i < 100; i++) {
            String tituloFalso = String.format("TLEAK_%03d", i);
            if (usuarioRepo.existsById(tituloFalso)) {
                usuarioRepo.deleteById(tituloFalso);
            }
        }
    }
}
