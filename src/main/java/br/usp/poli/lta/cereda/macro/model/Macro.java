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

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Representa uma macro.
 * @author Paulo Roberto Massa Cereda
 * @version 1.0
 * @since 1.0
 */
public class Macro {

    // nome da macro
    private String name;
    
    // mapa contendo os parâmetros e seus respectivos índices posicionais
    private Map<Integer, String> parameters;
    
    // corpo da macro
    private String body;

    /**
     * Obtém o nome da macro.
     * @return Nome da macro.
     */
    public String getName() {
        return name;
    }

    /**
     * Define o nome da macro.
     * @param name Nome da macro.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Obtém o mapa de parâmetros.
     * @return Mapa de parâmetros.
     */
    public Map<Integer, String> getParameters() {
        return parameters;
    }

    /**
     * Define o mapa de parâmetros.
     * @param parameters Mapa de parâmetros.
     */
    public void setParameters(Map<Integer, String> parameters) {
        this.parameters = parameters;
    }

    /**
     * Obtém o corpo da macro.
     * @return Corpo da macro.
     */
    public String getBody() {
        return body;
    }

    /**
     * Define o corpo da macro.
     * @param body Corpo da macro.
     */
    public void setBody(String body) {
        this.body = body;
    }

    /**
     * Construtor vazio.
     */
    public Macro() {
    }

    /**
     * Construtor de uma macro simples.
     * @param name Nome da macro.
     * @param body Corpo da macro.
     */
    public Macro(String name, String body) {
        this.name = name;
        this.parameters = new HashMap<>();
        this.body = body;
    }

    /**
     * Construtor de uma macro paramétrica.
     * @param name Nome da macro.
     * @param parameters Mapa de parâmetros.
     * @param body Corpo da macro.
     */
    public Macro(String name, Map<Integer, String> parameters, String body) {
        this.name = name;
        this.parameters = parameters;
        this.body = body;
    }

    /**
     * Obtém a identificação de hash do objeto corrente.
     * @return Um valor inteiro representando a identificação de hash do objeto
     * corrente. É importante observar que este é calculado de acordo com o nome
     * da macro e o número de seus parâmetros; a representação mnemônica de
     * cada parâmetro não importa.
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder().
                append(name).
                append(parameters.size()).
                build();
    }

    /**
     * Verifica se um objeto é igual ao objeto corrente.
     * @param object Objeto a ser comparado.
     * @return Um valor lógico indicando se o objeto fornecido é igual ao objeto
     * corrente. A mesma observação feita para o método hashCode() vale nessa
     * situação: uma macro é dita igual a outra se estas possuem o mesmo nome e
     * o mesmo número de parâmetros (o corpo da macro e o mnmenômico de cada
     * parâmetro são irrelevantes no contexto). Este método é utilizado pela
     * estrutura de dados Set para verificar se um objeto já está inserido no
     * conjunto (um conjunto não admite elementos duplicados).
     */
    @Override
    public boolean equals(Object object) {
        if (object == null) {
            return false;
        }
        if (getClass() != object.getClass()) {
            return false;
        }
        final Macro reference = (Macro) object;
        return new EqualsBuilder().
                append(name, reference.name).
                append(parameters.size(), reference.parameters.size()).
                isEquals();
    }

    /**
     * Fornece uma representação textual da macro.
     * @return Representação textual da macro.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Macro: {");
        sb.append("nome = ").append(name).append(",");
        sb.append("parâmetros = ").append(parameters).append(",");
        sb.append("corpo = ").append(body).append(" }");       
        return sb.toString();
    }

}
