// Reusable showConfirm helper: returns Promise<boolean>
function showConfirm(message, options = {}) {
  return new Promise((resolve) => {
    // If a confirm modal already exists, remove it
    const existing = document.getElementById('global-confirm-root');
    if (existing) existing.remove();

    const root = document.createElement('div');
    root.id = 'global-confirm-root';
    root.style.position = 'fixed';
    root.style.inset = '0';
    root.style.display = 'flex';
    root.style.alignItems = 'center';
    root.style.justifyContent = 'center';
    root.style.zIndex = '10000';

    const backdrop = document.createElement('div');
    backdrop.style.position = 'absolute';
    backdrop.style.inset = '0';
    backdrop.style.background = 'rgba(2,6,23,0.45)';
    root.appendChild(backdrop);

    const card = document.createElement('div');
    card.style.position = 'relative';
    card.style.minWidth = '320px';
    card.style.maxWidth = '520px';
    card.style.background = 'white';
    card.style.borderRadius = '14px';
    card.style.padding = '20px 18px';
    card.style.boxShadow = '0 20px 50px rgba(2,6,23,0.18)';
    card.style.display = 'flex';
    card.style.flexDirection = 'column';
    card.style.gap = '12px';
    card.style.zIndex = '10001';
    card.style.fontFamily = 'Inter, system-ui, Arial, sans-serif';

    const top = document.createElement('div');
    top.style.display = 'flex';
    top.style.gap = '12px';
    top.style.alignItems = 'flex-start';

    const icon = document.createElement('div');
    icon.style.width = '44px';
    icon.style.height = '44px';
    icon.style.borderRadius = '10px';
    icon.style.display = 'grid';
    icon.style.placeItems = 'center';
    icon.style.background = 'linear-gradient(90deg,#fb7185,#f97316)';
    icon.style.color = '#fff';
    icon.textContent = '!';

    const body = document.createElement('div');
    const title = document.createElement('div');
    title.style.fontWeight = '700';
    title.style.color = '#0f172a';
    title.style.fontSize = '16px';
    title.textContent = options.title || 'Confirm action';

    const msg = document.createElement('div');
    msg.style.color = '#475569';
    msg.style.fontSize = '14px';
    msg.style.marginTop = '6px';
    msg.textContent = message;

    body.appendChild(title);
    body.appendChild(msg);

    top.appendChild(icon);
    top.appendChild(body);

    const actions = document.createElement('div');
    actions.style.display = 'flex';
    actions.style.gap = '10px';
    actions.style.justifyContent = 'flex-end';
    actions.style.marginTop = '6px';

    const cancelBtn = document.createElement('button');
    cancelBtn.style.background = '#fff';
    cancelBtn.style.border = '1px solid rgba(2,6,23,0.06)';
    cancelBtn.style.padding = '8px 12px';
    cancelBtn.style.borderRadius = '10px';
    cancelBtn.style.color = '#0f172a';
    cancelBtn.textContent = options.cancelText || 'Cancel';

    const acceptBtn = document.createElement('button');
    acceptBtn.style.background = 'linear-gradient(90deg,#10b981,#059669)';
    acceptBtn.style.color = '#fff';
    acceptBtn.style.padding = '8px 12px';
    acceptBtn.style.borderRadius = '10px';
    acceptBtn.style.border = '1px solid rgba(6,95,70,0.08)';
    acceptBtn.textContent = options.acceptText || 'Confirm';

    actions.appendChild(cancelBtn);
    actions.appendChild(acceptBtn);

    card.appendChild(top);
    card.appendChild(actions);
    root.appendChild(card);

    document.body.appendChild(root);

    const cleanup = () => {
      cancelBtn.onclick = null;
      acceptBtn.onclick = null;
      backdrop.onclick = null;
      if (root.parentElement) root.remove();
    };

    acceptBtn.onclick = () => { cleanup(); resolve(true); };
    cancelBtn.onclick = () => { cleanup(); resolve(false); };
    backdrop.onclick = () => { cleanup(); resolve(false); };
  });
}
