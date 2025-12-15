# 🔍 Monitoring and Observability Guide

## Overview
This guide explains how to monitor SpringTeleEcom in production using built-in health checks, logs, and metrics.

---

## 🏥 Health Check Endpoints

### Basic Health Check
```bash
GET https://springteleecom.onrender.com/health
```

**Response:**
```json
{
  "status": "UP",
  "timestamp": "2025-12-14T10:30:00Z",
  "service": "SpringTeleEcom",
  "version": "6.0.1"
}
```

### Detailed Health Check
```bash
GET https://springteleecom.onrender.com/health/detailed
```

**Response:**
```json
{
  "status": "UP",
  "timestamp": "2025-12-14T10:30:00Z",
  "service": "SpringTeleEcom",
  "version": "6.0.1",
  "database": {
    "status": "UP",
    "connection": "active",
    "database": "springteleecom"
  },
  "application": {
    "liveness": "CORRECT",
    "readiness": "ACCEPTING_TRAFFIC",
    "beansLoaded": 245
  }
}
```

### Liveness Probe
```bash
GET https://springteleecom.onrender.com/health/liveness
```
**Purpose:** Checks if the app is alive (should it be restarted?)

### Readiness Probe
```bash
GET https://springteleecom.onrender.com/health/readiness
```
**Purpose:** Checks if the app is ready to accept traffic

---

## 📊 Actuator Endpoints

### Health (Spring Boot Actuator)
```bash
GET https://springteleecom.onrender.com/actuator/health
```

### Info
```bash
GET https://springteleecom.onrender.com/actuator/info
```

**Response:**
```json
{
  "app": {
    "name": "SpringTeleEcom",
    "version": "6.0.1",
    "description": "E-Commerce Backend with OAuth2"
  }
}
```

### Metrics
```bash
GET https://springteleecom.onrender.com/actuator/metrics
```

**Available Metrics:**
- `jvm.memory.used` - JVM memory usage
- `system.cpu.usage` - CPU usage
- `http.server.requests` - HTTP request stats
- `hikaricp.connections.active` - Active database connections
- `hikaricp.connections.pending` - Pending database connections

To get specific metric:
```bash
GET https://springteleecom.onrender.com/actuator/metrics/http.server.requests
```

### Prometheus Metrics (if using Prometheus)
```bash
GET https://springteleecom.onrender.com/actuator/prometheus
```

---

## 📝 Logging

### Log Levels
The application uses structured logging with the following levels:

| Component | Level | Description |
|-----------|-------|-------------|
| Root | INFO | General application logs |
| com.example.SpringTeleEcom | DEBUG | Application-specific logs |
| Spring Security | INFO | Security events |
| OAuth2 | DEBUG | OAuth login flows |
| Hibernate SQL | DEBUG | Database queries |
| HikariCP | DEBUG | Connection pool events |
| Web Requests | DEBUG | HTTP requests/responses |

### Log Format
```
2025-12-14 10:30:00.123  INFO --- [nio-8080-exec-1] c.e.SpringTeleEcom.controller.AuthController : 📥 Incoming Request: POST /api/auth/login | IP: 192.168.1.1 | Auth: None
```

### Viewing Logs on Render

