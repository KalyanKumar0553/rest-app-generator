# Deployment Configuration

## Build Configuration

The project is now configured for deployment with the following setup:

### Output Directory
- **Production build output**: `www/`
- **Development build output**: `target/classes/static`

### Build Commands
- **Production build**: `npm run build` (builds to `www/`)
- **Development build**: `npm run build:dev` (builds to `target/classes/static`)

### Deployment Platforms Supported

#### Netlify
Configuration file: `netlify.toml`
- Build command: `npm install && npm run build`
- Publish directory: `www`
- Node version: 20

#### Vercel
Configuration file: `vercel.json`
- Build command: `npm install && npm run build`
- Output directory: `www`

#### Manual Deployment
You can use the provided `build.sh` script:
```bash
./build.sh
```

### Key Configuration Files
1. **package.json** - Updated with production build command and Node engine requirements
2. **angular.json** - Configured with production build output to `www/`
3. **netlify.toml** - Netlify deployment configuration
4. **vercel.json** - Vercel deployment configuration
5. **.npmrc** - NPM configuration for handling peer dependencies
6. **build.sh** - Shell script for manual builds

### Requirements
- Node.js >= 18.0.0
- NPM >= 9.0.0

### Troubleshooting

If you encounter "command not found: ng", ensure:
1. Dependencies are installed first: `npm install`
2. The deployment platform is configured to run `npm install` before `npm run build`
3. Node version is 18 or higher
