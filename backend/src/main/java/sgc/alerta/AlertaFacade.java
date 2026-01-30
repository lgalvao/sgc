package sgc.alerta;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.alerta.dto.AlertaDto;
import sgc.alerta.mapper.AlertaMapper;
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
import java.util.function.Supplier;

/**
 * Facade para gerenciamento de alertas do sistema.
 *
 * <p>Responsável por criar alertas para unidades participantes de processos
 * e gerenciar a visualização/leitura de alertas por usuários. Delega operações
 * de persistência para {@link AlertaService}.
 *
 * @see AlertaService
 */
@Service
@Slf4j
public class AlertaFacade {
    private final AlertaService alertaService;
    private final UsuarioFacade usuarioService;
    private final AlertaMapper alertaMapper;
    private final UnidadeFacade unidadeService;
    
    // Lazy supplier para SEDOC - evita cache manual e é thread-safe
    @Getter(lazy = true)
    private final Unidade sedoc = unidadeService.buscarEntidadePorSigla("SEDOC");

    public AlertaFacade(AlertaService alertaService, 
                        UsuarioFacade usuarioService, 
                        AlertaMapper alertaMapper, 
                        @Lazy UnidadeFacade unidadeService) {
        this.alertaService = alertaService;
        this.usuarioService = usuarioService;
        this.alertaMapper = alertaMapper;
        this.unidadeService = unidadeService;
    }

    /**
     * Criar e persistir um alerta.
     */
    private Alerta criarAlerta(Processo processo, Unidade origem, Unidade destino, String descricao) {
        return alertaService.salvar(new Alerta()
                .setProcesso(processo)
                .setDataHora(LocalDateTime.now())
                .setUnidadeOrigem(origem)
                .setUnidadeDestino(destino)
                .setDescricao(descricao));
    }

    /**
     * Cria e persistir alerta com origem SEDOC
     */
    @Transactional
    public Alerta criarAlertaSedoc(Processo processo, Unidade destino, String descricao) {
        return doCriarAlertaSedoc(processo, destino, descricao);
    }

    private Alerta doCriarAlertaSedoc(Processo processo, Unidade destino, String descricao) {
        return criarAlerta(processo, getSedoc(), destino, descricao);
    }

    /**
     * Cria um alerta a partir de uma transição de subprocesso.
     * Usado pelo SubprocessoComunicacaoListener para processar eventos de transição.
     *
     * @param processo  Processo associado ao alerta
     * @param descricao Descrição do alerta (formatada)
     * @param origem    Unidade de origem da transição
     * @param destino   Unidade de destino (receberá o alerta)
     * @return O alerta criado
     */
    @Transactional
    public Alerta criarAlertaTransicao(Processo processo, String descricao, Unidade origem, Unidade destino) {
        return criarAlerta(processo, origem, destino, descricao);
    }

