import { Component, Input, OnChanges } from '@angular/core';
import { NgIf, DatePipe, CurrencyPipe } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatSortModule, Sort } from '@angular/material/sort';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';
import { TransactionDTO } from '../../../core/models/transaction.model';

@Component({
  selector: 'app-transaction-list',
  standalone: true,
  imports: [
    NgIf,
    DatePipe,
    CurrencyPipe,
    MatTableModule,
    MatSortModule,
    MatCardModule,
    MatChipsModule,
    MatIconModule
  ],
  templateUrl: './transaction-list.component.html',
  styleUrls: ['./transaction-list.component.scss']
})
export class TransactionListComponent implements OnChanges {
  @Input() transactions: TransactionDTO[] = [];
  @Input() showUsername = false;

  dataSource: TransactionDTO[] = [];

  get displayedColumns(): string[] {
    const cols = ['createdDate', 'type', 'amount', 'description'];
    return this.showUsername ? ['username', ...cols] : cols;
  }

  ngOnChanges(): void {
    this.dataSource = [...this.transactions];
  }

  onSort(sort: Sort): void {
    if (!sort.active || sort.direction === '') {
      this.dataSource = [...this.transactions];
      return;
    }
    this.dataSource = [...this.transactions].sort((a, b) => {
      const dir = sort.direction === 'asc' ? 1 : -1;
      if (sort.active === 'amount') return (a.amount - b.amount) * dir;
      if (sort.active === 'createdDate') {
        return (new Date(a.createdDate).getTime() - new Date(b.createdDate).getTime()) * dir;
      }
      return 0;
    });
  }
}
