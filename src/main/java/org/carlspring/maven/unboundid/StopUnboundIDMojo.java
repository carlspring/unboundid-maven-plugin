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

import java.security.GeneralSecurityException;

import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.util.ssl.SSLUtil;
import com.unboundid.util.ssl.TrustAllTrustManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * @author Martin Todorov (carlspring@gmail.com)
 * @author Neil A. Wilson
 */
@Mojo(name = "stop", requiresProject = false)
public class StopUnboundIDMojo
        extends AbstractUnboundIDMojo
{

    /**
     * Whether to fail, if LittleProxy is not running.
     */
    @Parameter(property = "proxy.fail.if.not.running", defaultValue = "true")
    boolean failIfNotRunning;


    public void execute()
            throws MojoExecutionException, MojoFailureException
    {
        try
        {
            System.out.println("Stopping the UnboundID server on port " + (useSSL() ? getPortSSL() : getPort()) + "... ");

            LDAPConnection connection;
            if (useSSL())
            {
                final SSLUtil sslUtil = new SSLUtil(new TrustAllTrustManager());
                connection = new LDAPConnection(sslUtil.createSSLSocketFactory(), "localhost", getPortSSL());
            }
            else
            {
                connection = new LDAPConnection("localhost", getPort());
            }

            connection.processExtendedOperation("1.2.3.4.5.6.7.899999999");
            connection.close();

            System.out.println("UnboundID service stopped.");
        }
        catch (LDAPException e)
        {
            if (failIfNotRunning && !e.getMessage().contains("Connection refused"))
            {
                throw new MojoExecutionException(e.getMessage(), e);
            }
            else
            {
                getLog().warn("Nothing to shut down, as the LittleProxy service was not running.");
            }
        }
        catch (GeneralSecurityException e)
        {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    public boolean isFailIfNotRunning()
    {
        return failIfNotRunning;
    }

    public void setFailIfNotRunning(boolean failIfNotRunning)
    {
        this.failIfNotRunning = failIfNotRunning;
    }

}
