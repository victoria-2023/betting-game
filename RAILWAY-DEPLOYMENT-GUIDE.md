# 🚂 Railway Deployment - Step by Step

## 🎯 Live Deployment Guide

### Step 1: Open Railway
1. Go to: https://railway.app
2. Click "Start a New Project"

### Step 2: Connect GitHub
1. Click "Login with GitHub"
2. Authorize Railway access
3. Click "Deploy from GitHub repo"

### Step 3: Select Repository
1. Find: `victoria-2023/betting-game`
2. Click "Deploy Now"

### Step 4: Wait for Deployment (2-3 minutes)
Railway automatically:
- 🔍 Detects `Dockerfile.simple`
- 🐳 Builds Docker image
- 🚀 Deploys your app
- 🌐 Creates public URL

### Step 5: Get Your Live URL
- Railway will show: `https://betting-game-production-xxxx.up.railway.app`
- Your betting game is now LIVE on the internet! 🎉

## 🔧 If You See Any Issues

### Issue: "No Dockerfile found"
**Solution**: Make sure Railway uses `Dockerfile.simple`
1. Go to Settings → Build
2. Set "Dockerfile Path" to: `Dockerfile.simple`

### Issue: "Port binding failed"
**Solution**: Railway should auto-detect PORT
1. Go to Variables
2. Add: `PORT` = `8080` (Railway usually sets this automatically)

### Issue: "Build timeout"
**Solution**: Railway free tier has build limits
1. Your app should build in ~2 minutes
2. If timeout, try again (free tier can be busy)

## 🎮 Testing Your Live App

Once deployed, test these features:
1. **Main page**: `https://your-app.railway.app/`
2. **Health check**: `https://your-app.railway.app/actuator/health`
3. **Game functionality**: Place bets, watch real-time updates
4. **WebSocket**: Real-time betting should work

## 🔄 Auto-Deployments

Every time you push to GitHub:
1. Railway automatically detects changes
2. Rebuilds your Docker image
3. Deploys new version
4. Zero downtime updates!

## 💰 Railway Pricing
- 🆓 $5/month free credit
- 📊 Usage-based pricing
- 💡 Perfect for portfolio projects
- 🚀 Scales automatically
