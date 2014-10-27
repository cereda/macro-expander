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

import br.usp.poli.lta.cereda.macro.model.exceptions.TextRetrievalException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import org.apache.commons.io.FileUtils;

/**
 * Implementa alguns métodos comuns.
 * @author Paulo Roberto Massa Cereda
 * @version 1.0
 * @since 1.0
 */
public class CommonUtils {
    
    /**
     * Obtém o conteúdo a partir do caminho informado.
     * @param path Caminho a ser pesquisado.
     * @return Conteúdo do caminho informado.
     * @throws TextRetrievalException Um erro ocorreu ao obter o arquivo.
     */
    public static String get(String path) throws TextRetrievalException {
        
        try {
            
            // cria uma nova URL e obtém o fluxo de bytes
            URL url = new URL(path);
            InputStream stream = url.openStream();
            
            // cria um arquivo temporário, copia o fluxo de bytes para ele, lê
            // o conteúdo e remove o arquivo temporário
            File temp = new File(
                            System.getProperty("user.home").concat(
                                    java.io.File.separator).
                                    concat("get-tmp.txt"));
            FileUtils.copyInputStreamToFile(stream, temp);
            String output = FileUtils.readFileToString(
                    temp,
                    Charset.forName("UTF-8")
            );
            temp.delete();
            
            // retorna o conteúdo
            return output;
            
        }
        catch (MalformedURLException mue) {
            
            // a URL é inválida, lançar exceção
            throw new TextRetrievalException(
                    "A URL informada é inválida."
            );
        }
        catch (IOException ioe) {
            
            // o documento não foi encontrado, lançar exceção
            throw new TextRetrievalException(
                    "O documento informado na URL não foi encontrado."
            );
        }
        
    }
    
}
