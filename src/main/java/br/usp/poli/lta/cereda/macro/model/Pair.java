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
package br.usp.poli.lta.cereda.macro.model;

/**
 * Oferece uma estrutura de representação de um par ordenado.
 * @author Paulo Roberto Massa Cereda
 * @version 1.0
 * @since 1.0
 * @param <A> Tipo do primeiro elemento.
 * @param <B> Tipo do segundo elemento.
 */
public class Pair<A, B> {
    
    // primeiro elemento
    private final A first;
    
    // segundo elemento
    private final B second;

    /**
     * Construtor.
     * @param first Primeiro elemento.
     * @param second Segundo elemento.
     */
    public Pair(A first, B second) {
        this.first = first;
        this.second = second;
    }

    /**
     * Obtém o primeiro elemento do par.
     * @return Primeiro elemento do par.
     */
    public A getFirst() {
        return first;
    }

    /**
     * Obtém o segundo elemento do par.
     * @return Segundo elemento do par.
     */
    public B getSecond() {
        return second;
    }
    
}
