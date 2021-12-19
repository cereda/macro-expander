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

import br.usp.poli.lta.cereda.macro.model.Expander;
import br.usp.poli.lta.cereda.macro.model.Macro;
import br.usp.poli.lta.cereda.macro.model.Pair;
import br.usp.poli.lta.cereda.macro.model.Primitive;
import br.usp.poli.lta.cereda.macro.model.Stack;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Fornece um conjunto de métodos utilitários para o expansor de macros.
 * @author Paulo Roberto Massa Cereda
 * @version 1.0
 * @since 1.0
 */
public class MacroUtils {

    // logger para gerenciamento do processo de expansão das macros
    private static final Logger logger = LogManager.getLogger(MacroUtils.class);
    
    // número de chamadas ao expansor de macros, na tentativa de evitar uma
    // possível recursão infinita
    private static int calls = 0;

    // representação de uma cadeia vazia
    private static final String EMPTY_STRING = "";
    
    // expressão regular que define as primitivas suportadas pelo expansor
    private static final String REGEX_PRIMITIVES =
            "^\\s*(\\bcomment\\b|\\bnew\\s+line\\b|\\bnew\\s+page\\b|\\bno\\s+expand\\b|\\brepeat\\b|\\binput\\s+text\\b|\\bsend\\s+message\\b|\\bdefine\\b|\\bglobal\\s+define\\b|\\bfor\\s+each\\b|\\bincrement\\b|\\bdecrement\\b|\\bincrement\\s+counter\\b|\\bdecrement\\s+counter\\b|\\bset\\s+counter\\b|\\bnew\\s+counter\\b|\\bcounter\\b|\\bcheck\\s+condition\\b|\\bis\\s+zero\\b|\\bis\\s+greater\\s+than\\b|\\bis\\s+less\\s+than\\b|\\bis\\s+equal\\b|\\bupload\\s+to\\s+google\\s+drive\\b|\\bget\\s+from\\s+google\\s+drive\\b|\\bget\\s+url\\b)\\s*?";

    // conjunto de símbolos ignorados pelo autômato
    private static final Set<Character> ignored =
            new HashSet<>(Arrays.asList(' ', '\t', '\n', '\r'));
    
    /**
     * Procura a macro paramêtrica em todos os escopos, a partir do local,
     * aumentando o nível até o global.
     * @param name Nome da macro.
     * @param parameters Número de parâmetros.
     * @return A macro procurada.
     * @throws MacroNotFoundException A macro procurada não existe nos escopos.
     */
    public static Macro find(String name, int parameters)
            throws MacroNotFoundException {

        logger.info(
                "Tentando encontrar a macro '{}' com {} parâmetros em todos os escopos.",
                name,
                parameters
        );

        // obtém a pilha do controlador de escopos
        Stack<Set<Macro>> stack = ScopeController.getInstance().getStack();

        // a partir da pilha, obtém a lista de escopos para realizar a busca
        List<Set<Macro>> scopes = stack.getList();

        // a lista de escopos é percorrida de modo reverso, do último escopo
        // inserido até o primeiro escopo (por definição, o escopo 0 é global).
        for (int i = scopes.size() - 1; i >= 0; i--) {
            
            logger.info(
                    "Procurando a macro no escopo {}.",
                    i
            );
            
            // procura pela macro no escopo corrente, procurando pelo nome e o
            // número de parâmetros
            for (Macro macro : scopes.get(i)) {
                
                // a macro foi encontrada, esta é retornada e a busca encerra-se
                if (macro.getName().equals(name) &&
                        macro.getParameters().size() == parameters) {
                    
                    logger.info(
                            "Encontrei a macro '{}' no escopo {}.",
                            name,
                            i
                    );
                    
                    return macro;
                }

            }
        }
        
        logger.error(
                "A macro '{}' com {} parâmetros não foi encontrada nos escopos.",
                name,
                parameters
        );
        
        // a macro não foi encontrada, lançar exceção
        throw new MacroNotFoundException(
                String.format(
                        "A macro '%s' (%d) não foi encontrada nos escopos.",
                        name,
                        parameters
                )
        );
    }

    /**
     * Procura a macro simples em todos os escopos, a partir do local,
     * aumentando o nível até o global.
     * @param name Nome da macro.
     * @return A macro procurada.
     * @throws MacroNotFoundException A macro não existe nos escopos.
     */
    public static Macro find(String name) throws MacroNotFoundException {
        return find(name, 0);
    }

    /**
     * Incrementa o contador de chamadas ao expansor e verifica se este está
     * em uma situação de uma recursão infinita em potencial (no momento,
     * definida em mais de 500 chamadas).
     * @throws PotentialInfiniteRecursionException O expansor foi chamado mais
     * de 500 vezes sem retornar.
     */
    public static void enterExpansion()
            throws PotentialInfiniteRecursionException {
        
        calls++;
        logger.info(
                "Entrando na expansão ({}).",
                calls
        );
        
        // o limite máximo de chamadas sem retornar foi alcançado
        if (calls > 500) {
            logger.error(
                    "A execução atingiu 500 chamadas internas, o que indica uma recursão infinita em potencial."
            );
            throw new PotentialInfiniteRecursionException(
                    "A execução atingiu 500 chamadas internas, o que indica uma recursão infinita em potencial."
            );
        }
    }

    /**
     * Decrementa o contador de chamadas ao expansor.
     */
    public static void exitExpansion() {
        logger.info(
                "Saindo da expansão."
        );
        calls--;
    }

