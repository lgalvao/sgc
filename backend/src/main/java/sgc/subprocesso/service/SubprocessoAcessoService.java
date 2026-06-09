package sgc.subprocesso.service;

import lombok.*;
import org.springframework.stereotype.*;
import sgc.mapa.service.*;
import sgc.organizacao.model.*;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.model.*;

import java.util.*;

import static sgc.subprocesso.model.SituacaoSubprocesso.*;

@Service
@RequiredArgsConstructor
public class SubprocessoAcessoService {

    private static final Set<SituacaoSubprocesso> SITUACOES_EDICAO_CADASTRO = Set.of(
            NAO_INICIADO, MAPEAMENTO_CADASTRO_EM_ANDAMENTO, REVISAO_CADASTRO_EM_ANDAMENTO);
    private static final Set<SituacaoSubprocesso> SITUACOES_DISPONIBILIZACAO_CADASTRO = Set.of(
            MAPEAMENTO_CADASTRO_EM_ANDAMENTO, REVISAO_CADASTRO_EM_ANDAMENTO);
    private static final Set<SituacaoSubprocesso> SITUACOES_ANALISE_CADASTRO = Set.of(
            MAPEAMENTO_CADASTRO_DISPONIBILIZADO, REVISAO_CADASTRO_DISPONIBILIZADA);
    private static final Set<SituacaoSubprocesso> SITUACOES_DISPONIBILIZACAO_MAPA = Set.of(
            MAPEAMENTO_CADASTRO_HOMOLOGADO,
            MAPEAMENTO_MAPA_CRIADO,
            MAPEAMENTO_MAPA_COM_SUGESTOES,
            REVISAO_CADASTRO_HOMOLOGADA,
            REVISAO_MAPA_AJUSTADO,
            REVISAO_MAPA_COM_SUGESTOES);
    private static final Set<SituacaoSubprocesso> SITUACOES_ANALISE_MAPA = Set.of(
            MAPEAMENTO_MAPA_DISPONIBILIZADO, REVISAO_MAPA_DISPONIBILIZADO);
    private static final Set<SituacaoSubprocesso> SITUACOES_COM_SUGESTOES_MAPA = Set.of(
            MAPEAMENTO_MAPA_COM_SUGESTOES, REVISAO_MAPA_COM_SUGESTOES);
    private static final Set<SituacaoSubprocesso> SITUACOES_GESTAO_MAPA = Set.of(
            MAPEAMENTO_MAPA_COM_SUGESTOES,
            MAPEAMENTO_MAPA_VALIDADO,
            REVISAO_MAPA_COM_SUGESTOES,
            REVISAO_MAPA_VALIDADO);
    private static final Set<SituacaoSubprocesso> SITUACOES_EDICAO_MAPA = Set.of(
            MAPEAMENTO_CADASTRO_HOMOLOGADO,
            MAPEAMENTO_MAPA_CRIADO,
            MAPEAMENTO_MAPA_COM_SUGESTOES,
            REVISAO_CADASTRO_HOMOLOGADA,
            REVISAO_MAPA_AJUSTADO,
            REVISAO_MAPA_COM_SUGESTOES,
            DIAGNOSTICO_EM_ANDAMENTO);

    private final ImpactoMapaService impactoMapaService;

    public PermissoesSubprocessoDto resolverPermissoes(SubprocessoConsultaService.ContextoConsultaSubprocesso contexto) {
        if (contexto.processoFinalizado()) {
            return construirPermissoesProcessoFinalizado(contexto);
        }
        return construirPermissoes(contexto);
    }

