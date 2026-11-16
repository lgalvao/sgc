package sesel.sgc.unidade;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@SuppressWarnings("SameReturnValue")
@Controller
@RequestMapping("/unidade")
public class UnidadeController {
    @GetMapping
    public String listarUnidades(Model model) {
        return "unidade/unidade-lista";
    }

    @GetMapping("/novo")
    public String novaUnidade(Model model) {
        return "unidade/unidade-form";
    }
}