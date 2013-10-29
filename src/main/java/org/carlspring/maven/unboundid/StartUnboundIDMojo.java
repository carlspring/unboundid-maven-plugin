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

import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.listener.InMemoryListenerConfig;
import com.unboundid.ldap.sdk.*;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * @author Martin Todorov (carlspring@gmail.com)
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
            // Create the configuration to use for the server.
            InMemoryDirectoryServerConfig config = new InMemoryDirectoryServerConfig(getBaseDn());
            config.addAdditionalBindCredentials("cn=" + getUsername(), getPassword());
            // TODO: Maybe parameterize this:
            config.setSchema(null);
            config.setListenerConfigs(new InMemoryListenerConfig(getBaseDn(), null, getPort(), null, null, null));


            // Create the directory server instance, populate it with data from the
            // "test-data.ldif" file, and start listening for client connections.
            InMemoryDirectoryServer ds = new InMemoryDirectoryServer(config);

            if (ldifFiles != null)
            {
                for (String ldifFile : ldifFiles)
                {
                    getLog().debug("Importing " + ldifFile);
                    ds.importFromLDIF(true, ldifFile);
                }
            }

            System.out.println("Starting UnboundID...");

            ds.startListening();

            // Disconnect from the server and cause the server to shut down.
            // connection.close();
            // ds.shutDown(true);
        }
        catch (LDAPException e)
        {
            throw new MojoExecutionException(e.getMessage(), e);
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
