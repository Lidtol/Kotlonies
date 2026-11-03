import { TestBed } from '@angular/core/testing';
import { signal } from '@angular/core';
import { of } from 'rxjs';

import { AppComponent } from './app.component';
import { AuthService, UsuarioPublico } from './core/auth.service';
import { ColonyService } from './core/colony.service';
import { CatService } from './core/cat.service';
import { UserService } from './core/user.service';

describe('AppComponent', () => {
  const usuario = signal<UsuarioPublico | null>(null);
  const cargando = signal(false);

  beforeEach(async () => {
    usuario.set(null);
    cargando.set(false);

    await TestBed.configureTestingModule({
      imports: [AppComponent],
      providers: [
        {
          provide: AuthService,
          useValue: {
            usuario,
            cargando,
            logout: () => of(void 0)
          }
        },
        {
          provide: ColonyService,
          useValue: {
            getColonies: () => of([
              {
                id: 1,
                name: 'Colonia real',
                description: '',
                location: 'Sevilla',
                active: true
              }
            ])
          }
        },
        {
          provide: CatService,
          useValue: {
            getCats: () => of([])
          }
        },
        {
          provide: UserService,
          useValue: {
            getUsers: () => of([])
          }
        }
      ]
    }).compileComponents();
  });

  it('should keep no view while the session is still loading', () => {
    cargando.set(true);
    const component = TestBed.createComponent(AppComponent).componentInstance;

    TestBed.tick();

    expect(component.activeView).toBe('Login');
    expect(component.cargandoSesion()).toBe(true);
  });

  it('should start on the login view', () => {
    const component = TestBed.createComponent(AppComponent).componentInstance;

    TestBed.tick();

    expect(component.activeView).toBe('Login');
  });

  it('should enter the app and load colonies when a session exists', () => {
    const component = TestBed.createComponent(AppComponent).componentInstance;

    usuario.set({
      id: 1,
      nombre: 'Lidia',
      email: 'lidia@example.com',
      rol: 'ADMIN'
    });

    TestBed.tick();

    expect(component.activeView).toBe('Inicio');
    expect(component.filteredColonies.length).toBe(1);
    expect(component.filteredColonies[0].name).toBe('Colonia real');
  });

  it('should change the active section', () => {
    const component = TestBed.createComponent(AppComponent).componentInstance;

    component.selectSection('Gatos');

    expect(component.activeView).toBe('Gatos');
  });
});
