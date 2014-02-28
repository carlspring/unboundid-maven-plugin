package org.carlspring.maven.unboundid;

/**
 * Copyright 2013 Carlspring Consulting & Development Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File;
import java.security.GeneralSecurityException;

import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.listener.InMemoryListenerConfig;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.util.ssl.KeyStoreKeyManager;
import com.unboundid.util.ssl.SSLUtil;
import com.unboundid.util.ssl.TrustAllTrustManager;
import com.unboundid.util.ssl.TrustStoreTrustManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * @author Martin Todorov (carlspring@gmail.com)
 * @author Neil A. Wilson
 */
@Mojo(name = "start", requiresProject = false)
public class StartUnboundIDMojo
        extends AbstractUnboundIDMojo
{

    @Parameter
    private String[] ldifFiles;


    @Override
    public void execute()
            throws MojoExecutionException, MojoFailureException
    {
        try
        {
            final ShutDownExtendedOperationHandler shutDownHandler = new ShutDownExtendedOperationHandler();

            // Create the configuration to use for the server.
            InMemoryDirectoryServerConfig config = new InMemoryDirectoryServerConfig(getBaseDn());
            config.addAdditionalBindCredentials("cn=" + getUsername(), getPassword());
            config.setSchema(null);
            config.setListenerConfigs(new InMemoryListenerConfig(getBaseDn(), null, getPort(), null, null, null));
            config.addExtendedOperationHandler(shutDownHandler);

            System.out.println("Starting UnboundID...");

            if (useSSL())
            {
                // As explained here (by Neil Wilson from the UnboundId team):
                // http://stackoverflow.com/questions/19713967/adding-an-ssl-listener-to-unboundid

                validateAndPrintSettings();

                final SSLUtil serverSSLUtil = new SSLUtil(new KeyStoreKeyManager(getKeyStorePath(),
                                                                                 getKeyStorePassword().toCharArray(),
                                                                                 "JKS",
                                                                                 "localhost"),
                                                          new TrustStoreTrustManager(getTrustStorePath()));
                final SSLUtil clientSSLUtil = new SSLUtil(new TrustAllTrustManager());

                config.setListenerConfigs(InMemoryListenerConfig.createLDAPSConfig("LDAPS",
                                                                                   null,
                                                                                   getPortSSL(),
                                                                                   serverSSLUtil.createSSLServerSocketFactory(),
                                                                                   clientSSLUtil.createSSLSocketFactory()));
            }

            // Create the directory server instance, populate it with data from the
            // LDIF file, and start listening for client connections.
            InMemoryDirectoryServer ds = new InMemoryDirectoryServer(config);

            shutDownHandler.setInMemoryDirectoryServer(ds);

            if (ldifFiles != null)
            {
                for (String ldifFile : ldifFiles)
                {
                    System.out.println("   Importing " + ldifFile + "...");
                    ds.importFromLDIF(true, ldifFile);
                }
            }

            ds.startListening();

            System.out.println("   Accepting connections on port " + (useSSL() ? getPortSSL() : getPort()) + ".");

            // Disconnect from the server and cause the server to shut down.
            // connection.close();
            // ds.shutDown(true);
        }
        catch (LDAPException e)
        {
            throw new MojoExecutionException(e.getMessage(), e);
        }
        catch (GeneralSecurityException e)
        {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    private void validateAndPrintSettings()
            throws MojoFailureException
    {
        getLog().info("   Using SSL.");
        final File keyStoreFile = new File(getKeyStorePath());
        if (keyStoreFile.exists())
        {
            getLog().info("   Using keystore:   " + keyStoreFile.getAbsolutePath());
        }
        else
        {
            throw new MojoFailureException("Failed to locate keystore: " + keyStoreFile.getAbsoluteFile());
        }

        final File trustStoreFile = new File(getTrustStorePath());
        if (trustStoreFile.exists())
        {
            getLog().info("   Using truststore: " + trustStoreFile.getAbsolutePath());
        }
        else
        {
            throw new MojoFailureException("Failed to locate truststore: " + trustStoreFile.getAbsoluteFile());
        }
    }

    public String[] getLdifFiles()
    {
        return ldifFiles;
    }

    public void setLdifFiles(String[] ldifFiles)
    {
        this.ldifFiles = ldifFiles;
    }

}
