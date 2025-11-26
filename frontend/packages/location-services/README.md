# Location Services Microfrontend

This microfrontend provides location-based services including map visualization for the Field Service Management application.

## Features

- Interactive Google Maps integration
- Default map controls (zoom, pan, fullscreen)
- Responsive design for different screen sizes
- Secure API key configuration via environment variables

## Setup

1. Install dependencies:
   ```bash
   npm install
   ```

2. Configure Google Maps API key:
   - Copy `.env.example` to `.env.development`
   - Add your Google Maps API key:
     ```
     VITE_GOOGLE_MAPS_API_KEY=your_api_key_here
     ```

## Development

Run the development server:
```bash
npm run dev
```

The application will be available at http://localhost:5178

## Testing

Run tests:
```bash
npm test
```

Run tests with coverage:
```bash
npm run test:coverage
```

## Building

Build for production:
```bash
npm run build
```

## Map Component

The `Map` component provides a configurable Google Maps interface:

```jsx
<Map 
  center={{ lat: 37.7749, lng: -122.4194 }}
  zoom={12}
  style={{ width: '100%', height: '400px' }}
/>
```

### Props

- `center` (object): Map center coordinates `{lat: number, lng: number}` (default: San Francisco)
- `zoom` (number): Initial zoom level 1-20 (default: 12)
- `mapId` (string): Google Maps Map ID for styling
- `style` (object): Custom styles for the map container
- `className` (string): Additional CSS classes

## Security

The Google Maps API key is stored securely in environment variables and is not committed to the repository. Always use `.env.development` for local development and configure environment variables appropriately in production.
