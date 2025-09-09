# Deployment Guide for Blog App Backend

## Prerequisites

- Your code should be in a Git repository (GitHub, GitLab, etc.)
- Aiven PostgreSQL database credentials (already provided)

## Deploy to Render

### Step 1: Connect Repository

1. Go to [Render Dashboard](https://dashboard.render.com/)
2. Click "New +" and select "Web Service"
3. Connect your Git repository containing the blogapp

### Step 2: Configure Service

1. **Name**: `blogapp-backend`
2. **Environment**: `Java`
3. **Build Command**: `chmod +x ./mvnw && ./mvnw clean package -DskipTests`
4. **Start Command**: `java -jar target/blogapp-0.0.1-SNAPSHOT.jar`
5. **Plan**: Free (or choose paid for better performance)

### Step 3: Environment Variables

Add these environment variables in Render dashboard (DO NOT commit these to your repo):

| Key                      | Value                                                                                                                                          |
| ------------------------ | ---------------------------------------------------------------------------------------------------------------------------------------------- |
| `DATABASE_URL`           | `jdbc:postgresql://[your-aiven-host]:19214/defaultdb?ssl=require&user=[username]&password=[password]` |
| `SPRING_PROFILES_ACTIVE` | `production`                                                                                                                                   |
| `JWT_SECRET`             | Generate a secure random string (32+ characters)                                                                                               |
| `CORS_ORIGINS`           | `https://blog-app-kappa-two-40.vercel.app`                                                                                                     |

**Important**: Never commit database credentials to your repository. Always add them directly in the Render dashboard.

**Your Aiven Database URL**: Use your actual Aiven PostgreSQL connection string from your Aiven console.

### Step 4: Deploy

1. Click "Create Web Service"
2. Render will automatically build and deploy your application
3. You'll get a URL like `https://blogapp-backend.onrender.com`

## Update Frontend Configuration

After deployment, update your frontend to use the new backend URL:

- Replace `http://localhost:8082` with your Render URL
- CORS is already configured for your Vercel domain: `https://blog-app-kappa-two-40.vercel.app`

## Database Notes

- Your Aiven PostgreSQL database is already configured
- The application will automatically create/update tables on first run
- SSL is required and properly configured

## Troubleshooting

- Check Render logs if deployment fails
- Ensure all environment variables are set correctly
- Verify database connectivity in logs
- Make sure CORS origins match your frontend domain

## Alternative: Using render.yaml

You can also use the included `render.yaml` file for Infrastructure as Code deployment:

1. Update the `CORS_ORIGINS` value with your actual frontend URL
2. Commit the render.yaml file to your repository
3. In Render, select "Deploy from render.yaml" when creating the service
