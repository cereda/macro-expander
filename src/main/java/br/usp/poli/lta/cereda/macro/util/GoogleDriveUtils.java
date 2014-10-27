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
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;

/**
 * Implementa métodos utilitários para o Google Drive.
 *
 * @author Paulo Roberto Massa Cereda
 * @version 1.0
 * @since 1.0
 */
public class GoogleDriveUtils {

    /**
     * Insere o conteúdo informado em um novo arquivo no Google Drive.
     * @param name Nome do arquivo a ser criado.
     * @param content Conteúdo a ser inserido.
     * @throws TextRetrievalException Ocorreu um erro no serviço.
     */
    public static void insert(String name, String content)
            throws TextRetrievalException {
        
        try {
            
            // obtém o serviço e cria os metadados do arquivo
            Drive drive = GoogleDriveController.getInstance().getService();
            File metadata = new File();
            metadata.setTitle(name);
            
            // cria um arquivo temporário para fazer o upload no Google Drive
            java.io.File record = new java.io.File(
                    System.getProperty("user.home").concat(
                            java.io.File.separator
                    ).concat(name).concat("-tmp.txt"));
            
            // grava o conteúdo no arquivo temporário e adiciona-o no conteúdo
            // a ser enviado para o Google Drive
            FileUtils.writeStringToFile(
                    record,
                    content,
                    Charset.forName("UTF-8"),
                    false
            );
            FileContent media = new FileContent("text/plain", record);
            
            // cria uma operação de inserção no Google Drive e executa-a
            Drive.Files.Insert insert = drive.files().insert(metadata, media);
            MediaHttpUploader uploader = insert.getMediaHttpUploader();
            uploader.setDirectUploadEnabled(true);
            insert.execute();
            
            // remove o arquivo temporário
            record.delete();
        } catch (IOException exception) {
            
            // ocorreu um erro de conexão, lançar exceção
            throw new TextRetrievalException(
                    "Consegui me conectar ao Google Drive para inserir o novo arquivo, mas o serviço está muito instável. Não consigo prosseguir."
            );
        }
    }
    
    /**
     * Faz o download de um arquivo do Google Drive como um fluxo de bytes.
     * @param file Referência do arquivo.
     * @return Conteúdo do arquivo como um fluxo de bytes.
     * @throws TextRetrievalException Ocorreu um erro no serviço do Google
     * Drive ou o arquivo tem credenciais válidas para download.
     */
    private static InputStream download(File file)
            throws TextRetrievalException {
        
        // verifica se o arquivo informado é válido e permite download
        if (file.getDownloadUrl() != null &&
                file.getDownloadUrl().length() > 0) {
            
            try {
                
                // obtém a instância do serviço e cria uma resposta HTTP
                // contendo as informações de download
                Drive drive = GoogleDriveController.getInstance().getService();
                HttpResponse response = drive.getRequestFactory().
                        buildGetRequest(new GenericUrl(file.getDownloadUrl())).
                        execute();
                
                // retorna o conteúdo da resposta HTTP
                return response.getContent();
            }
            catch (IOException exception) {

                // ocorreu um erro de leitura, lança exceção
                throw new TextRetrievalException(
                        "Consegui conectar no Google Drive, mas não consigo fazer o download do arquivo no momento. O serviço está muito instável."
                );
            }
        }
        else {
            
            // o arquivo não possui link de download
            throw new TextRetrievalException(
                    "Não consegui retornar o arquivo. Por favor, verifique as configurações do arquivo no Google Drive."
            );
        }
    }

    /**
     * Obtém o conteúdo do arquivo informado.
     * @param name Nome do arquivo.
     * @return Conteúdo do arquivo informado.
     * @throws TextRetrievalException Ocorreu um erro no serviço do Google
     * Drive ou o arquivo não existe.
     */
    public static String get(String name) throws TextRetrievalException {
        
        try {
            
            // obtém a instância do serviço e recupera a lista de todos os
            // arquivos existentes no Google Drive
            List<File> result = new ArrayList<>();
            Drive drive = GoogleDriveController.getInstance().getService();      
            Drive.Files.List request = drive.files().list();
            
            // Obtém os metadados de todos os arquivos disponíveis e adiciona-os
            // na lista de resultados
            do {
                FileList files = request.execute();
                result.addAll(files.getItems());
                request.setPageToken(files.getNextPageToken());
            } while (request.getPageToken() != null
                    && request.getPageToken().length() > 0);
            
            // para cada referência de arquivo, verifica se este contém o título
            // do arquivo informado
            for (File file : result) {
                
                // o arquivo foi encontrado
                if (file.getTitle().equals(name)) {
                    
                    // faz o download do fluxo de bytes do arquivo e salva o
                    // conteúdo em um arquivo texto temporário
                    InputStream stream = download(file);
                    java.io.File temp = new java.io.File(
                            System.getProperty("user.home").concat(
                                    java.io.File.separator).concat(name).
                                    concat("-tmp.txt"));
                    FileUtils.copyInputStreamToFile(stream, temp);
                    
                    // faz a leitura do arquivo, salvando o conteúdo
                    String output = FileUtils.readFileToString(
                            temp,
                            Charset.forName("UTF-8")
                    );
                    
                    // remove o arquivo temporário e retorna o conteúdo
                    temp.delete();
                    return output;
                }
            }
            
            // o arquivo não foi encontrado, lançar exceção
            throw new TextRetrievalException(
                    "O arquivo não foi encontrado no Google Drive."
            );
        }
        catch (IOException exception) {
            
            // o serviço está instável, lançar exceção
            throw new TextRetrievalException(
                    "Consegui me conectar no Google Drive, mas não consigo obter a lista de arquivos. O serviço está muito instável. Não posso prosseguir."
            );
        }
    }

}
