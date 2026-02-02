package com.nology.leaddecisions.etl.infraestructure.excel;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Define o contrato (Layout) do arquivo Excel esperado.
 * Centraliza nomes de Abas e Colunas para evitar Magic Strings no código.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE) // Garante que ninguém instancie essa classe
public final class LeadExcelSchema {

    public static final class Sheets {
        public static final String BASE = "BASE";
        public static final String MERCADO = "MERCADO";
        public static final String ORIGEM = "ORIGEM";
        public static final String LOCAL = "LOCAL";
        public static final String PORTE = "PORTE";
        public static final String OBJETIVO = "OBJETIVO";
    }

    public static final class Columns {
        // Colunas Comuns (Chaves)
        public static final String LEAD_ID = "LEAD_ID";

        // Aba Base
        public static final String DATA_CADASTRO = "DATA CADASTRO";
        public static final String VENDIDO = "VENDIDO";

        // Aba Mercado
        public static final String MERCADO = "MERCADO";

        // Aba Origem
        public static final String ORIGEM = "ORIGEM";
        public static final String SUB_ORIGEM = "SUB-ORIGEM";

        // Outras Abas
        public static final String LOCAL = "LOCAL";
        public static final String PORTE = "PORTE";
        public static final String OBJETIVO = "OBJETIVO";
    }
}