document.addEventListener("DOMContentLoaded", () => {
    initAppInfo();
    setupNavigation();
    setupLoginForm();
    setupPaymentForm();
    setupUserForms();
    setupMfaForm();
    setupReportForm();
});

let sseEventSource = null;

function initAppInfo() {
    fetch("/api/v1/config/app-info")
        .then(res => res.json())
        .then(data => {
            if (data.appName) {
                document.getElementById("app-title-login").innerText = data.appName;
                document.getElementById("app-name-display").innerText = data.appName;
            }
            if (data.authProvider) {
                document.getElementById("auth-mode-badge").innerText = data.authProvider;
            }
        })
        .catch(err => console.error("Error fetching app info:", err));
}

function setupNavigation() {
    const navItems = document.querySelectorAll(".nav-item[data-tab]");
    navItems.forEach(item => {
        item.addEventListener("click", () => {
            const targetTab = item.getAttribute("data-tab");

            navItems.forEach(nav => nav.classList.remove("active"));
            item.classList.add("active");

            document.querySelectorAll(".tab-content").forEach(tab => tab.classList.remove("active"));
            const activeContent = document.getElementById(targetTab);
            if (activeContent) {
                activeContent.classList.add("active");
            }

            document.getElementById("page-title").innerText = item.innerText.trim();

            if (targetTab === "tab-payments") loadProviders();
            if (targetTab === "tab-users") loadUsers();
            if (targetTab === "tab-security") loadMfaStatus();
        });
    });

    document.getElementById("btn-logout").addEventListener("click", () => {
        if (sseEventSource) {
            sseEventSource.close();
        }
        document.getElementById("app-container").classList.add("hidden");
        document.getElementById("login-container").classList.remove("hidden");
    });
}

function setupLoginForm() {
    const loginForm = document.getElementById("login-form");
    const loginError = document.getElementById("login-error");

    loginForm.addEventListener("submit", (e) => {
        e.preventDefault();
        loginError.classList.add("hidden");

        const username = document.getElementById("username").value;
        const password = document.getElementById("password").value;

        fetch("/api/v1/auth/login", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ username, password })
        })
        .then(res => {
            if (!res.ok) throw new Error("Invalid credentials or locked account.");
            return res.json();
        })
        .then(data => {
            document.getElementById("logged-user").innerText = username;
            document.getElementById("login-container").classList.add("hidden");
            document.getElementById("app-container").classList.remove("hidden");

            initSseFeed();
            loadProviders();
        })
        .catch(err => {
            loginError.innerText = err.message;
            loginError.classList.remove("hidden");
        });
    });
}

function initSseFeed() {
    if (sseEventSource) sseEventSource.close();

    const feedLog = document.getElementById("sse-feed-log");
    feedLog.innerHTML = `<p class="log-entry system">[SYSTEM] Connected to Server-Sent Events stream (/api/v1/dashboard/stream)</p>`;

    sseEventSource = new EventSource("/api/v1/dashboard/stream");

    sseEventSource.addEventListener("dashboard-metrics", (event) => {
        try {
            const metrics = JSON.parse(event.data);
            document.getElementById("metric-tps").innerText = metrics.throughputTps;
            document.getElementById("metric-workers").innerText = metrics.activeWorkerCount;

            const logEntry = document.createElement("p");
            logEntry.className = "log-entry data";
            logEntry.innerText = `[${metrics.timestamp}] Live TPS: ${metrics.throughputTps} | Active Workers: ${metrics.activeWorkerCount}`;
            feedLog.prepend(logEntry);

            if (feedLog.children.length > 20) {
                feedLog.removeChild(feedLog.lastChild);
            }
        } catch (e) {
            console.error("Error parsing SSE metrics:", e);
        }
    });

    sseEventSource.onerror = () => {
        console.warn("SSE stream disconnected.");
    };
}

