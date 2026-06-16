package com.Gabriel.API_Banco.dto;

import com.Gabriel.API_Banco.model.enums.PlanoLoja;
import lombok.Data;

@Data
public class AlterarPlanoLojaDTO {
    private Long idUsuario;
    private PlanoLoja plano;
}