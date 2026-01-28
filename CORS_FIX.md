# CORS Configuration Fix
**Date:** January 28, 2026  
**Issue:** IllegalArgumentException - CORS credentials with wildcard origins

## Problem

The application was throwing this error:
```
java.lang.IllegalArgumentException: When allowCredentials is true, allowedOrigins 
cannot contain the special value "*" since that cannot be set on the 
"Access-Control-Allow-Origin" response header. To allow credentials to a set of 
origins, list them explicitly or consider using "allowedOriginPatterns" instead.
```

## Root Cause

The CORS configuration had a conflict:
- **Global Config** (`CorsConfig.java`): Used `.allowedOrigins()` with `.allowCredentials(true)`
- **Controller Annotations**: Had `@CrossOrigin(origins = "*")` on controllers
- **Security Violation**: Spring Boot doesn't allow credentials (`allowCredentials = true`) with wildcard origins (`*`)

## Solution Applied

### 1. ✅ Fixed `CorsConfig.java`
**Changed:**
```java
// BEFORE (❌ Error)
.allowedOrigins(allowedOrigins.split(","))

// AFTER (✅ Fixed)
.allowedOriginPatterns(allowedOrigins.split(","))
```

**Why:** `allowedOriginPatterns()` supports wildcards and patterns while still allowing credentials.

### 2. ✅ Removed Redundant `@CrossOrigin` Annotations

Removed from:
- `StudentController.java` - Line 17: `@CrossOrigin(origins = "*")`
- `MessagingController.java` - Line 17: `@CrossOrigin(origins = "*")`
- `AuthController.java` - Line 19: `@CrossOrigin(origins = "*")`

**Why:** The global CORS configuration in `CorsConfig.java` handles all CORS settings. Controller-level annotations were redundant and conflicting.

## Current CORS Configuration

### Allowed Origins (from `application-production.properties`):
```
cors.allowed-origins=https://hostelattendancesystem.onrender.com,http://localhost:5500,http://127.0.0.1:5500
```

### CORS Settings:
- **Origin Patterns:** Configured origins (supports wildcards like `https://*.onrender.com`)
- **Methods:** GET, POST, PUT, DELETE, OPTIONS
- **Headers:** All headers allowed (`*`)
- **Credentials:** Enabled (`true`)
- **Max Age:** 3600 seconds (1 hour)

## Files Modified

1. `src/main/java/com/college/attendance/config/CorsConfig.java`
   - Changed `allowedOrigins()` → `allowedOriginPatterns()`

2. `src/main/java/com/college/attendance/controller/StudentController.java`
   - Removed `@CrossOrigin(origins = "*")`

3. `src/main/java/com/college/attendance/controller/MessagingController.java`
   - Removed `@CrossOrigin(origins = "*")`

4. `src/main/java/com/college/attendance/controller/AuthController.java`
   - Removed `@CrossOrigin(origins = "*")`

## Testing

After deployment, verify CORS is working:

### Test from Browser Console:
```javascript
// Should work without CORS errors
fetch('https://hostelattendancesystem.onrender.com/api/auth/send-login-otp', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  credentials: 'include',
  body: JSON.stringify({ sic: 'TEST123' })
})
.then(r => r.json())
.then(console.log)
.catch(console.error);
```

### Expected Response Headers:
```
Access-Control-Allow-Origin: https://hostelattendancesystem.onrender.com
Access-Control-Allow-Credentials: true
Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS
Access-Control-Max-Age: 3600
```

## Benefits

✅ **Security:** No wildcard origins with credentials  
✅ **Flexibility:** Pattern matching for subdomains  
✅ **Maintainability:** Single source of truth (CorsConfig.java)  
✅ **Compliance:** Follows Spring Security best practices  

## Deployment Status

- ✅ Committed to `fresh_start` branch
- ✅ Pushed to `main` branch (Render auto-deploys)
- ⏳ Waiting for Render to rebuild and redeploy

---

**Next:** Monitor Render logs to confirm the CORS error is resolved and the application starts successfully.
