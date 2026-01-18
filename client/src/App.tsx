import { BrowserRouter as Router, Routes, Route } from 'react-router-dom'

function App() {
  return (
    <Router>
      <div className="min-h-screen bg-gray-50">
        <Routes>
          <Route path="/" element={
            <div className="container mx-auto px-4 py-8">
              <h1 className="text-4xl font-bold text-primary-600">
                Welcome to LayerTen
              </h1>
              <p className="mt-4 text-gray-600">
                Countdown lists and blog posts coming soon!
              </p>
            </div>
          } />
        </Routes>
      </div>
    </Router>
  )
}

export default App
