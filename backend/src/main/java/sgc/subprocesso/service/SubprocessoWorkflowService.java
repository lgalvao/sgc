package sgc.subprocesso.service;

import static sgc.subprocesso.model.SituacaoSubprocesso.*;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.analise.AnaliseService;
import sgc.analise.dto.CriarAnaliseRequest;
import sgc.analise.model.TipoAcaoAnalise;
import sgc.analise.model.TipoAnalise;
import sgc.comum.erros.ErroAccessoNegado;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.comum.erros.ErroValidacao;
import sgc.mapa.model.Mapa;
import sgc.mapa.service.ImpactoMapaService;
import sgc.processo.eventos.*;
import sgc.processo.model.TipoProcesso;
import sgc.sgrh.model.Usuario;
import sgc.subprocesso.dto.SubmeterMapaAjustadoReq;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.unidade.model.Unidade;
import sgc.unidade.model.UnidadeRepo;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubprocessoWorkflowService {
    private final SubprocessoRepo repositorioSubprocesso;
    private final ApplicationEventPublisher publicadorDeEventos;
    private final UnidadeRepo unidadeRepo;
    private final AnaliseService analiseService;
    private final SubprocessoService subprocessoService;
    private final ImpactoMapaService impactoMapaService;

    @Transactional
    public void disponibilizarCadastro(Long codSubprocesso, Usuario usuario) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);

        validarSubprocessoParaDisponibilizacao(sp, usuario, codSubprocesso);
        sp.setSituacao(MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
        sp.setDataFimEtapa1(java.time.LocalDateTime.now());
        repositorioSubprocesso.save(sp);

        analiseService.removerPorSubprocesso(sp.getCodigo());

        Unidade unidadeSuperior =
                sp.getUnidade() != null ? sp.getUnidade().getUnidadeSuperior() : null;

        publicadorDeEventos.publishEvent(
                EventoSubprocessoCadastroDisponibilizado.builder()
                        .codSubprocesso(codSubprocesso)
                        .usuario(usuario)
                        .unidadeOrigem(sp.getUnidade())
                        .unidadeDestino(unidadeSuperior)
                        .build());
    }

    @Transactional
    public void disponibilizarRevisao(Long codSubprocesso, Usuario usuario) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);

        validarSubprocessoParaDisponibilizacao(sp, usuario, codSubprocesso);
        sp.setSituacao(REVISAO_CADASTRO_DISPONIBILIZADA);
        sp.setDataFimEtapa1(java.time.LocalDateTime.now());
        repositorioSubprocesso.save(sp);

        Unidade unidadeSuperior =
                sp.getUnidade() != null ? sp.getUnidade().getUnidadeSuperior() : null;
        analiseService.removerPorSubprocesso(sp.getCodigo());

        publicadorDeEventos.publishEvent(
                EventoSubprocessoRevisaoDisponibilizada.builder()
                        .codSubprocesso(codSubprocesso)
                        .usuario(usuario)
                        .unidadeOrigem(sp.getUnidade())
                        .unidadeDestino(unidadeSuperior)
                        .build());
    }

    private void validarSubprocessoParaDisponibilizacao(
            Subprocesso sp, Usuario usuario, Long codSubprocesso) {
        Unidade unidadeSubprocesso = sp.getUnidade();
        Usuario titularUnidade = unidadeSubprocesso.getTitular();

        if (!titularUnidade.equals(usuario)) {
            String msg =
                    "Usuário %s não é o titular da unidade (%s). Titular é %s"
                            .formatted(
                                    usuario.getTituloEleitoral(),
                                    unidadeSubprocesso.getSigla(),
                                    titularUnidade.getTituloEleitoral());
            throw new ErroAccessoNegado(msg);
        }
        if (!subprocessoService.obterAtividadesSemConhecimento(codSubprocesso).isEmpty()) {
            throw new ErroValidacao("Existem atividades sem conhecimentos associados.");
        }
        Mapa mapa = sp.getMapa();
        if (mapa == null || mapa.getCodigo() == null) {
            // TODO usar uma execção de negócio específica
            throw new IllegalStateException("Subprocesso sem mapa associado");
        }
    }

    @Transactional
    public void disponibilizarMapa(
            Long codSubprocesso,
            String observacoes,
            LocalDateTime dataLimiteEtapa2,
            Usuario usuario) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);

        final SituacaoSubprocesso situacaoAtual = sp.getSituacao();
        if (situacaoAtual != REVISAO_CADASTRO_HOMOLOGADA
                && situacaoAtual != REVISAO_MAPA_AJUSTADO) {
            // TODO usar uma execção de negócio específica
            throw new IllegalStateException(
                    "O mapa de competências só pode ser disponibilizado a partir dos estados"
                            + " 'Revisão de Cadastro Homologada' ou 'Mapa Ajustado'. Estado atual: "
                            + situacaoAtual);
        }
        if (sp.getMapa() == null) {
            // TODO usar uma execção de negócio específica
            throw new IllegalStateException("Subprocesso sem mapa associado");
        }

        subprocessoService.validarAssociacoesMapa(sp.getMapa().getCodigo());

        sp.getMapa().setSugestoes(null);
        analiseService.removerPorSubprocesso(codSubprocesso);

        if (observacoes != null && !observacoes.isBlank()) {
            sp.getMapa().setSugestoes(observacoes);
        }

        if (sp.getProcesso().getTipo() == TipoProcesso.MAPEAMENTO) {
            sp.setSituacao(MAPEAMENTO_MAPA_DISPONIBILIZADO);
        } else {
            sp.setSituacao(REVISAO_MAPA_DISPONIBILIZADO);
        }

        sp.setDataLimiteEtapa2(dataLimiteEtapa2);
        sp.setDataFimEtapa1(java.time.LocalDateTime.now());
        repositorioSubprocesso.save(sp);

        Unidade sedoc =
                unidadeRepo
                        .findBySigla("SEDOC")
                        .orElseThrow(
                                () -> new IllegalStateException("Unidade 'SEDOC' não encontrada."));

        publicadorDeEventos.publishEvent(
                EventoSubprocessoMapaDisponibilizado.builder()
                        .codSubprocesso(codSubprocesso)
                        .usuario(usuario)
                        .unidadeOrigem(sedoc)
                        .unidadeDestino(sp.getUnidade())
                        .observacoes(observacoes)
                        .build());
    }

    @Transactional
    public void apresentarSugestoes(Long codSubprocesso, String sugestoes, Usuario usuario) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);

        if (sp.getMapa() != null) {
            sp.getMapa().setSugestoes(sugestoes);
        }

        if (sp.getProcesso().getTipo() == TipoProcesso.MAPEAMENTO) {
            sp.setSituacao(MAPEAMENTO_MAPA_COM_SUGESTOES);
        } else {
            sp.setSituacao(REVISAO_MAPA_COM_SUGESTOES);
        }

        sp.setDataFimEtapa2(java.time.LocalDateTime.now());
        repositorioSubprocesso.save(sp);

        analiseService.removerPorSubprocesso(sp.getCodigo());

        publicadorDeEventos.publishEvent(
                EventoSubprocessoMapaComSugestoes.builder()
                        .codSubprocesso(codSubprocesso)
                        .usuario(usuario)
                        .unidadeOrigem(sp.getUnidade())
                        .unidadeDestino(sp.getUnidade().getUnidadeSuperior())
                        .observacoes(sugestoes)
                        .build());
    }

    @Transactional
    public void validarMapa(Long codSubprocesso, Usuario usuario) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);

        if (sp.getProcesso().getTipo() == TipoProcesso.MAPEAMENTO) {
            sp.setSituacao(MAPEAMENTO_MAPA_VALIDADO);
        } else {
            sp.setSituacao(REVISAO_MAPA_VALIDADO);
        }

        sp.setDataFimEtapa2(java.time.LocalDateTime.now());
        repositorioSubprocesso.save(sp);

        publicadorDeEventos.publishEvent(
                EventoSubprocessoMapaValidado.builder()
                        .codSubprocesso(codSubprocesso)
                        .usuario(usuario)
                        .unidadeOrigem(sp.getUnidade())
                        .unidadeDestino(sp.getUnidade().getUnidadeSuperior())
                        .build());
    }

    @Transactional
    public void devolverValidacao(Long codSubprocesso, String justificativa, Usuario usuario) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);

        analiseService.criarAnalise(
                CriarAnaliseRequest.builder()
                        .codSubprocesso(codSubprocesso)
                        .observacoes(justificativa)
                        .tipo(TipoAnalise.VALIDACAO)
                        .acao(TipoAcaoAnalise.DEVOLUCAO_MAPEAMENTO)
                        .siglaUnidade(sp.getUnidade().getUnidadeSuperior().getSigla())
                        .tituloUsuario(String.valueOf(usuario.getTituloEleitoral()))
                        .motivo(justificativa)
                        .build());

        Unidade unidadeDevolucao = sp.getUnidade();

        if (sp.getProcesso().getTipo() == TipoProcesso.MAPEAMENTO) {
            sp.setSituacao(MAPEAMENTO_MAPA_DISPONIBILIZADO);
        } else {
            sp.setSituacao(REVISAO_MAPA_DISPONIBILIZADO);
        }

        sp.setDataFimEtapa2(null);
        repositorioSubprocesso.save(sp);

        publicadorDeEventos.publishEvent(
                EventoSubprocessoMapaDevolvido.builder()
                        .codSubprocesso(codSubprocesso)
                        .usuario(usuario)
                        .unidadeOrigem(sp.getUnidade().getUnidadeSuperior())
                        .unidadeDestino(unidadeDevolucao)
                        .motivo(justificativa)
                        .build());
    }

    @Transactional
    public void aceitarValidacao(Long codSubprocesso, Usuario usuario) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);

        analiseService.criarAnalise(
                CriarAnaliseRequest.builder()
                        .codSubprocesso(codSubprocesso)
                        .observacoes("Aceite da validação")
                        .tipo(TipoAnalise.VALIDACAO)
                        .acao(TipoAcaoAnalise.ACEITE_MAPEAMENTO)
                        .siglaUnidade(sp.getUnidade().getUnidadeSuperior().getSigla())
                        .tituloUsuario(String.valueOf(usuario.getTituloEleitoral()))
                        .motivo(null)
                        .build());

        Unidade unidadeSuperior = sp.getUnidade().getUnidadeSuperior();
        Unidade proximaUnidade =
                unidadeSuperior != null ? unidadeSuperior.getUnidadeSuperior() : null;

        if (proximaUnidade == null) {
            if (sp.getProcesso().getTipo() == TipoProcesso.MAPEAMENTO) {
                sp.setSituacao(MAPEAMENTO_MAPA_HOMOLOGADO);
            } else {
                sp.setSituacao(REVISAO_MAPA_HOMOLOGADO);
            }
            repositorioSubprocesso.save(sp);
        } else {
            if (sp.getProcesso().getTipo() == TipoProcesso.MAPEAMENTO) {
                sp.setSituacao(MAPEAMENTO_MAPA_VALIDADO);
            } else {
                sp.setSituacao(REVISAO_MAPA_VALIDADO);
            }
            repositorioSubprocesso.save(sp);

            publicadorDeEventos.publishEvent(
                    EventoSubprocessoMapaAceito.builder()
                            .codSubprocesso(codSubprocesso)
                            .usuario(usuario)
                            .unidadeOrigem(unidadeSuperior)
                            .unidadeDestino(proximaUnidade)
                            .build());
        }
    }

    @Transactional
    public void homologarValidacao(Long codSubprocesso, Usuario usuario) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);

        if (sp.getProcesso().getTipo() == TipoProcesso.MAPEAMENTO) {
            sp.setSituacao(MAPEAMENTO_MAPA_HOMOLOGADO);
        } else {
            sp.setSituacao(REVISAO_MAPA_HOMOLOGADO);
        }
        repositorioSubprocesso.save(sp);

        Unidade sedoc =
                unidadeRepo
                        .findBySigla("SEDOC")
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Unidade 'SEDOC' não encontrada para registrar a"
                                                        + " homologação."));

        publicadorDeEventos.publishEvent(
                EventoSubprocessoMapaHomologado.builder()
                        .codSubprocesso(codSubprocesso)
                        .usuario(usuario)
                        .unidadeOrigem(sedoc)
                        .unidadeDestino(sedoc)
                        .build());
    }

    @Transactional
    public void submeterMapaAjustado(
            Long codSubprocesso, SubmeterMapaAjustadoReq request, Usuario usuario) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);

        subprocessoService.validarAssociacoesMapa(sp.getMapa().getCodigo());

        if (sp.getProcesso().getTipo() == TipoProcesso.MAPEAMENTO) {
            sp.setSituacao(MAPEAMENTO_MAPA_DISPONIBILIZADO);
        } else {
            sp.setSituacao(REVISAO_MAPA_DISPONIBILIZADO);
        }

        sp.setDataLimiteEtapa2(request.getDataLimiteEtapa2());
        sp.setDataFimEtapa1(java.time.LocalDateTime.now());
        repositorioSubprocesso.save(sp);

        publicadorDeEventos.publishEvent(
                EventoSubprocessoMapaAjustadoSubmetido.builder()
                        .codSubprocesso(codSubprocesso)
                        .usuario(usuario)
                        .unidadeOrigem(sp.getUnidade())
                        .unidadeDestino(sp.getUnidade())
                        .build());
    }

    @Transactional
    public void devolverCadastro(Long codSubprocesso, String observacoes, Usuario usuario) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);

        Unidade unidadeSuperior = sp.getUnidade().getUnidadeSuperior();
        if (unidadeSuperior == null) {
            throw new IllegalStateException(
                    "Unidade superior não encontrada para o subprocesso " + codSubprocesso);
        }

        analiseService.criarAnalise(
                CriarAnaliseRequest.builder()
                        .codSubprocesso(codSubprocesso)
                        .observacoes(observacoes)
                        .tipo(TipoAnalise.CADASTRO)
                        .acao(TipoAcaoAnalise.DEVOLUCAO_MAPEAMENTO)
                        .siglaUnidade(unidadeSuperior.getSigla())
                        .tituloUsuario(String.valueOf(usuario.getTituloEleitoral()))
                        .build());

        Unidade unidadeDevolucao = sp.getUnidade();

        sp.setSituacao(MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        sp.setDataFimEtapa1(null);
        repositorioSubprocesso.save(sp);

        publicadorDeEventos.publishEvent(
                EventoSubprocessoCadastroDevolvido.builder()
                        .codSubprocesso(codSubprocesso)
                        .usuario(usuario)
                        .unidadeOrigem(unidadeSuperior)
                        .unidadeDestino(unidadeDevolucao)
                        .observacoes(observacoes)
                        .build());
    }

    @Transactional
    public void aceitarCadastro(Long codSubprocesso, String observacoes, Usuario usuario) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);

        Unidade unidadeOrigem = sp.getUnidade();
        Unidade unidadeDestino = unidadeOrigem.getUnidadeSuperior();
        if (unidadeDestino == null) {
            throw new IllegalStateException(
                    "Não foi possível identificar a unidade superior para enviar a análise.");
        }

        analiseService.criarAnalise(
                CriarAnaliseRequest.builder()
                        .codSubprocesso(codSubprocesso)
                        .observacoes(observacoes)
                        .tipo(TipoAnalise.CADASTRO)
                        .acao(TipoAcaoAnalise.ACEITE_MAPEAMENTO)
                        .siglaUnidade(unidadeDestino.getSigla())
                        .tituloUsuario(String.valueOf(usuario.getTituloEleitoral()))
                        .motivo(null)
                        .build());

        sp.setSituacao(MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
        repositorioSubprocesso.save(sp);

        publicadorDeEventos.publishEvent(
                EventoSubprocessoCadastroAceito.builder()
                        .codSubprocesso(codSubprocesso)
                        .usuario(usuario)
                        .unidadeOrigem(unidadeOrigem)
                        .unidadeDestino(unidadeDestino)
                        .observacoes(observacoes)
                        .build());
    }

    @Transactional
    public void homologarCadastro(Long codSubprocesso, String observacoes, Usuario usuario) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);

        if (sp.getSituacao() != MAPEAMENTO_CADASTRO_DISPONIBILIZADO) {
            throw new IllegalStateException(
                    "Ação de homologar só pode ser executada em cadastros disponibilizados.");
        }

        Unidade sedoc =
                unidadeRepo
                        .findBySigla("SEDOC")
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Unidade 'SEDOC' não encontrada para registrar a"
                                                        + " homologação."));

        sp.setSituacao(MAPEAMENTO_CADASTRO_HOMOLOGADO);
        repositorioSubprocesso.save(sp);

        publicadorDeEventos.publishEvent(
                EventoSubprocessoCadastroHomologado.builder()
                        .codSubprocesso(codSubprocesso)
                        .usuario(usuario)
                        .unidadeOrigem(sedoc)
                        .unidadeDestino(sedoc)
                        .observacoes(observacoes)
                        .build());
    }

    @Transactional
    public void devolverRevisaoCadastro(Long codSubprocesso, String observacoes, Usuario usuario) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);

        if (sp.getSituacao() != REVISAO_CADASTRO_DISPONIBILIZADA) {
            throw new IllegalStateException(
                    "Ação de devolução só pode ser executada em revisões de cadastro"
                            + " disponibilizadas.");
        }

        Unidade unidadeAnalise = sp.getUnidade().getUnidadeSuperior();
        if (unidadeAnalise == null) {
            throw new IllegalStateException(
                    "Unidade superior não encontrada para o subprocesso " + codSubprocesso);
        }

        analiseService.criarAnalise(
                CriarAnaliseRequest.builder()
                        .codSubprocesso(codSubprocesso)
                        .observacoes(observacoes)
                        .tipo(TipoAnalise.CADASTRO)
                        .acao(TipoAcaoAnalise.DEVOLUCAO_REVISAO)
                        .siglaUnidade(unidadeAnalise.getSigla())
                        .tituloUsuario(String.valueOf(usuario.getTituloEleitoral()))
                        .build());

        Unidade unidadeDestino = sp.getUnidade();
        sp.setSituacao(REVISAO_CADASTRO_EM_ANDAMENTO);
        sp.setDataFimEtapa1(null);

        repositorioSubprocesso.save(sp);
        publicadorDeEventos.publishEvent(
                EventoSubprocessoRevisaoDevolvida.builder()
                        .codSubprocesso(codSubprocesso)
                        .usuario(usuario)
                        .unidadeOrigem(unidadeAnalise)
                        .unidadeDestino(unidadeDestino)
                        .observacoes(observacoes)
                        .build());
    }

    @Transactional
    public void aceitarRevisaoCadastro(Long codSubprocesso, String observacoes, Usuario usuario) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);

        if (sp.getSituacao() != REVISAO_CADASTRO_DISPONIBILIZADA) {
            throw new IllegalStateException(
                    "Ação de aceite só pode ser executada em revisões de cadastro"
                            + " disponibilizadas.");
        }

        Unidade unidadeAnalise = sp.getUnidade().getUnidadeSuperior();
        if (unidadeAnalise == null) {
            throw new IllegalStateException(
                    "Unidade superior não encontrada para o subprocesso " + codSubprocesso);
        }

        analiseService.criarAnalise(
                CriarAnaliseRequest.builder()
                        .codSubprocesso(codSubprocesso)
                        .observacoes(observacoes)
                        .tipo(TipoAnalise.CADASTRO)
                        .acao(TipoAcaoAnalise.ACEITE_REVISAO)
                        .siglaUnidade(unidadeAnalise.getSigla())
                        .tituloUsuario(String.valueOf(usuario.getTituloEleitoral()))
                        .motivo(null)
                        .build());

        Unidade unidadeDestino =
                unidadeAnalise.getUnidadeSuperior() != null
                        ? unidadeAnalise.getUnidadeSuperior()
                        : unidadeAnalise;

        sp.setSituacao(REVISAO_CADASTRO_DISPONIBILIZADA);
        repositorioSubprocesso.save(sp);

        publicadorDeEventos.publishEvent(
                EventoSubprocessoRevisaoAceita.builder()
                        .codSubprocesso(codSubprocesso)
                        .usuario(usuario)
                        .unidadeOrigem(unidadeAnalise)
                        .unidadeDestino(unidadeDestino)
                        .observacoes(observacoes)
                        .build());
    }

    @Transactional
    public void homologarRevisaoCadastro(Long codSubprocesso, String observacoes, Usuario usuario) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);

        if (sp.getSituacao() != REVISAO_CADASTRO_DISPONIBILIZADA) {
            throw new IllegalStateException(
                    "Ação de homologar só pode ser executada em revisões de cadastro aguardando"
                            + " homologação.");
        }

        var impactos = impactoMapaService.verificarImpactos(codSubprocesso, usuario);

        if (impactos.isTemImpactos()) {
            Unidade sedoc =
                    unidadeRepo
                            .findBySigla("SEDOC")
                            .orElseThrow(
                                    () ->
                                            new IllegalStateException(
                                                    "Unidade 'SEDOC' não encontrada para registrar"
                                                            + " a homologação."));

            sp.setSituacao(REVISAO_CADASTRO_HOMOLOGADA);

            publicadorDeEventos.publishEvent(
                    EventoSubprocessoRevisaoHomologada.builder()
                            .codSubprocesso(codSubprocesso)
                            .usuario(usuario)
                            .unidadeOrigem(sedoc)
                            .unidadeDestino(sedoc)
                            .observacoes(observacoes)
                            .build());
        } else {
            sp.setSituacao(REVISAO_MAPA_HOMOLOGADO);
        }

        repositorioSubprocesso.save(sp);
    }

    private Subprocesso buscarSubprocesso(Long codSubprocesso) {
        return repositorioSubprocesso
                .findById(codSubprocesso)
                .orElseThrow(
                        () ->
                                new ErroEntidadeNaoEncontrada(
                                        "Subprocesso não encontrado: " + codSubprocesso));
    }
}
