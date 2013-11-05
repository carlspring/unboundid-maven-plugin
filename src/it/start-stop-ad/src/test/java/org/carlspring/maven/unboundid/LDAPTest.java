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

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;
import java.util.Hashtable;

import org.junit.Test;
import static junit.framework.Assert.assertTrue;

/**
 * @author mtodorov
 */
public class LDAPTest
{

    private InitialDirContext getContext()
            throws NamingException
    {
        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        // TODO: Make this configurable:
        env.put(Context.PROVIDER_URL, "ldap://localhost:10389/");
        // TODO: Make this configurable:
        env.put(Context.SECURITY_AUTHENTICATION, "simple");

        return new InitialDirContext(env);
    }

    @Test
    public void testFindUser()
            throws NamingException
    {
        String uid = "mtodorov";
        String password = "password";

        // User user = null;
        DirContext ctx = null;
        NamingEnumeration results = null;

        try
        {
            // Step 1: Bind anonymously
            ctx = getContext();

            // Step 2: Search the directory
            // TODO: Make this configurable:
            String dn = "dc=carlspring,dc=com";
            // TODO: Make this configurable:
            String filter = "(&(objectClass=user)(sAMAccountName={0}))";

            String[] attrIDs = new String[]{ "ou",
                                             "sAMAccountName",   // uid
                                             "cn",
                                             "displayName",      // givenName
                                             "sn",
                                             "mail" };

            SearchControls ctls = new SearchControls();
            ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            ctls.setReturningAttributes(attrIDs);
            ctls.setReturningObjFlag(true);

            results = ctx.search(dn, filter, new String[]{ uid }, ctls);

            if (results.hasMore())
            {
                SearchResult result = (SearchResult) results.next();
                dn = result.getNameInNamespace();

                System.out.println("Getting user " + uid  + " (dn: " + dn + ")...");

                Attribute attrUid = result.getAttributes().get("sAMAccountName");
                Attribute attrDisplayName = result.getAttributes().get("displayName");
                Attribute attrEmail = result.getAttributes().get("mail");

                System.out.println(" * sAMAccountName : " + attrUid.get());
                System.out.println(" * displayName    : " + attrDisplayName.get());
                System.out.println(" * mail           : " + attrEmail.get());
                System.out.println();
            }

            if (dn == null || results.hasMore())
            {
                // uid not found or not unique
                throw new NamingException("Authentication failed.");
            }

            // Step 3: Bind with found DN and given password
            // ctx.addToEnvironment(Context.SECURITY_PRINCIPAL, dn);
            // ctx.addToEnvironment(Context.SECURITY_CREDENTIALS, password);

            // Perform a lookup in order to force a bind operation with JNDI
            // ctx.lookup(dn);

            // System.out.println("Authentication successful.");
        }
        finally
        {
            if (ctx != null)
            {
                ctx.close();
            }

            if (results != null)
            {
                results.close();
            }
        }
    }

    @Test
    public void testListAllUsers()
            throws NamingException
    {
        DirContext ctx = null;
        NamingEnumeration<?> results = null;

        try
        {
            // Step 1: Bind anonymously
            ctx = getContext();

            // Step 2: Search the directory
            String[] attrIDs = new String[]{ "ou",
                                             "sAMAccountName",   // uid
                                             "cn",
                                             "displayName",      // givenName
                                             "sn",
                                             "mail" };

            SearchControls controls = new SearchControls();
            controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            controls.setReturningAttributes(attrIDs);
            controls.setReturningObjFlag(true);


            //Get entries having objectclass=person
            String filter = "(&(objectClass=user))";
            results = ctx.search("", filter, controls);

            assertTrue("There are no results!", results.hasMore());

            System.out.println("Listing users...");

            while (results.hasMore())
            {
                SearchResult searchResult = (SearchResult) results.next();
                Attributes attributes = searchResult.getAttributes();

                Attribute attrUid = attributes.get("sAMAccountName");
                // Attribute pwd = attributes.get("userPassword");

                // The password string depends on the LDAP password policy
                // System.out.println(" * " + uid.get() + ": " + new String((byte[]) pwd.get()));

                System.out.println(" * sAMAccountName : " + attrUid.get());
            }

            System.out.println();
        }
        finally
        {
            if (ctx != null)
            {
                ctx.close();
            }

            if (results != null)
            {
                results.close();
            }
        }
    }

    @Test
    public void testGetMembersOfGroup()
            throws NamingException
    {
        DirContext ctx = null;
        NamingEnumeration<?> results = null;

        try
        {
            // Step 1: Bind anonymously
            ctx = getContext();

            // Step 2: Search the directory
            // TODO: Make this configurable:

            String[] attrIDs = new String[]{ "member" };

            SearchControls controls = new SearchControls();
            controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            controls.setReturningAttributes(attrIDs);
            controls.setReturningObjFlag(true);

            String filter = "(cn=Administrators)";

            String dn = "cn=Builtin,dc=carlspring,dc=com";

            results = ctx.search(dn, filter, attrIDs, controls);

            assertTrue("There are no results!", results.hasMore());

            System.out.println("Listing members of cn=Administrators," + dn + "...");

            while (results.hasMore())
            {
                SearchResult searchResult = (SearchResult) results.next();
                Attributes attributes = searchResult.getAttributes();

                NamingEnumeration<? extends Attribute> attrs = attributes.getAll();

                while (attrs.hasMore())
                {
                    final NamingEnumeration<?> memberAttributes = attrs.next().getAll();

                    while (memberAttributes.hasMore())
                    {
                        System.out.println(" * " + memberAttributes.next());
                    }
                }
            }

            System.out.println();
        }
        finally
        {
            if (ctx != null)
            {
                ctx.close();
            }

            if (results != null)
            {
                results.close();
            }
        }
    }

