package sgc.subprocesso.service;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sgc.mapa.service.ImpactoMapaService;
import sgc.organizacao.model.Perfil;
import sgc.subprocesso.dto.PermissoesSubprocessoDto;
import sgc.subprocesso.model.SituacaoSubprocesso;

import java.util.Set;

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
            MAPEAMENTO_MAPA_VALIDADO,
            REVISAO_CADASTRO_HOMOLOGADA,
            REVISAO_MAPA_AJUSTADO,
            REVISAO_MAPA_COM_SUGESTOES,
            REVISAO_MAPA_VALIDADO,
            DIAGNOSTICO_AUTOAVALIACAO_EM_ANDAMENTO);

    private final ImpactoMapaService impactoMapaService;

    public PermissoesSubprocessoDto resolverPermissoes(SubprocessoConsultaService.ContextoConsultaSubprocesso contexto) {
        if (contexto.processoFinalizado()) {
            return construirPermissoesProcessoFinalizado(contexto);
        }
        return construirPermissoes(contexto);
    }

    private PermissoesSubprocessoDto construirPermissoes(SubprocessoConsultaService.ContextoConsultaSubprocesso contexto) {
        PermissoesFluxo permissoesFluxo = calcularPermissoesFluxo(contexto);
        boolean mesmaUnidade = contexto.mesmaUnidade();
        boolean habilitarAcessoCadastro = verificarAcessoCadastroHabilitado(contexto);
        boolean habilitarAcessoMapa = verificarAcessoMapaHabilitado(contexto);

        return PermissoesSubprocessoDto.builder()
                .podeEditarCadastro(permissoesFluxo.podeEditarCadastro())
                .podeDisponibilizarCadastro(permissoesFluxo.podeDisponibilizarCadastro())
                .podeDevolverCadastro(permissoesFluxo.podeDevolverCadastro())
                .podeAceitarCadastro(permissoesFluxo.podeAceitarCadastro())
                .podeHomologarCadastro(permissoesFluxo.podeHomologarCadastro())
                .podeEditarMapa(permissoesFluxo.podeEditarMapa())
                .podeDisponibilizarMapa(permissoesFluxo.podeDisponibilizarMapa())
                .podeValidarMapa(permissoesFluxo.podeValidarMapa())
                .podeApresentarSugestoes(permissoesFluxo.podeApresentarSugestoes())
                .podeVerSugestoes(permissoesFluxo.podeVerSugestoes())
                .podeDevolverMapa(permissoesFluxo.podeDevolverMapa())
                .podeAceitarMapa(permissoesFluxo.podeAceitarMapa())
                .podeHomologarMapa(permissoesFluxo.podeHomologarMapa())
                .podeVisualizarImpacto(permissoesFluxo.podeVisualizarImpacto())
                .podeAlterarDataLimite(contexto.isAdmin())
                .podeReabrirCadastro(contexto.isAdmin() && isSituacaoMapeamentoAPartirDe(contexto.situacao(), MAPEAMENTO_MAPA_HOMOLOGADO))
                .podeReabrirRevisao(contexto.isAdmin() && isSituacaoRevisaoAPartirDe(contexto.situacao(), REVISAO_MAPA_HOMOLOGADO))
                .podeEnviarLembrete(contexto.isAdmin())
                .mesmaUnidade(mesmaUnidade)
                .habilitarAcessoCadastro(habilitarAcessoCadastro)
                .habilitarAcessoMapa(habilitarAcessoMapa)
                .habilitarEditarCadastro(permissoesFluxo.podeEditarCadastro() && mesmaUnidade)
                .habilitarDisponibilizarCadastro(permissoesFluxo.podeDisponibilizarCadastro() && mesmaUnidade)
                .habilitarDevolverCadastro(permissoesFluxo.podeDevolverCadastro() && mesmaUnidade)
                .habilitarAceitarCadastro(permissoesFluxo.podeAceitarCadastro() && mesmaUnidade)
                .habilitarHomologarCadastro(permissoesFluxo.podeHomologarCadastro() && mesmaUnidade)
                .habilitarEditarMapa(permissoesFluxo.podeEditarMapa() && mesmaUnidade)
                .habilitarDisponibilizarMapa(permissoesFluxo.podeDisponibilizarMapa() && mesmaUnidade)
                .habilitarValidarMapa(permissoesFluxo.podeValidarMapa() && mesmaUnidade)
                .habilitarApresentarSugestoes(permissoesFluxo.podeApresentarSugestoes() && mesmaUnidade)
                .habilitarDevolverMapa(permissoesFluxo.podeDevolverMapa() && mesmaUnidade)
                .habilitarAceitarMapa(permissoesFluxo.podeAceitarMapa() && mesmaUnidade)
                .habilitarHomologarMapa(permissoesFluxo.podeHomologarMapa() && mesmaUnidade)
                .build();
    }

    private PermissoesFluxo calcularPermissoesFluxo(SubprocessoConsultaService.ContextoConsultaSubprocesso contexto) {
        SituacaoSubprocesso situacao = contexto.situacao();
        return PermissoesFluxo.builder()
                .podeEditarCadastro(contexto.isChefe() && SITUACOES_EDICAO_CADASTRO.contains(situacao))
                .podeDisponibilizarCadastro(contexto.isChefe() && SITUACOES_DISPONIBILIZACAO_CADASTRO.contains(situacao))
                .podeDevolverCadastro(contexto.isGestorOuAdmin() && SITUACOES_ANALISE_CADASTRO.contains(situacao))
                .podeAceitarCadastro(contexto.isGestor() && SITUACOES_ANALISE_CADASTRO.contains(situacao))
                .podeHomologarCadastro(contexto.isAdmin() && SITUACOES_ANALISE_CADASTRO.contains(situacao))
                .podeEditarMapa(verificarEditarMapa(contexto))
                .podeDisponibilizarMapa(contexto.isAdmin() && SITUACOES_DISPONIBILIZACAO_MAPA.contains(situacao))
                .podeValidarMapa(contexto.isChefe() && SITUACOES_ANALISE_MAPA.contains(situacao))
                .podeApresentarSugestoes(contexto.isChefe() && SITUACOES_ANALISE_MAPA.contains(situacao))
                .podeVerSugestoes(contexto.isGestorOuAdmin() && SITUACOES_COM_SUGESTOES_MAPA.contains(situacao))
                .podeDevolverMapa(verificarGerirMapa(contexto.isGestorOuAdmin(), situacao))
                .podeAceitarMapa(verificarGerirMapa(contexto.isGestor(), situacao))
                .podeHomologarMapa(verificarGerirMapa(contexto.isAdmin(), situacao))
                .podeVisualizarImpacto(verificarVisualizarImpacto(contexto))
                .build();
    }

    private PermissoesSubprocessoDto construirPermissoesProcessoFinalizado(SubprocessoConsultaService.ContextoConsultaSubprocesso contexto) {
        return PermissoesSubprocessoDto.builder()
                .habilitarAcessoCadastro(verificarAcessoCadastroHabilitado(contexto))
                .habilitarAcessoMapa(verificarAcessoMapaHabilitado(contexto))
                .mesmaUnidade(contexto.mesmaUnidade())
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
                .build();
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

    private boolean verificarEditarMapa(SubprocessoConsultaService.ContextoConsultaSubprocesso contexto) {
        return contexto.isAdmin() && SITUACOES_EDICAO_MAPA.contains(contexto.situacao());
    }

    private boolean verificarGerirMapa(boolean isPermitido, SituacaoSubprocesso situacao) {
        return isPermitido && SITUACOES_GESTAO_MAPA.contains(situacao);
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

    @Builder
    private record PermissoesFluxo(
            boolean podeEditarCadastro,
            boolean podeDisponibilizarCadastro,
            boolean podeDevolverCadastro,
            boolean podeAceitarCadastro,
            boolean podeHomologarCadastro,
            boolean podeEditarMapa,
            boolean podeDisponibilizarMapa,
            boolean podeValidarMapa,
            boolean podeApresentarSugestoes,
            boolean podeVerSugestoes,
            boolean podeDevolverMapa,
            boolean podeAceitarMapa,
            boolean podeHomologarMapa,
            boolean podeVisualizarImpacto
    ) {
    }
}
