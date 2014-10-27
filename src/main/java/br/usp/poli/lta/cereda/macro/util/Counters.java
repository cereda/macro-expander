/**
* ------------------------------------------------------
*    Laboratório de Linguagens e Técnicas Adaptativas
*       Escola Politécnica, Universidade São Paulo
* ------------------------------------------------------
* 
* This program is free software: you can redistribute it
* and/or modify  it under the  terms of the  GNU General
* Public  License  as  published by  the  Free  Software
* Foundation, either  version 3  of the License,  or (at
* your option) any later version.
* 
* This program is  distributed in the hope  that it will
* be useful, but WITHOUT  ANY WARRANTY; without even the
* implied warranty  of MERCHANTABILITY or FITNESS  FOR A
* PARTICULAR PURPOSE. See the GNU General Public License
* for more details.
* 
**/
package br.usp.poli.lta.cereda.macro.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementa um gerenciador global de contadores do expansor de macros.
 * @author Paulo Roberto Massa Cereda
 * @version 1.0
 * @since 1.0
 */
public class Counters {
    
    // referência à instância
    private static final Counters instance = new Counters();
    
    // mapa de contadores inteiros
    private final Map<String, Integer> counters;
    
    /**
     * Construtor.
     */
    private Counters() {
        counters = new HashMap<>();
    }
    
    /**
     * Obtém a instância do gerenciador de contadores.
     * @return Instância do gerenciador de contadores inteiros.
     */
    public static Counters getInstance() {
        return instance;
    }
    
    /**
     * Verifica se o contador existe no mapa.
     * @param name Nome do contador a ser verificado.
     * @return Valor lógico indicando se o contador existe no mapa.
     */
    public boolean contains(String name) {
        return counters.containsKey(name);
    }
    
    /**
     * Obtém o valor inteiro associado ao contador informado.
     * @param name Nome do contador.
     * @return Valor inteiro associado ao contador informado.
     */
    public int get(String name) {
        return counters.get(name);
    }
    
    /**
     * Define o valor inteiro do contador.
     * @param name Nome do contador.
     * @param value Valor a ser inserido.
     */
    public void set(String name, int value) {
        counters.put(name, value);
    }
    
}
