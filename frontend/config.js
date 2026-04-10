(function () {
  var localHosts = ["localhost", "127.0.0.1"];
  var isLocal = localHosts.indexOf(window.location.hostname) !== -1;
  var savedApiBase = localStorage.getItem("API_BASE_URL");
  var cloudApiBase = "https://natural-milk-backend.onrender.com/api";
  var defaultApiBase = isLocal
    ? "http://localhost:4000/api"
    : cloudApiBase;

  function normalizeApiBase(value) {
    if (!value || typeof value !== "string") return "";
    try {
      var normalized = value.trim().replace(/\/+$/, "");
      var parsed = new URL(normalized);
      if (parsed.protocol !== "http:" && parsed.protocol !== "https:") return "";
      return normalized;
    } catch (_err) {
      return "";
    }
  }

  var normalizedSaved = normalizeApiBase(savedApiBase);
  var apiBase = defaultApiBase;

  if (normalizedSaved) {
    var savedHost = new URL(normalizedSaved).hostname;
    var isSavedLocal = localHosts.indexOf(savedHost) !== -1;

    // On deployed clients, ignore stale localhost overrides that cause long request timeouts.
    if (!(isSavedLocal && !isLocal)) {
      apiBase = normalizedSaved;
    }
  }

  localStorage.setItem("API_BASE_URL", apiBase);

  window.APP_CONFIG = {
    API_BASE: apiBase
  };
})();