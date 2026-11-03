import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface BackendColony {
  id: number;
  name: string;
  description: string | null;
  location: string;
  active: boolean;
}

export interface ColonyPayload {
  name: string;
  description: string | null;
  location: string;
  active: boolean;
}

@Injectable({ providedIn: 'root' })
export class ColonyService {
  private readonly http = inject(HttpClient);
  private readonly url = 'http://localhost:8080/api/colonies';
  private readonly opts = { withCredentials: true };

  getColonies(): Observable<BackendColony[]> {
    return this.http.get<BackendColony[]>(this.url, this.opts);
  }
  createColony(payload: ColonyPayload): Observable<BackendColony> {
    return this.http.post<BackendColony>(this.url, payload, this.opts);
  }
  updateColony(id: number, payload: ColonyPayload): Observable<BackendColony> {
    return this.http.put<BackendColony>(`${this.url}/${id}`, payload, this.opts);
  }
  archiveColony(id: number): Observable<BackendColony> {
    return this.http.patch<BackendColony>(`${this.url}/${id}/archive`, {}, this.opts);
  }
}