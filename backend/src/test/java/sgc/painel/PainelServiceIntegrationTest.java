package sgc.painel;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import sgc.processo.dto.CriarProcessoReq;
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
}
