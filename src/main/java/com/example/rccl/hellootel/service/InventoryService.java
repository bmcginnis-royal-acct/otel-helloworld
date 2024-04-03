package com.example.rccl.hellootel.service;

import com.example.rccl.hellootel.exceptions.InventoryLevelNotFound;
import com.example.rccl.hellootel.exceptions.UpstreamServiceError;
import com.example.rccl.hellootel.model.Cart;
import com.example.rccl.hellootel.model.request.CartRequest;
import com.example.rccl.hellootel.service.facades.InventoryFacade;
import com.example.rccl.hellootel.service.model.InventoryLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

  public final InventoryFacade inventoryFacade;

  public Mono<InventoryLevel> inventoryByItemId(String id) {

    var inventory = SampleDataGenerator.pullInventory();

    if ("666".equals(id)) {
      return Mono.empty();
    }

    var inventoryCount = inventory.get(id);

    return Mono.justOrEmpty(inventoryCount);
  }

  /**
   *
   * @param cart
   * @return a Mono<Cart></Cart> with inStock fields set based on current inventory levels.
   */
  public Mono<Cart> createCartWithStockCheck(CartRequest cart) {
    var inventory = SampleDataGenerator.pullInventory();

    cart.getItems().forEach(cartItem -> {
      var inventoryLevel = inventory.get(cartItem.itemId);
      cartItem.setStockLevel(inventoryLevel.getCount());
    });

    // return mutated cart.
    return Mono.just(new Cart(cart.cartId, cart.items));
  }





}
