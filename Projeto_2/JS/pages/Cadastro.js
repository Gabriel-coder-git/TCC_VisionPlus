import { validarEmail, validarNomeUsuario } from "../core/usuario.js";
import { configurarHeader } from "../components/header2.js";
import { iniciarRotacaoGifs } from "../components/authVisual.js";

document.addEventListener("DOMContentLoaded", () => {

    configurarHeader();
    iniciarRotacaoGifs();

    const form = document.querySelector("#cadastroForm");

    let termosAceitos = false;
    let turnstileToken = null;

    window.onTurnstileCadastroSuccess = function (token) {
        turnstileToken = token;
    };

    window.onTurnstileCadastroExpired = function () {
        turnstileToken = null;
    };

    function obterTurnstileToken() {
        const inputToken = document.querySelector('input[name="cf-turnstile-response"]');
        return turnstileToken || inputToken?.value || "";
    }

    const regrasCard = document.querySelector("#regrasCard");
    const senhaInput = document.querySelector("#senha");
    const confirmarSenhaInput = document.querySelector("#confirmarSenha");

    const regraTamanho = document.querySelector("#regraTamanho");
    const regraMaiuscula = document.querySelector("#regraMaiuscula");
    const regraMinuscula = document.querySelector("#regraMinuscula");
    const regraNumero = document.querySelector("#regraNumero");
    const regraEspecial = document.querySelector("#regraEspecial");
    const regraProibido = document.querySelector("#regraProibido");

    const inputNome = document.querySelector("#nome");
    const inputEmail = document.querySelector("#email");

    const msgNome = document.querySelector("#msgNome");
    const msgEmail = document.querySelector("#msgEmail");
    const msgSenha = document.querySelector("#msgSenha");

    const btnSubmit = form.querySelector("button[type='submit']");

    let timeoutNome;
    let timeoutEmail;
    let nomeDisponivel = false;
    let emailDisponivel = false;
    let processando = false;
    let cadastroSucesso = false;

    // ==============================
    // TERMOS DE USO
    // ==============================

    const modalTermos = document.getElementById("modalTermos");
    const checkTermos = document.getElementById("checkTermos");
    const checkModalTermos = document.getElementById("checkModalTermos");
    const btnAceitarTermos = document.getElementById("btnAceitarTermos");
    const btnCancelarTermos = document.getElementById("btnCancelarTermos");
    const abrirTermos = document.getElementById("abrirTermos");
    const avisoTermos = document.getElementById("avisoTermos");

    checkModalTermos.addEventListener("change", () => {
        btnAceitarTermos.classList.toggle("habilitado", checkModalTermos.checked);
    });

    function abrirModalTermos() {
        checkModalTermos.checked = false;
        btnAceitarTermos.classList.remove("habilitado");
        modalTermos.classList.add("ativo");
    }

    abrirTermos.addEventListener("click", (event) => {
        event.preventDefault();
        abrirModalTermos();
    });

    checkTermos.addEventListener("click", (event) => {
        if (!termosAceitos) {
            event.preventDefault();
            abrirModalTermos();
            return;
        }

        event.preventDefault();
        checkTermos.checked = true;
    });

    btnCancelarTermos.addEventListener("click", () => {
        modalTermos.classList.remove("ativo");
        checkModalTermos.checked = false;
        btnAceitarTermos.classList.remove("habilitado");

        termosAceitos = false;
        checkTermos.checked = false;
    });

    btnAceitarTermos.addEventListener("click", () => {
        if (!checkModalTermos.checked) return;

        termosAceitos = true;
        checkTermos.checked = true;

        modalTermos.classList.remove("ativo");
        avisoTermos.style.display = "none";

        mostrarMensagem(msgSenha, "Termos aceitos com sucesso.", "sucesso");
    });

    modalTermos.addEventListener("click", (event) => {
        if (event.target === modalTermos) {
            modalTermos.classList.remove("ativo");
        }
    });
    // ==============================
    // TOAST
    // ==============================

    function mostrarMensagem(elemento, texto, tipo) {
        if (!elemento) return;

        elemento.textContent = texto;
        elemento.classList.remove("sucesso", "erro");
        elemento.classList.add("mostrar", tipo);

        setTimeout(() => {
            elemento.classList.remove("mostrar");
        }, 5000);
    }

    // ==============================
    // VALIDAÇÃO DE NOME
    // ==============================

    inputNome.addEventListener("input", () => {
        clearTimeout(timeoutNome);
        nomeDisponivel = false;

        timeoutNome = setTimeout(async () => {
            const nome = inputNome.value.trim();

            if (nome.length < 3) {
                msgNome.textContent = "";
                msgNome.classList.remove("mostrar", "sucesso", "erro");
                return;
            }

            try {
                const existe = await validarNomeUsuario(nome);

                if (existe) {
                    mostrarMensagem(msgNome, "Nome já cadastrado.", "erro");
                    nomeDisponivel = false;
                } else {
                    mostrarMensagem(msgNome, "Nome disponível.", "sucesso");
                    nomeDisponivel = true;
                }

            } catch (error) {
                console.error(error);
                mostrarMensagem(msgNome, "Erro ao validar nome.", "erro");
                nomeDisponivel = false;
            }
        }, 600);
    });

    // ==============================
    // VALIDAÇÃO DE EMAIL
    // ==============================

    inputEmail.addEventListener("input", () => {
        clearTimeout(timeoutEmail);
        emailDisponivel = false;

        timeoutEmail = setTimeout(async () => {
            const email = inputEmail.value.trim();

            const regexEmail = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

            if (!regexEmail.test(email)) {
                msgEmail.textContent = "";
                msgEmail.classList.remove("mostrar", "sucesso", "erro");
                return;
            }

            try {
                const existe = await validarEmail(email);

                if (existe) {
                    mostrarMensagem(msgEmail, "Email já cadastrado.", "erro");
                    emailDisponivel = false;
                } else {
                    mostrarMensagem(msgEmail, "Email disponível.", "sucesso");
                    emailDisponivel = true;
                }

            } catch (error) {
                console.error(error);
                mostrarMensagem(msgEmail, "Erro ao validar email.", "erro");
                emailDisponivel = false;
            }
        }, 600);
    });

    // ==============================
    // VALIDAÇÃO DE SENHA
    // ==============================

    function mostrarRegrasCard() {
        regrasCard.classList.add("mostrar");
    }

    function esconderRegrasSeCamposVazios() {
        if (!senhaInput.value && !confirmarSenhaInput.value) {
            regrasCard.classList.remove("mostrar");
        }
    }

    function atualizarRegra(elemento, condicao) {
        if (condicao) {
            elemento.classList.remove("invalido");
            elemento.classList.add("valido");
        } else {
            elemento.classList.remove("valido");
            elemento.classList.add("invalido");
        }
    }

    function validarSenhaTempoReal() {
        const senha = senhaInput.value;

        const regexMaiuscula = /[A-Z]/;
        const regexMinuscula = /[a-z]/;
        const regexNumero = /[0-9]/;
        const regexEspecial = /[!@#$%^&*(),.?":{}|<>]/;
        const regexProibido = /[<>]/;

        const tamanhoValido = senha.length >= 8;
        const maiusculaValida = regexMaiuscula.test(senha);
        const minusculaValida = regexMinuscula.test(senha);
        const numeroValido = regexNumero.test(senha);
        const especialValido = regexEspecial.test(senha);
        const proibidoValido = !regexProibido.test(senha);

        atualizarRegra(regraTamanho, tamanhoValido);
        atualizarRegra(regraMaiuscula, maiusculaValida);
        atualizarRegra(regraMinuscula, minusculaValida);
        atualizarRegra(regraNumero, numeroValido);
        atualizarRegra(regraEspecial, especialValido);
        atualizarRegra(regraProibido, proibidoValido);

        const senhaEhValida =
            tamanhoValido &&
            maiusculaValida &&
            minusculaValida &&
            numeroValido &&
            especialValido &&
            proibidoValido;

        if (senha.length === 0) {
            senhaInput.classList.remove("senha-valida", "senha-invalida");
        } else if (senhaEhValida) {
            senhaInput.classList.remove("senha-invalida");
            senhaInput.classList.add("senha-valida");
        } else {
            senhaInput.classList.remove("senha-valida");
            senhaInput.classList.add("senha-invalida");
        }

        return senhaEhValida;
    }

    senhaInput.addEventListener("focus", mostrarRegrasCard);
    confirmarSenhaInput.addEventListener("focus", mostrarRegrasCard);

    senhaInput.addEventListener("input", () => {
        mostrarRegrasCard();
        validarSenhaTempoReal();
    });

    confirmarSenhaInput.addEventListener("input", () => {
        mostrarRegrasCard();
        validarSenhaTempoReal();
    });

    senhaInput.addEventListener("blur", esconderRegrasSeCamposVazios);
    confirmarSenhaInput.addEventListener("blur", esconderRegrasSeCamposVazios);
    // ==============================
    // SUBMIT
    // ==============================

    function travarBotao(texto = "Validando...") {
        processando = true;
        btnSubmit.classList.add("carregando");
        btnSubmit.disabled = true;
        btnSubmit.textContent = texto;
    }

    function destravarBotao() {
        processando = false;
        btnSubmit.classList.remove("carregando");
        btnSubmit.disabled = false;
        btnSubmit.textContent = "Registrar";
    }

    form.addEventListener("submit", async (event) => {
        event.preventDefault();

        if (processando) return;
        travarBotao("Validando...");
        cadastroSucesso = false;

        if (!termosAceitos || !checkTermos.checked) {
            avisoTermos.style.display = "block";
            mostrarMensagem(msgSenha, "Você precisa aceitar os termos de uso.", "erro");
            destravarBotao();
            return;
        }

        const captchaToken = obterTurnstileToken();

        if (!captchaToken) {
            mostrarMensagem(msgSenha, "Confirme que você não é um robô.", "erro");
            destravarBotao();
            return;
        }

        const nome = inputNome.value.trim();
        const email = inputEmail.value.trim();
        const senha = senhaInput.value;
        const confirmarSenha = confirmarSenhaInput.value;

        if (!nome || !email || !senha || !confirmarSenha) {
            mostrarMensagem(msgSenha, "Preencha todos os campos.", "erro");
            destravarBotao();
            return;
        }

        const senhaValida = validarSenhaTempoReal();

        if (!senhaValida) {
            mostrarMensagem(msgSenha, "Senha não atende aos requisitos.", "erro");
            mostrarRegrasCard();
            destravarBotao();
            return;
        }

        if (senha !== confirmarSenha) {
            mostrarMensagem(msgSenha, "As senhas não conferem.", "erro");
            destravarBotao();
            return;
        }

        try {

            const nomeJaExiste = await validarNomeUsuario(nome);
            const emailJaExiste = await validarEmail(email);

            if (nomeJaExiste) {
                nomeDisponivel = false;
                mostrarMensagem(msgNome, "Nome já cadastrado.", "erro");
                return;
            }

            if (emailJaExiste) {
                emailDisponivel = false;
                mostrarMensagem(msgEmail, "Email já cadastrado.", "erro");
                return;
            }

            nomeDisponivel = true;
            emailDisponivel = true;

            const usuario = {
                nome,
                email,
                senha,
                tipoUsuario: "Comum",
                aceitouTermos: termosAceitos,
                versaoTermos: "v2",
                captchaToken: captchaToken
            };

            btnSubmit.textContent = "Registrando...";

            const response = await fetch("https://tccvisionplus-production.up.railway.app/usuarios/registrar", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(usuario)
            });

            if (!response.ok) {
                const mensagemErro = await response.text();
                mostrarMensagem(msgSenha, mensagemErro || "Erro ao cadastrar usuário.", "erro");
                return;
            }

            const mensagem = await response.text();

            cadastroSucesso = true;
            btnSubmit.textContent = "Redirecionando...";

            mostrarMensagem(
                msgSenha,
                mensagem || "Cadastro realizado com sucesso! Redirecionando...",
                "sucesso"
            );

            form.reset();

            checkModalTermos.checked = false;
            btnAceitarTermos.classList.remove("habilitado");

            termosAceitos = false;
            checkTermos.checked = false;

            nomeDisponivel = false;
            emailDisponivel = false;

            senhaInput.classList.remove("senha-valida", "senha-invalida");
            confirmarSenhaInput.classList.remove("senha-valida", "senha-invalida");

            setTimeout(() => {
                window.location.href = "Login.html";
            }, 2500);

        } catch (error) {
            console.error(error);

            mostrarMensagem(
                msgSenha,
                error.message || "Erro ao conectar com o servidor.",
                "erro"
            );

        } finally {
            if (window.turnstile) {
                window.turnstile.reset();
                turnstileToken = null;
            }

            if (!cadastroSucesso) {
                processando = false;
                btnSubmit.classList.remove("carregando");
                btnSubmit.disabled = false;
                btnSubmit.textContent = "Registrar";
            }
        }
    });
});