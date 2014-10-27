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
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Properties;

/**
 * Implementa o controlador do Google Drive.
 * @author Paulo Roberto Massa Cereda
 * @version 1.0
 * @since 1.0
 */
public class GoogleDriveController {
    
    // define uma única instância do controle
    private static final GoogleDriveController instance =
            new GoogleDriveController();
    
    // serviço do Google Drive
    private Drive service;
    
    // exceção a ser lançada no escopo da instância (definida
    // para evitar tratamento no singleton)
    private static TextRetrievalException exception;
    
    /**
     * Construtor.
     */
    private GoogleDriveController() {

        // originalmente, o serviço é inválido
        service = null;
        
        // tenta obter a configuração do Google Drive a partir de um arquivo de
        // propriedades localizado no diretório de usuário
        File config = new File(System.getProperty("user.home").
                concat(File.separator).concat("me-driveconfig.properties"));
        
        // se a configuração não existe, lançar exceção
        if (!config.exists()) {
            exception = new TextRetrievalException(
                    "Não encontrei o arquivo de configuração do Google Drive ('me-driveconfig.properties') no diretório do usuário."
            );
        }
        else {
            
            // a configuração existe, carrega o mapa de chaves e valores e
            // faz a autenticação no Google Drive
            Properties properties = new Properties();
            try {
                
                // obtém o arquivo de propriedades e verifica se as chaves são
                // válidas
                properties.load(new FileReader(config));
                if (properties.containsKey("id") 
                        && properties.containsKey("secret")) {
                    
                    // cria as chaves de acesso de acordo com as informações
                    // contidas no arquivo de propriedades
                    GoogleClientSecrets secrets = new GoogleClientSecrets();
                    GoogleClientSecrets.Details details = 
                            new GoogleClientSecrets.Details();
                    details.setClientId(properties.getProperty("id"));
                    details.setClientSecret(properties.getProperty("secret"));
                    secrets.setInstalled(details);
                    
                    // cria um novo transporte HTTP e a factory do JSON para
                    // parsing da API do Google Drive
                    HttpTransport transport = GoogleNetHttpTransport.
                            newTrustedTransport();
                    JsonFactory factory = JacksonFactory.getDefaultInstance();
                    
                    // define o diretório do usuário como o local para
                    // armazenamento das credenciais de autenticação do
                    // Google Drive
                    FileDataStoreFactory store =
                            new FileDataStoreFactory(
                                    new File(System.getProperty("user.home"))
                            );
                    
                    // cria um fluxo de autorização do Google a partir de todas
                    // as informações coletadas anteriormente
                    GoogleAuthorizationCodeFlow flow =
                            new GoogleAuthorizationCodeFlow.Builder(
                                    transport,
                                    factory,
                                    secrets,
                                    Collections.singleton(
                                            DriveScopes.DRIVE_FILE
                                    )
                            ).setDataStoreFactory(store).build();
                    
                    // cria uma nova credencial a partir da autorização
                    Credential credential =
                            new AuthorizationCodeInstalledApp(
                                    flow,
                                    new LocalServerReceiver()
                            ).authorize("user");
                    
                    // cria efetivamente um novo serviço do Google Drive
                    // utilizando as informações coletadas anteriormente
                    service = new Drive.Builder(
                            transport,
                            factory,
                            credential
                    ).setApplicationName("macro-expander/1.0").build();
                }
                else {
                    // as chaves são inválidas, configura uma nova exceção
                    exception = new TextRetrievalException(
                            "O arquivo de configuração do Google Drive ('me-driveconfig.properties') não possui as chaves 'id' e 'secret'."
                    );
                }
            } catch (IOException ex) {
                // erro de leitura do arquivo de configuração
                exception = new TextRetrievalException(
                        "Não consegui ler o arquivo de configuração do Google Drive ('me-driveconfig.properties') no diretório do usuário."
                );
            } catch (GeneralSecurityException ex) {
                // erro de conexão
                exception = new TextRetrievalException(
                        "Não foi possível estabelecer uma conexão segura."
                );
            }
        }        
    }
    
    /**
     * Obtém a instância do controlador do Google Drive.
     * @return A instância do controlador do Google Drive.
     * @throws TextRetrievalException Ocorreu um erro na recuperação do texto.
     */
    public static GoogleDriveController getInstance()
            throws TextRetrievalException {
        if (exception == null) {
            return instance;
        }
        else {
            throw exception;
        }
    }

    /**
     * Obtém o serviço do Google Drive.
     * @return Serviço do Google Drive.
     */
    public Drive getService() {
        return service;
    }
    
}
