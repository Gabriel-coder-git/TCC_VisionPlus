import { configurarHeader } from "../components/header.js";
import { getUsuarioLogado } from "../core/auth.js";
import { getLojaDoUsuario, alterarPlanoLoja } from "../core/loja.js";

let usuario = null;
let lojaAtual = null;
let planoSelecionado = null;

const PRECOS = {
    FREE: "R$ 0/mês",
    STARTER: "R$ 49,90/mês",
    PRO: "R$ 89,90/mês",
    PLUS: "R$ 149,90/mês"
};

const NOMES_PLANOS = {
    FREE: "Gratuito",
    STARTER: "Plano 1",
    PRO: "Plano 2",
    PLUS: "Plano 3"
};


document.addEventListener("DOMContentLoaded", async () => {
    usuario = getUsuarioLogado();

    if (!usuario) {
        alert("Você precisa estar logado para acessar os planos.");
        window.location.href = "Login.html";
        return;
    }

    if (usuario.tipoUsuario !== "Vendedor" && usuario.tipoUsuario !== "Admin") {
        alert("Apenas lojas podem alterar planos.");
        window.location.href = "PaginaPrincipal.html";
        return;
    }

    configurarHeader();

    lojaAtual = await getLojaDoUsuario(usuario);

    if (!lojaAtual) {
        alert("Loja não encontrada.");
        window.location.href = "CadastroLoja.html";
        return;
    }

    configurarMinhaLoja();
    atualizarPlanoAtual();
    configurarBotoesPlanos();
    configurarModalPagamento();
});

function configurarMinhaLoja() {
    const btnMinhaLoja = document.getElementById("MinhaLoja");

    if (btnMinhaLoja && lojaAtual?.id) {
        btnMinhaLoja.addEventListener("click", () => {
            window.location.href = `PaginaLoja.html?id=${lojaAtual.id}`;
        });
    }
}

function atualizarPlanoAtual() {
    const planoAtual = lojaAtual?.plano || "FREE";
    const textoPlanoAtual = document.getElementById("plano-atual");

    if (textoPlanoAtual) {
        textoPlanoAtual.textContent = NOMES_PLANOS[planoAtual] || planoAtual;
    }

    document.querySelectorAll(".btn-plano").forEach(botao => {
        const plano = botao.dataset.plano;

        botao.disabled = false;
        botao.classList.remove("plano-atual");

        if (plano === planoAtual) {
            botao.textContent = "Plano atual";
            botao.disabled = true;
            botao.classList.add("plano-atual");
            return;
        }

        if (plano === "FREE") {
            botao.textContent = "Voltar para Gratuito";
        }

        if (plano === "STARTER") {
            botao.textContent = "Assinar Plano 1";
        }

        if (plano === "PRO") {
            botao.textContent = "Assinar Plano 2";
        }

        if (plano === "PLUS") {
            botao.textContent = "Assinar Plano 3";
        }
    });
}

function configurarBotoesPlanos() {
    document.querySelectorAll(".btn-plano").forEach(botao => {
        botao.addEventListener("click", () => {
            const plano = botao.dataset.plano;

            if (!plano || plano === lojaAtual.plano) return;

            abrirModalPagamento(plano);
        });
    });
}

function configurarModalPagamento() {
    const modal = document.getElementById("modal-pagamento");
    const fechar = document.getElementById("fechar-modal-pagamento");
    const confirmar = document.getElementById("confirmar-plano");

    fechar?.addEventListener("click", fecharModalPagamento);

    modal?.addEventListener("click", event => {
        if (event.target === modal) {
            fecharModalPagamento();
        }
    });

    confirmar?.addEventListener("click", confirmarAlteracaoPlano);
}

function abrirModalPagamento(plano) {
    planoSelecionado = plano;

    const modal = document.getElementById("modal-pagamento");
    const texto = document.getElementById("texto-confirmacao-plano");
    const confirmar = document.getElementById("confirmar-plano");

    if (texto) {
        texto.innerHTML = `
            Você está alterando o plano da loja
            <strong>${lojaAtual.nome}</strong>
            de <strong>${NOMES_PLANOS[lojaAtual.plano || "FREE"] || "Gratuito"}</strong>
            para <strong>${NOMES_PLANOS[plano] || plano}</strong>.
            <br>
            Valor simulado: <strong>${PRECOS[plano]}</strong>.
        `;
    }

    if (confirmar) {
        confirmar.textContent = `Confirmar ${NOMES_PLANOS[plano] || plano}`;
    }

    modal?.classList.add("ativo");
}

function fecharModalPagamento() {
    planoSelecionado = null;
    document.getElementById("modal-pagamento")?.classList.remove("ativo");
}

async function confirmarAlteracaoPlano() {
    if (!planoSelecionado || !lojaAtual || !usuario) return;

    const botao = document.getElementById("confirmar-plano");
    const textoOriginal = botao?.textContent;

    try {
        if (botao) {
            botao.disabled = true;
            botao.textContent = "Processando pagamento...";
        }

        await esperarPagamentoSimulado();

        lojaAtual = await alterarPlanoLoja(
            lojaAtual.id,
            usuario.id,
            planoSelecionado
        );

        fecharModalPagamento();
        atualizarPlanoAtual();
        mostrarToast(`Plano alterado para ${NOMES_PLANOS[lojaAtual.plano] || lojaAtual.plano} com sucesso!`);

    } catch (error) {
        console.error(error);
        alert(error.message || "Erro ao alterar plano.");
    } finally {
        if (botao) {
            botao.disabled = false;
            botao.textContent = textoOriginal || "Confirmar plano";
        }
    }
}

function esperarPagamentoSimulado() {
    return new Promise(resolve => {
        setTimeout(resolve, 900);
    });
}

function mostrarToast(mensagem) {
    const toast = document.getElementById("toast-planos");

    if (!toast) {
        alert(mensagem);
        return;
    }

    toast.textContent = mensagem;
    toast.classList.add("mostrar");

    setTimeout(() => {
        toast.classList.remove("mostrar");
    }, 3500);
}