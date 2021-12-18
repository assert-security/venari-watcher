package com.assertsecurity.venariwatcher.ldapserver;

import java.util.UUID;

import com.assertsecurity.venariwatcher.utils.PayloadMapper;
import com.unboundid.ldap.listener.interceptor.InMemoryInterceptedSearchResult;
import com.unboundid.ldap.listener.interceptor.InMemoryOperationInterceptor;

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
        String base = result.getRequest().getBaseDN();
        try
        {
            UUID id = UUID.fromString(base);
            PayloadMapper.Set(id);
        }
        catch (IllegalArgumentException e)
        {
            e.printStackTrace();
        }
    }


}
