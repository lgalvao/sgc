package sgc.processo.model;

import jakarta.persistence.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.*;
import org.springframework.data.domain.*;
import org.springframework.transaction.annotation.*;
import sgc.organizacao.model.*;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
@Tag("integration")
@DisplayName("ProcessoRepo - Testes de Repositório")
class ProcessoRepoTest {
    @Autowired
    private ProcessoRepo processoRepo;

    @Autowired
    private EntityManager entityManager;

    private Unidade criarUnidade(String nome, String sigla) {
        Unidade unidade = Unidade.builder()
                .nome(nome)
                .sigla(sigla)
                .matriculaTitular("12345678")
                .tituloTitular("123456789012")
                .dataInicioTitularidade(LocalDateTime.now())
                .tipo(TipoUnidade.OPERACIONAL)
                .situacao(SituacaoUnidade.ATIVA)
                .build();
        entityManager.persist(unidade);
        return unidade;
    }

    private void criarProcesso(String descricao, SituacaoProcesso situacao, Unidade... participantes) {
        Processo processo = Processo.builder()
                .descricao(descricao)
                .situacao(situacao)
                .tipo(TipoProcesso.MAPEAMENTO)
                .dataCriacao(LocalDateTime.now())
                .dataLimite(LocalDateTime.now().plusDays(30))
                .build();

        if (participantes.length > 0) {
            processo.adicionarParticipantes(Set.of(participantes));
        }
        entityManager.persist(processo);
    }

    @Test
    @DisplayName("Deve excluir processos com situação CRIADO")
    void deveExcluirProcessosComSituacaoCriado() {
        // Arrange
        Unidade u1 = criarUnidade("Unidade 1", "U1");

        criarProcesso("Processo Criado", SituacaoProcesso.CRIADO, u1);
        criarProcesso("Processo Em Andamento", SituacaoProcesso.EM_ANDAMENTO, u1);

        entityManager.flush(); // Ensure Persistence

        // Act
        Page<Processo> resultado = processoRepo.findDistinctByParticipantes_IdUnidadeCodigoInAndSituacaoNot(
                List.of(u1.getCodigo()),
                SituacaoProcesso.CRIADO,
                PageRequest.of(0, 10)
        );

        // Assert
        assertThat(resultado.getContent())
                .hasSize(1)
                .extracting(Processo::getDescricao)
                .containsExactly("Processo Em Andamento");

        assertThat(resultado.getContent())
                .extracting(Processo::getSituacao)
                .doesNotContain(SituacaoProcesso.CRIADO);
    }

    @Test
    @DisplayName("Deve filtrar corretamente por códigos de unidade")
    void deveFiltrarPorCodigosDeUnidade() {
        // Arrange
        Unidade u1 = criarUnidade("Unidade 1", "U1");
        Unidade u2 = criarUnidade("Unidade 2", "U2");

        criarProcesso("Processo U1", SituacaoProcesso.EM_ANDAMENTO, u1);
        criarProcesso("Processo U2", SituacaoProcesso.EM_ANDAMENTO, u2); // Não deve aparecer
        criarProcesso("Processo Ambos", SituacaoProcesso.EM_ANDAMENTO, u1, u2);

        entityManager.flush();

        // Act
        Page<Processo> resultado = processoRepo.findDistinctByParticipantes_IdUnidadeCodigoInAndSituacaoNot(
                List.of(u1.getCodigo()),
                SituacaoProcesso.CRIADO,
                PageRequest.of(0, 10)
        );

        // Assert
        assertThat(resultado.getContent())
                .hasSize(2)
                .extracting(Processo::getDescricao)
                .containsExactlyInAnyOrder("Processo U1", "Processo Ambos");
    }

    @Test
    @DisplayName("Deve verificar se DISTINCT evita duplicatas")
    void deveVerificarDistinct() {
        // Arrange
        Unidade u1 = criarUnidade("Unidade 1", "U1");
        Unidade u2 = criarUnidade("Unidade 2", "U2");

        // Processo participa de DUAS unidades que eu estou buscando
        criarProcesso("Processo Ambos", SituacaoProcesso.EM_ANDAMENTO, u1, u2);

        entityManager.flush();

        // Act
        Page<Processo> resultado = processoRepo.findDistinctByParticipantes_IdUnidadeCodigoInAndSituacaoNot(
                List.of(u1.getCodigo(), u2.getCodigo()), // Busco por AMBAS
                SituacaoProcesso.CRIADO,
                PageRequest.of(0, 10)
        );

        // Assert
        assertThat(resultado.getContent())
                .hasSize(1) // DISTINCT deve garantir apenas 1 resultado
                .extracting(Processo::getDescricao)
                .containsExactly("Processo Ambos");
    }

    @Test
    @DisplayName("Deve verificar contagem total correta na paginação")
    void deveVerificarContagemPaginacao() {
        // Arrange
        Unidade u1 = criarUnidade("Unidade 1", "U1");

        for (int i = 0; i < 5; i++) {
            criarProcesso("P" + i, SituacaoProcesso.EM_ANDAMENTO, u1);
        }

        entityManager.flush();

        // Act - Paginação com tamanho 2 (espera-se 3 páginas: 2, 2, 1)
        Page<Processo> resultado = processoRepo.findDistinctByParticipantes_IdUnidadeCodigoInAndSituacaoNot(
                List.of(u1.getCodigo()),
                SituacaoProcesso.CRIADO,
                PageRequest.of(0, 2)
        );

        // Assert
        assertThat(resultado.getTotalElements()).isEqualTo(5);
        assertThat(resultado.getTotalPages()).isEqualTo(3);
        assertThat(resultado.getContent()).hasSize(2);
    }

    @Test
    @DisplayName("Deve incluir status diferentes do excluído")
    void deveIncluirStatusDiferentes() {
        // Arrange
        Unidade u1 = criarUnidade("Unidade 1", "U1");

        criarProcesso("P Finalizado", SituacaoProcesso.FINALIZADO, u1);
        criarProcesso("P Andamento", SituacaoProcesso.EM_ANDAMENTO, u1);
        criarProcesso("P Criado", SituacaoProcesso.CRIADO, u1); // Deve ser ignorado

        entityManager.flush();

        // Act
        Page<Processo> resultado = processoRepo.findDistinctByParticipantes_IdUnidadeCodigoInAndSituacaoNot(
                List.of(u1.getCodigo()),
                SituacaoProcesso.CRIADO,
                PageRequest.of(0, 10)
        );

        // Assert
        assertThat(resultado.getContent())
                .hasSize(2)
                .extracting(Processo::getSituacao)
                .containsExactlyInAnyOrder(SituacaoProcesso.FINALIZADO, SituacaoProcesso.EM_ANDAMENTO);
    }
}
