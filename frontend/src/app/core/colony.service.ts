import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map, Observable } from 'rxjs';

export interface BackendColony {
  id: number;
  name: string;
  description: string;
  location: string;
  active: boolean;
}

@Injectable({ providedIn: 'root' })
export class ColonyService {
  private readonly apiUrl = 'http://localhost:8080/api';

  constructor(private readonly http: HttpClient) {}

  getColonies(): Observable<BackendColony[]> {
    return this.http.get<BackendColony[]>(`${this.apiUrl}/colonies`, { withCredentials: true });
  }
}
