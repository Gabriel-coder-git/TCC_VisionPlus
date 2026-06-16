package com.Gabriel.API_Banco.dto;

import lombok.Data;

@Data
public class AlterarSenhaDTO {
    private Long idUsuario;
    private String senhaAtual;
    private String novaSenha;
}