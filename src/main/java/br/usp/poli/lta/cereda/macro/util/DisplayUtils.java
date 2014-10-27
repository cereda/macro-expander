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
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Disponibiliza métodos utilitários para a exibição das janelas de edição.
 * @author Paulo Roberto Massa Cereda
 * @version 1.0
 * @since 1.0
 */
public class DisplayUtils {
    
    // logger para gerenciamento do processo de expansão das macros
    private static final Logger logger =
            LogManager.getLogger(DisplayUtils.class);
    
    /**
     * Inicializa a classe de exibição, definindo o tema das janelas.
     */
    public static void init() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (Exception exception) {
            logger.error(
                    "Ocorreu um erro: {}",
                    exception.getMessage()
            );
        }
    }
    
    /**
     * Exibe uma janela para inserção de texto, retornando o valor digitado.
     * @param title Título da janela.
     * @param text Texto inicial.
     * @return O texto digitado.
     */
    public static Pair<Boolean, String> getInputText(String title,
            String text) {
        InputText input = new InputText(title, text);
        return input.display();
    }
    
    /**
     * Exibe mensagem na tela.
     * @param title Título da janela.
     * @param text Texto da mensagem.
     */
    public static void showMessage(String title, String text) {
        JOptionPane.showMessageDialog(null, WordUtils.wrap(text, 70),
                title, JOptionPane.INFORMATION_MESSAGE);
    }
    
}
