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
import br.usp.poli.lta.cereda.macro.util.MacroUtils;
import br.usp.poli.lta.cereda.macro.util.ScopeController;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Classe utilitária principal, responsável pela expansão do texto fornecido.
 * @author Paulo Roberto Massa Cereda
 * @version 1.0
 * @since 1.0
 */
public class Expander {

    // logger para gerenciamento do processo de expansão das macros
    private static final Logger logger = LogManager.getLogger(Expander.class);

    /**
     * Expande o texto fornecido. 
     * @param input Texto a ser expandido.
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
     */
    public String parse(String input)
            throws PotentialInfiniteRecursionException,
            MalformedArgumentException, ArgumentNumberMismatchException,
            InvalidIntegerRangeException, MacroDefinitionException,
            DuplicateMacroException, MacroNotFoundException,
            MalformedMacroException, InvalidConditionValueException,
            DuplicateCounterException, CounterNotFoundException {

        // ativa o método utilitário que contabiliza a entrada em um novo escopo
        // de expansão; esse método é utilizado para evitar uma situação em que
        // a recursão é potencialmente infinita. Foi definido, no escopo deste
        // expansor, que um número de escopos excedendo 500 é considerado uma
        // situação de recursão infinita
        MacroUtils.enterExpansion();

        logger.info("Estou analisando: {}", input);

        // variável que conterá o texto já expandido
        String output = "";
        
        // variáveis que representam os componentes de um autômato de estados
        // finito adaptativo (estado, símbolo corrente e o cursor atuando na
        // cadeia de entrada)
        int state = 1;
        char symbol;
        int cursor = 0;

        // variáveis auxiliares que tratam do nome da macro, o nome de um
        // parâmetro, um mapa contendo os parâmetros da macro e o total de
        // parâmetros
        String macro = "";
        String parameter = "";
        Map<Integer, String> parameters = new HashMap<>();
        int total = 0;

        // delimitadores utilizados para tratar a dependência de contexto do
        // símbolo de marcação da macro e de cada parâmetro; eles simulam a
        // ação adaptativa que define um novo comportamento para o fechamento
        // do escopo
        char delimiter1 = '\0';
        char delimiter2 = '\0';

        // variável lógica que indica o término do processo de reconhecimento
        boolean done = false;

        // inicia o processo de reconhecimento da cadeia de entrada e a eventual
        // expansão da macro
        while (!done && input.length() > 0) {

            // obtém o símbolo corrente que é determinado pela posição do cursor
            // na cadeia de entrada
            symbol = input.charAt(cursor);

            // faz a análise de qual estado o autômato está no momento e aplica
            // a regra correspondente de acordo com o símbolo analisado
            switch (state) {

                case 1:

                    // símbolos convencionais são simplesmente copiados para a
                    // cadeia de saída, verbatim
                    if (symbol != '\\') {
                        output = output + symbol;
                    }
                    else {
                        
                        logger.info("Encontrei um início de macro com o cursor na posição {}.", cursor);

                        // uma macro em potencial, as variáveis auxiliares são
                        // devidamente reinicializadas
                        macro = "";
                        parameters = new HashMap<>();
                        total = 0;
                        
                        // estado de destino do autômato
                        state = 2;
                    }
                    
                    break;

                case 2:
                    
                    if (symbol == '(') {
                        throw new MalformedMacroException(
                                String.format("Encontrei '(' na posição %d. Ele não pode ser delimitador pois indica o início da lista de parâmetros.", cursor)
                        );
                    }

                    logger.info("Encontrei o símbolo '{}' na posição {}. O delimitador da macro é \\\\{} ... {}\\.", symbol, cursor, symbol, symbol);

                    // neste estado, o símbolo corrente na cadeia de entrada
                    // determina o delimitador da macro corrente; no autômato
                    // adaptativo, equivale a uma ação adaptativa que insere o
                    // mesmo delimitador no final para indicar o fechamento do
                    // aninhamento sintático
                    delimiter1 = symbol;

                    // estado de destino do autômato
                    state = 3;
                    break;

                case 3:

                    // verificação se o símbolo corrente é diferente do primeiro
                    // delimitador e se não é uma abertura de parênteses (o que
                    // indica o início de uma lista de parâmetros da macro em
                    // questão); nesse caso, o símbolo é simplesmente copiado na
                    // cadeia de saída
                    if (symbol != delimiter1) {
                        if (symbol != '(') {
                            macro = macro + symbol;
                        }
                        else {
                            logger.info("Encontrei '(' na posição {}, indicando que a macro em questão é paramétrica.", cursor);
                            
                            // o novo estado de destino do autômato, levando o
                            // reconhecimento para o tratamento de parâmetros da
                            // macro corrente
                            state = 5;
                        }
                    }
                    else {
                        logger.info("Encontrei o início do delimitador de fechamento '{}' da macro corrente na posição {}.", delimiter1, cursor);
                        
                        // novo estado de destino, tratando do fechamento da
                        // macro corrente
                        state = 4;
                    }

                    break;

                case 4:

                    // neste estado, espera-se encontrar o símbolo de fechamento
                    // de macros; qualquer outro símbolo gera um erro sintático
                    if (symbol == '\\') {

                        logger.info("Encontrei o símbolo de fechamento de macro na posição {}, resultando na macro '{}' com os parâmetros '{}'.", cursor, macro, parameters);
                        
                        macro = MacroUtils.sanitize(macro);
                        
                        // é feita uma análise para determinar se a macro
                        // encontrada é uma primitiva
                        Primitive result = MacroUtils.
                                checkPrimitive(macro, parameters);
                        
                        // a macro é uma primitiva, portanto é necessário um
                        // tratamento especial por parte do expansor de macros;
                        // o resultado da expansão da primitiva é adicionado na
                        // cadeia de saída
                        if (result != Primitive.NONE) {
                            
                            output = output + MacroUtils.
                                    handlePrimitive(result, parameters);
                        }
                        else {

                            logger.info("Estou procurando a macro '{}' nos escopos disponíveis.", macro);
                            
                            // faz a procura da macro, de acordo com o nome e o
                            // número de parâmetros; observe que a procura
                            // ocorre do escopo no topo da pilha até a base
                            // (a base sendo considerada o escopo global); caso
                            // a macro não seja encontrada (inclusive, com o
                            // número correto de parâmetros), uma exceção é
                            // lançada e a execução é interrompida
                            Macro execute = MacroUtils.
                                    find(macro, parameters.size());
                            
                            // os parâmetros serão convertidos para macros em
                            // um escopo local, após serem analisados e
                            // devidamente expandidos
                            List<Macro> macros = new ArrayList<>();
                            for (int i = 1; i <= parameters.size(); i++) {
                                
                                logger.info("Estou expandindo o parâmetro '{}' da macro '{}' (os parâmetros transformam-se em macros simples no escopo da macro paramétrica).", execute.getParameters().get(i), macro);
                                ScopeController.getInstance().createNewScope();
                                Expander expander = new Expander();
                                macros.add(
                                        new Macro(
                                                execute.getParameters().get(i),
                                                expander.parse(parameters.get(i))
                                        )
                                );
                            }

                            // exibe uma mensagem informando que os parâmetros,
                            // já expandidos e transformados em macros simples,
                            // serão adicionados ao escopo corrente
                            if (!parameters.isEmpty()) {
                                logger.info("Os parâmetros '{}' foram transformados em macros simples para a expansão do corpo da macro {}. Vou adicioná-los ao escopo corrente.", parameters, macro);
                            }
                            
                            // cria-se um novo escopo, adicionam-se as macros
                            // simples criadas anteriormente a partir dos
                            // parâmetros informados e expande-se o corpo da
                            // macro em questão
                            ScopeController.getInstance().createNewScope();
                            for (Macro m : macros) {
                                ScopeController.getInstance().
                                        addMacroToCurrentScope(m);
                            }
                            
                            // chama-se "recursivamente" o expansor de macros
                            // para tratar da macro em questão; o resultado de
                            // expansão é adicionado na cadeia de saída
                            Expander expander = new Expander();
                            output = output + expander.parse(execute.getBody());

                        }
                    }
                    else {
                        
                        // situação de erro, o símbolo de fechamento de macro
                        // não foi encontrado; é necessário abortar a execução
                        throw new MalformedMacroException(
                                String.format("Encontrei uma macro mal formada na posição %d. O símbolo de fechamento era esperado.", cursor)
                        );
                    }

                    // uma vez processada a macro, retorna-se ao estado inicial
                    // do autômato adaptativo
                    state = 1;
                    
                    break;

                case 5:

                    // neste estado, espera-se encontrar o símbolo de abertura
                    // de um parâmetro
                    if (symbol == '\\') {

                        logger.info("Encontrei o símbolo de abertura de um parâmetro na posição {}.", cursor);
                        
                        // novo estado de destino do autômato adaptativo, que
                        // determina o contexto do aninhamento sintático
                        state = 6;
                        
                    }
                    else {
                        
                        // verifica se o símbolo corrente não pode ser ignorado
                        if (!MacroUtils.ignore(symbol)) {
                            
                            // o símbolo de abertura de um parâmetro não foi
                            // encontrado, lançar exceção
                            throw new MalformedArgumentException(String.format("Encontrei um argumento mal formado (símbolo de abertura esperado) na posição %d.", cursor));
                        }
                    }

                    break;

                case 6:

                    // neste estado, é definido o delimitador de parâmetros da
                    // macro corrente
                    logger.info("Encontrei o símbolo '{}' na posição {}. O delimitador do parâmetro {} será \\\\{} ... {}\\.", symbol, cursor, (total + 1), symbol, symbol);
                    delimiter2 = symbol;
                    
                    // reinicializa a variável para receber, ao longo do
                    // processo de reconhecimento, o nome do parâmetro
                    parameter = "";
                    
                    // novo estado do autômato adaptativo
                    state = 7;

                    break;

                case 7:

                    // enquanto o símbolo corrente não for o símbolo delimitador
                    // do parâmetro corrente, adicioná-lo na cadeia de saída do
                    // parâmetro
                    if (symbol != delimiter2) {
                        parameter = parameter + symbol;
                    }
                    else {
                        
                        // o delimitador do parâmetro foi encontrado
                        logger.info("Encontrei o início do delimitador de fechamento '{}' do parâmetro {} na posição {}.", symbol, (total + 1), cursor);
                        
                        // novo estado do autômato adaptativo
                        state = 8;
                    }

                    break;

                case 8:

                    // neste estado, espera-se encontrar o símbolo de fechamento
                    // do parâmetro corrente
                    if (symbol == '\\') {
                        
                        logger.info("O parâmetro '{}' foi encontrado.", parameter);
                        
                        // o parâmetro foi determinado corretamente; o número de
                        // parâmetros é incrementado e o parâmetro é adicionado
                        // no mapa de parâmetros, indexado por sua posição na
                        // chamada
                        total++;
                        parameters.put(total, parameter);
                        
                        // novo estado do autômato adaptativo, na situação em
                        // que espera-se um separador para inserção de mais
                        // parâmetros ou o fechamento da lista de parâmetros
                        state = 9;
                        
                    }
                    else {
                        
                        // o argumento possui erro sintático, lançar exceção
                        throw new MalformedArgumentException(
                                String.format("Encontrei um argumento mal formado (término esperado) na posição %d.", cursor)
                        );
                    }

                    break;

                case 9:

                    // neste estado, espera-se encontrar um dos seguintes
                    // símbolos: separador de parâmetros ou fechamento da lista
                    // de parâmetros
                    
                    // o separador de parâmetros foi encontrado
                    if (symbol == ',') {
                        
                        logger.info("Encontrei o separador de parâmetros ',' na posição {}.", cursor);
                        
                        // novo estado de destino, retornando a análise para o
                        // estado em que espera-se um novo parâmetro
                        state = 5;
                        
                    }
                    else {
                        
                        // o fechamento da lista de parãmetros foi encontrado
                        if (symbol == ')') {
                            
                            logger.info("Encontrei o término da lista de parâmetros (fechamento de parênteses) na posição {}.", cursor);
                            
                            // novo estado de destino do autômato, retornando
                            // a análise o estado em que espera-se o delimitador
                            // de fechamento da macro paramétrica
                            state = 10;
                        }
                        else {
                            
                            // verifica se o símbolo é inválido no contexto
                            if (!MacroUtils.ignore(symbol)) {
                            
                                // erro sintático, lançar exceção
                                throw new MalformedMacroException(
                                        String.format("Era esperado o término da definição dos parâmetros da macro ou o separador de parâmetros na posição %d.", cursor)
                                );
                                
                            }
                        }
                        
                    }

                    break;

                case 10:

                    // neste estado, espera-se encontrar o símbolo delimitador
                    // de fechamento da macro paramétrica
                    if (symbol == delimiter1) {
                        
                        logger.info("Encontrei o início do delimitador de fechamento '{}' da macro paramétrica na posição {}.", symbol, cursor);
                        
                        // novo estado de destino, retornando para a situação
                        // na qual espera-se o símbolo de fechamento de macros
                        state = 4;
                    }                    
                    else {
                        
                        // verifica se o símbolo é inválido no contexto
                        if (!MacroUtils.ignore(symbol)) {
                            // ocorreu um erro sintático, a macro é mal formada
                            throw new MalformedMacroException(
                                    String.format("Encontrei uma macro mal formada (delimitador de fechamento '%c' da macro paramétrica era esperado) na posição %d.", delimiter1, cursor)
                            );
                        }
                    }
                    break;

            }

            // move o cursor de leitura para a posição seguinte na cadeia de
            // entrada
            cursor++;
            
            // verifica-se se a posição do cursor aponta para uma posição válida
            // na cadeia de entrada
            done = cursor >= input.length();

        }

        // é imperativo que o autômato termine o reconhecimento no estado de
        // aceitação; caso contrário, a análise do texto encerrou-se durante o
        // processamento de uma macro
        if (state != 1) {
            throw new MalformedMacroException("A análise do texto encerrou prematuramente durante o processamento de uma macro.");
        }

        // a expansão foi concluída com sucesso; libera-se o escopo corrente e
        // reduz o contador do número de expansões até o momento
        MacroUtils.exitExpansion();
        ScopeController.getInstance().removeCurrentScope();

        // retorna o texto devidamente expandido
        return output;

    }

}
