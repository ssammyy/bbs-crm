import { HttpInterceptorFn } from '@angular/common/http';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
    const token = localStorage.getItem('auth_token');
    const publicUrls = ['/auth/register', '/auth/login'];

    // check if the request url matches  any of public endpoints
    if (publicUrls.some(url => req.url.includes(url))){
        return next(req);
    }

    if (token) {
        const cloned = req.clone({
            setHeaders: { Authorization: `Bearer ${token}` }
        });
        return next(cloned);
    }
    return next(req);
};
