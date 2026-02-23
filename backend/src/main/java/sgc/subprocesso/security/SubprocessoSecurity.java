package sgc.subprocesso.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import sgc.organizacao.OrganizacaoFacade;
import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.organizacao.service.HierarquiaService;
import sgc.processo.model.SituacaoProcesso;
import sgc.seguranca.Acao;
import sgc.subprocesso.model.MovimentacaoRepo;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.dto.PermissoesSubprocessoDto;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;

import java.util.*;

import static sgc.organizacao.model.Perfil.*;
import static sgc.seguranca.Acao.*;
import static sgc.subprocesso.model.SituacaoSubprocesso.*;

/**
 * Bean de segurança consolidado para Subprocessos.
 * Fornece lógica de autorização baseada em perfis, situação e hierarquia,
 * substituindo o framework customizado sgc.seguranca.acesso.
 */
@Component("subprocessoSecurity")
@RequiredArgsConstructor
@Slf4j
public class SubprocessoSecurity {

    private final SubprocessoRepo subprocessoRepo;
    private final OrganizacaoFacade organizacaoFacade;
    private final HierarquiaService hierarquiaService;
    private final MovimentacaoRepo movimentacaoRepo;

    /**
     * Requisitos de hierarquia para validação.
     */
    private enum RequisitoHierarquia {
        NENHUM, MESMA_UNIDADE, MESMA_OU_SUBORDINADA, SUPERIOR_IMEDIATA, TITULAR_UNIDADE
    }

    private record RegrasAcao(
            EnumSet<Perfil> perfisPermitidos,
            EnumSet<SituacaoSubprocesso> situacoesPermitidas,
            RequisitoHierarquia requisitoHierarquia) {
    }

