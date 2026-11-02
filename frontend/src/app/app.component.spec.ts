import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AppComponent } from './app.component';

describe('AppComponent', () => {
  let fixture: ComponentFixture<AppComponent>;
  let component: AppComponent;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AppComponent]
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

  it('should filter colonies by name', () => {
    component.searchTerm = 'playa';
    expect(component.filteredColonies.length).toBe(1);
    expect(component.filteredColonies[0].name).toBe('Colonia Playa');
  });

  it('should change the active section', () => {
    component.selectSection('Gatos');
    expect(component.activeView).toBe('Gatos');
  });
});
