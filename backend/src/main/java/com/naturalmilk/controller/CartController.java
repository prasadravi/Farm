package com.naturalmilk.controller;

import java.util.Collections;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.naturalmilk.model.Cart;
import com.naturalmilk.model.Cart.CartItem;
import com.naturalmilk.security.JwtTokenProvider;
import com.naturalmilk.service.CartService;

@RestController
@RequestMapping("/cart")
public class CartController {
    private final CartService cartService;
    private final JwtTokenProvider jwtTokenProvider;

    public CartController(CartService cartService, JwtTokenProvider jwtTokenProvider) {
        this.cartService = cartService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    private String extractUserId(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return null;
        }

        String token = authorizationHeader.substring(7).trim();
        if (token.isEmpty() || !jwtTokenProvider.validateToken(token)) {
            return null;
        }

        return jwtTokenProvider.getUserIdFromToken(token);
    }

    @GetMapping
    public ResponseEntity<?> getCart(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        String userId = extractUserId(authorizationHeader);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        Cart cart = cartService.getCartByUserId(userId);
        List<CartItem> items = cart != null && cart.getItems() != null ? cart.getItems() : Collections.emptyList();
        return ResponseEntity.ok(new CartResponse(items));
    }

    @PutMapping
    public ResponseEntity<?> updateCart(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestBody CartPayload payload
    ) {
        String userId = extractUserId(authorizationHeader);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        List<CartItem> items = payload != null ? payload.getItems() : Collections.emptyList();
        Cart saved = cartService.saveCart(userId, items);
        List<CartItem> responseItems = saved.getItems() != null ? saved.getItems() : Collections.emptyList();
        return ResponseEntity.ok(new CartResponse(responseItems));
    }

    public static class CartPayload {
        private List<CartItem> items;

        public List<CartItem> getItems() { return items; }
        public void setItems(List<CartItem> items) { this.items = items; }
    }

    public static class CartResponse {
        private final List<CartItem> items;

        public CartResponse(List<CartItem> items) {
            this.items = items;
        }

        public List<CartItem> getItems() { return items; }
    }
}
