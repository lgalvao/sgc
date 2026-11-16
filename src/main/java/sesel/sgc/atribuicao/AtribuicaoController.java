package sesel.sgc.atribuicao;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import sesel.sgc.processo.Processo;
import sesel.sgc.processo.ProcessoService;
import sesel.sgc.servidor.Servidor;
import sesel.sgc.servidor.ServidorService;
import sesel.sgc.unidade.Unidade;
import sesel.sgc.unidade.UnidadeService;

import java.time.LocalDate;
import java.util.UUID;

@SuppressWarnings("SameReturnValue")
@Controller
@RequestMapping("/atribuicoes")
@RequiredArgsConstructor
public class AtribuicaoController {
    private final AtribuicaoService atribuicaoService;
    private final UnidadeService unidadeService;
    private final ServidorService servidorService;
    private final ProcessoService processoService;

    @PostMapping
    public String criarAtribuicao(
            @RequestParam UUID unidadeId,
            @RequestParam UUID servidorId,
            @RequestParam String processoId,
            @RequestParam LocalDate dataInicio,
            @RequestParam LocalDate dataTermino,
            @RequestParam String justificativa,
            Model model) {

        Unidade unidade = unidadeService.buscarPorId(unidadeId);

        Servidor servidor = servidorService.listarPorUnidade(unidadeId).stream()
                .filter(s -> s.getId().equals(servidorId)).findFirst().orElse(null);

        Processo processo = processoService.buscarPorId(UUID.fromString(processoId));

        atribuicaoService.criarAtribuicao(unidade, servidor, processo, dataInicio, dataTermino, justificativa);

        // Redireciona para a lista de atribuições da unidade
        return "redirect:/atribuicoes/unidade/%s".formatted(unidadeId);
    }

    @GetMapping("/unidade/{unidadeId}")
    public String buscarAtribuicoesPorUnidade(@PathVariable UUID unidadeId, Model model) {
        model.addAttribute("atribuicoes", atribuicaoService.buscarAtribuicoesPorUnidade(unidadeId));
        return "atribuicao/atribuicao-lista";
    }

    @GetMapping("/servidor/{servidorId}")
    public String buscarAtribuicoesPorServidor(@PathVariable UUID servidorId, Model model) {
        model.addAttribute("atribuicoes", atribuicaoService.buscarAtribuicoesPorServidor(servidorId));
        return "atribuicao/atribuicao-lista";
    }

    @PostMapping("/remover/{id}")
    public String removerAtribuicao(@PathVariable UUID id, @RequestParam UUID unidadeId) {
        atribuicaoService.removerAtribuicao(id);
        return "redirect:/atribuicoes/unidade/%s".formatted(unidadeId);
    }
}