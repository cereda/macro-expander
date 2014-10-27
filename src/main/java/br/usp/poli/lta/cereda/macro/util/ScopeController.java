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

import br.usp.poli.lta.cereda.macro.model.Macro;
import br.usp.poli.lta.cereda.macro.model.Stack;
import java.util.HashSet;
import java.util.Set;

/**
 * Implementa um controlador de escopo.
 * @author Paulo Roberto Massa Cereda
 * @version 1.0
 * @since 1.0
 */
public class ScopeController {
    
    // define uma única instância do controlador
    private static final ScopeController instance = new ScopeController();
    
    // pilha de conjuntos de macros
    private final Stack<Set<Macro>> stack;
    
    /**
     * Construtor.
     */
    private ScopeController() {
        stack = new Stack<>();
    }
    
    /**
     * Obtém a instância do controlador de escopo.
     * @return A instância do controlador de escopo.
     */
    public static ScopeController getInstance() {
        return instance;
    }

    /**
     * Obtém a pilha de conjuntos de macros.
     * @return Pilha de conjuntos de macros.
     */
    public Stack<Set<Macro>> getStack() {
        return stack;
    }
    
    /**
     * Cria um novo escopo, adicionando um novo conjunto vazio de macros no topo
     * da pilha.
     */
    public void createNewScope() {
        Set<Macro> scope = new HashSet<>();
        stack.push(scope);
    }
    
    /**
     * Adiciona a macro informada no escopo corrente.
     * @param macro Macro a ser adicionada no escopo corrente.
     * @return Um valor lógico informando se a macro foi adicionada com sucesso
     * no escopo corrente. É importante destacar que um conjunto não admite
     * elementos duplicados.
     */
    public boolean addMacroToCurrentScope(Macro macro) {
        return stack.top().add(macro);
    }
    
    /**
     * Adiciona a macro informada no escopo global.
     * @param macro Macro a ser adicionada no escopo global.
     * @return Um valor lógico informando se a macro foi adicionada com sucesso
     * no escopo global. É importante destacar que um conjunto não admite
     * elementos duplicados.
     */
    public boolean addMacroToGlobalScope(Macro macro) {
        return stack.bottom().add(macro);
    }
    
    /**
     * Remove o escopo corrente.
     */
    public void removeCurrentScope() {
        stack.pop();
    }    
    
}
