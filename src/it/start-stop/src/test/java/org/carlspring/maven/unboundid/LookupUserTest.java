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

import com.unboundid.ldap.sdk.*;
import org.junit.Test;
import static junit.framework.Assert.assertEquals;

/**
 * @author Martin Todorov (carlspring@gmail.com)
 */
public class LookupUserTest
{

    public static final String BASE_DN = "dc=carlspring,dc=com";


    @Test
    public void testUserLookup()
            throws LDAPException
    {
        // Get a client connection to the server and use it to perform various
        // operations.
        LDAPConnection connection = new LDAPConnection("localhost", 10389, "cn=admin", "password");

        SearchResult searchResults;
        searchResults = connection.search(BASE_DN, SearchScope.SUB, "(uid=mtodorov)", "+");

        assertEquals("Failed to lookup the user!", 1, searchResults.getEntryCount());
        {
            SearchResultEntry entry = searchResults.getSearchEntries().get(0);
            connection.close();

            // Do more stuff here....
            System.out.println("# " + entry.toString());
        }
    }

}