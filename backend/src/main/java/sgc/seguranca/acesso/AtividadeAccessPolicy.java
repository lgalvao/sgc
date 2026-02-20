package sgc.seguranca.acesso;

import org.springframework.stereotype.Component;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Mapa;
import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.organizacao.model.UsuarioPerfilRepo;
import sgc.organizacao.service.HierarquiaService;
import sgc.subprocesso.model.Subprocesso;

import java.util.EnumSet;
import java.util.Map;

import static sgc.organizacao.model.Perfil.CHEFE;
import static sgc.seguranca.acesso.Acao.*;
import static sgc.subprocesso.model.SituacaoSubprocesso.*;

/**
 * Política de acesso para operações em atividades.
 * As permissões são baseadas no subprocesso ao qual a atividade pertence
 * (através do mapa), verificando se o usuário é o titular da unidade.
 */
@Component
public class AtividadeAccessPolicy extends AbstractAccessPolicy<Atividade> {

    public AtividadeAccessPolicy(UsuarioPerfilRepo usuarioPerfilRepo, HierarquiaService hierarquiaService) {
        super(usuarioPerfilRepo, hierarquiaService);
    }

    /**
     * Situações nas quais a manipulação de atividades é permitida pelo responsável (CHEFE)
     */
    private static final EnumSet<sgc.subprocesso.model.SituacaoSubprocesso> SITUACOES_PERMITIDAS = EnumSet.of(
            NAO_INICIADO, MAPEAMENTO_CADASTRO_EM_ANDAMENTO, REVISAO_CADASTRO_EM_ANDAMENTO
    );

    /**
     * Mapeamento de ações para regras de acesso
     */
    private static final Map<Acao, RegrasAcaoAtividade> REGRAS = Map.ofEntries(
            Map.entry(CRIAR_ATIVIDADE, new RegrasAcaoAtividade(
                    EnumSet.of(CHEFE), SITUACOES_PERMITIDAS
            )),
            Map.entry(EDITAR_ATIVIDADE, new RegrasAcaoAtividade(
                    EnumSet.of(CHEFE), SITUACOES_PERMITIDAS
            )),
            Map.entry(EXCLUIR_ATIVIDADE, new RegrasAcaoAtividade(
                    EnumSet.of(CHEFE), SITUACOES_PERMITIDAS
            )),
            Map.entry(ASSOCIAR_CONHECIMENTOS, new RegrasAcaoAtividade(
                    EnumSet.of(CHEFE), SITUACOES_PERMITIDAS
            ))
    );

    @Override
    public boolean canExecute(Usuario usuario, Acao acao, Atividade atividade) {
        RegrasAcaoAtividade regras = REGRAS.get(acao);
        if (regras == null) {
            definirMotivoNegacao("Ação não reconhecida: " + acao);
            return false;
        }

        // 2. Verifica se o usuário tem o perfil permitido para a ação
        if (!temPerfilPermitido(usuario, regras.perfisPermitidos)) {
            definirMotivoNegacao(usuario, regras.perfisPermitidos, acao);
            return false;
        }

        Mapa mapa = atividade.getMapa();
        Subprocesso subprocesso = mapa.getSubprocesso();
        Unidade unidade = subprocesso.getUnidade();

        // 3. Verifica a situação do subprocesso correspondente ao mapa
        if (!regras.situacoesPermitidas.contains(subprocesso.getSituacao())) {
            definirMotivoNegacao(String.format(
                    "Ação '%s' em atividades não pode ser executada quando o subprocesso está na situação '%s'. " +
                            "Situações permitidas: %s",
                    acao.getDescricao(),
                    subprocesso.getSituacao().getDescricao(),
                    formatarSituacoes(regras.situacoesPermitidas)
            ));
            return false;
        }

        // 4. Verifica se é responsável pela unidade (obrigatório para as ações atuais)
        if (!verificaHierarquia(usuario, unidade, RequisitoHierarquia.TITULAR_UNIDADE)) {
            definirMotivoNegacao(obterMotivoNegacaoHierarquia(usuario, unidade, RequisitoHierarquia.TITULAR_UNIDADE));
            return false;
        }

        return true;
    }

    private String formatarSituacoes(EnumSet<sgc.subprocesso.model.SituacaoSubprocesso> situacoes) {
        return situacoes.stream()
                .map(sgc.subprocesso.model.SituacaoSubprocesso::getDescricao)
                .reduce((a, b) -> a + ", " + b)
                .orElse("nenhuma");
    }

    /**
     * Record para regras de ação de atividade
     */
    private record RegrasAcaoAtividade(
            EnumSet<Perfil> perfisPermitidos,
            EnumSet<sgc.subprocesso.model.SituacaoSubprocesso> situacoesPermitidas
    ) {
    }
}
