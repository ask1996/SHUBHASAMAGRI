# ShubhaSamagri - FREE Deployment Guide

## Architecture: Backend on Render + Frontend on Netlify

```
User → Netlify (React SPA) → Render (Spring Boot API) → Database
```

---

## PART 1: Deploy Backend on Render.com

### Prerequisites
- GitHub account with the project pushed
- Render.com free account (render.com)

### Step 1: Prepare Backend for Production

1. Ensure `application-prod.properties` exists (already done)
2. The `pom.xml` has spring-boot-maven-plugin configured

### Step 2: Create Render Web Service

1. Go to [render.com](https://render.com) → **New** → **Web Service**
2. Connect your GitHub repository
3. Configure the service:

| Field            | Value                                         |
|------------------|-----------------------------------------------|
| Name             | `shubha-samagri-api`                         |
| Root Directory   | `backend`                                     |
| Runtime          | **Java**                                      |
| Build Command    | `mvn clean package -DskipTests`              |
| Start Command    | `java -jar target/shubha-samagri-backend-1.0.0.jar` |
| Instance Type    | **Free**                                      |

### Step 3: Set Environment Variables on Render

In Render dashboard → Your Service → **Environment**:

| Variable                | Value                                   |
|-------------------------|-----------------------------------------|
| `SPRING_PROFILES_ACTIVE`| `prod`                                  |
| `DATABASE_URL`          | (from database step below)              |
| `DATABASE_USERNAME`     | (from database step below)              |
| `DATABASE_PASSWORD`     | (from database step below)              |
| `JWT_SECRET`            | (generate a random 64-char string)      |
| `CORS_ALLOWED_ORIGINS`  | `https://your-app.netlify.app`          |
| `PORT`                  | `8080`                                  |

**Generate JWT Secret:**
```bash
openssl rand -base64 64
```

### Step 4: Setup Free MySQL Database (Railway)

1. Go to [railway.app](https://railway.app) → New Project → **MySQL**
2. Click MySQL service → **Connect** tab
3. Copy the connection string: `mysql://user:password@host:port/railway`
4. Convert to Spring format:
   ```
   DATABASE_URL=jdbc:mysql://host:port/railway?useSSL=true&allowPublicKeyRetrieval=true
   DATABASE_USERNAME=root
   DATABASE_PASSWORD=<password-from-railway>
   ```

**Alternative - PlanetScale (free MySQL):**
1. Go to [planetscale.com](https://planetscale.com) → Create database
2. Create branch `main` → Connect → Framework: **Java**
3. Copy the connection URL

---

## PART 2: Deploy Frontend on Netlify

### Step 1: Build Configuration

Create `frontend/netlify.toml`:
```toml
[build]
  base = "frontend"
  command = "npm run build"
  publish = "dist"

[[redirects]]
  from = "/*"
  to = "/index.html"
  status = 200
```

### Step 2: Environment Variable for API URL

Create `frontend/.env.production`:
```
VITE_API_BASE_URL=https://shubha-samagri-api.onrender.com/api
```

Update `frontend/src/api/axios.js`:
```javascript
const axiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  ...
})
```

### Step 3: Deploy to Netlify

**Option A: Netlify CLI**
```bash
npm install -g netlify-cli
cd frontend
npm run build
netlify deploy --prod --dir=dist
```

**Option B: GitHub Integration**
1. Go to [netlify.com](https://netlify.com) → **New site from Git**
2. Connect your GitHub repo
3. Build settings:
   - Base directory: `frontend`
   - Build command: `npm run build`
   - Publish directory: `frontend/dist`
4. Add environment variable: `VITE_API_BASE_URL = https://shubha-samagri-api.onrender.com/api`

---

## PART 3: Run Locally

### Backend
```bash
cd backend
mvn spring-boot:run
# API: http://localhost:8080
# Swagger: http://localhost:8080/swagger-ui.html
# H2 Console: http://localhost:8080/h2-console
```

### Frontend
```bash
cd frontend
npm install
npm run dev
# App: http://localhost:5173
```

### Test Credentials (loaded by DataInitializer)
- **Admin:** admin@shubhasamagri.com / admin123
- **User:** ravi@example.com / password123

---

## PART 4: Verification Checklist

After deployment, verify:
- [ ] `GET https://shubha-samagri-api.onrender.com/api/occasions` returns JSON
- [ ] `POST /api/auth/login` returns JWT token
- [ ] Frontend loads at `https://your-app.netlify.app`
- [ ] Occasions display on home page (API call works)
- [ ] Login works and cart is accessible
- [ ] H2 console disabled in production (`spring.h2.console.enabled=false`)

---

## PART 5: Production Checklist

- [ ] JWT secret is at least 256 bits (32 chars minimum, 64+ recommended)
- [ ] `SPRING_PROFILES_ACTIVE=prod` set on Render
- [ ] CORS configured with exact Netlify domain (no trailing slash)
- [ ] `spring.jpa.show-sql=false` in prod
- [ ] Logging level set to INFO/WARN in prod
- [ ] Database backup configured
- [ ] HTTPS enforced (Render and Netlify do this automatically)

---

## Cost: $0/month

| Service  | Free Tier Limits                          |
|----------|-------------------------------------------|
| Render   | 512 MB RAM, spins down after 15min idle   |
| Netlify  | 100 GB bandwidth, 300 build minutes/month |
| Railway  | $5 credit/month (usually sufficient)      |

> **Note:** Render free tier spins down after inactivity. First request after idle takes ~30 seconds to wake up. For production, upgrade to Render Starter ($7/month) for always-on.