    private static final Map<Acao, RegrasAcao> REGRAS = Map.ofEntries(
            // ========== CRUD ==========
            Map.entry(LISTAR_SUBPROCESSOS, new RegrasAcao(EnumSet.of(ADMIN), EnumSet.allOf(SituacaoSubprocesso.class), RequisitoHierarquia.NENHUM)),
            Map.entry(VISUALIZAR_SUBPROCESSO, new RegrasAcao(EnumSet.of(ADMIN, GESTOR, CHEFE, SERVIDOR), EnumSet.allOf(SituacaoSubprocesso.class), RequisitoHierarquia.MESMA_OU_SUBORDINADA)),
            Map.entry(CONSULTAR_PARA_IMPORTACAO, new RegrasAcao(EnumSet.of(ADMIN, GESTOR, CHEFE), EnumSet.allOf(SituacaoSubprocesso.class), RequisitoHierarquia.NENHUM)),
            Map.entry(CRIAR_SUBPROCESSO, new RegrasAcao(EnumSet.of(ADMIN), EnumSet.allOf(SituacaoSubprocesso.class), RequisitoHierarquia.NENHUM)),
            Map.entry(EDITAR_SUBPROCESSO, new RegrasAcao(EnumSet.of(ADMIN), EnumSet.allOf(SituacaoSubprocesso.class), RequisitoHierarquia.NENHUM)),
            Map.entry(EXCLUIR_SUBPROCESSO, new RegrasAcao(EnumSet.of(ADMIN), EnumSet.allOf(SituacaoSubprocesso.class), RequisitoHierarquia.NENHUM)),
            Map.entry(ALTERAR_DATA_LIMITE, new RegrasAcao(EnumSet.of(ADMIN), EnumSet.allOf(SituacaoSubprocesso.class), RequisitoHierarquia.NENHUM)),
            Map.entry(REABRIR_CADASTRO, new RegrasAcao(EnumSet.of(ADMIN), EnumSet.allOf(SituacaoSubprocesso.class), RequisitoHierarquia.NENHUM)),
            Map.entry(REABRIR_REVISAO, new RegrasAcao(EnumSet.of(ADMIN), EnumSet.allOf(SituacaoSubprocesso.class), RequisitoHierarquia.NENHUM)),
            Map.entry(ENVIAR_LEMBRETE_PROCESSO, new RegrasAcao(EnumSet.of(ADMIN), EnumSet.allOf(SituacaoSubprocesso.class), RequisitoHierarquia.NENHUM)),

            // ========== CADASTRO (Atua sobre Mapeamento e Revisão quando a ação é genérica) ==========
            Map.entry(EDITAR_CADASTRO, new RegrasAcao(EnumSet.of(CHEFE), EnumSet.of(NAO_INICIADO, MAPEAMENTO_CADASTRO_EM_ANDAMENTO, REVISAO_CADASTRO_EM_ANDAMENTO), RequisitoHierarquia.MESMA_UNIDADE)),
            Map.entry(DISPONIBILIZAR_CADASTRO, new RegrasAcao(EnumSet.of(CHEFE), EnumSet.of(MAPEAMENTO_CADASTRO_EM_ANDAMENTO), RequisitoHierarquia.TITULAR_UNIDADE)),
            Map.entry(DEVOLVER_CADASTRO, new RegrasAcao(EnumSet.of(ADMIN, GESTOR), EnumSet.of(MAPEAMENTO_CADASTRO_DISPONIBILIZADO, REVISAO_CADASTRO_DISPONIBILIZADA), RequisitoHierarquia.MESMA_UNIDADE)),
            Map.entry(ACEITAR_CADASTRO, new RegrasAcao(EnumSet.of(GESTOR), EnumSet.of(MAPEAMENTO_CADASTRO_DISPONIBILIZADO, REVISAO_CADASTRO_DISPONIBILIZADA), RequisitoHierarquia.MESMA_UNIDADE)),
            Map.entry(HOMOLOGAR_CADASTRO, new RegrasAcao(EnumSet.of(ADMIN), EnumSet.of(MAPEAMENTO_CADASTRO_DISPONIBILIZADO, REVISAO_CADASTRO_DISPONIBILIZADA), RequisitoHierarquia.MESMA_UNIDADE)),

            // ========== REVISÃO CADASTRO (Ações específicas de revisão) ==========
            Map.entry(EDITAR_REVISAO_CADASTRO, new RegrasAcao(EnumSet.of(CHEFE), EnumSet.of(NAO_INICIADO, REVISAO_CADASTRO_EM_ANDAMENTO), RequisitoHierarquia.MESMA_UNIDADE)),
            Map.entry(DISPONIBILIZAR_REVISAO_CADASTRO, new RegrasAcao(EnumSet.of(CHEFE), EnumSet.of(REVISAO_CADASTRO_EM_ANDAMENTO), RequisitoHierarquia.TITULAR_UNIDADE)),
            Map.entry(DEVOLVER_REVISAO_CADASTRO, new RegrasAcao(EnumSet.of(ADMIN, GESTOR), EnumSet.of(REVISAO_CADASTRO_DISPONIBILIZADA), RequisitoHierarquia.MESMA_UNIDADE)),
            Map.entry(ACEITAR_REVISAO_CADASTRO, new RegrasAcao(EnumSet.of(GESTOR), EnumSet.of(REVISAO_CADASTRO_DISPONIBILIZADA), RequisitoHierarquia.MESMA_UNIDADE)),
            Map.entry(HOMOLOGAR_REVISAO_CADASTRO, new RegrasAcao(EnumSet.of(ADMIN), EnumSet.of(REVISAO_CADASTRO_DISPONIBILIZADA), RequisitoHierarquia.MESMA_UNIDADE)),

            // ========== MAPA ==========
            Map.entry(VISUALIZAR_MAPA, new RegrasAcao(EnumSet.of(ADMIN, GESTOR, CHEFE, SERVIDOR), EnumSet.allOf(SituacaoSubprocesso.class), RequisitoHierarquia.MESMA_OU_SUBORDINADA)),
            Map.entry(EDITAR_MAPA, new RegrasAcao(EnumSet.of(ADMIN), EnumSet.of(NAO_INICIADO, MAPEAMENTO_CADASTRO_EM_ANDAMENTO, MAPEAMENTO_CADASTRO_HOMOLOGADO, MAPEAMENTO_MAPA_CRIADO, MAPEAMENTO_MAPA_COM_SUGESTOES, REVISAO_CADASTRO_EM_ANDAMENTO, REVISAO_CADASTRO_HOMOLOGADA, REVISAO_MAPA_AJUSTADO, REVISAO_MAPA_COM_SUGESTOES, DIAGNOSTICO_AUTOAVALIACAO_EM_ANDAMENTO), RequisitoHierarquia.MESMA_UNIDADE)),
            Map.entry(DISPONIBILIZAR_MAPA, new RegrasAcao(EnumSet.of(ADMIN), EnumSet.of(MAPEAMENTO_CADASTRO_HOMOLOGADO, MAPEAMENTO_MAPA_CRIADO, MAPEAMENTO_MAPA_COM_SUGESTOES, REVISAO_CADASTRO_HOMOLOGADA, REVISAO_MAPA_AJUSTADO, REVISAO_MAPA_COM_SUGESTOES), RequisitoHierarquia.MESMA_UNIDADE)),
            Map.entry(APRESENTAR_SUGESTOES, new RegrasAcao(EnumSet.of(CHEFE), EnumSet.of(MAPEAMENTO_MAPA_DISPONIBILIZADO, REVISAO_MAPA_DISPONIBILIZADO), RequisitoHierarquia.MESMA_UNIDADE)),
            Map.entry(VALIDAR_MAPA, new RegrasAcao(EnumSet.of(CHEFE), EnumSet.of(MAPEAMENTO_MAPA_DISPONIBILIZADO, REVISAO_MAPA_DISPONIBILIZADO), RequisitoHierarquia.MESMA_UNIDADE)),
            Map.entry(DEVOLVER_MAPA, new RegrasAcao(EnumSet.of(ADMIN, GESTOR), EnumSet.of(MAPEAMENTO_MAPA_COM_SUGESTOES, MAPEAMENTO_MAPA_VALIDADO, REVISAO_MAPA_COM_SUGESTOES, REVISAO_MAPA_VALIDADO), RequisitoHierarquia.MESMA_UNIDADE)),
            Map.entry(ACEITAR_MAPA, new RegrasAcao(EnumSet.of(GESTOR), EnumSet.of(MAPEAMENTO_MAPA_COM_SUGESTOES, MAPEAMENTO_MAPA_VALIDADO, REVISAO_MAPA_COM_SUGESTOES, REVISAO_MAPA_VALIDADO), RequisitoHierarquia.MESMA_UNIDADE)),
            Map.entry(HOMOLOGAR_MAPA, new RegrasAcao(EnumSet.of(ADMIN), EnumSet.of(MAPEAMENTO_MAPA_COM_SUGESTOES, MAPEAMENTO_MAPA_VALIDADO, REVISAO_MAPA_COM_SUGESTOES, REVISAO_MAPA_VALIDADO), RequisitoHierarquia.MESMA_UNIDADE)),
            Map.entry(AJUSTAR_MAPA, new RegrasAcao(EnumSet.of(ADMIN), EnumSet.of(REVISAO_CADASTRO_HOMOLOGADA, REVISAO_MAPA_AJUSTADO), RequisitoHierarquia.MESMA_UNIDADE))
    );

