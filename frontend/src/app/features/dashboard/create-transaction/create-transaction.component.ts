import { Component, EventEmitter, Output, inject } from '@angular/core';
import { NgIf } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatIconModule } from '@angular/material/icon';
import { TransactionService } from '../../../core/services/transaction.service';

@Component({
  selector: 'app-create-transaction',
  standalone: true,
  imports: [
    NgIf,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatSnackBarModule,
    MatIconModule
  ],
  templateUrl: './create-transaction.component.html',
  styleUrls: ['./create-transaction.component.scss']
})
export class CreateTransactionComponent {
  @Output() created = new EventEmitter<void>();

  private readonly txService = inject(TransactionService);
  private readonly snackBar = inject(MatSnackBar);
  private readonly fb = inject(FormBuilder);

  form = this.fb.group({
    type: ['CREDIT' as 'CREDIT' | 'DEBIT', Validators.required],
    amount: [null as number | null, [Validators.required, Validators.min(0.01)]],
    description: ['', [Validators.required, Validators.maxLength(200)]]
  });

  loading = false;
  collapsed = true;

  submit(): void {
    if (this.form.invalid) return;
    this.loading = true;
    const { type, amount, description } = this.form.value;
    this.txService.createTransaction({
      type: type!,
      amount: amount!,
      description: description!
    }).subscribe({
      next: () => {
        this.snackBar.open('Transaction created successfully!', 'OK', {
          duration: 3000,
          panelClass: 'snack-success'
        });
        this.form.reset({ type: 'CREDIT' });
        this.loading = false;
        this.created.emit();
      },
      error: () => {
        this.snackBar.open('Failed to create transaction.', 'OK', {
          duration: 4000,
          panelClass: 'snack-error'
        });
        this.loading = false;
      }
    });
  }
}
