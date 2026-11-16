package sesel.sgc.mapa.atividades;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import sesel.sgc.mapa.ValidacaoErro;
import sesel.sgc.mapa.conhecimentos.ConhecimentoTipo;
import sesel.sgc.processounidade.ProcessoUnidadeService;

import java.util.*;

import static sesel.sgc.mapa.MapaUnidadeSituacao.FINALIZADA;

/**
 * Controlador para gerenciar as operações relacionadas a Atividades.
 */
@Controller
@RequestMapping("/processo-unidade/{processoUnidadeId}/atividades")
@RequiredArgsConstructor
@Slf4j
public class AtividadeController {
    private final AtividadeService atividadeService;
    private final ProcessoUnidadeService processoUnidadeService;

    /**
     * Exibe o formulário para cadastro/edição de atividades de um processoUnidade.
     */
    @GetMapping
    public String exibirFormularioAtividades(@PathVariable UUID processoUnidadeId, Model model, HttpSession session,
                                             @ModelAttribute("success") Optional<String> success, @ModelAttribute("error") Optional<String> error) {
        try {
            var processoUnidade = processoUnidadeService.buscarProcessoUnidadePorId(processoUnidadeId);
            if (processoUnidade == null) {
                throw new ValidacaoErro("Processo de unidade não encontrado com o ID: " + processoUnidadeId);
            }

            // Busca as atividades existentes
            List<AtividadeDto> atividades = atividadeService.buscarAtividadesPorProcessoUnidade(processoUnidadeId);
            log.info("Atividades retornadas: {}", atividades);

            // Get activities in 'add knowledge' mode from session
            @SuppressWarnings("unchecked")
            Set<Integer> atividadesEmModoAdicionar = (Set<Integer>) session.getAttribute("atividadesEmModoAdicionar");
            if (atividadesEmModoAdicionar == null) {
                atividadesEmModoAdicionar = new HashSet<>();
                session.setAttribute("atividadesEmModoAdicionar", atividadesEmModoAdicionar);
            }

            // Add the indices to the model for the template
            model.addAttribute("atividadesEmModoAdicionar", atividadesEmModoAdicionar);

            // Cria o formulário com as atividades existentes
            CadastroAtividadesDto atividadeForm = new CadastroAtividadesDto();
            atividadeForm.setProcessoUnidadeId(processoUnidadeId);
            atividadeForm.setAtividades(atividades);

            model.addAttribute("atividadeForm", atividadeForm);
            model.addAttribute("processoUnidadeId", processoUnidadeId);
            model.addAttribute("processoId", processoUnidade.getProcesso().getId());

            boolean somenteLeitura = processoUnidade.getSituacao() == FINALIZADA;
            model.addAttribute("somenteLeitura", somenteLeitura);

            // Add flash messages if present
            success.ifPresent(msg -> model.addAttribute("successMessage", msg));
            error.ifPresent(msg -> model.addAttribute("errorMessage", msg));

            return "mapa/atividades-form";

        } catch (Exception e) {
            log.error("Erro ao carregar formulário de atividades: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Erro ao carregar o formulário: " + e.getMessage());
            return "redirect:/processo-unidade/%s/atividades".formatted(processoUnidadeId);
        }
    }

    /**
     * Processa o formulário de cadastro/edição de atividades.
     */
    @GetMapping("/adicionar-conhecimento")
    public String iniciarAdicionarConhecimento(@PathVariable UUID processoUnidadeId, @RequestParam("index") int index,
                                               HttpSession session, RedirectAttributes redirectAttributes) {

        try {
            // Get or create the set of activity indices in 'add knowledge' mode
            Set<Integer> atividadesEmModoAdicionar = getAtividadesEmModoAdicionar(session);

            // Add the current activity index to the set
            atividadesEmModoAdicionar.add(index);
            log.info("Atividade {} em modo de adicionar conhecimento", index);

            redirectAttributes.addFlashAttribute("success", "Preencha o conhecimento abaixo");
        } catch (Exception e) {
            log.error("Erro ao processar o índice da atividade: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Erro ao adicionar conhecimento: " + e.getMessage());
        }

        return "redirect:/processo-unidade/%s/atividades".formatted(processoUnidadeId);
    }

