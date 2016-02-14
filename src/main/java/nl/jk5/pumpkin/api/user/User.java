package nl.jk5.pumpkin.api.user;

import java.util.UUID;

public interface User {

    int getId();

    String getUsername();

    String getFullName();

    String getEmail();

    UUID getOfflineMojangId();

    UUID getOnlineMojangId();

    void setOnlineMojangId(UUID uuid);

    void setOfflineMojangId(UUID uuid);

    String getPasswordHash();
}
