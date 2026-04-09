package sgc.alerta;

import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import sgc.organizacao.*;
import sgc.alerta.model.*;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;
import sgc.processo.model.*;

import java.time.*;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AlertaFacade {
    private final AlertaService alertaService;
    private final UsuarioService usuarioService;
    private final UnidadeService unidadeService;
    private final UnidadeHierarquiaService unidadeHierarquiaService;

    private Unidade unidadeRaiz() {
        return unidadeService.buscarPorCodigo(1L);
    }

    public List<Alerta> alertasPorUsuario(ContextoUsuarioAutenticado contextoUsuario) {
        String usuarioTitulo = contextoUsuario.usuarioTitulo();
        List<Alerta> alertas;
        if (contextoUsuario.perfil() == Perfil.SERVIDOR) {
            alertas = alertaService.listarParaServidor(usuarioTitulo);
        } else {
            alertas = alertaService.listarParaGestao(contextoUsuario.unidadeAtivaCodigo(), usuarioTitulo);
        }

        if (alertas.isEmpty()) return Collections.emptyList();

        List<Long> alertaCodigos = alertas.stream().map(Alerta::getCodigo).toList();
        List<AlertaUsuario> leituras = alertaService.alertasUsuarios(usuarioTitulo, alertaCodigos);

        Map<Long, LocalDateTime> mapaLeitura = new HashMap<>();
        for (AlertaUsuario alertaUsuario : leituras) {
            LocalDateTime dataHoraLeitura = alertaUsuario.getDataHoraLeitura();
            if (dataHoraLeitura != null) mapaLeitura.put(alertaUsuario.getCodigo().getAlertaCodigo(), dataHoraLeitura);
        }

        alertas.forEach(alerta -> alerta.setDataHoraLeitura(mapaLeitura.get(alerta.getCodigo())));

        return alertas;
    }

    public List<Alerta> listarNaoLidos(ContextoUsuarioAutenticado contextoUsuario) {
        return alertasPorUsuario(contextoUsuario).stream()
                .filter(alerta -> alerta.getDataHoraLeitura() == null)
                .toList();
    }

    public Page<Alerta> listarPorUnidade(ContextoUsuarioAutenticado contextoUsuario, Pageable pageable) {
        // Regra CDU-02 (3.3): Ordenação decrescente obrigatória por data/hora
        Pageable sortedPageable = pageable.isPaged()
                ? PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "dataHora"))
                : pageable;

        // Regra expressiva CDU-02 (3.2)
        if (contextoUsuario.perfil() == Perfil.SERVIDOR) {
            return alertaService.listarParaServidorPaginado(contextoUsuario.usuarioTitulo(), sortedPageable);
        } else {
            return alertaService.listarParaGestaoPaginado(
                    contextoUsuario.unidadeAtivaCodigo(),
                    contextoUsuario.usuarioTitulo(),
                    sortedPageable
            );
        }
    }

    public Map<Long, LocalDateTime> obterMapaDataHoraLeitura(String usuarioTitulo, List<Long> alertaCodigos) {
        if (alertaCodigos.isEmpty()) {
            return Map.of();
        }
        return alertaService.alertasUsuarios(usuarioTitulo, alertaCodigos).stream()
                .filter(alertaUsuario -> alertaUsuario.getDataHoraLeitura() != null)
                .collect(java.util.stream.Collectors.toMap(
                        alertaUsuario -> alertaUsuario.getCodigo().getAlertaCodigo(),
                        AlertaUsuario::getDataHoraLeitura,
                        (primeira, ignorada) -> primeira,
                        HashMap::new
                ));
    }

    public Optional<LocalDateTime> obterDataHoraLeitura(Long codigoAlerta, String usuarioTitulo) {
        return alertaService.dataHoraLeituraAlertaUsuario(codigoAlerta, usuarioTitulo);
    }

    @Transactional
    public Alerta criarAlertaAdmin(Processo processo, Unidade destino, String descricao) {
        return criarAlerta(processo, unidadeRaiz(), destino, descricao);
    }

    @Transactional
    public void criarAlertaTransicao(Processo processo, String descricao, Unidade origem, Unidade destino) {
        criarAlerta(processo, origem, destino, descricao);
    }

    @Transactional
    public List<Alerta> criarAlertasProcessoIniciado(Processo processo, List<Unidade> unidadesParticipantes) {
        Set<Long> codsOperacionais = new HashSet<>();
        Set<Long> codsIntermediarias = new HashSet<>();
        Map<Long, Unidade> todasUnidadesMap = new HashMap<>();

        for (Unidade unidade : unidadesParticipantes) {
            Long codigoUnidade = unidade.getCodigo();
            todasUnidadesMap.put(codigoUnidade, unidade);
            TipoUnidade tipo = unidade.getTipo();

            if (tipo == TipoUnidade.OPERACIONAL || tipo == TipoUnidade.INTEROPERACIONAL || tipo == TipoUnidade.RAIZ) {
                codsOperacionais.add(codigoUnidade);
            }

            if (tipo == TipoUnidade.INTERMEDIARIA || tipo == TipoUnidade.INTEROPERACIONAL) {
                codsIntermediarias.add(codigoUnidade);
            }
        }

        Set<Long> codigosSuperiores = new HashSet<>();
        for (Unidade unidade : unidadesParticipantes) {
            codigosSuperiores.addAll(unidadeHierarquiaService.buscarCodigosSuperiores(unidade.getCodigo()));
        }
        if (!codigosSuperiores.isEmpty()) {
            List<Unidade> unidadesSuperiores = unidadeService.buscarPorCodigos(new java.util.ArrayList<>(codigosSuperiores));
            unidadesSuperiores.forEach(u -> {
                todasUnidadesMap.put(u.getCodigo(), u);
                codsIntermediarias.add(u.getCodigo());
            });
        }

        List<Alerta> alertasCriados = new ArrayList<>();
        for (Long cod : codsOperacionais) {
            alertasCriados.add(criarAlertaEntidade(processo, unidadeRaiz(),
                    obterUnidadeObrigatoria(todasUnidadesMap, cod), "Início do processo"));
        }

        for (Long cod : codsIntermediarias) {
            alertasCriados.add(criarAlertaEntidade(processo, unidadeRaiz(),
                    obterUnidadeObrigatoria(todasUnidadesMap, cod), "Início do processo em unidade(s) subordinada(s)"));
        }

        return alertaService.salvarTodos(alertasCriados);
    }

    @Transactional
    public void criarAlertaCadastroDisponibilizado(Processo processo, Unidade unidadeOrigem, Unidade unidadeDestino) {
        String desc = "Cadastro disponibilizado pela unidade %s no processo '%s'."
                .formatted(unidadeOrigem.getSigla(), processo.getDescricao());

        criarAlerta(processo, unidadeOrigem, unidadeDestino, desc);
    }

    @Transactional
    public void criarAlertaCadastroDevolvido(Processo processo, Unidade unidadeDestino, String motivo) {
        String desc = "Cadastro devolvido no processo '%s'. Motivo: %s."
                .formatted(processo.getDescricao(), motivo);

        criarAlerta(processo, unidadeRaiz(), unidadeDestino, desc);
    }

    @Transactional
    public void criarAlertaAlteracaoDataLimite(Processo processo, Unidade unidadeDestino, String novaData, int etapa) {
        String desc = "Data limite da etapa %d alterada para %s".formatted(etapa, novaData);
        criarAlerta(processo, unidadeRaiz(), unidadeDestino, desc);
    }

    private Alerta criarAlerta(Processo processo, Unidade origem, Unidade destino, String descricao) {
        Alerta alerta = criarAlertaEntidade(processo, origem, destino, descricao);

        return alertaService.salvar(alerta);
    }

    private Alerta criarAlertaEntidade(Processo processo, Unidade origem, Unidade destino, String descricao) {
        return Alerta.builder()
                .processo(processo)
                .dataHora(LocalDateTime.now())
                .unidadeOrigem(origem)
                .unidadeDestino(destino)
                .descricao(descricao)
                .build();
    }

    private Unidade obterUnidadeObrigatoria(Map<Long, Unidade> unidadesPorCodigo, Long codigoUnidade) {
        Unidade unidade = unidadesPorCodigo.get(codigoUnidade);
        if (unidade == null) {
            throw new IllegalStateException("Unidade %d ausente na construção de alertas".formatted(codigoUnidade));
        }
        return unidade;
    }

    @Transactional
    public void marcarComoLidos(ContextoUsuarioAutenticado contextoUsuario, List<Long> alertaCodigos) {
        if (alertaCodigos.isEmpty()) {
            return;
        }

        String usuarioTitulo = contextoUsuario.usuarioTitulo();
        Usuario usuario = usuarioService.buscar(usuarioTitulo);
        LocalDateTime agora = LocalDateTime.now();
        Map<Long, AlertaUsuario> existentesPorCodigo = alertaService.alertasUsuarios(usuarioTitulo, alertaCodigos).stream()
                .collect(java.util.stream.Collectors.toMap(
                        alertaUsuario -> alertaUsuario.getCodigo().getAlertaCodigo(),
                        alertaUsuario -> alertaUsuario,
                        (primeiro, ignorado) -> primeiro,
                        HashMap::new
                ));

        List<AlertaUsuario> alertasUsuariosParaSalvar = new ArrayList<>();
        for (AlertaUsuario alertaUsuario : existentesPorCodigo.values()) {
            if (alertaUsuario.getDataHoraLeitura() == null) {
                alertaUsuario.setDataHoraLeitura(agora);
                alertasUsuariosParaSalvar.add(alertaUsuario);
            }
        }

        List<Long> codigosAusentes = alertaCodigos.stream()
                .filter(codigo -> !existentesPorCodigo.containsKey(codigo))
                .toList();
        if (!codigosAusentes.isEmpty()) {
            Map<Long, Alerta> alertasPorCodigo = alertaService.listarPorCodigos(codigosAusentes).stream()
                    .collect(java.util.stream.Collectors.toMap(
                            Alerta::getCodigo,
                            alerta -> alerta,
                            (primeiro, ignorado) -> primeiro,
                            HashMap::new
                    ));

            codigosAusentes.forEach(codigo -> {
                Alerta alerta = alertasPorCodigo.get(codigo);
                if (alerta == null) {
                    return;
                }
                alertasUsuariosParaSalvar.add(AlertaUsuario.builder()
                        .codigo(AlertaUsuario.Chave.builder()
                                .alertaCodigo(codigo)
                                .usuarioTitulo(usuarioTitulo)
                                .build())
                        .alerta(alerta)
                        .usuario(usuario)
                        .dataHoraLeitura(agora)
                        .build());
            });
        }

        if (!alertasUsuariosParaSalvar.isEmpty()) {
            alertaService.salvarAlertasUsuarios(alertasUsuariosParaSalvar);
        }
    }

    @Transactional
    public void criarAlertaReaberturaCadastro(Processo processo, Unidade unidade) {
        String descricao = "Cadastro de atividades reaberto";
        criarAlertaAdmin(processo, unidade, descricao);
    }

    @Transactional
    public void criarAlertaReaberturaCadastroSuperior(Processo processo, Unidade superior, Unidade subordinada) {
        String descricao = "Cadastro da unidade %s reaberto".formatted(subordinada.getSigla());
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
