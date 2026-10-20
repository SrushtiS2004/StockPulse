# StockPulse - AI Inventory & Dynamic Pricing Engine

## Demo Setup Instructions

This frontend application demonstrates the StockPulse system with mock data for presentation purposes. It works in two modes:

1. **Connected Mode**: Connects to a running backend API
2. **Demo Mode**: Uses built-in mock data when backend is unavailable

### Running the Demo

1. **Install dependencies**:
   ```bash
   npm install
   ```

2. **Start the development server**:
   ```bash
   npm run dev
   ```

3. **Open in browser**: http://localhost:5173

### Features Demonstrated

- Real-time inventory monitoring
- AI-powered pricing recommendations
- Automated reorder suggestions  
- Interactive product management
- Simulation controls for sales and stock updates

### Mock Data Included

The application includes comprehensive mock data for 8 products covering Electronics, Apparel, and Home categories with realistic pricing suggestions and reorder recommendations.

### Key Demo Paths

1. **Inventory Low Scenario**: PRD-003 (Organic Cotton T-Shirt) shows pricing and reorder suggestions when stock drops below threshold
2. **Demand Spike Scenario**: PRD-008 (Hoodie) demonstrates demand-driven recommendations
3. **Interactive Controls**: Simulate sales, update stock levels, and accept/reject AI suggestions

### Technical Notes

- Built with React 18 + Vite
- Responsive design works on all screen sizes
- No external dependencies required for demo mode
- Clean, production-ready code structure

For the hackathon submission, this frontend can be easily connected to the Spring Boot backend by ensuring the backend runs on port 8080.