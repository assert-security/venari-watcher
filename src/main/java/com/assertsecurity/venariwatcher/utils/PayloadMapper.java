package com.assertsecurity.venariwatcher.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.time.LocalDateTime;

public class PayloadMapper {
    public final static Map<UUID,LocalDateTime> payloads = new HashMap<>();
    public final static ArrayList<UUID> payloadList = new ArrayList<>();
    private final static int threshold = 30000;

    public static synchronized void Set(UUID id)
    {
        int size = payloadList.size();
        if (size >= threshold) 
        {
            ArrayList<UUID> keepList = new ArrayList<>();
            for (int i=0; i<size; i++)
            {                
                UUID current = payloadList.get(i);
                if (i < size / 3)
                {
                    payloads.remove(current);
                }
                else
                {
                    keepList.add(current);
                }
            }

            payloadList.clear();
            payloadList.addAll(keepList);
        }
        payloads.put(id, LocalDateTime.now());
    }

    public static synchronized boolean Remove(UUID id)
    {
        if (payloads.containsKey(id))
        {
            payloads.remove(id);
            return true;
        }
        return false;
    }

    public static synchronized boolean Has(UUID id)
    {
        if (payloads.containsKey(id))
        {
            return true;
        }
        return false;
    }
}