    private PermissoesSubprocessoDto construirPermissoes(SubprocessoConsultaService.ContextoConsultaSubprocesso contexto) {
        SituacaoSubprocesso situacao = contexto.situacao();
        boolean mesmaUnidade = contexto.mesmaUnidade();

        PermissoesSubprocessoDto.PermissoesSubprocessoDtoBuilder builder = PermissoesSubprocessoDto.builder()
                .podeEditarCadastro(contexto.isChefe())
                .podeDisponibilizarCadastro(contexto.isChefe())
                .podeDevolverCadastro(contexto.isGestorOuAdmin())
                .podeAceitarCadastro(contexto.isGestor())
                .podeHomologarCadastro(contexto.isAdmin())
                .podeEditarMapa(contexto.isAdmin())
                .podeDisponibilizarMapa(contexto.isAdmin())
                .podeValidarMapa(contexto.isChefe())
                .podeApresentarSugestoes(contexto.isChefe())
                .podeVerSugestoes(contexto.isGestorOuAdmin() && SITUACOES_COM_SUGESTOES_MAPA.contains(situacao))
                .podeDevolverMapa(contexto.isAdmin() || contexto.isGestor())
                .podeAceitarMapa(contexto.isGestor())
                .podeHomologarMapa(contexto.isAdmin())
                .podeVisualizarImpacto(verificarVisualizarImpacto(contexto))
                .podeAlterarDataLimite(contexto.isAdmin())
                .podeReabrirCadastro(contexto.isAdmin())
                .podeReabrirRevisao(contexto.isAdmin())
                .podeEnviarLembrete(contexto.isAdmin())
                .podePreencherAutoavaliacao(contexto.perfil() == Perfil.SERVIDOR)
                .podeCriarConsenso(contexto.isChefe())
                .podeConcluirDiagnostico(contexto.isChefe())
                .podeValidarDiagnostico(contexto.isGestor())
                .podeDevolverDiagnostico(contexto.isGestorOuAdmin())
                .podeHomologarDiagnostico(contexto.isAdmin())
                .mesmaUnidade(mesmaUnidade)
                .habilitarAcessoCadastro(verificarAcessoCadastroHabilitado(contexto))
                .habilitarAcessoMapa(verificarAcessoMapaHabilitado(contexto))
                .habilitarAcessoDiagnostico(verificarAcessoDiagnosticoHabilitado(contexto))
                .habilitarAlterarDataLimite(contexto.isAdmin())
                .habilitarEnviarLembrete(contexto.isAdmin());

        // Habilitação de ações de cadastro
        builder.habilitarEditarCadastro(contexto.isChefe() && SITUACOES_EDICAO_CADASTRO.contains(situacao) && mesmaUnidade)
                .habilitarDisponibilizarCadastro(contexto.isChefe() && SITUACOES_DISPONIBILIZACAO_CADASTRO.contains(situacao) && mesmaUnidade)
                .habilitarDevolverCadastro(contexto.isGestorOuAdmin() && SITUACOES_ANALISE_CADASTRO.contains(situacao) && mesmaUnidade)
                .habilitarAceitarCadastro(contexto.isGestor() && SITUACOES_ANALISE_CADASTRO.contains(situacao) && mesmaUnidade)
                .habilitarHomologarCadastro(contexto.isAdmin() && SITUACOES_ANALISE_CADASTRO.contains(situacao) && mesmaUnidade);

        // Habilitação de ações de mapa
        builder.habilitarEditarMapa(contexto.isAdmin() && SITUACOES_EDICAO_MAPA.contains(situacao) && mesmaUnidade)
                .habilitarDisponibilizarMapa(contexto.isAdmin() && SITUACOES_DISPONIBILIZACAO_MAPA.contains(situacao) && mesmaUnidade)
                .habilitarValidarMapa(contexto.isChefe() && SITUACOES_ANALISE_MAPA.contains(situacao) && mesmaUnidade)
                .habilitarApresentarSugestoes(contexto.isChefe() && SITUACOES_ANALISE_MAPA.contains(situacao) && mesmaUnidade)
                .habilitarDevolverMapa(verificarDevolverMapa(contexto) && mesmaUnidade)
                .habilitarAceitarMapa(contexto.isGestor() && SITUACOES_GESTAO_MAPA.contains(situacao) && mesmaUnidade)
                .habilitarHomologarMapa(verificarHomologarMapa(contexto) && mesmaUnidade);

        builder.habilitarPreencherAutoavaliacao(contexto.perfil() == Perfil.SERVIDOR && mesmaUnidade)
                .habilitarCriarConsenso(contexto.isChefe() && mesmaUnidade)
                .habilitarConcluirDiagnostico(contexto.isChefe()
                && situacao == DIAGNOSTICO_EM_ANDAMENTO
                        && mesmaUnidade)
                .habilitarValidarDiagnostico(contexto.isGestor() && situacao == DIAGNOSTICO_CONCLUIDO && mesmaUnidade)
                .habilitarDevolverDiagnostico(contexto.isGestorOuAdmin() && situacao == DIAGNOSTICO_CONCLUIDO && mesmaUnidade)
                .habilitarHomologarDiagnostico(contexto.isAdmin() && situacao == DIAGNOSTICO_CONCLUIDO && mesmaUnidade);

        // Reaberturas
        builder.habilitarReabrirCadastro(contexto.isAdmin() && isSituacaoMapeamentoAPartirDe(situacao, MAPEAMENTO_MAPA_HOMOLOGADO))
                .habilitarReabrirRevisao(contexto.isAdmin() && isSituacaoRevisaoAPartirDe(situacao, REVISAO_MAPA_HOMOLOGADO));

        return builder.build();
    }