function loadProviders() {
    fetch("/api/v1/payments/providers")
        .then(res => res.json())
        .then(data => {
            const listContainer = document.getElementById("providers-list");
            listContainer.innerHTML = "";

            let activeCount = 0;
            const totalCount = Object.keys(data).length;

            Object.entries(data).forEach(([provider, status]) => {
                if (status.enabled) activeCount++;

                const card = document.createElement("div");
                card.className = "provider-card";
                card.innerHTML = `
                    <div class="provider-header">
                        <strong>${provider}</strong>
                        <span class="badge ${status.enabled ? 'badge-success' : 'badge-danger'}">
                            ${status.enabled ? 'ENABLED' : 'DISABLED'}
                        </span>
                    </div>
                    <p style="font-size: 13px; color: #94a3b8;">Active Workers: <strong>${status.activeHandlerCount}</strong></p>
                    <p style="font-size: 11px; color: #64748b; margin-top: 4px;">Pool: ${status.handlerIds.join(', ')}</p>
                    ${status.enabled ? `
                        <div style="margin-top: 12px; display: flex; gap: 8px;">
                            <button onclick="scaleProvider('${provider}', ${status.activeHandlerCount + 1})" class="btn btn-secondary btn-sm">+ Worker</button>
                            <button onclick="scaleProvider('${provider}', ${Math.max(1, status.activeHandlerCount - 1)})" class="btn btn-secondary btn-sm">- Worker</button>
                        </div>
                    ` : ''}
                `;
                listContainer.appendChild(card);
            });

            document.getElementById("metric-gateways").innerText = `${activeCount} / ${totalCount}`;
        });
}

function scaleProvider(provider, targetCount) {
    fetch(`/api/v1/payments/providers/${provider}/scale?count=${targetCount}`, { method: "POST" })
        .then(res => res.json())
        .then(() => loadProviders());
}

function setupPaymentForm() {
    const form = document.getElementById("payment-test-form");
    const resultBox = document.getElementById("payment-result");

    form.addEventListener("submit", (e) => {
        e.preventDefault();
        const provider = document.getElementById("pay-provider").value;
        const amount = document.getElementById("pay-amount").value;
        const currency = document.getElementById("pay-currency").value;

        const payload = {
            transactionId: "tx-ui-" + Date.now(),
            amount: parseFloat(amount),
            currency: currency,
            provider: provider,
            customerId: "cust-demo",
            metadata: { source: "WEB_UI" }
        };

        fetch("/api/v1/payments/process", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
        })
        .then(res => res.json())
        .then(data => {
            resultBox.innerHTML = `<pre>${JSON.stringify(data, null, 2)}</pre>`;
            resultBox.classList.remove("hidden");
            loadProviders();
        })
        .catch(err => {
            resultBox.innerHTML = `<span style="color: #fca5a5;">Error: ${err.message}</span>`;
            resultBox.classList.remove("hidden");
        });
    });

    document.getElementById("btn-refresh-providers").addEventListener("click", loadProviders);
}

function loadUsers() {
    fetch("/api/v1/auth/users")
        .then(res => res.json())
        .then(users => {
            const tbody = document.getElementById("users-table-body");
            tbody.innerHTML = "";

            users.forEach(u => {
                const tr = document.createElement("tr");
                tr.innerHTML = `
                    <td>${u.id}</td>
                    <td><strong>${u.username}</strong></td>
                    <td>${u.email || '-'}</td>
                    <td><span class="badge ${u.status === 'ACTIVE' ? 'badge-success' : 'badge-danger'}">${u.status}</span></td>
                    <td>${u.accountLocked ? '<span style="color: #fca5a5;">YES</span>' : 'NO'}</td>
                    <td>${u.passwordExpiresAt ? u.passwordExpiresAt.substring(0, 10) : '-'}</td>
                    <td>
                        ${u.accountLocked ?
                            `<button onclick="unlockUser('${u.username}')" class="btn btn-success btn-sm"><i class="fa-solid fa-unlock"></i> Unlock</button>` :
                            `<button onclick="lockUser('${u.username}')" class="btn btn-danger btn-sm"><i class="fa-solid fa-lock"></i> Lock</button>`
                        }
                        <button onclick="resetPasswordPrompt('${u.username}')" class="btn btn-secondary btn-sm"><i class="fa-solid fa-key"></i> Reset Pass</button>
                    </td>
                `;
                tbody.appendChild(tr);
            });
        });
}

