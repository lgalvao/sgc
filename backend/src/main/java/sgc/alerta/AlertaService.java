package sgc.alerta;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.alerta.modelo.*;
import sgc.comum.erros.ErroDominioNaoEncontrado;
import sgc.processo.modelo.Processo;
import sgc.sgrh.Perfil;
import sgc.sgrh.SgrhService;
import sgc.sgrh.Usuario;
import sgc.sgrh.UsuarioRepo;
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

    /**
     * Cria um alerta para uma unidade específica.
     * Identifica automaticamente os destinatários (responsável + superiores).
     *
     * @param processo          Processo relacionado ao alerta
     * @param tipoAlerta        Tipo do alerta (PROCESSO_INICIADO_OPERACIONAL, etc)
     * @param codUnidadeDestino Código da unidade destino
     * @param descricao         Descrição do alerta
     * @param dataLimite        Data limite relacionada ao alerta (opcional)
     * @return Alerta criado
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
     * Cria alertas diferenciados para processo iniciado.
     * Considera o tipo de cada unidade para criar mensagens apropriadas:
     * - OPERACIONAL: "Preencha atividades e conhecimentos"
     * - INTERMEDIÁRIA: "Aguarde mapas das unidades subordinadas"
     * - INTEROPERACIONAL: Cria 2 alertas (operacional + intermediária)
     *
     * @param processo        Processo iniciado
     * @param codigosUnidades Lista de códigos das unidades
     * @param subprocessos    Lista de subprocessos criados
     * @return Lista de alertas criados
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
                log.error("Erro ao criar alerta para a unidade {}: {}", codUnidade, e.getClass().getSimpleName(), e);
            }
        }

        log.info("Foram criados {} alertas para o processo {}", alertasCriados.size(), processo.getCodigo());
        return alertasCriados;
    }

    /**
     * Cria alerta para cadastro disponibilizado.
     *
     * @param processo          Processo relacionado
     * @param codUnidadeOrigem  Código da unidade que disponibilizou
     * @param codUnidadeDestino Código da unidade destino (SEDOC)
     * @return Alerta criado
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
     * Cria alerta para cadastro devolvido.
     *
     * @param processo             Processo relacionado
     * @param codigoUnidadeDestino Código da unidade que receberá o alerta
     * @param motivo               Motivo da devolução
     * @return Alerta criado
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
                                    return usuarioRepo.save(novoUsuario);
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
     * Marca um alerta como lido para um usuário específico.
     * CDU-02
     *
     * @param alertaId O ID do alerta a ser marcado como lido.
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
}