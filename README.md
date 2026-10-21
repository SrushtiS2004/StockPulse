# StockPulse — AI Inventory & Dynamic Pricing Engine

StockPulse is an autonomous reactive commerce engine designed for modern e-commerce storefronts (ShopStream). It continuously monitors inventory depletion and demand velocity spikes, autonomously generates AI-powered dynamic pricing and replenishment recommendations, and routes them to a merchandising approval console before changes go live.

---

## 🚀 2-Minute Quick Start

### One-Click Launch (Windows)
Double-click `start-all.bat` in the root folder, or launch each component:

### 1. Launch Backend (Spring Boot 3.3.4, Java 17)
```powershell
cd backend
.\mvnw.cmd spring-boot:run
```
* Backend runs on **`http://localhost:8081`**
* H2 Database Console: **`http://localhost:8081/h2-console`** (`JDBC URL: jdbc:h2:mem:stockpulsedb`, User: `sa`, Password: empty)
* Pre-seeded with 8 products across Electronics, Apparel, and Home.
* Run Unit Tests: `.\mvnw.cmd test`

### 2. Launch Frontend (React 18 + Vite)
```powershell
cd frontend
npm install
npm run dev
```
* Frontend runs on **`http://localhost:5173`** (Vite automatically proxies all `/api` requests to port 8081).

---

## 🔒 Secrets & Keys Safety (Zero Leakage to GitHub)

This project strictly adheres to enterprise secret-management best practices to ensure **no credentials, cookies, or API keys are ever committed to GitHub**:

1. **Tracked Configuration (`application.properties`)**: Contains zero hardcoded secrets. Values are populated from environment variables (`LLM_API_KEY`, `LLM_COOKIE`) with safe empty defaults.
2. **Local Secrets File (`application-secret.properties`)**: Kept locally in `backend/src/main/resources/application-secret.properties` and **strictly ignored in `.gitignore`**. Spring Boot automatically imports it via `spring.config.import`.
3. **Environment Template (`.env.example`)**: Provided in the root directory for cloud or CI/CD deployments.
4. **Resilient Fallback**: If no API key is provided or the Zycus LiteLLM proxy is offline/unreachable, the engine automatically and silently falls back to `RuleBasedCommerceAdvisor` without crashing or dropping customer requests.

---

## 🎬 Live Walkthrough Trace (Demo Paths)

### Demo Path 1: Inventory Depletion Trigger (Low Stock)
1. Open the dashboard at `http://localhost:5173`.
2. Locate **`PRD-003` (Organic Cotton T-Shirt)** — already seeded near low threshold (`stock: 8`, `threshold: 15`). Notice pending pricing and reorder suggestions already waiting for approval.
3. Click **`+ Sale`** on **`PRD-007` (Portable Charger 20K)** (`stock: 18`, `threshold: 25`).
4. Notice stock decrements immediately (`18 -> 17`), velocity increments, and the endpoint returns `200 OK` instantly.
5. In the background, the agentic event fires: within seconds, an `INVENTORY_LOW` pricing suggestion and replenishment proposal appear in the queue with plain-English AI reasoning.
6. Click **`Accept & Publish Price`** — observe the price immediately updates live in the catalog!

### Demo Path 2: Demand Velocity Surge (Spike Trigger)
1. Locate **`PRD-008` (Hoodie — Heather Grey)** (`stock: 11`, `velocity: 15`).
2. Click **`⚡ Surge`** — simulates rapid viral orders pushing velocity above 3x the Apparel category average.
3. The agentic loop automatically queues a `DEMAND_SPIKE` pricing suggestion proposing a margin expansion price increase to capture consumer surplus without stalling viral velocity.

### Demo Path 3: Real-Time SSE Token Stream (Bonus +5 pts)
1. Click **`✦ AI Stream`** on any product in the inventory table.
2. A live neural stream modal opens, displaying real-time streaming reasoning tokens via Server-Sent Events (`/api/products/{id}/suggest-pricing/stream`).

### Demo Path 4: Zero-Downtime Strategy Switcher
1. In the top right header, click **`Rule-Based`**.
2. All suggestions dynamically calculate via deterministic fallback rules without restarting the server.
3. Click **`AI Advisor`** to switch back to neural LLM reasoning.

---

## 🏛️ System Architecture

```
[Storefront Order / Stock Update]
            │
            ▼
┌──────────────────────────────────────────────┐
│ ProductService.recordSale(id)                │  (Atomic stock decrement & velocity bump)
└──────────────────┬───────────────────────────┘
                   │
                   ▼  (Publishes ProductChangeEvent)
┌──────────────────────────────────────────────┐
│ ProductEventPublisher                        │
└──────────────────┬───────────────────────────┘
                   │
                   ▼  (@Async Thread Pool)
┌──────────────────────────────────────────────┐
│ ProductChangeEventHandler                    │
└──────────────────┬───────────────────────────┘
                   │
                   ▼  (Idempotency Check)
┌──────────────────────────────────────────────┐
│ AdvisorRegistry                              │
│   ├── AICommerceAdvisor (Differentiated LLM) │
│   └── RuleBasedCommerceAdvisor (Fallback)    │
└──────────────────┬───────────────────────────┘
                   │
                   ▼  (Queued as PENDING)
┌──────────────────────────────────────────────┐
│ Merchandising Approval Console (React 18)    │
│   ├── Accept: Atomically publishes price     │
│   └── Reject: Discards recommendation        │
└──────────────────────────────────────────────┘
```

---

## 📡 REST API Reference

| Method | Path | Description |
| :--- | :--- | :--- |
| `GET` | `/api/products` | Filterable catalog list (`?category=&status=`) |
| `POST` | `/api/products` | Create a new SKU |
| `PATCH` | `/api/products/{id}/stock` | Update stock level (triggers `INVENTORY_LOW` if `<= threshold`) |
| `POST` | `/api/products/{id}/orders` | Simulate sale (decrements stock, increments velocity, fires loop) |
| `POST` | `/api/products/{id}/suggest-pricing` | On-demand pricing recommendation |
| `POST` | `/api/products/{id}/suggest-reorder` | On-demand replenishment recommendation |
| `GET` | `/api/products/{id}/suggest-pricing/stream` | **SSE Token Stream** of AI reasoning tokens |
| `GET` | `/api/pricing-suggestions?status=PENDING` | Get pending pricing suggestions |
| `PATCH` | `/api/pricing-suggestions/{id}` | Accept or reject pricing suggestion |
| `GET` | `/api/reorder-suggestions?status=PENDING` | Get pending reorder suggestions |
| `PATCH` | `/api/reorder-suggestions/{id}` | Accept or reject reorder suggestion |
| `GET` | `/api/commerce/strategy` | View active commerce strategy |
| `POST` | `/api/commerce/strategy?strategy={ai\|rule-based}` | Switch strategy at runtime without restart |

---

## 🛡️ Resilience & Extensibility

* **Sanity Bounds Validation:** Prices are clamped to valid ranges (`0.2x` to `4.0x`), reorder quantities must be positive integers, and confidence scores are validated.
* **Deterministic Fallback:** Any network interruption or invalid LLM response automatically falls back to `RuleBasedCommerceAdvisor`.
* **Sprint 2 Seams:** `Product.costPrice` is modeled in the JPA schema as a placeholder for margin floor guardrails.