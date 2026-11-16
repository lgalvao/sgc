package sesel.sgc.atribuicao;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import sesel.sgc.processo.ProcessoService;
import sesel.sgc.unidade.UnidadeService;

@SuppressWarnings("SameReturnValue")
@Controller
@RequestMapping("/atribuicao")
@RequiredArgsConstructor
public class AtribuicaoViewController {
    private final UnidadeService unidadeService;
    private final ProcessoService processoService;

    @GetMapping("/atribuicao-temporaria")
    public String formAtribuicaoTemporaria(Model model) {
        model.addAttribute("unidades", unidadeService.listarTodas());
        model.addAttribute("processos", processoService.listarTodosProcessosDTO());
        return "processo/atrib-temporaria";
    }
}