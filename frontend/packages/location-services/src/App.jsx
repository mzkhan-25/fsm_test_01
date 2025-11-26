import './App.css'
import Map from './components/Map'

function App() {
  return (
    <div className="app-container">
      <header className="app-header">
        <h1>Location Services</h1>
      </header>
      <main className="map-container">
        <Map />
      </main>
    </div>
  )
}

export default App
