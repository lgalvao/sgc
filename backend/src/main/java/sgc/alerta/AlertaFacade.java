package sgc.alerta;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.alerta.model.Alerta;
import sgc.alerta.model.AlertaUsuario;
import sgc.organizacao.UnidadeFacade;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.model.TipoUnidade;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.processo.model.Processo;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Facade para gerenciamento de alertas do sistema.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AlertaFacade {
    private final AlertaService alertaService;
    private final UsuarioFacade usuarioService;
    private final UnidadeFacade unidadeService;

    /**
     * Obtém a unidade raiz (ADMIN) do sistema.
     */
    private Unidade getUnidadeRaiz() {
        return unidadeService.buscarEntidadePorId(1L);
    }

    /**
     * Cria e persiste alerta com origem ADMIN (unidade raiz).
     * Atalho para {@link #criarAlerta} quando a origem é o sistema.
     */
    @Transactional
    public Alerta criarAlertaAdmin(Processo processo, Unidade destino, String descricao) {
        return criarAlerta(processo, getUnidadeRaiz(), destino, descricao);
    }

    /**
     * Cria um alerta a partir de uma transição de subprocesso.
     * Usado pelo SubprocessoComunicacaoListener para processar eventos de transição.
     */
    @Transactional
    public Alerta criarAlertaTransicao(Processo processo, String descricao, Unidade origem, Unidade destino) {
        return criarAlerta(processo, origem, destino, descricao);
    }

    /**
     * Cria alertas para todas as unidades participantes quando um processo é iniciado.
     * <ul>
     *   <li>Operacional: "Início do processo"</li>
     *   <li>Intermediária: "Início do processo em unidade(s) subordinada(s)"</li>
     *   <li>Interoperacional: Recebe os dois alertas</li>
     * </ul>
     */
    @Transactional
    public List<Alerta> criarAlertasProcessoIniciado(Processo processo, List<Unidade> unidadesParticipantes) {
        // Ambas usam Map<Long, Unidade> para deduplicar por código,
        // independentemente da implementação de equals/hashCode da entidade.
        Map<Long, Unidade> unidadesOperacionais = new HashMap<>();
        Map<Long, Unidade> unidadesIntermediarias = new HashMap<>();

        for (Unidade unidade : unidadesParticipantes) {
            // Unidades participantes sempre recebem o alerta operacional
            unidadesOperacionais.put(unidade.getCodigo(), unidade);

            // Se for Interoperacional, também recebe o de intermediária
            if (unidade.getTipo() == TipoUnidade.INTEROPERACIONAL) {
                unidadesIntermediarias.put(unidade.getCodigo(), unidade);
            }

            // Serão notificadas todas as unidades superiores
            Unidade unidadeSuperior = unidade.getUnidadeSuperior();
            while (unidadeSuperior != null) {
                unidadesIntermediarias.put(unidadeSuperior.getCodigo(), unidadeSuperior);
                unidadeSuperior = unidadeSuperior.getUnidadeSuperior();
            }
        }

        List<Alerta> alertasCriados = new ArrayList<>();

        // Alertas operacionais
        for (Unidade unidade : unidadesOperacionais.values()) {
            alertasCriados.add(criarAlertaAdmin(processo, unidade, "Início do processo"));
        }

        // Alertas intermediários (consolidados)
        for (Unidade unidade : unidadesIntermediarias.values()) {
            Alerta alerta = criarAlertaAdmin(processo, unidade, "Início do processo em unidades subordinadas");
            alertasCriados.add(alerta);
        }

        return alertasCriados;
    }

    /**
     * Cria alerta quando cadastro é disponibilizado para validação.
     */
    @Transactional
    public void criarAlertaCadastroDisponibilizado(Processo processo, Unidade unidadeOrigem, Unidade unidadeDestino) {
        String desc = "Cadastro disponibilizado pela unidade %s no processo '%s'."
                .formatted(unidadeOrigem.getSigla(), processo.getDescricao());

        criarAlerta(processo, unidadeOrigem, unidadeDestino, desc);
    }

    /**
     * Cria alerta quando cadastro é devolvido para ajustes.
     */
    @Transactional
    public void criarAlertaCadastroDevolvido(Processo processo, Unidade unidadeDestino, String motivo) {
        String desc = "Cadastro devolvido no processo '%s'. Motivo: %s."
                .formatted(processo.getDescricao(), motivo);

        criarAlerta(processo, getUnidadeRaiz(), unidadeDestino, desc);
    }

    @Transactional
    public void criarAlertaAlteracaoDataLimite(Processo processo, Unidade unidadeDestino, String novaData, int etapa) {
        String desc = "Data limite da etapa %d alterada para %s".formatted(etapa, novaData);
        criarAlerta(processo, getUnidadeRaiz(), unidadeDestino, desc);
    }

    /**
     * Criar e persistir um alerta.
     */
    private Alerta criarAlerta(Processo processo, Unidade origem, Unidade destino, String descricao) {
        Alerta alerta = Alerta.builder()
                .processo(processo)
                .dataHora(LocalDateTime.now())
                .unidadeOrigem(origem)
                .unidadeDestino(destino)
                .descricao(descricao)
                .build();
        return alertaService.salvar(alerta);
    }

    /**
     * Marca múltiplos alertas como lidos para o usuário. Cria o AlertaUsuario se ainda não existir.
     */
    @Transactional
    public void marcarComoLidos(String usuarioTitulo, List<Long> alertaCodigos) {
        Usuario usuario = usuarioService.buscarPorId(usuarioTitulo);
        LocalDateTime agora = LocalDateTime.now();

        for (Long codigo : alertaCodigos) {
            AlertaUsuario.Chave chave = AlertaUsuario.Chave.builder()
                    .alertaCodigo(codigo)
                    .usuarioTitulo(usuarioTitulo)
                    .build();

            AlertaUsuario alertaUsuario = alertaService.buscarAlertaUsuario(chave)
                    .orElseGet(() -> {
                        Alerta alerta = alertaService.buscarPorCodigo(codigo).orElse(null);
                        if (alerta == null) {
                            return null;
                        }
                        AlertaUsuario novo = new AlertaUsuario();
                        novo.setId(chave);
                        novo.setAlerta(alerta);
                        novo.setUsuario(usuario);
                        return novo;
                    });

            // Persists read timestamp if alert was unread
            if (alertaUsuario != null && alertaUsuario.getDataHoraLeitura() == null) {
                alertaUsuario.setDataHoraLeitura(agora);
                alertaService.salvarAlertaUsuario(alertaUsuario);
            }
        }
    }

    /**
     * Lista alertas para o usuário baseado na sua unidade de lotação.
     */
    @Transactional(readOnly = true)
    public List<Alerta> listarAlertasPorUsuario(String usuarioTitulo) {
        Usuario usuario = usuarioService.buscarPorId(usuarioTitulo);
        Unidade lotacao = usuario.getUnidadeLotacao();

        List<Alerta> alertasUnidade = alertaService.buscarPorUnidadeDestino(lotacao.getCodigo());

        if (alertasUnidade.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> alertaCodigos = alertasUnidade.stream().map(Alerta::getCodigo).toList();

        List<AlertaUsuario> leituras = alertaService.buscarPorUsuarioEAlertas(usuarioTitulo, alertaCodigos);

        Map<Long, LocalDateTime> mapaLeitura = new HashMap<>();
        for (AlertaUsuario au : leituras) {
            mapaLeitura.put(au.getId().getAlertaCodigo(), au.getDataHoraLeitura());
        }

        // Preenche campos transientes nas entidades
        for (Alerta alerta : alertasUnidade) {
            alerta.setDataHoraLeitura(mapaLeitura.get(alerta.getCodigo()));
        }

        return alertasUnidade;
    }

    /**
     * Lista apenas alertas não lidos para o usuário.
     */
    @Transactional(readOnly = true)
    public List<Alerta> listarAlertasNaoLidos(String usuarioTitulo) {
        return listarAlertasPorUsuario(usuarioTitulo).stream()
                .filter(alerta -> alerta.getDataHoraLeitura() == null)
                .toList();
    }

    /**
     * Lista alertas destinados a uma unidade específica.
     */
    public Page<Alerta> listarPorUnidade(Long codigoUnidade, Pageable pageable) {
        return alertaService.buscarPorUnidadeDestino(codigoUnidade, pageable);
    }

    public Optional<LocalDateTime> obterDataHoraLeitura(Long codigoAlerta, String usuarioTitulo) {
        return alertaService.obterDataHoraLeitura(codigoAlerta, usuarioTitulo);
    }

    @Transactional
    public void criarAlertaReaberturaCadastro(Processo processo, Unidade unidade, String justificativa) {
        String descricao = "Cadastro de atividades reaberto pela ADMIN. Justificativa: %s".formatted(justificativa);
        criarAlertaAdmin(processo, unidade, descricao);
    }

    @Transactional
    public void criarAlertaReaberturaCadastroSuperior(Processo processo, Unidade superior, Unidade subordinada) {
        String descricao = "Cadastro da unidade %s reaberto pela ADMIN".formatted(subordinada.getSigla());
        criarAlertaAdmin(processo, superior, descricao);
    }

    @Transactional
    public void criarAlertaReaberturaRevisao(Processo processo, Unidade unidade, String justificativa) {
        String descricao = "Revisão de cadastro da unidade %s reaberta pela ADMIN. Justificativa: %s".formatted(unidade.getSigla(), justificativa);
        criarAlertaAdmin(processo, unidade, descricao);
    }

    @Transactional
    public void criarAlertaReaberturaRevisaoSuperior(Processo processo, Unidade superior, Unidade subordinada) {
        String descricao = "Revisão de cadastro da unidade %s reaberta pela ADMIN".formatted(subordinada.getSigla());
        criarAlertaAdmin(processo, superior, descricao);
    }
}
