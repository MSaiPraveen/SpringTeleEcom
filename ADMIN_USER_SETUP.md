# Admin User Setup Guide

## Problem
Product upload fails because the user doesn't have ADMIN role.

## Solutions

### Option 1: Direct Database Insert (Quickest)

Connect to your PostgreSQL database and run these SQL commands:

```sql
-- 1. Create ADMIN role if it doesn't exist
INSERT INTO role (name) VALUES ('ROLE_ADMIN')
ON CONFLICT DO NOTHING;

-- 2. Find the role ID
SELECT id FROM role WHERE name = 'ROLE_ADMIN';

-- 3. Find your user ID
SELECT id FROM users WHERE username = 'your_username';

-- 4. Assign ADMIN role to user (replace user_id and role_id with actual values)
INSERT INTO users_roles (user_id, role_id) 
VALUES (1, 2)  -- Replace with actual IDs from steps 2 and 3
ON CONFLICT DO NOTHING;
```

### Option 2: Create Admin via API

Use this endpoint to register a new admin user:

**Endpoint:** `POST /api/auth/register-admin`

**Request Body:**
```json
{
  "username": "admin",
  "password": "admin123",
  "fullName": "System Administrator"
}
```

**Example with curl:**
```bash
curl -X POST http://localhost:8080/api/auth/register-admin \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123","fullName":"System Administrator"}'
```

### Option 3: Promote Existing User to Admin

If you already have a user account, you can promote it to admin using the database:

```sql
-- Find your user
SELECT * FROM users WHERE username = 'your_username';

-- Get ADMIN role
INSERT INTO role (name) VALUES ('ROLE_ADMIN') ON CONFLICT DO NOTHING;
SELECT id FROM role WHERE name = 'ROLE_ADMIN';

-- Add ADMIN role to your user
INSERT INTO users_roles (user_id, role_id) 
SELECT u.id, r.id 
FROM users u, role r 
WHERE u.username = 'your_username' 
AND r.name = 'ROLE_ADMIN'
ON CONFLICT DO NOTHING;
```

## Verify Admin Access

After setting up admin, verify by logging in:

1. **Login:**
   ```bash
   curl -X POST http://localhost:8080/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{"username":"admin","password":"admin123"}'
   ```

2. **Check the response** - `isAdmin` should be `true`:
   ```json
   {
     "token": "eyJhbGc...",
     "username": "admin",
     "isAdmin": true
   }
   ```

3. **Test product upload** with the token:
   ```bash
   curl -X POST http://localhost:8080/api/product \
     -H "Authorization: Bearer YOUR_TOKEN_HERE" \
     -F 'product={"name":"Test","price":99.99,"category":"Electronics","description":"Test product","brand":"TestBrand","releaseDate":"2024-01-01","productAvailable":true,"stockQuantity":10};type=application/json' \
     -F 'imageFile=@/path/to/image.jpg'
   ```

## Common Issues

### 1. "403 Forbidden" when uploading products
- **Cause:** User doesn't have ADMIN role
- **Solution:** Follow one of the options above to assign ADMIN role

### 2. "401 Unauthorized"
- **Cause:** Missing or invalid JWT token
- **Solution:** Login again and use the new token

### 3. CORS errors
- **Cause:** Frontend origin not allowed
- **Solution:** Add your frontend URL to `FRONTEND_URL` environment variable

### 4. File upload fails
- **Cause:** File size too large
- **Solution:** Check `spring.servlet.multipart.max-file-size` setting (default: 10MB)

## Frontend Integration

When making product upload requests from frontend:

```javascript
const token = localStorage.getItem('token'); // or your token storage

const formData = new FormData();
formData.append('product', new Blob([JSON.stringify({
  name: 'Product Name',
  price: 99.99,
  category: 'Electronics',
  description: 'Product description',
  brand: 'Brand Name',
  releaseDate: '2024-01-01',
  productAvailable: true,
  stockQuantity: 10
})], { type: 'application/json' }));

formData.append('imageFile', imageFile); // File object from input

const response = await fetch('http://localhost:8080/api/product', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${token}`
  },
  body: formData
});
```

## Environment Variables for Render

Make sure these are set in your Render dashboard:

```
FRONTEND_URL=https://your-frontend.vercel.app
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_secret
GITHUB_CLIENT_ID=your_github_client_id
GITHUB_CLIENT_SECRET=your_github_secret
JWT_SECRET=your_secret_key_here
JWT_EXPIRATION_MS=86400000
SPRING_DATASOURCE_URL=your_postgres_url
SPRING_DATASOURCE_USERNAME=your_db_user
SPRING_DATASOURCE_PASSWORD=your_db_password
```

