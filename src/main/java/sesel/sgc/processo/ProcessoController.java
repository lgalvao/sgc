package sesel.sgc.processo;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import sesel.sgc.unidade.UnidadeService;

import java.util.UUID;

@SuppressWarnings("SameReturnValue")
@Controller
@RequestMapping("/processo")
@RequiredArgsConstructor
@Slf4j
public class ProcessoController {
    private final ProcessoService processoService;
    private final UnidadeService unidadeService;

    @GetMapping
    public String listarProcessos(Model model) {
        model.addAttribute("processos", processoService.listarTodosProcessosDTO());
        return "processo/processo-lista";
    }

    @GetMapping("/novo")
    public String novoProcesso(Model model) {
        model.addAttribute("processo", new Processo());
        model.addAttribute("unidades", unidadeService.listarHierarquicamente());
        return "processo/processo-form";
    }

    @PostMapping
    public String criarProcesso(@Valid @ModelAttribute Processo processo, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("unidades", unidadeService.listarHierarquicamente());
            return "processo/processo-form";
        }
        processoService.criarProcesso(processo);
        return "redirect:/";
    }

    @GetMapping("/{id}")
    public String visualizarProcesso(@PathVariable("id") UUID id, Model model) {
        ProcessoDto processo = processoService.buscarProcessoDTO(id);
        model.addAttribute("processo", processo);
        return "processo/processo-detalhes";
    }

    @ExceptionHandler(ProcessoNaoEncontradoErro.class)
    public ModelAndView handleProcessoNaoEncontrado(ProcessoNaoEncontradoErro ex) {
        ModelAndView mav = new ModelAndView("error/404");
        mav.addObject("mensagem", ex.getMessage());
        mav.setStatus(org.springframework.http.HttpStatus.NOT_FOUND);
        return mav;
    }
}
