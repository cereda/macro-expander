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
package br.usp.poli.lta.cereda.macro;

import br.usp.poli.lta.cereda.macro.model.Expander;
import br.usp.poli.lta.cereda.macro.model.exceptions.ArgumentNumberMismatchException;
import br.usp.poli.lta.cereda.macro.model.exceptions.CounterNotFoundException;
import br.usp.poli.lta.cereda.macro.model.exceptions.DuplicateCounterException;
import br.usp.poli.lta.cereda.macro.model.exceptions.DuplicateMacroException;
import br.usp.poli.lta.cereda.macro.model.exceptions.InvalidConditionValueException;
import br.usp.poli.lta.cereda.macro.model.exceptions.InvalidIntegerRangeException;
import br.usp.poli.lta.cereda.macro.model.exceptions.MacroDefinitionException;
import br.usp.poli.lta.cereda.macro.model.exceptions.MacroNotFoundException;
import br.usp.poli.lta.cereda.macro.model.exceptions.MalformedArgumentException;
import br.usp.poli.lta.cereda.macro.model.exceptions.MalformedMacroException;
import br.usp.poli.lta.cereda.macro.model.exceptions.PotentialInfiniteRecursionException;
import br.usp.poli.lta.cereda.macro.model.exceptions.TextRetrievalException;
import br.usp.poli.lta.cereda.macro.util.DisplayUtils;
import br.usp.poli.lta.cereda.macro.util.ScopeController;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Classe principal.
 * @author Paulo Roberto Massa Cereda
 * @version 1.0
 * @since 1.0
 */
public class MacroExpander {

    // logger para gerenciamento do processo de expansão das macros
    private static final Logger logger =
            LogManager.getLogger(MacroExpander.class);

    /**
     * Retorna o texto expandido.
     * @param text Texto a ser analisado e expandido.
     * @return Texto já devidamente expandido.
     * @throws PotentialInfiniteRecursionException Foi detectado uma recursão
     * infinita (em potencial). Ela é determinada através do número de escopos
     * abertos (no momento, definido em 500 instâncias).
     * @throws MalformedArgumentException Um argumento de uma macro paramétrica
     * está mal formado (provavelmente erro sintático).
     * @throws ArgumentNumberMismatchException O número de argumentos requerido
     * para uma macro paramétrica é diferente do número de argumentos fornecido.
     * @throws InvalidIntegerRangeException Um intervalo inteiro inválido foi
     * fornecido como parâmetro para a primitiva de repetição.
     * @throws MacroDefinitionException Foi detectado um problema sintático na
     * definição de uma nova macro.
     * @throws DuplicateMacroException Já existe uma macro com o mesmo nome no
     * escopo corrente.
     * @throws MacroNotFoundException A macro chamada não existe nos escopos da
     * expansão.
     * @throws MalformedMacroException A macro está mal formada (provavelmente
     * um erro sintático).
     * @throws InvalidConditionValueException A primitiva de verificação de
     * condição recebeu um valor inválido.
     * @throws DuplicateCounterException O contador já existe no gerenciador
     * global de contadores.
     * @throws CounterNotFoundException O contador informado não existe no
     * gerenciador global de contadores.
     * @throws TextRetrievalException Ocorreu um erro na recuperação do texto.
     */
    public static String parse(String text)
            throws PotentialInfiniteRecursionException,
            MalformedArgumentException, ArgumentNumberMismatchException,
            InvalidIntegerRangeException, MacroDefinitionException,
            DuplicateMacroException, MacroNotFoundException,
            MalformedMacroException, InvalidConditionValueException,
            DuplicateCounterException, CounterNotFoundException,
            TextRetrievalException {
        
        // inicia a configuração de layout da interface gráfica
        DisplayUtils.init();
        
        // inicia um novo escopo e chama o expansor de macros; é importante
        // destacar que é necessário criar um novo escopo sempre que uma nova
        // instância do expansor é definida; ao término do processo de expansão,
        // o expansor removerá o escopo corrente
        logger.info("Iniciando o expansor de macros no texto: {}", text);
        ScopeController.getInstance().createNewScope();
        Expander expander = new Expander();
        
        // realiza a expansão no texto informado.
        String output = expander.parse(text);
        
        // retorna o resultado
        logger.info("O texto expandido corresponde a: {}", output);
        return output;
    }

}
