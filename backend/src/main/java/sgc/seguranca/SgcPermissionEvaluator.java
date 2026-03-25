package sgc.seguranca;

import lombok.*;
import lombok.extern.slf4j.*;
import org.jspecify.annotations.*;
import org.springframework.security.access.*;
import org.springframework.security.core.*;
import org.springframework.stereotype.*;
import sgc.mapa.model.*;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;
import sgc.processo.model.*;
import sgc.subprocesso.model.*;

import java.io.*;
import java.util.*;

import static sgc.organizacao.model.Perfil.*;
import static sgc.processo.model.SituacaoProcesso.*;

/**
 * Avaliador central de permissões do SGC.
 * Implementa a interface padrão do Spring Security para uso em expressões @PreAuthorize.
 * Consolida as regras de acesso baseadas na "Regra de Ouro":
 * <ol>
 *     <li>Visualização (Leitura): Hierarquia da Unidade responsável.</li>
 *     <li>Execução (Escrita): Localização atual do Subprocesso (Unidade do Usuário == Localização).</li>
 * </ol>
 *
 * @see AcaoPermissao
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SgcPermissionEvaluator implements PermissionEvaluator {
    private final SubprocessoRepo subprocessoRepo;
    private final MovimentacaoRepo movimentacaoRepo;
    private final HierarquiaService hierarquiaService;
    private final ProcessoRepo processoRepo;
    private final MapaRepo mapaRepo;
    private final AtividadeRepo atividadeRepo;

    // ── Interface Spring Security ───────────────────────────────────

    @Override
    public boolean hasPermission(Authentication authentication, Object alvo, Object permissao) {
        if (!(authentication.getPrincipal() instanceof Usuario usuario)) {
            return false;
        }

        if (alvo instanceof Collection<?> colecao) {
            return colecao.stream().allMatch(item -> hasPermission(authentication, item, permissao));
        }

        AcaoPermissao acao = resolverAcao((String) permissao);
        return switch (alvo) {
            case Subprocesso sp -> verificarSubprocesso(usuario, sp, acao);
            case Processo p -> verificarProcesso(usuario, p, acao);
            case Mapa m -> verificarSubprocesso(usuario, m.getSubprocesso(), acao);
            case Atividade a -> verificarSubprocesso(usuario, a.getMapa().getSubprocesso(), acao);
            default -> false;
        };
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable codigoAlvo, String tipoAlvo, Object permissao) {
        if (!(authentication.getPrincipal() instanceof Usuario usuario)) {
            return false;
        }

        if (codigoAlvo instanceof Collection<?> colecao) {
            return colecao.stream().allMatch(codigo -> hasPermission(authentication, (Serializable) codigo, tipoAlvo, permissao));
        }

        AcaoPermissao acao = resolverAcao((String) permissao);

        return switch (tipoAlvo) {
            case "Subprocesso" -> subprocessoRepo.buscarPorCodigoComMapaEAtividades((Long) codigoAlvo)
                    .map(sp -> verificarSubprocesso(usuario, sp, acao))
                    .orElse(false);
            case "Processo" -> processoRepo.buscarPorCodigoComParticipantes((Long) codigoAlvo)
                    .map(p -> verificarProcesso(usuario, p, acao))
                    .orElse(false);
            case "Mapa" -> mapaRepo.findById((Long) codigoAlvo)
                    .map(m -> verificarSubprocesso(usuario, m.getSubprocesso(), acao))
                    .orElse(false);
            case "Atividade" -> atividadeRepo.findById((Long) codigoAlvo)
                    .map(a -> verificarSubprocesso(usuario, a.getMapa().getSubprocesso(), acao))
                    .orElse(false);
            default -> false;
        };
    }

    // ── API pública para facades/services ───────────────────────────

    public boolean verificarPermissao(@Nullable Usuario usuario, @Nullable Object alvo, AcaoPermissao acao) {
        if (usuario == null) return false;

        return switch (alvo) {
            case Collection<?> colecao -> colecao.stream().allMatch(item -> verificarPermissao(usuario, item, acao));
            case Subprocesso sp -> verificarSubprocesso(usuario, sp, acao);
            case Processo processo -> verificarProcesso(usuario, processo, acao);
            case null, default -> false;
        };
    }

    // ── Verificação de Subprocesso ──────────────────────────────────

    private boolean verificarSubprocesso(Usuario usuario, Subprocesso sp, AcaoPermissao acao) {
        Perfil perfil = usuario.getPerfilAtivo();
        Processo processo = sp.getProcesso();

        // Caso especial: importação permite consultar processos finalizados
        if (acao == AcaoPermissao.CONSULTAR_PARA_IMPORTACAO && perfil == CHEFE) {
            return processo.getSituacao() == FINALIZADO || verificarHierarquia(usuario, sp.getUnidade());
        }

        // Processo finalizado: bloqueia escrita, permite leitura
        if (processo.getSituacao() == FINALIZADO) {
            return !acao.dependeLocalizacao() && acao.permitePerfil(perfil);
        }

        // Ações de leitura: verificam hierarquia (exceto admin, que vê tudo)
        if (!acao.dependeLocalizacao()) {
            if (perfil == ADMIN) return true;
            if (acao == AcaoPermissao.VERIFICAR_IMPACTOS) return true; // controle feito no serviço
            return verificarHierarquia(usuario, sp.getUnidade());
        }

        // Ações de escrita: verificam perfil + localização
        if (!acao.permitePerfil(perfil)) {
            return false;
        }

        return verificarLocalizacao(usuario, sp);
    }

    // ── Verificação de Processo ──────────────────────────────────────

    private boolean verificarProcesso(Usuario usuario, Processo processo, AcaoPermissao acao) {
        Perfil perfil = usuario.getPerfilAtivo();

        if (acao == AcaoPermissao.VISUALIZAR_PROCESSO) return true;
        if (acao == AcaoPermissao.FINALIZAR_PROCESSO) {
            return perfil == ADMIN && processo.getSituacao() != FINALIZADO;
        }

        return acao.permitePerfil(perfil);
    }

    // ── Verificação de Hierarquia ───────────────────────────────────

    private boolean verificarHierarquia(Usuario usuario, Unidade unidadeAlvo) {
        Perfil perfil = usuario.getPerfilAtivo();

        // Chefe e Servidor veem apenas sua unidade
        if (perfil == CHEFE || perfil == SERVIDOR) {
            return Objects.equals(usuario.getUnidadeAtivaCodigo(), unidadeAlvo.getCodigo());
        }

        // Gestor vê sua unidade e subordinadas
        if (perfil == GESTOR) {
            Unidade unidadeUsuario = Unidade.builder().codigo(usuario.getUnidadeAtivaCodigo()).build();
            return hierarquiaService.ehMesmaOuSubordinada(unidadeAlvo, unidadeUsuario);
        }

        log.info("Acesso negado por hierarquia para {} (Perfil: {}, Unidade ativa: {}). Unidade alvo: {}.",
                mascarar(usuario.getTituloEleitoral()), perfil, usuario.getUnidadeAtivaCodigo(), unidadeAlvo.getCodigo());
        return false;
    }

    // ── Verificação de Localização ──────────────────────────────────

    private boolean verificarLocalizacao(Usuario usuario, Subprocesso sp) {
        Unidade localizacao = obterUnidadeLocalizacao(sp);
        boolean permitido = Objects.equals(usuario.getUnidadeAtivaCodigo(), localizacao.getCodigo());

        if (!permitido) {
            log.info("Acesso negado por localização. Usuário: {} (Unidade ativa: {}). Subprocesso {} localizado em {}.",
                    mascarar(usuario.getTituloEleitoral()),
                    usuario.getUnidadeAtivaCodigo(),
                    sp.getCodigo(),
                    localizacao.getCodigo());
        }
        return permitido;
    }

    private Unidade obterUnidadeLocalizacao(Subprocesso sp) {
        if (sp.getLocalizacaoAtual() != null) return sp.getLocalizacaoAtual();

        Unidade localizacao = movimentacaoRepo.findFirstBySubprocessoCodigoOrderByDataHoraDesc(sp.getCodigo())
                .map(Movimentacao::getUnidadeDestino)
                .orElse(sp.getUnidade());

        sp.setLocalizacaoAtual(localizacao);
        return localizacao;
    }

    // ── Utilitário ──────────────────────────────────────────────────

    private String mascarar(String valor) {
        if (valor.length() <= 4) return "***";
        return "***" + valor.substring(valor.length() - 4);
    }

    private AcaoPermissao resolverAcao(String permissao) {
        try {
            return AcaoPermissao.valueOf(permissao);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Ação de permissão desconhecida: '%s'. Verifique se o valor está registrado em AcaoPermissao.".formatted(permissao), e);
        }
    }
}
