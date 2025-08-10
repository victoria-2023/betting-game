# 🎨 Deploy to Render (Free Tier Available)

## Step 1: Go to Render
1. Visit: https://render.com
2. Sign up with GitHub

## Step 2: Create Web Service
1. Click "New +" → "Web Service"
2. Connect your GitHub: `victoria-2023/betting-game`
3. Render detects your `render.yaml` automatically!

## Step 3: Configuration (Auto-detected)
```yaml
# Your render.yaml is already configured:
services:
  - type: web
    runtime: docker
    dockerfilePath: ./Dockerfile.simple
    buildCommand: ""
    startCommand: ""
```

## Step 4: Deploy
- Render builds your Docker image
- Deploys automatically
- Provides HTTPS URL: `https://betting-game.onrender.com`

## What Render Provides
- 🆓 Free tier (with sleep after 15min idle)
- 🔒 Automatic HTTPS
- 🌐 Custom domains
- 📊 Built-in monitoring
- 🔄 Auto-deploys from GitHub

## Free Tier Limits
- ✅ Perfect for portfolio/demo
- ⚠️ Sleeps after 15min idle (wakes up in 30s)
- ⚠️ 750 hours/month free

## Paid Tier ($7/month)
- ✅ No sleeping
- ✅ Always-on
- ✅ Better performance
