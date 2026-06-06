import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, tap } from 'rxjs';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  name: string;
  email: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  userId: number; 
  email: string;
  role: string;
}

@Injectable({
  providedIn: 'root',
})
export class Auth {

  private apiUrl = '${environment.apiUrl}/api/auth';

  private isLoggedInSubject = new  BehaviorSubject<boolean>(this.hasToken());
  isLoggedIn$ = this.isLoggedInSubject.asObservable();

  constructor(private http: HttpClient) {}

  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, request)
      .pipe(
        tap(response => {
          localStorage.setItem('token', response.token);
          localStorage.setItem('email', response.email);
          localStorage.setItem('role', response.role);
          this.isLoggedInSubject.next(true);
        })
      );
  }

  register(request: RegisterRequest): Observable<string> {
    return this.http.post(`${this.apiUrl}/register`, request, 
      { responseType: 'text' });
  }

  logout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('email');
    localStorage.removeItem('role');
    this.isLoggedInSubject.next(false);
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }

  getEmail(): string | null {
    return localStorage.getItem('email');
  }

  getUserId(): number | null {
    return localStorage.getItem('userId') ? Number(localStorage.getItem('userId')) : null;
  };
  
  private hasToken(): boolean {
    return !!localStorage.getItem('token');
  }
}
