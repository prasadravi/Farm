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
  let toastTimer = null;

  async function fetchWithTimeout(url, options = {}, timeoutMs = 8000) {
    const controller = new AbortController();
    const timeoutId = window.setTimeout(() => controller.abort(), timeoutMs);
    try {
      return await fetch(url, { ...options, signal: controller.signal });
    } finally {
      window.clearTimeout(timeoutId);
    }
  }

  function showToast(message) {
    let toast = byId("appToast");
    if (!toast) {
      toast = document.createElement("div");
      toast.id = "appToast";
      toast.className = "toast";
      document.body.appendChild(toast);
    }

    toast.textContent = message;
    toast.classList.add("show");

    if (toastTimer) {
      window.clearTimeout(toastTimer);
    }

    toastTimer = window.setTimeout(() => {
      toast.classList.remove("show");
    }, 1300);
  }

  const postOrderToast = localStorage.getItem("post_order_toast");
  if (postOrderToast) {
    showToast(postOrderToast);
    localStorage.removeItem("post_order_toast");
  }

  /* ---------------------------
     Auth helpers
  ----------------------------*/
  function getToken() {
    return localStorage.getItem('token') || null;
  }

  function parseJwtPayload(token) {
    try {
      const parts = token.split(".");
      if (parts.length !== 3) return null;
      const normalized = parts[1].replace(/-/g, "+").replace(/_/g, "/");
      const json = atob(normalized.padEnd(Math.ceil(normalized.length / 4) * 4, "="));
      return JSON.parse(json);
    } catch (_err) {
      return null;
    }
  }

  const API_BASE = (window.APP_CONFIG && window.APP_CONFIG.API_BASE)
    ? window.APP_CONFIG.API_BASE
    : "http://localhost:4000/api";

  // --- KEEP-ALIVE PING (prevents backend cold start) ---
  setInterval(() => {
    fetch(`${API_BASE}/health`).catch(() => {});
  }, 180000); // every 3 minutes

  function isLoggedIn() {
    const token = getToken();
    if (!token) return false;
    const payload = parseJwtPayload(token);
    if (!payload) return false;
    if (payload.exp && (payload.exp * 1000) <= Date.now()) return false;
    return true;
  }

  let serverSessionValid = null;

  async function validateSessionWithServer(force = false) {
    if (!isLoggedIn()) return false;
    if (!force && serverSessionValid === true) return true;

    // Keep cart/actions responsive: use local JWT checks by default and only force server
    // validation when needed (checkout or explicit verification).
    if (!force) {
      serverSessionValid = true;
      return true;
    }

    const token = getToken();
    if (!token) return false;

    try {
      const res = await fetchWithTimeout(`${API_BASE}/auth/me`, {
        headers: { "Authorization": `Bearer ${token}` }
      }, 7000);
      if (!res.ok) throw new Error("Unauthorized");
      serverSessionValid = true;
      return true;
    } catch (_err) {
      serverSessionValid = null;

      // Clear auth only for explicit unauthorized responses, not transient timeout/network.
      if (_err && (_err.message === "Unauthorized")) {
        localStorage.removeItem("token");
        localStorage.removeItem(CART_KEY);
        return false;
      }

      return true;
    }
  }

  function enforceAuthState() {
    if (!isLoggedIn()) {
      localStorage.removeItem("token");
      localStorage.removeItem(CART_KEY);
    }
  }

  function syncAuthUi() {
    const cartPageBtn = byId("cartPageBtn");
    const loginLink = document.querySelector(".nav-login-link");
    const signupLink = document.querySelector(".nav-signup-link");
    const profileLink = document.querySelector(".nav-profile-link");
    const logoutBtn = byId("logoutBtn");

    const loggedIn = isLoggedIn();

    if (loginLink) loginLink.style.display = loggedIn ? "none" : "inline-flex";
    if (signupLink) signupLink.style.display = loggedIn ? "none" : "inline-flex";
    if (profileLink) profileLink.style.display = loggedIn ? "inline-flex" : "none";
    if (logoutBtn) logoutBtn.style.display = loggedIn ? "inline-flex" : "none";

    if (!cartPageBtn) return;

    enforceAuthState();

    cartPageBtn.classList.toggle("logged-in", loggedIn);
    cartPageBtn.style.display = "inline-flex";
  }

  /* ---------------------------
     Navbar scroll effect
  ----------------------------*/
  const navbar = byId("navbar");
  const mobileQuery = window.matchMedia("(max-width: 768px)");
  const navToggle = byId("navToggle");
  const navLinks = $$(".nav-left a, .nav-right a, .nav-right button");
  let lastScrollY = window.scrollY;
  let ticking = false;
  let navIntent = "show";
  let applyTimer = null;

  function setMenuState(open) {
    if (!navbar || !navToggle) return;
    navbar.classList.toggle("menu-open", open);
    navToggle.setAttribute("aria-expanded", String(open));
    if (open) {
      navbar.classList.add("mobile-navbar-visible");
      navbar.classList.remove("mobile-navbar-hidden");
    }
  }

  navToggle?.addEventListener("click", () => {
    if (!mobileQuery.matches) return;
    setMenuState(!navbar.classList.contains("menu-open"));
  });

  navLinks.forEach((el) => {
    el.addEventListener("click", () => {
      if (mobileQuery.matches) setMenuState(false);
    });
  });

  function applyNavState(intent) {
    if (!navbar) return;
    if (applyTimer) window.clearTimeout(applyTimer);
    applyTimer = window.setTimeout(() => {
      if (intent === "hide") {
        navbar.classList.add("mobile-navbar-hidden");
        navbar.classList.remove("mobile-navbar-visible");
      } else {
        navbar.classList.add("mobile-navbar-visible");
        navbar.classList.remove("mobile-navbar-hidden");
      }
    }, 70);
  }

  function updateNavbar() {
    if (!navbar) return;
    const currentY = window.scrollY;
    const isMobile = mobileQuery.matches;
    const menuOpen = navbar.classList.contains("menu-open");

    if (!isMobile) {
      setMenuState(false);
      navbar.classList.remove("mobile-navbar-hidden", "mobile-navbar-visible");
      if (currentY > 40) navbar.classList.add("scrolled");
      else navbar.classList.remove("scrolled");
      lastScrollY = currentY;
      return;
    }

    if (menuOpen) {
      navbar.classList.add("scrolled", "mobile-navbar-visible");
      navbar.classList.remove("mobile-navbar-hidden");
      lastScrollY = currentY;
      return;
    }

    const delta = currentY - lastScrollY;
    const absDelta = Math.abs(delta);

    navbar.classList.add("scrolled");

    if (currentY <= 8) {
      navIntent = "show";
      applyNavState("show");
    } else if (absDelta >= 8) {
      if (delta > 0 && currentY > 72 && navIntent !== "hide") {
        navIntent = "hide";
        applyNavState("hide");
      } else if (delta < 0 && navIntent !== "show") {
        navIntent = "show";
        applyNavState("show");
      }
    }

    lastScrollY = currentY;
  }

  function onScroll() {
    if (ticking) return;
    ticking = true;
    window.requestAnimationFrame(() => {
      updateNavbar();
      ticking = false;
    });
  }

  window.addEventListener("scroll", onScroll, { passive: true });
  mobileQuery.addEventListener("change", updateNavbar);
  updateNavbar();

  /* ---------------------------
     Hero slideshow (3s)
  ----------------------------*/
  const slideA = byId("slideA");
  const slideB = byId("slideB");
  const heroImages = ["images/milk-bg.webp", "images/farm-bg.webp", "images/cow-bg.webp"];
  (function initSlides() {
    if (!slideA || !slideB) return;
    let active = slideA;
    let passive = slideB;
    let idx = 0;
    let slideTimerId = null;
    const prefersReducedMotion = window.matchMedia("(prefers-reduced-motion: reduce)").matches;
    const setSlide = (el, src) => el.style.backgroundImage = `url('${src}')`;
    const preloadImage = (src) => {
      const img = new Image();
      img.decoding = "async";
      img.src = src;
    };

    setSlide(active, heroImages[0]);
    active.classList.add("visible");
    setSlide(passive, heroImages[0]);

    if ("requestIdleCallback" in window && heroImages[1]) {
      window.requestIdleCallback(() => preloadImage(heroImages[1]), { timeout: 1000 });
    }

    if (prefersReducedMotion || heroImages.length < 2) return;

    const rotateSlides = () => {
      idx = (idx + 1) % heroImages.length;
      setSlide(passive, heroImages[idx]);
      passive.classList.add("visible");
      active.classList.remove("visible");
      [active, passive] = [passive, active];
    };

    const startSlides = () => {
      if (slideTimerId !== null) return;
      slideTimerId = window.setInterval(rotateSlides, 5000);
    };

    const stopSlides = () => {
      if (slideTimerId === null) return;
      window.clearInterval(slideTimerId);
      slideTimerId = null;
    };

    document.addEventListener("visibilitychange", () => {
      if (document.hidden) stopSlides();
      else startSlides();
    });

    startSlides();
  })();

  /* ---------------------------
     Product List
  ----------------------------*/
  // Add product IDs here to mark variants out of stock, e.g. ["buffalomilk500", "cowcurd250"]
  const OUT_OF_STOCK_IDS = [];

  const outOfStockLookup = new Set(
    OUT_OF_STOCK_IDS.map((id) => String(id).trim().toLowerCase())
  );

  let baseProducts = [
    { key: "cowmilk", title: "Fresh Cow Milk", price500: 28, kind: "liquid", pack: "pouch", img: "images/stor-one.jpg" },
    { key: "cowcurd", title: "Cow Curd", price500: 110, kind: "solid", pack: "cup", img: "images/cow-curd.jpg" },
    { key: "buffalocurd", title: "Bufflo Curd", price500: 155, kind: "solid", pack: "cup", img: "images/buffalo-curd.jpg" },
    { key: "buffalomilk", title: "Bufflo Milk", price500: 110, kind: "liquid", pack: "pouch", img: "images/store-four.jpg" }
  ];

  const sizeOptions = [
    { value: 250, factor: 0.5 },
    { value: 500, factor: 1 }
  ];

  function getUnitLabel(base, sizeValue) {
    if (base.kind === "solid") {
      if (sizeValue === 250) return "0.25 kg";
      return "0.5 kg";
    }

    return `${sizeValue}ml`;
  }

  function getSizeBadgeText(unitText) {
    const text = String(unitText || "");
    if (text.includes("kg")) {
      return text.replace(/\s+/g, " ").toUpperCase();
    }
    const match = text.match(/\d+\s*ml/i);
    return match ? match[0].replace(/\s+/g, " ").toUpperCase() : text.toUpperCase();
  }

  let selectedSizeByBase = {};
  let productVariantsByBase = {};
  let productById = new Map();

  function slugify(value) {
    return String(value || "")
      .toLowerCase()
      .replace(/[^a-z0-9]+/g, "-")
      .replace(/^-+|-+$/g, "");
  }

  function inferKind(category, name) {
    const haystack = `${category || ""} ${name || ""}`.toLowerCase();
    if (haystack.includes("curd") || haystack.includes("paneer") || haystack.includes("cheese")) {
      return "solid";
    }
    return "liquid";
  }

  function normalizeApiProducts(items) {
    if (!Array.isArray(items)) return [];
    return items.map((item) => {
      const key = item.id || slugify(item.name);
      return {
        key: String(key || slugify(item.name || "product")),
        title: item.name || "Product",
        price500: Number(item.price || 0),
        kind: inferKind(item.category, item.name),
        pack: "pack",
        img: item.imageUrl || "images/stor-one.jpg",
        quantity: Number(item.quantity || 0)
      };
    });
  }

  function buildProductState(products) {
    baseProducts = products;
    selectedSizeByBase = Object.fromEntries(baseProducts.map((b) => [b.key, 500]));

    productVariantsByBase = Object.fromEntries(baseProducts.map((base) => {
      const isInStock = base.quantity == null ? true : base.quantity > 0;
      const variants = sizeOptions.map((size) => {
        const unit = getUnitLabel(base, size.value);
        return {
          id: `${base.key}${size.value}`,
          baseKey: base.key,
          title: `${base.title} (${unit})`,
          baseTitle: base.title,
          sizeValue: size.value,
          unit,
          price: Math.round(base.price500 * size.factor),
          inStock: isInStock && !outOfStockLookup.has(`${base.key}${size.value}`.toLowerCase())
        };
      });

      return [base.key, variants];
    }));

    productById = new Map(
      Object.values(productVariantsByBase)
        .flat()
        .map((item) => [item.id, item])
    );
  }

  buildProductState(baseProducts);

  const productGrid = byId("productGrid");
  const productDots = byId("productDots");
  const storePrev = byId("storePrev");
  const storeNext = byId("storeNext");

  let productCards = [];
  let productDotButtons = [];

  const lazyBgObserver = ("IntersectionObserver" in window)
    ? new IntersectionObserver((entries, observer) => {
      entries.forEach((entry) => {
        if (!entry.isIntersecting) return;
        const el = entry.target;
        const src = el.getAttribute("data-bg");
        if (src) {
          el.style.backgroundImage = `url('${src}')`;
          el.removeAttribute("data-bg");
        }
        observer.unobserve(el);
      });
    }, { rootMargin: "240px 0px" })
    : null;

  function hydrateLazyBackgrounds(scope = document) {
    const nodes = Array.from(scope.querySelectorAll("[data-bg]"));
    if (!nodes.length) return;

    if (!lazyBgObserver) {
      nodes.forEach((el) => {
        const src = el.getAttribute("data-bg");
        if (!src) return;
        el.style.backgroundImage = `url('${src}')`;
        el.removeAttribute("data-bg");
      });
      return;
    }

    nodes.forEach((el) => lazyBgObserver.observe(el));
  }

  function renderProducts() {
    if (!productGrid) return;

    productGrid.innerHTML = baseProducts.map((base, index) => {
      const selectedSize = selectedSizeByBase[base.key] || 500;
      const variants = productVariantsByBase[base.key] || [];
      const selectedVariant = variants.find((v) => v.sizeValue === selectedSize) || variants[0];
      const qty = getProductQty(selectedVariant?.id || "");

      return `
      <article class="store-card" data-index="${index}" data-base-key="${base.key}">
        <div class="card-inner">
          <div class="card-thumb">
            <img src="${base.img}" alt="${base.title}" loading="lazy" decoding="async" />
          </div>
          <div class="card-body">
            <div class="card-caption">${base.title}</div>
            <div class="size-options">
              ${variants.map((v) => `
                <button
                  class="size-option-btn${v.sizeValue === selectedVariant.sizeValue ? " active" : ""}"
                  type="button"
                  data-action="select-size"
                  data-base="${base.key}"
                  data-size="${v.sizeValue}"
                >${v.unit}</button>
              `).join("")}
            </div>
            <div class="card-controls">
              <div class="price">₹${selectedVariant.price}</div>
              ${renderProductControl(selectedVariant.id, selectedVariant.title, qty, selectedVariant.inStock)}
            </div>
          </div>
        </div>
      </article>
    `;
    }).join("");

    productCards = $$("#productGrid .store-card");
    renderProductDots();
  }

  function renderProductDots() {
    if (!productDots) return;
    productDots.innerHTML = baseProducts.map((p, index) => `
      <button class="store-dot${index === 0 ? ' active' : ''}" type="button" aria-label="Show ${p.title}" data-index="${index}"></button>
    `).join("");
    productDotButtons = Array.from(productDots.querySelectorAll(".store-dot"));
  }

  async function loadProductsFromApi() {
    if (!productGrid) return;
    try {
      const res = await fetchWithTimeout(`${API_BASE}/products`, {}, 7000);
      if (!res.ok) return;
      const data = await res.json();
      const normalized = normalizeApiProducts(data);
      if (!normalized.length) return;
      buildProductState(normalized);
      renderProducts();
      refreshProductCardControls();
      updateProductDots(0);
    } catch (_err) {
      // Ignore fetch errors and keep static products.
    }
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

  let productScrollTicking = false;
  productGrid?.addEventListener("scroll", () => {
    if (productScrollTicking) return;
    productScrollTicking = true;
    window.requestAnimationFrame(() => {
      updateProductDots(activeProductIndex());
      productScrollTicking = false;
    });
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
    scrollProductTo(Math.min(productCards.length - 1, activeProductIndex() + 1));
  });

  /* ---------------------------
     CART + LocalStorage
  ----------------------------*/
  const CART_KEY = "cart";
  const CART_SYNC_DELAY = 800;

  const getCart = () => {
    try {
      return JSON.parse(localStorage.getItem(CART_KEY) || "[]");
    } catch (_err) {
      return [];
    }
  };

  const saveCartLocal = (cart) => localStorage.setItem(CART_KEY, JSON.stringify(cart));

  let cartSyncTimer = null;
  let cartSyncInitialized = false;

  function mergeCartItems(localItems, serverItems) {
    const merged = new Map();
    const addItems = (items) => {
      (items || []).forEach((item) => {
        if (!item || !item.id) return;
        const qty = Number(item.qty || 0);
        if (qty <= 0) return;
        if (merged.has(item.id)) {
          const existing = merged.get(item.id);
          existing.qty = Number(existing.qty || 0) + qty;
          merged.set(item.id, existing);
        } else {
          merged.set(item.id, { ...item, qty });
        }
      });
    };

    addItems(serverItems);
    addItems(localItems);
    return Array.from(merged.values());
  }

  async function pushCartToServer(cart) {
    if (!isLoggedIn()) return;
    const token = getToken();
    if (!token) return;
    try {
      await fetchWithTimeout(`${API_BASE}/cart`, {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
          "Authorization": `Bearer ${token}`
        },
        body: JSON.stringify({ items: cart })
      }, 7000);
    } catch (_err) {
      // ignore sync errors
    }
  }

  function scheduleCartSync(cart) {
    if (!isLoggedIn()) return;
    if (cartSyncTimer) window.clearTimeout(cartSyncTimer);
    cartSyncTimer = window.setTimeout(() => {
      pushCartToServer(cart);
    }, CART_SYNC_DELAY);
  }

  function setCart(cart, options = {}) {
    saveCartLocal(cart);
    if (!options.skipSync) scheduleCartSync(cart);
  }

  async function loadCartFromServer() {
    const token = getToken();
    if (!token) return;

    try {
      const res = await fetchWithTimeout(`${API_BASE}/cart`, {
        headers: { "Authorization": `Bearer ${token}` }
      }, 7000);
      if (!res.ok) return;
      const payload = await res.json();
      const serverItems = Array.isArray(payload?.items)
        ? payload.items
        : (Array.isArray(payload) ? payload : []);
      const merged = mergeCartItems(getCart(), serverItems);
      setCart(merged, { skipSync: true });
      updateCartCount();
      renderDrawer();
      refreshProductCardControls();
      await pushCartToServer(merged);
    } catch (_err) {
      // ignore load errors
    }
  }

  async function initCartSync() {
    if (cartSyncInitialized) return;
    cartSyncInitialized = true;
    if (!isLoggedIn()) return;
    const ok = await validateSessionWithServer(true);
    if (!ok) return;
    await loadCartFromServer();
  }
  const logoutBtn = byId("logoutBtn");

  function getProductQty(productId) {
    const found = getCart().find(item => item.id === productId);
    return found ? Number(found.qty || 0) : 0;
  }

  function renderProductControl(productId, title, qty, inStock = true) {
    if (!inStock) {
      return `<button class="add-btn out-of-stock" type="button" disabled aria-label="${title} out of stock">Out of Stock</button>`;
    }

    if (qty > 0) {
      return `
        <div class="store-qty-pill" data-id="${productId}">
          <button class="store-qty-btn qty-dec" type="button" data-action="decrease" data-id="${productId}" aria-label="Decrease ${title}">−</button>
          <span class="store-qty-count">${qty}</span>
          <button class="store-qty-btn qty-inc" type="button" data-action="increase" data-id="${productId}" aria-label="Increase ${title}">+</button>
        </div>
      `;
    }
    return `<button class="add-btn" type="button" data-action="add" data-id="${productId}" aria-label="Add ${title}">ADD</button>`;
  }

  function refreshProductCardControls() {
    if (!productGrid) return;
    const cards = Array.from(productGrid.querySelectorAll(".store-card[data-base-key]"));
    cards.forEach(card => {
      const baseKey = card.dataset.baseKey;
      const selectedSize = selectedSizeByBase[baseKey] || 500;
      const variants = productVariantsByBase[baseKey] || [];
      const product = variants.find((v) => v.sizeValue === selectedSize) || variants[0];
      const controls = card.querySelector(".card-controls");
      if (!product || !controls) return;

      const qty = getProductQty(product.id);
      const controlHtml = renderProductControl(product.id, product.title, qty, product.inStock);
      controls.innerHTML = `<div class="price">₹${product.price}</div>${controlHtml}`;

      const sizeButtons = Array.from(card.querySelectorAll(".size-option-btn"));
      sizeButtons.forEach((btn) => {
        const sizeValue = Number(btn.dataset.size || 500);
        btn.classList.toggle("active", sizeValue === product.sizeValue);
      });
    });
  }

  logoutBtn?.addEventListener("click", () => {
    localStorage.removeItem("token");
    localStorage.removeItem(CART_KEY);
    serverSessionValid = false;
    updateCartCount();
    syncAuthUi();
    showToast("Logged out successfully");
  });

  const updateCartCount = () => {
    const el = byId("cartCount");
    if (!el) return;
    if (!isLoggedIn()) {
      el.textContent = "0";
      return;
    }
    const items = getCart();
    el.textContent = items.reduce((sum, i) => sum + (i.qty || 0), 0);
  };
  updateCartCount();
  initCartSync();

  async function changeCartQty(item, delta) {
    const canUseCart = await validateSessionWithServer();
    if (!canUseCart) {
      alert("Please login first to add items to cart.");
      window.location.href = "login.html?next=index.html";
      return;
    }

    if (!item.inStock && delta > 0) {
      showToast(`${item.title} is out of stock`);
      return;
    }

    const cart = getCart();
    const existing = cart.find(x => x.id === item.id);

    if (delta > 0) {
      if (existing) existing.qty += 1;
      else cart.push({ ...item, qty: 1 });
      showToast(`${item.title} added to cart`);
    }

    if (delta < 0 && existing) {
      existing.qty -= 1;
      if (existing.qty <= 0) {
        const idx = cart.findIndex(x => x.id === item.id);
        if (idx >= 0) cart.splice(idx, 1);
      }
    }

    setCart(cart);
    updateCartCount();
    renderDrawer();
    refreshProductCardControls();
  }

  productGrid?.addEventListener("click", e => {
    const sizeButton = e.target.closest("button[data-action='select-size'][data-base][data-size]");
    if (sizeButton) {
      const baseKey = sizeButton.dataset.base;
      const sizeValue = Number(sizeButton.dataset.size || 500);
      if (baseKey && Number.isFinite(sizeValue)) {
        selectedSizeByBase[baseKey] = sizeValue;
        refreshProductCardControls();
      }
      return;
    }

    const button = e.target.closest("button[data-id]");
    if (!button) return;

    const id = button.dataset.id;
    const action = button.dataset.action;
    const prod = productById.get(id);
    if (!prod) return;

    if (action === "add" || action === "increase") {
      if (!prod.inStock) {
        showToast(`${prod.title} is out of stock`);
        return;
      }
      changeCartQty(prod, 1);
      return;
    }

    if (action === "decrease") {
      changeCartQty(prod, -1);
    }
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
    window.location.href = "cart.html";
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
    if (!drawer || !drawerBackdrop) return;
    drawer.classList.remove("open");
    drawerBackdrop.classList.remove("show");
  });
  drawerBackdrop?.addEventListener("click", () => {
    if (!drawer || !drawerBackdrop) return;
    drawer.classList.remove("open");
    drawerBackdrop.classList.remove("show");
  });

  /* ---------------------------
     ✅ CHECKOUT — FIXED ENDPOINT
  ----------------------------*/
  drawerCheckout?.addEventListener("click", async () => {
    try {
      const token = getToken();
      const canUseCart = await validateSessionWithServer(true);
      if (!token || !canUseCart) {
        enforceAuthState();
        alert("Please login first.");
        window.location.href = "login.html?next=cart.html";
        return;
      }

      const items = getCart();
      if (items.length === 0) {
        alert("Your cart is empty.");
        return;
      }

      let savedDetails = null;
      try {
        savedDetails = JSON.parse(localStorage.getItem("delivery_details") || "null");
      } catch (_err) {
        savedDetails = null;
      }

      const fallbackAddress = (localStorage.getItem("delivery_address") || "").trim();
      const deliveryDetails = {
        address: (savedDetails?.address || fallbackAddress || "").trim(),
        landmark: (savedDetails?.landmark || "").trim(),
        pincode: (savedDetails?.pincode || "").trim(),
        phone: (savedDetails?.phone || "").trim(),
      };

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
        body: JSON.stringify({ items, total, deliveryDetails }),
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
  renderProducts();
  updateProductDots(0);
  loadProductsFromApi();
});
