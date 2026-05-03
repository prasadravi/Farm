package com.naturalmilk.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.naturalmilk.model.Cart;
import com.naturalmilk.model.Cart.CartItem;
import com.naturalmilk.repository.CartRepository;

@Service
public class CartService {
    private final CartRepository cartRepository;

    public CartService(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    public Cart getCartByUserId(String userId) {
        return cartRepository.findByUserId(userId).orElse(null);
    }

    public Cart saveCart(String userId, List<CartItem> items) {
        Cart cart = cartRepository.findByUserId(userId).orElseGet(Cart::new);
        cart.setUserId(userId);
        cart.setItems(items);
        cart.setUpdatedAt(System.currentTimeMillis());
        return cartRepository.save(cart);
    }
}
