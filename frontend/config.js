(function () {
  var localHosts = ["localhost", "127.0.0.1"];
  var isLocal = localHosts.indexOf(window.location.hostname) !== -1;
  var savedApiBase = localStorage.getItem("API_BASE_URL");
  var defaultApiBase = isLocal
    ? "http://localhost:4000/api"
    : "https://natural-milk-backend.onrender.com/api";

  var apiBase = (savedApiBase || defaultApiBase).replace(/\/+$/, "");

  window.APP_CONFIG = {
    API_BASE: apiBase
  };
})();