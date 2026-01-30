/**
 * Configuration file for SocialSync Frontend
 * This file contains all configuration constants used across the application
 * Change these values to match your deployment environment
 */

// API Base URL - Change this for production deployment
const API_BASE_URL = "http://localhost:8082";

// API Endpoints
const API_ENDPOINTS = {
  // Authentication endpoints
  AUTH: {
    LOGIN: `${API_BASE_URL}/auth/login`,
    SIGNUP: `${API_BASE_URL}/auth/signup`
  },
  
  // Posts endpoints
  POSTS: {
    ALL: `${API_BASE_URL}/api/posts/all`,
    SCHEDULE: `${API_BASE_URL}/api/posts/schedule`,
    GET_BY_ID: (postId) => `${API_BASE_URL}/api/posts/${postId}`,
    UPDATE: (postId) => `${API_BASE_URL}/api/posts/${postId}`
  },
  
  // Media endpoints
  MEDIA: {
    UPLOAD: `${API_BASE_URL}/api/media/upload`
  },
  
  // Channels endpoints
  CHANNELS: {
    CONNECT: (channel) => `${API_BASE_URL}/api/channels/connect/${channel.toLowerCase()}`,
    STATUS: `${API_BASE_URL}/api/channels/status`
  },
  
  // OAuth endpoints
  OAUTH: {
    YOUTUBE: `${API_BASE_URL}/oauth/youtube/connect`,
    LINKEDIN: (userId) => `${API_BASE_URL}/oauth/linkedin/connect?userId=${userId}`,
    INSTAGRAM: `${API_BASE_URL}/oauth/instagram/connect`,
    FACEBOOK: `${API_BASE_URL}/oauth/facebook/connect`,
    TWITTER: `${API_BASE_URL}/oauth/twitter/connect`,
    PINTEREST: `${API_BASE_URL}/oauth/pinterest/connect`,
    TIKTOK: `${API_BASE_URL}/oauth/tiktok/connect`,
    WHATSAPP: `${API_BASE_URL}/oauth/whatsapp/connect`
  }
};

// Export for use in modules (if using ES6 modules)
// export { API_BASE_URL, API_ENDPOINTS };
