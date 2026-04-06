package sgc.subprocesso.model;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.*;
import org.springframework.transaction.annotation.*;
import sgc.organizacao.model.*;

import java.time.*;
import java.util.*;

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

    @Test
    @DisplayName("deve buscar ultimas movimentacoes em lote")
    void deveBuscarUltimasMovimentacoesEmLote() {
        Subprocesso subprocesso1 = subprocessoRepo.findById(60000L).orElseThrow();
        Subprocesso subprocesso2 = subprocessoRepo.findById(60003L).orElseThrow();
        Unidade origem = unidadeRepo.findById(1L).orElseThrow();
        Unidade destino1 = unidadeRepo.findById(8L).orElseThrow();
        Unidade destino2 = unidadeRepo.findById(9L).orElseThrow();
        Usuario usuario = usuarioRepo.findById("1").orElseThrow();

        movimentacaoRepo.save(Movimentacao.builder()
                .subprocesso(subprocesso1)
                .unidadeOrigem(origem)
                .unidadeDestino(destino1)
                .usuario(usuario)
                .descricao("Lote 1")
                .dataHora(LocalDateTime.of(2099, 1, 6, 8, 0))
                .build());
        movimentacaoRepo.save(Movimentacao.builder()
                .subprocesso(subprocesso2)
                .unidadeOrigem(origem)
                .unidadeDestino(destino2)
                .usuario(usuario)
                .descricao("Lote 2")
                .dataHora(LocalDateTime.of(2099, 1, 6, 9, 0))
                .build());

        List<Movimentacao> movimentacoes = movimentacaoRepo.listarUltimasPorSubprocessos(List.of(60000L, 60003L));

        assertThat(movimentacoes)
                .extracting(movimentacao -> movimentacao.getSubprocesso().getCodigo())
                .contains(60000L, 60003L);
        assertThat(movimentacoes)
                .allSatisfy(movimentacao -> assertThat(movimentacao.getUnidadeDestino()).isNotNull());
    }
}
