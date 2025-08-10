# 🔄 Deployment Pipeline Example

## GitHub Actions (Auto-Deploy)
```yaml
name: Deploy Betting Game

on:
  push:
    branches: [main]

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Build Docker Image
        run: |
          docker build -f Dockerfile.simple -t betting-game .
          
      - name: Deploy to Production
        run: |
          docker tag betting-game ${{ secrets.REGISTRY_URL }}/betting-game
          docker push ${{ secrets.REGISTRY_URL }}/betting-game
          
      - name: Update Production Server
        run: |
          ssh production-server "docker pull betting-game && docker restart betting-game"
```

## Benefits
- ✅ **Automatic deployment** when you push code
- ✅ **Zero-downtime** updates
- ✅ **Rollback capability** if issues occur
- ✅ **Consistent environments** dev → staging → production
