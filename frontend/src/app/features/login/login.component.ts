import { Component, EventEmitter, Output, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { AuthService } from '../../core/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  @Output() readonly login = new EventEmitter<void>();

  private readonly auth = inject(AuthService);

  email = '';
  password = '';
  rememberMe = true;

  submitted = false;
  loading = false;
  errorMessage: string | null = null;

  get formValid(): boolean {
    return this.email.trim().length > 0 && this.password.length > 0;
  }

  submit(): void {
    this.submitted = true;
    if (!this.formValid) {
      this.errorMessage = 'Introduce tu correo y tu contraseña para continuar.';
      return;
    }

    this.loading = true;
    this.errorMessage = null;

    this.auth.login(this.email.trim(), this.password).subscribe({
      next: () => {
        this.loading = false;
        this.login.emit();
      },
      error: (error: HttpErrorResponse) => {
        this.loading = false;
        this.errorMessage = this.mensajeError(error);
      }
    });
  }

  private mensajeError(error: HttpErrorResponse): string {
    if (error.status === 401) {
      return error.error?.mensaje ?? 'Correo o contraseña incorrectos.';
    }
    if (error.status === 0) {
      return 'No hay conexión con el servidor. Comprueba que la API está levantada (puerto 8080).';
    }
    return 'No se ha podido iniciar sesión. Inténtalo de nuevo.';
  }
}
