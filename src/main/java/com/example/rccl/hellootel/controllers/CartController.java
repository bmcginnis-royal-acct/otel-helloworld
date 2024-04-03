package com.example.rccl.hellootel.controllers;



import com.example.rccl.hellootel.common.CommonHeaders;
import com.example.rccl.hellootel.common.ResponseBody;
import com.example.rccl.hellootel.common.Retry;
import com.example.rccl.hellootel.config.EnvironmentConfig;
import com.example.rccl.hellootel.model.Cart;
import com.example.rccl.hellootel.service.CartService;


import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/cart")
public class CartController {

  private final EnvironmentConfig config;

  private final CartService cartService;

  @GetMapping("/{id}")
  public Mono<ResponseEntity<ResponseBody<Cart>>> getCart(
      @PathVariable String id, @RequestHeader HttpHeaders httpHeaders) {

    log.info("\n\n====> Handling request for Get cart by id.");

    var retries = httpHeaders.get(CommonHeaders.HDR_RETRY);
    if ((retries != null) && !retries.isEmpty()) {
      log.info("Processing retry request attempt number: {}.", retries.get(0));
    }

    Mono<Cart> result = cartService.findById(id);
    return result.map(cart -> {
      var responseBody = ResponseBody.<Cart>builder()
          .payload(cart)
          .retry(new Retry())
          .status(HttpStatus.OK.value())
          .build();
      return ResponseEntity.ok(responseBody);
    }).defaultIfEmpty(ResponseEntity.notFound().build());

  }

  @WithSpan("CartController.getAllCarts")
  @GetMapping({"", "/"} )
  public Flux<Cart> getAllCarts() {
    return cartService.findAll();
  }

  @WithSpan("getCartsByCategory")
  private Mono<Cart> getCartsByCategory(@SpanAttribute("app.carts.category") String category) {
      var span = Span.current();

       Mono<Cart> cart = cartService.findById(category)
           .doOnError(e -> {
             // Decorate the Trace span with the error and set trace status to ERROR.
             span.addEvent("Error", Attributes.of(
                 AttributeKey.stringKey("exception.message"), e.getMessage()));
             span.setStatus(StatusCode.ERROR);
           })
           .onErrorResume(e -> Mono.empty());

       // Add an attribute to the current span for num items in cart.
       cart.subscribe(theCart -> span.setAttribute("app.cart.size", theCart.items.size()));
       return cart;
  }
}
