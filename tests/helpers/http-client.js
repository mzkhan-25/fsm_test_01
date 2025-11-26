/**
 * Simple HTTP client helper for smoke tests
 */

const http = require('http');
const https = require('https');

/**
 * Make HTTP request
 * @param {string} url - Full URL to request
 * @param {object} options - Request options (method, headers, body)
 * @returns {Promise<{status: number, data: any, headers: object}>}
 */
function request(url, options = {}) {
  return new Promise((resolve, reject) => {
    const urlObj = new URL(url);
    const client = urlObj.protocol === 'https:' ? https : http;
    
    const reqOptions = {
      hostname: urlObj.hostname,
      port: urlObj.port,
      path: urlObj.pathname + urlObj.search,
      method: options.method || 'GET',
      headers: options.headers || {},
      timeout: options.timeout || 10000
    };

    const req = client.request(reqOptions, (res) => {
      let data = '';

      res.on('data', (chunk) => {
        data += chunk;
      });

      res.on('end', () => {
        let parsedData;
        try {
          parsedData = data ? JSON.parse(data) : null;
        } catch (e) {
          parsedData = data;
        }

        resolve({
          status: res.statusCode,
          data: parsedData,
          headers: res.headers
        });
      });
    });

    req.on('error', (err) => {
      reject(err);
    });

    req.on('timeout', () => {
      req.destroy();
      reject(new Error('Request timeout'));
    });

    if (options.body) {
      req.write(JSON.stringify(options.body));
    }

    req.end();
  });
}

/**
 * Wait for service to be ready
 * @param {string} url - Health check URL
 * @param {number} maxAttempts - Maximum number of attempts
 * @param {number} delayMs - Delay between attempts in milliseconds
 * @returns {Promise<boolean>}
 */
async function waitForService(url, maxAttempts = 30, delayMs = 2000) {
  for (let i = 0; i < maxAttempts; i++) {
    try {
      const response = await request(url, { timeout: 5000 });
      if (response.status >= 200 && response.status < 300) {
        return true;
      }
    } catch (err) {
      // Service not ready yet
    }
    await new Promise(resolve => setTimeout(resolve, delayMs));
  }
  return false;
}

module.exports = {
  request,
  waitForService
};
