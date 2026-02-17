package sgc.painel;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;
import sgc.organizacao.model.Perfil;
import sgc.processo.dto.CriarProcessoRequest;
import sgc.processo.model.Processo;
import sgc.processo.dto.ProcessoResumoDto;
import sgc.processo.model.TipoProcesso;
import sgc.processo.service.ProcessoFacade;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("integration")
@SpringBootTest
@Transactional
@DisplayName("Testes de Integração do PainelFacade")
class PainelServiceIntegrationTest {
    @Autowired
    private PainelFacade painelService;

    @Autowired
    private ProcessoFacade processoFacade;

    @Nested
    @DisplayName("Listagem de Processos")
    class ListagemProcessos {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Admin deve ver processo de revisão recém-criado na listagem")
        void deveVerProcessoRevisaoNaListagem() {
            // Arrange: Criar processo de mapeamento
            CriarProcessoRequest reqMapeamento = new CriarProcessoRequest(
                    "Processo Mapeamento Teste",
                    TipoProcesso.MAPEAMENTO,
                    LocalDateTime.now().plusDays(30),
                    List.of(10L) // SESEL
            );
            processoFacade.criar(reqMapeamento);

            // Arrange: Criar processo de revisão
            CriarProcessoRequest reqRevisao = new CriarProcessoRequest(
                    "Processo Revisão Teste",
                    TipoProcesso.REVISAO,
                    LocalDateTime.now().plusDays(30),
                    List.of(10L) // SESEL
            );
            Processo processoRevisao = processoFacade.criar(reqRevisao);
            Long codigoProcessoRevisao = processoRevisao.getCodigo();

            // Act: Listar processos como ADMIN
            Page<ProcessoResumoDto> processos = painelService.listarProcessos(
                    Perfil.ADMIN,
                    null,
                    PageRequest.of(0, 20) // Página 0, 20 itens por página
            );

            // Assert: Verificar que o processo de revisão está na lista
            List<ProcessoResumoDto> listaProcessos = processos.getContent();

            // Verificar que existe pelo menos um processo com a descrição esperada
            boolean processoRevisaoEncontrado = listaProcessos.stream()
                    .anyMatch(p -> p.descricao().equals("Processo Revisão Teste"));

            assertThat(processoRevisaoEncontrado)
                    .withFailMessage("Processo de revisão não encontrado na listagem. " +
                            "Processos retornados: " + listaProcessos.size())
                    .isTrue();

            // Verificar que o linkDestino está correto
            ProcessoResumoDto pResumo = listaProcessos.stream()
                    .filter(p -> p.descricao().equals("Processo Revisão Teste"))
                    .findFirst()
                    .orElse(null);

            assertThat(pResumo).isNotNull();
            assertThat(pResumo.linkDestino())
                    .withFailMessage("LinkDestino incorreto para processo " + codigoProcessoRevisao +
                            ". Esperado: /processo/cadastro?codProcesso=" + codigoProcessoRevisao +
                            ", Obtido: " + pResumo.linkDestino())
                    .isEqualTo("/processo/cadastro?codProcesso=" + codigoProcessoRevisao);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Paginação deve incluir todos os processos quando tamanho da página é suficiente")
        void deveIncluirTodosProcessosNaPaginacao() {
            // Arrange: Criar múltiplos processos
            for (int i = 1; i <= 5; i++) {
                CriarProcessoRequest req = new CriarProcessoRequest(
                        "Processo Teste " + i,
                        TipoProcesso.MAPEAMENTO,
                        LocalDateTime.now().plusDays(30),
                        List.of(10L)
                );
                processoFacade.criar(req);
            }

            // Act: Listar processos com página grande o suficiente
            Page<ProcessoResumoDto> processos = painelService.listarProcessos(
                    Perfil.ADMIN,
                    null,
                    PageRequest.of(0, 50) // Página grande
            );

            // Assert: Verificar que todos os 5 processos criados estão na lista
            // (Mais os 2 processos seed do banco)
            assertThat(processos.getTotalElements())
                    .withFailMessage("Total de processos incorreto")
                    .isGreaterThanOrEqualTo(5);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Deve retornar links corretos quando existem processos finalizados e em andamento")
        void deveRetornarLinksCorretosComMixDeProcessos() {
            // Arrange: Simular o Processo 99 (Finalizado)
            CriarProcessoRequest reqSeedLike = new CriarProcessoRequest(
                    "Processo Like",
                    TipoProcesso.MAPEAMENTO,
                    LocalDateTime.now().plusDays(30),
                    List.of(10L)
            );
            Processo processoSeed = processoFacade.criar(reqSeedLike);

            // Criar processo de Revisão (Alvo do teste)
            CriarProcessoRequest reqRevisao = new CriarProcessoRequest(
                    "Processo Revisão Alvo",
                    TipoProcesso.REVISAO,
                    LocalDateTime.now().plusDays(30),
                    List.of(10L)
            );
            Processo processoRevisao = processoFacade.criar(reqRevisao);

            // Act: Buscar processos
            Page<ProcessoResumoDto> page = painelService.listarProcessos(Perfil.ADMIN, null, PageRequest.of(0, 50));
            List<ProcessoResumoDto> lista = page.getContent();

            ProcessoResumoDto dtoSeed = lista.stream()
                    .filter(p -> p.codigo().equals(processoSeed.getCodigo()))
                    .findFirst().orElseThrow();

            ProcessoResumoDto dtoRevisao = lista.stream()
                    .filter(p -> p.codigo().equals(processoRevisao.getCodigo()))
                    .findFirst().orElseThrow();

            // Assert: Links devem corresponder aos seus IDs
            assertThat(dtoSeed.linkDestino()).contains(processoSeed.getCodigo().toString());
            assertThat(dtoRevisao.linkDestino()).contains(processoRevisao.getCodigo().toString());

            // Garantir que um não tem o ID do outro
            assertThat(dtoRevisao.linkDestino()).doesNotContain(processoSeed.getCodigo().toString());
        }
    }
}
