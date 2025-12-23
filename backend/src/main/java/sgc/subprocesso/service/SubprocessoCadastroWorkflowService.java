package sgc.subprocesso.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.analise.AnaliseService;
import sgc.analise.api.CriarAnaliseRequest;
import sgc.analise.internal.model.TipoAcaoAnalise;
import sgc.analise.internal.model.TipoAnalise;
import sgc.comum.erros.ErroAccessoNegado;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.comum.erros.ErroValidacao;
import sgc.mapa.model.Mapa;
import sgc.mapa.service.ImpactoMapaService;
import sgc.processo.eventos.*;
import sgc.sgrh.model.Usuario;
import sgc.subprocesso.erros.ErroMapaNaoAssociado;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.unidade.model.Unidade;
import sgc.unidade.model.UnidadeRepo;

import static sgc.subprocesso.model.SituacaoSubprocesso.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubprocessoCadastroWorkflowService {
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
        String tituloTitular = unidadeSubprocesso.getTituloTitular();

        if (tituloTitular == null || !tituloTitular.equals(usuario.getTituloEleitoral())) {
            String msg =
                    "Usuário %s não é o titular da unidade (%s). Titular é %s"
                            .formatted(
                                    usuario.getTituloEleitoral(),
                                    unidadeSubprocesso.getSigla(),
                                    tituloTitular != null ? tituloTitular : "não definido");
            throw new ErroAccessoNegado(msg);
        }

        // Valida se há pelo menos uma atividade cadastrada
        subprocessoService.validarExistenciaAtividades(codSubprocesso);

        // Valida se todas as atividades têm conhecimentos associados
        if (!subprocessoService.obterAtividadesSemConhecimento(codSubprocesso).isEmpty()) {
            throw new ErroValidacao("Existem atividades sem conhecimentos associados.");
        }

        Mapa mapa = sp.getMapa();
        if (mapa == null || mapa.getCodigo() == null) {
            throw new ErroMapaNaoAssociado("Subprocesso sem mapa associado");
        }
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
