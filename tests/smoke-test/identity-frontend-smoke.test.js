/**
 * Smoke test for Identity Frontend Service
 * Tests: Health check (service responding) and basic UI rendering
 */

const { request, waitForService } = require('../helpers/http-client');

const SERVICE_URL = 'http://localhost:5174'; // Different port to avoid conflicts
const SERVICE_NAME = 'identity-frontend';

async function testHealthCheck() {
  console.log(`[${SERVICE_NAME}] Testing health check...`);
  try {
    const response = await request(SERVICE_URL, { timeout: 5000 });
    
    if (response.status === 200) {
      console.log(`[${SERVICE_NAME}] ✅ Health check passed (Status: ${response.status})`);
      return { passed: true, message: 'Service is responding' };
    } else {
      console.log(`[${SERVICE_NAME}] ❌ Health check failed (Status: ${response.status})`);
      return { passed: false, message: `Unexpected status: ${response.status}` };
    }
  } catch (error) {
    console.log(`[${SERVICE_NAME}] ❌ Health check failed: ${error.message}`);
    return { passed: false, message: error.message };
  }
}

async function testBasicUI() {
  console.log(`[${SERVICE_NAME}] Testing basic UI...`);
  try {
    const response = await request(SERVICE_URL, { timeout: 5000 });
    
    if (response.status === 200 && 
        (typeof response.data === 'string' && response.data.includes('<!DOCTYPE html>'))) {
      console.log(`[${SERVICE_NAME}] ✅ Basic UI test passed`);
      return { passed: true, message: 'HTML content returned' };
    } else {
      console.log(`[${SERVICE_NAME}] ❌ Basic UI test failed`);
      return { passed: false, message: 'No valid HTML content' };
    }
  } catch (error) {
    console.log(`[${SERVICE_NAME}] ❌ Basic UI test failed: ${error.message}`);
    return { passed: false, message: error.message };
  }
}

async function runTests() {
  console.log(`\n========== ${SERVICE_NAME.toUpperCase()} SMOKE TESTS ==========\n`);
  
  const results = {
    service: SERVICE_NAME,
    health: null,
    ui: null,
    overall: 'FAIL'
  };

  // Wait for service to be ready
  console.log(`[${SERVICE_NAME}] Waiting for service to be ready...`);
  const isReady = await waitForService(SERVICE_URL, 15, 2000);
  
  if (!isReady) {
    console.log(`[${SERVICE_NAME}] ❌ Service did not start within timeout`);
    results.health = { passed: false, message: 'Service not responding' };
    return results;
  }

  // Run tests
  results.health = await testHealthCheck();
  results.ui = await testBasicUI();

  // Determine overall status
  if (results.health.passed && results.ui.passed) {
    results.overall = 'PASS';
    console.log(`\n[${SERVICE_NAME}] ✅ All tests passed\n`);
  } else {
    console.log(`\n[${SERVICE_NAME}] ❌ Some tests failed\n`);
  }

  return results;
}

// Run if called directly
if (require.main === module) {
  runTests()
    .then(results => {
      console.log(JSON.stringify(results, null, 2));
      process.exit(results.overall === 'PASS' ? 0 : 1);
    })
    .catch(error => {
      console.error('Error running tests:', error);
      process.exit(1);
    });
}

module.exports = { runTests };
