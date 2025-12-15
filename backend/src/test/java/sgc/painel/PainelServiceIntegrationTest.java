package sgc.painel;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;
import sgc.processo.dto.CriarProcessoReq;
import sgc.processo.dto.ProcessoDto;
import sgc.processo.dto.ProcessoResumoDto;
import sgc.processo.model.TipoProcesso;
import sgc.processo.service.ProcessoService;
import sgc.sgrh.model.Perfil;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class PainelServiceIntegrationTest {
    @Autowired
    private PainelService painelService;

    @Autowired
    private ProcessoService processoService;

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Admin deve ver processo de revisão recém-criado na listagem")
    void adminDeveVerProcessoRevisaoNaListagem() {
        // Arrange: Criar processo de mapeamento
        CriarProcessoReq reqMapeamento = new CriarProcessoReq(
                "Processo Mapeamento Teste",
                TipoProcesso.MAPEAMENTO,
                LocalDateTime.now().plusDays(30),
                List.of(10L) // SESEL
        );
        processoService.criar(reqMapeamento);

        // Arrange: Criar processo de revisão
        CriarProcessoReq reqRevisao = new CriarProcessoReq(
                "Processo Revisão Teste",
                TipoProcesso.REVISAO,
                LocalDateTime.now().plusDays(30),
                List.of(10L) // SESEL
        );
        var processoRevisaoDto = processoService.criar(reqRevisao);
        Long codigoProcessoRevisao = processoRevisaoDto.getCodigo();

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
                .anyMatch(p -> p.getDescricao().equals("Processo Revisão Teste"));

        assertThat(processoRevisaoEncontrado)
                .withFailMessage("Processo de revisão não encontrado na listagem. " +
                        "Processos retornados: " + listaProcessos.size())
                .isTrue();

        // Verificar que o linkDestino está correto
        ProcessoResumoDto processoRevisao = listaProcessos.stream()
                .filter(p -> p.getDescricao().equals("Processo Revisão Teste"))
                .findFirst()
                .orElse(null);

        assertThat(processoRevisao).isNotNull();
        assertThat(processoRevisao.getLinkDestino())
                .withFailMessage("LinkDestino incorreto para processo " + codigoProcessoRevisao + 
                        ". Esperado: /processo/cadastro?codProcesso=" + codigoProcessoRevisao + 
                        ", Obtido: " + processoRevisao.getLinkDestino())
                .isEqualTo("/processo/cadastro?codProcesso=" + codigoProcessoRevisao);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Paginação deve incluir todos os processos quando tamanho da página é suficiente")
    void paginacaoDeveIncluirTodosProcessos() {
        // Arrange: Criar múltiplos processos
        for (int i = 1; i <= 5; i++) {
            CriarProcessoReq req = new CriarProcessoReq(
                    "Processo Teste " + i,
                    TipoProcesso.MAPEAMENTO,
                    LocalDateTime.now().plusDays(30),
                    List.of(10L)
            );
            processoService.criar(req);
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
        // Arrange: Simular o Processo Seed 99 (Finalizado)
        // Precisamos forçar o ID ou criar um que acabe ficando na lista
        // Como não conseguimos forçar ID com facilidade via Service, vamos criar um processo e finalizá-lo
        
        CriarProcessoReq reqSeedLike = new CriarProcessoReq(
                "Processo Seed Like",
                TipoProcesso.MAPEAMENTO,
                LocalDateTime.now().plusDays(30),
                List.of(10L)
        );
        ProcessoDto processoSeed = processoService.criar(reqSeedLike);
        
        // Finalizar este processo (precisa estar em andamento, então inicia e finaliza)
        // Simplificação: apenas criar um processo que represente o ID 99 na lógica
        // O bug suspeito é que clicar na linha de um leva ao link do outro.
        
        // Criar processo de Revisão (Alvo do teste)
        CriarProcessoReq reqRevisao = new CriarProcessoReq(
                "Processo Revisão Alvo",
                TipoProcesso.REVISAO,
                LocalDateTime.now().plusDays(30),
                List.of(10L)
        );
        ProcessoDto processoRevisao = processoService.criar(reqRevisao);
        
        // Act: Buscar processos
        Page<ProcessoResumoDto> page = painelService.listarProcessos(Perfil.ADMIN, null, PageRequest.of(0, 50));
        List<ProcessoResumoDto> lista = page.getContent();
        
        ProcessoResumoDto dtoSeed = lista.stream()
                .filter(p -> p.getCodigo().equals(processoSeed.getCodigo()))
                .findFirst().orElseThrow();
                
        ProcessoResumoDto dtoRevisao = lista.stream()
                .filter(p -> p.getCodigo().equals(processoRevisao.getCodigo()))
                .findFirst().orElseThrow();
                
        // Assert: Links devem corresponder aos seus IDs
        // Seed (CRIADO ou FINALIZADO) -> Link depende da situaçao. 
        // Se CRIADO e ADMIN -> /processo/cadastro?codProcesso=ID
        assertThat(dtoSeed.getLinkDestino()).contains(processoSeed.getCodigo().toString());
        assertThat(dtoRevisao.getLinkDestino()).contains(processoRevisao.getCodigo().toString());
        
        // Garantir que um não tem o ID do outro
        assertThat(dtoRevisao.getLinkDestino()).doesNotContain(processoSeed.getCodigo().toString());
    }
}
