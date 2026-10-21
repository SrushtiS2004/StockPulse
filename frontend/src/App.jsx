import React, { useState, useEffect, useRef } from 'react'
import './App.css'

function App() {
  const [products, setProducts] = useState([])
  const [pricingSuggestions, setPricingSuggestions] = useState([])
  const [reorderSuggestions, setReorderSuggestions] = useState([])
  const [activeStrategy, setActiveStrategy] = useState('ai')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)
  const [categoryFilter, setCategoryFilter] = useState('ALL')
  const [searchQuery, setSearchQuery] = useState('')
  const [activeTab, setActiveTab] = useState('pricing') // 'pricing' | 'reorder'
  const [toast, setToast] = useState(null)
  const [showGuide, setShowGuide] = useState(true)

  // SSE Stream Modal State (Bonus Feature)
  const [streamingProduct, setStreamingProduct] = useState(null)
  const [streamLogs, setStreamLogs] = useState([])
  const [isStreaming, setIsStreaming] = useState(false)
  const eventSourceRef = useRef(null)

  const showToast = (msg) => {
    setToast(msg)
    setTimeout(() => {
      setToast(prev => prev === msg ? null : prev)
    }, 3800)
  }

  // Fetch live state from backend
  const fetchData = async () => {
    try {
      setError(null)
      const [prodRes, priceRes, reorderRes, stratRes] = await Promise.all([
        fetch('/api/products').catch(() => fetch('http://localhost:8081/api/products')),
        fetch('/api/pricing-suggestions?status=PENDING').catch(() => fetch('http://localhost:8081/api/pricing-suggestions?status=PENDING')),
        fetch('/api/reorder-suggestions?status=PENDING').catch(() => fetch('http://localhost:8081/api/reorder-suggestions?status=PENDING')),
        fetch('/api/commerce/strategy').catch(() => fetch('http://localhost:8081/api/commerce/strategy'))
      ])

      if (prodRes && prodRes.ok) {
        const prodData = await prodRes.json()
        setProducts(prodData)
      }
      if (priceRes && priceRes.ok) {
        const priceData = await priceRes.json()
        setPricingSuggestions(priceData)
      }
      if (reorderRes && reorderRes.ok) {
        const reorderData = await reorderRes.json()
        setReorderSuggestions(reorderData)
      }
      if (stratRes && stratRes.ok) {
        const stratData = await stratRes.json()
        if (stratData.activeStrategy) {
          setActiveStrategy(stratData.activeStrategy)
        }
      }
    } catch (err) {
      console.error('Fetch error:', err)
      setError('Connecting to StockPulse Backend on port 8081...')
    } finally {
      setLoading(false)
    }
  }

  // Auto-poll every 3 seconds for reactive agentic loop updates
  useEffect(() => {
    setLoading(true)
    fetchData()
    const interval = setInterval(fetchData, 3000)
    return () => clearInterval(interval)
  }, [])

  // Strategy Switcher
  const handleToggleStrategy = async (strategyName) => {
    try {
      const res = await fetch(`/api/commerce/strategy?strategy=${strategyName}`, { method: 'POST' })
      if (res.ok) {
        setActiveStrategy(strategyName)
        showToast(`Strategy switched to ${strategyName.toUpperCase()}`)
      }
    } catch (e) {
      showToast(`Strategy toggle error: ${e.message}`)
    }
  }

  // Record Sale Simulation
  const handleSimulateSale = async (product) => {
    try {
      const res = await fetch(`/api/products/${product.id}/orders`, { method: 'POST' })
      if (res.ok) {
        showToast(`Sale recorded for ${product.name}! Stock -1, Velocity +1.`)
        await fetchData()
      }
    } catch (e) {
      showToast(`Sale error: ${e.message}`)
    }
  }

  // Rapid Demand Spike Simulation
  const handleSimulateSurge = async (product) => {
    try {
      showToast(`Simulating viral sales spike on ${product.name}...`)
      await Promise.all([
        fetch(`/api/products/${product.id}/orders`, { method: 'POST' }),
        fetch(`/api/products/${product.id}/orders`, { method: 'POST' }),
        fetch(`/api/products/${product.id}/orders`, { method: 'POST' }),
        fetch(`/api/products/${product.id}/orders`, { method: 'POST' })
      ])
      showToast(`Surge triggered! Demand velocity pushed above 3x category average.`)
      await fetchData()
    } catch (e) {
      showToast(`Surge error: ${e.message}`)
    }
  }

  // Reset Demo Catalog to Initial Seed
  const handleResetDemoData = async () => {
    try {
      showToast('Resetting catalog & re-seeding demo baseline...')
      const res = await fetch('/api/products/reset', { method: 'POST' })
      if (res.ok) {
        showToast('Demo catalog reset successfully! 8 fresh SKUs re-initialized.')
        await fetchData()
      }
    } catch (e) {
      showToast(`Reset error: ${e.message}`)
    }
  }

  // On-Demand AI Pricing Analysis
  const handleTriggerPricing = async (product) => {
    try {
      showToast(`Analyzing pricing for ${product.sku} via ${activeStrategy.toUpperCase()} advisor...`)
      const res = await fetch(`/api/products/${product.id}/suggest-pricing`, { method: 'POST' })
      if (res.ok) {
        showToast(`AI pricing generated for ${product.sku}! View in Dynamic Pricing tab.`)
        setActiveTab('pricing')
        await fetchData()
      }
    } catch (e) {
      showToast(`Pricing error: ${e.message}`)
    }
  }

  // On-Demand AI Reorder PO Analysis
  const handleTriggerReorder = async (product) => {
    try {
      showToast(`Evaluating replenishment PO for ${product.sku}...`)
      const res = await fetch(`/api/products/${product.id}/suggest-reorder`, { method: 'POST' })
      if (res.ok) {
        showToast(`AI replenishment PO generated for ${product.sku}! View in Replenishment tab.`)
        setActiveTab('reorder')
        await fetchData()
      }
    } catch (e) {
      showToast(`Reorder error: ${e.message}`)
    }
  }

  // Quick Stock Level Update
  const handleUpdateStock = async (product, valStr) => {
    const val = parseInt(valStr, 10)
    if (isNaN(val) || val < 0 || val === product.stockLevel) return

    try {
      const res = await fetch(`/api/products/${product.id}/stock`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ stockLevel: val })
      })
      if (res.ok) {
        showToast(`Stock updated to ${val} units. Checking agentic triggers...`)
        await fetchData()
      }
    } catch (e) {
      showToast(`Update error: ${e.message}`)
    }
  }

  // Accept Pricing Suggestion
  const handleAcceptPricing = async (suggestion) => {
    try {
      const res = await fetch(`/api/pricing-suggestions/${suggestion.id}`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ status: 'ACCEPTED' })
      })
      if (res.ok) {
        showToast(`Approved! ${suggestion.productSku} price published to $${suggestion.recommendedPrice.toFixed(2)}`)
        await fetchData()
      }
    } catch (e) {
      showToast(`Accept error: ${e.message}`)
    }
  }

  // Reject Pricing Suggestion
  const handleRejectPricing = async (suggestion) => {
    try {
      const res = await fetch(`/api/pricing-suggestions/${suggestion.id}`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ status: 'REJECTED' })
      })
      if (res.ok) {
        showToast(`Pricing proposal declined.`)
        await fetchData()
      }
    } catch (e) {
      showToast(`Reject error: ${e.message}`)
    }
  }

  // Accept Reorder Proposal
  const handleAcceptReorder = async (suggestion) => {
    try {
      const res = await fetch(`/api/reorder-suggestions/${suggestion.id}`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ status: 'ACCEPTED' })
      })
      if (res.ok) {
        showToast(`Inbound stock approved! +${suggestion.recommendedQuantity} units added to inventory.`)
        await fetchData()
      }
    } catch (e) {
      showToast(`Accept error: ${e.message}`)
    }
  }

  // Reject Reorder Proposal
  const handleRejectReorder = async (suggestion) => {
    try {
      const res = await fetch(`/api/reorder-suggestions/${suggestion.id}`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ status: 'REJECTED' })
      })
      if (res.ok) {
        showToast(`Reorder proposal declined.`)
        await fetchData()
      }
    } catch (e) {
      showToast(`Reject error: ${e.message}`)
    }
  }

  // SSE Stream Handler (Bonus)
  const handleStartSseStream = (product) => {
    if (eventSourceRef.current) {
      eventSourceRef.current.close()
    }
    setStreamingProduct(product)
    setStreamLogs([])
    setIsStreaming(true)

    const url = `/api/products/${product.id}/suggest-pricing/stream`
    const es = new EventSource(url)
    eventSourceRef.current = es

    es.addEventListener('status', (e) => {
      setStreamLogs(prev => [...prev, { type: 'status', text: e.data }])
    })
    es.addEventListener('context', (e) => {
      setStreamLogs(prev => [...prev, { type: 'context', text: e.data }])
    })
    es.addEventListener('chunk', (e) => {
      setStreamLogs(prev => [...prev, { type: 'chunk', text: e.data }])
    })
    es.addEventListener('complete', (e) => {
      setStreamLogs(prev => [...prev, { type: 'complete', text: e.data }])
      setIsStreaming(false)
      es.close()
      fetch(`/api/products/${product.id}/suggest-pricing`, { method: 'POST' }).then(() => fetchData())
    })
    es.onerror = () => {
      setIsStreaming(false)
      es.close()
    }
  }

  const handleCloseStream = () => {
    if (eventSourceRef.current) {
      eventSourceRef.current.close()
    }
    setStreamingProduct(null)
    setIsStreaming(false)
  }

  // Filtered Products
  const filteredProducts = products.filter(p => {
    const matchesCat = categoryFilter === 'ALL' || p.category === categoryFilter
    const matchesSearch = !searchQuery || 
      p.name.toLowerCase().includes(searchQuery.toLowerCase()) || 
      p.sku.toLowerCase().includes(searchQuery.toLowerCase())
    return matchesCat && matchesSearch
  })

  // KPI Calculations
  const totalSkus = products.length
  const lowStockCount = products.filter(p => p.stockLevel <= p.reorderThreshold).length
  const pendingPricingCount = pricingSuggestions.length
  const pendingReorderCount = reorderSuggestions.length
  const totalUnitsToReorder = reorderSuggestions.reduce((acc, r) => acc + (r.recommendedQuantity || 0), 0)

  return (
    <div className="app-shell">
      {/* Toast */}
      {toast && <div className="toast-msg">⚡ {toast}</div>}

      {/* Top Navbar */}
      <nav className="navbar">
        <div className="brand-section">
          <div className="brand-logo">⚡</div>
          <div>
            <div className="brand-title">
              StockPulse <span>Engine</span>
              <span className="brand-tag">v2.4 Reactive</span>
            </div>
          </div>
        </div>

        <div className="nav-actions">
          <div className="status-pill">
            <span className="radar-dot"></span>
            AGENTIC LOOP ONLINE
          </div>

          <div className="strategy-segmented">
            <button 
              className={`strategy-opt ${activeStrategy === 'ai' ? 'active' : ''}`}
              onClick={() => handleToggleStrategy('ai')}
              title="Activate LLM Neural Commerce Intelligence"
            >
              ✦ AI Neural
            </button>
            <button 
              className={`strategy-opt ${activeStrategy === 'rule-based' ? 'active' : ''}`}
              onClick={() => handleToggleStrategy('rule-based')}
              title="Activate Deterministic Fallback Rule Engine"
            >
              ⚙ Rules
            </button>
          </div>

          <button className="btn-icon" onClick={fetchData} title="Sync Live State">
            ↻ Sync
          </button>

          <button className="btn-reset" onClick={handleResetDemoData} title="Reset catalog to clean baseline">
            ↺ Reset Demo Data
          </button>
        </div>
      </nav>

      {/* Connection Notice */}
      {error && (
        <div style={{ background: 'rgba(245, 158, 11, 0.1)', border: '1px solid rgba(245, 158, 11, 0.3)', color: '#fbbf24', padding: '12px 18px', borderRadius: '8px', marginBottom: '20px', fontSize: '13px' }}>
          <strong>Backend Status:</strong> {error}
        </div>
      )}

      {/* Hackathon Interactive Walkthrough Guide */}
      <div className="guide-banner">
        <div className="guide-header" onClick={() => setShowGuide(!showGuide)}>
          <div className="guide-title">
            <span>⚡</span>
            <span>HACKATHON EVALUATION GUIDE &amp; TEST SCENARIOS</span>
            <span style={{ fontSize: '11px', background: 'rgba(99, 102, 241, 0.25)', padding: '2px 8px', borderRadius: '4px', marginLeft: '6px' }}>
              4 Core Scenarios
            </span>
          </div>
          <span style={{ color: 'var(--cyan)', fontSize: '13px', fontFamily: 'var(--font-mono)' }}>
            {showGuide ? '▲ Hide Guide' : '▼ Show Test Guide'}
          </span>
        </div>

        {showGuide && (
          <div className="guide-content">
            <div className="guide-step">
              <div className="guide-step-num">Flow 1 · Low Stock &amp; Replenishment</div>
              <div>
                Click <strong>+ Sale</strong> on <code>PRD-001</code> (stock 16 &rarr; 15, hitting threshold 15) or change stock directly in the table input box.
                The event listener triggers an autonomous purchase order in the <strong>Replenishment POs</strong> tab!
              </div>
            </div>

            <div className="guide-step">
              <div className="guide-step-num">Flow 2 · Demand Surge &amp; Dynamic Pricing</div>
              <div>
                Click <strong>⚡ Surge</strong> on any item (e.g. <code>PRD-002</code>). This fires 4 rapid orders, pushing 24h velocity over 3x the category baseline, triggering dynamic pricing proposals.
              </div>
            </div>

            <div className="guide-step">
              <div className="guide-step-num">Flow 3 · Human-in-the-Loop Decisioning</div>
              <div>
                Inspect proposals in <strong>Dynamic Pricing</strong> and <strong>Replenishment POs</strong> tabs with AI reasoning and confidence meters.
                Click <strong>Publish Price &rarr;</strong> or <strong>Approve Inbound &rarr;</strong> to verify live database execution.
              </div>
            </div>

            <div className="guide-step">
              <div className="guide-step-num">Flow 4 · Model Switch &amp; SSE Streaming (Bonus)</div>
              <div>
                Toggle between <strong>✦ AI Neural</strong> and <strong>⚙ Rules</strong> in the top navbar.
                Click <strong>✦ AI Stream</strong> on any row to open the real-time SSE terminal modal and watch live LLM tokens streaming!
              </div>
            </div>
          </div>
        )}
      </div>

      {/* KPI Metrics Strip */}
      <div className="kpi-grid">
        <div className="kpi-card accent-cyan">
          <div className="kpi-header">
            <span className="kpi-title">Catalog Health</span>
            <span style={{ fontSize: '18px' }}>📦</span>
          </div>
          <div className="kpi-stat">{totalSkus} <span style={{ fontSize: '14px', color: 'var(--text-muted)' }}>SKUs</span></div>
          <div className="kpi-sub">Across Electronics, Apparel & Home</div>
        </div>

        <div className="kpi-card accent-coral">
          <div className="kpi-header">
            <span className="kpi-title">Stock Breaches</span>
            <span style={{ fontSize: '18px' }}>⚠️</span>
          </div>
          <div className="kpi-stat" style={{ color: lowStockCount > 0 ? 'var(--coral)' : 'var(--text-primary)' }}>
            {lowStockCount}
          </div>
          <div className="kpi-sub">Items below reorder threshold</div>
        </div>

        <div className="kpi-card accent-amber">
          <div className="kpi-header">
            <span className="kpi-title">Pricing Proposals</span>
            <span style={{ fontSize: '18px' }}>📈</span>
          </div>
          <div className="kpi-stat" style={{ color: 'var(--amber)' }}>{pendingPricingCount}</div>
          <div className="kpi-sub">Pending human review & publishing</div>
        </div>

        <div className="kpi-card accent-emerald">
          <div className="kpi-header">
            <span className="kpi-title">Replenishment POs</span>
            <span style={{ fontSize: '18px' }}>🔄</span>
          </div>
          <div className="kpi-stat" style={{ color: 'var(--emerald)' }}>{pendingReorderCount}</div>
          <div className="kpi-sub">{totalUnitsToReorder} total units to reorder</div>
        </div>
      </div>

      {/* AI Decision Hub (Pricing & Reorder Queues) */}
      <div className="hub-section">
        <div className="hub-header">
          <div className="hub-title">
            Autonomous Decision Hub
            <span className="count-chip">{pricingSuggestions.length + reorderSuggestions.length} Total Actions Required</span>
          </div>

          <div style={{ display: 'flex', gap: '8px' }}>
            <button 
              className={`pill-btn ${activeTab === 'pricing' ? 'active' : ''}`}
              onClick={() => setActiveTab('pricing')}
            >
              Dynamic Pricing ({pricingSuggestions.length})
            </button>
            <button 
              className={`pill-btn ${activeTab === 'reorder' ? 'active' : ''}`}
              onClick={() => setActiveTab('reorder')}
            >
              Replenishment POs ({reorderSuggestions.length})
            </button>
          </div>
        </div>

        {/* Dynamic Pricing Tab */}
        {activeTab === 'pricing' && (
          <div>
            {pricingSuggestions.length === 0 ? (
              <div className="empty-hero">
                <div className="empty-icon">✓</div>
                <div className="empty-text">
                  All prices are calibrated. Simulate a sale on any product below to trigger the inventory depletion loop!
                </div>
              </div>
            ) : (
              <div className="cards-grid">
                {pricingSuggestions.map(s => {
                  const diff = s.recommendedPrice - s.currentPrice
                  const pct = ((diff / s.currentPrice) * 100).toFixed(1)
                  return (
                    <div key={s.id} className="decision-card">
                      <div className="card-top">
                        <span className="sku-badge">{s.productSku}</span>
                        <span className={`trigger-badge ${s.triggerReason === 'INVENTORY_LOW' ? 'low-stock' : s.triggerReason === 'DEMAND_SPIKE' ? 'demand-spike' : 'manual'}`}>
                          {s.triggerReason.replace('_', ' ')}
                        </span>
                      </div>

                      <div className="card-product-name">{s.productName}</div>

                      <div className="recommend-box">
                        <div className="price-flow">
                          <span className="old-price">${s.currentPrice.toFixed(2)}</span>
                          <span className="new-price">${s.recommendedPrice.toFixed(2)}</span>
                        </div>
                        <span className={`percent-chip ${diff > 0 ? 'up' : diff < 0 ? 'down' : 'hold'}`}>
                          {diff > 0 ? `▲ +${pct}%` : `${pct}%`}
                        </span>
                      </div>

                      <div className="meter-section">
                        <div className="meter-labels">
                          <span>AI Decision Confidence</span>
                          <span>{Math.round(s.confidence * 100)}%</span>
                        </div>
                        <div className="meter-track">
                          <div className="meter-fill" style={{ width: `${s.confidence * 100}%` }}></div>
                        </div>
                      </div>

                      <div className="ai-reasoning">
                        "{s.reasoning}"
                      </div>

                      <div className="card-btns">
                        <button className="btn-decline" onClick={() => handleRejectPricing(s)}>
                          Reject
                        </button>
                        <button className="btn-approve" onClick={() => handleAcceptPricing(s)}>
                          Publish Price →
                        </button>
                      </div>
                    </div>
                  )
                })}
              </div>
            )}
          </div>
        )}

        {/* Replenishment Tab */}
        {activeTab === 'reorder' && (
          <div>
            {reorderSuggestions.length === 0 ? (
              <div className="empty-hero">
                <div className="empty-icon">✓</div>
                <div className="empty-text">
                  Inventory buffers healthy. Lower a stock level below reorder threshold to trigger automatic replenishment suggestions.
                </div>
              </div>
            ) : (
              <div className="cards-grid">
                {reorderSuggestions.map(s => (
                  <div key={s.id} className="decision-card reorder-card">
                    <div className="card-top">
                      <span className="sku-badge">{s.productSku}</span>
                      <span className={`trigger-badge ${s.triggerReason === 'INVENTORY_LOW' ? 'low-stock' : s.triggerReason === 'DEMAND_SPIKE' ? 'demand-spike' : 'manual'}`}>
                        {s.triggerReason.replace('_', ' ')}
                      </span>
                    </div>

                    <div className="card-product-name">{s.productName}</div>

                    <div className="recommend-box">
                      <div>
                        <span style={{ fontSize: '10px', color: 'var(--text-muted)', display: 'block', textTransform: 'uppercase' }}>Current Stock</span>
                        <span style={{ fontSize: '18px', fontWeight: 600, color: 'var(--coral)' }}>{s.currentStock} units</span>
                      </div>
                      <div style={{ textAlign: 'right' }}>
                        <span style={{ fontSize: '10px', color: 'var(--cyan)', display: 'block', textTransform: 'uppercase', fontWeight: 600 }}>Suggested PO</span>
                        <span style={{ fontSize: '20px', fontWeight: 700, color: 'var(--cyan)' }}>+{s.recommendedQuantity} units</span>
                      </div>
                    </div>

                    <div className="meter-section">
                      <div className="meter-labels">
                        <span>Lead Time: {s.suggestedLeadTime} days</span>
                        <span>Confidence: {Math.round(s.confidence * 100)}%</span>
                      </div>
                      <div className="meter-track">
                        <div className="meter-fill reorder" style={{ width: `${s.confidence * 100}%` }}></div>
                      </div>
                    </div>

                    <div className="ai-reasoning reorder">
                      "{s.reasoning}"
                    </div>

                    <div className="card-btns">
                      <button className="btn-decline" onClick={() => handleRejectReorder(s)}>
                        Reject
                      </button>
                      <button className="btn-approve" onClick={() => handleAcceptReorder(s)}>
                        Approve Inbound →
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        )}
      </div>

      {/* Storefront Catalog & Simulator Table */}
      <div className="catalog-section">
        <div className="hub-header">
          <div className="hub-title">
            Storefront Catalog & Reactive Simulator
          </div>

          <div style={{ display: 'flex', gap: '12px', alignItems: 'center' }}>
            <input 
              type="text"
              placeholder="Search SKU or product..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              style={{
                background: 'var(--bg-surface-elevated)',
                border: '1px solid var(--border-subtle)',
                borderRadius: '8px',
                padding: '6px 14px',
                color: '#fff',
                fontSize: '12px',
                fontFamily: 'var(--font-mono)',
                width: '200px'
              }}
            />
          </div>
        </div>

        {/* Filter Pills */}
        <div className="filter-bar">
          <div className="filter-pills">
            {['ALL', 'ELECTRONICS', 'APPAREL', 'HOME'].map(cat => (
              <button 
                key={cat}
                className={`pill-btn ${categoryFilter === cat ? 'active' : ''}`}
                onClick={() => setCategoryFilter(cat)}
              >
                {cat}
              </button>
            ))}
          </div>

          <span style={{ fontSize: '12px', color: 'var(--text-muted)', fontFamily: 'var(--font-mono)' }}>
            Showing {filteredProducts.length} of {products.length} catalog items
          </span>
        </div>

        {/* Table */}
        <div className="table-wrapper">
          <table className="catalog-tbl">
            <thead>
              <tr>
                <th>SKU</th>
                <th>Product Description</th>
                <th>Category</th>
                <th>Price</th>
                <th>Stock / Threshold</th>
                <th>24h Velocity</th>
                <th>Lifecycle Status</th>
                <th style={{ textAlign: 'right' }}>Interactive Simulation</th>
              </tr>
            </thead>
            <tbody>
              {filteredProducts.map(p => {
                const isBreached = p.stockLevel <= p.reorderThreshold
                const stockPct = Math.min(100, Math.round((p.stockLevel / (p.reorderThreshold * 2.5)) * 100))
                return (
                  <tr key={p.id}>
                    <td>
                      <span className="sku-badge">{p.sku}</span>
                    </td>
                    <td>
                      <strong style={{ color: '#fff' }}>{p.name}</strong>
                    </td>
                    <td>
                      <span style={{ color: 'var(--text-secondary)', fontFamily: 'var(--font-mono)', fontSize: '11px' }}>
                        {p.category}
                      </span>
                    </td>
                    <td>
                      <span style={{ fontFamily: 'var(--font-mono)', fontWeight: 600, color: 'var(--emerald)' }}>
                        ${p.currentPrice.toFixed(2)}
                      </span>
                    </td>
                    <td>
                      <div className="stock-meter">
                        <div className={`stock-num ${isBreached ? 'warn' : 'safe'}`}>
                          <span>{p.stockLevel}</span>
                          <span style={{ color: 'var(--text-muted)', fontWeight: 400, fontSize: '11px' }}>/ {p.reorderThreshold}</span>
                          {isBreached && <span title="Breached reorder threshold">⚠️</span>}
                        </div>
                        <div className="stock-track">
                          <div 
                            className={`stock-bar ${isBreached ? 'low' : ''}`}
                            style={{ width: `${stockPct}%` }}
                          ></div>
                        </div>
                      </div>
                    </td>
                    <td>
                      <span style={{ fontFamily: 'var(--font-mono)', fontSize: '12px', color: p.demandVelocity >= 10 ? 'var(--purple)' : 'var(--text-secondary)' }}>
                        ⚡ {p.demandVelocity} / day
                      </span>
                    </td>
                    <td>
                      <span className={`status-tag ${p.status.toLowerCase()}`}>
                        {p.status.replace(/_/g, ' ')}
                      </span>
                    </td>
                    <td>
                      <div className="action-cluster">
                        {/* Sale Simulator */}
                        <button 
                          className="btn-tool sale"
                          onClick={() => handleSimulateSale(p)}
                          disabled={p.stockLevel <= 0}
                          title="Simulate 1 purchase (stock -1, velocity +1)"
                        >
                          + Sale
                        </button>

                        {/* Inline Stock Edit */}
                        <input 
                          type="number"
                          className="stock-stepper"
                          defaultValue={p.stockLevel}
                          key={`${p.id}-${p.stockLevel}`}
                          onKeyDown={(e) => {
                            if (e.key === 'Enter') {
                              handleUpdateStock(p, e.target.value)
                              e.target.blur()
                            }
                          }}
                          onBlur={(e) => handleUpdateStock(p, e.target.value)}
                          title="Enter new stock and press Enter"
                        />

                        {/* Demand Surge Simulator */}
                        <button 
                          className="btn-tool surge"
                          onClick={() => handleSimulateSurge(p)}
                          title="Simulate viral demand spike (4 rapid orders)"
                        >
                          ⚡ Surge
                        </button>

                        {/* On-Demand AI Pricing */}
                        <button 
                          className="btn-tool"
                          style={{ background: 'rgba(99, 102, 241, 0.12)', border: '1px solid rgba(99, 102, 241, 0.3)', color: '#c7d2fe' }}
                          onClick={() => handleTriggerPricing(p)}
                          title="Generate AI pricing proposal for this SKU"
                        >
                          ✦ AI Price
                        </button>

                        {/* On-Demand AI Reorder */}
                        <button 
                          className="btn-tool"
                          style={{ background: 'rgba(6, 182, 212, 0.12)', border: '1px solid rgba(6, 182, 212, 0.3)', color: '#67e8f9' }}
                          onClick={() => handleTriggerReorder(p)}
                          title="Generate AI replenishment proposal for this SKU"
                        >
                          ✦ AI PO
                        </button>

                        {/* SSE Stream Button */}
                        <button 
                          className="btn-tool stream"
                          onClick={() => handleStartSseStream(p)}
                          title="Stream live AI reasoning tokens in real time"
                        >
                          ✦ Stream
                        </button>
                      </div>
                    </td>
                  </tr>
                )
              })}
            </tbody>
          </table>
        </div>
      </div>

      {/* Real-Time SSE Neural Stream Modal */}
      {streamingProduct && (
        <div className="terminal-backdrop" onClick={handleCloseStream}>
          <div className="terminal-window" onClick={(e) => e.stopPropagation()}>
            <div className="terminal-bar">
              <div className="window-dots">
                <span className="dot red"></span>
                <span className="dot yellow"></span>
                <span className="dot green"></span>
              </div>
              <div className="terminal-title">
                <span>✦</span> SSE REAL-TIME NEURAL STREAM · {streamingProduct.sku}
              </div>
              <button 
                onClick={handleCloseStream}
                style={{ background: 'none', border: 'none', color: '#64748b', fontSize: '18px', cursor: 'pointer' }}
              >
                ✕
              </button>
            </div>

            <div className="terminal-body">
              {streamLogs.map((log, i) => (
                <div key={i} className={`stream-row ${log.type}`}>
                  <span style={{ color: '#6366f1' }}>&gt;</span> {log.text}
                </div>
              ))}
              {isStreaming && (
                <div className="stream-row">
                  <span style={{ color: '#6366f1' }}>&gt;</span>
                  <span style={{ color: '#64748b' }}>Synthesizing dynamic strategy tokens</span>
                  <span className="cursor"></span>
                </div>
              )}
            </div>

            <div style={{ padding: '12px 20px', borderTop: '1px solid rgba(255,255,255,0.08)', display: 'flex', justifyContent: 'space-between', alignItems: 'center', background: '#0d1322' }}>
              <span style={{ fontSize: '11px', color: '#64748b', fontFamily: 'var(--font-mono)' }}>
                Target: {streamingProduct.name} (${streamingProduct.currentPrice.toFixed(2)})
              </span>
              <button 
                className="btn-tool" 
                style={{ background: 'var(--primary)', color: '#fff', border: 'none' }}
                onClick={handleCloseStream}
              >
                Close Stream
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}

export default App