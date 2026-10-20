# Architectural Decision Records (ADRs)

## 1. Placement of Commerce Logic

**Context**: We needed to decide where to implement the core commerce advisory logic for pricing and reorder recommendations.

**Options**:
- Monolithic service with all logic combined
- Separate strategy interfaces with multiple implementations
- Rule engine approach
- Microservices architecture

**Decision**: We chose separate strategy interfaces with multiple implementations. This provides clear separation of concerns and allows for easy swapping between rule-based and AI-based approaches.

**Tradeoffs**:
- **Pros**: Clean separation, easy testing, runtime switchability, extensible
- **Cons**: Slight complexity overhead, potential duplication across strategies

## 2. Unified versus Separate Pricing/Reorder Contracts

**Context**: Whether to use unified or separate data structures for pricing and reorder suggestions.

**Options**:
- Single generic suggestion entity
- Separate entities with common base
- Completely separate with no shared structure

**Decision**: We chose separate entities with distinct purposes. While they share some common fields (status, confidence), they have fundamentally different business meanings and data requirements.

**Tradeoffs**:
- **Pros**: Clear domain modeling, type safety, specialized validation, easier maintenance
- **Cons**: Some code duplication, slightly more complex querying logic

## 3. Runtime Strategy Switching

**Context**: How to allow switching between rule-based and AI strategies at runtime without restarting the application.

**Options**:
- Environment variable requiring restart
- Configuration file reload
- In-memory registry with programmatic switching
- Database-driven configuration

**Decision**: We implemented an in-memory advisor registry that can switch strategies programmatically and responds to configuration changes.

**Tradeoffs**:
- **Pros**: Immediate effect, no downtime, simple implementation
- **Cons**: Changes don't persist across restarts, limited to predefined strategies

## 4. LLM Validation and Fallback

**Context**: How to handle AI service failures and ensure robust operation.

**Options**:
- Fail-fast approach (return errors)
- Silent fallback to rules
- Explicit fallback with logging
- Retry mechanisms

**Decision**: We implemented explicit validation with graceful fallback to rule-based strategies when AI services fail or return invalid responses.

**Tradeoffs**:
- **Pros**: Robust operation, clear error handling, maintains business continuity
- **Cons**: Additional complexity, potential performance impact from validation

## 5. Async Event-Driven Triggers and Duplicate Prevention

**Context**: How to handle automatic suggestion generation when product states change, preventing duplicate suggestions.

**Options**:
- Synchronous processing in API calls
- Async event processing with duplication checks
- Database-level constraints only
- In-memory tracking

**Decision**: We chose async event-driven processing with database-level duplicate prevention using unique constraints on (product, trigger_reason, status=PENDING).

**Tradeoffs**:
- **Pros**: Non-blocking operations, prevents duplicates, scalable
- **Cons**: Eventual consistency, more complex debugging, requires careful transaction management

## 6. Human Approval and State Transitions

**Context**: How to manage the lifecycle of suggestions through human review.

**Options**:
- Direct database updates
- Service-layer state management
- State machine pattern
- Simple conditional logic

**Decision**: We used service-layer state management with explicit transitions, ensuring that acceptance actions automatically apply the suggested changes to products.

**Tradeoffs**:
- **Pros**: Clear business logic, atomic operations, audit trail
- **Cons**: More complex than direct updates, requires careful transaction handling

## 7. Extensibility and Deliberately Deferred Features

**Context**: How to design for future enhancements while meeting current requirements.

**Design Principles Applied**:
- Nullable costPrice field for future margin calculations
- Abstract CommerceAdvisor interface allowing new strategies
- Configurable LLM parameters rather than hardcoded values
- Event-driven architecture supporting new trigger types
- Repository pattern for data access flexibility

**Future Extensions Planned**:
- Competitor-aware pricing through external API integration
- Supplier relationship management with lead time optimization
- Advanced analytics and reporting dashboards
- Multi-channel inventory coordination
- Seasonal and promotional pricing rules