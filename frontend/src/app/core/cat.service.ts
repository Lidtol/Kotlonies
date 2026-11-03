import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface BackendCat {
  id: number;
  name: string | null;
  sex: string | null;
  birthDate: string | null;
  color: string | null;
  status: string;
  neutered: boolean | null;
  identified: boolean | null;
  colonyId: number | null;
  colonyName: string | null;
  observations: string | null;
}

export type CatPayload = Omit<BackendCat, 'id' | 'colonyName'>;

@Injectable({ providedIn: 'root' })
export class CatService {
  private readonly http = inject(HttpClient);
  private readonly url = 'http://localhost:8080/api/cats';
  private readonly opts = { withCredentials: true };

  getCats(): Observable<BackendCat[]> {
    return this.http.get<BackendCat[]>(this.url, this.opts);
  }
  createCat(payload: CatPayload): Observable<BackendCat> {
    return this.http.post<BackendCat>(this.url, payload, this.opts);
  }
  updateCat(id: number, payload: CatPayload): Observable<BackendCat> {
    return this.http.put<BackendCat>(`${this.url}/${id}`, payload, this.opts);
  }
  deleteCat(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`, this.opts);
  }
}