function lockUser(username) {
    fetch(`/api/v1/auth/users/${username}/lock`, { method: "POST" })
        .then(() => loadUsers());
}

function unlockUser(username) {
    fetch(`/api/v1/auth/users/${username}/unlock`, { method: "POST" })
        .then(() => loadUsers());
}

function resetPasswordPrompt(username) {
    const newPass = prompt(`Enter new password for ${username}:`);
    if (newPass) {
        fetch(`/api/v1/auth/users/${username}/reset-password`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ newPassword: newPass })
        })
        .then(res => res.json())
        .then(data => alert(`Password reset result: ${data.status}`))
        .then(() => loadUsers());
    }
}

function setupUserForms() {
    const createForm = document.getElementById("create-user-form");
    createForm.addEventListener("submit", (e) => {
        e.preventDefault();
        const payload = {
            username: document.getElementById("new-username").value,
            password: document.getElementById("new-password").value,
            email: document.getElementById("new-email").value,
            firstName: document.getElementById("new-firstname").value,
            lastName: document.getElementById("new-lastname").value
        };

        fetch("/api/v1/auth/register", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
        })
        .then(res => res.json())
        .then(() => {
            alert("Account created successfully!");
            createForm.reset();
            loadUsers();
        });
    });

    document.getElementById("btn-refresh-users").addEventListener("click", loadUsers);
}

function loadMfaStatus() {
    fetch("/api/v1/auth/mfa/status")
        .then(res => res.json())
        .then(data => {
            const toggle = document.getElementById("mfa-toggle-switch");
            const statusText = document.getElementById("mfa-status-text");
            toggle.checked = data.mfaEnabled;
            statusText.innerText = data.mfaEnabled ? "ENABLED (SMS/Email Active)" : "DISABLED (Demo Bypass Active)";
            statusText.className = "status-badge " + (data.mfaEnabled ? "enabled" : "disabled");
        });
}

function setupMfaForm() {
    const toggle = document.getElementById("mfa-toggle-switch");
    toggle.addEventListener("change", () => {
        fetch(`/api/v1/auth/mfa/toggle?enabled=${toggle.checked}`, { method: "POST" })
            .then(res => res.json())
            .then(() => loadMfaStatus());
    });

    const mfaResult = document.getElementById("mfa-result");

    document.getElementById("btn-generate-otp").addEventListener("click", () => {
        const username = document.getElementById("mfa-username").value;
        fetch(`/api/v1/auth/mfa/generate?username=${username}`, { method: "POST" })
            .then(res => res.json())
            .then(data => {
                mfaResult.innerHTML = `<pre>${JSON.stringify(data, null, 2)}</pre>`;
                mfaResult.classList.remove("hidden");
            });
    });

    document.getElementById("btn-verify-otp").addEventListener("click", () => {
        const username = document.getElementById("mfa-username").value;
        const code = document.getElementById("mfa-code").value;
        fetch(`/api/v1/auth/mfa/verify?username=${username}&code=${code}`, { method: "POST" })
            .then(res => res.json())
            .then(data => {
                mfaResult.innerHTML = `<pre>${JSON.stringify(data, null, 2)}</pre>`;
                mfaResult.classList.remove("hidden");
            });
    });
}

function setupReportForm() {
    const form = document.getElementById("report-filter-form");
    const output = document.getElementById("report-output");

    form.addEventListener("submit", (e) => {
        e.preventDefault();
        const startDate = document.getElementById("report-start-date").value;
        const endDate = document.getElementById("report-end-date").value;
        const provider = document.getElementById("report-provider").value;

        let url = `/api/v1/reports/summary?provider=${provider}`;
        if (startDate) url += `&startDate=${startDate}`;
        if (endDate) url += `&endDate=${endDate}`;

        fetch(url)
            .then(res => res.json())
            .then(data => {
                output.innerHTML = `<pre>${JSON.stringify(data, null, 2)}</pre>`;
            });
    });
}
