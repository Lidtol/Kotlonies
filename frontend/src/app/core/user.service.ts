import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface BackendUser {
  id: number;
  nombre: string;
  email: string;
  rol: 'ADMIN' | 'CUIDADOR' | 'CONSULTA';
  activo: boolean;
}

export interface UserPayload {
  nombre: string;
  email: string;
  password: string;
  rol: string;
  activo: boolean;
}

@Injectable({ providedIn: 'root' })
export class UserService {
  private readonly http = inject(HttpClient);

  getUsers(): Observable<BackendUser[]> {
    return this.http.get<BackendUser[]>('http://localhost:8080/api/usuarios', { withCredentials: true });
  }

  createUser(payload: UserPayload): Observable<BackendUser> {
    return this.http.post<BackendUser>('http://localhost:8080/api/usuarios', payload, { withCredentials: true });
  }
}