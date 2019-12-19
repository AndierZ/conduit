package io.vertx.conduit.entities;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Version;
import io.vertx.core.json.JsonObject;
import org.bson.types.ObjectId;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;

@Entity
public abstract class Base implements Serializable {

    public static DateFormat DF;
    static {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        DF.setTimeZone(tz);
    }

    @Id
    private ObjectId id;

    @Version
    private long version;
    private String createUser;
    private long createdAt;
    private long updatedAt;
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

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
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
        json.put("id", id == null ? null : id.toHexString());
        json.put("createdAt", this.createdAt);
        json.put("updatedAt", this.updatedAt);
    }

    protected void fromJson(JsonObject json) {
        String id = json.getString("id");
        if (id != null) {
            this.id = new ObjectId(id);
        }
        if (json.getLong("createdAt") != null) {
            this.createdAt = json.getLong("createdAt");
        }
        if (json.getLong("updatedAt") != null) {
            this.updatedAt = json.getLong("updatedAt");
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
        return Objects.equals(id, base.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
