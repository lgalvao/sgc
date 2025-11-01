package sgc.alerta;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.alerta.modelo.*;
import sgc.comum.erros.ErroDominioNaoEncontrado;
import sgc.processo.modelo.Processo;
import sgc.sgrh.modelo.Perfil;
import sgc.sgrh.service.SgrhService;
import sgc.sgrh.modelo.Usuario;
import sgc.sgrh.modelo.UsuarioRepo;
import sgc.sgrh.dto.ResponsavelDto;
import sgc.sgrh.dto.UnidadeDto;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.unidade.modelo.TipoUnidade;
import sgc.unidade.modelo.Unidade;
import sgc.unidade.modelo.UnidadeRepo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import sgc.alerta.dto.AlertaDto;
import sgc.alerta.dto.AlertaMapper;

import static sgc.alerta.modelo.TipoAlerta.*;

/**
 * Serviço para gerenciar alertas do sistema.
 * <p>
 * Responsável por criar alertas diferenciados para unidades participantes
 * de processos, considerando os tipos de unidade (OPERACIONAL, INTERMEDIÁRIA, INTEROPERACIONAL).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AlertaService {
    private final AlertaRepo repositorioAlerta;
    private final AlertaUsuarioRepo alertaUsuarioRepo;
    private final UnidadeRepo unidadeRepo;
    private final SgrhService servicoSgrh;
    private final UsuarioRepo usuarioRepo;
    private final AlertaMapper alertaMapper;

    /**
     * Cria um alerta genérico para uma unidade específica e o associa aos seus
     * responsáveis (titular e substituto).
     * <p>
     * Este método busca a unidade de destino no repositório local e, em seguida,
     * busca os responsáveis (titular e substituto) da unidade no serviço SGRH.
     * Para cada responsável encontrado, cria um {@link AlertaUsuario} associando-o ao alerta.
     *
     * @param processo          O processo ao qual o alerta está associado.
     * @param tipoAlerta        O tipo de alerta a ser criado (e.g., PROCESSO_INICIADO_OPERACIONAL).
     * @param codUnidadeDestino O código da unidade para a qual o alerta é destinado.
     * @param descricao         O texto descritivo do alerta.
     * @param dataLimite        A data limite para a ação relacionada ao alerta (pode ser nulo).
     * @return A entidade {@link Alerta} que foi criada e persistida.
     * @throws ErroDominioNaoEncontrado se a unidade de destino não for encontrada.
     */
    @Transactional
    public Alerta criarAlerta(
            Processo processo,
            TipoAlerta tipoAlerta,
            Long codUnidadeDestino,
            String descricao,
            LocalDateTime dataLimite) {

        log.debug("Criando alerta tipo={} para unidade.", tipoAlerta);

        Unidade unidadeDestino = unidadeRepo.findById(codUnidadeDestino)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Unidade", codUnidadeDestino));

        Alerta alerta = new Alerta();
        alerta.setProcesso(processo);
        alerta.setDataHora(LocalDateTime.now());
        alerta.setUnidadeOrigem(null); // SEDOC não tem registro como unidade
        alerta.setUnidadeDestino(unidadeDestino);
        alerta.setDescricao(sanitizeHtml(descricao));

        Alerta alertaSalvo = repositorioAlerta.save(alerta);
        log.info("Alerta criado: código={}, tipo={}, unidade={}",
                alertaSalvo.getCodigo(), tipoAlerta, unidadeDestino.getNome());

        // Buscar responsável da unidade via SGRH
        try {
            Optional<ResponsavelDto> responsavel = servicoSgrh.buscarResponsavelUnidade(codUnidadeDestino);
            if (responsavel.isPresent() && responsavel.get().titularTitulo() != null) {
                criarAlertaUsuario(alertaSalvo, responsavel.get().titularTitulo(), codUnidadeDestino);
                // Se houver substituto, também o adiciona
                if (responsavel.get().substitutoTitulo() != null) {
                    criarAlertaUsuario(alertaSalvo, responsavel.get().substitutoTitulo(), codUnidadeDestino);
                }
            } else {
                log.warn("Responsável não encontrado para a unidade.");
            }
        } catch (Exception e) {
            log.error("Erro ao buscar responsável da unidade {}: {}", codUnidadeDestino, e.getClass().getSimpleName(), e);
            // Não interrompe o fluxo se não conseguir buscar o responsável
        }
        return alertaSalvo;
    }

    /**
     * Cria alertas específicos para o início de um processo, com mensagens
     * customizadas baseadas no tipo de cada unidade participante.
     * <p>
     * Para cada unidade, o método determina seu tipo (OPERACIONAL, INTERMEDIARIA,
     * INTEROPERACIONAL) consultando o serviço SGRH e gera alertas com descrições
     * adequadas à sua função no processo.
     * <p>
     * OPERACIONAL: Recebe um alerta para iniciar o preenchimento do mapa.
     * <p>
     * INTERMEDIARIA: Recebe um alerta para aguardar o envio dos mapas das unidades subordinadas.
     * <p>
     * INTEROPERACIONAL: Recebe ambos os tipos de alertas.
     *
     * @param processo        O processo que foi iniciado.
     * @param codigosUnidades A lista de códigos das unidades participantes.
     * @param subprocessos    A lista de subprocessos correspondentes, usada para obter a data limite de cada unidade.
     * @return Uma lista contendo todos os alertas que foram criados.
     */
    @Transactional
    public List<Alerta> criarAlertasProcessoIniciado(
            Processo processo,
            List<Long> codigosUnidades,
            List<Subprocesso> subprocessos) {

        log.info("Criando alertas para processo iniciado: {} unidades", codigosUnidades.size());
        List<Alerta> alertasCriados = new ArrayList<>();

        for (Long codUnidade : codigosUnidades) {
            try {
                // Buscar tipo da unidade via SGRH
                Optional<UnidadeDto> unidadeDtoOptional = servicoSgrh.buscarUnidadePorCodigo(codUnidade);
                if (unidadeDtoOptional.isEmpty()) {
                    log.warn("Unidade não encontrada no SGRH: {}", codUnidade);
                    continue;
                }

                TipoUnidade tipoUnidade = TipoUnidade.valueOf(unidadeDtoOptional.get().tipo());
                String nomeProcesso = processo.getDescricao();

                // Encontrar o subprocesso correspondente para obter a data limite
                LocalDateTime dataLimite = subprocessos.stream()
                        .filter(sp -> sp.getUnidade() != null && sp.getUnidade().getCodigo().equals(codUnidade))
                        .map(Subprocesso::getDataLimiteEtapa1)
                        .findFirst()
                        .orElse(processo.getDataLimite()); // Fallback para a data limite do processo se não encontrar subprocesso

                // Criar alertas baseados no tipo de unidade
                if (TipoUnidade.OPERACIONAL.equals(tipoUnidade)) {
                    String descricao = String.format(
                            "Início do processo '%s'. Preencha as atividades e conhecimentos até %s.",
                            nomeProcesso,
                            formatarData(dataLimite)
                    );

                    Alerta alerta = criarAlerta(processo, PROCESSO_INICIADO_OPERACIONAL, codUnidade, descricao, dataLimite);
                    alertasCriados.add(alerta);

                } else if (TipoUnidade.INTERMEDIARIA.equals(tipoUnidade)) {
                    String descricao = String.format(
                            "Início do processo '%s' em unidade(s) subordinada(s). " +
                                    "Aguarde a disponibilização dos mapas para validação até %s.",
                            nomeProcesso,
                            formatarData(dataLimite)
                    );

                    Alerta alerta = criarAlerta(processo, PROCESSO_INICIADO_INTERMEDIARIA, codUnidade, descricao, dataLimite);
                    alertasCriados.add(alerta);

                } else if (TipoUnidade.INTEROPERACIONAL.equals(tipoUnidade)) {
                    String descOperacional = String.format(
                            "Início do processo '%s'. Preencha as atividades e conhecimentos até %s.",
                            nomeProcesso,
                            formatarData(dataLimite)
                    );
                    log.debug("Descrição para PROCESSO_INICIADO_INTEROPERACIONAL_OP: {}", descOperacional);
                    Alerta alertaOperacional = criarAlerta(processo,
                            PROCESSO_INICIADO_INTEROPERACIONAL_OP,
                            codUnidade,
                            descOperacional,
                            dataLimite);

                    alertasCriados.add(alertaOperacional);

                    String descIntermediaria = String.format(
                            "Início do processo '%s' em unidade(s) subordinada(s). " +
                                    "Aguarde a disponibilização dos mapas para validação até %s.",
                            nomeProcesso,
                            formatarData(dataLimite)
                    );
                    log.debug("Descrição para PROCESSO_INICIADO_INTEROPERACIONAL_INT: {}", descIntermediaria);

                    Alerta alertaIntermediaria = criarAlerta(processo,
                            PROCESSO_INICIADO_INTEROPERACIONAL_INT,
                            codUnidade,
                            descIntermediaria,
                            dataLimite);

                    alertasCriados.add(alertaIntermediaria);

                } else {
                    log.warn("Tipo de unidade desconhecido: {} (unidade={})", tipoUnidade, codUnidade);
                }
            } catch (Exception e) {
                // TODO essa exceção precisa subir pra camada de controle
                log.error("Erro ao criar alerta para a unidade {}: {}", codUnidade, e.getClass().getSimpleName(), e);
            }
        }

        log.info("Foram criados {} alertas para o processo {}", alertasCriados.size(), processo.getCodigo());
        return alertasCriados;
    }

    /**
     * Cria um alerta para notificar que o cadastro de um mapa foi disponibilizado
     * por uma unidade.
     * <p>
     * O alerta é destinado à unidade de destino (geralmente a SEDOC), informando
     * qual unidade de origem concluiu o cadastro e que a análise já pode ser realizada.
     *
     * @param processo          O processo ao qual o cadastro pertence.
     * @param codUnidadeOrigem  O código da unidade que disponibilizou o cadastro.
     * @param codUnidadeDestino O código da unidade que deve analisar o cadastro (destino).
     * @return O {@link Alerta} criado.
     * @throws ErroDominioNaoEncontrado se a unidade de origem não for encontrada.
     */
    @Transactional
    public Alerta criarAlertaCadastroDisponibilizado(
            Processo processo,
            Long codUnidadeOrigem,
            Long codUnidadeDestino) {

        Unidade unidadeOrigem = unidadeRepo.findById(codUnidadeOrigem)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Unidade de origem", codUnidadeOrigem));

        String descricao = String.format(
                "Cadastro disponibilizado pela unidade %s no processo '%s'. Realize a análise do cadastro.",
                unidadeOrigem.getSigla(),
                processo.getDescricao()
        );

        return criarAlerta(processo, TipoAlerta.CADASTRO_DISPONIBILIZADO, codUnidadeDestino, descricao, null);
    }

    /**
     * Cria um alerta para notificar que um cadastro foi devolvido para ajustes.
     * <p>
     * O alerta é enviado para a unidade que originalmente submeteu o cadastro,
     * informando o motivo da devolução e solicitando que os ajustes necessários
     * sejam realizados.
     *
     * @param processo             O processo ao qual o cadastro pertence.
     * @param codigoUnidadeDestino O código da unidade que precisa ajustar o cadastro.
     * @param motivo               A descrição do motivo pelo qual o cadastro foi devolvido.
     * @return O {@link Alerta} criado.
     */
    @Transactional
    public Alerta criarAlertaCadastroDevolvido(
            Processo processo,
            Long codigoUnidadeDestino,
            String motivo) {

        String descricao = String.format(
                "Cadastro devolvido no processo '%s'. Motivo: %s. Realize os ajustes necessários e disponibilize novamente.",
                processo.getDescricao(),
                motivo
        );

        return criarAlerta(processo, TipoAlerta.CADASTRO_DEVOLVIDO, codigoUnidadeDestino, descricao, null);
    }

    private void criarAlertaUsuario(Alerta alerta, String tituloStr, Long codUnidade) {
        try {
            Long titulo = Long.parseLong(tituloStr);
            Usuario usuario = usuarioRepo.findById(titulo)
                    .orElseGet(() -> {
                        log.info("Usuário {} não encontrado no banco de dados. Buscando no SGRH...", titulo);
                        return servicoSgrh.buscarUsuarioPorTitulo(tituloStr)
                                .map(usuarioDto -> {
                                    Usuario novoUsuario = new Usuario();
                                    novoUsuario.setTituloEleitoral(Long.parseLong(usuarioDto.titulo()));
                                    novoUsuario.setNome(usuarioDto.nome());
                                    novoUsuario.setEmail(usuarioDto.email());
                                    novoUsuario.setPerfis(java.util.Set.of(Perfil.CHEFE)); // Default role
                                    unidadeRepo.findById(codUnidade).ifPresent(novoUsuario::setUnidade);
                                    return novoUsuario;
                                })
                                .orElseThrow(() -> new ErroDominioNaoEncontrado("Usuário", titulo));
                    });

            AlertaUsuario alertaUsuario = new AlertaUsuario();
            AlertaUsuario.Chave id = new AlertaUsuario.Chave(alerta.getCodigo(), titulo);
            alertaUsuario.setId(id);
            alertaUsuario.setAlerta(alerta);
            alertaUsuario.setUsuario(usuario);
            alertaUsuario.setDataHoraLeitura(null);

            alertaUsuarioRepo.save(alertaUsuario);
            log.debug("AlertaUsuario criado: alerta={}, usuario={}", alerta.getCodigo(), titulo);
        } catch (Exception e) {
            log.error("Erro ao criar AlertaUsuario para o alerta={}, usuario={}: {}", alerta.getCodigo(), tituloStr, e.getClass().getSimpleName(), e);
        }
    }

    private String formatarData(LocalDateTime data) {
        return data != null
                ? String.format("%02d/%02d/%d", data.getDayOfMonth(), data.getMonthValue(), data.getYear())
                : "Data não definida";
    }

    private String sanitizeHtml(String input) {
        if (input == null) {
            return null;
        }
        // Remove tags HTML básicas para uma sanitização simples
        return input.replaceAll("<[^>]*>", "");
    }

    /**
     * Marca um alerta específico como lido para um determinado usuário.
     * <p>
     * Este método localiza a associação {@link AlertaUsuario} pela sua chave composta
     * (código do alerta e título de eleitor do usuário) e, caso o alerta ainda não
     * tenha sido lido, define a data e hora da leitura como o momento atual.
     * <p>
     * Corresponde à ação do CDU-02: Visualizar alertas.
     *
     * @param usuarioTituloStr O título de eleitor do usuário (em formato String).
     * @param alertaId         O código do alerta a ser marcado como lido.
     * @throws ErroDominioNaoEncontrado se a associação entre o alerta e o usuário não for encontrada.
     * @throws NumberFormatException se o {@code usuarioTituloStr} não for um número válido.
     */
    @Transactional
    public void marcarComoLido(String usuarioTituloStr, Long alertaId) {
        Long usuarioTitulo = Long.parseLong(usuarioTituloStr);
        AlertaUsuario.Chave id = new AlertaUsuario.Chave(alertaId, usuarioTitulo);
        AlertaUsuario alertaUsuario = alertaUsuarioRepo.findById(id).orElseThrow(() -> new ErroDominioNaoEncontrado(
                "Não foi encontrado o alerta %d para o usuário %d".formatted(alertaId, usuarioTitulo))
        );

        if (alertaUsuario.getDataHoraLeitura() == null) {
            alertaUsuario.setDataHoraLeitura(LocalDateTime.now());
            alertaUsuarioRepo.save(alertaUsuario);
            log.info("Alerta {} marcado como lido para o usuário {}", alertaId, usuarioTitulo);
        }
    }

    /**
     * Lista todos os alertas para um usuário específico.
     *
     * @param usuarioTituloStr O título de eleitor do usuário.
     * @return Uma lista de {@link AlertaDto}.
     */
    @Transactional(readOnly = true)
    public List<AlertaDto> listarAlertasPorUsuario(String usuarioTituloStr) {
        Long usuarioTitulo = Long.parseLong(usuarioTituloStr);
        List<AlertaUsuario> alertasUsuario = alertaUsuarioRepo.findById_UsuarioTituloEleitoral(usuarioTitulo);

        return alertasUsuario.stream()
            .map(alertaUsuario -> {
                Alerta alerta = alertaUsuario.getAlerta();
                AlertaDto dto = alertaMapper.toDto(alerta);
                // Adicionar a data de leitura específica do usuário ao DTO
                return AlertaDto.builder()
                    .codigo(dto.getCodigo())
                    .codProcesso(dto.getCodProcesso())
                    .unidadeOrigem(dto.getUnidadeOrigem())
                    .unidadeDestino(dto.getUnidadeDestino())
                    .descricao(dto.getDescricao())
                    .dataHora(dto.getDataHora())
                    .dataHoraLeitura(alertaUsuario.getDataHoraLeitura())
                    .build();
            })
            .toList();
    }
}