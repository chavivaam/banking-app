import { Component, inject } from '@angular/core';
import { NgIf } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatIconModule } from '@angular/material/icon';
import { UserService } from '../../../core/services/user.service';

@Component({
  selector: 'app-add-user',
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
  templateUrl: './add-user.component.html',
  styleUrls: ['./add-user.component.scss']
})
export class AddUserComponent {
  private readonly userService = inject(UserService);
  private readonly snackBar = inject(MatSnackBar);
  private readonly fb = inject(FormBuilder);

  form = this.fb.group({
    userName: ['', [Validators.required, Validators.minLength(3)]],
    password: ['', [Validators.required, Validators.minLength(6)]],
    role: ['USER' as 'USER' | 'ADMIN', Validators.required]
  });

  loading = false;
  hidePassword = true;

  submit(): void {
    if (this.form.invalid) return;
    this.loading = true;
    const { userName, password, role } = this.form.value;
    this.userService.createUser({ userName: userName!, password: password!, role: role! })
      .subscribe({
        next: user => {
          this.snackBar.open(`User "${user.username}" created!`, 'OK', {
            duration: 3000,
            panelClass: 'snack-success'
          });
          this.form.reset({ role: 'USER' });
          this.loading = false;
        },
        error: () => {
          this.snackBar.open('Failed to create user.', 'OK', {
            duration: 4000,
            panelClass: 'snack-error'
          });
          this.loading = false;
        }
      });
  }
}
