
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


export interface UserUpdatePayload {
  nombre: string;
  email: string;
  password?: string | null;
  rol: string;
  activo: boolean;
}


@Injectable({ providedIn: 'root' })
export class UserService {
  private readonly http = inject(HttpClient);

  getUsers(): Observable<BackendUser[]> {
    return this.http.get<BackendUser[]>(
      'http://localhost:8080/api/usuarios',
      { withCredentials: true }
    );


  }


updateUser(id: number, payload: UserUpdatePayload): Observable<BackendUser> {
  return this.http.put<BackendUser>(
    `http://localhost:8080/api/usuarios/${id}`,
    payload,
    { withCredentials: true }
  );
}


  createUser(payload: UserPayload): Observable<BackendUser> {
    return this.http.post<BackendUser>(
      'http://localhost:8080/api/usuarios',
      payload,
      { withCredentials: true }
    );
  }

  deleteUser(id: number): Observable<void> {
    return this.http.delete<void>(
      `http://localhost:8080/api/usuarios/${id}`,
      { withCredentials: true }
    );
  }
}