    private static final EnumSet<SituacaoSubprocesso> SITUACOES_VER_IMPACTO_ADMIN = EnumSet.of(REVISAO_CADASTRO_DISPONIBILIZADA, REVISAO_CADASTRO_HOMOLOGADA, REVISAO_MAPA_AJUSTADO);
    private static final EnumSet<SituacaoSubprocesso> SITUACOES_VER_IMPACTO_CHEFE = EnumSet.of(NAO_INICIADO, REVISAO_CADASTRO_EM_ANDAMENTO);

    public boolean canExecute(Usuario usuario, Acao acao, Subprocesso sp) {
        if (usuario == null || sp == null) return false;
        return verificarRegras(usuario, acao, sp);
    }

    public boolean canExecute(Long codSubprocesso, String acaoStr) {
        if (codSubprocesso == null) return false;
        Usuario usuario = getUsuarioAutenticado();
        if (usuario == null) return false;

        Subprocesso sp = subprocessoRepo.findById(codSubprocesso).orElse(null);
        if (sp == null) return false;

        Acao acao = Acao.valueOf(acaoStr);
        return canExecute(usuario, acao, sp);
    }

    public boolean canExecuteBulk(List<Long> codigos, String acao) {
        if (codigos == null || codigos.isEmpty()) return true;
        return codigos.stream().allMatch(cod -> canExecute(cod, acao));
    }

