// This file can be replaced during build by using the `fileReplacements` array.
// `ng build --prod` replaces `environment.ts` with `environment.prod.ts`.
// The list of file replacements can be found in `angular.json`.

export const environment = {
  LOGIN_REDIRECT_URI: 'http://localhost:4200/callback',
  LOGOUT_REDIRECT_URI: 'http://localhost:4200/',
  nvflatServicePath: 'http://localhost:8083/NVFLAT/',
  nvhomeServicePath: 'http://localhost:8084/NVHOME/',
  production: false
};

/*
 * For easier debugging in development mode, you can import the following file
 * to ignore zone related response stack frames such as `zone.run`, `zoneDelegate.invokeTask`.
 *
 * This import should be commented out in production mode because it will have a negative impact
 * on performance if an response is thrown.
 */
// import 'zone.js/dist/zone-response';  // Included with Angular CLI.
