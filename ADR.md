# Architecture Decision Records (ADR) — StockPulse

**Project:** StockPulse — AI Inventory & Dynamic Pricing Engine  
**System:** ShopStream Reactive Commerce Advisor  
**Author:** Solo Hackathon Submission  
**Status:** Approved & Implemented  

---

## 1. Placement of Commerce Logic

### Context
ShopStream requires an automated system to recommend price adjustments and reorder quantities when stock reaches critical thresholds or demand velocities surge. We needed to establish where commerce logic lives to prevent `ProductService` or the `Product` entity from accumulating excessive responsibilities (pricing algorithms, inventory math, event publishing, LLM interaction, and persistence).

### Options Considered
1. **Rich Domain Entity (Domain-Driven Design):** Embed pricing algorithms directly inside `Product.java`.
2. **Fat Service Layer:** Implement pricing and reorder logic directly inside `ProductService.java`.
3. **Dedicated Strategy Components with Service Orchestration (Selected):** Isolate commerce calculations into dedicated `CommerceAdvisor` implementations (`RuleBasedCommerceAdvisor`, `AICommerceAdvisor`), orchestrated via an `AdvisorRegistry`.

### Decision
We selected **Option 3**. `Product` remains an entity maintaining state and lifecycle invariants (`ACTIVE`, `PRICE_REVIEW_PENDING`, `OUT_OF_STOCK`). `ProductService` orchestrates catalog changes, persistence, and event dispatch. All commerce intelligence is encapsulated in `CommerceAdvisor` implementations behind a clean, strategy-based contract.

### Tradeoffs
* **Pros:** Strict Single Responsibility Principle (SRP). Strategy implementations can be unit-tested without database mocks. New strategies (e.g. Sprint 2 `CompetitorAwareStrategy`) can be added without modifying existing service code.
* **Cons:** Introduces extra interface abstractions and registry lookups compared to inline calculations.

---

## 2. Paired Advisory Contracts & Differentiated Prompts

### Context
Pricing and replenishment are inextricably linked in retail merchandising: raising a price slows depletion while waiting for inventory replenishment, while high order velocity necessitates larger reorder quantities. We had to decide whether to bundle both outputs into a single unified LLM prompt/call or split them into distinct contracts.

### Options Considered
1. **Single Monolithic Prompt/Call:** A single prompt returning both pricing and reorder quantities in one JSON object.
2. **Completely Disconnected Interfaces:** Independent `PricingEngine` and `ReplenishmentEngine` with no shared context or registry.
3. **Cohesive Strategy Interface with Differentiated Prompts (Selected):** A single `CommerceAdvisor` contract exposing `generatePricingSuggestion(Product, TriggerReason)` and `generateReorderSuggestion(Product, TriggerReason)`, utilizing trigger-tailored prompts.

### Decision
We chose **Option 3**. Both functions are grouped under `CommerceAdvisor`, allowing callers (HTTP endpoints and async event handlers) to interact with a unified advisor. However, internally the prompts are **differentiated by trigger context**:
* **`INVENTORY_LOW` Prompt:** Focuses on scarcity protection vs. clearance tradeoffs. Directs the model to evaluate whether to increase price by 5%–20% to conserve remaining stock or hold/discount.
* **`DEMAND_SPIKE` Prompt:** Focuses on capturing consumer surplus during viral momentum (5%–15% increase) without killing conversion velocity.
* **`MANUAL` Prompt:** Balanced margin optimization.

### Tradeoffs
* **Pros:** High prompt relevance; prevents confusing the LLM with contradictory instructions. Independent fallback handling (e.g., if pricing LLM fails, reorder can still succeed or fall back gracefully).
* **Cons:** Results in two distinct API calls during full agentic execution rather than a single batched call.

---

## 3. Runtime Strategy Switching without Restart

### Context
Merchandising teams must be able to switch between the deterministic `RuleBasedCommerceAdvisor` (e.g., during LLM maintenance or high traffic) and `AICommerceAdvisor` dynamically without restarting the application or redeploying code.

### Options Considered
1. **Compile-time / Profile-based Binding:** Use Spring `@Profile("ai")` requiring container restart.
2. **In-Memory Dynamic Registry with HTTP/Config Binding (Selected):** Maintain a thread-safe `ConcurrentHashMap` of registered `CommerceAdvisor` beans inside `AdvisorRegistry`, switchable via `POST /api/commerce/strategy?strategy=...` or application properties.

### Decision
We implemented **Option 2**. `AdvisorRegistry` registers all available advisors (`"rule-based"`, `"ai"`) at startup. The active advisor reference is volatile and thread-safe. Both the HTTP controllers and the background `@Async` event handlers always call `advisorRegistry.getActiveAdvisor()`, guaranteeing immediate, zero-downtime strategy switching across the entire system.

