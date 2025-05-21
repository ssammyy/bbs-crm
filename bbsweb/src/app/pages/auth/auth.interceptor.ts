import { HttpInterceptorFn, HttpErrorResponse, HttpEventType } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { MessageService } from 'primeng/api';
import { throwError, tap } from 'rxjs';
import { catchError } from 'rxjs/operators';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
    const token = localStorage.getItem('auth_token');
    const router = inject(Router);
    const messageService = inject(MessageService);

    const publicUrls = ['/auth/register', '/auth/login'];

    // Skip authentication for public URLs
    if (publicUrls.some(url => req.url.includes(url))) {
        return next(req);
    }

    // Clone request and add Authorization header if token exists
    const clonedRequest = token ? req.clone({
        setHeaders: { Authorization: `Bearer ${token}` }
    }) : req;

    return next(clonedRequest).pipe(
        tap(event => {
            if (event.type === HttpEventType.Response) {
                // Check for successful status (200-299)
                if (event.status >= 200 && event.status < 300) {
                    // Successful response, no error handling needed.
                    return event;
                }
                //return event if the event is not successful.
                return event;
            }
            // Return event if the event type is not response.
            return event;
        }),
        // catchError((error: HttpErrorResponse) => {
        //     let errorMessage = 'An unknown error occurred!';
        //
        //     if (error.error?.message) {
        //         errorMessage = error.error.message; // Use API error message if available
        //     }
        //
        //     switch (error.status) {
        //         case 401:
        //             errorMessage = 'Unauthorized! Please log in again.';
        //             router.navigate(['/']); // Redirect to login
        //             break;
        //         case 403:
        //             errorMessage = 'Forbidden! You do not have permission.';
        //             router.navigate(['/']); // Redirect to login
        //
        //             break;
        //         case 500:
        //             errorMessage = 'Server error! Please try again later.';
        //             break;
        //         default:
        //             errorMessage = `Error: ${error.status} - ${error.statusText}`;
        //             break;
        //     }
        //
        //     messageService.add({ severity: 'error', summary: 'Error', detail: errorMessage });
        //     return throwError(() => new Error(errorMessage));
        // })
    );
};