    /**
     * Cria alertas para todas as unidades participantes quando um processo é iniciado.
     * - Operacional: "Início do processo"
     * - Intermediária: "Início do processo em unidade(s) subordinada(s)"
     * - Interoperacional: Recebe os dois alertas
     */
    @Transactional
    public List<Alerta> criarAlertasProcessoIniciado(Processo processo, List<Unidade> unidadesParticipantes) {
        Set<Unidade> unidadesOperacionais = new HashSet<>();
        Map<Long, Unidade> unidadesIntermediarias = new HashMap<>();

        for (Unidade unidade : unidadesParticipantes) {
            // Unidades participantes sempre recebem o alerta operacional
            unidadesOperacionais.add(unidade);

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
        for (Unidade unidade : unidadesOperacionais) {
            alertasCriados.add(doCriarAlertaSedoc(processo, unidade, "Início do processo"));
        }

        // Alertas intermediários (consolidados)
        for (Unidade unidade : unidadesIntermediarias.values()) {
            Alerta alerta = doCriarAlertaSedoc(processo, unidade, "Início do processo em unidades subordinadas");
            alertasCriados.add(alerta);
        }

        return alertasCriados;
    }

    /**
     * Cria alerta quando cadastro é disponibilizado para validação.
     */
    @Transactional
    public void criarAlertaCadastroDisponibilizado(Processo processo, Unidade unidadeOrigem, Unidade unidadeDestino) {
        String desc = "Cadastro disponibilizado pela unidade %s no processo '%s'. Realize a análise do cadastro."
                .formatted(unidadeOrigem.getSigla(), processo.getDescricao());

        criarAlerta(processo, unidadeOrigem, unidadeDestino, desc);
    }

    /**
     * Cria alerta quando cadastro é devolvido para ajustes.
     */
    @Transactional
    public void criarAlertaCadastroDevolvido(Processo processo, Unidade unidadeDestino, String motivo) {
        String desc = "Cadastro devolvido no processo '%s'. Motivo: %s. Realize os ajustes necessários."
                .formatted(processo.getDescricao(), motivo);

        criarAlerta(processo, getSedoc(), unidadeDestino, desc);
    }

    @Transactional
    public void criarAlertaAlteracaoDataLimite(Processo processo, Unidade unidadeDestino, String novaData, int etapa) {
        String desc = "Data limite da etapa %d alterada para %s".formatted(etapa, novaData);
        criarAlerta(processo, getSedoc(), unidadeDestino, desc);
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
     * Apenas leitura, sem efeitos colaterais.
     */
    @Transactional(readOnly = true)
    public List<AlertaDto> listarAlertasPorUsuario(String usuarioTitulo) {
        return doListarAlertasPorUsuario(usuarioTitulo);
    }

    private List<AlertaDto> doListarAlertasPorUsuario(String usuarioTitulo) {
        Usuario usuario = usuarioService.buscarPorId(usuarioTitulo);
        Unidade lotacao = usuario.getUnidadeLotacao();

        List<Alerta> alertasUnidade = alertaService.buscarPorUnidadeDestino(lotacao.getCodigo());

        if (alertasUnidade.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> alertaCodigos = alertasUnidade.stream().map(Alerta::getCodigo).toList();

        // Fetch all read statuses in a single query to avoid N+1 problem (Optimization)
        List<AlertaUsuario> leituras = alertaService.buscarPorUsuarioEAlertas(usuarioTitulo, alertaCodigos);

        Map<Long, LocalDateTime> mapaLeitura = new HashMap<>();
        for (AlertaUsuario au : leituras) {
            mapaLeitura.put(au.getId().getAlertaCodigo(), au.getDataHoraLeitura());
        }

        // Maps alerts to DTOs with read timestamps
        return alertasUnidade.stream().map(alerta -> {
            LocalDateTime dataHoraLeitura = mapaLeitura.get(alerta.getCodigo());
            return alertaMapper.toDto(alerta, dataHoraLeitura);
        }).toList();
    }

    /**
     * Lista apenas alertas não lidos para o usuário.
     */
    @Transactional(readOnly = true)
    public List<AlertaDto> listarAlertasNaoLidos(String usuarioTitulo) {
        return doListarAlertasPorUsuario(usuarioTitulo).stream()
                .filter(dto -> dto.getDataHoraLeitura() == null)
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
        String descricao = "Cadastro de atividades reaberto pela SEDOC. Justificativa: %s".formatted(justificativa);
        doCriarAlertaSedoc(processo, unidade, descricao);
    }

    @Transactional
    public void criarAlertaReaberturaCadastroSuperior(Processo processo, Unidade superior, Unidade subordinada) {
        String descricao = "Cadastro da unidade %s reaberto pela SEDOC".formatted(subordinada.getSigla());
        doCriarAlertaSedoc(processo, superior, descricao);
    }

    @Transactional
    public void criarAlertaReaberturaRevisao(Processo processo, Unidade unidade, String justificativa) {
        String descricao = "Revisão de cadastro da unidade %s reaberta pela SEDOC. Justificativa: %s".formatted(unidade.getSigla(), justificativa);
        doCriarAlertaSedoc(processo, unidade, descricao);
    }

    @Transactional
    public void criarAlertaReaberturaRevisaoSuperior(Processo processo, Unidade superior, Unidade subordinada) {
        String descricao = "Revisão de cadastro da unidade %s reaberta pela SEDOC".formatted(subordinada.getSigla());
        doCriarAlertaSedoc(processo, superior, descricao);
    }
}
