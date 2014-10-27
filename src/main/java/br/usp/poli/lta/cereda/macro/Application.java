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

import br.usp.poli.lta.cereda.macro.model.Pair;
import br.usp.poli.lta.cereda.macro.util.CLIParser;
import java.io.File;
import java.nio.charset.Charset;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

/**
 * Classe principal do programa de linha de comando.
 * @author Paulo Roberto Massa Cereda
 * @version 1.0
 * @since 1.0
 */
public class Application {
    
    /**
     * Método principal.
     * @param args Argumentos de linha de comando.
     */
    public static void main(String[] args) {
        
        // imprime banner
        System.out.println(
                StringUtils.repeat("-", 50)
        );
        System.out.println(
                StringUtils.center("Expansor de macros", 50)
        );
        System.out.println(
                StringUtils.repeat("-", 50)
        );
        System.out.println(
                StringUtils.center(
                        "Laboratório de linguagens e técnicas adaptativas",
                        50
                )
        );
        System.out.println(
                StringUtils.center(
                        "Escola Politécnica - Universidade de São Paulo",
                        50
                )
        );
        System.out.println();
        
        try {
            
            // faz o parsing dos argumentos de linha de comando
            CLIParser parser = new CLIParser(args);
            Pair<String, File> pair = parser.parse();
            
            // se o par não é nulo, é possível prosseguir com a expansão
            if (pair != null) {
                
                // obtém a expansão do texto fornecido na entrada
                String output = MacroExpander.parse(pair.getFirst());
                
                // se foi definido um arquivo de saída, grava a expansão do
                // texto nele, ou imprime o resultado no terminal, caso
                // contrário
                if (pair.getSecond() != null) {
                    FileUtils.writeStringToFile(
                            pair.getSecond(),
                            output,
                            Charset.forName("UTF-8")
                    );
                    System.out.println("Arquivo gerado com sucesso.");
                }
                else {
                    System.out.println(output);
                }
                
            }
        }
        catch (Exception exception) {
            
            // ocorreu uma exceção, imprime a mensagem de erro
            System.out.println(StringUtils.rightPad("ERRO: ", 50, "-"));
            System.out.println(WordUtils.wrap(exception.getMessage(), 50));
            System.out.println(StringUtils.repeat(".", 50));
        }
    }
    
    // that's all, folks
    
}
