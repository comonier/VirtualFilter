package com.comonier.virtualfilter.manager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ConfirmationManager {
    private final Map<UUID, String> pending = new HashMap<>();
    public void setPending(UUID uuid, String data) { pending.put(uuid, data); }
    public String getPending(UUID uuid) { return pending.get(uuid); }
    public void clearPending(UUID uuid) { pending.remove(uuid); }
    public boolean hasPending(UUID uuid) { return pending.containsKey(uuid); }
}
