package sgc.subprocesso.model;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.*;
import org.springframework.transaction.annotation.*;
import sgc.organizacao.model.*;

import java.time.*;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
@Tag("integration")
@DisplayName("MovimentacaoRepo - Testes de Repositório")
class MovimentacaoRepoTest {

    @Autowired
    private MovimentacaoRepo movimentacaoRepo;

    @Autowired
    private SubprocessoRepo subprocessoRepo;

    @Autowired
    private UnidadeRepo unidadeRepo;

    @Autowired
    private UsuarioRepo usuarioRepo;

    @Test
    @DisplayName("deve buscar primeira movimentacao em ordem decrescente")
    void deveBuscarPrimeiraMovimentacaoEmOrdemDecrescente() {
        Subprocesso subprocesso = subprocessoRepo.findById(60000L).orElseThrow();
        Unidade origem = unidadeRepo.findById(1L).orElseThrow();
        Unidade destino = unidadeRepo.findById(8L).orElseThrow();
        Usuario usuario = usuarioRepo.findById("1").orElseThrow();

        movimentacaoRepo.save(Movimentacao.builder()
                .subprocesso(subprocesso)
                .unidadeOrigem(origem)
                .unidadeDestino(destino)
                .usuario(usuario)
                .descricao("Movimentacao antiga")
                .dataHora(LocalDateTime.of(2099, 1, 5, 8, 0))
                .build());
        movimentacaoRepo.save(Movimentacao.builder()
                .subprocesso(subprocesso)
                .unidadeOrigem(origem)
                .unidadeDestino(destino)
                .usuario(usuario)
                .descricao("Movimentacao recente")
                .dataHora(LocalDateTime.of(2099, 1, 5, 9, 0))
                .build());

        assertThat(movimentacaoRepo.buscarUltimaPorSubprocesso(60000L))
                .get()
                .extracting(Movimentacao::getDescricao)
                .isEqualTo("Movimentacao recente");
        assertThat(movimentacaoRepo.findBySubprocessoCodigo(60000L)).hasSizeGreaterThanOrEqualTo(3);
    }
}
