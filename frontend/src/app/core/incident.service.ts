
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export type IncidentType =
  | 'SALUD'
  | 'ALIMENTACION'
  | 'ABANDONO'
  | 'SEGURIDAD'
  | 'OTRO';

export type IncidentPriority =
  | 'BAJA'
  | 'MEDIA'
  | 'ALTA'
  | 'URGENTE';

export type IncidentStatus =
  | 'ABIERTA'
  | 'EN_PROCESO'
  | 'RESUELTA'
  | 'CERRADA';

export interface BackendIncident {
  id: number;
  titulo: string;
  descripcion: string;
  tipo: IncidentType;
  prioridad: IncidentPriority;
  estado: IncidentStatus;
  fechaCreacion: string;
  catId: number | null;
  colonyId: number | null;
  catName: string | null;
  colonyName: string | null;
}

export type IncidentPayload = Pick<
  BackendIncident,
  | 'titulo'
  | 'descripcion'
  | 'tipo'
  | 'prioridad'
  | 'estado'
  | 'catId'
  | 'colonyId'
>;

@Injectable({
  providedIn: 'root'
})
export class IncidentService {

  private readonly http = inject(HttpClient);

  private readonly url = 'http://localhost:8080/api/incidencias';

  private readonly opts = { withCredentials: true };

  getIncidents(): Observable<BackendIncident[]> {
    return this.http.get<BackendIncident[]>(this.url, this.opts);
  }

  createIncident(payload: IncidentPayload): Observable<BackendIncident> {
    return this.http.post<BackendIncident>(
      this.url,
      payload,
      this.opts
    );
  }

  updateIncident(
    id: number,
    payload: IncidentPayload
  ): Observable<BackendIncident> {
    return this.http.put<BackendIncident>(
      `${this.url}/${id}`,
      payload,
      this.opts
    );
  }

  deleteIncident(id: number): Observable<void> {
    return this.http.delete<void>(
      `${this.url}/${id}`,
      this.opts
    );
  }
}
