package com.example.rccl.hellootel.service.facades;


import static org.springframework.web.reactive.function.BodyInserters.fromValue;

import com.example.rccl.hellootel.config.EnvironmentConfig;
import com.example.rccl.hellootel.exceptions.InventoryLevelNotFound;
import com.example.rccl.hellootel.model.Cart;
import com.example.rccl.hellootel.model.request.CartRequest;
import com.example.rccl.hellootel.service.model.InventoryLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import org.springframework.http.ResponseEntity;
import com.example.rccl.hellootel.common.ResponseBody;


@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryFacade {

  private static final ParameterizedTypeReference<ResponseBody<Cart>> RESPONSE_TYPE =
      new ParameterizedTypeReference<ResponseBody<Cart>>() {
      };

  private final EnvironmentConfig config;
  private final WebClient webClient;

  public Mono<Cart> createCartWithInventory(CartRequest cartRequest) {
    var base = config.inventoryBaseUrl;
    var uri = base + "/cartset";



    return webClient
        .post()
        .uri(uri)
        .body(fromValue(cartRequest))
        .exchangeToMono(clientResponse -> {
          var statusCode = clientResponse.statusCode();
          if (statusCode.is2xxSuccessful()) {
            return clientResponse
                .bodyToMono(RESPONSE_TYPE)
                .map(cartResponseBody -> {
                  return cartResponseBody.getPayload();

                });
          }

          log.error("Error fetching inventory level. XXX todo");
          return Mono.error(new InventoryLevelNotFound(cartRequest.cartId));
        });

  }

}
