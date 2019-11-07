package org.acme;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

import io.quarkus.vertx.web.Route;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

@ApplicationScoped
public class RestApplication {

  public void init(@Observes Router router) {
    // http :8080/hi?name=world
    router.get("/hi")
          .handler(rc -> rc.response().end("Hi from router"));
    // http :8080/hi?name=world key=val
    router.post("/hi")
          .handler(BodyHandler.create())
          .handler(hi("Hi, here is your body."));
    router.post("/hi/bug")
    	.consumes("application/json")
	    .handler(BodyHandler.create())
	    .handler(hi("Nohi, here is your body."));
    router.post("/hi/workaround1")
          .handler(BodyHandler.create())
          .consumes("application/json")
          .handler(hi("Nohi, here is your body."));
    router.post("/hi/workaround2")
	    .consumes("application/json")
	    .handler(new ResumeHandler(BodyHandler.create()))
	    .handler(hi("Nohi, here is your body."));
  }

  class ResumeHandler implements Handler<RoutingContext> {

	    final Handler<RoutingContext> next;

	    ResumeHandler(Handler<RoutingContext> next) {
	        this.next = next;
	    }

	    @Override
	    public void handle(RoutingContext event) {
	        //we resume the request to make up for the pause that was done in the root handler
	        //this maintains normal vert.x semantics in the handlers
	        event.request().resume();
	        next.handle(event);
	    }
	}
  
  private Handler<RoutingContext> hi(String message) {
    return  rc -> rc.response().end(message+"\n"+new String(rc.getBody().getBytes()));
  }

  // http :8080/cheers?name=world
  @Route(path = "/cheers", methods = HttpMethod.GET)
  public void greetings(RoutingContext rc) {
      String name = rc.request().getParam("name");
      rc.response().end("cheers " + Optional.ofNullable(name).orElse("world"));
  }

  // http :8080/cheers?name=world key=val
  @Route(path = "/cheers", methods = HttpMethod.POST, type = Route.HandlerType.NORMAL)
  public void greetingsBody(RoutingContext rc) {
    String body = new String(rc.getBody().getBytes());
    rc.response().end("cheers "+ Optional.ofNullable(body).orElse("nobody"));
  }

  // http :8080/nocheers?name=world key=val
  @Route(path = "/nocheers", consumes = { "application/json" }, methods = HttpMethod.POST, type = Route.HandlerType.NORMAL)
  public void greetingsNobody(RoutingContext rc) {
    String body = new String(rc.getBody().getBytes());
    rc.response().end("cheers "+ Optional.ofNullable(body).orElse("nobody"));
  }

}