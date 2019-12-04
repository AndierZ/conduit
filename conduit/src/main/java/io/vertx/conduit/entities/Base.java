package io.vertx.conduit.entities;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Version;
import io.vertx.core.json.JsonObject;
import org.bson.types.ObjectId;

import java.io.Serializable;
import java.util.Objects;

@Entity
public abstract class Base implements Serializable {
    @Id
    private ObjectId id;

    @Version
    private long version;
    private String createUser;
    private long createTime;
    private long updateTime;
    private String updateUser;
    private boolean isRetired;

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public String getUpdateUser() {
        return updateUser;
    }

    public void setUpdateUser(String updateUser) {
        this.updateUser = updateUser;
    }

    public boolean getIsActive() {
        return isRetired;
    }

    public void setIsActive(boolean isActive) {
        this.isRetired = isActive;
    }

    protected void toJson(JsonObject json) {
        json.put("_id", id == null ? null : id.toHexString());
    }

    protected void fromJson(JsonObject json) {
        String id = json.getString("_id");
        if (id != null) {
            this.id = new ObjectId(id);
        }
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Base)) return false;
        Base base = (Base) o;
        return id.equals(base.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
