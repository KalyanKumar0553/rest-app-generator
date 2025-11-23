import { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
<<<<<<< HEAD
  appId: 'com.quadprosol.app',
  appName: 'QuadProSol',
  webDir: 'dist/demo/browser',
  server: {
    androidScheme: 'https'
  },
  plugins: {
    SplashScreen: {
      launchShowDuration: 2000,
      backgroundColor: '#059669',
      showSpinner: false
    },
    StatusBar: {
      style: 'dark',
      backgroundColor: '#059669'
    }
  }
=======
  appId: 'io.ionic.starter',
  appName: 'proserv-admin-app',
  webDir: 'www'
>>>>>>> 6947445 (For Adding responsive UI and fixing bugs)
};

export default config;