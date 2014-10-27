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
 * Contém a classificação de uma macro de acordo com sua pertinência ao conjunto
 * de primitivas definido para o expansor de macros.
 * @author Paulo Roberto Massa Cereda
 * @version 1.0
 * @since 1.0
 */
public enum Primitive {
    
    NONE,
    COMMENT,
    DEFINE,
    GLOBALDEFINE,
    NEWLINE,
    NEWPAGE,
    NOEXPAND,
    REPEAT,
    INPUTTEXT,
    SENDMESSAGE,
    FOREACH,
    INCREMENT,
    DECREMENT,
    SETCOUNTER,
    NEWCOUNTER,
    COUNTER,
    INCREMENTCOUNTER,
    DECREMENTCOUNTER,
    CHECKCONDITION,
    ISZERO,
    ISGREATERTHAN,
    ISLESSTHAN,
    ISEQUAL
    
}
