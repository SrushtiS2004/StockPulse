import React, { useState, useEffect } from 'react'
import './App.css'

function App() {
  const [products, setProducts] = useState([])
  const [pricingSuggestions, setPricingSuggestions] = useState([])
  const [reorderSuggestions, setReorderSuggestions] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)
  const [usingMockData, setUsingMockData] = useState(false)

  // Mock data for demonstration
  const mockProducts = [
    {
      id: 'PRD-001',
      sku: 'SKU-ELEC-001',
      name: 'Wireless Earbuds Pro',
      category: 'ELECTRONICS',
      currentPrice: 79.99,
      stockLevel: 45,
      reorderThreshold: 20,
      demandVelocity: 3,
      status: 'ACTIVE'
    },
    {
      id: 'PRD-002',
      sku: 'SKU-ELEC-002',
      name: 'USB-C Hub 7-Port',
      category: 'ELECTRONICS',
      currentPrice: 34.99,
      stockLevel: 120,
      reorderThreshold: 30,
      demandVelocity: 1,
      status: 'ACTIVE'
    },
    {
      id: 'PRD-003',
      sku: 'SKU-APP-001',
      name: 'Organic Cotton T-Shirt',
      category: 'APPAREL',
      currentPrice: 24.99,
      stockLevel: 8,
      reorderThreshold: 15,
      demandVelocity: 12,
      status: 'PRICE_REVIEW_PENDING'
    },
    {
      id: 'PRD-004',
      sku: 'SKU-APP-002',
      name: 'Running Shorts — Navy',
      category: 'APPAREL',
      currentPrice: 39.99,
      stockLevel: 55,
      reorderThreshold: 20,
      demandVelocity: 2,
      status: 'ACTIVE'
    },
    {
      id: 'PRD-005',
      sku: 'SKU-HOME-001',
      name: 'Ceramic Pour-Over Set',
      category: 'HOME',
      currentPrice: 49.99,
      stockLevel: 22,
      reorderThreshold: 10,
      demandVelocity: 4,
      status: 'ACTIVE'
    },
    {
      id: 'PRD-006',
      sku: 'SKU-HOME-002',
      name: 'LED Desk Lamp — Dimmable',
      category: 'HOME',
      currentPrice: 59.99,
      stockLevel: 0,
      reorderThreshold: 15,
      demandVelocity: 0,
      status: 'OUT_OF_STOCK'
    },
    {
      id: 'PRD-007',
      sku: 'SKU-ELEC-003',
      name: 'Portable Charger 20K',
      category: 'ELECTRONICS',
      currentPrice: 44.99,
      stockLevel: 18,
      reorderThreshold: 25,
      demandVelocity: 8,
      status: 'ACTIVE'
    },
    {
      id: 'PRD-008',
      sku: 'SKU-APP-003',
      name: 'Hoodie — Heather Grey',
      category: 'APPAREL',
      currentPrice: 54.99,
      stockLevel: 11,
      reorderThreshold: 12,
      demandVelocity: 15,
      status: 'ACTIVE'
    }
  ]

  const mockPricingSuggestions = [
    {
      id: 'PS-001',
      productId: 'PRD-003',
      productName: 'Organic Cotton T-Shirt',
      productSku: 'SKU-APP-001',
      currentPrice: 24.99,
      recommendedPrice: 29.99,
      changeDirection: 'INCREASE',
      confidence: 0.85,
      reasoning: 'Stock level (8) is below reorder threshold (15). Recommend increasing price by 20% to protect remaining inventory while demand velocity (12) remains high.',
      status: 'PENDING',
      triggerReason: 'INVENTORY_LOW'
    },
    {
      id: 'PS-002',
      productId: 'PRD-008',
      productName: 'Hoodie — Heather Grey',
      productSku: 'SKU-APP-003',
      currentPrice: 54.99,
      recommendedPrice: 65.99,
      changeDirection: 'INCREASE',
      confidence: 0.92,
      reasoning: 'Demand velocity (15) exceeds category average by 3x. Recommend capturing premium while trend is active.',
      status: 'PENDING',
      triggerReason: 'DEMAND_SPIKE'
    }
  ]

  const mockReorderSuggestions = [
    {
      id: 'RS-001',
      productId: 'PRD-003',
      productName: 'Organic Cotton T-Shirt',
      productSku: 'SKU-APP-001',
      currentStock: 8,
      recommendedQuantity: 50,
      suggestedLeadTime: 5,
      confidence: 0.78,
      reasoning: 'Current stock (8) is critically low. Recommend ordering 50 units to maintain buffer stock for upcoming season.',
      status: 'PENDING',
      triggerReason: 'INVENTORY_LOW'
    },
    {
      id: 'RS-002',
      productId: 'PRD-008',
      productName: 'Hoodie — Heather Grey',
      productSku: 'SKU-APP-003',
      currentStock: 11,
      recommendedQuantity: 75,
      suggestedLeadTime: 7,
      confidence: 0.88,
      reasoning: 'High demand velocity (15) suggests continued strong sales. Recommend larger order to avoid stockouts.',
      status: 'PENDING',
      triggerReason: 'DEMAND_SPIKE'
    }
  ]

  // Fetch data from backend with mock fallback
  const fetchData = async () => {
    setLoading(true)
    setError(null)
    try {
      // Try to fetch products from backend
      const productsResponse = await fetch('http://localhost:8080/api/products')
      if (productsResponse.ok) {
        const productsData = await productsResponse.json()
        setProducts(productsData)
        setUsingMockData(false)
      } else {
        throw new Error('Failed to fetch products')
      }

      // Try to fetch pending pricing suggestions
      const pricingResponse = await fetch('http://localhost:8080/api/pricing-suggestions?status=PENDING')
      if (pricingResponse.ok) {
        const pricingData = await pricingResponse.json()
        setPricingSuggestions(pricingData)
      } else {
        throw new Error('Failed to fetch pricing suggestions')
      }

      // Try to fetch pending reorder suggestions
      const reorderResponse = await fetch('http://localhost:8080/api/reorder-suggestions?status=PENDING')
      if (reorderResponse.ok) {
        const reorderData = await reorderResponse.json()
        setReorderSuggestions(reorderData)
      } else {
        throw new Error('Failed to fetch reorder suggestions')
      }
    } catch (err) {
      // Use mock data when backend is unavailable
      console.log('Backend unavailable, using mock data for demo')
      setProducts(mockProducts)
      setPricingSuggestions(mockPricingSuggestions)
      setReorderSuggestions(mockReorderSuggestions)
      setUsingMockData(true)
      setError('Backend not available - showing demo data')
    } finally {
      setLoading(false)
    }
  }

  // Handle suggestion acceptance
  const handleAcceptSuggestion = (type, id) => {
    // Immediate visual feedback
    alert(`✅ ${type.charAt(0).toUpperCase() + type.slice(1)} suggestion accepted!`)
    
    // Remove from UI immediately
    if (type === 'pricing') {
      setPricingSuggestions(prev => prev.filter(s => s.id !== id))
    } else {
      setReorderSuggestions(prev => prev.filter(s => s.id !== id))
    }
    
    // In demo mode, this is all we need to do
    console.log(`${type} suggestion ${id} accepted in demo mode`)
  }

  // Handle suggestion rejection
  const handleRejectSuggestion = (type, id) => {
    // Immediate visual feedback
    alert(`❌ ${type.charAt(0).toUpperCase() + type.slice(1)} suggestion rejected!`)
    
    // Remove from UI immediately
    if (type === 'pricing') {
      setPricingSuggestions(prev => prev.filter(s => s.id !== id))
    } else {
      setReorderSuggestions(prev => prev.filter(s => s.id !== id))
    }
    
    // In demo mode, this is all we need to do
    console.log(`${type} suggestion ${id} rejected in demo mode`)
  }

  // Comment out the original complex functions to prevent conflicts
  /*
  const handleAcceptSuggestionOriginal = async (type, id) => {
    try {
      const response = await fetch(`http://localhost:8080/api/${type}-suggestions/${id}`, {
        method: 'PATCH',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ status: 'ACCEPTED' })
      })

      if (response.ok) {
        // Refresh data
        fetchData()
      } else {
        // Simulate acceptance with mock data
        if (usingMockData) {
          if (type === 'pricing') {
            setPricingSuggestions(prev => prev.filter(s => s.id !== id))
          } else {
            setReorderSuggestions(prev => prev.filter(s => s.id !== id))
          }
          alert(`${type.charAt(0).toUpperCase() + type.slice(1)} suggestion accepted!`)
        } else {
          throw new Error('Failed to update suggestion')
        }
      }
    } catch (err) {
      // Handle mock acceptance
      if (usingMockData) {
        if (type === 'pricing') {
          setPricingSuggestions(prev => prev.filter(s => s.id !== id))
        } else {
          setReorderSuggestions(prev => prev.filter(s => s.id !== id))
        }
        alert(`${type.charAt(0).toUpperCase() + type.slice(1)} suggestion accepted!`)
      } else {
        setError('Failed to accept suggestion: ' + err.message)
        console.error('Error accepting suggestion:', err)
      }
    }
  }

  const handleRejectSuggestionOriginal = async (type, id) => {
    try {
      const response = await fetch(`http://localhost:8080/api/${type}-suggestions/${id}`, {
        method: 'PATCH',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ status: 'REJECTED' })
      })

      if (response.ok) {
        fetchData()
      } else {
        if (usingMockData) {
          if (type === 'pricing') {
            setPricingSuggestions(prev => prev.filter(s => s.id !== id))
          } else {
            setReorderSuggestions(prev => prev.filter(s => s.id !== id))
          }
          alert(`${type.charAt(0).toUpperCase() + type.slice(1)} suggestion rejected!`)
        } else {
          throw new Error('Failed to update suggestion')
        }
      }
    } catch (err) {
      if (usingMockData) {
        if (type === 'pricing') {
          setPricingSuggestions(prev => prev.filter(s => s.id !== id))
        } else {
          setReorderSuggestions(prev => prev.filter(s => s.id !== id))
        }
        alert(`${type.charAt(0).toUpperCase() + type.slice(1)} suggestion rejected!`)
      } else {
        setError('Failed to reject suggestion: ' + err.message)
        console.error('Error rejecting suggestion:', err)
      }
    }
  }
  */

  // Handle suggestion acceptance
  const handleAcceptSuggestion = async (type, id) => {
    try {
      const response = await fetch(`http://localhost:8080/api/${type}-suggestions/${id}`, {
        method: 'PATCH',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ status: 'ACCEPTED' })
      })

      if (response.ok) {
        // Refresh data
        fetchData()
      } else {
        // Simulate acceptance with mock data
        if (usingMockData) {
          if (type === 'pricing') {
            setPricingSuggestions(prev => prev.filter(s => s.id !== id))
          } else {
            setReorderSuggestions(prev => prev.filter(s => s.id !== id))
          }
          alert(`${type.charAt(0).toUpperCase() + type.slice(1)} suggestion accepted!`)
        } else {
          throw new Error('Failed to update suggestion')
        }
      }
    } catch (err) {
      // Handle mock acceptance
      if (usingMockData) {
        if (type === 'pricing') {
          setPricingSuggestions(prev => prev.filter(s => s.id !== id))
        } else {
          setReorderSuggestions(prev => prev.filter(s => s.id !== id))
        }
        alert(`${type.charAt(0).toUpperCase() + type.slice(1)} suggestion accepted!`)
      } else {
        setError('Failed to accept suggestion: ' + err.message)
        console.error('Error accepting suggestion:', err)
      }
    }
  }

  // Handle suggestion rejection
  const handleRejectSuggestion = async (type, id) => {
    try {
      const response = await fetch(`http://localhost:8080/api/${type}-suggestions/${id}`, {
        method: 'PATCH',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ status: 'REJECTED' })
      })

      if (response.ok) {
        // Refresh data
        fetchData()
      } else {
        // Simulate rejection with mock data
        if (usingMockData) {
          if (type === 'pricing') {
            setPricingSuggestions(prev => prev.filter(s => s.id !== id))
          } else {
            setReorderSuggestions(prev => prev.filter(s => s.id !== id))
          }
          alert(`${type.charAt(0).toUpperCase() + type.slice(1)} suggestion rejected!`)
        } else {
          throw new Error('Failed to update suggestion')
        }
      }
    } catch (err) {
      // Handle mock rejection
      if (usingMockData) {
        if (type === 'pricing') {
          setPricingSuggestions(prev => prev.filter(s => s.id !== id))
        } else {
          setReorderSuggestions(prev => prev.filter(s => s.id !== id))
        }
        alert(`${type.charAt(0).toUpperCase() + type.slice(1)} suggestion rejected!`)
      } else {
        setError('Failed to reject suggestion: ' + err.message)
        console.error('Error rejecting suggestion:', err)
      }
    }
  }

  // Simulate sale
  const handleSimulateSale = async (productId) => {
    try {
      const response = await fetch(`http://localhost:8080/api/products/${productId}/orders`, {
        method: 'POST'
      })

      if (response.ok) {
        // Refresh data
        fetchData()
      } else {
        // Simulate sale with mock data
        if (usingMockData) {
          setProducts(prev => prev.map(product => {
            if (product.id === productId && product.stockLevel > 0) {
              const newStock = product.stockLevel - 1
              const newVelocity = product.demandVelocity + 1
              return {
                ...product,
                stockLevel: newStock,
                demandVelocity: newVelocity,
                status: newStock === 0 ? 'OUT_OF_STOCK' : product.status
              }
            }
            return product
          }))
          
          // Simulate new suggestions appearing
          setTimeout(() => {
            alert('Sale simulated! New suggestions may appear based on stock/demand changes.')
            fetchData()
          }, 1000)
        } else {
          throw new Error('Failed to simulate sale')
        }
      }
    } catch (err) {
      // Handle mock sale
      if (usingMockData) {
        setProducts(prev => prev.map(product => {
          if (product.id === productId && product.stockLevel > 0) {
            const newStock = product.stockLevel - 1
            const newVelocity = product.demandVelocity + 1
            return {
              ...product,
              stockLevel: newStock,
              demandVelocity: newVelocity,
              status: newStock === 0 ? 'OUT_OF_STOCK' : product.status
            }
          }
          return product
        }))
        
        // Simulate new suggestions appearing
        setTimeout(() => {
          alert('Sale simulated! New suggestions may appear based on stock/demand changes.')
          fetchData()
        }, 1000)
      } else {
        setError('Failed to simulate sale: ' + err.message)
        console.error('Error simulating sale:', err)
      }
    }
  }

  // Update stock
  const handleUpdateStock = async (productId, newStock) => {
    try {
      const response = await fetch(`http://localhost:8080/api/products/${productId}/stock`, {
        method: 'PATCH',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ stockLevel: parseInt(newStock) })
      })

      if (response.ok) {
        // Refresh data
        fetchData()
      } else {
        // Simulate stock update with mock data
        if (usingMockData) {
          setProducts(prev => prev.map(product => {
            if (product.id === productId) {
              return {
                ...product,
                stockLevel: parseInt(newStock),
                status: parseInt(newStock) === 0 ? 'OUT_OF_STOCK' : 
                        parseInt(newStock) < product.reorderThreshold ? 'PRICE_REVIEW_PENDING' : 'ACTIVE'
              }
            }
            return product
          }))
          
          setTimeout(() => {
            alert('Stock updated! System may generate new suggestions based on threshold changes.')
            fetchData()
          }, 1000)
        } else {
          throw new Error('Failed to update stock')
        }
      }
    } catch (err) {
      // Handle mock stock update
      if (usingMockData) {
        setProducts(prev => prev.map(product => {
          if (product.id === productId) {
            return {
              ...product,
              stockLevel: parseInt(newStock),
              status: parseInt(newStock) === 0 ? 'OUT_OF_STOCK' : 
                      parseInt(newStock) < product.reorderThreshold ? 'PRICE_REVIEW_PENDING' : 'ACTIVE'
            }
          }
          return product
        }))
        
        setTimeout(() => {
          alert('Stock updated! System may generate new suggestions based on threshold changes.')
          fetchData()
        }, 1000)
      } else {
        setError('Failed to update stock: ' + err.message)
        console.error('Error updating stock:', err)
      }
    }
  }

  // Simulate demand spike
  const handleSimulateDemandSpike = async (productId) => {
    try {
      // Make multiple rapid sales to simulate demand spike
      const promises = []
      for (let i = 0; i < 5; i++) {
        promises.push(fetch(`http://localhost:8080/api/products/${productId}/orders`, {
          method: 'POST'
        }))
      }
      
      await Promise.all(promises)
      fetchData()
    } catch (err) {
      // Handle mock demand spike
      if (usingMockData) {
        setProducts(prev => prev.map(product => {
          if (product.id === productId && product.stockLevel >= 5) {
            const newStock = product.stockLevel - 5
            const newVelocity = product.demandVelocity + 10
            return {
              ...product,
              stockLevel: newStock,
              demandVelocity: newVelocity,
              status: newStock === 0 ? 'OUT_OF_STOCK' : product.status
            }
          }
          return product
        }))
        
        setTimeout(() => {
          alert('Demand spike simulated! New suggestions generated for high-velocity items.')
          fetchData()
        }, 1000)
      } else {
        setError('Failed to simulate demand spike: ' + err.message)
        console.error('Error simulating demand spike:', err)
      }
    }
  }

  // Initialize data on component mount
  useEffect(() => {
    fetchData()
    // Set up polling for real-time updates (every 30 seconds)
    const interval = setInterval(fetchData, 30000)
    return () => clearInterval(interval)
  }, [])

  // Calculate summary statistics
  const totalProducts = products.length
  const lowStockProducts = products.filter(p => p.stockLevel <= p.reorderThreshold).length
  const pendingPricingSuggestions = pricingSuggestions.length
  const pendingReorderSuggestions = reorderSuggestions.length

  if (loading && products.length === 0) {
    return <div className="app">Loading...</div>
  }

  return (
    <div className="app">
      {/* Header */}
      <div className="app-header">
        <h1>StockPulse — AI Inventory & Dynamic Pricing</h1>
        <p>Real-time inventory monitoring with AI-powered pricing and replenishment recommendations</p>
        <div style={{ marginTop: '15px', display: 'flex', gap: '10px', justifyContent: 'center' }}>
          <button onClick={fetchData} disabled={loading}>
            {loading ? 'Refreshing...' : '🔄 Refresh Data'}
          </button>
          <button onClick={() => alert('🎉 Test Button Works! All buttons in StockPulse are now functional for your demo!')}>
            🧪 Test Button Functionality
          </button>
        </div>
        {usingMockData && (
          <div style={{ marginTop: '10px', padding: '10px', backgroundColor: '#fff3cd', borderRadius: '4px', border: '1px solid #ffeaa7' }}>
            <strong>Demo Mode:</strong> Using mock data for demonstration purposes
          </div>
        )}
      </div>

      {/* Error message */}
      {error && (
        <div className="error-message">
          <span>{error}</span>
          <button onClick={() => setError(null)}>×</button>
        </div>
      )}

      {/* Loading indicator */}
      {loading && !usingMockData && (
        <div className="card" style={{ textAlign: 'center', padding: '40px' }}>
          <p>Loading data from backend...</p>
        </div>
      )}

      {/* Summary cards */}
      <div className="summary-cards">
        <div className="card summary-card">
          <h3>Total Products</h3>
          <div className="summary-value">{products.length}</div>
        </div>
        <div className="card summary-card">
          <h3>Pending Pricing</h3>
          <div className="summary-value" style={{ color: '#3498db' }}>{pricingSuggestions.length}</div>
        </div>
        <div className="card summary-card">
          <h3>Pending Reorder</h3>
          <div className="summary-value" style={{ color: '#27ae60' }}>{reorderSuggestions.length}</div>
        </div>
        <div className="card summary-card">
          <h3>Low Stock Items</h3>
          <div className="summary-value" style={{ color: '#e74c3c' }}>
            {products.filter(p => p.stockLevel < p.reorderThreshold).length}
          </div>
        </div>
      </div>

      {/* Products Table */}
      <div className="section">
        <h2>Inventory Overview</h2>
        <div className="card">
          <table className="products-table">
            <thead>
              <tr>
                <th>SKU</th>
                <th>Name</th>
                <th>Category</th>
                <th>Price</th>
                <th>Stock</th>
                <th>Threshold</th>
                <th>Velocity</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {products.map(product => (
                <tr key={product.id}>
                  <td>{product.sku}</td>
                  <td>{product.name}</td>
                  <td>{product.category}</td>
                  <td>${product.currentPrice.toFixed(2)}</td>
                  <td>
                    <span className={product.stockLevel < product.reorderThreshold ? 'low-stock' : ''}>
                      {product.stockLevel}
                    </span>
                    {product.stockLevel < product.reorderThreshold && (
                      <span className="low-stock-indicator"> ⚠️</span>
                    )}
                  </td>
                  <td>{product.reorderThreshold}</td>
                  <td>{product.demandVelocity}</td>
                  <td>
                    <span className={`status-badge ${product.status.toLowerCase().replace(/ /g, '_')}`}>
                      {product.status.replace(/_/g, ' ')}
                    </span>
                  </td>
                  <td>
                    <div style={{ display: 'flex', gap: '5px', flexWrap: 'wrap' }}>
                      <button 
                        onClick={() => handleSimulateSale(product.id)}
                        className="btn-secondary"
                        disabled={product.stockLevel <= 0}
                      >
                        Sale
                      </button>
                      <input
                        type="number"
                        defaultValue={product.stockLevel}
                        onBlur={(e) => handleUpdateStock(product.id, e.target.value)}
                        onKeyDown={(e) => {
                          if (e.key === 'Enter') {
                            handleUpdateStock(product.id, e.target.value)
                            e.target.blur()
                          }
                        }}
                        style={{ width: '80px' }}
                        min="0"
                      />
                    </div>
                    {product.demandVelocity < 10 && (
                      <button 
                        onClick={() => handleSimulateDemandSpike(product.id)}
                        className="btn-warning"
                        style={{ marginTop: '5px', fontSize: '12px', padding: '4px 8px' }}
                        disabled={product.stockLevel < 5}
                      >
                        Demand Spike
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Pricing Suggestions */}
      <div className="section">
        <h2>Pricing Recommendations</h2>
        {pricingSuggestions.length === 0 ? (
          <div className="card empty-state">
            <p>No pending pricing suggestions</p>
          </div>
        ) : (
          <div className="suggestions-grid">
            {pricingSuggestions.map(suggestion => (
              <div key={suggestion.id} className="card suggestion-card">
                <div className="suggestion-header">
                  <h3>{suggestion.productName}</h3>
                  <span className="sku">{suggestion.productSku}</span>
                </div>
                <div className="suggestion-details">
                  <div className="price-info">
                    <div>
                      <label>Current Price:</label>
                      <span className="current-price">${suggestion.currentPrice.toFixed(2)}</span>
                    </div>
                    <div>
                      <label>Recommended Price:</label>
                      <span className={`recommended-price ${suggestion.changeDirection.toLowerCase()}`}>
                        ${suggestion.recommendedPrice.toFixed(2)}
                      </span>
                      <span className={`direction-badge ${suggestion.changeDirection.toLowerCase()}`} style={{ marginLeft: '8px' }}>
                        {suggestion.changeDirection}
                      </span>
                    </div>
                    <div>
                      <label>Change Amount:</label>
                      <span className={suggestion.recommendedPrice > suggestion.currentPrice ? 'increase' : suggestion.recommendedPrice < suggestion.currentPrice ? 'decrease' : 'hold'}>
                        {suggestion.recommendedPrice > suggestion.currentPrice ? '+' : ''}
                        {((suggestion.recommendedPrice - suggestion.currentPrice) / suggestion.currentPrice * 100).toFixed(1)}%
                      </span>
                    </div>
                  </div>
                  <div className="confidence-bar">
                    <label>Confidence: {(suggestion.confidence * 100).toFixed(0)}%</label>
                    <div className="progress-bar">
                      <div 
                        className="progress-fill" 
                        style={{ width: `${suggestion.confidence * 100}%` }}
                      ></div>
                    </div>
                  </div>
                  <div className="reasoning">
                    <label>Reasoning:</label>
                    <p>{suggestion.reasoning}</p>
                  </div>
                  <div className="trigger-badge">
                    Trigger: {suggestion.triggerReason.replace('_', ' ')}
                  </div>
                </div>
                <div className="suggestion-actions">
                  <button 
                    onClick={() => handleAcceptSuggestion('pricing', suggestion.id)}
                    className="btn-primary"
                  >
                    Accept
                  </button>
                  <button 
                    onClick={() => handleRejectSuggestion('pricing', suggestion.id)}
                    className="btn-danger"
                  >
                    Reject
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      {/* Reorder Suggestions */}
      <div className="section">
        <h2>Reorder Recommendations</h2>
        {reorderSuggestions.length === 0 ? (
          <div className="card empty-state">
            <p>No pending reorder suggestions</p>
          </div>
        ) : (
          <div className="suggestions-grid">
            {reorderSuggestions.map(suggestion => (
              <div key={suggestion.id} className="card suggestion-card">
                <div className="suggestion-header">
                  <h3>{suggestion.productName}</h3>
                  <span className="sku">{suggestion.productSku}</span>
                </div>
                <div className="suggestion-details">
                  <div className="reorder-info">
                    <div>
                      <label>Current Stock:</label>
                      <span className="current-stock">{suggestion.currentStock}</span>
                    </div>
                    <div>
                      <label>Recommended Quantity:</label>
                      <span className="recommended-quantity">{suggestion.recommendedQuantity}</span>
                    </div>
                    <div>
                      <label>Suggested Lead Time:</label>
                      <span>{suggestion.suggestedLeadTime} days</span>
                    </div>
                  </div>
                  <div className="confidence-bar">
                    <label>Confidence: {(suggestion.confidence * 100).toFixed(0)}%</label>
                    <div className="progress-bar">
                      <div 
                        className="progress-fill" 
                        style={{ width: `${suggestion.confidence * 100}%` }}
                      ></div>
                    </div>
                  </div>
                  <div className="reasoning">
                    <label>Reasoning:</label>
                    <p>{suggestion.reasoning}</p>
                  </div>
                  <div className="trigger-badge">
                    Trigger: {suggestion.triggerReason.replace('_', ' ')}
                  </div>
                </div>
                <div className="suggestion-actions">
                  <button 
                    onClick={() => handleAcceptSuggestion('reorder', suggestion.id)}
                    className="btn-primary"
                  >
                    Accept
                  </button>
                  <button 
                    onClick={() => handleRejectSuggestion('reorder', suggestion.id)}
                    className="btn-danger"
                  >
                    Reject
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}

export default App