### Tradeoffs
* **Pros:** Zero downtime. Immediate feedback during demos and operational switchovers. Compatible with both UI toggle controls and automated circuit-breaker fallbacks.
* **Cons:** Runtime strategy state must be coordinated if scaled horizontally across multiple instances (in Sprint 3, this would be backed by Redis or Consul).

---

## 4. LLM Failure Handling & Resilience

### Context
External LLM gateways are prone to network timeouts, rate-limit quotas, unparseable markdown formatting, or hallucinated outputs (e.g. negative prices or $999,999). A commercial commerce engine must never fail silently or drop recommendations.

### Options Considered
1. **Fail-Fast (Throw 500 / Log Error):** Abort recommendation generation on LLM failure.
2. **Silent Retry Loop:** Repeatedly retry LLM calls until a response is obtained.
3. **Strict Bounds Validation with Deterministic Rule-Based Fallback (Selected):** Connect with strict read/connect timeouts (6s/12s), strip markdown syntax wrappers, validate sanity bounds, and automatically delegate to `RuleBasedCommerceAdvisor` on any exception.

### Decision
We implemented **Option 3**. 
* **Sanity Bounds:** Recommended prices must be positive and fall within `0.2x` to `4.0x` of the current price; reorder quantities must be positive integers between `1` and `50,000`; confidence scores must be in `[0.0, 1.0]`.
* **Graceful Degradation:** If the LLM gateway fails, times out, or returns malformed JSON, `AICommerceAdvisor` immediately logs a warning and returns `fallbackAdvisor.generatePricingSuggestion(product, triggerReason)`. The agentic loop never drops a recommendation.

### Tradeoffs
* **Pros:** 100% system availability. Async event loops never stall. Human merchandisers always receive actionable proposals.
* **Cons:** If LLM is down, recommendations temporarily revert to formulaic percentages (+10% / +5%) rather than deep linguistic reasoning.

---

## 5. Agentic Loop Decoupling & Idempotency

### Context
Inventory sales (`POST /products/{id}/orders`) and stock updates (`PATCH /products/{id}/stock`) occur in high-frequency request paths. Generating LLM recommendations synchronously would block HTTP threads and degrade user experience. Furthermore, rapid consecutive sales must not spawn duplicate pending suggestions.

### Options Considered
1. **Scheduled Poller (Cron Job):** A background timer checking for low-stock products every N minutes.
2. **Synchronous In-Controller Generation:** Compute suggestions inside `ProductController` before returning response.
3. **Event-Driven Decoupled Loop with Repository Idempotency (Selected):** Fire `ProductChangeEvent` via Spring's `ApplicationEventPublisher`. The order endpoint returns immediately (200 OK). An `@Async` event handler processes suggestions in background threads. Duplicate `PENDING` suggestions are suppressed.

### Decision
We implemented **Option 3**.
* **Decoupling:** `ProductService.recordSale(id)` updates stock and velocity and dispatches `ProductChangeEvent` asynchronously.
* **Idempotency:** Before generating a suggestion, `SuggestionService` queries `findByProductAndStatusAndTriggerReason(product, SuggestionStatus.PENDING, triggerReason)`. If a suggestion is already awaiting human approval for that trigger, duplicate generation is skipped.
* **Agentic Checkpoint:** Suggestions remain in `PENDING` status until a human merchandiser explicitly calls `PATCH /pricing-suggestions/{id}` with `ACCEPTED` (which updates `Product.currentPrice` atomically) or `REJECTED`.

### Tradeoffs
* **Pros:** Blazing fast order response times. Clean separation between transaction commit and background reasoning. UI updates seamlessly via reactive polling.
* **Cons:** Eventual consistency — recommendations appear a few hundred milliseconds after the order event.

---

## 6. Extensibility Seams & Deliberate Exclusions

### Context
Designing for future phases (Sprint 2 & 3) while keeping Sprint 1 focused on the reactive inventory-signal → AI recommendation → human approval loop.

### Extensibility Seams Implemented in Active Code
1. **`Product.costPrice` (Sprint 2 Seam):** Present as a nullable column in `Product.java`. Provides the immediate foundation for margin floor validation (`recommendedPrice >= costPrice * (1 + marginFloor)`).
2. **Category Velocity Aggregation (`ProductRepository.findAverageDemandVelocityByCategory`):** Enables category-level pricing caps and peer benchmarking.
3. **Pluggable `CommerceAdvisor` Interface:** Registering a future `CompetitorAwareStrategy` (using scraped competitor market data) requires only implementing `CommerceAdvisor` and registering it in `AdvisorRegistry`.

### Deliberately Deferred Features (Prioritization Rationale)
* **Full Customer Shopping Cart & Payment Gateway:** Out of scope; focused on merchant decision intelligence.
* **Automated Purchase Order (PO) Transmission to Supplier EDI:** High operational risk for Sprint 1; approval currently simulates inbound stock reception.
* **Multi-tenant Role-Based Access Control (RBAC):** Merchandiser console is scoped to single-tenant operations for rapid prototype validation.