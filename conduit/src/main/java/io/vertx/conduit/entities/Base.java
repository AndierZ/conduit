package io.vertx.conduit.entities;

import io.vertx.core.json.JsonObject;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

public abstract class Base implements Serializable {
    private String _id;
    private String createdBy;
    private Date createdDate;
    private Date updatedDate;
    private String updatedBy;
    private Boolean isActive;

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

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Date updatedDate) {
        this.updatedDate = updatedDate;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Base base = (Base) o;
        return _id.equals(base._id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_id);
    }

    protected JsonObject toBaseJson(Base base, JsonObject json) {
        if (base.getCreatedBy() != null) {
            json.put("createdBy", base.getCreatedBy());
        }
        if (base.getUpdatedBy() != null) {
            json.put("updatedBy", base.getUpdatedBy());
        }
        if (base.get_id() != null) {
            json.put("_id", base.get_id());
        }
        return json;
    }

    public void fromBaseJson(JsonObject json, Base obj) {
        final String createdBy = json.getString("createdBy");
        if (createdBy != null) {
            obj.setCreatedBy(createdBy);
        }
        final String updatedBy = json.getString("updatedBy");
        if (updatedBy != null) {
            obj.setUpdatedBy(updatedBy);
        }

        final String id = json.getString("_id");
        if (id != null) {
            this.set_id(id);
        }
    }
}
