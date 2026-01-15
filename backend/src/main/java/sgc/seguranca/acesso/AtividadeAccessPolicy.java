package sgc.seguranca.acesso;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Mapa;
import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.subprocesso.model.Subprocesso;

import java.util.EnumSet;
import java.util.Map;

import static sgc.organizacao.model.Perfil.*;
import static sgc.seguranca.acesso.Acao.*;

/**
 * Política de acesso para operações em atividades.
 * As permissões são baseadas no subprocesso ao qual a atividade pertence
 * (através do mapa), verificando se o usuário é o titular da unidade.
 */
@Component
@RequiredArgsConstructor
public class AtividadeAccessPolicy extends AbstractAccessPolicy<Atividade> {

    /**
     * Mapeamento de ações para regras de acesso
     */
    private static final Map<Acao, RegrasAcaoAtividade> REGRAS = Map.ofEntries(
            Map.entry(CRIAR_ATIVIDADE, new RegrasAcaoAtividade(
                    EnumSet.of(ADMIN, GESTOR, CHEFE),
                    true // Requer ser titular
            )),
            Map.entry(EDITAR_ATIVIDADE, new RegrasAcaoAtividade(
                    EnumSet.of(ADMIN, GESTOR, CHEFE),
                    true // Requer ser titular
            )),
            Map.entry(EXCLUIR_ATIVIDADE, new RegrasAcaoAtividade(
                    EnumSet.of(ADMIN, GESTOR, CHEFE),
                    true // Requer ser titular
            )),
            Map.entry(ASSOCIAR_CONHECIMENTOS, new RegrasAcaoAtividade(
                    EnumSet.of(ADMIN, GESTOR, CHEFE),
                    true // Requer ser titular
            ))
    );

    @Override
    @SuppressWarnings("squid:S2589") // Sonar FP: Atividade.mapa é @Nullable, pode ser null
    public boolean canExecute(Usuario usuario, Acao acao, Atividade atividade) {
        RegrasAcaoAtividade regras = REGRAS.get(acao);
        if (regras == null) {
            definirMotivoNegacao("Ação não reconhecida: " + acao);
            return false;
        }

        // 1. Verifica perfil
        if (!temPerfilPermitido(usuario, regras.perfisPermitidos)) {
            definirMotivoNegacao(usuario, regras.perfisPermitidos, acao);
            return false;
        }

        // 2. Verifica se é titular da unidade (quando requerido)
        if (regras.requerTitular) {
            Mapa mapa = atividade.getMapa();
            if (mapa == null) {
                definirMotivoNegacao("Atividade não possui mapa associado");
                return false;
            }

            Subprocesso subprocesso = mapa.getSubprocesso();
            if (subprocesso == null) {
                definirMotivoNegacao("Mapa não possui subprocesso associado");
                return false;
            }

            Unidade unidade = subprocesso.getUnidade();
            if (unidade == null) {
                definirMotivoNegacao("Subprocesso não possui unidade associada");
                return false;
            }

            String tituloTitular = unidade.getTituloTitular();
            if (!usuario.getTituloEleitoral().equals(tituloTitular)) {
                definirMotivoNegacao(String.format(
                        "Usuário '%s' não é o titular da unidade '%s'. Titular: %s",
                        usuario.getTituloEleitoral(),
                        unidade.getSigla(),
                        java.util.Objects.toString(tituloTitular, "não definido")
                ));
                return false;
            }
        }

        return true;
    }

    /**
     * Record para regras de ação de atividade
     */
    private record RegrasAcaoAtividade(
            EnumSet<Perfil> perfisPermitidos,
            boolean requerTitular
    ) {}
}
