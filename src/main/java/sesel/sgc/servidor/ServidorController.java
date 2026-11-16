package sesel.sgc.servidor;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;

@SuppressWarnings("SameReturnValue")
@Controller
@RequestMapping("/servidores")
@RequiredArgsConstructor
public class ServidorController {
    private final ServidorService servidorService;

    @GetMapping("/unidade/{unidadeId}")
    public String listarPorUnidade(@PathVariable UUID unidadeId, Model model) {
        model.addAttribute("servidores", servidorService.listarPorUnidade(unidadeId));
        return "servidor/servidor-lista";
    }
}