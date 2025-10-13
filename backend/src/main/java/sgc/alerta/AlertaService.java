package sgc.alerta;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.alerta.modelo.TipoAlerta;
import sgc.alerta.modelo.Alerta;
import sgc.alerta.modelo.AlertaRepo;
import sgc.alerta.modelo.AlertaUsuario;
import sgc.alerta.modelo.AlertaUsuarioRepo;
import sgc.processo.modelo.Processo;
import sgc.sgrh.SgrhService;
import sgc.sgrh.Usuario;
import sgc.sgrh.UsuarioRepo;
import sgc.sgrh.dto.ResponsavelDto;
import sgc.sgrh.dto.UnidadeDto;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.unidade.modelo.TipoUnidade;
import sgc.unidade.modelo.Unidade;
import sgc.unidade.modelo.UnidadeRepo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    private final AlertaUsuarioRepo repositorioAlertaUsuario;
    private final UnidadeRepo repositorioUnidade;
    private final SgrhService servicoSgrh;
    private final UsuarioRepo usuarioRepo;

    /**
     * Cria um alerta para uma unidade específica.
     * Identifica automaticamente os destinatários (responsável + superiores).
     *
     * @param processo Processo relacionado ao alerta
     * @param tipoAlerta Tipo do alerta (PROCESSO_INICIADO_OPERACIONAL, etc)
     * @param codigoUnidadeDestino Código da unidade destino
     * @param descricao Descrição do alerta
     * @param dataLimite Data limite relacionada ao alerta (opcional)
     * @return Alerta criado
     */
    @Transactional
    public Alerta criarAlerta(
            Processo processo,
            TipoAlerta tipoAlerta,
            Long codigoUnidadeDestino,
            String descricao,
            LocalDate dataLimite) {

        log.debug("Criando alerta tipo={} para unidade={}", tipoAlerta, codigoUnidadeDestino);

        // Buscar unidade destino
        Unidade unidadeDestino = repositorioUnidade.findById(codigoUnidadeDestino)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unidade não encontrada: " + codigoUnidadeDestino));

        // Criar alerta
        Alerta alerta = new Alerta();
        alerta.setProcesso(processo);
        alerta.setDataHora(LocalDateTime.now());
        alerta.setUnidadeOrigem(null); // SEDOC não tem registro como unidade
        alerta.setUnidadeDestino(unidadeDestino);
        alerta.setDescricao(descricao);

        Alerta alertaSalvo = repositorioAlerta.save(alerta);
        log.info("Alerta criado: código={}, tipo={}, unidade={}",
                alertaSalvo.getCodigo(), tipoAlerta, unidadeDestino.getSigla());

        // Buscar responsável da unidade via SGRH
        try {
            Optional<ResponsavelDto> responsavel = servicoSgrh.buscarResponsavelUnidade(codigoUnidadeDestino);

            if (responsavel.isPresent() && responsavel.get().titularTitulo() != null) {
                // Criar AlertaUsuario para o titular
                criarAlertaUsuario(alertaSalvo, responsavel.get().titularTitulo(), codigoUnidadeDestino);

                // Se houver substituto, também o adiciona
                if (responsavel.get().substitutoTitulo() != null) {
                    criarAlertaUsuario(alertaSalvo, responsavel.get().substitutoTitulo(), codigoUnidadeDestino);
                }
            } else {
                log.warn("Responsável não encontrado para a unidade {}", codigoUnidadeDestino);
            }
        } catch (Exception e) {
            log.error("Erro ao buscar responsável da unidade {}: {}",
                    codigoUnidadeDestino, e.getMessage(), e);
            // Não interrompe o fluxo se não conseguir buscar o responsável
        }

        return alertaSalvo;
    }

    /**
     * Cria alertas diferenciados para processo iniciado.
     * Considera o tipo de cada unidade para criar mensagens apropriadas:
     * - OPERACIONAL: "Preencha atividades e conhecimentos"
     * - INTERMEDIÁRIA: "Aguarde mapas das unidades subordinadas"
     * - INTEROPERACIONAL: Cria 2 alertas (operacional + intermediária)
     *
     * @param processo Processo iniciado
     * @param codigosUnidades Lista de códigos das unidades
     * @param subprocessos Lista de subprocessos criados
     * @return Lista de alertas criados
     */
    @Transactional
    public List<Alerta> criarAlertasProcessoIniciado(
            Processo processo,
            List<Long> codigosUnidades,
            List<Subprocesso> subprocessos) {

        log.info("Criando alertas para processo iniciado: {} unidades", codigosUnidades.size());
        List<Alerta> alertasCriados = new ArrayList<>();

        for (Long codigoUnidade : codigosUnidades) {
            try {
                // Buscar tipo da unidade via SGRH
                Optional<UnidadeDto> unidadeDtoOptional = servicoSgrh.buscarUnidadePorCodigo(codigoUnidade);

                if (unidadeDtoOptional.isEmpty()) {
                    log.warn("Unidade não encontrada no SGRH: {}", codigoUnidade);
                    continue;
                }

                TipoUnidade tipoUnidade = TipoUnidade.valueOf(unidadeDtoOptional.get().tipo());
                String nomeProcesso = processo.getDescricao();
                // Encontrar o subprocesso correspondente para obter a data limite
                LocalDate dataLimite = subprocessos.stream()
                    .filter(sp -> sp.getUnidade() != null && sp.getUnidade().getCodigo().equals(codigoUnidade))
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

                    Alerta alerta = criarAlerta(processo, TipoAlerta.PROCESSO_INICIADO_OPERACIONAL, codigoUnidade, descricao, dataLimite);
                    alertasCriados.add(alerta);

                } else if (TipoUnidade.INTERMEDIARIA.equals(tipoUnidade)) {
                    String descricao = String.format(
                            "Início do processo '%s' em unidade(s) subordinada(s). " +
                            "Aguarde a disponibilização dos mapas para validação até %s.",
                            nomeProcesso,
                            formatarData(dataLimite)
                    );

                    Alerta alerta = criarAlerta(processo, TipoAlerta.PROCESSO_INICIADO_INTERMEDIARIA, codigoUnidade, descricao, dataLimite);
                    alertasCriados.add(alerta);

                } else if (TipoUnidade.INTEROPERACIONAL.equals(tipoUnidade)) {
                    String descOperacional = String.format(
                            "Início do processo '%s'. Preencha as atividades e conhecimentos até %s.",
                            nomeProcesso,
                            formatarData(dataLimite)
                    );
                    log.debug("Descrição para PROCESSO_INICIADO_INTEROPERACIONAL_OP: {}", descOperacional);
                    Alerta alertaOperacional = criarAlerta(processo, TipoAlerta.PROCESSO_INICIADO_INTEROPERACIONAL_OP, codigoUnidade, descOperacional, dataLimite);
                    alertasCriados.add(alertaOperacional);

                    String descIntermediaria = String.format(
                            "Início do processo '%s' em unidade(s) subordinada(s). " +
                            "Aguarde a disponibilização dos mapas para validação até %s.",
                            nomeProcesso,
                            formatarData(dataLimite)
                    );
                    log.debug("Descrição para PROCESSO_INICIADO_INTEROPERACIONAL_INT: {}", descIntermediaria);
                    Alerta alertaIntermediaria = criarAlerta(processo, TipoAlerta.PROCESSO_INICIADO_INTEROPERACIONAL_INT, codigoUnidade, descIntermediaria, dataLimite);
                    alertasCriados.add(alertaIntermediaria);

                } else {
                    log.warn("Tipo de unidade desconhecido: {} (unidade={})", tipoUnidade, codigoUnidade);
                }

            } catch (Exception e) {
                log.error("Erro ao criar alerta para a unidade {}: {}", codigoUnidade, e.getMessage(), e);
            }
        }

        log.info("Foram criados {} alertas para o processo {}", alertasCriados.size(), processo.getCodigo());
        return alertasCriados;
    }

    /**
     * Cria alerta para cadastro disponibilizado.
     *
     * @param processo Processo relacionado
     * @param codigoUnidadeOrigem Código da unidade que disponibilizou
     * @param codigoUnidadeDestino Código da unidade destino (SEDOC)
     * @return Alerta criado
     */
    @Transactional
    public Alerta criarAlertaCadastroDisponibilizado(
            Processo processo,
            Long codigoUnidadeOrigem,
            Long codigoUnidadeDestino) {

        Unidade unidadeOrigem = repositorioUnidade.findById(codigoUnidadeOrigem)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unidade de origem não encontrada: " + codigoUnidadeOrigem));

        String descricao = String.format(
                "Cadastro disponibilizado pela unidade %s no processo '%s'. " +
                "Realize a análise do cadastro.",
                unidadeOrigem.getSigla(),
                processo.getDescricao()
        );

        return criarAlerta(processo, TipoAlerta.CADASTRO_DISPONIBILIZADO, codigoUnidadeDestino, descricao, null);
    }

    /**
     * Cria alerta para cadastro devolvido.
     *
     * @param processo Processo relacionado
     * @param codigoUnidadeDestino Código da unidade que receberá o alerta
     * @param motivo Motivo da devolução
     * @return Alerta criado
     */
    @Transactional
    public Alerta criarAlertaCadastroDevolvido(
            Processo processo,
            Long codigoUnidadeDestino,
            String motivo) {

        String descricao = String.format(
                "Cadastro devolvido no processo '%s'. Motivo: %s. " +
                "Realize os ajustes necessários e disponibilize novamente.",
                processo.getDescricao(),
                motivo
        );

        return criarAlerta(processo, TipoAlerta.CADASTRO_DEVOLVIDO, codigoUnidadeDestino, descricao, null);
    }

    private void criarAlertaUsuario(Alerta alerta, String usuarioTitulo, Long codigoUnidade) {
        try {
            Usuario usuario = usuarioRepo.findById(usuarioTitulo)
                    .orElseGet(() -> {
                        log.info("Usuário {} não encontrado no banco de dados. Buscando no SGRH...", usuarioTitulo);
                        return servicoSgrh.buscarUsuarioPorTitulo(usuarioTitulo)
                                .map(usuarioDto -> {
                                    Usuario novoUsuario = new Usuario();
                                    novoUsuario.setTitulo(usuarioDto.titulo());
                                    novoUsuario.setNome(usuarioDto.nome());
                                    novoUsuario.setEmail(usuarioDto.email());
                                    novoUsuario.setRamal(null);
                                    repositorioUnidade.findById(codigoUnidade).ifPresent(novoUsuario::setUnidade);
                                    Usuario savedUsuario = usuarioRepo.save(novoUsuario);
                                    return usuarioRepo.findById(savedUsuario.getTitulo()).orElseThrow(); // Re-fetch to ensure it's fully managed
                                })
                                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado no SGRH: " + usuarioTitulo));
                    });

            AlertaUsuario alertaUsuario = new AlertaUsuario();
            AlertaUsuario.Chave id = new AlertaUsuario.Chave(alerta.getCodigo(), usuarioTitulo);
            alertaUsuario.setId(id);
            alertaUsuario.setAlerta(alerta);
            alertaUsuario.setUsuario(usuario); // Definir o usuário
            alertaUsuario.setDataHoraLeitura(null); // Não lido

            repositorioAlertaUsuario.save(alertaUsuario);
            log.debug("AlertaUsuario criado: alerta={}, usuario={}", alerta.getCodigo(), usuarioTitulo);
        } catch (Exception e) {
            log.error("Erro ao criar AlertaUsuario para o alerta={}, usuario={}: {}", alerta.getCodigo(), usuarioTitulo, e.getMessage(), e);
        }
    }

    private String formatarData(LocalDate data) {
        if (data == null) {
            return "data não definida";
        }
        return String.format("%02d/%02d/%d", data.getDayOfMonth(), data.getMonthValue(), data.getYear());
    }

    /**
     * Marca um alerta como lido para um usuário específico.
     * CDU-02
     *
     * @param usuarioTitulo O título (matrícula) do usuário que leu o alerta.
     * @param alertaId O ID do alerta a ser marcado como lido.
     */
    @Transactional
    public void marcarComoLido(String usuarioTitulo, Long alertaId) {
        AlertaUsuario.Chave id = new AlertaUsuario.Chave(alertaId, usuarioTitulo);
        AlertaUsuario alertaUsuario = repositorioAlertaUsuario.findById(id)
            .orElseThrow(() -> new sgc.comum.erros.ErroEntidadeNaoEncontrada(
                "Não foi encontrado o alerta " + alertaId + " para o usuário " + usuarioTitulo
            ));

        if (alertaUsuario.getDataHoraLeitura() == null) {
            alertaUsuario.setDataHoraLeitura(LocalDateTime.now());
            repositorioAlertaUsuario.save(alertaUsuario);
            log.info("Alerta {} marcado como lido para o usuário {}", alertaId, usuarioTitulo);
        }
    }
}