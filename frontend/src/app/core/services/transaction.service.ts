import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { TransactionDTO, TransactionRequest } from '../models/transaction.model';

@Injectable({ providedIn: 'root' })
export class TransactionService {
  private readonly http = inject(HttpClient);

  getMyTransactions(): Observable<TransactionDTO[]> {
    return this.http.get<TransactionDTO[]>('/transaction/getAllTransactions');
  }

  getAllTransactions(): Observable<TransactionDTO[]> {
    return this.http.get<TransactionDTO[]>('/transaction/getAllUsersTransactions');
  }

  createTransaction(req: TransactionRequest): Observable<TransactionDTO> {
    return this.http.post<TransactionDTO>('/transaction/create', req);
  }
}
