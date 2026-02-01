import { useEffect, useMemo, useState } from "react";

const API_BASE = "http://localhost:8080";

async function api(path, { token, method = "GET", body } = {}) {
  const headers = { "Content-Type": "application/json" };
  if (token) headers.Authorization = `Bearer ${token}`;

  const res = await fetch(`${API_BASE}${path}`, {
    method,
    headers,
    body: body ? JSON.stringify(body) : undefined,
  });

  const text = await res.text();
  const data = text ? safeJson(text) : null;

  if (!res.ok) {
    const msg = data?.error || data?.message || `HTTP ${res.status}`;
    throw new Error(msg);
  }
  return data;
}

function safeJson(text) {
  try { return JSON.parse(text); } catch { return { raw: text }; }
}

export default function App() {
  const [mode, setMode] = useState("login"); // login | register
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const [token, setToken] = useState(() => localStorage.getItem("token") || "");
  const [status, setStatus] = useState("");
  const [todos, setTodos] = useState([]);
  const [newTodo, setNewTodo] = useState("");

  const authed = useMemo(() => Boolean(token), [token]);

  async function loadTodos(t = token) {
    const list = await api("/api/todos", { token: t });
    setTodos(list);
  }

  async function onAuthSubmit(e) {
    e.preventDefault();
    setStatus("");

    try {
      const path = mode === "register" ? "/api/auth/register" : "/api/auth/login";
      const out = await api(path, { method: "POST", body: { email, password } });
      const t = out.token;
      setToken(t);
      localStorage.setItem("token", t);
      setPassword("");
      await loadTodos(t);
      setStatus("Authenticated ✅");
    } catch (err) {
      setStatus(err.message);
    }
  }

  async function addTodo(e) {
    e.preventDefault();
    setStatus("");

    try {
      await api("/api/todos", { token, method: "POST", body: { title: newTodo } });
      setNewTodo("");
      await loadTodos();
    } catch (err) {
      setStatus(err.message);
    }
  }

  async function toggleTodo(id) {
    setStatus("");
    try {
      await api(`/api/todos/${id}/toggle`, { token, method: "PATCH" });
      await loadTodos();
    } catch (err) {
      setStatus(err.message);
    }
  }

  async function deleteTodo(id) {
    setStatus("");
    try {
      await api(`/api/todos/${id}`, { token, method: "DELETE" });
      await loadTodos();
    } catch (err) {
      setStatus(err.message);
    }
  }

  function logout() {
    setToken("");
    localStorage.removeItem("token");
    setTodos([]);
    setStatus("Logged out.");
  }

  useEffect(() => {
    if (token) {
      loadTodos().catch(() => {
        // token might be expired/invalid
        logout();
      });
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <div style={{ maxWidth: 720, margin: "40px auto", fontFamily: "system-ui", padding: 16 }}>
      <h1>Real API + Auth Frontend</h1>
      <p style={{ opacity: 0.8 }}>
        Backend: <code>localhost:8080</code> • Frontend: <code>localhost:5173</code>
      </p>

      {status && (
        <div style={{ padding: 12, border: "1px solid #ddd", borderRadius: 10, marginBottom: 14 }}>
          {status}
        </div>
      )}

      {!authed ? (
        <div style={{ border: "1px solid #ddd", borderRadius: 14, padding: 16 }}>
          <div style={{ display: "flex", gap: 10, marginBottom: 12 }}>
            <button onClick={() => setMode("login")} disabled={mode === "login"}>
              Login
            </button>
            <button onClick={() => setMode("register")} disabled={mode === "register"}>
              Register
            </button>
          </div>

          <form onSubmit={onAuthSubmit} style={{ display: "grid", gap: 10 }}>
            <input
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="email"
              autoComplete="email"
            />
            <input
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="password"
              type="password"
              autoComplete={mode === "register" ? "new-password" : "current-password"}
            />
            <button type="submit">{mode === "register" ? "Create account" : "Login"}</button>
          </form>
        </div>
      ) : (
        <div style={{ border: "1px solid #ddd", borderRadius: 14, padding: 16 }}>
          <div style={{ display: "flex", justifyContent: "space-between", gap: 12, alignItems: "center" }}>
            <div>
              <div style={{ fontWeight: 700 }}>Authenticated</div>
              <div style={{ fontSize: 12, opacity: 0.7 }}>
                Token stored in localStorage
              </div>
            </div>
            <button onClick={logout}>Logout</button>
          </div>

          <hr style={{ margin: "16px 0" }} />

          <form onSubmit={addTodo} style={{ display: "flex", gap: 10 }}>
            <input
              style={{ flex: 1 }}
              value={newTodo}
              onChange={(e) => setNewTodo(e.target.value)}
              placeholder="New todo..."
            />
            <button type="submit" disabled={!newTodo.trim()}>Add</button>
          </form>

          <ul style={{ listStyle: "none", padding: 0, marginTop: 16 }}>
            {todos.map((t) => (
              <li key={t.id} style={{ display: "flex", gap: 10, padding: 10, borderBottom: "1px solid #eee" }}>
                <input type="checkbox" checked={t.done} onChange={() => toggleTodo(t.id)} />
                <div style={{ flex: 1, textDecoration: t.done ? "line-through" : "none" }}>
                  {t.title}
                </div>
                <button onClick={() => deleteTodo(t.id)}>Delete</button>
              </li>
            ))}
          </ul>
        </div>
      )}
    </div>
  );
}

