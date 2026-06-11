import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { NgIf } from '@angular/common';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { AuthService } from '../../core/services/auth.service';
import { TransactionService } from '../../core/services/transaction.service';
import { TransactionDTO } from '../../core/models/transaction.model';
import { TransactionListComponent } from './transaction-list/transaction-list.component';
import { CreateTransactionComponent } from './create-transaction/create-transaction.component';
import { AddUserComponent } from './add-user/add-user.component';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    NgIf,
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    MatProgressBarModule,
    MatTooltipModule,
    TransactionListComponent,
    CreateTransactionComponent,
    AddUserComponent
  ],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {
  protected readonly auth = inject(AuthService);
  private readonly txService = inject(TransactionService);

  readonly isAdmin = computed(() => this.auth.currentUser()?.role === 'ADMIN');

  transactions = signal<TransactionDTO[]>([]);
  loading = signal(true);

  ngOnInit(): void {
    this.loadTransactions();
  }

  loadTransactions(): void {
    this.loading.set(true);
    const call = this.isAdmin()
      ? this.txService.getAllTransactions()
      : this.txService.getMyTransactions();

    call.subscribe({
      next: data => { this.transactions.set(data); this.loading.set(false); },
      error: () => this.loading.set(false)
    });
  }

  logout(): void {
    this.auth.logout();
  }
}
