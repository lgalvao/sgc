package sgc.alerta;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.processo.Processo;
import sgc.sgrh.dto.ResponsavelDto;
import sgc.sgrh.dto.UnidadeDto;
import sgc.sgrh.service.SgrhService;
import sgc.subprocesso.Subprocesso;
import sgc.unidade.Unidade;
import sgc.unidade.UnidadeRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementação do serviço de alertas.
 * <p>
 * Cria alertas diferenciados baseados no tipo de unidade e contexto do processo,
 * integrando com SGRH para identificar destinatários.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AlertaServiceImpl implements AlertaService {
    
    private final AlertaRepository alertaRepository;
    private final AlertaUsuarioRepository alertaUsuarioRepository;
    private final UnidadeRepository unidadeRepository;
    private final SgrhService sgrhService;
    
    @Override
    @Transactional
    public Alerta criarAlerta(
            Processo processo,
            String tipoAlerta,
            Long unidadeDestinoCodigo,
            String descricao,
            LocalDate dataLimite) {
        
        log.debug("Criando alerta tipo={} para unidade={}", tipoAlerta, unidadeDestinoCodigo);
        
        // Buscar unidade destino
        Unidade unidadeDestino = unidadeRepository.findById(unidadeDestinoCodigo)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unidade não encontrada: " + unidadeDestinoCodigo));
        
        // Criar alerta
        Alerta alerta = new Alerta();
        alerta.setProcesso(processo);
        alerta.setDataHora(LocalDateTime.now());
        alerta.setUnidadeOrigem(null); // SEDOC não tem registro como unidade
        alerta.setUnidadeDestino(unidadeDestino);
        alerta.setDescricao(descricao);
        
        Alerta alertaSalvo = alertaRepository.save(alerta);
        log.info("Alerta criado: codigo={}, tipo={}, unidade={}", 
                alertaSalvo.getCodigo(), tipoAlerta, unidadeDestino.getSigla());
        
        // Buscar responsável da unidade via SGRH
        try {
            Optional<ResponsavelDto> responsavel = sgrhService.buscarResponsavelUnidade(unidadeDestinoCodigo);
            
            if (responsavel.isPresent() && responsavel.get().titularTitulo() != null) {
                // Criar AlertaUsuario para o titular
                criarAlertaUsuario(alertaSalvo, responsavel.get().titularTitulo());
                
                // Se houver substituto, também adiciona
                if (responsavel.get().substitutoTitulo() != null) {
                    criarAlertaUsuario(alertaSalvo, responsavel.get().substitutoTitulo());
                }
            } else {
                log.warn("Responsável não encontrado para unidade {}", unidadeDestinoCodigo);
            }
        } catch (Exception e) {
            log.error("Erro ao buscar responsável da unidade {}: {}", 
                    unidadeDestinoCodigo, e.getMessage(), e);
            // Não interrompe o fluxo se não conseguir buscar responsável
        }
        
        return alertaSalvo;
    }
    
    @Override
    @Transactional
    public List<Alerta> criarAlertasProcessoIniciado(
            Processo processo,
            List<Subprocesso> subprocessos) {
        
        log.info("Criando alertas para processo iniciado: {} subprocessos", subprocessos.size());
        List<Alerta> alertas = new ArrayList<>();
        
        for (Subprocesso subprocesso : subprocessos) {
            if (subprocesso.getUnidade() == null) {
                log.warn("Subprocesso sem unidade: {}", subprocesso.getCodigo());
                continue;
            }
            
            Long unidadeCodigo = subprocesso.getUnidade().getCodigo();
            
            try {
                // Buscar tipo da unidade via SGRH
                Optional<UnidadeDto> unidadeDto = sgrhService.buscarUnidadePorCodigo(unidadeCodigo);
                
                if (unidadeDto.isEmpty()) {
                    log.warn("Unidade não encontrada no SGRH: {}", unidadeCodigo);
                    continue;
                }
                
                String tipoUnidade = unidadeDto.get().tipo();
                String nomeProcesso = processo.getDescricao();
                LocalDate dataLimite = subprocesso.getDataLimiteEtapa1();
                
                // Criar alertas baseados no tipo de unidade
                if ("OPERACIONAL".equalsIgnoreCase(tipoUnidade)) {
                    // Alerta para unidade operacional
                    String descricao = String.format(
                            "Início do processo '%s'. Preencha atividades e conhecimentos até %s.",
                            nomeProcesso,
                            formatarData(dataLimite)
                    );
                    
                    Alerta alerta = criarAlerta(
                            processo,
                            "PROCESSO_INICIADO_OPERACIONAL",
                            unidadeCodigo,
                            descricao,
                            dataLimite
                    );
                    alertas.add(alerta);
                    
                } else if ("INTERMEDIARIA".equalsIgnoreCase(tipoUnidade)) {
                    // Alerta para unidade intermediária
                    String descricao = String.format(
                            "Início do processo '%s' em unidade(s) subordinada(s). " +
                            "Aguarde disponibilização dos mapas para validação até %s.",
                            nomeProcesso,
                            formatarData(dataLimite)
                    );
                    
                    Alerta alerta = criarAlerta(
                            processo,
                            "PROCESSO_INICIADO_INTERMEDIARIA",
                            unidadeCodigo,
                            descricao,
                            dataLimite
                    );
                    alertas.add(alerta);
                    
                } else if ("INTEROPERACIONAL".equalsIgnoreCase(tipoUnidade)) {
                    // Unidade interoperacional: criar 2 alertas (operacional + intermediária)
                    
                    // Alerta 1: Como operacional
                    String descricao1 = String.format(
                            "Início do processo '%s'. Preencha atividades e conhecimentos até %s.",
                            nomeProcesso,
                            formatarData(dataLimite)
                    );
                    
                    Alerta alerta1 = criarAlerta(
                            processo,
                            "PROCESSO_INICIADO_INTEROPERACIONAL_OP",
                            unidadeCodigo,
                            descricao1,
                            dataLimite
                    );
                    alertas.add(alerta1);
                    
                    // Alerta 2: Como intermediária
                    String descricao2 = String.format(
                            "Início do processo '%s' em unidade(s) subordinada(s). " +
                            "Aguarde disponibilização dos mapas para validação até %s.",
                            nomeProcesso,
                            formatarData(dataLimite)
                    );
                    
                    Alerta alerta2 = criarAlerta(
                            processo,
                            "PROCESSO_INICIADO_INTEROPERACIONAL_INT",
                            unidadeCodigo,
                            descricao2,
                            dataLimite
                    );
                    alertas.add(alerta2);
                    
                } else {
                    log.warn("Tipo de unidade desconhecido: {} (unidade={})", 
                            tipoUnidade, unidadeCodigo);
                }
                
            } catch (Exception e) {
                log.error("Erro ao criar alerta para subprocesso {}: {}", 
                        subprocesso.getCodigo(), e.getMessage(), e);
                // Continua processando outros subprocessos
            }
        }
        
        log.info("Criados {} alertas para processo {}", alertas.size(), processo.getCodigo());
        return alertas;
    }
    
    @Override
    @Transactional
    public Alerta criarAlertaCadastroDisponibilizado(
            Processo processo,
            Long unidadeOrigemCodigo,
            Long unidadeDestinoCodigo) {
        
        Unidade unidadeOrigem = unidadeRepository.findById(unidadeOrigemCodigo)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unidade origem não encontrada: " + unidadeOrigemCodigo));
        
        String descricao = String.format(
                "Cadastro disponibilizado pela unidade %s no processo '%s'. " +
                "Realize a análise do cadastro.",
                unidadeOrigem.getSigla(),
                processo.getDescricao()
        );
        
        return criarAlerta(
                processo,
                "CADASTRO_DISPONIBILIZADO",
                unidadeDestinoCodigo,
                descricao,
                null
        );
    }
    
    @Override
    @Transactional
    public Alerta criarAlertaCadastroDevolvido(
            Processo processo,
            Long unidadeDestinoCodigo,
            String motivo) {
        
        String descricao = String.format(
                "Cadastro devolvido no processo '%s'. Motivo: %s. " +
                "Realize os ajustes necessários e disponibilize novamente.",
                processo.getDescricao(),
                motivo
        );
        
        return criarAlerta(
                processo,
                "CADASTRO_DEVOLVIDO",
                unidadeDestinoCodigo,
                descricao,
                null
        );
    }
    
    /**
     * Cria entrada na tabela ALERTA_USUARIO vinculando alerta ao usuário.
     */
    private void criarAlertaUsuario(Alerta alerta, String usuarioTitulo) {
        try {
            AlertaUsuario alertaUsuario = new AlertaUsuario();
            AlertaUsuario.Id id = new AlertaUsuario.Id(alerta.getCodigo(), usuarioTitulo);
            alertaUsuario.setId(id);
            alertaUsuario.setAlerta(alerta);
            alertaUsuario.setDataHoraLeitura(null); // Não lido
            
            alertaUsuarioRepository.save(alertaUsuario);
            log.debug("AlertaUsuario criado: alerta={}, usuario={}", 
                    alerta.getCodigo(), usuarioTitulo);
        } catch (Exception e) {
            log.error("Erro ao criar AlertaUsuario para alerta={}, usuario={}: {}", 
                    alerta.getCodigo(), usuarioTitulo, e.getMessage(), e);
        }
    }
    
    /**
     * Formata data para exibição em mensagens.
     */
    private String formatarData(LocalDate data) {
        if (data == null) {
            return "data não definida";
        }
        return String.format("%02d/%02d/%d", 
                data.getDayOfMonth(), 
                data.getMonthValue(), 
                data.getYear());
    }
}