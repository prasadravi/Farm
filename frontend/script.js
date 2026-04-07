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
    const logoutBtn = byId("logoutBtn");
    const profileMenu = byId("profileMenu");
    const cartPageBtn = byId("cartPageBtn");

    if (!logoutBtn || !profileMenu || !cartPageBtn) return;

    const loggedIn = isLoggedIn();
    logoutBtn.style.display = loggedIn ? "flex" : "none";
    profileMenu.dataset.loggedIn = loggedIn ? "true" : "false";
    cartPageBtn.classList.toggle("logged-in", loggedIn);
    cartPageBtn.style.display = loggedIn ? "inline-flex" : "none";
  }

  /* ---------------------------
     Navbar scroll effect
  ----------------------------*/
  const navbar = byId("navbar");
  let lastScrollY = window.scrollY;

  function updateNavbar() {
    if (!navbar) return;
    const currentY = window.scrollY;
    const scrollingDown = currentY > lastScrollY;

    if (currentY <= 24) {
      navbar.classList.remove("scrolled", "is-visible");
    } else if (scrollingDown) {
      navbar.classList.add("scrolled", "is-visible");
    } else {
      navbar.classList.remove("is-visible");
      navbar.classList.add("scrolled");
    }

    lastScrollY = currentY;
  }

  window.addEventListener("scroll", updateNavbar, { passive: true });
  updateNavbar();

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
  const productDots = byId("productDots");
  const storePrev = byId("storePrev");
  const storeNext = byId("storeNext");

  let productCards = [];
  let productDotButtons = [];

  function renderProducts() {
    if (!productGrid) return;
    productGrid.innerHTML = products.map((p, index) => `
      <article class="store-card" data-index="${index}" style="background-image:url('${p.img}')">
        <div class="card-inner">
          <div class="card-caption">${p.title}</div>
          <div class="card-subtitle">${p.unit}</div>
          <div class="card-controls">
            <div class="price">₹${p.price}</div>
            <button class="add-btn" data-id="${p.id}">Add to Cart</button>
          </div>
        </div>
      </article>
    `).join("");

    productCards = $$("#productGrid .store-card");
    renderProductDots();
  }
  renderProducts();

  function renderProductDots() {
    if (!productDots) return;
    productDots.innerHTML = products.map((p, index) => `
      <button class="store-dot${index === 0 ? ' active' : ''}" type="button" aria-label="Show ${p.title}" data-index="${index}"></button>
    `).join("");
    productDotButtons = Array.from(productDots.querySelectorAll(".store-dot"));
  }

  function updateProductDots(activeIndex = 0) {
    productDotButtons.forEach((dot, index) => {
      dot.classList.toggle("active", index === activeIndex);
    });
  }

  function scrollProductTo(index) {
    if (!productGrid || !productCards.length) return;
    const target = Math.max(0, Math.min(index, productCards.length - 1));
    productCards[target].scrollIntoView({ behavior: "smooth", inline: "start", block: "nearest" });
    updateProductDots(target);
  }

  function activeProductIndex() {
    if (!productGrid || !productCards.length) return 0;
    const gridRect = productGrid.getBoundingClientRect();
    let closest = 0;
    let minDistance = Infinity;

    productCards.forEach((card, index) => {
      const rect = card.getBoundingClientRect();
      const distance = Math.abs(rect.left - gridRect.left);
      if (distance < minDistance) {
        minDistance = distance;
        closest = index;
      }
    });

    return closest;
  }

  productGrid?.addEventListener("scroll", () => {
    updateProductDots(activeProductIndex());
  }, { passive: true });

  productDots?.addEventListener("click", e => {
    const target = e.target.closest(".store-dot");
    if (!target) return;
    scrollProductTo(Number(target.dataset.index || 0));
  });

  storePrev?.addEventListener("click", () => {
    scrollProductTo(Math.max(0, activeProductIndex() - 1));
  });

  storeNext?.addEventListener("click", () => {
    scrollProductTo(Math.min(products.length - 1, activeProductIndex() + 1));
  });

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
     Profile menu
  ----------------------------*/
  const profileBtn = byId("profileBtn");
  const profileMenu = byId("profileMenu");

  function closeProfileMenu() {
    profileMenu?.classList.remove("open");
    profileMenu?.setAttribute("aria-hidden", "true");
    profileBtn?.setAttribute("aria-expanded", "false");
  }

  profileBtn?.addEventListener("click", (e) => {
    e.stopPropagation();
    if (!profileMenu) return;
    const willOpen = !profileMenu.classList.contains("open");
    profileMenu.classList.toggle("open", willOpen);
    profileMenu.setAttribute("aria-hidden", String(!willOpen));
    profileBtn.setAttribute("aria-expanded", String(willOpen));
  });

  document.addEventListener("click", (e) => {
    if (!profileMenu || !profileBtn) return;
    if (!profileMenu.contains(e.target) && !profileBtn.contains(e.target)) {
      closeProfileMenu();
    }
  });

  profileMenu?.addEventListener("click", (e) => {
    if ((e.target instanceof HTMLElement) && e.target.matches(".profile-action, .logout")) {
      closeProfileMenu();
    }
  });

  byId("logoutBtn")?.addEventListener("click", () => {
    localStorage.removeItem("token");
    localStorage.removeItem("cart");
    syncAuthUi();
    window.location.href = "index.html";
  });

  /* ---------------------------
     Drawer / Cart display
  ----------------------------*/
  const drawer = byId("cartDrawer");
  const drawerBackdrop = byId("drawerBackdrop");
  const openCartBtn = byId("openCart") || byId("cartPageBtn");
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

  /* ---------------------------
     Gallery arrows
  ----------------------------*/
  const galleryViewport = byId("galleryViewport");
  const galleryStrip = byId("galleryStrip");
  const gPrev = byId("gPrev");
  const gNext = byId("gNext");

  function scrollGallery(delta) {
    const viewport = galleryViewport || galleryStrip;
    if (!viewport) return;
    viewport.scrollBy({ left: delta, behavior: "smooth" });
  }

  gPrev?.addEventListener("click", () => scrollGallery(-(galleryViewport?.clientWidth || 320) * 0.85));
  gNext?.addEventListener("click", () => scrollGallery((galleryViewport?.clientWidth || 320) * 0.85));

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
  updateProductDots(0);
});