    private PermissoesSubprocessoDto construirPermissoesProcessoFinalizado(SubprocessoConsultaService.ContextoConsultaSubprocesso contexto) {
        return PermissoesSubprocessoDto.builder()
                .podeEditarCadastro(contexto.isChefe())
                .podeDisponibilizarCadastro(contexto.isChefe())
                .podeDevolverCadastro(contexto.isGestorOuAdmin())
                .podeAceitarCadastro(contexto.isGestor())
                .podeHomologarCadastro(contexto.isAdmin())
                .podeEditarMapa(contexto.isAdmin())
                .podeDisponibilizarMapa(contexto.isAdmin())
                .podeValidarMapa(contexto.isChefe())
                .podeApresentarSugestoes(contexto.isChefe())
                .podeVerSugestoes(contexto.isGestorOuAdmin() && SITUACOES_COM_SUGESTOES_MAPA.contains(contexto.situacao()))
                .podeDevolverMapa(contexto.isAdmin() || contexto.isGestor())
                .podeAceitarMapa(contexto.isGestor())
                .podeHomologarMapa(contexto.isAdmin())
                .podeVisualizarImpacto(verificarVisualizarImpacto(contexto))
                .podeAlterarDataLimite(contexto.isAdmin())
                .podeReabrirCadastro(contexto.isAdmin())
                .podeReabrirRevisao(contexto.isAdmin())
                .podeEnviarLembrete(contexto.isAdmin())
                .podePreencherAutoavaliacao(contexto.perfil() == Perfil.SERVIDOR)
                .podeCriarConsenso(contexto.isChefe())
                .podeConcluirDiagnostico(contexto.isChefe())
                .podeValidarDiagnostico(contexto.isGestor())
                .podeDevolverDiagnostico(contexto.isGestorOuAdmin())
                .podeHomologarDiagnostico(contexto.isAdmin())
                .mesmaUnidade(contexto.mesmaUnidade())
                .habilitarAcessoCadastro(verificarAcessoCadastroHabilitado(contexto))
                .habilitarAcessoMapa(verificarAcessoMapaHabilitado(contexto))
                .habilitarAcessoDiagnostico(verificarAcessoDiagnosticoHabilitado(contexto))
                .habilitarEditarCadastro(false)
                .habilitarDisponibilizarCadastro(false)
                .habilitarDevolverCadastro(false)
                .habilitarAceitarCadastro(false)
                .habilitarHomologarCadastro(false)
                .habilitarEditarMapa(false)
                .habilitarDisponibilizarMapa(false)
                .habilitarValidarMapa(false)
                .habilitarApresentarSugestoes(false)
                .habilitarDevolverMapa(false)
                .habilitarAceitarMapa(false)
                .habilitarHomologarMapa(false)
                .habilitarAlterarDataLimite(false)
                .habilitarReabrirCadastro(false)
                .habilitarReabrirRevisao(false)
                .habilitarEnviarLembrete(false)
                .habilitarPreencherAutoavaliacao(false)
                .habilitarCriarConsenso(false)
                .habilitarConcluirDiagnostico(false)
                .habilitarValidarDiagnostico(false)
                .habilitarDevolverDiagnostico(false)
                .habilitarHomologarDiagnostico(false)
                .build();
    }

    private boolean verificarAcessoDiagnosticoHabilitado(SubprocessoConsultaService.ContextoConsultaSubprocesso contexto) {
        if (!contexto.situacao().name().startsWith("DIAGNOSTICO")) {
            return false;
        }
        return switch (contexto.perfil()) {
            case ADMIN -> true;
            case GESTOR -> contexto.isUnidadeAlvoNaHierarquiaUsuario();
            case CHEFE, Perfil.SERVIDOR -> contexto.isMesmaUnidadeAlvo();
        };
    }