    public boolean canView(Long codSubprocesso) {
        return canExecute(codSubprocesso, "VISUALIZAR_SUBPROCESSO");
    }

    public boolean canView(Long codProcesso, String siglaUnidade) {
        if (codProcesso == null || siglaUnidade == null) return false;
        Usuario usuario = getUsuarioAutenticado();
        if (usuario == null) return false;

        Unidade unidade = organizacaoFacade.buscarEntidadePorSigla(siglaUnidade);
        return subprocessoRepo.findByProcessoCodigoAndUnidadeCodigo(codProcesso, unidade.getCodigo())
                .map(sp -> verificarRegras(usuario, Acao.VISUALIZAR_SUBPROCESSO, sp))
                .orElse(false);
    }

    private boolean verificarRegras(Usuario usuario, Acao acao, Subprocesso sp) {
        if (acao == VERIFICAR_IMPACTOS) return canExecuteVerificarImpactos(usuario, sp);

        RegrasAcao regras = REGRAS.get(acao);
        if (regras == null) return false;

        if (!regras.perfisPermitidos.contains(usuario.getPerfilAtivo())) return false;

        // Regra especial para importação de histórico: qualquer um pode consultar processos finalizados para este fim
        if (acao == CONSULTAR_PARA_IMPORTACAO && sp.getProcesso().getSituacao() == SituacaoProcesso.FINALIZADO) {
            return true;
        }

        if (!regras.situacoesPermitidas.contains(sp.getSituacao())) return false;

        // Administrador visualiza tudo (Leitura), mas para escrita segue regra de localização
        if (usuario.getPerfilAtivo() == Perfil.ADMIN && !isAcaoEscrita(acao)) {
            return true;
        }

        Unidade unidadeVerificacao = isAcaoEscrita(acao) ? obterUnidadeLocalizacao(sp) : sp.getUnidade();
        return verificaHierarquia(usuario, unidadeVerificacao, regras.requisitoHierarquia);
    }

    private boolean canExecuteVerificarImpactos(Usuario usuario, Subprocesso sp) {
        if (sp.getProcesso().getTipo() != sgc.processo.model.TipoProcesso.REVISAO) return false;

        SituacaoSubprocesso situacao = sp.getSituacao();
        Unidade localizacao = obterUnidadeLocalizacao(sp);

        if (usuario.getPerfilAtivo() == ADMIN && SITUACOES_VER_IMPACTO_ADMIN.contains(situacao)) return true;
        if (usuario.getPerfilAtivo() == GESTOR && situacao == REVISAO_CADASTRO_DISPONIBILIZADA) {
            return hierarquiaService.isMesmaOuSubordinada(localizacao, Unidade.builder().codigo(usuario.getUnidadeAtivaCodigo()).build());
        }
        if (usuario.getPerfilAtivo() == CHEFE && SITUACOES_VER_IMPACTO_CHEFE.contains(situacao)) {
            return Objects.equals(usuario.getUnidadeAtivaCodigo(), localizacao.getCodigo());
        }
        return false;
    }

    private boolean isAcaoEscrita(Acao acao) {
        return EnumSet.of(EDITAR_CADASTRO, DISPONIBILIZAR_CADASTRO, DEVOLVER_CADASTRO, ACEITAR_CADASTRO, HOMOLOGAR_CADASTRO,
                EDITAR_REVISAO_CADASTRO, DISPONIBILIZAR_REVISAO_CADASTRO, DEVOLVER_REVISAO_CADASTRO, ACEITAR_REVISAO_CADASTRO, HOMOLOGAR_REVISAO_CADASTRO,
                EDITAR_MAPA, DISPONIBILIZAR_MAPA, APRESENTAR_SUGESTOES, VALIDAR_MAPA, DEVOLVER_MAPA, ACEITAR_MAPA, HOMOLOGAR_MAPA, AJUSTAR_MAPA,
                REALIZAR_AUTOAVALIACAO).contains(acao);
    }

