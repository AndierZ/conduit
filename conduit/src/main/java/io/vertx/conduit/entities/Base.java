package io.vertx.conduit.entities;

import io.vertx.core.json.JsonObject;

import java.io.Serializable;

public abstract class Base implements Serializable {
    private String _id;
    private String createdBy;
    private long createdTime;
    private long updatedTime;
    private String updatedBy;
    private boolean isActive;

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
    }

    public long getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(long updatedTime) {
        this.updatedTime = updatedTime;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    protected void toJson(JsonObject json) {
        json.put("_id", _id);
    }

    protected void fromJson(JsonObject json) {
        String id = json.getString("_id");
        if (id != null) {
            _id = id;
        }
    }

}
