import { TestBed } from '@angular/core/testing';
import { signal } from '@angular/core';
import { NEVER, Subject, of } from 'rxjs';

import { AppComponent } from './app.component';
import { AuthService, UsuarioPublico } from './core/auth.service';
import { ColonyService } from './core/colony.service';
import { CatService } from './core/cat.service';
import { UserService } from './core/user.service';

describe('AppComponent', () => {
  const usuario = signal<UsuarioPublico | null>(null);
  const cargando = signal(false);

  // Peticiones simuladas que nunca resuelven solas: permiten contar cuántas se disparan
  // y abrir o cerrar la respuesta de la colonia a voluntad de cada prueba.
  let createColonyCalls: number;
  let createCatCalls: number;
  let createUserCalls: number;
  let colonySaveResponse: Subject<never>;
  const savedColonyResponse = new Subject<{
    id: number;
    name: string;
    description: string;
    location: string;
    active: boolean;
  }>();

  beforeEach(async () => {
    usuario.set(null);
    cargando.set(false);
    createColonyCalls = 0;
    createCatCalls = 0;
    createUserCalls = 0;
    colonySaveResponse = new Subject<never>();
    savedColonyResponse.next({
      id: 1,
      name: 'Colonia real',
      description: '',
      location: 'Sevilla',
      active: true
    });

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
            ]),
            createColony: () => {
              createColonyCalls++;
              return colonySaveResponse;
            }
          }
        },
        {
          provide: CatService,
          useValue: {
            getCats: () => of([]),
            createCat: () => {
              createCatCalls++;
              return NEVER;
            }
          }
        },
        {
          provide: UserService,
          useValue: {
            getUsers: () => of([]),
            createUser: () => {
              createUserCalls++;
              return NEVER;
            }
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

  it('should send only one request when saving a colony several times', () => {
    const component = TestBed.createComponent(AppComponent).componentInstance;

    component.newColony();
    component.saveColony();
    component.saveColony();
    component.saveColony();

    expect(createColonyCalls).toBe(1);
    expect(component.savingColony).toBe(true);
    expect(component.colonySubmitLabel).toBe('Creando colonia...');
  });

  it('should send only one request when saving a cat several times', () => {
    const component = TestBed.createComponent(AppComponent).componentInstance;

    component.newCat();
    component.saveCat();
    component.saveCat();

    expect(createCatCalls).toBe(1);
    expect(component.savingCat).toBe(true);
    expect(component.catSubmitLabel).toBe('Creando gato...');
  });

  it('should send only one request when saving a user several times', () => {
    usuario.set({ id: 1, nombre: 'Lidia', email: 'lidia@example.com', rol: 'ADMIN' });
    const component = TestBed.createComponent(AppComponent).componentInstance;

    component.newUser();
    component.saveUser();
    component.saveUser();

    expect(createUserCalls).toBe(1);
    expect(component.savingUser).toBe(true);
    expect(component.userSubmitLabel).toBe('Creando acceso...');
  });

  it('should re-enable the button when the request fails', () => {
    const component = TestBed.createComponent(AppComponent).componentInstance;

    component.newColony();
    component.saveColony();
    expect(component.savingColony).toBe(true);

    // La petición falla: el botón vuelve a estar operativo y se muestra el error.
    colonySaveResponse.error({ status: 0 });

    expect(component.savingColony).toBe(false);
    expect(component.colonySubmitLabel).toBe('Guardar colonia');
    expect(component.formError).toBe('No hay conexión con el servidor.');
  });

  it('should re-enable the button and reload the list when the save succeeds', () => {
    const component = TestBed.createComponent(AppComponent).componentInstance;

    component.newColony();
    component.saveColony();
    expect(component.savingColony).toBe(true);

    savedColonyResponse.next({
      id: 2,
      name: 'Colonia nueva',
      description: '',
      location: 'Madrid',
      active: true
    });

    expect(component.savingColony).toBe(false);
    expect(component.colonyFormOpen).toBe(false);
    expect(component.colonySubmitLabel).toBe('Guardar colonia');
  });
});
