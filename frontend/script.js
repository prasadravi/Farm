/* script.js — consolidated frontend interactions
   Replace your existing script.js with this file.
   ✅ Fixed:
   - Checkout now posts to /api/orders (correct route)
   - Added clearer console messages for order success/failure
*/

document.addEventListener("DOMContentLoaded", () => {
  /* ---------------------------
     Utilities
  ----------------------------*/
  function byId(id) { return document.getElementById(id); }
  const $ = sel => document.querySelector(sel);
  const $$ = sel => Array.from(document.querySelectorAll(sel));

  /* ---------------------------
     Auth helpers
  ----------------------------*/
  function getToken() {
    return localStorage.getItem('token') || null;
  }

  const API_BASE = (window.APP_CONFIG && window.APP_CONFIG.API_BASE)
    ? window.APP_CONFIG.API_BASE
    : "http://localhost:4000/api";

  function isLoggedIn() {
    return !!getToken();
  }

  function syncAuthUi() {
    const loginBtn = byId("loginBtn");
    const signUpBtn = byId("signUpBtn");
    const logoutBtn = byId("logoutBtn");
    const cartPageBtn = byId("cartPageBtn");

    if (!loginBtn || !signUpBtn || !logoutBtn || !cartPageBtn) return;

    const loggedIn = isLoggedIn();
    loginBtn.style.display = loggedIn ? "none" : "inline-flex";
    signUpBtn.style.display = loggedIn ? "none" : "inline-flex";
    logoutBtn.style.display = loggedIn ? "inline-flex" : "none";
    cartPageBtn.style.display = loggedIn ? "inline-flex" : "none";
  }

  /* ---------------------------
     Navbar scroll effect
  ----------------------------*/
  const navbar = byId("navbar");
  window.addEventListener("scroll", () => {
    if (!navbar) return;
    if (window.scrollY > 60) navbar.classList.add("scrolled");
    else navbar.classList.remove("scrolled");
  });

  /* ---------------------------
     Hero slideshow (3s)
  ----------------------------*/
  const slideA = byId("slideA");
  const slideB = byId("slideB");
  const heroImages = ["images/milk-bg.jpg", "images/farm-bg.jpg", "images/cow-bg.jpg"];
  (function initSlides() {
    if (!slideA || !slideB) return;
    let active = slideA, passive = slideB;
    let idx = 0;
    const setSlide = (el, src) => el.style.backgroundImage = `url('${src}')`;
    heroImages.forEach(src => { const img = new Image(); img.src = src; });
    setSlide(active, heroImages[0]);
    active.classList.add("visible");
    setSlide(passive, heroImages[1] || heroImages[0]);
    setInterval(() => {
      idx = (idx + 1) % heroImages.length;
      setSlide(passive, heroImages[idx]);
      passive.classList.add("visible");
      active.classList.remove("visible");
      [active, passive] = [passive, active];
    }, 3000);
  })();

  /* ---------------------------
     Product List
  ----------------------------*/
  const products = [
    { id: "milk500", title: "Fresh Cow Milk", price: 28, img: "images/store1.jpg", unit: "500 ml pouch" },
    { id: "curd500", title: "Farm Curd", price: 40, img: "images/store3.jpg", unit: "500 g cup" },
    { id: "cheese1", title: "Blue Cheese", price: 220, img: "images/store2.jpg", unit: "200 g wedge" }
  ];

  const productGrid = byId("productGrid");
  function renderProducts() {
    if (!productGrid) return;
    productGrid.innerHTML = products.map(p => `
      <div class="store-card" style="background-image:url('${p.img}')">
        <div class="card-inner">
          <div class="card-caption">${p.title}</div>
          <div class="card-controls">
            <div class="price">₹${p.price}</div>
            <button class="add-btn" data-id="${p.id}">Add to Cart</button>
          </div>
        </div>
      </div>
    `).join("");
  }
  renderProducts();

  /* ---------------------------
     CART + LocalStorage
  ----------------------------*/
  const CART_KEY = "cart";
  const getCart = () => JSON.parse(localStorage.getItem(CART_KEY) || "[]");
  const setCart = c => localStorage.setItem(CART_KEY, JSON.stringify(c));
  const updateCartCount = () => {
    const el = byId("cartCount");
    if (!el) return;
    const items = getCart();
    el.textContent = items.reduce((sum, i) => sum + (i.qty || 0), 0);
  };
  updateCartCount();

  function addToCart(item) {
    if (!isLoggedIn()) {
      alert("Please login first to add items to cart.");
      window.location.href = "login.html";
      return;
    }

    const cart = getCart();
    const existing = cart.find(x => x.id === item.id);
    if (existing) existing.qty += 1;
    else cart.push({ ...item, qty: 1 });
    setCart(cart);
    updateCartCount();
    renderDrawer();
    alert(`${item.title} added to cart`);
  }

  productGrid?.addEventListener("click", e => {
    const t = e.target;
    if (t.matches(".add-btn")) {
      const id = t.dataset.id;
      const prod = products.find(p => p.id === id);
      if (prod) addToCart(prod);
    }
  });

  /* ---------------------------
     Drawer / Cart display
  ----------------------------*/
  const drawer = byId("cartDrawer");
  const drawerBackdrop = byId("drawerBackdrop");
  const openCartBtn = byId("openCart");
  const closeCartDrawer = byId("closeCartDrawer");
  const drawerItems = byId("drawerItems");
  const drawerEmpty = byId("drawerEmpty");
  const drawerTotal = byId("drawerTotal");
  const drawerCheckout = byId("drawerCheckout");

  function calcTotal(items) {
    return items.reduce((sum, i) => sum + i.price * (i.qty || 0), 0);
  }

  function renderDrawer() {
    const items = getCart();
    if (!drawerItems) return;

    if (items.length === 0) {
      drawerItems.innerHTML = "<p>Your cart is empty.</p>";
      if (drawerTotal) drawerTotal.textContent = "Subtotal: ₹0";
      return;
    }

    drawerItems.innerHTML = items.map((it, idx) => `
      <div class="cart-item">
        <img src="${it.img}" alt="${it.title}">
        <div class="meta">
          <div class="title">${it.title}</div>
          <div class="price">₹${it.price} × ${it.qty}</div>
        </div>
        <div class="actions">
          <button onclick="removeItem(${idx})">Remove</button>
        </div>
      </div>
    `).join("");

    if (drawerTotal) drawerTotal.textContent = `Subtotal: ₹${calcTotal(items)}`;
  }

  window.removeItem = function (i) {
    const cart = getCart();
    cart.splice(i, 1);
    setCart(cart);
    renderDrawer();
    updateCartCount();
  };

  openCartBtn?.addEventListener("click", e => {
    e.preventDefault();
    if (!isLoggedIn()) {
      window.location.href = "login.html";
      return;
    }
    drawer.classList.add("open");
    drawerBackdrop.classList.add("show");
    renderDrawer();
  });

  const logoutBtn = byId("logoutBtn");
  logoutBtn?.addEventListener("click", () => {
    localStorage.removeItem("token");
    localStorage.removeItem("cart");
    window.location.href = "index.html";
  });
  closeCartDrawer?.addEventListener("click", () => {
    drawer.classList.remove("open");
    drawerBackdrop.classList.remove("show");
  });
  drawerBackdrop?.addEventListener("click", () => {
    drawer.classList.remove("open");
    drawerBackdrop.classList.remove("show");
  });

  /* ---------------------------
     ✅ CHECKOUT — FIXED ENDPOINT
  ----------------------------*/
  drawerCheckout?.addEventListener("click", async () => {
    try {
      const token = getToken();
      if (!token) {
        alert("Please login first.");
        window.location.href = "login.html";
        return;
      }

      const items = getCart();
      if (items.length === 0) {
        alert("Your cart is empty.");
        return;
      }

      const total = calcTotal(items);
      drawerCheckout.disabled = true;
      drawerCheckout.textContent = "Placing order...";

      // ✅ FIXED ROUTE HERE:
      const res = await fetch(`${API_BASE}/orders`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "Authorization": `Bearer ${token}`,
        },
        body: JSON.stringify({ items, total }),
      });

      const data = await res.json();

      if (res.ok) {
        alert("✅ Order placed successfully!");
        console.log("Order:", data.order);
        localStorage.removeItem("cart");
        renderDrawer();
        updateCartCount();
      } else {
        console.error("Order error:", data);
        alert(data.message || "Failed to place order.");
      }
    } catch (err) {
      console.error("Checkout error:", err);
      alert("Network or server error during checkout.");
    } finally {
      drawerCheckout.disabled = false;
      drawerCheckout.textContent = "Checkout";
    }
  });

  /* ---------------------------
     Go Top button
  ----------------------------*/
  const goTop = byId("goTop");
  if (goTop) {
    goTop.addEventListener("click", () => window.scrollTo({ top: 0, behavior: "smooth" }));
  }

  renderDrawer();
  updateCartCount();
  syncAuthUi();
});
