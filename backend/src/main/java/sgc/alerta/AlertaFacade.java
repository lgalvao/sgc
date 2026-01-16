package sgc.alerta;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.alerta.dto.AlertaDto;
import sgc.alerta.mapper.AlertaMapper;
import sgc.alerta.model.Alerta;
import sgc.alerta.model.AlertaRepo;
import sgc.alerta.model.AlertaUsuario;
import sgc.alerta.model.AlertaUsuarioRepo;
import sgc.organizacao.UnidadeFacade;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.model.TipoUnidade;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.processo.model.Processo;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Serviço para gerenciar alertas do sistema.
 *
 * <p>Responsável por criar alertas para unidades participantes de processos
 * e gerenciar a visualização/leitura de alertas por usuários.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AlertaFacade {
    private final AlertaRepo alertaRepo;
    private final AlertaUsuarioRepo alertaUsuarioRepo;
    private final UsuarioFacade usuarioService;
    private final AlertaMapper alertaMapper;
    private final UnidadeFacade unidadeService;

    private @Nullable Unidade sedoc;

    private Unidade getSedoc() {
        if (sedoc == null) {
            sedoc = unidadeService.buscarEntidadePorSigla("SEDOC");
        }
        return sedoc;
    }

    /**
     * Criar e persistir um alerta.
     */
    private Alerta criarAlerta(Processo processo, Unidade origem, Unidade destino, String descricao) {
        return alertaRepo.save(new Alerta()
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
            alertasCriados.add(criarAlertaSedoc(processo, unidade, "Início do processo"));
        }

        // Alertas intermediários (consolidados)
        for (Unidade unidade : unidadesIntermediarias.values()) {
            Alerta alerta = criarAlertaSedoc(processo, unidade, "Início do processo em unidades subordinadas");
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
            AlertaUsuario.Chave chave = new AlertaUsuario.Chave(codigo, usuarioTitulo);
            
            AlertaUsuario alertaUsuario = alertaUsuarioRepo.findById(chave)
                    .orElseGet(() -> {
                        Alerta alerta = alertaRepo.findById(codigo).orElse(null);
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
                alertaUsuarioRepo.save(alertaUsuario);
            }
        }
    }

    /**
     * Lista alertas para o usuário baseado na sua unidade de lotação.
     * Apenas leitura, sem efeitos colaterais.
     */
    @Transactional(readOnly = true)
    public List<AlertaDto> listarAlertasPorUsuario(String usuarioTitulo) {
        Usuario usuario = usuarioService.buscarPorId(usuarioTitulo);
        Unidade lotacao = usuario.getUnidadeLotacao();

        if (lotacao == null) {
            return Collections.emptyList();
        }

        List<Alerta> alertasUnidade = alertaRepo.findByUnidadeDestino_Codigo(lotacao.getCodigo());

        // Maps alerts to DTOs with read timestamps
        return alertasUnidade.stream().map(alerta -> {
            LocalDateTime dataHoraLeitura = obterDataHoraLeitura(alerta.getCodigo(), usuarioTitulo).orElse(null);
            return alertaMapper.toDto(alerta, dataHoraLeitura);
        }).toList();
    }

    /**
     * Lista apenas alertas não lidos para o usuário.
     */
    @Transactional(readOnly = true)
    public List<AlertaDto> listarAlertasNaoLidos(String usuarioTitulo) {
        return listarAlertasPorUsuario(usuarioTitulo).stream()
                .filter(dto -> dto.getDataHoraLeitura() == null)
                .toList();
    }

    /**
     * Lista alertas destinados a uma unidade específica.
     */
    public Page<Alerta> listarPorUnidade(Long codigoUnidade, Pageable pageable) {
        return alertaRepo.findByUnidadeDestino_Codigo(codigoUnidade, pageable);
    }

    public Optional<LocalDateTime> obterDataHoraLeitura(Long codigoAlerta, String usuarioTitulo) {
        return alertaUsuarioRepo
                .findById(new AlertaUsuario.Chave(codigoAlerta, usuarioTitulo))
                .map(AlertaUsuario::getDataHoraLeitura);
    }

    @Transactional
    public void criarAlertaReaberturaCadastro(Processo processo, Unidade unidade, String justificativa) {
        String descricao = "Cadastro de atividades reaberto pela SEDOC. Justificativa: %s".formatted(justificativa);
        criarAlertaSedoc(processo, unidade, descricao);
    }

    @Transactional
    public void criarAlertaReaberturaCadastroSuperior(Processo processo, Unidade superior, Unidade subordinada) {
        String descricao = "Cadastro da unidade %s reaberto pela SEDOC".formatted(subordinada.getSigla());
        criarAlertaSedoc(processo, superior, descricao);
    }

    @Transactional
    public void criarAlertaReaberturaRevisao(Processo processo, Unidade unidade, String justificativa) {
        String descricao = "Revisão de cadastro da unidade %s reaberta pela SEDOC. Justificativa: %s".formatted(unidade.getSigla(), justificativa);
        criarAlertaSedoc(processo, unidade, descricao);
    }

    @Transactional
    public void criarAlertaReaberturaRevisaoSuperior(Processo processo, Unidade superior, Unidade subordinada) {
        String descricao = "Revisão de cadastro da unidade %s reaberta pela SEDOC".formatted(subordinada.getSigla());
        criarAlertaSedoc(processo, superior, descricao);
    }
}
