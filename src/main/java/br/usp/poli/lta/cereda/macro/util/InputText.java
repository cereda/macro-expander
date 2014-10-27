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
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import net.miginfocom.swing.MigLayout;

/**
 * Implementa a janela de edição.
 * @author Paulo Roberto Massa Cereda
 * @version 1.0
 * @since 1.0
 */
public class InputText extends JDialog {

    // par a ser retornado
    private Pair<Boolean, String> pair;
    
    // campo de texto
    private final JTextArea text;
    
    // área de rolagem
    private final JScrollPane pane;
    
    // botão de confirmação
    private final JButton ok;
    
    // botão para cancelar
    private final JButton cancel;
    
    // campo de opção para definir expansão posterior
    private final JCheckBox expand;

    /**
     * Construtor.
     * @param title Título da janela.
     * @param value Valor inicial do campo de texto.
     */
    public InputText(String title, final String value) {
        super((Frame) null, title, true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setLayout(new MigLayout());

        text = new JTextArea(20, 50);
        text.setWrapStyleWord(true);
        text.setText(value);
        text.setLineWrap(true);

        pane = new JScrollPane(text);
        add(pane, "span");

        ok = new JButton("Confirmar");
        ok.setIcon(new ImageIcon(InputText.class.getResource(
                "/br/usp/poli/lta/cereda/macro/images/confirmar.png"))
        );
        
        cancel = new JButton("Cancelar");
        cancel.setIcon(new ImageIcon(InputText.class.getResource(
                "/br/usp/poli/lta/cereda/macro/images/cancelar.png"))
        );

        add(ok);
        add(cancel);

        expand = new JCheckBox("Expandir texto");
        expand.setSelected(false);
        add(expand);

        ok.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                setVisible(false);
                dispose();
                pair = new Pair<>(expand.isSelected(), text.getText());
            }
        });

        cancel.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                setVisible(false);
                dispose();
                pair = new Pair<>(expand.isSelected(), value);
            }
        });

        pack();
        setLocationRelativeTo(null);

    }

    /**
     * Exibe a janela de exibição e retorna um par contendo as informações de
     * expansão posterior e o texto digitado.
     * @return Par contendo as informações de expansão posterior e o texto
     * digitado.
     */
    public Pair<Boolean, String> display() {
        setVisible(true);
        return pair;
    }

}
