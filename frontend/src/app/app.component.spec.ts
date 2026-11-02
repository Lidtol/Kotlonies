import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { AppComponent } from './app.component';
import { AuthService } from './core/auth.service';
import { ColonyService } from './core/colony.service';

describe('AppComponent', () => {
  let fixture: ComponentFixture<AppComponent>;
  let component: AppComponent;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AppComponent],
      providers: [
        {
          provide: AuthService,
          useValue: {
            usuario: { set: jasmine.createSpy('set') },
            logout: () => of(void 0)
          }
        },
        {
          provide: ColonyService,
          useValue: {
            getColonies: () => of([
              { id: 1, name: 'Colonia real', description: '', location: 'Sevilla', active: true }
            ])
          }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(AppComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create the dashboard', () => {
    expect(component).toBeTruthy();
  });

  it('should start on the login view', () => {
    expect(component.activeView).toBe('Login');
  });

  it('should enter the app after login', () => {
    component.login();
    expect(component.activeView).toBe('Inicio');
  });

  it('should load colonies from the backend service', () => {
    component['colonies'] = [
      { name: 'Colonia real', location: 'Sevilla', cats: 3, volunteers: 1, status: 'Activa', image: '' }
    ];

    expect(component.filteredColonies.length).toBe(1);
    expect(component.filteredColonies[0].name).toBe('Colonia real');
  });

  it('should change the active section', () => {
    component.selectSection('Gatos');
    expect(component.activeView).toBe('Gatos');
  });
});
