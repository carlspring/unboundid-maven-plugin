package org.carlspring.maven.unboundid;

import java.util.Arrays;
import java.util.List;

import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryExtendedOperationHandler;
import com.unboundid.ldap.listener.InMemoryRequestHandler;
import com.unboundid.ldap.sdk.ExtendedRequest;
import com.unboundid.ldap.sdk.ExtendedResult;
import com.unboundid.ldap.sdk.ResultCode;

/**
 * @author Neil A. Wilson
 * @author mtodorov
 */
public final class ShutDownExtendedOperationHandler
        extends InMemoryExtendedOperationHandler
{

    private volatile InMemoryDirectoryServer server;

    public ShutDownExtendedOperationHandler()
    {
        server = null;
    }

    public void setInMemoryDirectoryServer(
                                                  final InMemoryDirectoryServer server)
    {
        this.server = server;
    }

    public String getExtendedOperationHandlerName()
    {
        return "Shut Down";
    }

    public List<String> getSupportedExtendedRequestOIDs()
    {
        return Arrays.asList("1.2.3.4.5.6.7.899999999");
    }

    public ExtendedResult processExtendedOperation(
                                                          final InMemoryRequestHandler handler,
                                                          final int messageID,
                                                          final ExtendedRequest request)
    {
        if (server == null)
        {
            return new ExtendedResult(messageID, ResultCode.OTHER,
                                      "The extended operation handler does not have a " +
                                      "handle to the server to shut down.",
                                      null, null, null, null, null);
        }
        else
        {
            server.shutDown(false);
            return new ExtendedResult(messageID, ResultCode.SUCCESS,
                                      null, null, null, null, null, null);
        }
    }
}