function formatarNumero(valor, casas = 2) {
    if (typeof valor !== "number" || Number.isNaN(valor)) {
        return "-";
    }

    return valor.toLocaleString("pt-BR", {
        minimumFractionDigits: casas,
        maximumFractionDigits: casas
    });
}

function formatarDuracao(ms) {
    if (typeof ms !== "number") {
        return "-";
    }

    const segundos = Math.round(ms / 1000);
    if (segundos < 60) {
        return `${segundos}s`;
    }

    const minutos = Math.floor(segundos / 60);
    const resto = segundos % 60;
    return `${minutos}m ${resto}s`;
}

function criarCard(titulo, valor, classe = "") {
    const artigo = document.createElement("article");
    artigo.className = "card";
    artigo.innerHTML = `<h3>${titulo}</h3><strong class="${classe}">${valor}</strong>`;
    return artigo;
}

function criarStatus(status) {
    return `<span class="status ${status}">${status}</span>`;
}

function criarBarra(percentual) {
    const valorNormalizado = Math.max(0, Math.min(100, Number(percentual ?? 0)));
    return `<div class="barra"><span style="width:${valorNormalizado}%"></span></div>`;
}

function renderizarResumo(snapshot) {
    const container = document.getElementById("resumoCards");
    const resumo = snapshot.resumo ?? {};
    const totais = resumo.totais ?? {};

    container.append(
        criarCard("Status geral", criarStatus(resumo.statusGeral ?? "-")),
        criarCard("Indice de saude", `${formatarNumero(resumo.indiceSaude)}%`),
        criarCard("Verificacoes", `${totais.verificacoes ?? 0}`),
        criarCard("Sucessos", `${totais.sucesso ?? 0}`),
        criarCard("Alertas", `${totais.alerta ?? 0}`),
        criarCard("Falhas", `${totais.falha ?? 0}`)
    );

    const git = snapshot.metadados?.git ?? {};
    const texto = [
        `Gerado em ${new Date(snapshot.metadados?.geradoEm ?? Date.now()).toLocaleString("pt-BR")}`,
        `Perfil: ${snapshot.metadados?.perfilExecucao ?? "-"}`,
        `Branch: ${git.branch ?? "-"}`,
        `Commit: ${git.commitCurto ?? git.commit ?? "-"}`
    ].join(" | ");

    document.getElementById("metadadosResumo").textContent = texto;
}

function renderizarVerificacoes(snapshot) {
    const corpoTabela = document.getElementById("tabelaVerificacoes");
    const verificacoes = snapshot.verificacoes ?? [];

    for (const verificacao of verificacoes) {
        const linha = document.createElement("tr");
        linha.innerHTML = `
            <td>${verificacao.nome}</td>
            <td>${criarStatus(verificacao.status)}</td>
            <td>${formatarDuracao(verificacao.duracaoMs)}</td>
            <td>${verificacao.sumario ?? "-"}</td>
        `;
        corpoTabela.append(linha);
    }
}

function renderizarCobertura(snapshot) {
    const bloco = document.getElementById("coberturaMetricas");
    const cobertura = snapshot.cobertura ?? {};
    const backend = cobertura.backend ?? {};
    const frontend = cobertura.frontend ?? {};

    const metricas = [
        {titulo: "Backend - linhas", valor: backend.linhas?.percentual},
        {titulo: "Backend - branches", valor: backend.branches?.percentual},
        {titulo: "Frontend - linhas", valor: frontend.lines?.percentual},
        {titulo: "Frontend - branches", valor: frontend.branches?.percentual},
        {titulo: "Frontend - funcoes", valor: frontend.functions?.percentual},
        {titulo: "Frontend - statements", valor: frontend.statements?.percentual}
    ];

    const lista = document.createElement("ul");

    for (const metrica of metricas) {
        const item = document.createElement("li");
        const percentual = Number(metrica.valor ?? 0);
        item.innerHTML = `${metrica.titulo}: <strong>${formatarNumero(percentual)}%</strong>${criarBarra(percentual)}`;
        lista.append(item);
    }

    bloco.append(lista);
}

function renderizarQualidade(snapshot) {
    const bloco = document.getElementById("qualidadeMetricas");
    const qualidade = snapshot.qualidade ?? {};
    const confiabilidade = snapshot.confiabilidade ?? {};

    const lista = document.createElement("ul");
    lista.innerHTML = `
        <li>Erros de lint: <strong>${qualidade.lint?.erros ?? 0}</strong></li>
        <li>Avisos de lint: <strong>${qualidade.lint?.avisos ?? 0}</strong></li>
        <li>Erros de typecheck: <strong>${qualidade.typecheck?.erros ?? 0}</strong></li>
        <li>Testes ignorados: <strong>${confiabilidade.testesIgnorados ?? 0}</strong></li>
        <li>Testes flaky: <strong>${confiabilidade.testesFlaky ?? 0}</strong></li>
    `;

    bloco.append(lista);

    const suites = document.createElement("div");
    suites.innerHTML = "<h3>Suites mais lentas</h3>";
    const listaSuites = document.createElement("ol");

    for (const suite of confiabilidade.suitesLentas ?? []) {
        const item = document.createElement("li");
        item.textContent = `${suite.nome}: ${formatarDuracao(suite.duracaoMs)}`;
        listaSuites.append(item);
    }

    suites.append(listaSuites);
    bloco.append(suites);
}

function renderizarHotspots(snapshot) {
    const lista = document.getElementById("listaHotspots");
    const hotspots = snapshot.hotspots ?? [];

    if (hotspots.length === 0) {
        lista.innerHTML = "<li>Nenhum hotspot calculado.</li>";
        return;
    }

    for (const hotspot of hotspots) {
        const item = document.createElement("li");
        const motivos = (hotspot.motivos ?? []).join("; ");
        item.innerHTML = `<strong>${hotspot.nome}</strong> - risco ${formatarNumero(hotspot.risco)}. ${motivos}`;
        lista.append(item);
    }
}

function renderizarErro(mensagem) {
    const elemento = document.getElementById("metadadosResumo");
    elemento.textContent = mensagem;
    elemento.style.color = "#b91c1c";
}

async function carregarSnapshot() {
    const resposta = await fetch("./latest/ultimo-snapshot.json", {cache: "no-store"});

    if (!resposta.ok) {
        throw new Error(`Falha ao carregar snapshot: HTTP ${resposta.status}`);
    }

    return resposta.json();
}

async function inicializarDashboard() {
    try {
        const snapshot = await carregarSnapshot();
        renderizarResumo(snapshot);
        renderizarVerificacoes(snapshot);
        renderizarCobertura(snapshot);
        renderizarQualidade(snapshot);
        renderizarHotspots(snapshot);
    } catch (erro) {
        renderizarErro(`Nao foi possivel carregar os dados do dashboard. ${erro.message}`);
    }
}

inicializarDashboard();
