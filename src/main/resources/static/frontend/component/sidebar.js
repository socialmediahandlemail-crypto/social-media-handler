async function loadSidebar() {
  const container = document.getElementById("sidebar-container");
  if (!container) return;

  const res = await fetch("component/sidebar.html");
  container.innerHTML = await res.text();

  initSidebarLogic();
}

function initSidebarLogic() {
  const items = document.querySelectorAll(".channel-item");
  const showMoreBtn = document.getElementById("showMoreBtn");
  let showMore = false;

  // Channel click handler
  items.forEach(item => {
    item.addEventListener("click", () => {
      items.forEach(i => i.classList.remove("bg-blue-50", "text-blue-600"));
      item.classList.add("bg-blue-50", "text-blue-600");

      const channel = item.dataset.channel;
      
      // Only connect to channel if it's not "all"
      if (channel !== "all") {
        // Mark that we're redirecting - prevent auto-click on page reload
        sessionStorage.setItem("isRedirecting", "true");
        sessionStorage.setItem("redirectingChannel", channel);
        connectChannel(channel);
      } else {
        // Safe to save "all" as active channel
        localStorage.setItem("activeChannel", channel);
      }
    });
  });

  // Show more button handler
  if (showMoreBtn) {
    showMoreBtn.addEventListener("click", () => {
      showMore = !showMore;
      const hiddenItems = document.querySelectorAll(".channel-item[data-channel='facebook'], .channel-item[data-channel='youtube'], .channel-item[data-channel='tiktok'], .channel-item[data-channel='twitter'], .channel-item[data-channel='snapchat'], .channel-item[data-channel='pinterest'], .channel-item[data-channel='whatsapp']");
      
      hiddenItems.forEach(item => {
        if (showMore) {
          item.classList.remove("hidden");
        } else {
          item.classList.add("hidden");
        }
      });

      // Update button text and rotation
      if (showMore) {
        showMoreBtn.innerHTML = '⌃ <span>Show less channels</span>';
      } else {
        showMoreBtn.innerHTML = '⌄ <span>Show more channels</span>';
      }
    });
  }

  // Restore active channel ONLY if we're not redirecting
  const isRedirecting = sessionStorage.getItem("isRedirecting");
  const saved = localStorage.getItem("activeChannel");
  
  if (saved && !isRedirecting) {
    // Just highlight the channel, don't click it (don't trigger connection)
    const savedItem = document.querySelector(`[data-channel="${saved}"]`);
    if (savedItem) {
      savedItem.classList.add("bg-blue-50", "text-blue-600");
    }
  } else if (saved && isRedirecting) {
    // Clear the redirect flag after a short delay
    setTimeout(() => {
      sessionStorage.removeItem("isRedirecting");
      sessionStorage.removeItem("redirectingChannel");
    }, 1000);
  }
}

// Import config from config.js (ensure config.js is loaded before this script)
// const API_BASE_URL and API_ENDPOINTS are now defined in config.js

// Track ongoing connection attempts to prevent duplicate redirects
const ONGOING_CONNECTIONS = new Set();

// Timeout for redirect (ms) - prevents hanging if OAuth flow is cancelled
const REDIRECT_TIMEOUT = 5000;

// Channel OAuth endpoints mapping (now using API_ENDPOINTS from config.js)
const OAUTH_ENDPOINTS = {
  youtube: API_ENDPOINTS.OAUTH.YOUTUBE,
  linkedin: (userEmail) => API_ENDPOINTS.OAUTH.LINKEDIN(userEmail),
  instagram: API_ENDPOINTS.OAUTH.INSTAGRAM,
  facebook: API_ENDPOINTS.OAUTH.FACEBOOK,
  twitter: API_ENDPOINTS.OAUTH.TWITTER,
  pinterest: API_ENDPOINTS.OAUTH.PINTEREST,
  tiktok: API_ENDPOINTS.OAUTH.TIKTOK,
  whatsapp: API_ENDPOINTS.OAUTH.WHATSAPP
};

