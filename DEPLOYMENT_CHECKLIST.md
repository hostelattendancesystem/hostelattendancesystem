# 🚀 Render Deployment Checklist

## ✅ Pre-Deployment Checklist

### 1. Files Created/Updated
- [x] `Dockerfile` - Multi-stage Docker build configuration
- [x] `.dockerignore` - Excludes unnecessary files from Docker build
- [x] `render.yaml` - Infrastructure as Code for Render
- [x] `pom.xml` - Updated with Spring Boot Actuator
- [x] `src/main/resources/application-production.properties` - Production config
- [x] `src/main/java/com/college/attendance/config/CorsConfig.java` - CORS configuration
- [x] `RENDER_DEPLOYMENT_GUIDE.md` - Detailed deployment instructions
- [x] `RENDER_ENV_VARIABLES.txt` - Environment variables reference
- [x] `docker-build.bat` - Local Docker testing script
- [x] `docker-cleanup.bat` - Docker cleanup script

### 2. Code Repository
- [ ] All files committed to Git
- [ ] Pushed to GitHub repository
- [ ] Repository is public or Render has access

### 3. External Services
- [x] Aiven PostgreSQL database is running
- [x] Database credentials are correct
- [x] Gmail SMTP credentials are valid
- [ ] Test database connection locally

### 4. Render Account
- [ ] Render account created (free tier)
- [ ] GitHub account connected to Render

---

## 📋 Deployment Steps

### Step 1: Test Locally (Optional but Recommended)
```bash
# If you have Docker installed:
docker-build.bat

# Test the health endpoint:
# http://localhost:8081/actuator/health
```

### Step 2: Push to GitHub
```bash
git add .
git commit -m "Add Render deployment configuration"
git push origin main
```

### Step 3: Deploy to Render

#### Option A: Blueprint (Automated - Recommended)
1. Go to https://dashboard.render.com/
2. Click "New +" → "Blueprint"
3. Select your GitHub repository
4. Render detects `render.yaml` automatically
5. Add secret environment variables:
   - `SPRING_DATASOURCE_PASSWORD`
   - `SPRING_MAIL_PASSWORD`
6. Click "Apply"

#### Option B: Manual
1. Go to https://dashboard.render.com/
2. Click "New +" → "Web Service"
3. Connect GitHub repository
4. Configure:
   - Name: `attendance-automation-backend`
   - Region: Singapore
   - Branch: `main`
   - Runtime: Docker
   - Plan: Free
5. Add all environment variables from `RENDER_ENV_VARIABLES.txt`
6. Click "Create Web Service"

### Step 4: Monitor Deployment
- [ ] Watch build logs in Render dashboard
- [ ] Wait for "Live" status (5-10 minutes)
- [ ] Check health endpoint: `https://your-service.onrender.com/actuator/health`

### Step 5: Test Backend
- [ ] Health check returns `{"status":"UP"}`
- [ ] Database connection successful
- [ ] Test API endpoints
- [ ] Verify scheduled tasks are running

### Step 6: Update Frontend
- [ ] Update frontend API base URL to Render URL
- [ ] Add frontend URL to CORS configuration
- [ ] Test frontend-backend integration

---

## 🔍 Verification Tests

### Health Check
```bash
curl https://your-service.onrender.com/actuator/health
```
Expected: `{"status":"UP"}`

### Test Student Registration
```bash
curl -X POST https://your-service.onrender.com/api/students/register \
  -H "Content-Type: application/json" \
  -d '{
    "sic": "TEST123",
    "name": "Test Student",
    "email": "test@example.com",
    "phoneNumber": "1234567890"
  }'
```

### Test OTP Generation
```bash
curl -X POST https://your-service.onrender.com/api/auth/admin/send-otp
```

---

## 🐛 Troubleshooting

### Build Fails
- [ ] Check Java version is 17
- [ ] Verify all dependencies in pom.xml
- [ ] Review build logs in Render

### Database Connection Issues
- [ ] Verify Aiven database is running
- [ ] Check connection string format
- [ ] Ensure SSL mode is enabled
- [ ] Test connection locally first

### Application Won't Start
- [ ] Check environment variables are set
- [ ] Review application logs
- [ ] Verify health check path is correct
- [ ] Check memory limits (512MB for free tier)

### Email Not Sending
- [ ] Verify Gmail credentials
- [ ] Check App Password is valid
- [ ] Review email service logs
- [ ] Test email locally first

---

## 📊 Post-Deployment

### Monitoring
- [ ] Set up health check monitoring
- [ ] Review application logs regularly
- [ ] Monitor database connections
- [ ] Check email delivery

### Performance
- [ ] Test API response times
- [ ] Monitor memory usage
- [ ] Check database query performance
- [ ] Verify scheduled tasks run on time

### Security
- [ ] All sensitive data in environment variables
- [ ] CORS configured for frontend only
- [ ] Database uses SSL/TLS
- [ ] No credentials in code

---

## 🎯 Success Criteria

Your deployment is successful when:
- ✅ Health endpoint returns `{"status":"UP"}`
- ✅ Database connection is established
- ✅ Student registration works
- ✅ OTP emails are sent successfully
- ✅ Scheduled attendance checks run
- ✅ Frontend can communicate with backend
- ✅ No errors in application logs

---

## 📞 Support Resources

- **Render Documentation**: https://render.com/docs
- **Render Status**: https://status.render.com/
- **Spring Boot Docs**: https://spring.io/projects/spring-boot
- **Aiven Support**: https://aiven.io/support

---

## 🔄 Continuous Deployment

After initial deployment, any push to `main` branch will:
1. Trigger automatic rebuild on Render
2. Run tests (if configured)
3. Deploy new version
4. Health check validates deployment
5. Rollback if health check fails

---

**Deployment Prepared**: January 28, 2026  
**Target Platform**: Render (Free Tier)  
**Database**: Aiven PostgreSQL  
**Region**: Singapore  
**Estimated Build Time**: 5-10 minutes  
**Estimated Deployment Time**: 2-3 minutes
