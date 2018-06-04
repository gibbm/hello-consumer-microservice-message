package io.vertx.book.message;

import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.eventbus.EventBus;
import io.vertx.rxjava.core.eventbus.Message;
import rx.Single;

public class HelloConsumerMicroservice extends AbstractVerticle {

    @Override
    public void start() {
        vertx.createHttpServer()
                .requestHandler(
                        req -> {
                            EventBus bus = vertx.eventBus();
                            Single<JsonObject> obs1 = bus.<JsonObject>rxSend("hello", "vert.x").map(Message::body);
                            Single<JsonObject> obs2 = bus.<JsonObject>rxSend("hello", "Star").map(Message::body);
                            Single.zip(obs1, obs2, (o, o2) ->
                                    new JsonObject()
                                            .put("vertx", o.getString("message") + " from " + o.getString("served-by"))
                                            .put("star", o2.getString("message") + " from " + o2.getString("served-by"))
                            ).subscribe(
                                    entries -> req.response().end(entries.encodePrettily()),
                                    throwable -> {
                                        throwable.printStackTrace();
                                        req.response().setStatusCode(500).end(throwable.getMessage());
                                    }
                            );
                        })
                .listen(8082);

    }
}