    @Test
    public void testGetMembersOfGroupTransitively()
            throws NamingException
    {
        DirContext ctx = null;
        NamingEnumeration<?> results = null;

        try
        {
            // Step 1: Bind anonymously
            ctx = getContext();

            // Step 2: Search the directory
            // TODO: Make this configurable:

            String[] attrIDs = new String[]{ "member" };

            SearchControls controls = new SearchControls();
            controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            controls.setReturningAttributes(attrIDs);
            controls.setReturningObjFlag(true);

            String filter = "(cn=Administrators)";

            String dn = "cn=Builtin,dc=carlspring,dc=com";

            results = ctx.search(dn, filter, attrIDs, controls);

            assertTrue("There are no results!", results.hasMore());

            System.out.println("Listing members of cn=Administrators," + dn + "...");

            while (results.hasMore())
            {
                SearchResult searchResult = (SearchResult) results.next();
                Attributes attributes = searchResult.getAttributes();

                NamingEnumeration<? extends Attribute> attrs = attributes.getAll();

                while (attrs.hasMore())
                {
                    final NamingEnumeration<?> memberAttributes = attrs.next().getAll();

                    while (memberAttributes.hasMore())
                    {
                        System.out.println(" * " + memberAttributes.next());
                    }
                }
            }

            System.out.println();
        }
        finally
        {
            if (ctx != null)
            {
                ctx.close();
            }

            if (results != null)
            {
                results.close();
            }
        }
    }

    @Test
    public void testGetGroup()
            throws NamingException
    {
        DirContext ctx = null;
        NamingEnumeration<?> results = null;

        try
        {
            // Step 1: Bind anonymously
            ctx = getContext();

            // Step 2: Search the directory
            // TODO: Make this configurable:

            String[] attrIDs = new String[]{ "dn", "cn", "member", "memberOf", "objectClass" };

            SearchControls controls = new SearchControls();
            controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            controls.setReturningAttributes(attrIDs);
            controls.setReturningObjFlag(true);

            String filter = "(cn=Administrators)";

            String dn = "cn=Builtin,dc=carlspring,dc=com";

            results = ctx.search(dn, filter, attrIDs, controls);

            assertTrue("There are no results!", results.hasMore());

            System.out.println("Listing members of cn=Administrators," + dn + "...");

            while (results.hasMore())
            {
                SearchResult searchResult = (SearchResult) results.next();
                Attributes attributes = searchResult.getAttributes();

                NamingEnumeration<? extends Attribute> attrs = attributes.getAll();

                while (attrs.hasMore())
                {
                    // final NamingEnumeration<?> memberAttributes = attrs.next().getAll();

                    // while (memberAttributes.hasMore())
                    // {
                    //     System.out.println(" * " + memberAttributes.next());
                    // }
                    final Attribute attribute = attrs.next();
                    if (attribute.size() == 1)
                    {
                        System.out.println(" * " + attribute);
                    }
                    else
                    {
                        final NamingEnumeration<?> subAttrs = attribute.getAll();

                        String id = attribute.getID();
                        while (subAttrs.hasMore())
                        {
                            System.out.println(" * " + id + ": " + subAttrs.next());
                        }
                    }
                }
            }

            System.out.println();
        }
        finally
        {
            if (ctx != null)
            {
                ctx.close();
            }

            if (results != null)
            {
                results.close();
            }
        }
    }

    @Test
    public void testGetAllGroups()
            throws NamingException
    {
        DirContext ctx = null;
        NamingEnumeration<?> results = null;

        try
        {
            // Step 1: Bind anonymously
            ctx = getContext();

            // Step 2: Search the directory
            String[] attrIDs = new String[]{ "cn" };

            SearchControls controls = new SearchControls();
            controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            controls.setReturningAttributes(attrIDs);
            controls.setReturningObjFlag(true);

            String dn = "DC=carlspring,DC=com";

            String filter = "(objectClass=group)";
            results = ctx.search(dn, filter, controls);

            assertTrue("There are no results!", results.hasMore());

            System.out.println("Listing groups...");

            assertTrue("Could not find any groups!", results.hasMore());

            while (results.hasMore())
            {
                SearchResult searchResult = (SearchResult) results.next();
                Attributes attributes = searchResult.getAttributes();

                NamingEnumeration<? extends Attribute> attrs = attributes.getAll();

                while (attrs.hasMore())
                {
                    System.out.println(" * " + attrs.next());
                }
            }

            System.out.println();
        }
        finally
        {
            if (ctx != null)
            {
                ctx.close();
            }

            if (results != null)
            {
                results.close();
            }
        }
    }

}