    private Unidade obterUnidadeLocalizacao(Subprocesso sp) {
        if (sp.getLocalizacaoAtualCache() != null) return sp.getLocalizacaoAtualCache();
        
        if (sp.getCodigo() == null) return sp.getUnidade();
        Unidade localizacao = movimentacaoRepo.findFirstBySubprocessoCodigoOrderByDataHoraDesc(sp.getCodigo())
                .map(m -> m.getUnidadeDestino() != null ? m.getUnidadeDestino() : sp.getUnidade())
                .orElse(sp.getUnidade());
        
        sp.setLocalizacaoAtualCache(localizacao);
        return localizacao;
    }

    private boolean verificaHierarquia(Usuario usuario, Unidade unidade, RequisitoHierarquia requisito) {
        return switch (requisito) {
            case NENHUM -> true;
            case MESMA_UNIDADE -> Objects.equals(usuario.getUnidadeAtivaCodigo(), unidade.getCodigo());
            case MESMA_OU_SUBORDINADA -> hierarquiaService.isMesmaOuSubordinada(unidade, Unidade.builder().codigo(usuario.getUnidadeAtivaCodigo()).build());
            case SUPERIOR_IMEDIATA -> hierarquiaService.isSuperiorImediata(unidade, Unidade.builder().codigo(usuario.getUnidadeAtivaCodigo()).build());
            case TITULAR_UNIDADE -> hierarquiaService.isResponsavel(unidade, usuario);
        };
    }

    public PermissoesSubprocessoDto obterPermissoesUI(Usuario usuario, Subprocesso sp) {
        return PermissoesSubprocessoDto.builder()
                .podeEditarCadastro(canExecute(usuario, EDITAR_CADASTRO, sp) || canExecute(usuario, EDITAR_REVISAO_CADASTRO, sp))
                .podeDisponibilizarCadastro(canExecute(usuario, DISPONIBILIZAR_CADASTRO, sp) || canExecute(usuario, DISPONIBILIZAR_REVISAO_CADASTRO, sp))
                .podeDevolverCadastro(canExecute(usuario, DEVOLVER_CADASTRO, sp) || canExecute(usuario, DEVOLVER_REVISAO_CADASTRO, sp))
                .podeAceitarCadastro(canExecute(usuario, ACEITAR_CADASTRO, sp) || canExecute(usuario, ACEITAR_REVISAO_CADASTRO, sp))
                .podeHomologarCadastro(canExecute(usuario, HOMOLOGAR_CADASTRO, sp) || canExecute(usuario, HOMOLOGAR_REVISAO_CADASTRO, sp))
                .podeEditarMapa(canExecute(usuario, EDITAR_MAPA, sp))
                .podeDisponibilizarMapa(canExecute(usuario, DISPONIBILIZAR_MAPA, sp))
                .podeValidarMapa(canExecute(usuario, VALIDAR_MAPA, sp))
                .podeApresentarSugestoes(canExecute(usuario, APRESENTAR_SUGESTOES, sp))
                .podeDevolverMapa(canExecute(usuario, DEVOLVER_MAPA, sp))
                .podeAceitarMapa(canExecute(usuario, ACEITAR_MAPA, sp))
                .podeHomologarMapa(canExecute(usuario, HOMOLOGAR_MAPA, sp))
                .podeVisualizarImpacto(canExecute(usuario, VERIFICAR_IMPACTOS, sp))
                .podeAlterarDataLimite(canExecute(usuario, ALTERAR_DATA_LIMITE, sp))
                .podeReabrirCadastro(canExecute(usuario, REABRIR_CADASTRO, sp))
                .podeReabrirRevisao(canExecute(usuario, REABRIR_REVISAO, sp))
                .podeEnviarLembrete(canExecute(usuario, ENVIAR_LEMBRETE_PROCESSO, sp))
                .build();
    }

    private Usuario getUsuarioAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Usuario usuario) return usuario;
        return null;
    }
}
