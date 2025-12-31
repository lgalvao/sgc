package sgc.alerta;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.alerta.dto.AlertaDto;
import sgc.alerta.dto.AlertaMapper;
import sgc.alerta.model.*;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.processo.model.Processo;
import sgc.usuario.model.Usuario;
import sgc.subprocesso.model.Subprocesso;
import sgc.unidade.model.TipoUnidade;
import sgc.unidade.model.Unidade;
import sgc.usuario.UsuarioService;

import java.time.LocalDateTime;
import java.util.*;

import static sgc.alerta.model.TipoAlerta.PROCESSO_INICIADO_INTERMEDIARIA;
import static sgc.alerta.model.TipoAlerta.PROCESSO_INICIADO_OPERACIONAL;

/**
 * Serviço para gerenciar alertas do sistema.
 *
 * <p>Responsável por criar alertas para unidades participantes de processos
 * e gerenciar a visualização/leitura de alertas por usuários.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AlertaService {
    private final AlertaRepo repositorioAlerta;
    private final AlertaUsuarioRepo alertaUsuarioRepo;
    private final UsuarioService usuarioService;
    private final AlertaMapper alertaMapper;

    /**
     * Cria um alerta básico para uma unidade destino.
     */
    @Transactional
    public Alerta criarAlerta(
            Processo processo,
            TipoAlerta tipoAlerta,
            Unidade unidadeDestino,
            String descricao) {

        log.debug("Criando alerta tipo={} para unidade {}.", tipoAlerta, unidadeDestino.getCodigo());

        Alerta alerta = new Alerta()
                .setProcesso(processo)
                .setDataHora(LocalDateTime.now())
                .setUnidadeOrigem(null) // SEDOC não tem registro como unidade
                .setUnidadeDestino(unidadeDestino)
                .setDescricao(descricao);

        return repositorioAlerta.save(alerta);
    }

    /**
     * Cria um alerta genérico a partir de uma transição de subprocesso.
     * Usado pelo SubprocessoComunicacaoListener para processar eventos de transição.
     *
     * @param processo Processo associado ao alerta
     * @param descricao Descrição do alerta (já formatada)
     * @param unidadeOrigem Unidade de origem da transição
     * @param unidadeDestino Unidade de destino (receberá o alerta)
     * @return O alerta criado
     */
    @Transactional
    public Alerta criarAlertaTransicao(
            Processo processo,
            String descricao,
            Unidade unidadeOrigem,
            Unidade unidadeDestino) {

        log.debug("Criando alerta de transição: descricao='{}', destino={}",
                descricao, unidadeDestino != null ? unidadeDestino.getSigla() : "null");

        if (unidadeDestino == null) {
            log.warn("Unidade destino é nula, alerta não será criado");
            return null;
        }

        Alerta alerta = new Alerta()
                .setProcesso(processo)
                .setDataHora(LocalDateTime.now())
                .setUnidadeOrigem(unidadeOrigem)
                .setUnidadeDestino(unidadeDestino)
                .setDescricao(descricao);

        return repositorioAlerta.save(alerta);
    }

    /**
     * Cria alertas para todas as unidades participantes quando um processo é iniciado.
     * Conforme CDU-04/CDU-05:
     * - Operacional: "Início do processo"
     * - Intermediária: "Início do processo em unidade(s) subordinada(s)"
     * - Interoperacional: Recebe os dois alertas
     * - Unidades ancestrais: Recebem o alerta de "unidade(s) subordinada(s)"
     */
    @Transactional
    public List<Alerta> criarAlertasProcessoIniciado(Processo processo, List<Unidade> unidadesParticipantes) {
        Set<Unidade> unidadesOperacionais = new HashSet<>();
        Map<Long, Unidade> unidadesIntermediarias = new HashMap<>();

        for (Unidade unidade : unidadesParticipantes) {
            // Unidades participantes sempre recebem o alerta operacional
            unidadesOperacionais.add(unidade);

            // Se for Interoperacional, também recebe o de intermediária conforme requisito
            if (unidade.getTipo() == TipoUnidade.INTEROPERACIONAL) {
                unidadesIntermediarias.put(unidade.getCodigo(), unidade);
            }

            // Notificar todos os ancestrais
            Unidade pai = unidade.getUnidadeSuperior();
            while (pai != null) {
                unidadesIntermediarias.put(pai.getCodigo(), pai);
                pai = pai.getUnidadeSuperior();
            }
        }

        List<Alerta> alertasCriados = new ArrayList<>();

        // Alertas operacionais
        for (Unidade u : unidadesOperacionais) {
            alertasCriados.add(criarAlerta(processo, PROCESSO_INICIADO_OPERACIONAL, u, "Início do processo"));
        }

        // Alertas intermediários (consolidado por unidade)
        for (Unidade u : unidadesIntermediarias.values()) {
            alertasCriados.add(criarAlerta(processo, PROCESSO_INICIADO_INTERMEDIARIA, u, "Início do processo em unidade(s) subordinada(s)"));
        }

        log.debug("Criados {} alertas para o processo {}", alertasCriados.size(), processo.getCodigo());
        return alertasCriados;
    }

    /**
     * Cria alerta quando cadastro é disponibilizado para validação.
     */
    @Transactional
    public void criarAlertaCadastroDisponibilizado(
            Processo processo, Unidade unidadeOrigem, Unidade unidadeDestino) {

        String descricao = "Cadastro disponibilizado pela unidade %s no processo '%s'. Realize a análise do cadastro."
                .formatted(unidadeOrigem.getSigla(), processo.getDescricao());

        criarAlerta(processo, TipoAlerta.CADASTRO_DISPONIBILIZADO, unidadeDestino, descricao);
    }

    /**
     * Cria alerta quando cadastro é devolvido para ajustes.
     */
    @Transactional
    public void criarAlertaCadastroDevolvido(Processo processo, Unidade unidadeDestino, String motivo) {

        String desc = "Cadastro devolvido no processo '%s'. Motivo: %s. Realize os ajustes necessários."
                .formatted(processo.getDescricao(), motivo);

        criarAlerta(processo, TipoAlerta.CADASTRO_DEVOLVIDO, unidadeDestino, desc);
    }

    @Transactional
    public void criarAlertaAlteracaoDataLimite(
            Processo processo, Unidade unidadeDestino, String novaData, int etapa) {

        String descricao = "Data limite da etapa %d alterada para %s"
                .formatted(etapa, novaData);

        Alerta alerta = new Alerta()
                .setProcesso(processo)
                .setDataHora(LocalDateTime.now())
                .setUnidadeOrigem(null) // SEDOC
                .setUnidadeDestino(unidadeDestino)
                .setDescricao(descricao);

        repositorioAlerta.save(alerta);
    }

    /**
     * Marca um alerta como lido para o usuário especificado.
     */
    @Transactional
    public void marcarComoLido(String usuarioTitulo, Long alertaCodigo) {
        AlertaUsuario.Chave id = new AlertaUsuario.Chave(alertaCodigo, usuarioTitulo);
        AlertaUsuario alertaUsuario = alertaUsuarioRepo.findById(id)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada(
                        "Não foi encontrado o alerta %d para o usuário %s".formatted(alertaCodigo, usuarioTitulo)));

        if (alertaUsuario.getDataHoraLeitura() == null) {
            alertaUsuario.setDataHoraLeitura(LocalDateTime.now());
            alertaUsuarioRepo.save(alertaUsuario);
            log.info("Alerta {} marcado como lido para o usuário {}", alertaCodigo, usuarioTitulo);
        }
    }

    /**
     * Lista alertas para o usuário baseado na sua unidade de lotação.
     * Implementa lazy creation: cria AlertaUsuario se não existir.
     */
    @Transactional
    public List<AlertaDto> listarAlertasPorUsuario(String usuarioTitulo) {
        // Buscar usuário para obter sua unidade de lotação
        Usuario usuario = usuarioService.buscarEntidadePorId(usuarioTitulo);

        if (usuario.getUnidadeLotacao() == null) {
            log.warn("Usuário {} sem unidade de lotação", usuarioTitulo);
            return List.of();
        }

        Long codUnidade = usuario.getUnidadeLotacao().getCodigo();

        // Buscar alertas da unidade do usuário
        List<Alerta> alertasUnidade = repositorioAlerta.findByUnidadeDestino_Codigo(codUnidade);

        // Para cada alerta, garantir que existe AlertaUsuario (lazy creation)
        return alertasUnidade.stream().map(alerta -> {
            AlertaUsuario.Chave chave = new AlertaUsuario.Chave(alerta.getCodigo(), usuarioTitulo);

            AlertaUsuario alertaUsuario = alertaUsuarioRepo.findById(chave)
                    .orElseGet(() -> {
                        AlertaUsuario novoAlertaUsuario = new AlertaUsuario();
                        novoAlertaUsuario.setId(chave);
                        novoAlertaUsuario.setAlerta(alerta);
                        novoAlertaUsuario.setUsuario(usuario);
                        novoAlertaUsuario.setDataHoraLeitura(null);
                        return alertaUsuarioRepo.save(novoAlertaUsuario);
                    });

            AlertaDto dto = alertaMapper.toDto(alerta);
            return AlertaDto.builder()
                    .codigo(dto.getCodigo())
                    .codProcesso(dto.getCodProcesso())
                    .unidadeOrigem(dto.getUnidadeOrigem())
                    .unidadeDestino(dto.getUnidadeDestino())
                    .descricao(dto.getDescricao())
                    .dataHora(dto.getDataHora())
                    .dataHoraLeitura(alertaUsuario.getDataHoraLeitura())
                    .mensagem(dto.getMensagem())
                    .dataHoraFormatada(dto.getDataHoraFormatada())
                    .processo(dto.getProcesso())
                    .origem(dto.getOrigem())
                    .build();
        }).toList();
    }

    public Page<Alerta> listarPorUsuario(String usuarioTitulo, Pageable pageable) {
        return repositorioAlerta.findByUsuarioDestino_TituloEleitoral(usuarioTitulo, pageable);
    }

    public Page<Alerta> listarPorUnidades(List<Long> unidadeIds, Pageable pageable) {
        return repositorioAlerta.findByUnidadeDestino_CodigoIn(unidadeIds, pageable);
    }

    public Page<Alerta> listarTodos(Pageable pageable) {
        return repositorioAlerta.findAll(pageable);
    }

    public Optional<LocalDateTime> obterDataHoraLeitura(Long codigoAlerta, String usuarioTitulo) {
        return alertaUsuarioRepo
                .findById(new AlertaUsuario.Chave(codigoAlerta, usuarioTitulo))
                .map(AlertaUsuario::getDataHoraLeitura);
    }
}
