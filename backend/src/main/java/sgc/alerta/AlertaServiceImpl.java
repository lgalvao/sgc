package sgc.alerta;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.alerta.modelo.Alerta;
import sgc.alerta.modelo.AlertaRepo;
import sgc.alerta.modelo.AlertaUsuario;
import sgc.alerta.modelo.AlertaUsuarioRepo;
import sgc.processo.modelo.Processo;
import sgc.sgrh.SgrhService;
import sgc.sgrh.dto.ResponsavelDto;
import sgc.sgrh.dto.UnidadeDto;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.unidade.modelo.Unidade;
import sgc.unidade.modelo.UnidadeRepo;

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
    private final AlertaRepo repositorioAlerta;
    private final AlertaUsuarioRepo repositorioAlertaUsuario;
    private final UnidadeRepo repositorioUnidade;
    private final SgrhService servicoSgrh;
    
    @Override
    @Transactional
    public Alerta criarAlerta(
            Processo processo,
            String tipoAlerta,
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
                criarAlertaUsuario(alertaSalvo, responsavel.get().titularTitulo());
                
                // Se houver substituto, também o adiciona
                if (responsavel.get().substitutoTitulo() != null) {
                    criarAlertaUsuario(alertaSalvo, responsavel.get().substitutoTitulo());
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
    
    @Override
    @Transactional
    public List<Alerta> criarAlertasProcessoIniciado(
            Processo processo,
            List<Subprocesso> subprocessos) {
        
        log.info("Criando alertas para processo iniciado: {} subprocessos", subprocessos.size());
        List<Alerta> alertasCriados = new ArrayList<>();
        
        for (Subprocesso subprocesso : subprocessos) {
            if (subprocesso.getUnidade() == null) {
                log.warn("Subprocesso sem unidade: {}", subprocesso.getCodigo());
                continue;
            }
            
            Long codigoUnidade = subprocesso.getUnidade().getCodigo();
            
            try {
                // Buscar tipo da unidade via SGRH
                Optional<UnidadeDto> unidadeDtoOptional = servicoSgrh.buscarUnidadePorCodigo(codigoUnidade);
                
                if (unidadeDtoOptional.isEmpty()) {
                    log.warn("Unidade não encontrada no SGRH: {}", codigoUnidade);
                    continue;
                }
                
                String tipoUnidade = unidadeDtoOptional.get().tipo();
                String nomeProcesso = processo.getDescricao();
                LocalDate dataLimite = subprocesso.getDataLimiteEtapa1();
                
                // Criar alertas baseados no tipo de unidade
                if ("OPERACIONAL".equalsIgnoreCase(tipoUnidade)) {
                    String descricao = String.format(
                            "Início do processo '%s'. Preencha as atividades e conhecimentos até %s.",
                            nomeProcesso,
                            formatarData(dataLimite)
                    );
                    
                    Alerta alerta = criarAlerta(processo, "PROCESSO_INICIADO_OPERACIONAL", codigoUnidade, descricao, dataLimite);
                    alertasCriados.add(alerta);
                    
                } else if ("INTERMEDIARIA".equalsIgnoreCase(tipoUnidade)) {
                    String descricao = String.format(
                            "Início do processo '%s' em unidade(s) subordinada(s). " +
                            "Aguarde a disponibilização dos mapas para validação até %s.",
                            nomeProcesso,
                            formatarData(dataLimite)
                    );
                    
                    Alerta alerta = criarAlerta(processo, "PROCESSO_INICIADO_INTERMEDIARIA", codigoUnidade, descricao, dataLimite);
                    alertasCriados.add(alerta);
                    
                } else if ("INTEROPERACIONAL".equalsIgnoreCase(tipoUnidade)) {
                    String descOperacional = String.format(
                            "Início do processo '%s'. Preencha as atividades e conhecimentos até %s.",
                            nomeProcesso,
                            formatarData(dataLimite)
                    );
                    Alerta alertaOperacional = criarAlerta(processo, "PROCESSO_INICIADO_INTEROPERACIONAL_OP", codigoUnidade, descOperacional, dataLimite);
                    alertasCriados.add(alertaOperacional);
                    
                    String descIntermediaria = String.format(
                            "Início do processo '%s' em unidade(s) subordinada(s). " +
                            "Aguarde a disponibilização dos mapas para validação até %s.",
                            nomeProcesso,
                            formatarData(dataLimite)
                    );
                    Alerta alertaIntermediaria = criarAlerta(processo, "PROCESSO_INICIADO_INTEROPERACIONAL_INT", codigoUnidade, descIntermediaria, dataLimite);
                    alertasCriados.add(alertaIntermediaria);
                    
                } else {
                    log.warn("Tipo de unidade desconhecido: {} (unidade={})", tipoUnidade, codigoUnidade);
                }
                
            } catch (Exception e) {
                log.error("Erro ao criar alerta para o subprocesso {}: {}", subprocesso.getCodigo(), e.getMessage(), e);
            }
        }
        
        log.info("Foram criados {} alertas para o processo {}", alertasCriados.size(), processo.getCodigo());
        return alertasCriados;
    }
    
    @Override
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
        
        return criarAlerta(processo, "CADASTRO_DISPONIBILIZADO", codigoUnidadeDestino, descricao, null);
    }
    
    @Override
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
        
        return criarAlerta(processo, "CADASTRO_DEVOLVIDO", codigoUnidadeDestino, descricao, null);
    }
    
    private void criarAlertaUsuario(Alerta alerta, String usuarioTitulo) {
        try {
            AlertaUsuario alertaUsuario = new AlertaUsuario();
            AlertaUsuario.Chave id = new AlertaUsuario.Chave(alerta.getCodigo(), usuarioTitulo);
            alertaUsuario.setId(id);
            alertaUsuario.setAlerta(alerta);
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
}