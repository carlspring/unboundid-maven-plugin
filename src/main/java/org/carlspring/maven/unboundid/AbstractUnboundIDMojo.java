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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * @author Martin Todorov (carlspring@gmail.com)
 */
public abstract class AbstractUnboundIDMojo
        extends AbstractMojo
{

    @Parameter(readonly = true, property = "project", required = true)
    public MavenProject project;

    @Parameter(property = "basedir")
    public String basedir;

    /**
     * The port to start Derby on.
     */
    @Parameter(property = "ldap.port", defaultValue = "10389")
    private int port;

    /**
     * The username to use when authenticating.
     */
    @Parameter(property = "ldap.username", defaultValue = "admin")
    private String username;

    /**
     * The password to use when authenticating.
     */
    @Parameter(property = "ldap.password", defaultValue = "password")
    private String password;

    @Parameter(property = "ldap.baseDN", required = true)
    private String baseDn;


    public MavenProject getProject()
    {
        return project;
    }

    public void setProject(MavenProject project)
    {
        this.project = project;
    }

    public int getPort()
    {
        return port;
    }

    public void setPort(int port)
    {
        this.port = port;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getBasedir()
    {
        return basedir;
    }

    public void setBasedir(String basedir)
    {
        this.basedir = basedir;
    }

    public String getBaseDn()
    {
        return baseDn;
    }

    public void setBaseDn(String baseDn)
    {
        this.baseDn = baseDn;
    }

}
