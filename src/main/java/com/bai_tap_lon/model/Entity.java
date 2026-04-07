package com.auction.onlineauctionsystem.model;
import java.io.Serializable;
public abstract class Entity implements Serializable {
    private static final long serialVersionUID = 1L; // Cần có để lưu file không bị lỗi

    protected long id; // Mã định danh duy nhất

    public Entity() {
    }

    public Entity(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
