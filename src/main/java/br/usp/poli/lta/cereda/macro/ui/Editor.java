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
package br.usp.poli.lta.cereda.macro.ui;

import br.usp.poli.lta.cereda.macro.MacroExpander;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.charset.Charset;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileNameExtensionFilter;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;

/**
 * Editor embutido de macros.
 * @author Paulo Roberto Massa Cereda
 * @version 1.0
 * @since 1.0
 */
public class Editor extends JFrame {

    // atributos da classe
    private final JButton open;
    private final JButton save;
    private final JButton run;
    private final JButton clear;
    private final JFileChooser chooser;
    private final RSyntaxTextArea input;
    private final RSyntaxTextArea output;
    
    /**
     * Construtor.
     */
    public Editor() {
        
        // define as configurações de exibição
        super("Expansor de macros");
        setPreferredSize(new Dimension(550, 550));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);
        setLayout(new MigLayout());
        
        // cria os botões e suas respectivas ações
        open = new JButton("Abrir", new ImageIcon(getClass().
                getResource("/br/usp/poli/lta/cereda/macro/images/open.png")));
        save = new JButton("Salvar", new ImageIcon(getClass().
                getResource("/br/usp/poli/lta/cereda/macro/images/save.png")));
        run = new JButton("Executar", new ImageIcon(getClass().
                getResource("/br/usp/poli/lta/cereda/macro/images/play.png")));
        clear = new JButton("Limpar", new ImageIcon(getClass().
                getResource("/br/usp/poli/lta/cereda/macro/images/clear.png")));
        
        // cria uma janela de diálogo para abrir e salvar arquivos de texto
        chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(false);
        FileNameExtensionFilter filter =
                new FileNameExtensionFilter("Arquivos de texto", "txt", "text");
        chooser.setFileFilter(filter);
        
        // ação de abertura de arquivo
        open.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                int value = chooser.showOpenDialog(Editor.this);
                if (value == JFileChooser.APPROVE_OPTION) {
                    File file = chooser.getSelectedFile();
                    try {
                        String content = FileUtils.readFileToString(file);
                        input.setText(content);
                        output.setText("");
                    }
                    catch (Exception e) {}
                }
            }
        });
        
        // ação de salvamento de arquivo
        save.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                int value = chooser.showSaveDialog(Editor.this);
                if (value == JFileChooser.APPROVE_OPTION) {
                    File file = chooser.getSelectedFile();
                    try {
                        FileUtils.writeStringToFile(
                                file,
                                input.getText(),
                                Charset.forName("UTF-8")
                        );
                        output.setText("");
                    }
                    catch (Exception e) {}
                }
            }
        });
        
        // ação de limpeza da janela de saída
        clear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                output.setText("");
            }
        });
        
        // ação de execução do expansor de macros
        run.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                try {
                    output.setText(MacroExpander.parse(input.getText()));
                }
                catch (Exception exception) {
                    String out = StringUtils.
                            rightPad("ERRO: ", 50, "-").concat("\n");
                    out = out.concat(WordUtils.
                            wrap(exception.getMessage(), 50)).concat("\n");
                    out = out.concat(StringUtils.repeat(".", 50)).concat("\n");
                    output.setText(out);
                }
            }
        });
        
        // tela de entrada do texto
        input = new RSyntaxTextArea(14, 60);
        input.setCodeFoldingEnabled(true);
        input.setWrapStyleWord(true);
        input.setLineWrap(true);
        RTextScrollPane iinput = new RTextScrollPane(input);
        add(iinput, "span 4, wrap");
        
        // adiciona os botões
        add(open);
        add(save);
        add(run);
        add(clear, "wrap");
        
        // tela de saída da expansão
        output = new RSyntaxTextArea(14, 60);
        output.setEditable(false);
        output.setCodeFoldingEnabled(true);
        output.setWrapStyleWord(true);
        output.setLineWrap(true);
        RTextScrollPane ioutput = new RTextScrollPane(output);
        add(ioutput, "span 4");
        
        // ajustes finais
        pack();
        setLocationRelativeTo(null);
        
    }
    
}
