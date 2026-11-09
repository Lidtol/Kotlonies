
import { HttpInterceptorFn } from '@angular/common/http';

const API_URL = 'http://localhost:8080/api';

export const csrfInterceptor: HttpInterceptorFn = (req, next) => {
  const methods = ['POST', 'PUT', 'PATCH', 'DELETE'];

  const isApiRequest = req.url.startsWith(API_URL);
  const requiresCsrf = methods.includes(req.method);

  if (!isApiRequest || !requiresCsrf) {
    return next(req);
  }

  const cookie = document.cookie
    .split('; ')
    .find(value => value.startsWith('XSRF-TOKEN='));

  if (!cookie) {
    console.error('No se ha encontrado la cookie XSRF-TOKEN.');
    return next(req);
  }

  const token = decodeURIComponent(cookie.substring('XSRF-TOKEN='.length));

  return next(
    req.clone({
      setHeaders: {
        'X-XSRF-TOKEN': token
      }
    })
  );
};
