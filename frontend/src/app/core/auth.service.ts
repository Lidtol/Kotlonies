import { Injectable, computed, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { tap } from 'rxjs';

export interface UsuarioPublico {
  id: number;
  nombre: string;
  email: string;
  rol: 'ADMIN' | 'CUIDADOR' | 'CONSULTA';
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly apiUrl = 'http://localhost:8080/api';

  /** Usuario en sesión; null mientras no se haya autenticado. */
  readonly usuario = signal<UsuarioPublico | null>(null);

  /** Cierto mientras se desconoce si hay sesión abierta en el backend. */
  readonly cargando = signal(true);

  readonly isLoggedIn = computed(() => this.usuario() !== null);

  constructor(private readonly http: HttpClient) {
    // Si hay sesión abierta en el backend al recargar, recuperamos el usuario.
    this.http.get<UsuarioPublico>(`${this.apiUrl}/auth/sesion`, { withCredentials: true })
      .subscribe({
        next: usuario => {
          this.usuario.set(usuario);
          this.cargando.set(false);
        },
        error: () => {
          this.usuario.set(null);
          this.cargando.set(false);
        }
      });
  }

  login(email: string, password: string) {
    return this.http.post<UsuarioPublico>(
      `${this.apiUrl}/auth/login`,
      { email, password },
      { withCredentials: true }
    ).pipe(tap(usuario => this.usuario.set(usuario)));
  }

  logout() {
    return this.http.post<void>(`${this.apiUrl}/auth/logout`, {}, { withCredentials: true })
      .pipe(tap(() => this.usuario.set(null)));
  }
}
