document.addEventListener("DOMContentLoaded", () => {
  // Import config from config.js
  // Note: config.js must be loaded before this script

  const listBtn = document.getElementById("listBtn");
  const calendarBtn = document.getElementById("calendarBtn");

  const listViewSection = document.getElementById("listViewContent");
  const calendarViewSection = document.getElementById("calendarViewContent");
  const postTabsContainer = document.getElementById("postTabsContainer");
  const upcomingViewContent = document.getElementById("upcomingViewContent");
  const pastViewContent = document.getElementById("pastViewContent");

  const calendarTitle = document.getElementById("calendarTitle");
  const calendarGrid = document.getElementById("calendarGrid");
  const prevMonthBtn = document.getElementById("prevMonth");
  const nextMonthBtn = document.getElementById("nextMonth");

  if (!listBtn || !calendarBtn) return;

  let allPosts = []; // Store posts globally

  // Function to fetch posts
  async function fetchAndRenderCalendar() {
    try {
      const response = await fetch(API_ENDPOINTS.POSTS.ALL, {
        headers: { 'Authorization': 'Bearer ' + localStorage.getItem('authToken') }
      });
      allPosts = await response.json();
      renderCalendar(allPosts);
    } catch (error) {
      console.error("Error loading posts for calendar:", error);
      renderCalendar([]);
    }
  }

  // ===== VIEW TOGGLE HANDLERS =====
  listBtn.onclick = () => {
    upcomingViewContent.classList.remove("hidden");
    pastViewContent.classList.add("hidden");
    calendarViewSection.classList.add("hidden");
    postTabsContainer.classList.remove("hidden");

    listBtn.classList.add("active");
    calendarBtn.classList.remove("active");

    // Remove sliding background from container
    const btnGroup = document.querySelector(".view-btn-group");
    if (btnGroup) {
      btnGroup.classList.remove("calendar-active");
    }
  };

  calendarBtn.onclick = async () => {
    calendarViewSection.classList.remove("hidden");
    upcomingViewContent.classList.add("hidden");
    pastViewContent.classList.add("hidden");
    postTabsContainer.classList.add("hidden");

    calendarBtn.classList.add("active");
    listBtn.classList.remove("active");

    // Add sliding background to container
    const btnGroup = document.querySelector(".view-btn-group");
    if (btnGroup) {
      btnGroup.classList.add("calendar-active");
    }

    // Fetch posts and render calendar
    try {
      const response = await fetch(API_ENDPOINTS.POSTS.ALL, {
        headers: { 'Authorization': 'Bearer ' + localStorage.getItem('authToken') }
      });
      const posts = await response.json();
      renderCalendar(posts);
    } catch (error) {
      console.error("Error loading posts for calendar:", error);
      renderCalendar([]);
    }
  };

  // ===== CALENDAR LOGIC =====
  const scheduledPosts = {
    "2025-01-05": 2,
    "2025-01-10": 1,
    "2025-01-18": 3
  };

  let currentDate = new Date();
function renderCalendar(posts = []) {
  calendarGrid.innerHTML = "";

  const year = currentDate.getFullYear();
  const month = currentDate.getMonth();

  calendarTitle.textContent =
    currentDate.toLocaleString("default", { month: "long" }) + " " + year;

  const firstDay = new Date(year, month, 1).getDay();
  const daysInMonth = new Date(year, month + 1, 0).getDate();

  // Empty cells
  for (let i = 0; i < firstDay; i++) {
    calendarGrid.innerHTML += `
      <div class="bg-white/50 h-20 rounded-lg border border-cyan-200/30"></div>
    `;
  }

  // Group posts by date
  const postsByDate = {};
  posts.forEach(post => {
    const dateKey = new Date(post.scheduledTime)
      .toISOString()
      .split("T")[0];
    postsByDate[dateKey] = postsByDate[dateKey] || [];
    postsByDate[dateKey].push(post);
  });

  // Render days
  for (let day = 1; day <= daysInMonth; day++) {
    const dateKey = `${year}-${String(month + 1).padStart(2, "0")}-${String(day).padStart(2, "0")}`;
    const dayPosts = postsByDate[dateKey] || [];

    calendarGrid.innerHTML += `
      <div class="bg-white/60 h-20 p-2 border border-cyan-200/50 hover:border-cyan-400/50 transition-all rounded-lg group backdrop-blur-sm">
        <div class="text-xs font-bold text-cyan-600 mb-0.5">${day}</div>

        <div class="space-y-0.5">
          ${
            dayPosts.slice(0, 2).map(post => `
              <div class="text-[9px] px-1.5 py-0.5 rounded-sm bg-cyan-100/50 text-cyan-700 font-medium truncate border border-cyan-300/50 group-hover:bg-cyan-200/60 transition">
                ${post.platform} • ${post.content}
              </div>
            `).join("")
          }

          ${
            dayPosts.length > 2
              ? `<div class="text-[8px] text-cyan-600 font-bold">
                   +${dayPosts.length - 2} more
                 </div>`
              : ""
          }
        </div>
      </div>
    `;
  }
}


  prevMonthBtn.onclick = async () => {
    currentDate.setMonth(currentDate.getMonth() - 1);
    try {
      const response = await fetch(API_ENDPOINTS.POSTS.ALL, {
        headers: { 'Authorization': 'Bearer ' + localStorage.getItem('authToken') }
      });
      const posts = await response.json();
      renderCalendar(posts);
    } catch (error) {
      console.error("Error loading posts:", error);
      renderCalendar([]);
    }
  };

  nextMonthBtn.onclick = async () => {
    currentDate.setMonth(currentDate.getMonth() + 1);
    try {
      const response = await fetch(API_ENDPOINTS.POSTS.ALL, {
        headers: { 'Authorization': 'Bearer ' + localStorage.getItem('authToken') }
      });
      const posts = await response.json();
      renderCalendar(posts);
    } catch (error) {
      console.error("Error loading posts:", error);
      renderCalendar([]);
    }
  };

  // Initial calendar render on page load
  fetchAndRenderCalendar();

});
