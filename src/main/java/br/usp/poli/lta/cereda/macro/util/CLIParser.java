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

import br.usp.poli.lta.cereda.macro.model.Pair;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Parser;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Implementa um analisador de linha de comando.
 * @author Paulo Roberto Massa Cereda
 * @version 1.0
 * @since 1.0
 */
public class CLIParser {

    // logger para gerenciamento do processo de expansão das macros
    private static final Logger logger = LogManager.getLogger(CLIParser.class);

    // vetor de argumentos de linha de comando
    private final String[] arguments;
    
    private boolean editor = false;

    /**
     * Construtor.
     * @param arguments Argumentos de linha de comando.
     */
    public CLIParser(String[] arguments) {
        this.arguments = arguments;
    }
    
    /**
     * Realiza a análise dos argumentos de linha de comando e retorna um par
     * contendo o texto a ser expandido e o arquivo de saída.
     * @return Um par contendo o texto a ser expandido e o arquivo de saída.
     * @throws IOException Um dos arquivos de entrada não existe.
     */
    public Pair<String, File> parse() throws IOException {
        
        // opção de entrada
        Option input = OptionBuilder.withLongOpt("input").
                hasArgs().withArgName("lista de arquivos").
                withDescription("arquivos de entrada").
                create("i");
        
        // opção de saída
        Option output = OptionBuilder.withLongOpt("output").
                hasArg().withArgName("arquivo").
                withDescription("arquivo de saída").create("o");
        
        // opção do editor embutido
        Option ui = OptionBuilder.withLongOpt("editor").
                withDescription("editor gráfico").create("e");
        
        Options options = new Options();
        options.addOption(input);
        options.addOption(output);
        options.addOption(ui);
        
        try {
            
            // parsing dos argumentos
            Parser parser = new BasicParser();
            CommandLine line = parser.parse(options, arguments);
            
            // verifica se é uma chamada ao editor e retorna em caso positivo
            if (line.hasOption("e")) {
                editor = true;
                return null;
            }
            
            // se não é uma chamada ao editor de macros, é necessário verificar
            // se existe um arquivo de entrada
            if (!line.hasOption("i")) {
                throw new ParseException("");
            }
            
            // existem argumentos restantes, o que representa situação de erro
            if (!line.getArgList().isEmpty()) {
                throw new ParseException("");
            }
            
            String text = "";
            File out = line.hasOption("output") ?
                    new File(line.getOptionValue("output")) : null;
            
            if (out == null) {
                logger.info(
                        "A saída será gerada no terminal."
                );
            }
            else {
                logger.info(
                        "A saída será gerada no arquivo '{}'.",
                        out.getName()
                );
            }
            
            // faz a leitura de todos os arquivos e concatena seu conteúdo em
            // uma variável
            logger.info(
                    "Iniciando a leitura dos arquivos de entrada."
            );
            String[] files = line.getOptionValues("input");
            for (String file : files) {
                logger.info(
                        "Lendo arquivo '{}'.",
                        file
                );
                text = text.concat(FileUtils.readFileToString(
                        new File(file), Charset.forName("UTF-8"))
                );
            }
            
            // retorna o par da variável contendo o texto de todos os arquivos
            // e a referência ao arquivo de saída (podendo este ser nulo)
            return new Pair<>(text, out);
            
        }
        catch (ParseException exception) {
            
            // imprime a ajuda
            HelpFormatter help = new HelpFormatter();
            help.printHelp(
                    "expander ( --editor | --input <lista de arquivos>"
                            + " [ --output <arquivo> ] )",
                    options
            );
        }

        // retorna um valor inválido indicando para não prosseguir com o
        // processo de expansão
        return null;
        
    }

    /**
     * Verifica se é uma chamada ao editor embutido.
     * @return Valor lógico que indica se é uma chamada ao editor de macros.
     */
    public boolean isEditor() {
        return editor;
    }

}