async function connectChannel(channel) {
  console.log("Connecting to:", channel);

  // Prevent duplicate connection attempts for the same channel
  if (ONGOING_CONNECTIONS.has(channel)) {
    console.warn(`Connection to ${channel} is already in progress. Ignoring duplicate request.`);
    showNotification(`Connection to ${channel} is already in progress...`, "warning");
    return;
  }

  // Mark this channel as having an active connection attempt
  ONGOING_CONNECTIONS.add(channel);

  try {
    // Check if OAuth endpoint exists for this channel
    if (OAUTH_ENDPOINTS[channel]) {
      let oauthUrl = OAUTH_ENDPOINTS[channel];
      
      // Handle dynamic LinkedIn URL
      if (typeof oauthUrl === 'function') {
        oauthUrl = oauthUrl(localStorage.getItem("userEmail") || "");
      }
      
      console.log("Redirecting to OAuth:", oauthUrl);
      
      // Show loading state
      showNotification(`Redirecting to ${channel} authorization...`, "info");
      
      // Set a timeout to handle cancelled OAuth flows
      const timeoutId = setTimeout(() => {
        if (ONGOING_CONNECTIONS.has(channel)) {
          ONGOING_CONNECTIONS.delete(channel);
          console.warn(`${channel} OAuth redirect timeout - user may have cancelled`);
          showNotification(`${channel} connection was cancelled or timed out.`, "warning");
        }
      }, REDIRECT_TIMEOUT);

      // Perform redirect - if user completes OAuth, page will reload
      // If user cancels, they'll return to this page and timeout will clean up
      window.location.href = oauthUrl;
      
      // Store timeout ID for potential cleanup
      sessionStorage.setItem(`${channel}_timeout`, timeoutId);
      return;
    }

    // Fallback: Use generic connect API
    console.log("Using generic connect API for:", channel);
    showNotification(`Connecting to ${channel}...`, "info");

    const response = await fetch(API_ENDPOINTS.CHANNELS.CONNECT(channel), {
      method: "POST",
      headers: { 
        "Content-Type": "application/json",
        "Authorization": `Bearer ${localStorage.getItem("authToken") || ""}`
      },
      body: JSON.stringify({ channel })
    });

    if (!response.ok) {
      throw new Error(`Failed to connect ${channel}: ${response.statusText}`);
    }

    const data = await response.json();
    console.log("Connection successful:", data);
    
    // Show success feedback
    showNotification(`Successfully connected to ${channel}!`, "success");
    
    // Clear the connection tracking
    ONGOING_CONNECTIONS.delete(channel);
    
  } catch (error) {
    console.error("Connection error:", error);
    showNotification(`Failed to connect to ${channel}. ${error.message}`, "error");
    
    // Clear the connection tracking on error
    ONGOING_CONNECTIONS.delete(channel);
  }
}

// Clean up on page visibility change (e.g., when returning from OAuth)
document.addEventListener("visibilitychange", () => {
  if (document.visibilityState === "visible") {
    console.log("Page became visible - clearing stale connection attempts");
    // Clear any stale connection attempts when returning to page
    setTimeout(() => {
      ONGOING_CONNECTIONS.forEach(channel => {
        // Only clear if it's been longer than expected redirect time
        const timeout = sessionStorage.getItem(`${channel}_timeout`);
        if (timeout) {
          clearTimeout(parseInt(timeout));
          sessionStorage.removeItem(`${channel}_timeout`);
        }
        ONGOING_CONNECTIONS.delete(channel);
      });
    }, 1000);
  }
});

// Notification helper (optional - customize based on your UI)
function showNotification(message, type = "info") {
  const bgColor = type === "success" ? "bg-green-500" : type === "error" ? "bg-red-500" : type === "warning" ? "bg-yellow-500" : "bg-blue-500";
  console.log(`[${type.toUpperCase()}] ${message}`);
  
  // Optional: Add visual notification
  // TODO: Implement a toast/alert system here if needed
}
loadSidebar();

