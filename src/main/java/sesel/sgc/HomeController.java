package sesel.sgc;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import sesel.sgc.processo.ProcessoService;

@SuppressWarnings("SameReturnValue")
@Controller
@RequiredArgsConstructor
public class HomeController {
    private final ProcessoService processoService;

    @GetMapping("/")
    public String home(Model model, HttpServletRequest request) {
        model.addAttribute("currentUri", request.getRequestURI());
        model.addAttribute("processos", processoService.listarTodosProcessosDTO());

        // Adiciona contadores para o dashboard
        long totalProcessos = processoService.contarProcessos();
        model.addAttribute("totalProcessos", totalProcessos);
        model.addAttribute("processosFinalizados", processoService.contarProcessosFinalizados());

        // TODO: Implementar contagem de unidade ativas quando disponível
        model.addAttribute("unidadesAtivas", 0);

        return "index";
    }
}