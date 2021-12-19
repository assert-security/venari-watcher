package com.assertsecurity.venariwatcher.ldapserver;

import java.util.UUID;

import com.assertsecurity.venariwatcher.utils.DateTimeUtils;
import com.assertsecurity.venariwatcher.utils.PayloadMapper;
import com.unboundid.ldap.listener.interceptor.InMemoryInterceptedSearchResult;
import com.unboundid.ldap.listener.interceptor.InMemoryOperationInterceptor;
import com.unboundid.ldap.sdk.ReadOnlySearchRequest;

public class OperationInterceptor extends InMemoryOperationInterceptor {


    /**
     *
     */
    public OperationInterceptor() {
    }

    /**
     * {@inheritDoc}
     *
     * @see com.unboundid.ldap.listener.interceptor.InMemoryOperationInterceptor#processSearchResult(com.unboundid.ldap.listener.interceptor.InMemoryInterceptedSearchResult)
     */
    @Override
    public void processSearchResult(InMemoryInterceptedSearchResult result) {
        ReadOnlySearchRequest request = result.getRequest();
        String base = request.getBaseDN();
        try
        {
            UUID id = UUID.fromString(base);
            PayloadMapper.Set(id);
            System.out.println(DateTimeUtils.getDateTimeString() + " [LDAP Server]  >> Payload received " + id.toString());
        }
        catch (IllegalArgumentException e)
        {
            e.printStackTrace();
        }
    }


}
