package com.example.rccl.hellootel.service;

import com.example.rccl.hellootel.exceptions.InventoryLevelNotFound;
import com.example.rccl.hellootel.exceptions.UpstreamServiceError;
import com.example.rccl.hellootel.model.Cart;
import com.example.rccl.hellootel.model.CartItem;
import com.example.rccl.hellootel.model.request.CartRequest;
import com.example.rccl.hellootel.service.facades.InventoryFacade;
import com.example.rccl.hellootel.service.model.InventoryLevel;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {

  public final InventoryFacade inventoryFacade;

  @WithSpan
  public Mono<Cart> findById(String id) {
    if ("666".equals(id)) {
      return Mono.empty();
    }

    // Add attrobites to the current span
    Span span = Span.current();
    span.setAttribute("sevicezero.finder", "find by cartId");
    span.setAttribute("sevicezero.extraInfo", 303);


    var cart = SampleDataGenerator.simpleCart();
    cart.cartId = id;

    var requestBody = CartRequest.fromCart(cart);
    Mono<Cart> updatedCart = inventoryFacade.createCartWithInventory(requestBody);
    return updatedCart;

  }

  public Flux<Cart> findAll() {
    var allCarts = SampleDataGenerator.getCarts();
    return Flux.fromIterable(allCarts);
  }

  private void handleError(Throwable err, String id) {
    String message = err.getMessage();
    String responseBody = "";

    if (err instanceof InventoryLevelNotFound) {
      log.info("InventoryLevelNotFound found in handleError().");
      return;
    }

    throw new UpstreamServiceError();
  }
}
