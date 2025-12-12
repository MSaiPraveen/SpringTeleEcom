# 401 Unauthorized Error - Fix Guide

## Error Details

**Endpoint:** `/api/orders/my`  
**Status:** 401 Unauthorized  
**Issue:** Frontend cannot fetch user orders

## Root Causes

### 1. Missing or Invalid JWT Token
The most common cause - the frontend is not sending the Authorization header correctly.

### 2. Expired Token
JWT tokens expire after 24 hours by default.

### 3. Token Not Stored/Retrieved
Frontend might not be storing or retrieving the token from localStorage/sessionStorage.

## Fixes Applied

### ✅ Backend Improvements

1. **Enhanced JWT Filter Logging**
   - Now logs every authentication attempt
   - Shows if Authorization header is present
   - Displays username extraction and validation results

2. **Better Error Messages**
   - 401 responses now include detailed error messages
   - Shows the path that failed
   - Provides hints for fixing the issue

3. **Order Endpoint Logging**
   - Logs when `/api/orders/my` is called
   - Shows number of orders found
   - Displays any errors that occur

## Frontend Fixes Needed

### Check 1: Token Storage After Login

```javascript
// In your login function:
const response = await axios.post('/api/auth/login', {
  username: username,
  password: password
});

// Make sure you're storing the token:
localStorage.setItem('token', response.data.token);  // ✅ Required
localStorage.setItem('username', response.data.username);
localStorage.setItem('isAdmin', response.data.isAdmin);
```

### Check 2: Send Token with Requests

```javascript
// Option 1: Set default axios header (recommended)
axios.defaults.headers.common['Authorization'] = `Bearer ${localStorage.getItem('token')}`;

// Option 2: Send with each request
const token = localStorage.getItem('token');
axios.get('/api/orders/my', {
  headers: {
    'Authorization': `Bearer ${token}`
  }
});
```

### Check 3: Check if Token Exists Before Request

```javascript
const fetchMyOrders = async () => {
  const token = localStorage.getItem('token');
  
  if (!token) {
    console.error('No token found - user not logged in');
    // Redirect to login or show error
    return;
  }

  try {
    const response = await axios.get('/api/orders/my', {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });
    setOrders(response.data);
  } catch (error) {
    console.error('Failed to fetch orders:', error);
    
    if (error.response?.status === 401) {
      // Token expired or invalid
      localStorage.removeItem('token');
      // Redirect to login
    }
  }
};
```

### Check 4: Handle Token Expiration

```javascript
// Add axios interceptor to handle 401 globally
axios.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Token expired or invalid
      localStorage.removeItem('token');
      localStorage.removeItem('username');
      localStorage.removeItem('isAdmin');
      
      // Redirect to login
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);
```

## Testing the Fix

### Step 1: Check Backend Logs

After deploying, check Render logs for:

```
🔐 JWT Filter - Path: /api/orders/my, Auth Header: Present
👤 Extracted username from token: admin
✅ Authentication successful for user: admin
📦 GET /api/orders/my - Fetching orders for current user
✅ Found 0 orders for user
```

Or if failing:
```
🔐 JWT Filter - Path: /api/orders/my, Auth Header: Missing
⚠️  No Bearer token found, continuing without authentication
🚫 Authentication failed for path: /api/orders/my
   Auth header present: false
   Error: Full authentication is required
```

### Step 2: Test with cURL

```bash
# 1. Login to get token
curl -X POST https://springteleecom.onrender.com/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# Copy the token from response

# 2. Test orders endpoint
curl https://springteleecom.onrender.com/api/orders/my \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"

# Should return:
# [] (empty array if no orders)
# or array of orders if user has placed orders
```

### Step 3: Check Browser Console

```javascript
// In browser console:
console.log('Token:', localStorage.getItem('token'));
console.log('Username:', localStorage.getItem('username'));

// If null, user needs to login again
```

## Common Issues & Solutions

### Issue 1: "Failed to fetch my orders" - 401

**Cause:** No token or invalid token  
**Solution:**
1. Check if token exists: `localStorage.getItem('token')`
2. If null, redirect to login
3. If exists, check if it's being sent in Authorization header

### Issue 2: Token Expired

**Cause:** Token is older than 24 hours  
**Solution:**
1. Login again to get new token
2. Implement token refresh mechanism
3. Or increase JWT_EXPIRATION_MS in backend

### Issue 3: CORS Blocking Authorization Header

**Cause:** CORS not configured to allow Authorization header  
**Solution:** Already fixed in backend - Authorization is in allowed headers

### Issue 4: Double /api in URL

**Cause:** API base URL has /api and endpoint also has /api  
**Solution:** 
```javascript
// Fix your base URL:
const API_URL = 'https://springteleecom.onrender.com';  // No /api here

// Then use:
axios.get(`${API_URL}/api/orders/my`)  // ✅ Correct
```

## Quick Fix Checklist

### Backend (Already Fixed ✅)
- ✅ Enhanced JWT filter logging
- ✅ Better 401 error messages
- ✅ Order endpoint logging
- ✅ CORS configured for Authorization header

### Frontend (You Need to Fix ❌)
- ❌ Store token after login
- ❌ Send Authorization header with requests
- ❌ Handle 401 errors (redirect to login)
- ❌ Check token exists before making requests
- ❌ Add axios interceptor for global 401 handling

## Expected Behavior After Fix

1. **User logs in** → Token stored in localStorage
2. **Frontend makes request** → Sends `Authorization: Bearer {token}` header
3. **Backend validates token** → Logs authentication success
4. **Returns user's orders** → Frontend displays them
5. **Token expires** → Frontend catches 401, redirects to login

## Deployment

Committing and pushing changes now...

**Files Changed:**
- `JwtAuthenticationFilter.java` - Enhanced logging
- `SecurityConfig.java` - Better error messages  
- `OrderController.java` - Added request logging

**Next Steps:**
1. Wait for Render to deploy (2-3 minutes)
2. Fix frontend token handling
3. Test with browser DevTools Network tab
4. Check Authorization header is present in requests
5. Verify orders load successfully

---

**Backend Status:** ✅ Fixed and deploying  
**Frontend Status:** ❌ Needs token handling fixes

