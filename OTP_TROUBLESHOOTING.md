# OTP Not Sending - Troubleshooting Guide

**Issue:** Frontend shows "Sending OTP..." but OTP never arrives and button stays in loading state.

## Possible Causes & Solutions

### 1. 🔴 **Email Service Not Configured in Render**

**Most Likely Cause!** The Gmail password environment variable might not be set in Render.

#### Check Render Dashboard:
1. Go to https://dashboard.render.com
2. Select your service: `attendance-automation-backend`
3. Go to **Environment** tab
4. Verify these variables are set:
   - `SPRING_MAIL_PASSWORD` = `<your-gmail-app-password>` (Gmail app password)
   - `SPRING_DATASOURCE_PASSWORD` = `<your-aiven-password>` (Aiven password)

#### If Missing:
1. Click **Add Environment Variable**
2. Key: `SPRING_MAIL_PASSWORD`
3. Value: `<your-gmail-app-password>`
4. Click **Save Changes**
5. Service will auto-redeploy

---

### 2. 🟡 **CORS Blocking Requests**

The frontend might not be able to reach the backend due to CORS.

#### Test:
1. Open `api-test.html` in your browser:
   ```
   file:///c:/Users/tsrib/OneDrive/Desktop/Attendance Automation/frontend/api-test.html
   ```
2. Click "Test Health Endpoint"
3. Click "Test CORS"
4. Check if requests succeed

#### If CORS Fails:
- The error message will show in the test tool
- Check browser console (F12) for CORS errors

---

### 3. 🟢 **Request Timeout**

The backend might be taking too long to respond (cold start on free tier).

#### Symptoms:
- Request stays "pending" for 30+ seconds
- Eventually times out
- No response from server

#### Solution:
- Wait longer (first request after sleep can take 30-60 seconds)
- Try again after the service "warms up"

---

### 4. 🔵 **Frontend Cached**

Browser might be using old cached version of JavaScript files.

#### Solution:
1. Open user-auth.html
2. Press `Ctrl + Shift + R` (hard refresh)
3. Or clear browser cache:
   - Chrome: `Ctrl + Shift + Delete`
   - Select "Cached images and files"
   - Click "Clear data"

---

## Diagnostic Steps

### Step 1: Test Backend Health
```bash
curl https://hostelattendancesystem.onrender.com/actuator/health
```

**Expected Response:**
```json
{"status":"UP"}
```

### Step 2: Test OTP Endpoint Directly
```bash
curl -X POST https://hostelattendancesystem.onrender.com/api/auth/send-login-otp \
  -H "Content-Type: application/json" \
  -d '{"sic":"TEST123","email":"test@example.com"}'
```

**If Email Service Works:**
```json
{
  "success": true,
  "message": "OTP sent successfully",
  "otp": "123456"
}
```

**If Email Service Fails:**
```json
{
  "success": false,
  "message": "Failed to send OTP"
}
```

### Step 3: Check Render Logs
1. Go to Render Dashboard
2. Click on your service
3. Go to **Logs** tab
4. Try sending OTP from frontend
5. Watch for errors like:
   ```
   Failed to send email
   Authentication failed
   Could not connect to SMTP host
   ```

---

## Quick Fix Checklist

- [ ] **Environment Variables Set in Render**
  - [ ] `SPRING_MAIL_PASSWORD` = `<your-gmail-app-password>`
  - [ ] `SPRING_DATASOURCE_PASSWORD` = `<your-aiven-password>`

- [ ] **Backend is Running**
  - [ ] Health endpoint responds: `/actuator/health`
  - [ ] Service shows "Live" in Render dashboard

- [ ] **Frontend URL is Correct**
  - [ ] Check `user-auth.js` line 2
  - [ ] Should be: `https://hostelattendancesystem.onrender.com/api`

- [ ] **Browser Cache Cleared**
  - [ ] Hard refresh: `Ctrl + Shift + R`
  - [ ] Or clear cache completely

- [ ] **CORS is Working**
  - [ ] Use `api-test.html` to verify
  - [ ] No CORS errors in browser console

---

## Testing Tools Created

### 1. API Test Tool
**File:** `frontend/api-test.html`

**Usage:**
1. Open in browser
2. Click test buttons
3. See detailed results and errors

**Tests:**
- ✅ Health check
- ✅ CORS configuration
- ✅ Student API
- ✅ OTP sending

---

## Most Likely Solution

**90% chance the issue is:** Missing `SPRING_MAIL_PASSWORD` in Render environment variables.

**Fix:**
1. Go to Render Dashboard → Your Service → Environment
2. Add: `SPRING_MAIL_PASSWORD` = `<your-gmail-app-password>`
3. Save (auto-redeploys)
4. Wait 5-10 minutes for redeploy
5. Try sending OTP again

---

## Need More Help?

If none of these work, check:
1. **Render Logs** - Look for email errors when OTP is sent
2. **Browser Console** - Look for JavaScript errors or CORS blocks
3. **Network Tab** - See if request reaches backend or fails

The logs will tell you exactly what's failing!
