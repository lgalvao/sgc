package sesel.sgc.processounidade;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import sesel.sgc.processo.ProcessoDto;
import sesel.sgc.processo.ProcessoService;

import java.util.UUID;

/**
 * Controller para gerenciar os processo de unidade.
 */
@Controller
@RequestMapping("/processo/{processoId}/unidade")
@RequiredArgsConstructor
public class ProcessoUnidadeController {
    private static final String MSG_ProcessoUnidade_NAO_ENCONTRADA = "ProcessoUnidade não encontrada";

    private final ProcessoService processoService;
    private final ProcessoUnidadeService processoUnidadeService;

    /**
     * Exibe os detalhes de um processoUnidade específico.
     */
    @GetMapping("/{unidadeId}")
    public String visualizarProcessoUnidade(
            @PathVariable UUID processoId,
            @PathVariable UUID unidadeId,
            @RequestParam(name = "success", required = false) String successMessage,
            @RequestParam(name = "error", required = false) String errorMessage,
            Model model) {

        try {
            // Busca o processo para exibir informações básicas
            ProcessoDto processoDto = processoService.buscarProcessoDTO(processoId);

            // Busca os detalhes do processoUnidade específico
            var processoUnidade = processoUnidadeService.buscarProcessoUnidadeDetalhada(processoId, unidadeId);
            var processoUnidadeDto = processoUnidadeService.toProcessoUnidadeDto(processoUnidade);

            model.addAttribute("processo", processoDto);
            model.addAttribute("processoUnidade", processoUnidadeDto);

            // Adiciona mensagens de sucesso/erro ao modelo, se houver
            if (successMessage != null && !successMessage.isEmpty()) {
                model.addAttribute("successMessage", successMessage);
            }
            if (errorMessage != null && !errorMessage.isEmpty()) {
                model.addAttribute("errorMessage", errorMessage);
            }

            return "processo/processo-unidade-detalhes";

        } catch (ProcessoUnidadeNaoEncontradoErro e) {
            return createErrorRedirect(processoId, MSG_ProcessoUnidade_NAO_ENCONTRADA);
        } catch (Exception e) {
            return createErrorRedirect(processoId,
                    "Erro ao carregar os detalhes do processoUnidade: %s".formatted(e.getMessage()));
        }
    }

    /**
     * Finaliza um processoUnidade no processo.
     */
    @PostMapping("/{unidadeId}/finalizar")
    public String finalizarProcessoUnidade(
            @PathVariable UUID processoId,
            @PathVariable UUID unidadeId) {
        try {
            processoUnidadeService.finalizarProcessoUnidade(processoId, unidadeId);
            return createSuccessRedirect(processoId, unidadeId, "Processo finalizado");
        } catch (ProcessoUnidadeNaoEncontradoErro e) {
            return createErrorRedirect(processoId, MSG_ProcessoUnidade_NAO_ENCONTRADA);
        } catch (Exception e) {
            return createErrorRedirect(processoId, "Erro ao finalizar processo de unidade: %s".formatted(e.getMessage()));
        }
    }

    /**
     * Reabre um processoUnidade finalizado no processo.
     */
    @PostMapping("/{unidadeId}/reabrir")
    public String reabrirProcessoUnidade(
            @PathVariable UUID processoId,
            @PathVariable UUID unidadeId) {
        try {
            processoUnidadeService.reabrirProcessoUnidade(processoId, unidadeId);
            return createSuccessRedirect(processoId, unidadeId, "Processo de unidade reaberto");
        } catch (ProcessoUnidadeNaoEncontradoErro e) {
            return createErrorRedirect(processoId, MSG_ProcessoUnidade_NAO_ENCONTRADA);
        } catch (Exception e) {
            return createErrorRedirect(processoId, "Erro ao reabrir processo de unidade: %s".formatted(e.getMessage()));
        }
    }

    /**
     * Cria uma URL de redirecionamento para a página de erro do processo.
     */
    private String createErrorRedirect(UUID processoId, String errorMessage) {
        String baseUri = getProcessoUri(processoId);
        return UriComponentsBuilder.fromUriString("redirect:" + baseUri)
                .queryParam("error", errorMessage)
                .build()
                .toUriString();
    }

    /**
     * Cria uma URL de redirecionamento para a página de detalhes do processoUnidade
     * com mensagem de sucesso.
     */
    private String createSuccessRedirect(UUID processoId, UUID unidadeId, String successMessage) {
        String baseUri = getProcessoUnidadeUri(processoId, unidadeId);
        return UriComponentsBuilder.fromUriString("redirect:" + baseUri)
                .queryParam("success", successMessage)
                .build()
                .toUriString();
    }

    /**
     * Constrói a URI base para um processo.
     */
    private String getProcessoUri(UUID processoId) {
        return UriComponentsBuilder.fromPath("/processo/{id}")
                .buildAndExpand(processoId)
                .toUriString();
    }

    /**
     * Constrói a URI base para um processoUnidade específico.
     */
    private String getProcessoUnidadeUri(UUID processoId, UUID unidadeId) {
        return UriComponentsBuilder.fromPath("/processo/{processoId}/unidade/{unidadeId}")
                .buildAndExpand(processoId, unidadeId)
                .toUriString();
    }
}