    private boolean verificarAcessoCadastroHabilitado(SubprocessoConsultaService.ContextoConsultaSubprocesso contexto) {
        if (contexto.isChefe()) {
            return contexto.isMesmaUnidadeAlvo();
        }

        boolean cadastroDisponibilizado = verificarCadastroDisponibilizadoParaVisualizacao(contexto.situacao());
        if (!cadastroDisponibilizado) {
            return false;
        }

        return switch (contexto.perfil()) {
            case ADMIN -> true;
            case GESTOR -> contexto.isUnidadeAlvoNaHierarquiaUsuario();
            case CHEFE, Perfil.SERVIDOR -> contexto.isMesmaUnidadeAlvo();
        };
    }

    private boolean verificarAcessoMapaHabilitado(SubprocessoConsultaService.ContextoConsultaSubprocesso contexto) {
        boolean mapaDisponibilizado = verificarMapaDisponibilizadoParaVisualizacao(contexto.situacao());
        boolean mesmaHierarquia = contexto.isUnidadeAlvoNaHierarquiaUsuario();
        boolean mesmaUnidadeAlvo = contexto.isMesmaUnidadeAlvo();

        return switch (contexto.perfil()) {
            case ADMIN -> verificarMapaHabilitadoParaAdmin(contexto.situacao());
            case GESTOR -> mapaDisponibilizado && mesmaHierarquia;
            case CHEFE, Perfil.SERVIDOR -> mapaDisponibilizado && mesmaUnidadeAlvo;
        };
    }

    private boolean verificarCadastroDisponibilizadoParaVisualizacao(SituacaoSubprocesso situacao) {
        return isSituacaoMapeamentoAPartirDe(situacao, MAPEAMENTO_CADASTRO_DISPONIBILIZADO) || isSituacaoRevisaoAPartirDe(situacao, REVISAO_CADASTRO_DISPONIBILIZADA);
    }

    private boolean verificarMapaDisponibilizadoParaVisualizacao(SituacaoSubprocesso situacao) {
        return isSituacaoMapeamentoAPartirDe(situacao, MAPEAMENTO_MAPA_DISPONIBILIZADO) || isSituacaoRevisaoAPartirDe(situacao, REVISAO_MAPA_DISPONIBILIZADO);
    }

    private boolean verificarMapaHabilitadoParaAdmin(SituacaoSubprocesso situacao) {
        return isSituacaoMapeamentoAPartirDe(situacao, MAPEAMENTO_CADASTRO_HOMOLOGADO) || isSituacaoRevisaoAPartirDe(situacao, REVISAO_CADASTRO_HOMOLOGADA);
    }

    private boolean verificarVisualizarImpacto(SubprocessoConsultaService.ContextoConsultaSubprocesso contexto) {
        return contexto.temMapaVigente()
                && impactoMapaService.podeVisualizarImpactos(contexto.subprocesso());
    }

    private boolean verificarDevolverMapa(SubprocessoConsultaService.ContextoConsultaSubprocesso contexto) {
        SituacaoSubprocesso situacao = contexto.situacao();
        if (contexto.isAdmin()) {
            return situacao == MAPEAMENTO_MAPA_COM_SUGESTOES || situacao == REVISAO_MAPA_COM_SUGESTOES;
        }
        return contexto.isGestor() && SITUACOES_GESTAO_MAPA.contains(situacao);
    }

    private boolean verificarHomologarMapa(SubprocessoConsultaService.ContextoConsultaSubprocesso contexto) {
        SituacaoSubprocesso situacao = contexto.situacao();
        if (!contexto.isAdmin()) {
            return false;
        }
        return situacao == MAPEAMENTO_MAPA_VALIDADO || situacao == REVISAO_MAPA_VALIDADO;
    }

    private boolean isSituacaoMapeamentoAPartirDe(SituacaoSubprocesso situacaoAtual, SituacaoSubprocesso marcoInicial) {
        return isFluxoMapeamento(situacaoAtual) && situacaoAtual.ordinal() >= marcoInicial.ordinal();
    }

    private boolean isSituacaoRevisaoAPartirDe(SituacaoSubprocesso situacaoAtual, SituacaoSubprocesso marcoInicial) {
        return isFluxoRevisao(situacaoAtual) && situacaoAtual.ordinal() >= marcoInicial.ordinal();
    }

    private boolean isFluxoMapeamento(SituacaoSubprocesso situacao) {
        return situacao.name().startsWith("MAPEAMENTO");
    }

    private boolean isFluxoRevisao(SituacaoSubprocesso situacao) {
        return situacao.name().startsWith("REVISAO");
    }
}
