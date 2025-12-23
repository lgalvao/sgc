package sgc.alerta;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.alerta.api.AlertaDto;
import sgc.alerta.internal.AlertaMapper;
import sgc.alerta.internal.model.*;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.processo.model.Processo;
import sgc.sgrh.dto.UnidadeDto;
import sgc.sgrh.model.Usuario;
import sgc.sgrh.model.UsuarioRepo;
import sgc.sgrh.SgrhService;
import sgc.subprocesso.model.Subprocesso;
import sgc.unidade.model.TipoUnidade;
import sgc.unidade.model.Unidade;
import sgc.unidade.model.UnidadeRepo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static sgc.alerta.internal.model.TipoAlerta.PROCESSO_INICIADO_INTERMEDIARIA;
import static sgc.alerta.internal.model.TipoAlerta.PROCESSO_INICIADO_OPERACIONAL;

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
    private final UnidadeRepo unidadeRepo;
    private final SgrhService sgrhService;
    private final UsuarioRepo usuarioRepo;
    private final AlertaMapper alertaMapper;

    /**
     * Cria um alerta básico para uma unidade destino.
     */
    @Transactional
    public Alerta criarAlerta(
            Processo processo,
            TipoAlerta tipoAlerta,
            Long codUnidadeDestino,
            String descricao) {

        log.debug("Criando alerta tipo={} para unidade {}.", tipoAlerta, codUnidadeDestino);

        Unidade unidadeDestino = unidadeRepo.findById(codUnidadeDestino)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Unidade", codUnidadeDestino));

        Alerta alerta = new Alerta()
                .setProcesso(processo)
                .setDataHora(LocalDateTime.now())
                .setUnidadeOrigem(null) // SEDOC não tem registro como unidade
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
     */
    @Transactional
    public List<Alerta> criarAlertasProcessoIniciado(Processo processo, List<Long> codigosUnidades, List<Subprocesso> subprocessos) {
        List<Alerta> alertasCriados = new ArrayList<>();

        for (Long codUnidade : codigosUnidades) {
            Optional<UnidadeDto> unidadeDtoOptional = sgrhService.buscarUnidadePorCodigo(codUnidade);
            if (unidadeDtoOptional.isEmpty()) {
                log.warn("Unidade não encontrada no SGRH: {}", codUnidade);
                continue;
            }

            TipoUnidade tipoUnidade = TipoUnidade.valueOf(unidadeDtoOptional.get().getTipo());

            // Operacional ou Interoperacional: alerta de início do processo
            if (tipoUnidade == TipoUnidade.OPERACIONAL || tipoUnidade == TipoUnidade.INTEROPERACIONAL) {
                Alerta alerta = criarAlerta(
                        processo,
                        PROCESSO_INICIADO_OPERACIONAL,
                        codUnidade,
                        "Início do processo");
                alertasCriados.add(alerta);
            }

            // Intermediária ou Interoperacional: alerta de unidades subordinadas
            if (tipoUnidade == TipoUnidade.INTERMEDIARIA || tipoUnidade == TipoUnidade.INTEROPERACIONAL) {
                Alerta alerta = criarAlerta(
                        processo,
                        PROCESSO_INICIADO_INTERMEDIARIA,
                        codUnidade,
                        "Início do processo em unidade(s) subordinada(s)");
                alertasCriados.add(alerta);
            }
        }

        log.debug("Criados {} alertas para o processo {}", alertasCriados.size(), processo.getCodigo());
        return alertasCriados;
    }

    /**
     * Cria alerta quando cadastro é disponibilizado para validação.
     */
    @Transactional
    public void criarAlertaCadastroDisponibilizado(
            Processo processo, Long codUnidadeOrigem, Long codUnidadeDestino) {

        Unidade unidadeOrigem = unidadeRepo.findById(codUnidadeOrigem)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Unidade de origem", codUnidadeOrigem));

        String descricao = "Cadastro disponibilizado pela unidade %s no processo '%s'. Realize a análise do cadastro."
                .formatted(unidadeOrigem.getSigla(), processo.getDescricao());

        criarAlerta(processo, TipoAlerta.CADASTRO_DISPONIBILIZADO, codUnidadeDestino, descricao);
    }

    /**
     * Cria alerta quando cadastro é devolvido para ajustes.
     */
    @Transactional
    public void criarAlertaCadastroDevolvido(Processo processo, Long codUnidadeDestino, String motivo) {

        String desc = "Cadastro devolvido no processo '%s'. Motivo: %s. Realize os ajustes necessários."
                .formatted(processo.getDescricao(), motivo);

        criarAlerta(processo, TipoAlerta.CADASTRO_DEVOLVIDO, codUnidadeDestino, desc);
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
        Usuario usuario = usuarioRepo.findById(usuarioTitulo)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Usuário", usuarioTitulo));

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
}