    @GetMapping("/cancelar-adicionar-conhecimento")
    public String cancelarAdicionarConhecimento(@PathVariable UUID processoUnidadeId, @RequestParam("index") int index,
                                                HttpSession session, RedirectAttributes redirectAttributes) {

        try {
            // Get the set of activity indices in 'add knowledge' mode
            Set<Integer> atividadesEmModoAdicionar = getAtividadesEmModoAdicionar(session);

            // Remove the current activity index from the set
            atividadesEmModoAdicionar.remove(index);
            log.info("Atividade {} removida do modo de adicionar conhecimento", index);

            redirectAttributes.addFlashAttribute("info", "Adição de conhecimento cancelada");
        } catch (Exception e) {
            log.error("Erro ao cancelar a adição de conhecimento: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error",
                    "Erro ao cancelar a adição de conhecimento: " + e.getMessage());
        }

        return "redirect:/processo-unidade/%s/atividades".formatted(processoUnidadeId);
    }

    @SuppressWarnings("unchecked")
    private Set<Integer> getAtividadesEmModoAdicionar(HttpSession session) {
        Set<Integer> atividadesEmModoAdicionar = (Set<Integer>) session.getAttribute("atividadesEmModoAdicionar");
        if (atividadesEmModoAdicionar == null) {
            atividadesEmModoAdicionar = new HashSet<>();
            session.setAttribute("atividadesEmModoAdicionar", atividadesEmModoAdicionar);
        }
        return atividadesEmModoAdicionar;
    }