1. Go to [Render Dashboard](https://dashboard.render.com/)
2. Select your service: `springteleecom`
3. Click on **Logs** tab
4. Use filters to search:
   - `ERROR` - Show only errors
   - `WARN` - Show warnings
   - `401` - Show unauthorized requests
   - `Database` - Show database issues

### Important Log Patterns

| Pattern | Meaning | Action |
|---------|---------|--------|
| `🚀 Starting SpringTeleEcom Application...` | App starting | Normal |
| `🎉 SpringTeleEcom v6.0.1 Started Successfully!` | App started | Normal |
| `📥 Incoming Request` | HTTP request received | Debug |
| `✅ Response` | Successful response | Normal |
| `⚠️ Response` | Client error (4xx) | Check request |
| `❌ Response` | Server error (5xx) | **INVESTIGATE** |
| `⏱️ SLOW REQUEST` | Request took >2s | **Performance issue** |
| `💥 Exception during request processing` | Unhandled exception | **INVESTIGATE** |
| `🚫 Authentication failed` | JWT validation failed | Check token |
| `❌ Database health check failed` | DB connection lost | **CRITICAL** |
| `🛑 SpringTeleEcom Application shutting down` | App stopping | Normal shutdown |

---

## 🔔 Alerts You Should Set Up

### Critical Alerts (Page Someone)
1. **Health Check Failing**
   - Endpoint: `/health/readiness`
   - Alert if: Returns 503 for >2 minutes
   - Action: Check database connectivity

2. **Database Connection Pool Exhausted**
   - Log pattern: `HikariPool-1 - Connection is not available`
   - Action: Increase pool size or investigate slow queries

3. **High Error Rate**
   - Log pattern: `❌ Response.*5\d{2}`
   - Alert if: >10 errors in 5 minutes
   - Action: Check recent deployments

### Warning Alerts (Investigate Soon)
1. **Slow Requests**
   - Log pattern: `⏱️ SLOW REQUEST`
   - Alert if: >5 in 10 minutes
   - Action: Optimize queries

2. **Authentication Failures**
   - Log pattern: `🚫 Authentication failed`
   - Alert if: >20 in 5 minutes (possible attack)
   - Action: Check for brute force attacks

3. **Memory Usage High**
   - Metric: `jvm.memory.used`
   - Alert if: >80% of max
   - Action: Consider increasing memory

---

## 🖥️ Frontend Monitoring (Vercel)

### Viewing Frontend Logs

1. Go to [Vercel Dashboard](https://vercel.com/dashboard)
2. Select project: `teleecom`
3. Click on **Deployments**
4. Select latest deployment
5. Click on **Logs** or **Real-time Logs**

### Frontend Health Indicators

#### Browser Console Errors to Watch For

```javascript
// CORS Error (Backend not allowing frontend origin)
❌ Access to XMLHttpRequest at 'https://springteleecom.onrender.com/api/product' 
   has been blocked by CORS policy

// Authentication Error (Invalid/expired JWT)
❌ Failed to fetch my orders: 401 Unauthorized

// Network Error (Backend down)
❌ Failed to load products: Network Error (ERR_NETWORK)

// API Error (Backend returned error)
❌ Failed to upload product: 500 Internal Server Error
```

### Adding Frontend Monitoring Code

Add this to your frontend (e.g., in `src/utils/apiMonitor.js`):

```javascript
// Track API call success/failure rates
export const apiMonitor = {
  calls: { success: 0, failed: 0 },
  
  logSuccess(endpoint) {
    this.calls.success++;
    console.log(`✅ API Success: ${endpoint} (Success rate: ${this.getSuccessRate()}%)`);
  },
  
  logError(endpoint, error) {
    this.calls.failed++;
    console.error(`❌ API Error: ${endpoint}`, {
      message: error.message,
      status: error.response?.status,
      data: error.response?.data,
      successRate: this.getSuccessRate()
    });
    
    // Alert if success rate drops below 80%
    if (this.getSuccessRate() < 80) {
      console.warn('⚠️ API success rate dropped below 80%!');
    }
  },
  
  getSuccessRate() {
    const total = this.calls.success + this.calls.failed;
    return total === 0 ? 100 : Math.round((this.calls.success / total) * 100);
  }
};
```

Then wrap your API calls:

```javascript
try {
  const response = await axios.get('/api/product');
  apiMonitor.logSuccess('GET /api/product');
  return response.data;
} catch (error) {
  apiMonitor.logError('GET /api/product', error);
  throw error;
}
```

---

## 🧪 Testing Health Checks Locally

### 1. Start the application
```bash
mvn spring-boot:run
```

### 2. Test health endpoints
```bash
# Basic health
curl http://localhost:8080/health

# Detailed health
curl http://localhost:8080/health/detailed

# Liveness
curl http://localhost:8080/health/liveness

# Readiness
curl http://localhost:8080/health/readiness

# Actuator health
curl http://localhost:8080/actuator/health

# Metrics
curl http://localhost:8080/actuator/metrics
```

---

## 🚨 Common Issues and How to Detect Them

### Issue 1: Database Connection Lost
**Symptoms:**
- `/health/readiness` returns 503
- Logs show: `HikariPool-1 - Failed to validate connection`

**Detection:**
```bash
curl https://springteleecom.onrender.com/health/readiness
# Returns: {"status":"not_ready","reason":"database_unreachable"}
```

**Fix:**
- Check Neon database is up
- Verify `SPRING_DATASOURCE_URL` environment variable
- Restart the service

### Issue 2: Application Not Starting
**Symptoms:**
- Health checks timeout
- Render logs show startup errors

**Detection:**
- Check Render logs for `💥 APPLICATION FAILED TO START!`
- Look for `Caused by:` in stack traces

**Common Causes:**
- Missing environment variables
- Invalid OAuth client IDs
- Database migration failures

### Issue 3: Slow Performance
**Symptoms:**
- Requests take >2 seconds
- Logs show `⏱️ SLOW REQUEST`

**Detection:**
```bash
# Check metrics
curl https://springteleecom.onrender.com/actuator/metrics/http.server.requests
```

**Fix:**
- Check database query performance
- Look for N+1 query problems
- Consider adding database indexes

### Issue 4: High Memory Usage
**Symptoms:**
- Application crashes
- Render restarts service

**Detection:**
```bash
curl https://springteleecom.onrender.com/actuator/metrics/jvm.memory.used
```

**Fix:**
- Increase memory limit on Render
- Look for memory leaks (connections not closed)
- Optimize image uploads (resize before storing)

---

## 📈 Setting Up Render Health Checks

Update your `render.yaml`:

```yaml
services:
  - type: web
    name: springteleecom
    env: docker
    
    # Health check configuration
    healthCheckPath: /health/readiness
    
    # Environment variables
    envVars:
      - key: SPRING_DATASOURCE_URL
        sync: false
      # ... other vars
```

Render will:
- Call `/health/readiness` every 30 seconds
- Restart service if it returns 503 for >2 minutes
- Mark service as "unhealthy" in dashboard

---

## 🎯 Monitoring Checklist

### Daily
- [ ] Check Render dashboard for service status
- [ ] Review error logs (search for `ERROR`)
- [ ] Verify health check is passing

### Weekly
- [ ] Review slow request logs
- [ ] Check database connection pool metrics
- [ ] Analyze API success/failure rates
- [ ] Review authentication failure patterns

### After Deployment
- [ ] Verify health check passes
- [ ] Check logs for startup errors
- [ ] Test critical endpoints (login, product fetch, order placement)
- [ ] Monitor error rate for 15 minutes

---

## 🔗 Quick Links

- **Render Dashboard:** https://dashboard.render.com/
- **Vercel Dashboard:** https://vercel.com/dashboard
- **Production Health:** https://springteleecom.onrender.com/health/detailed
- **Production Metrics:** https://springteleecom.onrender.com/actuator/metrics
- **Frontend:** https://teleecom.vercel.app

---

## 📚 Further Reading

- [Spring Boot Actuator Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [Micrometer Documentation](https://micrometer.io/docs)
- [Render Health Checks](https://render.com/docs/health-checks)
- [Vercel Monitoring](https://vercel.com/docs/analytics)

---

**Remember:** You can't fix what you can't see. Monitor your application! 👀

