package sesel.sgc.seguranca;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@SuppressWarnings("SameReturnValue")
@Controller
public class LoginController {
    @GetMapping("/login")
    public String login(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model modelo) {

        if (error != null) modelo.addAttribute("error", "Usuário ou senha inválidos");
        if (logout != null) modelo.addAttribute("message", "Você saiu do sistema.");

        return "login";
    }

    @GetMapping("/acesso-negado")
    public String acessoNegado() {
        return "error/403";
    }
}
