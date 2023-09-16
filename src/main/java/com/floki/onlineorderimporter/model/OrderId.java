package com.floki.onlineorderimporter.model;

import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.UUID;

@Embeddable
public class OrderId implements Serializable {
    private Long id;
    private UUID uuid;

    public OrderId() {

    }
    public OrderId(Long id, UUID uuid) {
        this.id = id;
        this.uuid = uuid;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    // getters y setters
}
