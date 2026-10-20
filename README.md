# StockPulse - AI Inventory & Dynamic Pricing Engine

StockPulse is an intelligent inventory management and dynamic pricing system that combines rule-based algorithms with AI-powered recommendations to optimize retail operations.

## 🏗️ Architecture Overview

### Backend (Spring Boot 3.x)
- **Entities**: Product, PricingSuggestion, ReorderSuggestion with proper JPA mappings
- **Services**: ProductService, SuggestionService, CommerceAdvisor strategies
- **Controllers**: RESTful APIs for products and suggestions
- **AI Integration**: Pluggable LLM gateway with fallback to rule-based logic
- **Event System**: Async event-driven recommendation generation
- **Data**: H2 in-memory database with automatic seeding

### Frontend (React 18 + Vite)
- **Dashboard**: Real-time inventory monitoring and suggestion management
- **UI Components**: Responsive cards, tables, and action controls
- **Data Flow**: REST API consumption with error handling and polling

## 🚀 Quick Start

### Prerequisites
- Java 21
- Maven 3.8+
- Node.js 16+
- npm 8+

### Backend Setup

```powershell
cd backend
mvn clean install
mvn spring-boot:run
```

The backend will start on http://localhost:8080 with H2 console available at http://localhost:8080/h2-console

### Frontend Setup

```powershell
cd frontend
npm install
npm run dev
```

The frontend will start on http://localhost:3000 with proxy to backend

## 📊 API Endpoints

### Product Management
- `POST /api/products` - Create new product
- `GET /api/products?status=&category=` - List products with filters
- `PATCH /api/products/{id}/stock` - Update product stock level
- `POST /api/products/{id}/orders` - Record product order (increments demand velocity)

### Suggestion Generation
- `POST /api/products/{id}/suggest-pricing` - Generate pricing suggestion
- `POST /api/products/{id}/suggest-reorder` - Generate reorder suggestion

### Suggestion Management
- `GET /api/pricing-suggestions?status=PENDING` - Get pricing suggestions
- `GET /api/reorder-suggestions?status=PENDING` - Get reorder suggestions
- `PATCH /api/pricing-suggestions/{id}` - Accept/reject pricing suggestion
- `PATCH /api/reorder-suggestions/{id}` - Accept/reject reorder suggestion

## 🛒 5-Minute Demo Instructions

1. **Start the application**:
   ```powershell
   # Terminal 1: Start backend
   cd backend
   mvn spring-boot:run
   
   # Terminal 2: Start frontend
   cd frontend
   npm install
   npm run dev
   ```

2. **View seed data**: Open http://localhost:3000 to see 8 pre-loaded products

3. **Generate suggestions manually**:
   ```bash
   # Generate pricing suggestion for PRD-003 (Organic Cotton T-Shirt)
   curl -X POST http://localhost:8080/api/products/3/suggest-pricing
   
   # Generate reorder suggestion for PRD-007 (Portable Charger 20K)
   curl -X POST http://localhost:8080/api/products/7/suggest-reorder
   ```

4. **Trigger automatic suggestions**:
   - Click "Simulate Sale" on any product multiple times
   - Reduce stock levels below reorder thresholds
   - Observe async-generated suggestions appearing in the dashboard

5. **Manage suggestions**:
   - Accept/reject pricing and reorder suggestions
   - Watch prices and stock levels update accordingly

## 🌱 Seeded Products

| SKU | Name | Category | Price | Stock | Threshold | Velocity |
|-----|------|----------|-------|-------|-----------|----------|
| PRD-001 | Wireless Earbuds Pro | ELECTRONICS | $79.99 | 45 | 20 | 3 |
| PRD-002 | USB-C Hub 7-Port | ELECTRONICS | $34.99 | 120 | 30 | 1 |
| PRD-003 | Organic Cotton T-Shirt | APPAREL | $24.99 | 8 | 15 | 12 |
| PRD-004 | Running Shorts — Navy | APPAREL | $39.99 | 55 | 20 | 2 |
| PRD-005 | Ceramic Pour-Over Set | HOME | $49.99 | 22 | 10 | 4 |
| PRD-006 | LED Desk Lamp — Dimmable | HOME | $59.99 | 0 | 15 | 0 |
| PRD-007 | Portable Charger 20K | ELECTRONICS | $44.99 | 18 | 25 | 8 |
| PRD-008 | Hoodie — Heather Grey | APPAREL | $54.99 | 11 | 12 | 15 |

## ⚙️ Configuration

### Environment Variables
```bash
# LLM Configuration (optional)
LLM_BASE_URL=http://localhost:11434/v1
LLM_API_KEY=your-api-key
LLM_MODEL=gpt-3.5-turbo
LLM_PROVIDER=rule-based  # or "ai" to enable LLM
```

### Rule-Based Logic
- **Pricing**: 10% increase if stock low, 5% increase if high velocity, else HOLD
- **Reordering**: Quantity = max(1, reorderThreshold * 3 - currentStock)

## 🧪 Testing

### Backend Tests
```powershell
cd backend
mvn test
```

### Manual Testing
- All API endpoints can be tested via curl or Postman
- Frontend dashboard provides real-time validation
- H2 console available for database inspection

## 🔧 Known Limitations

1. **AI Integration**: Currently uses rule-based fallback; LLM integration requires valid API credentials
2. **Authentication**: No user authentication/authorization implemented
3. **Persistence**: Data stored in memory (H2); restarts clear all non-seed data
4. **Advanced Filtering**: Limited filtering options in UI (basic status/category only)
5. **Real-time Updates**: UI polls every 30 seconds; no WebSocket push notifications

## 🔄 Future Enhancements

- Competitor price monitoring and matching
- Advanced analytics and forecasting
- Multi-user role-based access control
- Persistent database storage (PostgreSQL/MySQL)
- Mobile-responsive design improvements
- Export/import functionality for bulk operations
- Integration with popular e-commerce platforms