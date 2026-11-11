import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export type TareaTipo = 'ALIMENTACION' | 'VISITA' | 'VETERINARIO' | 'REVISION' | 'OTRO';
export type TareaEstado = 'PENDIENTE' | 'COMPLETADA';

export interface BackendTarea {
  id: number;
  titulo: string;
  descripcion: string | null;
  tipo: TareaTipo;
  estado: TareaEstado;
  fechaHora: string;
  catId: number | null;
  catName: string | null;
  colonyId: number | null;
  colonyName: string | null;
}

export type TareaPayload = {
  titulo: string;
  descripcion: string | null;
  tipo: TareaTipo;
  fechaHora: string;
  estado?: TareaEstado | null;
  catId: number | null;
  colonyId: number | null;
};

@Injectable({ providedIn: 'root' })
export class TareaService {
  private readonly http = inject(HttpClient);
  private readonly url = 'http://localhost:8080/api/tareas';
  private readonly opts = { withCredentials: true };

  getTareas(): Observable<BackendTarea[]> {
    return this.http.get<BackendTarea[]>(this.url, this.opts);
  }

  getTarea(id: number): Observable<BackendTarea> {
    return this.http.get<BackendTarea>(`${this.url}/${id}`, this.opts);
  }

  createTarea(payload: TareaPayload): Observable<BackendTarea> {
    return this.http.post<BackendTarea>(this.url, payload, this.opts);
  }

  updateTarea(id: number, payload: TareaPayload): Observable<BackendTarea> {
    return this.http.put<BackendTarea>(`${this.url}/${id}`, payload, this.opts);
  }

  completarTarea(id: number, completada = true): Observable<BackendTarea> {
    return this.http.patch<BackendTarea>(`${this.url}/${id}/completar`, { completada }, this.opts);
  }

  deleteTarea(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`, this.opts);
  }
}