    @PostMapping
    public String salvarAtividades(@PathVariable UUID processoUnidadeId,
                                   @Valid @ModelAttribute("atividadeForm") CadastroAtividadesDto atividadeForm, BindingResult result,
                                   RedirectAttributes redirectAttributes, Model model,
                                   @RequestParam(value = "acao", required = false) String acao, HttpServletRequest request) {

        // Se houver erros de validação, reexibe o formulário imediatamente
        if (result.hasErrors()) {
            return exibirFormularioAtividades(processoUnidadeId, model, request.getSession(), Optional.empty(),
                    Optional.of("Erro de validação no formulário."));
        }

        if (atividadeForm.getAtividades() != null) {
            try {
                atividadeService.cadastrarAtividades(atividadeForm);
            } catch (Exception e) {
                log.error("Erro ao salvar atividades: {}", e.getMessage(), e);
                redirectAttributes.addFlashAttribute("error", "Erro ao salvar as alterações: " + e.getMessage());
            }
        }

        if (atividadeForm.getAtividades() != null) {
            for (AtividadeDto atividade : atividadeForm.getAtividades()) {
                if (atividade.getConhecimentos() == null) {
                    atividade.setConhecimentos(new ArrayList<>());
                }
            }
        }
        if ("iniciarAdicionarAtividade".equals(acao)) {
            model.addAttribute("adicionandoAtividade", true);
            HttpSession session = request.getSession();
            return exibirFormularioAtividades(processoUnidadeId, model, session, Optional.empty(), Optional.empty());
        }

        if ("salvarNovaAtividade".equals(acao)) {
            if (atividadeForm.getNovaAtividadeDescricao() != null
                    && !atividadeForm.getNovaAtividadeDescricao().isBlank()) {
                AtividadeDto nova = new AtividadeDto();
                nova.setDescricao(atividadeForm.getNovaAtividadeDescricao());
                nova.setConhecimentos(new ArrayList<>());
                if (atividadeForm.getAtividades() == null) {
                    atividadeForm.setAtividades(new ArrayList<>());
                }
                atividadeForm.getAtividades().add(nova);
                atividadeForm.setNovaAtividadeDescricao("");
            }
            model.addAttribute("adicionandoAtividade", false);
            atividadeService.cadastrarAtividades(atividadeForm);
            return "redirect:/processo-unidade/%s/atividades".formatted(processoUnidadeId);
        }
        if (acao != null && acao.startsWith("iniciarAdicionarConhecimento-")) {

            String[] partes = acao.split("-");
            if (partes.length > 1) {
                try {
                    int index = Integer.parseInt(partes[1]);
                    List<AtividadeDto> atividadesAtuais = atividadeService
                            .buscarAtividadesPorProcessoUnidade(processoUnidadeId);

                    if (atividadesAtuais == null || index >= atividadesAtuais.size()) {
                        log.error("ERRO: Índice {} fora dos limites (tamanho: {})", index,
                                atividadesAtuais != null ? atividadesAtuais.size() : 0);
                        redirectAttributes.addFlashAttribute("error", "Índice de atividade inválido");
                    } else {
                        AtividadeDto atividadeAtual = atividadesAtuais.get(index);
                        log.info("Atividade encontrada: {} - {}", index, atividadeAtual.getDescricao());

                        Set<Integer> atividadesEmModoAdicionar = getAtividadesEmModoAdicionar(request.getSession());
                        atividadesEmModoAdicionar.add(index);
                        log.info("Atividade {} adicionada ao modo de adicionar conhecimento", index);

                        redirectAttributes.addFlashAttribute("success", "Preencha o conhecimento abaixo");
                        return "redirect:/processo-unidade/%s/atividades".formatted(processoUnidadeId);
                    }
                } catch (Exception e) {
                    log.error("Erro ao processar o índice da atividade: {}", e.getMessage(), e);
                    redirectAttributes.addFlashAttribute("error", "Erro ao adicionar conhecimento: " + e.getMessage());
                }
            }
            return "redirect:/processo-unidade/%s/atividades".formatted(processoUnidadeId);
        }
        if (acao != null && acao.startsWith("adicionarConhecimento-")) {
            String[] partes = acao.split("-");
            if (partes.length > 1) {
                try {
                    int index = Integer.parseInt(partes[1]);
                    if (atividadeForm.getAtividades() != null && index < atividadeForm.getAtividades().size()) {
                        AtividadeDto atividade = atividadeForm.getAtividades().get(index);
                        if (atividade.getNovoConhecimento() != null && !atividade.getNovoConhecimento().isBlank()) {
                            AtividadeDto.ConhecimentoDto novo = new AtividadeDto.ConhecimentoDto();
                            novo.setDescricao(atividade.getNovoConhecimento().trim());
                            novo.setTipo(ConhecimentoTipo.TECNICO); // ou conforme o formulário
                            if (atividade.getConhecimentos() == null) {
                                atividade.setConhecimentos(new ArrayList<>());
                            }
                            atividade.getConhecimentos().add(novo);
                            atividade.setNovoConhecimento("");
                        }
                    }
                } catch (NumberFormatException ignored) {
                }
            }
            atividadeService.cadastrarAtividades(atividadeForm);
            return "redirect:/processo-unidade/%s/atividades".formatted(processoUnidadeId);
        }
        if (acao != null && acao.startsWith("removerConhecimento-")) {
            String[] partes = acao.split("-");
            if (partes.length > 2) {
                try {
                    int indexAtividade = Integer.parseInt(partes[1]);
                    int indexConhecimento = Integer.parseInt(partes[2]);
                    if (atividadeForm.getAtividades() != null
                            && indexAtividade < atividadeForm.getAtividades().size()) {
                        AtividadeDto atividade = atividadeForm.getAtividades().get(indexAtividade);
                        if (atividade.getConhecimentos() != null
                                && indexConhecimento < atividade.getConhecimentos().size()) {
                            atividade.getConhecimentos().remove(indexConhecimento);
                        }
                    }
                } catch (NumberFormatException ignored) {
                }
            }
            return "redirect:/processo-unidade/%s/atividades".formatted(processoUnidadeId);
        }
        if (acao != null && acao.startsWith("removerAtividade-")) {
            String[] partes = acao.split("-");
            if (partes.length > 1) {
                try {
                    int index = Integer.parseInt(partes[1]);
                    if (atividadeForm.getAtividades() != null && index < atividadeForm.getAtividades().size()) {
                        AtividadeDto atividadeRemovida = atividadeForm.getAtividades().remove(index);
                        log.info("Removendo atividade: {}", atividadeRemovida.getDescricao());
                        try {
                            atividadeService.cadastrarAtividades(atividadeForm);
                            redirectAttributes.addFlashAttribute("success", "Atividade removida com sucesso!");
                        } catch (ValidacaoErro e) {
                            // Se ocorrer algum erro ao salvar, mostra a mensagem de erro
                            redirectAttributes.addFlashAttribute("error", e.getMessage());
                        }
                    }
                } catch (NumberFormatException ignored) {
                    redirectAttributes.addFlashAttribute("error", "Índice de atividade inválido");
                }
            }
            return "redirect:/processo-unidade/%s/atividades".formatted(processoUnidadeId);
        }
        try {
            atividadeService.cadastrarAtividades(atividadeForm);
            return "redirect:/processo-unidade/%s/atividades".formatted(processoUnidadeId);
        } catch (ValidacaoErro e) {
            model.addAttribute("error", e.getMessage());
            return exibirFormularioAtividades(processoUnidadeId, model, request.getSession(), Optional.empty(),
                    Optional.of(e.getMessage()));
        }
    }
}
