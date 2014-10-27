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

import java.util.ArrayList;
import java.util.List;

/**
 * Representa uma estrutura de dados na forma de pilha.
 * @author Paulo Roberto Massa Cereda
 * @version 1.0
 * @since 1.0
 * @param <TYPE> Tipo de dado armazenado na pilha.
 */
public class Stack<TYPE> {

    // a pilha é representada internamente por uma lista de elementos
    private final List<TYPE> stack;

    /**
     * Construtor.
     */
    public Stack() {
        stack = new ArrayList<>();
    }

    /**
     * Empilha o elemento na pilha.
     * @param entry Elemento a ser empilhado.
     */
    public void push(TYPE entry) {
        stack.add(entry);
    }

    /**
     * Retorna o elemento no topo da pilha, removendo-o em seguida.
     * @return Elemento no topo da pilha.
     */
    public TYPE pop() {
        return stack.remove(stack.size() - 1);
    }

    /**
     * Retorna o Elemento no topo da pilha.
     * @return Elemento no topo da pilha.
     */
    public TYPE top() {
        return stack.get(stack.size() - 1);
    }

    /**
     * Verifica se a pilha está vazia.
     * @return Um valor lógico indicando se a pilha está vazia.
     */
    public boolean isEmpty() {
        return stack.isEmpty();
    }

    /**
     * Esvazia a pilha.
     */
    public void clear() {
        stack.clear();
    }

    /**
     * Obtém a lista interna.
     * @return A lista interna.
     */
    public List<TYPE> getList() {
        return stack;
    }

    /**
     * Obtém o elemento na base da pilha.
     * @return Elemento na base da pilha.
     */
    public TYPE bottom() {
        return stack.get(0);
    }

    /**
     * Retorna uma representação textual da pilha.
     * @return Representação textual da pilha.
     */
    @Override
    public String toString() {
        return "Pilha: { " + stack + " }";
    }

}
