import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { LoginComponent } from './features/login/login.component';
import { AuthService } from './core/auth.service';
import { ColonyService } from './core/colony.service';

type CatFilter = 'Todos' | 'Esterilizados' | 'Pendientes';

type Section = 'Inicio' | 'Colonias' | 'Gatos' | 'Voluntariado' | 'Calendario' | 'Perfil';

type View = 'Login' | Section;

interface Cat {
  name: string | null;
  code: string;
  colony: string;
  trait: string;
  sterilized: boolean;
  lastVet: string;
}

interface Colony {
  name: string;
  location: string;
  cats: number;
  volunteers: number;
  status: 'Activa' | 'Pendiente';
  image: string;
}

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule, LoginComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit {
  activeView: View = 'Login';
  searchTerm = '';

  colonies: Colony[] = [];
  cats: Cat[] = [];
  volunteers: { name: string; role: string; roleClass: string; colony: string }[] = [];

  constructor(
    private readonly auth: AuthService,
    private readonly colonyService: ColonyService
  ) {}

  ngOnInit(): void {
    this.loadColonies();
  }

  readonly navigation: { label: Section; icon: string }[] = [
    { label: 'Inicio', icon: '⌂' },
    { label: 'Colonias', icon: '♧' },
    { label: 'Gatos', icon: '♢' },
    { label: 'Voluntariado', icon: '♙' },
    { label: 'Calendario', icon: '▦' },
    { label: 'Perfil', icon: '◉' }
  ];

  get stats() {
    return [
      { label: 'Colonias activas', value: String(this.colonies.length), icon: '⌂', tone: 'violet' },
      { label: 'Gatos registrados', value: String(this.cats.length), icon: '♧', tone: 'pink' },
      { label: 'Personas voluntarias', value: String(this.volunteers.length), icon: '♙', tone: 'blue' },
      { label: 'En adopción', value: '0', icon: '♡', tone: 'peach' }
    ];
  }

  readonly subtitles: Record<Section, string> = {
    Inicio: 'Esto es lo que está pasando hoy en tus colonias.',
    Colonias: 'Todas las colonias que gestionas y su estado.',
    Gatos: 'Fichas de los gatos, incluidos los que aún no tienen nombre.',
    Voluntariado: 'Las personas que cuidan de las colonias y su rol.',
    Calendario: 'Visitas, alimentación y revisiones previstas.',
    Perfil: 'Tus datos y los permisos de cada rol.'
  };

  // Gatos
  catFilter: CatFilter = 'Todos';
  readonly catFilters: CatFilter[] = ['Todos', 'Esterilizados', 'Pendientes'];

  // Voluntariado

  // Calendario
  readonly calendarMonth = 'Calendario';
  readonly todayDay = 1;
  readonly weekDays = ['Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb', 'Dom'];
  readonly events: { day: number; title: string; colony: string; tone: string }[] = [];
  readonly upcomingEvents = this.events.filter(e => e.day >= this.todayDay);
  readonly calendarWeeks: (number | null)[][] = this.buildCalendar(2026, 9);

  // Perfil
  readonly roles: { name: string; description: string }[] = [];

  get filteredColonies(): Colony[] {
    const term = this.searchTerm.trim().toLocaleLowerCase('es');
    if (!term) return this.colonies;
    return this.colonies.filter(colony =>
      colony.name.toLocaleLowerCase('es').includes(term) ||
      colony.location.toLocaleLowerCase('es').includes(term)
    );
  }

  get filteredCats(): Cat[] {
    const term = this.searchTerm.trim().toLocaleLowerCase('es');
    return this.cats.filter(cat => {
      const matchesFilter = this.catFilter === 'Todos' || (this.catFilter === 'Esterilizados') === cat.sterilized;
      const text = `${cat.name ?? 'Sin nombre'} ${cat.code} ${cat.colony}`.toLocaleLowerCase('es');
      return matchesFilter && (!term || text.includes(term));
    });
  }

  eventsOn(day: number) {
    return this.events.filter(event => event.day === day);
  }

  private buildCalendar(year: number, month: number): (number | null)[][] {
    const offset = (new Date(year, month, 1).getDay() + 6) % 7; // semana desde el lunes
    const total = new Date(year, month + 1, 0).getDate();
    const cells: (number | null)[] = [
      ...Array<null>(offset).fill(null),
      ...Array.from({ length: total }, (_, i) => i + 1)
    ];
    while (cells.length % 7 !== 0) cells.push(null);
    const weeks: (number | null)[][] = [];
    for (let i = 0; i < cells.length; i += 7) weeks.push(cells.slice(i, i + 7));
    return weeks;
  }

  selectSection(section: Section): void {
    this.activeView = section;
    this.searchTerm = '';
    this.catFilter = 'Todos';
  }

  // Demostración del login: al autenticarse se entra en Inicio.
  login(): void {
    this.activeView = 'Inicio';
  }

  private loadColonies(): void {
    this.colonyService.getColonies().subscribe({
      next: colonies => {
        this.colonies = colonies.map(colony => ({
          name: colony.name,
          location: colony.location,
          cats: 0,
          volunteers: 0,
          status: colony.active ? 'Activa' : 'Pendiente',
          image: ''
        }));
      },
      error: () => {
        this.colonies = [];
      }
    });
  }

  logout(): void {
    this.auth.logout().subscribe({
      complete: () => this.auth.usuario.set(null)
    });
    this.activeView = 'Login';
  }
}