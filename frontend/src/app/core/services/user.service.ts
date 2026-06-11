import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CreateUserRequest, UserDTO } from '../models/user.model';

@Injectable({ providedIn: 'root' })
export class UserService {
  private readonly http = inject(HttpClient);

  createUser(req: CreateUserRequest): Observable<UserDTO> {
    return this.http.post<UserDTO>('/user/create', req);
  }

  getAllUsers(): Observable<UserDTO[]> {
    return this.http.get<UserDTO[]>('/user/getAllUsers');
  }
}
