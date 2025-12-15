package sgc.alerta;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.alerta.dto.AlertaDto;
import sgc.alerta.dto.AlertaMapper;
import sgc.alerta.erros.ErroAlerta;
import sgc.alerta.model.*;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.processo.model.Processo;
import sgc.sgrh.dto.ResponsavelDto;
import sgc.sgrh.dto.UnidadeDto;
import sgc.sgrh.model.Usuario;
import sgc.sgrh.model.UsuarioRepo;
import sgc.sgrh.service.SgrhService;
import sgc.subprocesso.model.Subprocesso;
import sgc.unidade.model.TipoUnidade;
import sgc.unidade.model.Unidade;
import sgc.unidade.model.UnidadeRepo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static sgc.alerta.model.TipoAlerta.*;

/**
 * Serviço para gerenciar alertas do sistema.
 *
 * <p>Responsável por criar alertas diferenciados para unidades participantes de processos,
 * considerando os tipos de unidade (OPERACIONAL, INTERMEDIÁRIA, INTEROPERACIONAL).
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
     * Cria um alerta genérico para uma unidade específica e o associa aos seus responsáveis
     * (titular e substituto).
     *
     * <p>Este método busca a unidade de destino no repositório local e, em seguida, busca os
     * responsáveis (titular e substituto) da unidade no serviço SGRH. Para cada responsável
     * encontrado, cria um {@link AlertaUsuario} associando-o ao alerta.
     *
     * @param processo          O processo ao qual o alerta está associado.
     * @param tipoAlerta        O tipo de alerta a ser criado (e.g., PROCESSO_INICIADO_OPERACIONAL).
     * @param codUnidadeDestino O código da unidade para a qual o alerta é destinado.
     * @param descricao         O texto descritivo do alerta.
     * @param dataLimite        A data limite para a ação relacionada ao alerta (pode ser nulo).
     * @return A entidade {@link Alerta} que foi criada e persistida.
     * @throws ErroEntidadeNaoEncontrada se a unidade de destino não for encontrada.
     */
    @Transactional
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    public Alerta criarAlerta(
            Processo processo,
            TipoAlerta tipoAlerta,
            Long codUnidadeDestino,
            String descricao,
            LocalDateTime dataLimite) {

        log.debug("Criando alerta tipo={} para unidade.", tipoAlerta);

        Unidade unidadeDestino = unidadeRepo.findById(codUnidadeDestino)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Unidade", codUnidadeDestino));

        Alerta alerta = new Alerta()
                .setProcesso(processo)
                .setDataHora(LocalDateTime.now())
                .setUnidadeOrigem(null) // SEDOC não tem registro como unidade
                .setUnidadeDestino(unidadeDestino)
                .setDescricao(descricao);

        Alerta alertaSalvo = repositorioAlerta.save(alerta);
        try {
            Optional<ResponsavelDto> responsavel = sgrhService.buscarResponsavelUnidade(codUnidadeDestino);
            if (responsavel.isPresent() && responsavel.get().getTitularTitulo() != null) {
                criarAlertaUsuario(alertaSalvo, responsavel.get().getTitularTitulo(), codUnidadeDestino);

                // Se houver substituto, também adiciona
                if (responsavel.get().getSubstitutoTitulo() != null) {
                    criarAlertaUsuario(
                            alertaSalvo,
                            responsavel.get().getSubstitutoTitulo(),
                            codUnidadeDestino);
                }
            }
        } catch (RuntimeException e) {
            log.warn(
                    "Erro ao buscar responsável da unidade {} no SGRH. Alerta criado sem associação"
                            + " de usuário: {}",
                    codUnidadeDestino,
                    e.getMessage());
        }
        return alertaSalvo;
    }

    /**
     * Cria alertas específicos para o início de um processo, com mensagens customizadas baseadas no
     * tipo de cada unidade participante.
     *
     * <p>Para cada unidade, o método determina seu tipo (OPERACIONAL, INTERMEDIARIA,
     * INTEROPERACIONAL) consultando o serviço SGRH e gera alertas com descrições adequadas à sua
     * função no processo.
     *
     * <p>OPERACIONAL: Recebe um alerta para iniciar o preenchimento do mapa.
     *
     * <p>INTERMEDIARIA: Recebe um alerta para aguardar o envio dos mapas das unidades subordinadas.
     *
     * <p>INTEROPERACIONAL: Recebe ambos os tipos de alertas.
     *
     * @param processo        O processo que foi iniciado.
     * @param codigosUnidades A lista de códigos das unidades participantes.
     * @param subprocessos    A lista de subprocessos correspondentes, usada para obter a data limite
     *                        de cada unidade.
     * @return Uma lista contendo todos os alertas que foram criados.
     */
    @Transactional
    public List<Alerta> criarAlertasProcessoIniciado(
            Processo processo, List<Long> codigosUnidades, List<Subprocesso> subprocessos) {

        log.debug("Criando alertas para processo iniciado: {} unidades", codigosUnidades.size());

        List<Alerta> alertasCriados = new ArrayList<>();
        for (Long codUnidade : codigosUnidades) {
            try {
                // Buscar tipo da unidade via SGRH
                Optional<UnidadeDto> unidadeDtoOptional = sgrhService.buscarUnidadePorCodigo(codUnidade);
                if (unidadeDtoOptional.isEmpty()) {
                    log.warn("Unidade não encontrada no SGRH: {}", codUnidade);
                    continue;
                }

                TipoUnidade tipoUnidade = TipoUnidade.valueOf(unidadeDtoOptional.get().getTipo());
                String nomeProcesso = processo.getDescricao();

                // Encontrar o subprocesso correspondente para obter a data limite
                LocalDateTime dataLimite = subprocessos.stream()
                        .filter(sp -> sp.getUnidade() != null
                                && sp.getUnidade()
                                .getCodigo()
                                .equals(codUnidade))
                        .map(Subprocesso::getDataLimiteEtapa1)
                        .findFirst()
                        .orElse(processo.getDataLimite());

                // Criar alertas baseados no tipo de unidade
                switch (tipoUnidade) {
                    case OPERACIONAL -> {
                        String desc =
                                "Início do processo '%s'. Preencha as atividades e conhecimentos até %s."
                                        .formatted(nomeProcesso, fmtData(dataLimite));
                        Alerta alerta = criarAlerta(
                                processo,
                                PROCESSO_INICIADO_OPERACIONAL,
                                codUnidade,
                                desc,
                                dataLimite);
                        alertasCriados.add(alerta);
                    }
                    case INTERMEDIARIA -> {
                        String desc = """
                                Início do processo '%s' em unidade(s) subordinada(s).%n
                                Aguarde a disponibilização dos mapas para validação até %s."""
                                .formatted(nomeProcesso, fmtData(dataLimite));

                        Alerta alerta = criarAlerta(
                                processo,
                                PROCESSO_INICIADO_INTERMEDIARIA,
                                codUnidade,
                                desc,
                                dataLimite);
                        alertasCriados.add(alerta);
                    }
                    case INTEROPERACIONAL -> {
                        String desc = "Início do processo '%s'. Preencha as atividades e conhecimentos até %s."
                                .formatted(nomeProcesso, fmtData(dataLimite));

                        Alerta alertaOperacional = criarAlerta(
                                processo,
                                PROCESSO_INICIADO_INTEROPERACIONAL_OP,
                                codUnidade,
                                desc,
                                dataLimite);
                        alertasCriados.add(alertaOperacional);

                        String descIntermediaria = """
                                Início do processo '%s' em unidade(s) subordinada(s).
                                %nAguarde a disponibilização dos mapas para validação até %s."""
                                .formatted(nomeProcesso, fmtData(dataLimite));

                        Alerta alertaIntermediaria = criarAlerta(
                                processo,
                                PROCESSO_INICIADO_INTEROPERACIONAL_INT,
                                codUnidade,
                                descIntermediaria,
                                dataLimite);

                        alertasCriados.add(alertaIntermediaria);
                    }
                    default -> {
                    }
                }
            } catch (RuntimeException e) {
                log.error(
                        "Erro ao criar alerta para a unidade {}: {}",
                        codUnidade,
                        e.getClass().getSimpleName(),
                        e);
                throw new ErroAlerta(
                        "Falha ao criar alerta para a unidade %d: %s"
                                .formatted(codUnidade, e.getMessage()),
                        e);
            }
        }

        log.debug("Foram criados {} alertas para o processo {}", alertasCriados.size(), processo.getCodigo());
        return alertasCriados;
    }

    /**
     * Cria um alerta para notificar que o cadastro de um mapa foi disponibilizado por uma unidade.
     *
     * <p>O alerta é destinado à unidade de destino (geralmente a SEDOC), informando qual unidade de
     * origem concluiu o cadastro e que a análise já pode ser realizada.
     *
     * @param processo          O processo ao qual o cadastro pertence.
     * @param codUnidadeOrigem  O código da unidade que disponibilizou o cadastro.
     * @param codUnidadeDestino O código da unidade que deve analisar o cadastro (destino).
     * @throws ErroEntidadeNaoEncontrada se a unidade de origem não for encontrada.
     */
    @Transactional
    public void criarAlertaCadastroDisponibilizado(
            Processo processo, Long codUnidadeOrigem, Long codUnidadeDestino) {

        Unidade unidadeOrigem =
                unidadeRepo
                        .findById(codUnidadeOrigem)
                        .orElseThrow(
                                () ->
                                        new ErroEntidadeNaoEncontrada(
                                                "Unidade de origem", codUnidadeOrigem));

        String descricao = """
                Cadastro disponibilizado pela unidade %s no processo
                '%s'.%nRealize a análise do cadastro.""".formatted(unidadeOrigem.getSigla(), processo.getDescricao());

        criarAlerta(processo, TipoAlerta.CADASTRO_DISPONIBILIZADO, codUnidadeDestino, descricao, null);
    }

    /**
     * Cria um alerta para notificar que um cadastro foi devolvido para ajustes.
     *
     * <p>O alerta é enviado para a unidade que originalmente submeteu o cadastro, informando o
     * motivo da devolução e solicitando que os ajustes necessários sejam realizados.
     *
     * @param processo          O processo ao qual o cadastro pertence.
     * @param codUnidadeDestino O código da unidade que precisa ajustar o cadastro.
     * @param motivo            A descrição do motivo pelo qual o cadastro foi devolvido.
     */
    @Transactional
    public void criarAlertaCadastroDevolvido(
            Processo processo, Long codUnidadeDestino, String motivo) {
        String desc = """
                Cadastro devolvido no processo '%s'. Motivo: %s.
                Realize os ajustes necessários e disponibilize novamente."""
                .formatted(processo.getDescricao(), motivo);

        criarAlerta(processo, TipoAlerta.CADASTRO_DEVOLVIDO, codUnidadeDestino, desc, null);
    }

    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    private void criarAlertaUsuario(Alerta alerta, String titulo, Long codUnidade) {
        try {
            Usuario usuario = usuarioRepo.findById(titulo)
                    .orElseGet(() -> {
                        log.info("Usuário {} não encontrado no banco de dados. Buscando no SGRH.", titulo);
                        return sgrhService.buscarUsuarioPorTitulo(titulo)
                                .map(usuarioDto -> {
                                    Unidade unidade = unidadeRepo
                                            .findById(codUnidade)
                                            .orElseThrow(
                                                    () -> new ErroEntidadeNaoEncontrada("Unidade", codUnidade)
                                            );
                                    Usuario novoUsuario = new Usuario();
                                    novoUsuario.setTituloEleitoral(usuarioDto.getTitulo());
                                    novoUsuario.setNome(usuarioDto.getNome());
                                    novoUsuario.setEmail(usuarioDto.getEmail());
                                    novoUsuario.setUnidadeLotacao(unidade);
                                    return novoUsuario;
                                }).orElseThrow(() -> new ErroEntidadeNaoEncontrada("Usuário", titulo));
                    });

            AlertaUsuario alertaUsuario = new AlertaUsuario();
            AlertaUsuario.Chave id = new AlertaUsuario.Chave(alerta.getCodigo(), titulo);
            alertaUsuario.setId(id);
            alertaUsuario.setAlerta(alerta);
            alertaUsuario.setUsuario(usuario);
            alertaUsuario.setDataHoraLeitura(null);

            alertaUsuarioRepo.save(alertaUsuario);
            log.debug("AlertaUsuario criado: alerta={}, usuario={}", alerta.getCodigo(), titulo);
        } catch (RuntimeException e) {
            log.error("Erro ao criar AlertaUsuario para o alerta={}, usuario={}: {}",
                    alerta.getCodigo(),
                    titulo,
                    e.getClass().getSimpleName(),
                    e);
        }
    }

    private String fmtData(LocalDateTime data) {
        return data != null
                ? String.format(
                "%02d/%02d/%d", data.getDayOfMonth(), data.getMonthValue(), data.getYear())
                : "Data não definida";
    }

    /**
     * Marca um alerta específico como lido para um determinado usuário.
     *
     * <p>Este método localiza a associação {@link AlertaUsuario} pela sua chave composta (código do
     * alerta e título de eleitor do usuário) e, caso o alerta ainda não tenha sido lido, define a
     * data e hora da leitura como o momento atual.
     *
     * <p>Corresponde à ação do CDU-02: Visualizar alertas.
     *
     * @param usuarioTitulo O título de eleitor do usuário (em formato String).
     * @param alertaId      O código do alerta a ser marcado como lido.
     * @throws ErroEntidadeNaoEncontrada se a associação entre o alerta e o usuário não for
     *                                   encontrada.
     * @throws NumberFormatException     se o {@code usuarioTituloStr} não for um número válido.
     */
    @Transactional
    public void marcarComoLido(String usuarioTitulo, Long alertaId) {
        AlertaUsuario.Chave id = new AlertaUsuario.Chave(alertaId, usuarioTitulo);
        AlertaUsuario alertaUsuario = alertaUsuarioRepo.findById(id)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada(
                        "Não foi encontrado o alerta %d para o usuário %s".formatted(alertaId, usuarioTitulo)));

        if (alertaUsuario.getDataHoraLeitura() == null) {
            alertaUsuario.setDataHoraLeitura(LocalDateTime.now());
            alertaUsuarioRepo.save(alertaUsuario);
            log.info("Alerta {} marcado como lido para o usuário {}", alertaId, usuarioTitulo);
        }
    }

    /**
     * Lista todos os alertas para um usuário específico.
     *
     * @param usuarioTitulo O título de eleitor do usuário.
     * @return Uma lista de {@link AlertaDto}.
     */
    @Transactional(readOnly = true)
    public List<AlertaDto> listarAlertasPorUsuario(String usuarioTitulo) {
        List<AlertaUsuario> alertasUsuario =
                alertaUsuarioRepo.findById_UsuarioTitulo(usuarioTitulo);

        return alertasUsuario.stream().map(alertaUsuario -> {
            Alerta alerta = alertaUsuario.getAlerta();
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
