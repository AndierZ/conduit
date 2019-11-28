package io.vertx.conduit.entities;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

@DataObject(generateConverter = true)
public class Comment extends Base {

    public Comment() {}

    public Comment(JsonObject jsonObject) {}
}