    /**
     * Verifica se a macro (simples ou paramétrica) é uma primitiva.
     * @param name Nome da macro.
     * @param parameters Mapa de parâmetros.
     * @return Enumeração com a classificação da macro fornecida.
     */
    public static Primitive checkPrimitive(String name,
            Map<Integer, String> parameters) {
        
        logger.info(
                "Verificando se a macro '{}' com parâmetros '{}' é uma primitiva.",
                name,
                parameters
        );

        // utiliza-se uma expressão regular para verificar se a macro fornecida
        // é uma primitiva
        Pattern pattern = Pattern.compile(REGEX_PRIMITIVES);
        Matcher matcher = pattern.matcher(name);
        
        // a macro é uma primitiva, classificar
        if (matcher.matches()) {
            String primitive = matcher.group(1).replaceAll("\\s+", " ").trim();

            logger.info(
                    "A primitiva {} foi encontrada.",
                    primitive
            );
            Primitive result;

            if (primitive.equals("comment")) {
                result = Primitive.COMMENT;
            }
            else {
                if (primitive.equals("new line")) {
                    result = Primitive.NEWLINE;
                }
                else {
                    if (primitive.equals("new page")) {
                        result = Primitive.NEWPAGE;
                    }
                    else {
                        if (primitive.equals("no expand")) {
                            result = Primitive.NOEXPAND;
                        }
                        else {
                            if (primitive.equals("repeat")) {
                                result = Primitive.REPEAT;
                            }
                            else {
                                if (primitive.equals("input text")) {
                                    result = Primitive.INPUTTEXT;
                                }
                                else {
                                    if (primitive.equals("send message")) {
                                        result = Primitive.SENDMESSAGE;
                                    }
                                    else {
                                        if (primitive.equals("define")) {
                                            result = Primitive.DEFINE;
                                        }
                                        else {
                                            if (primitive.equals("global define")) {
                                                result = Primitive.GLOBALDEFINE;
                                            }
                                            else {
                                                if (primitive.equals("for each")) {
                                                    result = Primitive.FOREACH;
                                                }
                                                else {
                                                    if (primitive.equals("increment")) {
                                                        result = Primitive.INCREMENT;
                                                    }
                                                    else {
                                                        if (primitive.equals("decrement")) {
                                                            result = Primitive.DECREMENT;
                                                        }
                                                        else {
                                                            if (primitive.equals("new counter")) {
                                                                result = Primitive.NEWCOUNTER;
                                                            }
                                                            else {
                                                                if (primitive.equals("set counter")) {
                                                                    result = Primitive.SETCOUNTER;
                                                                }
                                                                else {
                                                                    if (primitive.equals("counter")) {
                                                                        result = Primitive.COUNTER;
                                                                    }
                                                                    else {
                                                                        if (primitive.equals("increment counter")) {
                                                                            result = Primitive.INCREMENTCOUNTER;
                                                                        }
                                                                        else {
                                                                            if (primitive.equals("decrement counter")) {
                                                                                result = Primitive.DECREMENTCOUNTER;
                                                                            }
                                                                            else {
                                                                                if (primitive.equals("check condition")) {
                                                                                    result = Primitive.CHECKCONDITION;
                                                                                }
                                                                                else {
                                                                                    if (primitive.equals("is zero")) {
                                                                                        result = Primitive.ISZERO;
                                                                                    }
                                                                                    else {
                                                                                        if (primitive.equals("is greater than")) {
                                                                                            result = Primitive.ISGREATERTHAN;
                                                                                        }
                                                                                        else {
                                                                                            if (primitive.equals("is less than")) {
                                                                                                result = Primitive.ISLESSTHAN;
                                                                                            }
                                                                                            else {
                                                                                                if (primitive.equals("is equal")) {
                                                                                                    result = Primitive.ISEQUAL;
                                                                                                }
                                                                                                else {
                                                                                                    result = Primitive.GETURL;
                                                                                                }
                                                                                            }
                                                                                        }
                                                                                    }
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            return result;
        }
        else {
            
            // a macro fornecida não é uma primitiva
            return Primitive.NONE;
        }

    }

    /**
     * Trata a primitiva de acordo com seus parâmetros.
     * @param primitive Primitiva.
     * @param parameters Mapa de parâmetros.
     * @return Cadeia de saída do tratamento da primitiva.
     * @throws MalformedArgumentException Um argumento da macro está mal formado
     * (provavelmente um erro sintático).
     * @throws ArgumentNumberMismatchException O número de argumentos da
     * primitiva não coincide com sua definição.
     * @throws PotentialInfiniteRecursionException O expansor de macros
     * provavelmente entrou em uma situação de recursão infinita.
     * @throws InvalidIntegerRangeException O valor fornecido como pârametro
     * para a primitiva de repetição está em um intervalo inteiro inválido.
     * @throws MacroDefinitionException Erro sintático na definição de uma nova
     * macro.
     * @throws DuplicateMacroException A macro já existe no escopo corrente.
     * @throws MacroNotFoundException A macro não foi encontrada.
     * @throws MalformedMacroException A macro está mal formada (provavelmente
     * um erro sintático)
     * @throws InvalidConditionValueException O valor indicado na condição é
     * inválido.
     * @throws DuplicateCounterException O contador já existe no gerenciador.
     * @throws CounterNotFoundException O contador não existe no gerenciador.
     * @throws TextRetrievalException Ocorreu um erro na tentativa de
     * recuperação do texto.
     */
    public static String handlePrimitive(Primitive primitive,
            Map<Integer, String> parameters) throws MalformedArgumentException,
            ArgumentNumberMismatchException,
            PotentialInfiniteRecursionException, InvalidIntegerRangeException,
            MacroDefinitionException, DuplicateMacroException,
            MacroNotFoundException, MalformedMacroException,
            InvalidConditionValueException, DuplicateCounterException,
            CounterNotFoundException, TextRetrievalException {

        // cadeia de saída, inicialmente vazia
        String output = EMPTY_STRING;

        // realiza o tratamento da primitiva de acordo com sua classificação
        switch (primitive) {

            case DEFINE:
            case GLOBALDEFINE:

                logger.info(
                        "Encontrei uma primitiva de definição de novas macros."
                );

                // o número de parâmetros é inválido
                if (parameters.size() != 1) {
                    
                    logger.info(
                            "A definição de novas macros requer um parâmetro."
                    );
                    throw new ArgumentNumberMismatchException(
                            "A definição de novas macros requer um parâmetro."
                    );
                }

                // define-se uma nova macro a partir do parâmetro da primitiva
                Macro macro = defineNewMacro(parameters.get(1));
                
                // se a primitiva é local, tenta-se adicionar a nova macro no
                // escopo local, ou lançar uma exceção caso uma macro com as
                // mesmas características já exista no escopo
                if (primitive == Primitive.DEFINE) {
                    
                    if (!ScopeController.getInstance().addMacroToCurrentScope(macro)) {
                        logger.error(
                                "Não foi possível adicionar a macro '{}' no escopo corrente. Ela já está definida.",
                                macro
                        );
                        throw new DuplicateMacroException(
                                String.format(
                                        "Não foi possível adicionar a macro '%s' no escopo corrente. Ela já está definida.",
                                        macro
                                )
                        );
                    }
                    
                }
                else {
                    
                    // a primitiva prevê a inserção da macro no escopo global,
                    // ou lança-se um erro caso uma macro com as mesmas 
                    // características já exista no escopo
                    if (!ScopeController.getInstance().addMacroToGlobalScope(macro)) {
                        logger.error(
                                "Não foi possível adicionar a macro '{}' no escopo global. Ela já está definida.",
                                macro
                        );
                        throw new DuplicateMacroException(
                                String.format(
                                        "Não foi possível adicionar a macro '%s' no escopo global. Ela já está definida.",
                                        macro
                                )
                        );
                    }
                }

                break;

            case NEWLINE:

                logger.info(
                        "Encontrei uma primitiva de definição de nova linha."
                );

                // o número de parâmetros é inválido, tem que ser vazio
                if (!parameters.isEmpty()) {
                    logger.info(
                            "A definição de nova linha não tem parâmetro."
                    );
                    throw new ArgumentNumberMismatchException(
                            "A definição de nova linha não tem parâmetro."
                    );
                }

                // define o valor da cadeia de saída
                output = "\n";
                break;

            case NEWPAGE:

                logger.info(
                        "Encontrei uma primitiva de definição de nova página."
                );

                // o número de parâmetros é inválido, tem que ser vazio
                if (!parameters.isEmpty()) {
                    logger.info(
                            "A definição de nova página não tem parâmetro."
                    );
                    throw new ArgumentNumberMismatchException(
                            "A definição de nova página não tem parâmetro."
                    );
                }

                // define o valor da cadeia de saída
                output = "<NEW PAGE BODY>";
                break;

            case NOEXPAND:

                logger.info(
                        "Encontrei uma primitiva de bloco literal."
                );

                // o número de parâmetros é inválido
                if (parameters.size() != 1) {
                    logger.info(
                            "A primitiva de bloco literal requer um parâmetro."
                    );
                    throw new ArgumentNumberMismatchException(
                            "A primitiva de bloco literal requer um parâmetro."
                    );
                }

                // define o valor da cadeia de saída, que é o bloco literal
                output = parameters.get(1);
                break;

            case INPUTTEXT:
            case SENDMESSAGE:
            case REPEAT:

                // o número de parâmetros é inválido
                if (parameters.size() != 2) {
                    logger.error(
                            "O número de parâmetros da primitiva '{}' não confere (a primitiva requer dois parâmetros). Foi encontrado: {}",
                            primitive,
                            parameters
                    );
                    throw new ArgumentNumberMismatchException(
                            String.format("O número de parâmetros da primitiva '%s' não confere. (a primitiva requer dois parâmetros). Foi encontrado: %s",
                                    primitive, parameters
                            )
                    );
                }

                // se é uma entrada de texto, exibe a tela de diálogo para a
                // entrada do texto
                if (primitive == Primitive.INPUTTEXT) {

                    logger.info(
                            "Tratando a primitiva 'input text'."
                    );

                    // cria-se um novo escopo, trata do primeiro parâmetro
                    ScopeController.getInstance().createNewScope();
                    Expander expander = new Expander();

                    logger.info(
                            "Expandindo o primeiro parâmetro."
                    );
                    String first = expander.parse(parameters.get(1));

                    // cria-se um novo escopo, trata do segundo parâmetro
                    ScopeController.getInstance().createNewScope();
                    expander = new Expander();

                    logger.info(
                            "Expandindo o segundo parâmetro."
                    );
                    String second = expander.parse(parameters.get(2));

                    // exibe a tela de diálogo e obtém o texto
                    logger.info(
                            "Exibindo a janela de edição."
                    );
                    Pair<Boolean, String> pair =
                            DisplayUtils.getInputText(first, second);

                    // verifica se o texto retornado deve ser expandido
                    if (pair.getFirst()) {

                        logger.info(
                                "Expandindo o texto informado."
                        );
                        
                        // cria-se um novo escopo, trata do texto informado
                        ScopeController.getInstance().createNewScope();
                        expander = new Expander();
                        
                        // define o valor da cadeia de saída
                        output = expander.parse(pair.getSecond());

                    }
                    else {
                        
                        // define o valor da cadeia de saída, o texto obtido
                        // será inserido verbatim
                        output = pair.getSecond();
                    }

                }
                else {
                    
                    // verifica se a primitiva é de envio de mensagem
                    if (primitive == Primitive.SENDMESSAGE) {

                        logger.info(
                                "Tratando a primitiva 'send message'."
                        );

                        // cria-se um novo escopo, trata do primeiro parâmetro
                        ScopeController.getInstance().createNewScope();
                        Expander expander = new Expander();

                        logger.info(
                                "Expandindo o primeiro parâmetro."
                        );
                        String first = expander.parse(parameters.get(1));

                        // cria-se um novo escopo, trata do segundo parâmetro
                        ScopeController.getInstance().createNewScope();
                        expander = new Expander();

                        logger.info(
                                "Expandindo o segundo parâmetro."
                        );
                        String second = expander.parse(parameters.get(2));

                        // exibe a mensagem
                        logger.info(
                                "Exibindo a mensagem ao usuário."
                        );
                        DisplayUtils.showMessage(first, second);

                    }
                    else {

                        // trata da primitiva de repetição
                        logger.info(
                                "Tratando a primitiva 'repeat'."
                        );

                        // cria-se um novo escopo, trata do primeiro parâmetro
                        ScopeController.getInstance().createNewScope();
                        Expander expander = new Expander();

                        logger.info(
                                "Expandindo o primeiro parâmetro."
                        );
                        String first = expander.parse(parameters.get(1));

                        // a primitiva de repetição requer um valor inteiro
                        int times = 0;
                        
                        // converte o valor textual para um valor inteiro
                        try {
                            logger.info(
                                    "Tentando converter o primeiro parâmetro para um valor inteiro."
                            );
                            times = Integer.parseInt(first.trim());
                        }
                        catch (NumberFormatException exception) {
                            logger.error(
                                    "Não foi possível converter o parâmetro da primitiva 'repeat', esperando um valor inteiro."
                            );
                            throw new NumberFormatException(
                                    "Não foi possível converter o parâmetro da primitiva 'repeat', esperando um valor inteiro."
                            );
                        }

                        // verifica o intervalo do valor inteiro
                        if (times <= 0) {
                            logger.error(
                                    "O primeiro parâmetro possui um intervalo inválido."
                            );
                            throw new InvalidIntegerRangeException(
                                    "O primeiro parâmetro possui um intervalo inválido."
                            );
                        }

                        // repete a expansão de acordo com o número de vezes
                        // fornecido como primeiro parâmetro da primitiva
                        for (int i = 1; i <= times; i++) {

                            // cria-se um novo escopo, trata do segundo
                            // parâmetro a cada iteração
                            ScopeController.getInstance().createNewScope();
                            expander = new Expander();

                            logger.info(
                                    "(Iteração {}) Expandindo o segundo parâmetro.",
                                    i
                            );
                            String second = expander.parse(parameters.get(2));

                            logger.info(
                                    "(Iteração {}) Adicionando a saída na repetição.",
                                    i
                            );
                            output = output + second;

                        }
                    }
                }
                break;
                
            case FOREACH:
                
                // o número de parâmetros é inválido
                if (parameters.size() < 2) {
                    logger.error("O número de parâmetros da primitiva 'foreach' não confere (a primitiva requer no mínimo dois parâmetros). Foi encontrado: {}", parameters);
                    throw new ArgumentNumberMismatchException(
                            String.format(
                                    "O número de parâmetros da primitiva 'foreach' não confere. (a primitiva requer no mínimo dois parâmetros). Foi encontrado: %s",
                                    parameters
                            )
                    );
                }
                else {
                    
                    // obtém o padrão a ser expandido
                    String pattern = parameters.get(parameters.size());

                    // percorre a lista de parâmetros, replicando cada elemento
                    // no padrão informado
                    for (int i = 1; i < parameters.size(); i++) {

                        // cria um novo escopo e expande o elemento da lista
                        ScopeController.getInstance().createNewScope();
                        Expander expander = new Expander();

                        // uma nova macro é criada para definir o valor de cada
                        // elemento da lista expandido no padrão
                        Macro m = new Macro("it", 
                                expander.parse(parameters.get(i)));

                        // cria um novo escopo, adiciona a nova macro no escopo
                        // corrente e expande o padrão
                        ScopeController.getInstance().createNewScope();
                        ScopeController.getInstance().addMacroToCurrentScope(m);
                        
                        expander = new Expander();
                        output = output + expander.parse(pattern);

                    }
                }
                
                break;
                
            case CHECKCONDITION:
                
                // o número de parâmetros é inválido
                if (parameters.size() != 3) {
                    logger.error(
                            "O número de parâmetros da primitiva 'check condition' não confere (a primitiva requer três parâmetros). Foi encontrado: {}",
                            parameters
                    );
                    throw new ArgumentNumberMismatchException(
                            String.format(
                                    "O número de parâmetros da primitiva 'check condition' não confere. (a primitiva requer três parâmetros). Foi encontrado: %s",
                                    parameters
                            )
                    );
                }
                else {
                    
                    // cria novo escopo e expande a condição
                    ScopeController.getInstance().createNewScope();
                    Expander expander = new Expander();
                    String condition = expander.parse(parameters.get(1)).trim();

                    // se a condição é verdadeira, expande o segundo parâmetro,
                    // ou o terceiro parâmetro, caso contrário
                    ScopeController.getInstance().createNewScope();
                    expander = new Expander();
                    output = expander.parse(
                            parameters.get(getConditionIndex(condition)));
                }
            
                break;
                
            case NEWCOUNTER:
                
                // o número de parâmetros é inválido
                if (parameters.size() != 1) {
                    logger.error(
                            "O número de parâmetros da primitiva 'new counter' não confere (a primitiva requer um parâmetro). Foi encontrado: {}",
                            parameters
                    );
                    throw new ArgumentNumberMismatchException(
                            String.format(
                                    "O número de parâmetros da primitiva 'new counter' não confere. (a primitiva requer um parâmetro). Foi encontrado: %s",
                                    parameters
                            )
                    );
                }
                else {
                    
                    // cria um novo escopo e expande o nome do contador
                    ScopeController.getInstance().createNewScope();
                    Expander expander = new Expander();
                    String name = expander.parse(parameters.get(1));
                    
                    // se o contador já existe, é um erro de definição
                    if (Counters.getInstance().contains(name)) {
                        throw new DuplicateCounterException(
                                String.format(
                                        "O contador '%s' já está definido.",
                                        name
                                )
                        );
                    }
                    else {
                        
                        // cria um novo contador no gerenciador global de
                        // contadores e define seu valor inicial como zero
                        Counters.getInstance().set(name, 0);
                    }
                }
                
                break;
                
            case SETCOUNTER:
                
                // o número de parâmetros é inválido
                if (parameters.size() != 2) {
                    logger.error(
                            "O número de parâmetros da primitiva 'set counter' não confere (a primitiva requer dois parâmetros). Foi encontrado: {}",
                            parameters
                    );
                    throw new ArgumentNumberMismatchException(
                            String.format(
                                    "O número de parâmetros da primitiva 'set counter' não confere. (a primitiva requer dois parâmetros). Foi encontrado: %s",
                                    parameters
                            )
                    );
                }
                else {
                    
                    // cria um novo escopo e expande o nome do contador
                    ScopeController.getInstance().createNewScope();
                    Expander expander = new Expander();
                    String name = expander.parse(parameters.get(1));
                    
                    // cria um novo escopo e expande o valor do contador
                    ScopeController.getInstance().createNewScope();
                    expander = new Expander();
                    String parameter = expander.parse(parameters.get(2));
                    
                    // converte o valor obtido para uma representação inteira
                    int value;
                    try {
                        value = Integer.parseInt(parameter.trim());
                    }
                    catch (NumberFormatException exception) {
                        throw new NumberFormatException(
                                "Não foi possível converter o valor do contador para uma representação inteira."
                        );
                    }
                    
                    // tenta atribuir o novo valor ao contador
                    if (Counters.getInstance().contains(name)) {
                        Counters.getInstance().set(name, value);
                    }
                    else {
                        throw new CounterNotFoundException(
                                String.format(
                                        "Não foi possível definir o valor do contador '%s' porque este não foi definido.", name
                                )
                        );
                    }
                }
            
                break;
                
            case COUNTER:
                
                // número inválido de parâmetros
                if (parameters.size() != 1) {
                    logger.error(
                            "O número de parâmetros da primitiva counter não confere (a primitiva requer um parâmetro). Foi encontrado: {}",
                            parameters
                    );
                    throw new ArgumentNumberMismatchException(
                            String.format(
                                    "O número de parâmetros da primitiva 'counter' não confere. (a primitiva requer um parâmetro). Foi encontrado: %s",
                                    parameters
                            )
                    );
                }
                else {
                    
                    // cria um novo escopo e expande o nome do contador
                    ScopeController.getInstance().createNewScope();
                    Expander expander = new Expander();
                    String name = expander.parse(parameters.get(1));
                    
                    // obtém o valor do contador, converte-o para o formato
                    // textual e adiciona na cadeia de saída
                    if (Counters.getInstance().contains(name)) {
                        output = String.valueOf(
                                Counters.getInstance().get(name));
                    }
                    else {
                        throw new CounterNotFoundException(
                                String.format(
                                        "Não foi possível obter o valor do contador '%s' porque este não foi definido.",
                                        name
                                )
                        );
                    }
                }
            
                break;
                
            case INCREMENT:
            case DECREMENT:
                
                // número inválido de parâmetros
                if (parameters.size() != 1) {
                    logger.error(
                            "O número de parâmetros da primitiva '{}' não confere (a primitiva requer um parâmetro). Foi encontrado: {}",
                            primitive,
                            parameters
                    );
                    throw new ArgumentNumberMismatchException(
                            String.format(
                                    "O número de parâmetros da primitiva '%s' não confere. (a primitiva requer um parâmetro). Foi encontrado: %s",
                                    primitive,
                                    parameters
                            )
                    );
                }
                else {
                    
                    // cria um novo escopo e expande o parâmetro
                    ScopeController.getInstance().createNewScope();
                    Expander expander = new Expander();
                    String parameter = expander.parse(parameters.get(1));
                    
                    // obtém a representação inteira do parâmetro
                    int value;
                    try {
                        value = Integer.parseInt(parameter.trim());
                    }
                    catch (NumberFormatException exception) {
                        throw new NumberFormatException(
                                "Não foi possível converter o valor do parâmetro para uma representação inteira."
                        );
                    }
                    
                    // realiza a operação de acordo com o tipo de primitiva
                    // (incremento ou decremento do valor)
                    if (primitive == Primitive.INCREMENT) {
                        value++;
                    }
                    else {
                        value--;
                    }
                    
                    // retorna uma representação textual do valor obtido
                    output = String.valueOf(value);
                    
                }
            
                break;
                
            case INCREMENTCOUNTER:
            case DECREMENTCOUNTER:
                
                // número inválido de parâmetros
                if (parameters.size() != 1) {
                    logger.error(
                            "O número de parâmetros da primitiva '{}' não confere (a primitiva requer um parâmetro). Foi encontrado: {}",
                            primitive,
                            parameters
                    );
                    throw new ArgumentNumberMismatchException(
                            String.format(
                                    "O número de parâmetros da primitiva '%s' não confere. (a primitiva requer um parâmetro). Foi encontrado: %s",
                                    primitive,
                                    parameters
                            ));
                }
                else {
                 
                    // cria um novo escopo e expande o nome do contador
                    ScopeController.getInstance().createNewScope();
                    Expander expander = new Expander();
                    String name = expander.parse(parameters.get(1));
                    
                    // verifica se o contador existe no gerenciador global de
                    // contadores
                    if (Counters.getInstance().contains(name)) {
                        
                        // obtém o valor a partir do nome do contador
                        int value = Counters.getInstance().get(name);
                        
                        // realiza a operação de acordo com o tipo de primitiva
                        // (incremento ou decremento do valor do contador)
                        if (primitive == Primitive.INCREMENTCOUNTER) {
                            value++;
                        }
                        else {
                            value--;
                        }
                        
                        // atualiza o contador com o novo valor
                        Counters.getInstance().set(name, value);
                        
                    }
                    else {
                        throw new CounterNotFoundException(
                                String.format(
                                        "Não foi possível definir o valor do contador '%s' porque este não foi definido.",
                                        name
                                )
                        );
                    }
                    
                }
            
                break;
                
            case ISGREATERTHAN:
            case ISLESSTHAN:
            case ISEQUAL:
                
                // número inválido de parâmetros
                if (parameters.size() != 2) {
                    logger.error("O número de parâmetros da primitiva '{}' não confere (a primitiva requer dois parâmetros). Foi encontrado: {}",
                            primitive,
                            parameters
                    );
                    throw new ArgumentNumberMismatchException(
                            String.format(
                                    "O número de parâmetros da primitiva '%s' não confere. (a primitiva requer dois parâmetros). Foi encontrado: %s",
                                    primitive,
                                    parameters
                            )
                    );
                }
                else {
                    
                    // cria um novo escopo e expande o primeiro valor
                    ScopeController.getInstance().createNewScope();
                    Expander expander = new Expander();
                    String parameter1 = expander.parse(parameters.get(1));
                    
                    // cria um novo escopo e expande o segundo valor
                    ScopeController.getInstance().createNewScope();
                    expander = new Expander();
                    String parameter2 = expander.parse(parameters.get(2));
                    
                    // obtém a representação inteira dos dois parâmetros
                    int value1;
                    int value2;
                    try {
                        value1 = Integer.parseInt(parameter1.trim());
                        value2 = Integer.parseInt(parameter2.trim());
                    }
                    catch (NumberFormatException exception) {
                        throw new NumberFormatException(
                                "Não foi possível converter o valor do parâmetro para uma representação inteira."
                        );
                    }
                    
                    // verifica qual operação realizar e realiza o teste
                    // necessário
                    if (primitive == Primitive.ISGREATERTHAN) {
                        output = value1 > value2 ? "true" : "false";
                    }
                    else {
                        if (primitive == Primitive.ISLESSTHAN) {
                            output = value1 < value2 ? "true" : "false";
                        }
                        else {
                            output = value1 == value2 ? "true" : "false";
                        }
                    }
                    
                }
            
                break;
                
            case ISZERO:
                
                // número inválido de parâmetros
                if (parameters.size() != 1) {
                    logger.error(
                            "O número de parâmetros da primitiva 'is zero' não confere (a primitiva requer um parâmetro). Foi encontrado: {}", parameters);
                    throw new ArgumentNumberMismatchException(
                            String.format(
                                    "O número de parâmetros da primitiva 'is zero' não confere. (a primitiva requer um parâmetro). Foi encontrado: %s",
                                    parameters
                            )
                    );
                }
                else {
                    
                    // cria um novo escopo e expande o parâmetro
                    ScopeController.getInstance().createNewScope();
                    Expander expander = new Expander();
                    String parameter = expander.parse(parameters.get(1));
                    
                    // obtém uma representação inteira do parâmetro
                    int value;
                    try {
                        value = Integer.parseInt(parameter.trim());
                    }
                    catch (NumberFormatException exception) {
                        throw new NumberFormatException(
                                "Não foi possível converter o valor do parâmetro para uma representação inteira."
                        );
                    }
                    
                    // retorna o resultado da comparação com zero
                    output = value == 0 ? "true" : "false";
                    
                }
            
                break;
                
            case GETURL:
                
                // número inválido de parâmetros
                if (parameters.size() != 1) {
                    logger.error(
                            "O número de parâmetros da primitiva '{}' não confere (a primitiva requer um parâmetro). Foi encontrado: {}",
                            primitive,
                            parameters
                    );
                    throw new ArgumentNumberMismatchException(
                            String.format(
                                    "O número de parâmetros da primitiva '%s' não confere. (a primitiva requer um parâmetro). Foi encontrado: %s",
                                    primitive,
                                    parameters
                            )
                    );
                }
                else {
                    
                    // cria um novo escopo e expande o parâmetro
                    ScopeController.getInstance().createNewScope();
                    Expander expander = new Expander();
                    String parameter = expander.parse(parameters.get(1));                  
                    output = CommonUtils.get(parameter);
                    
                }
                
                break;
                

        }

        // retorna a cadeia de saída
        return output;
    }

    /**
     * Define uma nova macro a partir do texto de entrada.
     * @param input Texto de entrada.
     * @return Uma nova macro.
     * @throws MacroDefinitionException Ocorreu um erro na definição da nova
     * macro.
     * @throws MalformedMacroException A nova macro está mal formada
     * (provavelmente um erro sintático)
     * @throws MalformedArgumentException Um dos argumentos está mal formado
     * (provavelmente um erro sintático)
     */
    private static Macro defineNewMacro(String input)
            throws MacroDefinitionException, MalformedMacroException,
            MalformedArgumentException {

        logger.info(
                "Obtendo definição da nova macro: {}",
                input
        );
        
        // elementos do autômato adaptativo, com o indicador do estado corrente,
        // o símbolo a ser consumido e o cursor que percorre a cadeia de entrada
        int state = 1;
        char symbol;
        int cursor = 0;

        // variáveis auxiliares para compôr a nova macro
        String name = "";
        String parameter = "";
        String body = "";
        Map<Integer, String> parameters = new HashMap<>();
        int total = 0;

        // delimitadores
        char delimiter1 = '\0';
        char delimiter2 = '\0';

        // variável lógica para determinar a condição de parada do processo de
        // reconhecimento e análise da cadeia de entrada
        boolean done = false;

        // inicia o processo de reconhecimento e análise da cadeia de entrada
        while (!done && input.length() > 0) {

            // obtém o símbolo indicado pelo cursor na cadeia de entrada
            symbol = input.charAt(cursor);

            // determina a ação de acordo com o estado corrente
            switch (state) {

                case 1:

                    // verifica se o primeiro símbolo é um símbolo de início de
                    // macro
                    if (symbol == '\\') {
                    
                        logger.info(
                                "Início da macro a ser definida (posição {}).",
                                cursor
                        );
                        
                        // novo estado
                        state = 2;
                    }
                    else {
                        
                        // verifica se o símbolo é inválido no contexto
                        if (!ignore(symbol)) {
                            
                            // condição de erro, lançar exceção
                            throw new MalformedMacroException(
                                    String.format(
                                            "Era esperado o símbolo de início de macro na posição %d.",
                                            cursor
                                    )
                            );
                        }
                        
                    }
                    
                    break;

                case 2:
                    
                    if (symbol == '(') {
                        throw new MalformedMacroException(
                                String.format(
                                        "Encontrei '(' na posição %d. Ele não pode ser delimitador pois indica o início da lista de parâmetros.",
                                        cursor
                                )
                        );
                    }

                    logger.info(
                            "Encontrei o símbolo '{}' na posição {}. O delimitador da macro é \\\\{} ... {}\\.",
                            symbol,
                            cursor,
                            symbol,
                            symbol
                    );
                    
                    // o próximo símbolo é o delimitador do nome da macro
                    delimiter1 = symbol;
                    
                    // avança para o próximo estado
                    state = 3;
                    
                    break;

                case 3:

                    // se o símbolo corrente é diferente do delimitador e de
                    // abertura de parênteses, adicione-o ao nome da macro
                    if (symbol != delimiter1) {
                        if (symbol != '(') {
                            name = name + symbol;
                        }
                        else {
                            
                            // o símbolo é uma abertura de parênteses
                            logger.info(
                                    "Encontrei um início da lista de parâmetros da macro a ser definida, na posição {}.",
                                    cursor
                            );
                            
                            // avança para o novo estado
                            state = 4;
                        }
                    }
                    else {
                        
                        // o símbolo é o delimitador da macro
                        logger.info(
                                "Encontrei o delimitador '{}' da macro a ser definida, na posição {}.",
                                delimiter1,
                                cursor
                        );
                        
                        // novo estado do autômato adaptativo
                        state = 10;
                    }
                    
                    break;

                case 4:

                    // neste estado, espera-se encontrar o início de um
                    // parâmetro, que nada mais é do que uma macro simples
                    if (symbol == '\\') {
                        
                        // novo estado
                        state = 5;
                    }
                    else {
                        
                        // verifica se o símbolo é inválido no contexto
                        if (!ignore(symbol)) {
                        
                            // erro sintático, lançar exceção
                            throw new MalformedArgumentException(
                                    String.format(
                                            "Era esperado um início de macro simples na posição %d para definição do argumento.",
                                            cursor
                                    )
                            );
                        
                        }

                    }
                    
                    break;

                case 5:

                    logger.info(
                            "Encontrei o símbolo '{}'. O delimitador do parâmetro {} é \\\\{} ... {}\\.",
                            symbol,
                            (total + 1),
                            symbol,
                            symbol
                    );
                    
                    // define o novo símbolo e reinicializa a variável que
                    // conterá o valor do parâmetro
                    delimiter2 = symbol;
                    parameter = "";
                    
                    // define o novo estado
                    state = 6;
                    
                    break;

                case 6:

                    // se o símbolo corrente não for o delimitador, adicione-o
                    // ao parâmetro corrente
                    if (symbol != delimiter2) {
                        parameter = parameter + symbol;
                    } else {
                        
                        logger.info(
                                "Encontrei o delimitador '{}' na posição {}.",
                                symbol,
                                cursor
                        );
                        
                        // o delimitador do parâmetro corrente foi encontrado,
                        // avançar para o novo estado
                        state = 7;
                    }
                    
                    break;

                case 7:

                    // neste estado, espera-se encontrar o símbolo de
                    // fechamento do parâmetro.
                    if (symbol == '\\') {
                        
                        logger.info(
                                "Encontrei o parâmetro '{}' da nova macro.",
                                parameter
                        );
                        
                        // incrementa o número de parâmetros, e adiciona o nome
                        // no mapa de parâmetros, indexado por sua posição na
                        // lista de parâmetros na nova macro
                        total++;
                        parameters.put(total, parameter);
                        
                        // novo estado de destino
                        state = 8;
                        
                    }
                    else {
                        //error = true;
                        throw new MalformedArgumentException(
                                String.format(
                                        "Era esperado um delimitador de fechamento do parâmetro na posição %d.",
                                        cursor
                                )
                        );
                    }
                    
                    break;

                case 8:

                    // neste estado, verifica-se se o símbolo corrente é um
                    // separador de parâmetros ou um fechamento da lista de
                    // parâmetros
                    
                    // o símbolo é um separador de parâmetros
                    if (symbol == ',') {
                        
                        logger.info(
                                "Encontrei um separador de parâmetros ',' na posição {}.",
                                cursor
                        );
                        
                        // novo estado de destino, volta para a identificação do
                        // próximo parâmetro
                        state = 4;
                    }
                    else {
                        
                        // o símbolo é um fechamento da lista de parâmetros
                        if (symbol == ')') {
                            
                            logger.info(
                                    "Encontrei o símbolo de fechamento da lista de parâmetros na posição {}.",
                                    cursor
                            );
                            
                            // novo estado de destino, voltando para a análise
                            // do fechamento da definição da macro
                            state = 9;
                            
                        } else {
                            
                            // verifica se o símbolo é inválido no contexto
                            if (!ignore(symbol)) {
                            
                                // erro sintático, lançar exceção
                                throw new MalformedMacroException(
                                        String.format(
                                                "Era esperado o término da definição dos parâmetros da macro ou o separador de parâmetros na posição %d.",
                                                cursor
                                        )
                                );
                                
                            }
                        }
                    }
                    
                    break;

                case 9:

                    // espera-se o delimitador de término da macro
                    if (symbol == delimiter1) {
                        
                        logger.info(
                                "Encontrei o delimitador de término '{}' da macro na posição {}.",
                                delimiter1,
                                cursor
                        );
                        
                        // novo estado de destino
                        state = 10;    
                    }
                    else {
                        
                        // verifica se o símbolo é inválido no contexto
                        if (!ignore(symbol)) {
                        
                            // erro sintático, lançar exceção
                            throw new MalformedMacroException(
                                    String.format(
                                            "Era esperado o delimitador de término '%c' da nova macro na posição %d.",
                                            delimiter1,
                                            cursor
                                    )
                            );
                        
                        }
                        
                    }
                    
                    break;

                case 10:

                    // verifica se o símbolo de fechamento da macro foi
                    // atingido
                    if (symbol == '\\') {
                        
                        logger.info(
                                "Encontrei o símbolo de fechamento da nova macro na posição {}.",
                                cursor
                        );
                        
                        // novo estado de destino
                        state = 11;
                    }
                    else {
                        
                        // erro sintático, lançar exceção
                        throw new MalformedMacroException(
                                String.format(
                                        "Era esperado o símbolo de fechamento da nova macro na posição %d.",
                                        cursor
                                )
                        );
                    }
                    
                    break;

                case 11:

                    // espera-se o símbolo de atribuição
                    if (symbol == '=') {
                        
                        logger.info(
                                "Encontrei o símbolo de atribuição na posição {}.",
                                cursor
                        );
                        
                        // novo estado de destino
                        state = 12;
                    }
                    else {
                        
                        // verifica se o símbolo é inválido no contexto
                        if (!ignore(symbol)) {
                        
                            // erro sintático, lançar exceção
                            throw new MacroDefinitionException(
                                    String.format(
                                            "Era esperado o símbolo de atribuição à nova macro na posição %d.",
                                            cursor
                                    )
                            );
                            
                        }
                    }
                    
                    break;

                case 12:

                    // neste estado, espera-se encontrar o símbolo de abertura
                    // da definição do corpo da nova macro
                    if (symbol == '\\') {
                        
                        logger.info(
                                "Encontrei o símbolo de início da definição do corpo da nova macro na posição {}.",
                                cursor
                        );
                        
                        // novo estado de destino
                        state = 13;
                        
                    }
                    else {
                        
                        // verifica se o símbolo é inválido no contexto
                        if (!ignore(symbol)) {
                        
                            // erro sintático, lançar exceção
                            throw new MacroDefinitionException(
                                    String.format(
                                            "Era esperado o símbolo de início da definição do corpo da nova macro na posição %d.",
                                            cursor
                                    )
                            );
                            
                        }
                    }
                    
                    break;

                case 13:

                    logger.info(
                            "Encontrei o símbolo '{}' na posição {}. O delimitador do corpo da nova macro é \\\\{} ... {}\\.",
                            symbol,
                            cursor,
                            symbol,
                            symbol
                    );
                    
                    // neste estado, obtém-se o delimitador da definição do
                    // corpo da nova macro
                    delimiter1 = symbol;
                    
                    // novo estado de destino
                    state = 14;
                    
                    break;

                case 14:

                    // se o símbolo corrente for diferente do delimitador, copie
                    // o mesmo para a variável que representa o corpo da nova
                    // macro
                    if (symbol != delimiter1) {
                        body = body + symbol;
                    }
                    else {
                        
                        logger.info(
                                "Encontrei o delimitador '{}' do corpo da nova macro na posição {}.",
                                delimiter1,
                                cursor
                        );
                        
                        // novo estado de destino
                        state = 15;
                    }
                    break;

                case 15:

                    // espera-se o símbolo de término do corpo da definição da
                    // nova macro
                    if (symbol == '\\') {
                        
                        logger.info(
                                "Encontrei o símbolo de término do corpo da definição da nova macro na posição {}.",
                                cursor
                        );
                        
                        // novo estado
                        state = 16;
                    }
                    else {
                        
                        // erro sintático, lançar exceção
                        throw new MacroDefinitionException(
                                String.format(
                                        "Era esperado o término do delimitador do corpo da definição da nova macro na posição %d.",
                                        cursor
                                )
                        );
                    }
                    
                    break;

                case 16:

                    // verifica se existem símbolos inválidos que não possam
                    // ser ignorados no término da cadeia
                    if (!ignore(symbol)) {
                        
                        // erro sintático
                        throw new MacroDefinitionException(
                                String.format(
                                        "Existe um símbolo inválido após a definição da macro, na posição %d.",
                                        cursor
                                )
                        );
                    }
                    
                    break;

            }

            // movimenta o cursor ao longo da cadeia de entrada
            cursor++;
            
            // interrompe o reconhecimento tão logo que o cursor alcance uma
            // posição inválida da cadeia de entrada
            done = cursor >= input.length();
        }
        
        // verifica se o autômato encerrou-se em um estado de aceitação
        if (state != 16) {
        
            // erro sintático, provavelmente
            throw new MacroDefinitionException(
                    "Houve um erro na definição da nova macro, o reconhecimento encerrou-se prematuramente."
            );
        }
        else {
            
            // faz a limpeza no nome da macro
            name = sanitize(name);
            
            // define a nova macro e a retorna
            Macro macro = new Macro(name, parameters, body);
            logger.info(
                    "A nova macro foi definida: {}",
                    macro
            );
            return macro;
        }
    }
    
    /**
     * Verifica se o símbolo informado está presente no conjunto de símbolos
     * ignorados pelo autômato adaptativo.
     * @param symbol Símbolo a ser verificado.
     * @return Valor lógico indicando se o símbolo está presente no conjunto de
     * ignorados.
     */
    public static boolean ignore(char symbol) {
        return ignored.contains(symbol);
    }
    
    /**
     * Obtém o índice da condição de acordo com o valor informado.
     * @param value Valor textual.
     * @return Índice paramétrico de acordo com o valor informado.
     * @throws InvalidConditionValueException O valor informado é inválido
     */
    private static int getConditionIndex(String value)
            throws InvalidConditionValueException {
        
        // quando a condição é verdadeira, retorna-se o segundo parâmetro da
        // lista de parâmetros (bloco de "então")
        if (value.equals("true")) {
            return 2;
        }
        else {
            
            // quando a condição é verdadeira, retorna-se o terceiro parâmetro
            // da lista de parâmetros (bloco de "senão")
            if (value.equals("false")) {
                return 3;
            }
            else {
                
                // o valor informado é inválido, lançar exceção
                throw new InvalidConditionValueException(
                        "Encontrei um valor inválido para a condição a ser testada."
                );
            }
        }
    }
    
    /**
     * Realiza a limpeza do nome da macro e retorna a versão tratada.
     * @param name Nome da macro a ser limpa.
     * @return Nome da macro já tratada.
     * @throws MalformedMacroException O nome da macro não pode ser vazio.
     */
    public static String sanitize(String name) throws MalformedMacroException {
        String result = name.replaceAll("\\s+", " ").trim();
        if (result.isEmpty()) {
            throw new MalformedMacroException(
                    "O nome da macro não pode ser vazio."
            );
        }
        else {
            return result;
        }
    }

}
